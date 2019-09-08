require_relative 'tool'

# Runs unit tests using Xcode with `xcodebuild`
class UnitTests < Tool

	@@TIMEOUT = '60'.freeze
	@@REPORT_FILE = 'TEST-report.xml'

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
		@configuration = properties[:configuration]
		@simulator = properties[:simulator]
		@exclude_from_coverage = properties[:exclude_from_coverage]
		@report_folder = options.report_folder
		super(properties, options)
	end
	
	def run
		logger.info('Running...')
		cmd = "#{self.class.command[:xcodebuild]} clean build-for-testing test"
		cmd += " -workspace \"#{@workspace}\"" unless @workspace.nil?
		cmd += " -project \"#{@project}\"" unless !@workspace.nil?
		cmd += " -scheme \"#{@scheme}\""
		cmd += " -configuration \"#{@configuration}\""
		cmd += " -enableCodeCoverage YES"
		cmd += " -destination '#{@simulator}' -destination-timeout #{@@TIMEOUT}" unless @simulator.nil?
		cmd += " -quiet" unless logger.level == Logger::DEBUG
		cmd += " | tee xcodebuild.log"
		cmd += " | #{self.class.command[:xcpretty]} -t --report junit -o #{@report_folder}/#{@@REPORT_FILE}"
		logger.debug("Will run `#{cmd}`")
		system(cmd)
	end
	
	private
	
	def validate_settings!
		# @workspace is optional
		fatal_error('A project must be set in order to run test') if @project.nil?
		fatal_error('A scheme must be set in order to build and test the app') if @scheme.nil?
		fatal_error('A configuration must be set in order to build and test the app') if @configuration.nil?
		logger.warn('No simulator specified') if @simulator.nil?
		# @exclude_from_coverage is optional
	end
	
end