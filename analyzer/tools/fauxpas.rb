require_relative 'tool'

class FauxPas < Tool
  def self.command
    'fauxpas'
  end

  def initialize(options)
    @workspace = options[:workspace]
    @project = options[:project]
    @scheme = options[:scheme]
    super(options)
  end

  def run()
	logger.info('Running...')
	cmd = "#{self.class.command} check -o json #{@project}"
	cmd += " --workspace #{@workspace}" unless @workspace.nil?
	cmd += " --scheme #{@scheme}"
	cmd += " > sonar-reports/fauxpas.json"
    logger.debug("Will run `#{cmd}`")
	system(cmd)
  end

  private

  def validate_settings!
    # @workspace is optional
    fatal_error('A project must be set in order to compute coverage') if @project.nil?
    fatal_error('A scheme must be set in order to compute coverage') if @scheme.nil?
  end
end