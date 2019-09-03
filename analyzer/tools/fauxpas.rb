require_relative 'tool'

class FauxPas < Tool

	@@REPORT_FILE = 'fauxpas.json'.freeze

	def self.command
		{ 
			fauxpas: 'fauxpas'
		}
	end
	
	def initialize(properties, options)
		@workspace = properties[:workspace]
		@project = properties[:project]
		@scheme = properties[:scheme]
		@report_folder = options.report_folder
		super(properties, options)
	end
	
	def run()
		logger.info('Running...')
		cmd = "#{self.class.command[:fauxpas]} check -o json #{@project}"
		cmd += " --workspace #{@workspace}" unless @workspace.nil?
		cmd += " --scheme #{@scheme}"
		cmd += " -v "
		cmd += if logger.level == Logger::DEBUG then "yes" else "no" end
		cmd += " > #{@report_folder}/#{@@REPORT_FILE}"
		logger.debug("Will run `#{cmd}`")
		system(cmd)
	end
	
	private
	
	def validate_settings!
		# @workspace is optional
		fatal_error('A project must be set in order run FauxPas') if @project.nil?
		fatal_error('A scheme must be set in order run FauxPas') if @scheme.nil?
	end
end