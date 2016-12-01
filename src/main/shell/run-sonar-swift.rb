#!/usr/bin/env ruby
#
# Instruments build and tools to generate reports for Sonar. These
# reports are then pushed to a SonarQube project.
#
# Sonar Scanner can use a file on your system to configure itself.
# Ensure this file exists in
# /usr/local/cellar/sonar-scanner/<version>/libexec/conf/sonar-scanner.properties
# (if using HomeBrew to install Sonar Scanner).
require 'fileutils'
require 'java-properties'
require 'logger'
require 'optparse'

# Adds logging capability where included.
module Logging
  def logger
    @logger ||= Logging.logger_for(self.class.name)
  end

  # Use a hash class-ivar to cache a unique Logger per class:
  @@loggers = {}
  @@logger_level = Logger::INFO

  class << self
    def logger_for(classname)
      @@loggers[classname] ||= configure_logger_for(classname)
    end

    def configure_logger_for(classname)
      logger = Logger.new(STDOUT)
      logger.progname = classname
      logger.level = @@logger_level
      logger
    end

    def logger_level=(level)
      @@logger_level = level
    end
  end
end

# Adds a fatal_error method that logs and exit.
# This module expects the Logging module included.
module CanFail
  def fatal_error(msg)
    logger.error(msg)
    exit(false)
  end
end

# PropertiesReader reads and check the sonar-project.properties file.
# It also creates a Hash with all read properties.
class PropertiesReader
  include Logging
  include CanFail

  def initialize(file)
    fatal_error('No sonar-project.properties in current directory') unless File.exist?(file)
    @file = file
  end

  # Read the Java properties file and return a Hash suitable for the script.
  def read
    properties = JavaProperties.load(@file)
    options = read_properties(properties)
    validate_settings!(options)
  end

  private

  # Map the Java properties hash to more Ruby hash
  def read_properties(properties)
    options = {}
    options[:project] = properties[:'sonar.swift.project']
    options[:workspace] = properties[:'sonar.swift.workspace']
    options[:sources] = properties[:'sonar.sources'].split(',')
    options[:scheme] = properties[:'sonar.swift.appScheme']
    options[:configuration] = properties[:'sonar.swift.appConfiguration']
    options[:simulator] = properties[:'sonar.swift.simulator']
    options[:exclude_from_coverage] = properties[:'sonar.swift.excludedPathsFromCoverage'].split(',')
    options
  end

  def validate_settings!(options)
    fatal_error('No project or workspace specified in sonar-project.properties.') if (options[:workspace].nil? && options[:project].nil?)
    fatal_error('No sources folder specified in sonar-project.properties') if options[:sources].nil?
    fatal_error('No scheme specified in sonar-project.properties') if options[:scheme].nil?
    if options[:configuration].nil?
      logger.warn('No build configuration set in sonar-project.properties, defaulting to Debug')
      options[:configuration] = 'Debug'
    end
    options
  end
end

# A base class for tool wrappers.
#
# Mainly defines a common interface + includes some useful modules.
class Tool
  include Logging
  include CanFail

  def self.command
    '<not specified>'
  end

  def initialize(_options)
    validate_settings!
  end

  def run
  end

  protected

  def validate_settings!
  end
end

# Runs unit tests using Xcode with `xcodebuild`, and slather to report
# the code coverage.
class UnitTests < Tool
  def self.command
    'xcodebuild'
  end

  def initialize(options)
    @workspace = options[:workspace]
    @project = options[:project]
    @scheme = options[:scheme]
    @configuration = options[:configuration]
    @simulator = options[:simulator]

    @exclude_from_coverage = options[:exclude_from_coverage]

    super(options)
  end

  def run
    logger.info('Running clean/build/test')
    build_and_test

    logger.info('Running Slather...')
    compute_coverage
  end

  private

  def validate_settings!
    # @workspace is optional
    fatal_error('A project must be set in order to compute coverage') if @project.nil?
    fatal_error('A scheme must be set in order to build and test the app') if @scheme.nil?
    fatal_error('A configuration must be set in order to build and test the app') if @configuration.nil?
    logger.warn('No simulator specified in sonar-project.properties') if @simulator.nil?
    # @exclude_from_coverage is optional
  end

  # Run tests with Xcode
  def build_and_test
    container = if @project
                  "-project \"#{@project}\""
                else
                  "-workspace \"#{@workspace}\""
                end

    xcode_cmd = "xcodebuild clean build test #{container} -scheme \"#{@scheme}\" -configuration \"#{@configuration}\" -enableCodeCoverage YES"
    unless @simulator.nil?
      xcode_cmd += " -destination '#{@simulator}' -destination-timeout 60"
    end

    cmd = "set -o pipefail && #{xcode_cmd} | tee xcodebuild.log | xcpretty -t -r junit -o sonar-reports/TEST-report.xml"
    logger.debug("Will run #{cmd}")
    system(cmd)
  end

  def compute_coverage
    exclusion = @exclude_from_coverage.join(' -i ')
    exclusion = "-i #{exclusion}" unless exclusion.empty?

    cmd = "slather coverage --input-format profdata #{exclusion} --cobertura-xml --output-directory sonar-reports"
    cmd += " --workspace #{@workspace}" unless @workspace.nil?
    cmd += " --scheme #{@scheme} #{@project}"

    logger.debug("Slather command: #{cmd}")
    system(cmd)

    FileUtils.mv('sonar-reports/cobertura.xml', 'sonar-reports/coverage.xml')
  end
end

# SwiftLint checks code style and conventions.
#
# It is mainly based on the [Swift Style
# Guide](https://github.com/github/swift-style-guide) and it may also be
# used to enforce custom conventions.
#
# https://github.com/realm/SwiftLint
class SwiftLint < Tool
  def self.command
    'swiftlint'
  end

  def initialize(options)
    @sources = options[:sources]
    super(options)
  end

  def run
    logger.info('Running SwiftLint...')
    @sources.each do |source|
      report_name = "#{source.tr(' ', '_')}-swiftlint.txt"
      cmd = "swiftlint lint --path #{source} > sonar-reports/#{report_name}"
      logger.debug("Will run #{cmd}")
      system(cmd)
    end
  end

  private

  def validate_settings!
    fatal_error('Sources must be set') if @sources.nil?
  end
end

# Lizard computes the code complexity.
#
# @see http://www.lizard.ws
class Lizard < Tool
  def self.command
    'lizard'
  end

  def initialize(options)
    @sources = options[:sources]
    super(options)
  end

  def run
    logger.info('Running Lizard...')
    cmd = "lizard --xml #{@sources.join(' ')} > sonar-reports/lizard-reports.xml"
    logger.info("Will run #{cmd}")
    system(cmd)
  end

  private

  def validate_settings!
    fatal_error('Sources must be set') if @sources.nil?
  end
end

# Entry point of the script.
#
# It reads the configurations, run all analytic tools and send reports
# to Sonar.
class RunSonarSwift
  include Logging

  def run
    tools = [UnitTests, SwiftLint, Lizard]
    upload = true

    # Read command line options
    OptionParser.new do |opt|
      opt.on('-v', '--verbose', 'Vervose mode') { |_| Logging.logger_level = Logger::DEBUG }
      opt.on('--disable-upload', 'Only execute tools, do not call Sonar Scanner. Useful to ensure all tools are configured') do |_|
        upload = false
      end
      opt.on('--disable-unit-tests', 'Disable unit tests') { |_| tools.delete(UnitTests) }
      opt.on('--disable-swiftlint', 'Disable SwiftLint') { |_| tools.delete(SwiftLint) }
      opt.on('--disable-lizard', 'Disable Lizard') { |_| tools.delete(Lizard) }
    end.parse!

    # Read properties
    opts_reader = PropertiesReader.new('sonar-project.properties')
    options = opts_reader.read

    # Initiate reports
    bootstrap_reports_folder
    bootstrap_mandatory_reports

    # Filter tools by availability
    tools = available_tools(tools)

    # Call tools
    tools.each do |tool|
      tool.new(options).run
    end

    # Send reports
    send_reports if upload
  end

  private

  # Put default xml files with no tests and no coverage. This is needed to
  # ensure a file is present, either the Xcode build test step worked or
  # not. Without this, Sonar Scanner will fail uploading results.
  def bootstrap_mandatory_reports
    empty_test_report = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><testsuites name='AllTestUnits'></testsuites>"
    File.write('sonar-reports/TEST-report.xml', empty_test_report)

    empty_coverage_report = "<?xml version='1.0' ?><!DOCTYPE coverage SYSTEM 'http://cobertura.sourceforge.net/xml/coverage-03.dtd'><coverage><sources></sources><packages></packages></coverage>"
    File.write('sonar-reports/coverage.xml', empty_coverage_report)
  end

  def bootstrap_reports_folder
    logger.info('Deleting and creating directory sonar-reports/')
    FileUtils.rm_rf('sonar-reports')
    Dir.mkdir('sonar-reports')
  end

  # Check each tool is available and return an updated list of tool.
  def available_tools(tools)
    tools.select do |tool|
      if command? tool.command
        true
      else
        logger.warn("#{tool.command} is not found in PATH and won't be available in reports.")
        false
      end
    end
  end

  # Check if command is available in PATH
  def command?(command)
    system("which #{command} 2>&1 > /dev/null")
  end

  def send_reports
    if command? 'sonar-scanner'
      system('sonar-scanner')
    else
      system('sonar-runner')
    end
  end
end

RunSonarSwift.new.run
