require_relative 'tool'

# Runs unit tests using Xcode with `xcodebuild`
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
    logger.info('Running ...')
    cmd = "#{self.class.command} clean build-for-testing test"
	cmd += " -workspace \"#{@workspace}\"" unless @workspace.nil?
	cmd += " -project \"#{@project}\"" unless !@workspace.nil?
	cmd += " -scheme \"#{@scheme}\""
	cmd += " -configuration \"#{@configuration}\""
	cmd += " -enableCodeCoverage YES"
	cmd += " -destination '#{@simulator}' -destination-timeout 60" unless @simulator.nil?
	cmd += " | tee sonar-reports/xcodebuild.log"
    logger.debug("Will run `#{cmd}`")
    system(cmd)
  end

  private

  def validate_settings!
    # @workspace is optional
    fatal_error('A project must be set in order to compute coverage') if @project.nil?
    fatal_error('A scheme must be set in order to build and test the app') if @scheme.nil?
    fatal_error('A configuration must be set in order to build and test the app') if @configuration.nil?
    logger.warn('No simulator specified') if @simulator.nil?
    # @exclude_from_coverage is optional
  end

end