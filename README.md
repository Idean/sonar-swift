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

SCREENSHOT

TBD




###Features

- [ ] Complexity
- [ ] Design
- [x] Documentation
- [x] Duplications
- [x] Issues (SwiftLint)
- [x] Size
- [x] Tests
- [ ] Code coverage (soon)


###Download

TBD

###Prerequisites

- a Mac with Xcode 7 or +
- [SonarQube](http://docs.codehaus.org/display/SONAR/Setup+and+Upgrade) and [SonarQube Runner](http://docs.codehaus.org/display/SONAR/Installing+and+Configuring+SonarQube+Runner) installed ([HomeBrew](http://brew.sh) installed and ```brew install sonar-runner```)
- [xcpretty](https://github.com/supermarin/xcpretty) (```gem install xcpretty```)
- [SwiftLint](https://github.com/realm/SwiftLint) ([HomeBrew](http://brew.sh) installed and ```brew install swiftlint```). Version 0.3.0 or above.
- [slather](https://github.com/venmo/slather) ```gem install slather```

###Installation (once for all your Swift projects)
- Download the plugin binary into the $SONARQUBE_HOME/extensions/plugins directory
- Copy [run-sonar.sh](https://rawgithub.com/Backelite/sonar-swift/master/src/main/shell/run-sonar-swift.sh) somewhere in your PATH
- Restart the SonarQube server.

###Configuration (once per project)
- Copy [sonar-project.properties](https://rawgithub.com/Backelite/sonar-objective-c/master/sample/sonar-project.properties) in your Xcode project root folder (along your .xcodeproj file)
- Edit the ```sonar-project.properties``` file to match your Xcode iOS/MacOS project

**The good news is that you don't have to modify your Xcode project to enable SonarQube!**. Ok, there might be one needed modification if you don't have a specific scheme for your test target, but that's all.

###Analysis
- Run the script ```run-sonar-swift.sh``` in your Xcode project root folder
- Enjoy or file an issue!

###Update (once per plugin update)
- Install the lastest plugin version
- Copy ```run-sonar-swift.sh``` somewhere in your PATH

If you still have *run-sonar-swift.sh* file in each of your project (not recommended), you will need to update all those files.

###Contributing

Feel free to contribute to this plugin by issuing pull requests to this repository.

###License

SonarQube Plugin for Swift is released under the [GNU LGPL 3 license](http://www.gnu.org/licenses/lgpl.txt).
