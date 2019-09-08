#!/usr/bin/env ruby
require 'ostruct'
require 'logging'
require 'tools/swiftlint'
require 'tools/lizard'
require 'tools/sonar_scanner'
require 'tools/sonar_runner'
require 'tools/unit_tests'
require 'tools/slather'
require 'tools/oclint'
require 'tools/fauxpas'
require 'tools/json_compilation_database'
require 'options'
require 'properties_reader'
require 'helper'

# Entry point of the script.
#
# It reads the configurations, run all analytic tools and send reports
# to Sonar.
class Analyzer
	include Logging
	
	def initialize
		@options = OpenStruct.new 
		# list of tools by default
		@options.tools = [JSONCompilationDatabase, UnitTests, Slather, SwiftLint, Lizard, OCLint, FauxPas]
		# reporter by default
		@options.reporter = SonarScanner
		# upload results to SonarQube by default
		@options.upload = true
		# upload results to SonarQube by default
		@options.path = 'sonar-project.properties'
		# report folder
		@options.report_folder = 'sonar-reports'
		
		@helper = Helper.new
		
	end
	
	def run
		
		# read CLI arguments and update configuration
		Options.new.parse(ARGV, @options)
		
		# Initiate reports
		@helper.bootstrap_reports(@options.report_folder)
		
		# Read Sonar project properties
		@properties = SonarPropertiesReader.new(@options.path).read
		
		tools
		reporter if @options.upload
		
	end
	
	private
	def tools
		# Filter tools by availability
		@options.tools = @helper.available(@options.tools)
		
		# Run tools
		@options.tools.each do |tool|
			tool.new(@properties, @options).run
		end
	end
	
	private
	def reporter
		# Filter reporters by availability
		@options.reporter = @helper.available(@options.reporter)
		
		# Send reports
		@options.reporter.new(@properties, @options).run unless @options.reporter.nil?
		
	end
	
end

Analyzer.new.run