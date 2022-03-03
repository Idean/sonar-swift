#
# backelite-sonar-swift-plugin - Enables analysis of Swift and Objective-C projects into SonarQube.
# Copyright © 2019 David Yang (david.tcha.yang@gmail.com)
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

require "java-properties"
require "shellwords"

desc "Launch code analysis for SonarQube"
lane :metrics do |options|
	properties = JavaProperties.load(File.join(ENV["PWD"], "sonar-project.properties"))
	default_output_directory = "./sonar-reports"
	derived_data_path = "./DerivedData"

	sonar_run_tests(properties: properties, output_directory: default_output_directory)
	sonar_run_slather(properties: properties, output_directory: default_output_directory)
	sonar_run_oclint(properties: properties, output_directory: default_output_directory)
	sonar_run_swiftlint(properties: properties, output_directory: default_output_directory)
	sonar_run_lizard(properties: properties, output_directory: default_output_directory)
	sonar_run_scanner(properties: properties, sonar_url: options[:sonar_url], sonar_login: options[:sonar_login])
end

desc "Build and run project to gather compile commands database and test report"
private_lane :sonar_run_tests do |options|
	# Extract sonar property values
	workspace	= options[:properties][:"sonar.swift.workspace"]
	app_scheme = options[:properties][:"sonar.swift.appScheme"]
	destination = options[:properties][:"sonar.swift.simulator"]
	app_configuration = options[:properties][:"sonar.swift.appConfiguration"]
	configuration = "Debug" if app_configuration.to_s.empty?
	derived_data_path = options[:derived_data_path]

	run_tests(
    	workspace: workspace,
    	scheme: app_scheme,
			destination: destination,
			configuration: configuration,
    	clean: true,
			code_coverage: true,
			output_types: "json-compilation-database,junit",
    	output_directory: options[:output_directory],
			output_files: "compile_commands.json,TEST-report.xml",
    	xcargs: "COMPILER_INDEX_STORE_ENABLE=NO"
	)
end

desc "Run Slather coverage"
private_lane :sonar_run_slather do |options|
	# Extract sonar property values
	binary_basename = options[:properties][:"sonar.coverage.binaryNames"]
	app_scheme = options[:properties][:"sonar.swift.appScheme"]
	excluded_paths_from_coverage = options[:properties][:"sonar.swift.excludedPathsFromCoverage"]
	project = options[:properties][:"sonar.swift.project"]
	derived_data_path = lane_context[SharedValues::SCAN_DERIVED_DATA_PATH]

	if binary_basename.nil? || binary_basename.empty?
		slather(
		   	cobertura_xml: true, 
		   	scheme: app_scheme,
		   	input_format: "profdata", 
		   	ignore: excluded_paths_from_coverage.split(","), 
		   	build_directory: derived_data_path, 
		   	output_directory: options[:output_directory],
		   	proj: project
		)
	else
		slather(
		   	cobertura_xml: true, 
		   	scheme: app_scheme,
		   	input_format: "profdata", 
		   	ignore: excluded_paths_from_coverage.split(","), 
		   	binary_basename: binary_basename.split(","),
		   	build_directory: derived_data_path, 
		   	output_directory: options[:output_directory],
		   	proj: project
		)
	end
    # Rename the file
    sh "cd ../#{options[:output_directory]} && mv cobertura.xml coverage-swift.xml"
end

desc "Run OCLint analysis"
private_lane :sonar_run_oclint do |options|
	# Extract sonar property values
	sources = options[:properties][:"sonar.sources"]

	sources.split(",").each do |source|
		# if source path contains objective-c file (*.m)
		unless Dir.glob("../#{source}/**/*.m").empty?
			output_filename = source.gsub("/", "_") + "-oclint.xml"
			sh "cd ../#{options[:output_directory]} && oclint-json-compilation-database -v --include .*/#{source} -- -rc LONG_LINE=250 -max-priority-1 10000 -max-priority-2 10000 -max-priority-3 10000 -report-type pmd -o #{output_filename}"
		end
	end
end

desc "Run Lizard"
private_lane :sonar_run_lizard do |options|
	executablePath = './tools/lizard/lizard.py'
	sources = options[:properties][:"sonar.sources"]
	source_folders = sources.split(",").map { |source| source.shellescape }.join(" ")

	lizard(
		executable: executablePath,
		source_folder: source_folders,
		language: 'swift,objectivec',
		export_type: 'xml',
		report_file: options[:output_directory] + '/lizard-report.xml'
	)
end

desc "Run SwiftLint analysis"
private_lane :sonar_run_swiftlint do |options|
	# Extract sonar property values
	sources = options[:properties][:"sonar.sources"]

	# iterating through each sources path
	sources.split(",").each do |source|
		output_filename = options[:output_directory] + "/" + source.gsub("/", "_") + "-swiftlint.txt"
		swiftlint(mode: :lint, path: source, output_file: output_filename, ignore_exit_status: true)
	end
end

desc "Run sonar-scanner"
private_lane :sonar_run_scanner do |options|
	version_number = get_version_number(
		xcodeproj: options[:properties][:"sonar.swift.project"],
		target: options[:properties][:"sonar.swift.appScheme"]
	)

	sonar(
		project_configuration_path: "sonar-project.properties",
		project_version: version_number,
		sonar_url: options[:sonar_url], 
		sonar_login: options[:sonar_login]
	)
end
