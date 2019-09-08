lib = File.expand_path('../lib', __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)
require 'version'

Gem::Specification.new do |s|
  s.name = "analyzer"
  s.version = AnalyzerV::VERSION
  s.summary = "SonarSwift plugin analyzer"
  s.authors = ["Gaël FOppolo"]
  
  s.description = "SonarSwift plugin analyzer"
  s.license = 'MIT'
 
  s.required_ruby_version = '>= 2.0.0'

  s.files = `git ls-files`.split($/)
  s.executables = s.files.grep(%r{^bin/}) { |f| File.basename(f) }
  s.require_paths = ["lib"]
  
  s.add_runtime_dependency 'java-properties', "= 0.2.0"
  s.add_runtime_dependency 'slather', "= 2.4.7"
  s.add_runtime_dependency 'xcpretty', "= 0.3.0"

end