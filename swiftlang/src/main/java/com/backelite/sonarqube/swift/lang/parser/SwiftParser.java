/**
 * Swift SonarQube Plugin - Swift module - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.swift.lang.parser;

import com.backelite.sonarqube.swift.lang.SwiftConfiguration;
import com.backelite.sonarqube.swift.lang.api.SwiftGrammar;
import com.backelite.sonarqube.swift.lang.lexer.SwiftLexer;
import com.sonar.sslr.impl.Parser;

public class SwiftParser {

    private SwiftParser() {

    }

    public static Parser<SwiftGrammar> create() {
        return create(new SwiftConfiguration());
    }

    public static Parser<SwiftGrammar> create(SwiftConfiguration conf) {
        return Parser.builder((SwiftGrammar) new SwiftGrammarImpl())
                .withLexer(SwiftLexer.create(conf))
                .build();
    }

}
