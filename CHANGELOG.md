# _SonarQube Plugin for Swift_ Changelog

## 🔖 v0.3.7

- Add SwiftLint 0.25.0 support by [gaelfoppolo](https://github.com/gaelfoppolo) . See [PR 135](https://github.com/Backelite/sonar-swift/pull/135)



## 🔖 v0.3.6

- SonarQube 7 support by [Hugal31](https://github.com/Hugal31).  See [PR 125](https://github.com/Backelite/sonar-swift/pull/125)
- Lower case fastlane by [milch](https://github.com/milch). See [PR 113](https://github.com/Backelite/sonar-swift/pull/113)
- Ability to locate test files in sub-directories by [Hugal31](https://github.com/Hugal31). See [PR 123](https://github.com/Backelite/sonar-swift/pull/123)

## 🔖 v0.3.5

- SwiftLint 0.21.0 (95 rules now)
- SonarQube 6.5 support
- Fixes properties with space by [Branlute](https://github.com/Branlute). See [PR 84](https://github.com/Backelite/sonar-swift/pull/84)
- Finds project version automatically with CFBundleShortVersionString by [Branlute](https://github.com/Branlute). See [PR 87](https://github.com/Backelite/sonar-swift/pull/87)
- Fixes coverage with mixed objc swift project by [Branlute](https://github.com/Branlute). See [PR 88](https://github.com/Backelite/sonar-swift/pull/88)
- Quotes support for multi-word project names by [PetrJandak](https://github.com/PetrJandak). See [PR 97](https://github.com/Backelite/sonar-swift/pull/97)

## 🔖 v0.3.4

- SwiftLint 0.18.1 (add 8 more rules)
- Fix README headers
- Update README URLs to SonarQube

## 🔖 v0.3.3

- Updated run-sonar-swift.sh to support -usesonarscanner (for sonar-scanner instead of sonar-runner). 

## 🔖 v0.3.2

- SwiftLint 0.16.1 (75 rules now).
- Fixed [Metric 'test_data' should not be computed by a Sensor](https://github.com/Backelite/sonar-swift/issues/61) with SonarQube 6.2
- fastlane documentation update by [mammuth](https://github.com/mammuth). See [PR 62](https://github.com/Backelite/sonar-swift/pull/62)
- run-sonar-swift.sh fix by [TheSkwiggs](https://github.com/mammuth). See [PR 64](https://github.com/Backelite/sonar-swift/pull/64)

## 🔖 v0.3.1

- Now falls back to sonar-scanner if sonar-runner is not installed (thanks to [MaikoHermans](https://github.com/MaikoHermans). See [PR 59](https://github.com/Backelite/sonar-swift/pull/59))
- Ability to set *sonar.swift.appName*. Useful when basename is different from targeted scheme, or when slather fails with 'No product binary found' (thanks to [MaikoHermans](https://github.com/MaikoHermans). See [PR 58](https://github.com/Backelite/sonar-swift/pull/58))
- Added a second linter: Tailor. Enables analysis of Swift code on linux. (thanks to [tzwickl](https://github.com/tzwickl) for the hard work. See [PR 51](https://github.com/Backelite/sonar-swift/pull/51))

## 🔖 v0.3.0

- SonarQube 6 support. Important : will work with SonarQube 5.x and above only. Will not work anymore with SonarQube 4.5.x anymore.
- SwiftLint 0.13.1 support (49 rules now).
- Desactivation of unit tests and coverage is now allowed. Use **run-sonar-swift.sh -nounittests** to do it.

## 🔖 v0.2.4

- Analysis does not fail anymore when an unkwown issue is reported by SwiftLint. See [issue 35](https://github.com/Backelite/sonar-swift/issues/35)
- fastlane documentation (thanks to [viteinfinite](https://github.com/viteinfinite)). See [PR 33](https://github.com/Backelite/sonar-swift/pull/33)
- Fixed fastlane JUnit report support
- SwiftLint 0.11.1 support
- Better return code suppot for run-sonar-swift.sh

## 🔖 v0.2.3

- Fixed Lizard Sensor wrong file path

## 🔖 v0.2.2

- Added support for .xcodeproj only projects (thanks to [delannoyk](https://github.com/delannoyk))
- Fix for Lizard Sensor to find indexed files (thanks to [gretzki](https://github.com/gretzki))
- Got rid of confusion with commercial plugin in the update center

## 🔖 v0.2.1

- SwiftLint 0.8 support (new rules added).

## 🔖 v0.2.0

- Lizard complexity report support (thanks to [akshaysyaduvanshi](https://github.com/akshaysyaduvanshi))

## 🔖 v0.1.2

- SwiftLint 0.5.1 support (new rules added).
- Added *sonar.swift.simulator* key in *sonar-project.properties* to select destination simulator for running tests
- SwiftLint scans source directories only

## 🔖 v0.1.1

- SwiftLint 0.4.0 support (new rules added).

## 🔖 v0.1.0

- Initial release.
