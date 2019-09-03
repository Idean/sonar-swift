require_relative 'tool'
require 'fileutils'

class OCLint < Tool
	
	@@MAX_PRIORITY = 10000
	@@LONG_LINE_THRESHOLD = 250
	@@REPORT_FILE = '-oclint.xml'.freeze
	
	def self.command
		{ 
			oclint: 'oclint-json-compilation-database'
		}
	end
	
	def initialize(properties, options)
		@sources = properties[:sources]
		@report_folder = options.report_folder
		super(properties, options)
	end
	
	def run()
		logger.info('Running...')
		@sources.each do |source|

			next unless `find \"#{source}/\" -name '*.m' | wc -l | tr -d ' ' 2>&1` != 0

			report_name = "#{source.tr(' ', '_')}#{@@REPORT_FILE}"
			
			cmd = "#{self.class.command[:oclint]}"
			cmd += " -v" if logger.level == Logger::DEBUG
			cmd += " --include #{source}"
			cmd += " -- -rc LONG_LINE=#{@@LONG_LINE_THRESHOLD}"
			cmd += " -max-priority-1 #{@@MAX_PRIORITY} -max-priority-2 #{@@MAX_PRIORITY} -max-priority-3 #{@@MAX_PRIORITY}"
			cmd += " -report-type pmd"
			cmd += " -o #{report_folder}/#{report_name}"
			
			logger.debug("Will run `#{cmd}`")
			system(cmd)
		end
		
	end
	
	private
	
	def validate_settings!
		fatal_error('Sources must be set in order to run OCLint') if @sources.nil?
		fatal_error("compile_commands.json not found") unless File.exist?("compile_commands.json")
	end
end