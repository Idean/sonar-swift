require_relative 'tool'

# SwiftLint checks code style and conventions.
#
# It is mainly based on the [Swift Style
# Guide](https://github.com/github/swift-style-guide) and it may also be
# used to enforce custom conventions.
#
# https://github.com/realm/SwiftLint
class SwiftLint < Tool
  def self.command
    'swiftlint'
  end

  def initialize(options)
    @sources = options[:sources]
    super(options)
  end

  def run()
    logger.info('Running...')
    @sources.each do |source|
      report_name = "#{source.tr(' ', '_')}-swiftlint.txt"
      cmd = "#{self.class.command} lint --path \"#{source}\" > sonar-reports/#{report_name}"
      logger.debug("Will run `#{cmd}`")
      system(cmd)
    end
  end

  private

  def validate_settings!
    fatal_error('Sources must be set in order to lint') if @sources.nil?
  end
end