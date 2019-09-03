require_relative 'tool'

class SonarRunner < Tool
	def self.command
		{
			runner: 'sonar-runner'
		}
	end
	
	def initialize(properties, options)
		super(properties, options)
	end
	
	def run()
		logger.info('Running...')
		cmd = "#{self.class.command[:runner]}"
		logger.debug("Will run `#{cmd}`")
		system(cmd)
	end
	
end