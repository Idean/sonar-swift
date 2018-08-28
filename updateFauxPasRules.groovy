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
// Update profile-fauxpas.xml from Faux Pas online rules documentation
// Severity is determined from the category

import groovy.json.JsonBuilder
import groovy.xml.MarkupBuilder
@Grab(group = 'org.codehaus.groovy.modules.http-builder',
        module = 'http-builder', version = '0.7')

import groovyx.net.http.*
@Grab(group = 'org.codehaus.groovy.modules.http-builder',
        module = 'http-builder', version = '0.7')

import groovyx.net.http.*

def parseRules(url, catMapping) {

    def result = []

    def http = new HTTPBuilder(url)
    http.contentEncoding = ContentEncoding.Type.GZIP
    def html = http.get(contentType: 'text/html;charset=UTF-8')

    def categories = html."**".findAll { it.@class.toString().contains('tag-section') }
    categories.each { cat ->

        def rules = cat."**".findAll { it.@class.toString().contains('rule') }
        rules.each { r ->

            def k = r."**".find { it.@class.toString().contains("short-name") }.text()

            def rule = [
                    category   : cat.H2.text(),
                    key        : k,
                    name       : (r.H3.text().trim().replaceAll('\\n', ' ') - k).trim(),
                    description: r."**".find {
                        it.@class.toString().contains("description")
                    }.text().trim().replaceAll('\\n', ' '),
                    severity   : catMapping[cat.H2.text()]
            ]

            result.add(rule)
        }
    }



    return result
}

def writeProfileFauxPas(rls, file) {
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)
    xml.profile() {
        name "FauxPas"
        language "objc"
        rules {
            rls.each { rl ->
                rule {
                    repositoryKey "FauxPas"
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
File rulesJson = new File('objclang/src/main/resources/org/sonar/plugins/fauxpas/rules.json')
File profileXml = new File('objclang/src/main/resources/org/sonar/plugins/fauxpas/profile-fauxpas.xml')

// Parse online documentation
def rules = parseRules('http://fauxpasapp.com/rules/', [
        BestPractice : 'MAJOR',
        Resources    : 'MAJOR',
        Config       : 'MINOR',
        Localization : 'MAJOR',
        APIUsage     : 'CRITICAL',
        VCS          : 'INFO',
        Style        : 'MAJOR',
        Pedantic     : 'MINOR',
        Miscellaneous: 'MINOR'
])

// Write profile
writeProfileFauxPas(rules, profileXml)

// Write rules
writeRules(rules, rulesJson)