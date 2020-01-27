require 'tools/tool'

# Lizard computes the code complexity.
#
# @see http://www.lizard.ws
class Lizard < Tool

	@@REPORT_FILE = 'lizard-report.xml'.freeze

	def self.command
		{ 
			lizard: 'lizard'
		}
	end
	
	def initialize(properties, options)
		@sources = properties[:sources]
		@report_folder = options.report_folder
		super(properties, options)
	end
	
	def run()
		logger.info('Running...')
		cmd = "#{self.class.command[:lizard]} --xml"
		@sources.each do |source|
			cmd += " \"#{source}\""
		end
		cmd += " > #{@report_folder}/#{@@REPORT_FILE}"
		logger.debug("Will run `#{cmd}`")
		system(cmd)
	end
	
	private
	
	def validate_settings!
		fatal_error('Sources must be set in order to compute Lizard complexity') if @sources.nil?
	end
end