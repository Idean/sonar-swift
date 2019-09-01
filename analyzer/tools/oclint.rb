require_relative 'tool'
require 'fileutils'

class OCLint < Tool
	
	MAX_PRIORITY = 10000
    LONG_LINE_THRESHOLD = 250
	
	def self.command
		'oclint-json-compilation-database'
	end
	
	def initialize(options)
		@sources = options[:sources]
		super(options)
	end
	
	def run()
		logger.info('Running...')
		@sources.each do |source|
			report_name = "#{source.tr(' ', '_')}-oclint.xml"

			cmd = "#{self.class.command}"
			cmd += " --include \"#{source}\""
			cmd += " -- -rc LONG_LINE=#{LONG_LINE_THRESHOLD}"
			cmd += " -max-priority-1 #{MAX_PRIORITY} -max-priority-2 #{MAX_PRIORITY} -max-priority-3 #{MAX_PRIORITY}"
			cmd += " -report-type pmd"
			cmd += " -o sonar-reports/#{report_name}"
			  
			logger.debug("Will run `#{cmd}`")
			system(cmd)
		end
	  	
	end
	
	private
	
	def validate_settings!
		fatal_error('Sources must be set in order to lint') if @sources.nil?
		fatal_error("compile_commands.json not found") unless File.exist?("compile_commands.json")
	end
end