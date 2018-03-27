<p align="left">
  <img src="https://www.backelite.com/wp-content/uploads/2016/09/logo-backelite-square.png" width="100"/>
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

In SonarQube under Quality Profiles the used Linter can be specified by selecting either the SwiftLint Profile or the Tailor Profile as Default profile for Swift Projects:
<p align="center">

  <img src="SwitchProfiles.png" alt="Set preferred profile (SwiftLint or Tailor) to default in SonarQube." width="100%"/>
</p>

### Features

| Feature 		| Supported	| MacOS	| Unix |
|---------------|----------|:-----------:|:-----------:|
| Complexity	|YES			|Uses [Lizard](https://github.com/terryyin/lizard)| Uses [Lizard](https://github.com/terryyin/lizard)|
| Design		|NO			|			| |
| Documentation	|YES		|			| |
| Duplications	|YES		|			| |
| Issues		|YES		| Uses [SwiftLint](https://github.com/realm/SwiftLint) and/or [Tailor](https://github.com/sleekbyte/tailor)| Uses [Tailor](https://github.com/sleekbyte/tailor)|
| Size			|YES		|			||
| Tests			|YES		| Uses xcodebuild + xcpretty [xcpretty](https://github.com/supermarin/xcpretty)	| Not Supported |
| Code coverage	|YES			| Uses [slather](https://github.com/venmo/slather)			| Not Supported|


### Download

Checkout the [Releases](https://github.com/Backelite/sonar-swift/releases) page.

### Launching an analysis
If you use [fastlane](https://fastlane.tools), please read [our fastlane integration doc](docs/sonarqube-fastlane.md).
Otherwise, run the ```run-sonar-swift.sh``` script from your Xcode project root folder

### Release history

#### 0.3.6
- SonarQube 7 support by [Hugal31](https://github.com/Hugal31).  See [PR 135](https://github.com/Backelite/sonar-swift/pull/125)
- Lower case fastlane by [milch](https://github.com/milch). See [PR 113](https://github.com/Backelite/sonar-swift/pull/113)
- Ability to locate test files in sub-directories by [Hugal31](https://github.com/Hugal31). See [PR 123](https://github.com/Backelite/sonar-swift/pull/123)


#### 0.3.5
- SwiftLint 0.21.0 (95 rules now)
- SonarQube 6.5 support
- Fixes properties with space by [Branlute](https://github.com/Branlute). See [PR 84](https://github.com/Backelite/sonar-swift/pull/84)
- Finds project version automatically with CFBundleShortVersionString by [Branlute](https://github.com/Branlute). See [PR 87](https://github.com/Backelite/sonar-swift/pull/87)
- Fixes coverage with mixed objc swift project by [Branlute](https://github.com/Branlute). See [PR 88](https://github.com/Backelite/sonar-swift/pull/88)
- Quotes support for multi-word project names by [PetrJandak](https://github.com/PetrJandak). See [PR 97](https://github.com/Backelite/sonar-swift/pull/97)

#### 0.3.4
- SwiftLint 0.18.1 (add 8 more rules)
- Fix README headers
- Update README URLs to SonarQube

#### 0.3.3
- Updated run-sonar-swift.sh to support -usesonarscanner (for sonar-scanner instead of sonar-runner). 

#### 0.3.2
- SwiftLint 0.16.1 (75 rules now).
- Fixed [Metric 'test_data' should not be computed by a Sensor](https://github.com/Backelite/sonar-swift/issues/61) with SonarQube 6.2
- fastlane documentation update by [mammuth](https://github.com/mammuth). See [PR 62](https://github.com/Backelite/sonar-swift/pull/62)
- run-sonar-swift.sh fix by [TheSkwiggs](https://github.com/mammuth). See [PR 64](https://github.com/Backelite/sonar-swift/pull/64)

#### 0.3.1
- Now falls back to sonar-scanner if sonar-runner is not installed (thanks to [MaikoHermans](https://github.com/MaikoHermans). See [PR 59](https://github.com/Backelite/sonar-swift/pull/59))
- Ability to set *sonar.swift.appName*. Useful when basename is different from targeted scheme, or when slather fails with 'No product binary found' (thanks to [MaikoHermans](https://github.com/MaikoHermans). See [PR 58](https://github.com/Backelite/sonar-swift/pull/58))
- Added a second linter: Tailor. Enables analysis of Swift code on linux. (thanks to [tzwickl](https://github.com/tzwickl) for the hard work. See [PR 51](https://github.com/Backelite/sonar-swift/pull/51))


#### 0.3.0
- SonarQube 6 support. Important : will work with SonarQube 5.x and above only. Will not work anymore with SonarQube 4.5.x anymore.
- SwiftLint 0.13.1 support (49 rules now).
- Desactivation of unit tests and coverage is now allowed. Use **run-sonar-swift.sh -nounittests** to do it.

#### 0.2.4
- Analysis does not fail anymore when an unkwown issue is reported by SwiftLint. See [issue 35](https://github.com/Backelite/sonar-swift/issues/35)
- fastlane documentation (thanks to [viteinfinite](https://github.com/viteinfinite)). See [PR 33](https://github.com/Backelite/sonar-swift/pull/33)
- Fixed fastlane JUnit report support
- SwiftLint 0.11.1 support
- Better return code suppot for run-sonar-swift.sh

#### 0.2.3
- Fixed Lizard Sensor wrong file path

#### 0.2.2
- Added support for .xcodeproj only projects (thanks to [delannoyk](https://github.com/delannoyk))
- Fix for Lizard Sensor to find indexed files (thanks to [gretzki](https://github.com/gretzki))
- Got rid of confusion with commercial plugin in the update center

#### 0.2.1
- SwiftLint 0.8 support (new rules added).

#### 0.2.0
- Lizard complexity report support (thanks to [akshaysyaduvanshi](https://github.com/akshaysyaduvanshi))

#### 0.1.2
- SwiftLint 0.5.1 support (new rules added).
- Added *sonar.swift.simulator* key in *sonar-project.properties* to select destination simulator for running tests
- SwiftLint scans source directories only

#### 0.1.1
- SwiftLint 0.4.0 support (new rules added).

#### 0.1.0
- Initial release.


### Prerequisites

- a Mac with Xcode 7 or +
- [SonarQube](https://docs.sonarqube.org/display/SONAR/Setup+and+Upgrade) and [SonarQube Scanner](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner)
- [xcpretty](https://github.com/supermarin/xcpretty) (see instructions below)
- [SwiftLint](https://github.com/realm/SwiftLint) ([HomeBrew](http://brew.sh) installed and ```brew install swiftlint```). Version 0.3.0 or above.
- [Tailor](https://github.com/sleekbyte/tailor) ([HomeBrew](http://brew.sh) installed and ```brew install tailor```). Version 0.11.1 or above.
- [slather](https://github.com/SlatherOrg/slather) (```gem install slather```). Version 2.1.0 or above (2.4 since Xcode 8.3).
- [lizard](https://github.com/terryyin/lizard) ([PIP](https://pip.pypa.io/en/stable/installing/) installed and ```sudo pip install lizard```)

### Installation of xcpretty with JUnit reports fix

At the time, xcpretty needs to be fixed to work with SonarQube.

To install the fixed version, follow those steps :

	git clone https://github.com/Backelite/xcpretty.git
	cd xcpretty
	git checkout fix/duration_of_failed_tests_workaround
	gem build xcpretty.gemspec
	sudo gem install --both xcpretty-0.2.2.gem

### Installation (once for all your Swift projects)
- Download the plugin binary into the $SONARQUBE_HOME/extensions/plugins directory
- Copy [run-sonar-swift.sh](https://rawgithub.com/Backelite/sonar-swift/master/src/main/shell/run-sonar-swift.sh) somewhere in your PATH
- Restart the SonarQube server.

### Configuration (once per project)
- Copy [sonar-project.properties](https://raw.githubusercontent.com/Backelite/sonar-swift/master/sonar-project.properties) in your Xcode project root folder (along your .xcodeproj file)
- Edit the ```sonar-project.properties``` file to match your Xcode iOS/MacOS project

**The good news is that you don't have to modify your Xcode project to enable SonarQube!**. Ok, there might be one needed modification if you don't have a specific scheme for your test target, but that's all.

### Update (once per plugin update)
- Install the lastest plugin version
- Copy ```run-sonar-swift.sh``` somewhere in your PATH

If you still have *run-sonar-swift.sh* file in each of your project (not recommended), you will need to update all those files.

### Contributing

Feel free to contribute to this plugin by issuing pull requests to this repository.

When creating a pull request: always create it for the *develop* branch. 

### License

SonarQube Plugin for Swift is released under the [GNU LGPL 3 license](http://www.gnu.org/licenses/lgpl.txt).
