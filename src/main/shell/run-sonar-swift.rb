#!/usr/bin/env ruby
#
# Instruments build and tools to generate reports for Sonar. These
# reports are then pushed to a SonarQube project.
#
# Sonar Scanner can use a file on your system to configure itself.
# Ensure this file exists in
# /usr/local/cellar/sonar-scanner/<version>/libexec/conf/sonar-scanner.properties
# (if using HomeBrew to install Sonar Scanner).
#
# TODO: add an arg to disable unit tests execution and reports
# TODO: add command args to disable tools/steps
# TODO: disable tool if not installed and show a warning message
require 'fileutils'
require 'java-properties'
require 'logger'

logger = Logger.new(STDOUT)
logger.level = Logger::DEBUG

def fatal_error(msg)
  logger.error(msg)
  exit(false)
end

## Initialization

props_file = 'sonar-project.properties'
unless File.exist?(props_file)
  fatal_error 'No sonar-project.properties in current directory'
end

properties = JavaProperties.load(props_file)

project = properties[:'sonar.swift.project']
workspace = properties[:'sonar.swift.workspace']
sources = properties[:'sonar.sources'].split(',')
scheme = properties[:'sonar.swift.appScheme']
configuration = properties[:'sonar.swift.appConfiguration'] || 'Debug'

fatal_error('No project or workspace specified in sonar-project.properties.') if (workspace.nil? && project.nil?)
fatal_error('No sources folder specified in sonar-project.properties') if sources.nil?
fatal_error('No scheme specified in sonar-project.properties') if scheme.nil?
logger.warn('No build configuration set in sonar-project.properties, defaulting to Debug') if properties[:'sonar.swift.appConfiguration'].nil?

## Tools and unit tests

logger.info('Deleting and creating directory sonar-reports/')
FileUtils.rm_rf('sonar-reports')
Dir.mkdir('sonar-reports')

### Unit Tests
# TODO: check unit tests are enabled

simulator = properties[:'sonar.swift.simulator']
logger.warn('No simulator specified in sonar-project.properties') if simulator.nil?

# Put default xml files with no tests and no coverage. This is neeided to
# ensure a file is present, either the Xcode build test step worked or
# not. Without this, Sonar Scanner will fail uploading results.
File.write('sonar-reports/TEST-report.xml', "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><testsuites name='AllTestUnits'></testsuites>")
File.write('sonar-reports/coverage.xml', "<?xml version='1.0' ?><!DOCTYPE coverage SYSTEM 'http://cobertura.sourceforge.net/xml/coverage-03.dtd'><coverage><sources></sources><packages></packages></coverage>")

logger.info('Running clean/build/test')

# Run tests with Xcode
prj_or_wspc = if project
                "-project \"#{project}\""
              else
                "-workspace \"#{workspace}\""
              end

cmd = "xcodebuild clean build test #{prj_or_wspc} -scheme \"#{scheme}\" -configuration \"#{configuration}\" -enableCodeCoverage YES"
unless simulator.nil?
  cmd += " -destination '#{simulator}' -destination-timeout 60"
end

full_cmd = "set -o pipefail && #{cmd} | tee xcodebuild.log | xcpretty -t -r junit -o sonar-reports/TEST-report.xml"
logger.debug("Will run #{full_cmd}")
system(full_cmd)

### Compute coverage with Slather
logger.info('Running Slather...')

# TODO: could we automatically find the project to use?
fatal_error('a project must be set in order to compute coverage') if project.nil?

exclude_from_coverage = properties[:'sonar.swift.excludedPathsFromCoverage']
exclusion = exclude_from_coverage.split(',').join(' -i ')
exclusion = "-i #{exclusion}" unless exclusion.empty?

cmd = "slather coverage --input-format profdata #{exclusion} --cobertura-xml --output-directory sonar-reports"
cmd += " --workspace #{workspace}" unless workspace.nil?
cmd += " --scheme #{scheme} #{project}"
logger.debug("Slather command: #{cmd}")

system(cmd)
FileUtils.mv('sonar-reports/cobertura.xml', 'sonar-reports/coverage.xml')

### SwiftLint
logger.info('Running SwiftLint...')
sources.each do |source|
  system("swiftlint lint --path #{source} > sonar-reports/#{source.gsub(' ', '_')}-swiftlint.txt")
end


### Lizard
logger.info('Running Lizard...')
system("lizard --xml #{sources} > sonar-reports/lizard-reports.xml")

## Sending to Sonar
system('sonar-scanner')

