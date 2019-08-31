require_relative 'logging'
require_relative 'canfail'
require 'fileutils'
require 'java-properties'

# SonarPropertiesReader reads and check the sonar-project.properties file.
# It also creates a Hash with all read properties.
class SonarPropertiesReader
  include Logging
  include CanFail

  def initialize(path)
    fatal_error("No #{path} found") unless File.exist?(path)
    @file = path
  end

  # Read the Java properties file and return a Hash suitable for the script.
  def read
	logger.info('Reading Sonar project properties...')
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
    fatal_error('No project or workspace specified.') if (options[:workspace].nil? && options[:project].nil?)
    fatal_error('No sources folder specified.') if options[:sources].nil?
    fatal_error('No scheme specified.') if options[:scheme].nil?
    if options[:configuration].nil?
      logger.warn('No build configuration set, defaulting to Debug')
      options[:configuration] = 'Debug'
    end
    options
  end
end