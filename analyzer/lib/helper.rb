require 'fileutils'
require 'logging'

class Helper
	include Logging

	# Put default xml files with no tests and no coverage. This is needed to
	# ensure a file is present, either the Xcode build test step worked or
	# not. Without this, Sonar Scanner will fail uploading results.
	def bootstrap_reports(folder)
		reports_folder(folder)
		mandatory_reports(folder)
	end

	# Check each program is available and return an updated list.
	def available(programs)
		case programs
			when Array
				programs.select do |program|
					_available(program)
				end
			else 
				if _available(programs) then programs else nil end
					
			end
	end

	# Check program is available and return an updated list.
	private
	def _available(program)
		availability = program.command.values.reduce(true) { |available, tool| 
			toolAvailable = system("which #{tool} 2>&1 > /dev/null")
			logger.warn("#{tool} is not found in PATH") if !toolAvailable
			available &= toolAvailable
		}
		logger.warn("Skipping #{program}.") if !availability
		availability
	end
	
	private
	def mandatory_reports(folder)
		logger.info('Creating default reports')
		empty_test_report = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><testsuites name='AllTestUnits'></testsuites>"
		File.write("#{folder}/TEST-report.xml", empty_test_report)
		
		empty_coverage_report = "<?xml version='1.0' ?><!DOCTYPE coverage SYSTEM 'http://cobertura.sourceforge.net/xml/coverage-03.dtd'><coverage><sources></sources><packages></packages></coverage>"
		File.write("#{folder}/coverage.xml", empty_coverage_report)
	end
	
	private
	def reports_folder(folder)
		logger.info("Deleting and creating directory #{folder}")
		FileUtils.rm_rf(folder)
		Dir.mkdir(folder)
	end
	
end

