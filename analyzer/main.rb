#!/usr/bin/env ruby
require 'ostruct'
require_relative 'logging'
require_relative 'swiftlint'
require_relative 'options'
require_relative 'properties_reader'

# Entry point of the script.
#
# It reads the configurations, run all analytic tools and send reports
# to Sonar.
class RunSonarSwift
	include Logging
	
	def run
		options = OpenStruct.new 
		# list of tools by default
		options.tools = [SwiftLint]
		# upload results to SonarQube by default
		options.upload = true
		# upload results to SonarQube by default
		options.path = 'sonar-project.properties'

		# read CLI arguments and update configuration
		Options.new.parse(ARGV, options)

		# Read properties
    	properties = SonarPropertiesReader.new(options.path).read

		# Initiate reports
		# TODO

		# Filter tools by availability
		options.tools = available_tools(options.tools)
		
		# Run tools
		options.tools.each do |tool|
			tool.new(properties).run
		end

		# Send reports
		# TODO
		
	end
	
	# Check each tool is available and return an updated list of tool.
	def available_tools(tools)
		tools.select do |tool|
			if tool.availability
				true
			else
				logger.warn("#{tool.command} is not found in PATH and won't be available in reports.")
				false
			end
		end
	end
	
end

RunSonarSwift.new.run
