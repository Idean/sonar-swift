require 'logger'

# Adds logging capability where included.
module Logging
  def logger
    @logger ||= Logging.logger_for(self.class.name)
  end

  # Use a hash class-ivar to cache a unique Logger per class:
  @@loggers = {}
  @@logger_level = Logger::INFO

  class << self
    def logger_for(classname)
      @@loggers[classname] ||= configure_logger_for(classname)
    end

    def configure_logger_for(classname)
      logger = Logger.new(STDOUT)
      logger.progname = classname
      logger.level = @@logger_level
      logger
    end

    def logger_level=(level)
      @@logger_level = level
    end
  end
end