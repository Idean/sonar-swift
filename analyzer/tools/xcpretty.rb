require 'fileutils'
require_relative 'tool'

class XCPretty < Tool
  def self.command
    'oclint-json-compilation-database'
  end

  def initialize(options)
    super(options)
  end

  def run()
    logger.info('Running...')
	cmd = "cat sonar-reports/xcodebuild.log"
	cmd += " | #{self.class.command} -t --report junit -o sonar-reports/TEST-report.xml"
    logger.debug("Will run `#{cmd}`")
	system(cmd)
  end

  private

  def validate_settings!
  end
end