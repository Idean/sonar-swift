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
// Update profile-tailor.xml from local rules.txt
// Severity is determined from ...

import groovy.xml.MarkupBuilder

import java.awt.geom.Line2D
import groovy.json.JsonBuilder

def magicSerevityAttribution(rule) {

	if (rule.key.contains('lower-camel-case')) return 'CRITICAL'
	if (rule.key.contains('constant-naming')) return 'CRITICAL'
	if (rule.key.contains('upper-camel-case')) return 'CRITICAL'
	if (rule.key.contains('force')) return 'MAJOR'
	if (rule.key.contains('whitespace')) return 'MINOR'
	if (rule.key.contains('trailing')) return 'MINOR'
	if (rule.key.contains('length')) return 'MAJOR'
	if (rule.key.contains('cyclomatic')) return 'CRITICAL'

	return 'MINOR'

}

def readTailorRules() {

	def result = []
	def descriptionKey = "Description: "
	def styleGuideKey = "Style Guide: "
	def linesToSkip = 2

	def processRules = "tailor --show-rules".execute()

	def rule = [:]

	// Extract rule identifiers
	processRules.text.eachLine {line ->

		// skip lines
		if (linesToSkip != 0) {
			linesToSkip--
			return
		}

		if (line == "") {
			// skip blank lines
			result.add rule
			println "Added new rule ${rule.key}"
			rule = [:]
			return
		}

		if (line.startsWith(descriptionKey)) {
			// add description
			def desc = line.split(descriptionKey)
			rule.description = desc[1]
		} else if (line.startsWith(styleGuideKey)) {
			// add style guide
			def styleGuide = line.split(styleGuideKey)
			rule.styleguide = styleGuide[1]
		} else {
			// add key
			rule.key = line
			rule.category = "Tailor"

			// create a nice name for this rule
			def names = line.split("-")
			rule.name = names.collect { name ->

				def chars = name.toCharArray()
				chars[0] = Character.toUpperCase(chars[0])
				chars.toString()

			}.join(" ")

			// set severity of this rule
			rule.severity = magicSerevityAttribution(rule)
		}
	}

	result

}

def writeProfileTailor(rls, file) {

	def writer = new StringWriter()
	def xml = new MarkupBuilder(writer)
	xml.profile() {
		name "Tailor"
		language "swift"
		rules {
			rls.each {rl ->
				rule {
					repositoryKey "Tailor"
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
File rulesJson = new File('swiftlang/src/main/resources/org/sonar/plugins/tailor/rules.json')
File profileXml = new File('swiftlang/src/main/resources/org/sonar/plugins/tailor/profile-tailor.xml')

// Read rules from swiftlint_rules.txt
def rules = readTailorRules()

// Write JSON rules
writeRules(rules, rulesJson)

// Write profile
writeProfileTailor(rules, profileXml)