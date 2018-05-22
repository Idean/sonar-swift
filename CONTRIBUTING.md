# Contributing to _SonarQube Plugin for Swift_

The following is a set of guidelines for contributing to _SonarQube Plugin for Swift_ on GitHub.

> Above all, thank you for your interest in the project and for taking the time to contribute! 👍

## I want to report a problem or ask a question

Before submitting a new GitHub issue, please make sure to

- Check out the [documentation](https://github.com/Backelite/sonar-swift/tree/develop/docs).
- Check out the README and especially the usage guide.
- Search for [existing GitHub issues](https://github.com/Backelite/sonar-swift/issues).

If the above doesn't help, please [submit an issue](https://github.com/Backelite/sonar-swift/issues) on GitHub and provide information about your setup (SonarQube version, etc.)

**Note**: If you want to report a regression in *SonarQube Plugin for Swift* (something that has worked before, but broke with a new release), please mark your issue title as such using `[Regression] Your title here`. This enables us to quickly detect and fix regressions.

## I want to contribute to _SonarQube Plugin for Swift_

### Prerequisites

To develop _SonarQube Plugin for Swift_, you will need to meet the prerequisites as specified in the [README](https://github.com/Backelite/sonar-swift#prerequisites).

### Checking out the repository

- Make sure you are on the *develop* branch: https://github.com/Backelite/sonar-swift/tree/develop
- Click the “Fork” button in the upper right corner of repo
- Clone your fork:
    - `git clone https://github.com/<YOUR_GITHUB_USERNAME>/sonar-swift.git`
    - Learn more about how to manage your fork: <https://help.github.com/articles/working-with-forks/>
- Create a new branch to work on:
    - `git checkout -b <YOUR_BRANCH_NAME>`
    - A good name for a branch describes the thing you’ll be working on, e.g. `add-swiftlint-rules`, `fix-swift-lexer`, etc.

That’s it! Now you’re ready to work on _SonarQube Plugin for Swift_.

### Things to keep in mind

- Please do not change the minimum SonarQube version
- Always document new public methods and properties

### Testing your local changes

Before opening a pull request, please make sure your changes don't break things.

- The project and the plugin should build without warnings
- The [Swift Language Weather](https://github.com/JakeLin/SwiftLanguageWeather) project should run without issues

### Submitting the PR

When the coding is done and you’ve finished testing your changes, you are ready to submit the PR to the [main repo](https://github.com/Backelite/sonar-swift). Again, make sure you submit your PR to the *develop* branch: https://github.com/Backelite/sonar-swift/tree/develop

Some best practices are:

- Use a descriptive title
- Link the issues that are related to your PR in the body

## Code of Conduct

Help us keep _SonarQube Plugin for Swift_ open and inclusive. Please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md).

## Above All, Thanks for Your Contributions

Thank you for reading to the end, and for taking the time to contribute to the project! If you include the 🔑 emoji at the top of the body of your issue or pull request, we'll know that you've given this your full attention and are doing your best to help!

## License

This project is licensed under the terms of the GNU LGPL v3 license. See the [LICENSE](./LICENSE.md) file for more info.

_These contribution guidelines were adapted from [_fastlane_](https://github.com/fastlane/fastlane) guides._