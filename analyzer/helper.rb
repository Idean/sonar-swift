require 'fileutils'
require_relative 'logging'

class Helper
	include Logging

	# Put default xml files with no tests and no coverage. This is needed to
	# ensure a file is present, either the Xcode build test step worked or
	# not. Without this, Sonar Scanner will fail uploading results.
	def bootstrap_reports
		reports_folder
		mandatory_reports
	end

	# Check each program is available and return an updated list.
	def available(programs)
		case programs
			when Array
				programs.select do |program|
					_available(program) unless nil
				end
			else 
				_available(programs)
			end
	end

	# Check program is available and return an updated list.
	private
	def _available(program)
		if program.availability
			program
		else
			logger.warn("#{program.command} is not found in PATH.")
			nil
		end
	end
	
	private
	def mandatory_reports
		logger.info('Creating default reports')
		empty_test_report = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><testsuites name='AllTestUnits'></testsuites>"
		File.write('sonar-reports/TEST-report.xml', empty_test_report)
		
		empty_coverage_report = "<?xml version='1.0' ?><!DOCTYPE coverage SYSTEM 'http://cobertura.sourceforge.net/xml/coverage-03.dtd'><coverage><sources></sources><packages></packages></coverage>"
		File.write('sonar-reports/coverage.xml', empty_coverage_report)
	end
	
	private
	def reports_folder
		logger.info('Deleting and creating directory sonar-reports/')
		FileUtils.rm_rf('sonar-reports')
		Dir.mkdir('sonar-reports')
	end
	
end

