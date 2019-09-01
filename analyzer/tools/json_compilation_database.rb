require_relative 'tool'

class JSONCompilationDatabase < Tool
	def self.command
		{ 
			xcodebuild: 'xcodebuild', 
			xcpretty: 'xcpretty' 
		}
	end
	
	def initialize(options)
		@workspace = options[:workspace]
		@project = options[:project]
		@scheme = options[:scheme]
		@simulator = options[:simulator]
		super(options)
	end
	
	def run
		logger.info('Running ...')
		cmd = "#{self.class.command[:xcodebuild]} clean build"
		cmd += " -workspace \"#{@workspace}\"" unless @workspace.nil?
		cmd += " -project \"#{@project}\"" unless !@workspace.nil?
		cmd += " -scheme \"#{@scheme}\""
		cmd += " -destination '#{@simulator}' -destination-timeout 360 COMPILER_INDEX_STORE_ENABLE=NO" unless @simulator.nil?
		cmd += " | tee xcodebuild.log"
		cmd += " | #{self.class.command[:xcpretty]} -r json-compilation-database -o compile_commands.json"
		logger.debug("Will run `#{cmd}`")
		system(cmd)
	end
	
	private
	
	def validate_settings!
		# @workspace is optional
		fatal_error('A project must be set in order to compute coverage') if @project.nil?
		fatal_error('A scheme must be set in order to build and test the app') if @scheme.nil?
		logger.warn('No simulator specified') if @simulator.nil?
	end
	
end