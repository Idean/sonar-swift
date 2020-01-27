# README

# Installation

```sh
gem build analyzer.gemspec --output=analyzer.gem
```
```sh
gem install analyzer.gem
```

# Usage

```sh
analyzer
```

To see available options: `analyzer -h`

# Requirements

* Xcode
* XCPretty
* Slather
* SwiftLint
* Lizard
* OCLint
* FauxPas
* SonarScanner

## Behavior

When a tool is not installed, the analyzer will gracefully skip it.