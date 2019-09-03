require_relative 'tool'

# SwiftLint checks code style and conventions.
#
# It is mainly based on the [Swift Style
# Guide](https://github.com/github/swift-style-guide) and it may also be
# used to enforce custom conventions.
#
# https://github.com/realm/SwiftLint
class SwiftLint < Tool

	@@REPORT_FILE = '-swiftlint.txt'.freeze

	def self.command
		{
			swiftlint: 'swiftlint'
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
			report_name = "#{source.tr(' ', '_')}#{@@REPORT_FILE}"
			cmd = "#{self.class.command[:swiftlint]} lint --path \"#{source}\""
			cmd += " --quiet" unless logger.level == Logger::DEBUG 
			cmd += " > #{report_folder}/#{report_name}"
			logger.debug("Will run `#{cmd}`")
			system(cmd)
		end
	end
	
	private
	
	def validate_settings!
		fatal_error('Sources must be set in order to lint') if @sources.nil?
	end
end