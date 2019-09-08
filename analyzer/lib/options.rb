require 'optparse'

class Options
	
	REPORTER_ALIASES = { :scanner => "SonarScanner", :runner => "SonarRunner" }
	
	#
	# Return a structure describing the options.
	#
	def parse(args, options)
		# The options specified on the command line will be updated in *options*.
		
		opt_parser = OptionParser.new do |opts|
			opts.banner = "Usage: main.rb [options]"
			
			opts.separator ""
			opts.separator "Specific options:"
			
			# Optional
			opts.on("-p", "--path PATH", String, "Path to Sonar properties file.") do |p|
				options.path = p
			end
			
			# Optional
			opts.on("-r", "--reporter [REPORTER]", REPORTER_ALIASES.keys, "Select Sonar reporter (scanner, runner)") do |r|
				options.reporter = REPORTER_ALIASES[r]
			end
			
			opts.separator ""
			opts.separator "Disable tools:"
			
			# Optional
			opts.on("--disable-swiftlint", "Disable SwiftLint") do |_|
				options.tools.delete_at(options.tools.index(SwiftLint))
			end
			
			# Optional
			opts.on("--disable-oclint", "Disable OCLint") do |_|
				options.tools.delete_at(options.tools.index(JSONCompilationDatabase))
				options.tools.delete_at(options.tools.index(OCLint))
			end
			
			# Optional
			opts.on("--disable-slather", "Disable Slather") do |_|
				options.tools.delete_at(options.tools.index(Slather))
			end
			
			# Optional
			opts.on("--disable-lizard", "Disable Lizard") do |_|
				options.tools.delete_at(options.tools.index(Lizard))
			end
			
			# Optional
			opts.on("--disable-fauxpas", "Disable FauxPas") do |_|
				options.tools.delete_at(options.tools.index(FauxPas))
			end

			# Optional
			opts.on("--disable-upload", "Disable upload to SonarQube server") do |_|
				options.upload = false
			end
			
			opts.separator ""
			opts.separator "Common options:"
			
			opts.on("-v", "--verbose", "Run verbosely") do |_|
				Logging.logger_level = Logger::DEBUG
			end
			
			# No argument, shows at tail.  This will print an options summary.
			# Try it and see!
			opts.on_tail("-h", "--help", "Show this message") do
				puts opts
				exit
			end
		end
		
		opt_parser.parse!(args)
		
	end
	
end