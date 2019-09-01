require 'fileutils'
require_relative 'tool'

class Slather < Tool
  def self.command
    'slather'
  end

  def initialize(options)
	@workspace = options[:workspace]
    @project = options[:project]
    @scheme = options[:scheme]
	@exclude_from_coverage = options[:exclude_from_coverage]
	@binary_names = options[:binary_names]
    super(options)
  end

  def run()
	logger.info('Running...')
	
	cmd = "#{self.class.command} coverage"
	unless @binary_names.nil?
		@binary_names.each do |binary|
			cmd += " --binary-basename \"#{binary}\""
		end
	end
	unless @exclude_from_coverage.nil?
		@exclude_from_coverage.each do |exclusion|
			cmd += " -i \"#{exclusion}\""
		end
	end
	cmd += " --input-format profdata --cobertura-xml --output-directory sonar-reports"
    cmd += " --workspace #{@workspace}" unless @workspace.nil?
	cmd += " --scheme #{@scheme} #{@project}"
	logger.debug("Will run `#{cmd}`")
	system(cmd)

	FileUtils.mv('sonar-reports/cobertura.xml', 'sonar-reports/coverage-swift.xml')

  end

  private

  def validate_settings!
	# @workspace is optional
    fatal_error('A project must be set in order to compute coverage') if @project.nil?
    fatal_error('A scheme must be set in order to compute coverage') if @scheme.nil?
	# @exclude_from_coverage is optional
	# @binary_names is optional
  end
end