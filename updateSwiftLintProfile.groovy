// Update profile-swiftlint.xml from local rules.txt
// Severity is determined from ...

import groovy.xml.MarkupBuilder
import groovy.json.JsonBuilder

def magicSerevityAttribution(rule) {

    if (rule.key.contains('variable_name')) return 'CRITICAL'
    if (rule.key.contains('nesting')) return 'CRITICAL'
    if (rule.key.contains('force')) return 'MAJOR'
    if (rule.key.contains('whitespace')) return 'MINOR'
    if (rule.key.contains('trailing')) return 'MINOR'
    if (rule.key.contains('length')) return 'MAJOR'
    if (rule.key.contains('cyclomatic')) return 'CRITICAL'

    return 'MINOR'

}

def readSwiftLintRules() {

    def result = []

    def processRules = "swiftlint rules".execute()
    // Extract rule identifiers
    processRules.text.eachLine {line ->

        def rule = [:]


        if (!line.startsWith('+')) {

            def matcher = line =~ /\| (\w+)/

            rule.key = matcher[0][1]

            if (rule.key != 'identifier') {
                result.add rule
            }
        }

    }

    // Get details of each rule
    result.each {rule ->
        def processRuleDetails = "swiftlint rules ${rule.key}".execute()
        def details = processRuleDetails.text.readLines().first()

        println "Processing rule ${rule.key}"

        def matcher = details =~ /(.*) \((\w+)\): (.*)/
        rule.category = 'SwiftLint'
        rule.name = matcher[0][1] - ' Rule'
        rule.description = matcher[0][3]
        rule.severity = magicSerevityAttribution(rule)

    }

    result

}

def writeProfileSwiftLint(rls, file) {
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)
    xml.profile() {
        name "SwiftLint"
        language "swift"
        rules {
            rls.each {rl ->
                rule {
                    repositoryKey "SwiftLint"
                    key rl.key
                }
            }
        }
    }

    file.text = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + writer.toString()

}

def writeRules(rls, file) {

    def builder = new JsonBuilder()
    builder(rls)

    file.text = builder.toPrettyString()

}

// Files
File rulesJson = new File('src/main/resources/org/sonar/plugins/swiftlint/rules.json')
File profileXml = new File('src/main/resources/org/sonar/plugins/swiftlint/profile-swiftlint.xml')

// Read rules from swiftlint_rules.txt
def rules = readSwiftLintRules()

// Write JSON rules
writeRules(rules, rulesJson)

// Write profile
writeProfileSwiftLint(rules, profileXml)