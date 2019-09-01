require_relative 'tool'

# Lizard computes the code complexity.
#
# @see http://www.lizard.ws
class Lizard < Tool
  def self.command
    'lizard'
  end

  def initialize(options)
    @sources = options[:sources]
    super(options)
  end

  def run()
	logger.info('Running...')
	cmd = "#{self.class.command} --xml"
	@sources.each do |source|
      cmd += " \"#{source}\""
    end
	cmd += " > sonar-reports/lizard-reports.xml"
    logger.debug("Will run `#{cmd}`")
    system(cmd)
  end

  private

  def validate_settings!
    fatal_error('Sources must be set.') if @sources.nil?
  end
end