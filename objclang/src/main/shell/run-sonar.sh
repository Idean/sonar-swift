#### run-sonar.sh ####
#!/bin/bash
#
# Objective-C Language - Enables analysis of Swift and Objective-C projects into SonarQube.
# Copyright © 2015 Backelite (${email})
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

## INSTALLATION: script to copy in your Xcode project in the same directory as the .xcodeproj file
## USAGE: ./run-sonar.sh
## DEBUG: ./run-sonar.sh -v
## WARNING: edit your project parameters in sonar-project.properties rather than modifying this script
#

# Global parameters
XCODEBUILD_CMD=xcodebuild
SLATHER_CMD=slather
XCPRETTY_CMD=xcpretty
LIZARD_CMD=lizard

trap "echo 'Script interrupted by Ctrl+C'; stopProgress; exit 1" SIGHUP SIGINT SIGTERM

function startProgress() {
	while true
	do
    	echo -n "."
	    sleep 5
	done
}

function stopProgress() {
	if [ "$vflag" = "" -a "$nflag" = "" ]; then
		kill $PROGRESS_PID &>/dev/null
	fi
}

function testIsInstalled() {

	hash $1 2>/dev/null
	if [ $? -eq 1 ]; then
		echo >&2 "ERROR - $1 is not installed or not in your PATH"; exit 1;
	fi
}

function testIsXcodeMinMajorVersionAvailable() {    

    XCODE_VERSION="$($XCODEBUILD_CMD -version | grep -a -A 1 "Xcode" | head -n1 | sed "s/Xcode \([0-9]*\)\..*/\1/")"
    if (( "$1" <= "$XCODE_VERSION" )); then
        return 0
    else
        return 1
    fi
}

function readParameter() {

	variable=$1
	shift
	parameter=$1
	shift

	eval $variable="\"$(sed '/^\#/d' sonar-project.properties | grep $parameter | tail -n 1 | cut -d '=' -f2- | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')\""
}

# Run a set of commands with logging and error handling
function runCommand() {

	# 1st arg: redirect stdout
	# 2nd arg: command to run
	# 3rd..nth arg: args
	redirect=$1
	shift

	command=$1
	shift

	if [ "$nflag" = "on" ]; then
		# don't execute command, just echo it
		echo
		if [ "$redirect" = "/dev/stdout" ]; then
			if [ "$vflag" = "on" ]; then
				echo "+" $command "$@"
			else
				echo "+" $command "$@" "> /dev/null"
			fi
		elif [ "$redirect" != "no" ]; then
			echo "+" $command "$@" "> $redirect"
		else
			echo "+" $command "$@"
		fi

	elif [ "$vflag" = "on" ]; then
		echo

		if [ "$redirect" = "/dev/stdout" ]; then
			set -x #echo on
			$command "$@"
			returnValue=$?
			set +x #echo off
		elif [ "$redirect" != "no" ]; then
			set -x #echo on
			$command "$@" > $redirect
			returnValue=$?
			set +x #echo off
		else
			set -x #echo on
			$command "$@"
			returnValue=$?
			set +x #echo off
		fi

		if [[ $returnValue != 0 && $returnValue != 5 ]] ; then
			stopProgress
			echo "ERROR - Command '$command $@' failed with error code: $returnValue"
			exit $returnValue
		fi
	else

		if [ "$redirect" = "/dev/stdout" ]; then
			$command "$@" > /dev/null
		elif [ "$redirect" != "no" ]; then
			$command "$@" > $redirect
		else
			$command "$@"
		fi

        returnValue=$?
		if [[ $returnValue != 0 && $returnValue != 5 ]] ; then
			stopProgress
			echo "ERROR - Command '$command $@' failed with error code: $returnValue"
			exit $?
		fi


		echo
	fi
}

## COMMAND LINE OPTIONS
vflag=""
nflag=""
unittests="on"
oclint="on"
fauxpas="on"
lizard="on"
sonarscanner=""

while [ $# -gt 0 ]
do
    case "$1" in
    -v)	vflag=on;;
    -n) nflag=on;;
    -nounittests) unittests="";;
    -nooclint) oclint="";;
    -nofauxpas) fauxpas="";;
    -usesonarscanner) sonarscanner="on";;
	--)	shift; break;;
	-*)
        echo >&2 "Usage: $0 [-v]"
		exit 1;;
	*)	break;;		# terminate while loop
    esac
    shift
done

# Usage OK
echo "Running run-sonar.sh..."

## CHECK PREREQUISITES

# xctool, oclint installed
testIsInstalled $XCODEBUILD_CMD
testIsInstalled oclint

# sonar-project.properties in current directory
if [ ! -f sonar-project.properties ]; then
	echo >&2 "ERROR - No sonar-project.properties in current directory"; exit 1;
fi

## READ PARAMETERS from sonar-project.properties

# Read destination simulator
destinationSimulator=''; readParameter destinationSimulator 'sonar.objectivec.simulator'

# Your .xcworkspace/.xcodeproj filename
workspaceFile=''; readParameter workspaceFile 'sonar.objectivec.workspace'
projectFile=''; readParameter projectFile 'sonar.objectivec.project'

# Count projects
projectCount=$(echo $projectFile | sed -n 1'p' | tr ',' '\n' | wc -l | tr -d '[[:space:]]')
if [ "$vflag" = "on" ]; then
    echo "Project count is [$projectCount]"
fi

# Source directories for .h/.m files
srcDirs=''; readParameter srcDirs 'sonar.sources'
# The name of your application scheme in Xcode
appScheme=''; readParameter appScheme 'sonar.objectivec.appScheme'

# The name of your test scheme in Xcode
testScheme=''; readParameter testScheme 'sonar.objectivec.testScheme'
# The file patterns to exclude from coverage report
excludedPathsFromCoverage=''; readParameter excludedPathsFromCoverage 'sonar.objectivec.excludedPathsFromCoverage'
# Read coverage type
coverageType=''; readParameter coverageType 'sonar.objectivec.coverageType'


# Check for mandatory parameters
if [ -z "$projectFile" -o "$projectFile" = " " ]; then

	if [ ! -z "$workspaceFile" -a "$workspaceFile" != " " ]; then
		echo >&2 "ERROR - sonar.objectivec.project parameter is missing in sonar-project.properties. You must specify which projects (comma-separated list) are application code within the workspace $workspaceFile."
	else
		echo >&2 "ERROR - sonar.objectivec.project parameter is missing in sonar-project.properties (name of your .xcodeproj)"
	fi
	exit 1
fi
if [ -z "$srcDirs" -o "$srcDirs" = " " ]; then
	echo >&2 "ERROR - sonar.sources parameter is missing in sonar-project.properties. You must specify which directories contain your .h/.m source files (comma-separated list)."
	exit 1
fi
if [ -z "$appScheme" -o "$appScheme" = " " ]; then
	echo >&2 "ERROR - sonar.objectivec.appScheme parameter is missing in sonar-project.properties. You must specify which scheme is used to build your application."
	exit 1
fi
if [ -z "$destinationSimulator" -o "$destinationSimulator" = " " ]; then
	echo >&2 "ERROR - sonar.objectivec.simulator parameter is missing in sonar-project.properties. You must specify which simulator to use."
	exit 1
fi

if [ "$vflag" = "on" ]; then
 	echo "Xcode workspace file is: $workspaceFile"
 	echo "Xcode project file is: $projectFile"
 	echo "Xcode application scheme is: $appScheme"
 	echo "Xcode test scheme is: $testScheme"
 	echo "Excluded paths from coverage are: $excludedPathsFromCoverage"
fi

## SCRIPT

# Start progress indicator in the background
if [ "$vflag" = "" -a "$nflag" = "" ]; then
	startProgress &
	# Save PID
	PROGRESS_PID=$!
fi

# Create sonar-reports/ for reports output
if [ "$vflag" = "on" ]; then
    echo 'Creating directory sonar-reports/'
fi
rm -rf sonar-reports
mkdir sonar-reports

# Extracting project information needed later
echo -n 'Extracting Xcode project information'
if [[ "$workspaceFile" != "" ]] ; then
    buildCmdPrefix="-workspace $workspaceFile"
else
    buildCmdPrefix="-project $projectFile"
fi
buildCmd=($XCODEBUILD_CMD clean build $buildCmdPrefix -scheme $appScheme)
if [[ ! -z "$destinationSimulator" ]]; then
    buildCmd+=(-destination "$destinationSimulator" -destination-timeout 360 COMPILER_INDEX_STORE_ENABLE=NO)
fi
runCommand  xcodebuild.log "${buildCmd[@]}"
#oclint-xcodebuild # Transform the xcodebuild.log file into a compile_command.json file
cat xcodebuild.log | $XCPRETTY_CMD -r json-compilation-database -o compile_commands.json

# Unit surefire and coverage
if [ "$testScheme" = "" ] || [ "$unittests" = "" ]; then
	echo 'Skipping surefire!'

	# Put default xml files with no surefire and no coverage...
	echo "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><testsuites name='AllTestUnits'></testsuites>" > sonar-reports/TEST-report.xml
	echo "<?xml version='1.0' ?><!DOCTYPE coverage SYSTEM 'http://cobertura.sourceforge.net/xml/coverage-03.dtd'><coverage><sources></sources><packages></packages></coverage>" > sonar-reports/coverage.xml
else

    echo -n 'Running surefire'

    if [ "$coverageType" = "profdata" -o "$coverageType" = "" ]; then
    	# profdata
    	buildCmd=($XCODEBUILD_CMD test $buildCmdPrefix -scheme "$testScheme" -configuration Debug -enableCodeCoverage YES)
        xcode8BuildForTestingCmd=($XCODEBUILD_CMD build-for-testing $buildCmdPrefix -scheme "$testScheme" -configuration Debug -enableCodeCoverage YES)
        xcode8TestCmd=($XCODEBUILD_CMD test-without-building $buildCmdPrefix -scheme "$testScheme" -configuration Debug -enableCodeCoverage YES)
    else
    	# Legacy coverage
    	buildCmd=($XCODEBUILD_CMD test $buildCmdPrefix -scheme "$testScheme" -configuration Debug)
    	xcode8BuildForTestingCmd=($XCODEBUILD_CMD build-for-testing $buildCmdPrefix -scheme "$testScheme" -configuration Debug)
        xcode8TestCmd=($XCODEBUILD_CMD test-without-building $buildCmdPrefix -scheme "$testScheme" -configuration Debug)
    fi

    if [[ ! -z "$destinationSimulator" ]]; then
        buildCmd+=(-destination "$destinationSimulator" -destination-timeout 360)
        xcode8BuildForTestingCmd+=(-destination "$destinationSimulator" -destination-timeout 360)
        xcode8TestCmd+=(-destination "$destinationSimulator" -destination-timeout 360)
    fi
    
    if testIsXcodeMinMajorVersionAvailable 8 ; then
        echo "Running build-for-testing"
        "${xcode8BuildForTestingCmd[@]}"  | $XCPRETTY_CMD
        echo "Running test-without-building"
        "${xcode8TestCmd[@]}"  | $XCPRETTY_CMD -t --report junit
    else
        echo "Testing"
        "${buildCmd[@]}"  | $XCPRETTY_CMD -t --report junit
    fi

    mv build/reports/junit.xml sonar-reports/TEST-report.xml

	echo -n 'Computing coverage report'

	if [ "$coverageType" = "profdata" -o "$coverageType" = "" ]; then

	    # profdata = use slather

	    echo 'Using profdata'

	    # Build the --exclude flags
        excludedCommandLineFlags=""
        if [ ! -z "$excludedPathsFromCoverage" -a "$excludedPathsFromCoverage" != " " ]; then
            echo $excludedPathsFromCoverage | sed -n 1'p' | tr ',' '\n' > tmpFileRunSonarSh2
            while read word; do
                excludedCommandLineFlags+=" -i $word"
            done < tmpFileRunSonarSh2
            rm -rf tmpFileRunSonarSh2
        fi
        if [ "$vflag" = "on" ]; then
            echo "Command line exclusion flags for slather is:$excludedCommandLineFlags"
        fi


		firstProject=$(echo $projectFile | sed -n 1'p' | tr ',' '\n' | head -n 1)

        slatherCmd=($SLATHER_CMD coverage --input-format profdata $excludedCommandLineFlags --cobertura-xml --output-directory sonar-reports)
		if [[ ! -z "$workspaceFile" ]]; then
			slatherCmd+=( --workspace "$workspaceFile")
		fi
		slatherCmd+=( --scheme "$appScheme" "$firstProject")

		runCommand /dev/stdout "${slatherCmd[@]}"
		mv sonar-reports/cobertura.xml sonar-reports/coverage.xml

	else

	    # Legacy mode = use gcovr

		# Build the --exclude flags
		excludedCommandLineFlags=""
		if [ ! -z "$excludedPathsFromCoverage" -a "$excludedPathsFromCoverage" != " " ]; then
			echo $excludedPathsFromCoverage | sed -n 1'p' | tr ',' '\n' > tmpFileRunSonarSh2
			while read word; do
				excludedCommandLineFlags+=" --exclude $word"
			done < tmpFileRunSonarSh2
			rm -rf tmpFileRunSonarSh2
		fi
		if [ "$vflag" = "on" ]; then
			echo "Command line exclusion flags for gcovr is:$excludedCommandLineFlags"
		fi

		# Create symlink on the build directory to enable its access from the workspace
		coverageFilesPath=$(grep 'command' compile_commands.json | sed 's#^.*-o \\/#\/#;s#",##' | grep "${projectName%%.*}.build" | awk 'NR<2' | sed 's/\\\//\//g' | sed 's/\\\\//g' | xargs -0 dirname)
		splitIndex=$(awk -v a="$coverageFilesPath" -v b="/Intermediates" 'BEGIN{print index(a,b)}')
		coverageFilesPath=$(echo ${coverageFilesPath:0:$splitIndex}Intermediates)
		ln -s $coverageFilesPath sonar-reports/build

		# Run gcovr with the right options
		runCommand "sonar-reports/coverage.xml" gcovr -r . $excludedCommandLineFlags --xml

	fi


fi

if [ "$oclint" = "on" ]; then

	# OCLint
	echo -n 'Running OCLint...'

	# Options
	maxPriority=10000
    longLineThreshold=250

	# Build the --include flags
	currentDirectory=${PWD##*/}
	echo "$srcDirs" | sed -n 1'p' | tr ',' '\n' > tmpFileRunSonarSh
	while read word; do

		includedCommandLineFlags=" --include .*/${currentDirectory}/${word}"
		if [ "$vflag" = "on" ]; then
            echo
            echo -n "Path included in oclint analysis is:$includedCommandLineFlags"
        fi
		# Run OCLint with the right set of compiler options
	    runCommand no oclint-json-compilation-database -v $includedCommandLineFlags -- -rc LONG_LINE=$longLineThreshold -max-priority-1 $maxPriority -max-priority-2 $maxPriority -max-priority-3 $maxPriority -report-type pmd -o sonar-reports/$(echo $word | sed 's/\//_/g')-oclint.xml

	done < tmpFileRunSonarSh
	rm -rf tmpFileRunSonarSh


else
	echo 'Skipping OCLint (test purposes only!)'
fi

if [ "$fauxpas" = "on" ]; then
    hash fauxpas 2>/dev/null
    if [ $? -eq 0 ]; then

        #FauxPas
        echo -n 'Running FauxPas...'

        if [ "$projectCount" = "1" ]
        then

            fauxpas -o json check $projectFile --workspace $workspaceFile --scheme $appScheme > sonar-reports/fauxpas.json


        else

            echo $projectFile | sed -n 1'p' | tr ',' '\n' > tmpFileRunSonarSh
            while read projectName; do

                $XCODEBUILD_CMD -list -project $projectName | sed -n '/Schemes/,$p' | while read scheme
                do

                if [ "$scheme" = "" ]
                then
                exit
                fi

                if [ "$scheme" == "${scheme/Schemes/}" ]
                then
                    if [ "$scheme" != "$testScheme" ]
                    then
                        projectBaseDir=$(dirname $projectName)
                        workspaceRelativePath=$(python -c "import os.path; print os.path.relpath('$workspaceFile', '$projectBaseDir')")
                        fauxpas -o json check $projectName --workspace $workspaceRelativePath --scheme $scheme > sonar-reports/$(basename $projectName .xcodeproj)-$scheme-fauxpas.json
                    fi
                fi

                done

            done < tmpFileRunSonarSh
            rm -rf tmpFileRunSonarSh

	    fi

    else
        echo 'Skipping FauxPas (not installed)'
    fi
else
    echo 'Skipping FauxPas'
fi

# Lizard Complexity
if [ "$lizard" = "on" ]; then
	if hash $LIZARD_CMD 2>/dev/null; then
		echo -n 'Running Lizard...'
  		$LIZARD_CMD --xml "$srcDirs" > sonar-reports/lizard-report.xml
  	else
  		echo 'Skipping Lizard (not installed!)'
  	fi
else
 	echo 'Skipping Lizard (test purposes only!)'
fi

# SonarQube
if [ "$sonarscanner" = "on" ]; then
    echo -n 'Running SonarQube using SonarQube Scanner'
    if hash /dev/stdout sonar-scanner 2>/dev/null; then
        runCommand /dev/stdout sonar-scanner
    else
        echo 'Skipping sonar-scanner (not installed!)'
    fi
else
    echo -n 'Running SonarQube using SonarQube Runner'
    if hash /dev/stdout sonar-runner 2>/dev/null; then
        runCommand /dev/stdout sonar-runner 
    else
        runCommand /dev/stdout sonar-scanner
    fi
fi

# Kill progress indicator
stopProgress

exit 0
