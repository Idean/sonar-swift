/**
 * Swift SonarQube Plugin - Enables analysis of Swift and Objective-C projects into SonarQube.
 * Copyright © 2015 Backelite (${email})
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
// Update rules.txt and profile-clint.xml from OCLint documentation
// Severity is determined from the category

@Grab(group='org.codehaus.groovy.modules.http-builder',
        module='http-builder', version='0.7')

import groovyx.net.http.*
import groovy.xml.MarkupBuilder

def splitCamelCase(value) {
    value.replaceAll(
            String.format("%s|%s|%s",
                    "(?<=[A-Z])(?=[A-Z][a-z])",
                    "(?<=[^A-Z])(?=[A-Z])",
                    "(?<=[A-Za-z])(?=[^A-Za-z])"
            ),
            " "
    ).toLowerCase()
}


def parseCategory(url, name, severity) {

    def result = []

    def http = new HTTPBuilder(url)
    def html = http.get([:])

    def root = html."**".find { it.@id.toString().contains(name)}
    root."**".findAll { it.@class.toString() == 'section'}.each {rule ->

        def entry = [:]


        def ruleName =  splitCamelCase(rule.H2.text() - '¶').capitalize()

        // Original name
        entry.originalName = null
        try {
            def sourceHttp = new HTTPBuilder(rule."**".findAll {it.name() == 'A'}.last().@href)
            def sourceHtml = sourceHttp.get[:]

            def found = sourceHtml."**".find {it.name() == "TR" && it.text().contains("return\"")}.text()
            def match = found =~ /"([^"]*)"/
            entry.originalName = match[0][1]

        } catch (Exception e) {

        }

        if (entry.originalName) {

            // Name
            entry.name = ruleName


            println "Retrieving rule $entry.originalName"

            // Summary
            entry.summary = rule.P[1].text()

            // Severity
            entry.severity = severity

            result.add entry
        } else {
            println "Unable to retrieve rule with name $entry.name"
        }
    }

    result
}

def writeRulesTxt(rules, file) {

    def text = "Available issues:\n" +
            "\n" +
            "OCLint\n" +
            "======\n\n"

    rules.each {rule ->
        if (rule.name != '') {
            text += rule.originalName + '\n'
            text += '----------\n'
            text += '\n'

            // Summary
            text += "Summary: $rule.summary\n"
            text += '\n'

            text += "Severity: $rule.severity\n"
            text += "Category: OCLint\n"

            text += '\n'
        }
    }

    file.text = text
}

def readRulesTxt(file) {

    def result = []

    def previousLine = ''
    def rule = null
    file.eachLine {line ->

        if (line.startsWith('--')) {
            rule = [:]
            rule.originalName = previousLine.trim()
            rule.name = rule.originalName
        }

        if (line.startsWith('Summary:') && rule) {
            rule.summary = (line - 'Summary:').trim()
        }

        if (line.startsWith('Severity:') && rule) {
            rule.severity = Integer.parseInt((line - 'Severity:').trim())
        }

        if (line.startsWith('Category:') && rule) {
            rule.category = (line - 'Category:').trim()
            result.add rule
            rule = null
        }

        previousLine = line
    }

    result

}

def writeProfileOCLint(rls, file) {
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)
    xml.profile() {
        name "OCLint"
        language "objc"
        rules {
            rls.each {rl ->
                rule {
                    repositoryKey "OCLint"
                    key rl.originalName
                }
            }
        }
    }

    file.text = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + writer.toString()

}

def mergeRules(existingRules, freshRules) {

    def result = []

    // Update existing rules
    existingRules.each {rule ->

        def freshRule = freshRules.find {it.originalName?.trim() == rule.originalName?.trim()}
        if (freshRule) {

            println "Updating rule [$rule.originalName]"
            rule.severity = freshRule.severity
            rule.category = freshRule.category
            rule.summary = freshRule.summary
        }

        if (!result.find {it.originalName?.trim() == rule.originalName?.trim()}) {
            result.add rule
        } else {
            println "Skipping rule [$rule.originalName]"
        }
    }

    // Add new rules (if any)
    freshRules.each {rule ->

        def existingRule =  existingRules.find {it.originalName?.trim() == rule.originalName?.trim()}
        if (!existingRule) {
            result.add rule
        }
    }

    result
}

// Files
File rulesTxt = new File('objclang/src/main/resources/org/sonar/plugins/oclint/rules.txt')
File profileXml = new File('objclang/src/main/resources/org/sonar/plugins/oclint/profile-oclint.xml')


// Parse OCLint online documentation
def rules = []

rules.addAll parseCategory("http://docs.oclint.org/en/stable/rules/basic.html", "basic", 3)
rules.addAll parseCategory("http://docs.oclint.org/en/stable/rules/convention.html", "convention", 2)
rules.addAll parseCategory("http://docs.oclint.org/en/stable/rules/empty.html", "empty", 3)
rules.addAll parseCategory("http://docs.oclint.org/en/stable/rules/migration.html", "migration", 1)
rules.addAll parseCategory("http://docs.oclint.org/en/stable/rules/naming.html", "naming", 2)
rules.addAll parseCategory("http://docs.oclint.org/en/stable/rules/redundant.html", "redundant", 1)
rules.addAll parseCategory("http://docs.oclint.org/en/stable/rules/size.html", "size", 3)
rules.addAll parseCategory("http://docs.oclint.org/en/stable/rules/unused.html", "unused", 0)
println "${rules.size()} rules found"


// Read existing rules
def existingRules = readRulesTxt(rulesTxt)

// Update existing rules with fresh rules
def finalRules = mergeRules(existingRules, rules)

writeRulesTxt(finalRules, rulesTxt)
writeProfileOCLint(finalRules, profileXml)