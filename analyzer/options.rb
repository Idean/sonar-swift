require 'optparse'

class Options

  #
  # Return a structure describing the options.
  #
  def parse(args, options)
    # The options specified on the command line will be updated in *options*.

    opt_parser = OptionParser.new do |opts|
	  opts.banner = "Usage: main.rb [options]"

      opts.separator ""
	  opts.separator "Specific options:"
	  
      opts.on("-v", "--verbose", "Run verbosely") do |_|
        Logging.logger_level = Logger::DEBUG
	  end
	  
	  # Optional
      opts.on("--disable-swiftlint", "Disable SwiftLint") do |_|
		options.tools.delete_at(options.tools.index(SwiftLint))
	  end
	  
	  # Optional
	  opts.on("-p", "--path PATH", String, "Path to properties file.") do |p|
		options.path = p
      end

      opts.separator ""
      opts.separator "Common options:"

      # No argument, shows at tail.  This will print an options summary.
      # Try it and see!
      opts.on_tail("-h", "--help", "Show this message") do
        puts opts
        exit
	  end
	end

    opt_parser.parse!(args)

  end  # parse()

end  # class OptparseExample