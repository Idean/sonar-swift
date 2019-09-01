require_relative 'tool'

class SonarRunner < Tool
  def self.command
    'sonar-runner'
  end

  def initialize(options)
    super(options)
  end

  def run()
    logger.info('Running...')
    cmd = "#{self.class.command}"
    logger.debug("Will run `#{cmd}`")
    system(cmd)
  end

end