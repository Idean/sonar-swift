/**
 * Swift SonarQube Plugin - Objective-C module - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.objectivec.lang.parser;

import com.backelite.sonarqube.objectivec.lang.ObjectiveCConfiguration;
import com.backelite.sonarqube.objectivec.lang.api.ObjectiveCGrammar;
import com.backelite.sonarqube.objectivec.lang.lexer.ObjectiveCLexer;

import com.sonar.sslr.impl.Parser;

public class ObjectiveCParser {

    private ObjectiveCParser() {
    }

    public static Parser<ObjectiveCGrammar> create() {
        return create(new ObjectiveCConfiguration());
    }

    public static Parser<ObjectiveCGrammar> create(ObjectiveCConfiguration conf) {
        return Parser.builder((ObjectiveCGrammar) new ObjectiveCGrammarImpl())
                .withLexer(ObjectiveCLexer.create(conf))
                .build();
    }

}
