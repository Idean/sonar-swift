#!/bin/bash
## INSTALLATION: script to copy in your Xcode project in the same directory as the .xcodeproj file
## USAGE: ./run-sonar-swift.sh
## DEBUG: ./run-sonar-swift.sh -v
## WARNING: edit your project parameters in sonar-project.properties rather than modifying this script
#

# Global parameters
XCTOOL_CMD=xctool
SLATHER_CMD=slather
SWIFTLINT_CMD=swiftlint
XCPRETTY_CMD=xcpretty


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
swiftlint="on"
while [ $# -gt 0 ]
do
    case "$1" in
    -v)	vflag=on;;
    -n) nflag=on;;
	-noswiftlint) swiftlint="";;
	--)	shift; break;;
	-*)
        echo >&2 "Usage: $0 [-v]"
		exit 1;;
	*)	break;;		# terminate while loop
    esac
    shift
done

# Usage OK
echo "Running run-sonar-swift.sh..."

## CHECK PREREQUISITES

# sonar-project.properties in current directory
if [ ! -f sonar-project.properties ]; then
	echo >&2 "ERROR - No sonar-project.properties in current directory"; exit 1;
fi

## READ PARAMETERS from sonar-project.properties

#.xcodeproj filename
projectFile=''; readParameter projectFile 'sonar.swift.project'
workspaceFile=''; readParameter workspaceFile 'sonar.swift.workspace'

# Count projects
projectCount=$(echo $projectFile | sed -n 1'p' | tr ',' '\n' | wc -l | tr -d '[[:space:]]')
if [ "$vflag" = "on" ]; then
    echo "Project count is [$projectCount]"
fi

# Source directories for .swift files
srcDirs=''; readParameter srcDirs 'sonar.sources'
# The name of your application scheme in Xcode
appScheme=''; readParameter appScheme 'sonar.swift.appScheme'

# Read destination simulator
destinationSimulator=''; readParameter destinationSimulator 'sonar.swift.simulator'


# The file patterns to exclude from coverage report
excludedPathsFromCoverage=''; readParameter excludedPathsFromCoverage 'sonar.swift.excludedPathsFromCoverage'

# Check for mandatory parameters
if [ -z "$projectFile" -o "$projectFile" = " " ]; then
	echo >&2 "ERROR - sonar.swift.project parameter is missing in sonar-project.properties. You must specify which projects (comma-separated list) are application code."
	exit 1
fi
if [ -z "$workspaceFile" -o "$workspaceFile" = " " ]; then
	echo >&2 "ERROR - sonar.swift.workspace parameter is missing in sonar-project.properties. You must specify which workspace to use."
	exit 1
fi
if [ -z "$srcDirs" -o "$srcDirs" = " " ]; then
	echo >&2 "ERROR - sonar.sources parameter is missing in sonar-project.properties. You must specify which directories contain your .swift source files (comma-separated list)."
	exit 1
fi
if [ -z "$appScheme" -o "$appScheme" = " " ]; then
	echo >&2 "ERROR - sonar.swift.appScheme parameter is missing in sonar-project.properties. You must specify which scheme is used to build your application."
	exit 1
fi
if [ -z "$destinationSimulator" -o "$destinationSimulator" = " " ]; then
	echo >&2 "ERROR - sonar.swift.simulator parameter is missing in sonar-project.properties. You must specify which simulator to use."
	exit 1
fi

if [ "$vflag" = "on" ]; then
 	echo "Xcode project file is: $projectFile"
 	echo "Xcode application scheme is: $appScheme"
 	echo "Destination simulator is: $destinationSimulator"
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

# Unit tests and coverage

# Put default xml files with no tests and no coverage...
echo "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><testsuites name='AllTestUnits'></testsuites>" > sonar-reports/TEST-report.xml
echo "<?xml version='1.0' ?><!DOCTYPE coverage SYSTEM 'http://cobertura.sourceforge.net/xml/coverage-03.dtd'><coverage><sources></sources><packages></packages></coverage>" > sonar-reports/coverage.xml

echo -n 'Running tests'
runCommand /dev/stdout xcodebuild clean -workspace $workspaceFile -scheme $appScheme
buildCmd=(xcodebuild test -workspace $workspaceFile -scheme $appScheme -sdk iphonesimulator -configuration Debug -enableCodeCoverage YES)
if [[ ! -z "$destinationSimulator" ]]; then
    buildCmd+=(-destination "$destinationSimulator" -destination-timeout 60)
fi
runCommand  sonar-reports/xcodebuild.log "${buildCmd[@]}"
cat sonar-reports/xcodebuild.log  | $XCPRETTY_CMD -t --report junit
mv build/reports/junit.xml sonar-reports/TEST-report.xml


echo -n 'Computing coverage report'

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

runCommand /dev/stdout $SLATHER_CMD coverage --input-format profdata $excludedCommandLineFlags --cobertura-xml --output-directory sonar-reports --scheme $appScheme $projectFile
mv sonar-reports/cobertura.xml sonar-reports/coverage.xml


# SwiftLint
if [ "$swiftlint" = "on" ]; then

	echo -n 'Running SwiftLint...'


	# Build the --include flags
	currentDirectory=${PWD##*/}
	echo "$srcDirs" | sed -n 1'p' | tr ',' '\n' > tmpFileRunSonarSh
	while read word; do

		# Run SwiftLint command
	    $SWIFTLINT_CMD > sonar-reports/$(echo $word | sed 's/\//_/g')-swiftlint.txt

	done < tmpFileRunSonarSh
	rm -rf tmpFileRunSonarSh


else
	echo 'Skipping SwiftLint (test purposes only!)'
fi

# SonarQube
echo -n 'Running SonarQube using SonarQube Runner'
runCommand /dev/stdout sonar-runner
	
# Kill progress indicator
stopProgress

exit 0
