<p align="left">
  <img src="http://www.backelite.com/bkimages/extension/backelite/design/backelite/templates/img/header_logo.png/3840/2160/PNG" width="100"/>
</p>

| Branch   |      Status                                                                                                                                |
|----------|:------------------------------------------------------------------------------------------------------------------------------------------:|
| master | [![Build Status](https://travis-ci.org/Backelite/sonar-swift.svg?branch=master)](https://travis-ci.org/Backelite/sonar-swift)  |
| develop| [![Build Status](https://travis-ci.org/Backelite/sonar-swift.svg?branch=develop)](https://travis-ci.org/Backelite/sonar-swift) |

SonarQube Plugin for Swift
================================

This is an open source initiative for Apple Swift language support in SonarQube.
The structure of the plugin is based on the [sonar-objective-c](https://github.com/octo-technology/sonar-objective-c) plugin.

<p align="center">
  <img src="screenshot.png" alt="Example iOS SonarQube dashboard" width="100%"/>
</p>

###Features


| Feature 		| Supported	| Details	|
|---------------|----------|:-----------:|
| Complexity	|YES			|Uses [Lizard](https://github.com/terryyin/lizard)			|
| Design		|NO			|			|
| Documentation	|YES		|			|
| Duplications	|YES		|			|
| Issues		|YES		| Uses [SwiftLint](https://github.com/realm/SwiftLint)|
| Size			|YES		|			|
| Tests			|YES		| Uses xcodebuild + xcpretty [xcpretty](https://github.com/supermarin/xcpretty)			|
| Code coverage	|YES			| Uses [slather](https://github.com/venmo/slather)			|


###Download

Checkout the [Releases](https://github.com/Backelite/sonar-swift/releases) page.

###Launching an analysis
If you use [Fastlane](https://fastlane.tools), please read [our Fastlane integration doc](docs/sonarqube-fastlane.md).
Otherwise, run the ```run-sonar-swift.sh``` script from your Xcode project root folder

###Release history

####0.2.3
- Fixed Lizard Sensor wrong file path

####0.2.2
- Added support for .xcodeproj only projects (thanks to [delannoyk](https://github.com/delannoyk))
- Fix for Lizard Sensor to find indexed files (thanks to [gretzki](https://github.com/gretzki))
- Got rid of confusion with commercial plugin in the update center

####0.2.1
- SwiftLint 0.8 support (new rules added).

####0.2.0
- Lizard complexity report support (thanks to [akshaysyaduvanshi](https://github.com/akshaysyaduvanshi))

####0.1.2
- SwiftLint 0.5.1 support (new rules added).
- Added *sonar.swift.simulator* key in *sonar-project.properties* to select destination simulator for running tests
- SwiftLint scans source directories only

####0.1.1
- SwiftLint 0.4.0 support (new rules added).

####0.1.0
- Initial release.



###Prerequisites

- a Mac with Xcode 7 or +
- [SonarQube](http://docs.codehaus.org/display/SONAR/Setup+and+Upgrade) and [SonarQube Runner](http://docs.codehaus.org/display/SONAR/Installing+and+Configuring+SonarQube+Runner) installed ([HomeBrew](http://brew.sh) installed and ```brew install sonar-runner```)
- [xcpretty](https://github.com/supermarin/xcpretty) (see instructions below)
- [SwiftLint](https://github.com/realm/SwiftLint) ([HomeBrew](http://brew.sh) installed and ```brew install swiftlint```). Version 0.3.0 or above.
- [slather](https://github.com/SlatherOrg/slather) (```gem install slather```). Version 2.1.0 or above.
- [lizard](https://github.com/terryyin/lizard) ([PIP](https://pip.pypa.io/en/stable/installing/) installed and ```sudo pip install lizard```)

###Installation of xcpretty with JUnit reports fix

At the time, xcpretty needs to be fixed to work with SonarQube.

To install the fixed version, follow those steps :

	git clone https://github.com/Backelite/xcpretty.git
	cd xcpretty
	git checkout fix/duration_of_failed_tests_workaround
	gem build xcpretty.gemspec
	sudo gem install --both xcpretty-0.2.2.gem

###Installation (once for all your Swift projects)
- Download the plugin binary into the $SONARQUBE_HOME/extensions/plugins directory
- Copy [run-sonar-swift.sh](https://rawgithub.com/Backelite/sonar-swift/master/src/main/shell/run-sonar-swift.sh) somewhere in your PATH
- Restart the SonarQube server.

###Configuration (once per project)
- Copy [sonar-project.properties](https://raw.githubusercontent.com/Backelite/sonar-swift/master/sonar-project.properties) in your Xcode project root folder (along your .xcodeproj file)
- Edit the ```sonar-project.properties``` file to match your Xcode iOS/MacOS project

**The good news is that you don't have to modify your Xcode project to enable SonarQube!**. Ok, there might be one needed modification if you don't have a specific scheme for your test target, but that's all.

###Update (once per plugin update)
- Install the lastest plugin version
- Copy ```run-sonar-swift.sh``` somewhere in your PATH

If you still have *run-sonar-swift.sh* file in each of your project (not recommended), you will need to update all those files.

###Contributing

Feel free to contribute to this plugin by issuing pull requests to this repository.

###License

SonarQube Plugin for Swift is released under the [GNU LGPL 3 license](http://www.gnu.org/licenses/lgpl.txt).
