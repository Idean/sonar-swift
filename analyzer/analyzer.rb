#!/usr/bin/env ruby
require 'ostruct'
require_relative 'logging'
require_relative 'tools/swiftlint'
require_relative 'tools/lizard'
require_relative 'tools/sonar_scanner'
require_relative 'tools/sonar_runner'
require_relative 'tools/unit_tests'
require_relative 'tools/slather'
require_relative 'tools/oclint'
require_relative 'tools/fauxpas'
require_relative 'tools/json_compilation_database'
require_relative 'options'
require_relative 'properties_reader'
require_relative 'helper'

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
		
		@helper = Helper.new
		
	end
	
	def run
		
		# read CLI arguments and update configuration
		Options.new.parse(ARGV, @options)
		
		# Initiate reports
		@helper.bootstrap_reports
		
		# Read Sonar project properties
		@properties = SonarPropertiesReader.new(@options.path).read
		
		tools
		reporter if upload
		
	end
	
	private
	def tools
		# Filter tools by availability
		@options.tools = @helper.available(@options.tools)
		
		# Run tools
		@options.tools.each do |tool|
			tool.new(@properties).run
		end
	end
	
	private
	def reporter
		# Filter reporters by availability
		@options.reporter = @helper.available(@options.reporter)
		
		# Send reports
		@options.reporter.new([]).run unless @options.reporter.nil?
		
	end
	
end

Analyzer.new.run