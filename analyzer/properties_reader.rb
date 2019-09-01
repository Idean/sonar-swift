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
	options[:sources] = properties[:'sonar.sources']
	options[:sources] = options[:sources].split(',') unless options[:sources].nil?
	options[:scheme] = properties[:'sonar.swift.appScheme']
    options[:configuration] = properties[:'sonar.swift.appConfiguration']
    options[:simulator] = properties[:'sonar.swift.simulator']
	options[:exclude_from_coverage] = properties[:'sonar.swift.excludedPathsFromCoverage']
	options[:exclude_from_coverage] = options[:exclude_from_coverage].split(',') unless options[:exclude_from_coverage].nil?
	options[:binary_names] = properties[:'sonar.coverage.binaryNames']
	options[:binary_names] = options[:binary_names].split(',') unless options[:binary_names].nil?
    options
  end

  def validate_settings!(options)
	fatal_error("No project or workspace specified in #{@file}") if (options[:workspace].nil? && options[:project].nil?)
	check_file(options[:workspace])
	check_file(options[:project])
	fatal_error("No sources folder specified in #{@file}") if options[:sources].nil?
	options[:sources].each do |source|
		check_file(source)
	end
    fatal_error("No scheme specified in #{@file}") if options[:scheme].nil?
    if options[:configuration].nil?
      logger.warn('No build configuration set, defaulting to Debug')
      options[:configuration] = 'Debug'
    end
    options
  end

  def check_file(file)
	unless file.nil?
		fatal_error("#{file} not found") unless File.exist?("file")
	end
  end

end