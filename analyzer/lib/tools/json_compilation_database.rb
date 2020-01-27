require 'tools/tool'

class JSONCompilationDatabase < Tool

	@@TIMEOUT = '360'.freeze
	@@COMPILE_COMMANDS_FILE = 'compile_commands.json'.freeze
	@@XCODEBUILD_FILE = 'xcodebuild.log'.freeze

	def self.command
		{ 
			xcodebuild: 'xcodebuild', 
			xcpretty: 'xcpretty' 
		}
	end
	
	def initialize(properties, options)
		@workspace = properties[:workspace]
		@project = properties[:project]
		@scheme = properties[:scheme]
		@simulator = properties[:simulator]
		super(properties, options)
	end
	
	def run
		logger.info('Running ...')
		cmd = "#{self.class.command[:xcodebuild]} clean build"
		cmd += " -workspace \"#{@workspace}\"" unless @workspace.nil?
		cmd += " -project \"#{@project}\"" unless !@workspace.nil?
		cmd += " -scheme \"#{@scheme}\""
		cmd += " -destination '#{@simulator}' -destination-timeout #{@@TIMEOUT} COMPILER_INDEX_STORE_ENABLE=NO" unless @simulator.nil?
		cmd += " | tee #{@@XCODEBUILD_FILE}"
		cmd += " | #{self.class.command[:xcpretty]} -r json-compilation-database -o #{@@COMPILE_COMMANDS_FILE}"
		logger.debug("Will run `#{cmd}`")
		system(cmd)
	end
	
	private
	
	def validate_settings!
		# @workspace is optional
		fatal_error('A project must be set in order to compute JSON compilation database') if @project.nil?
		fatal_error('A scheme must be set in order to compute JSON compilation database') if @scheme.nil?
		logger.warn('No simulator specified') if @simulator.nil?
	end
	
end