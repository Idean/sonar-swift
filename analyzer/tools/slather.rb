require 'fileutils'
require_relative 'tool'

class Slather < Tool

	@@REPORT_FILE_DEFAULT = 'cobertura.xml'.freeze
	@@REPORT_FILE = 'coverage-swift.xml'.freeze

	def self.command
		{ 
			slather: 'slather'
		}
	end
	
	def initialize(properties, options)
		@workspace = properties[:workspace]
		@project = properties[:project]
		@scheme = properties[:scheme]
		@exclude_from_coverage = properties[:exclude_from_coverage]
		@binary_names = properties[:binary_names]
		@report_folder = options.report_folder
		super(properties, options)
	end
	
	def run()
		logger.info('Running...')
		
		cmd = "#{self.class.command[:slather]} coverage"
		cmd += " --verbose" if logger.level == Logger::DEBUG
		unless @binary_names.nil?
			@binary_names.each do |binary|
				cmd += " --binary-basename \"#{binary}\""
			end
		end
		unless @exclude_from_coverage.nil?
			@exclude_from_coverage.each do |exclusion|
				cmd += " -i \"#{exclusion}\""
			end
		end
		cmd += " --input-format profdata --cobertura-xml --output-directory #{@report_folder}"
		cmd += " --workspace #{@workspace}" unless @workspace.nil?
		cmd += " --scheme #{@scheme} #{@project}"
		logger.debug("Will run `#{cmd}`")
		system(cmd)
		
		FileUtils.mv("#{@report_folder}/#{@@REPORT_FILE_DEFAULT}", "#{@report_folder}/#{@@REPORT_FILE}")
		
	end
	
	private
	
	def validate_settings!
		# @workspace is optional
		fatal_error('A project must be set in order to run Slather') if @project.nil?
		fatal_error('A scheme must be set in order to run Slather') if @scheme.nil?
		# @exclude_from_coverage is optional
		# @binary_names is optional
	end
end