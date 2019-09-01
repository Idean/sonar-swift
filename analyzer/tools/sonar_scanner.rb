require_relative 'tool'

class SonarScanner < Tool
	def self.command
		{
			:scanner: 'sonar-scanner'
		}
	end
	
	def initialize(options)
		super(options)
	end
	
	def run()
		logger.info('Running...')
		cmd = "#{self.class.command[:scanner]}"
		logger.debug("Will run `#{cmd}`")
		system(cmd)
	end
	
end