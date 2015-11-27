/*
 * SonarQube Swift Plugin
 * Copyright (C) 2015 Backelite
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.swift.lang.parser;

import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.impl.events.ParsingEventListener;
import org.sonar.plugins.swift.lang.SwiftConfiguration;
import org.sonar.plugins.swift.lang.api.SwiftGrammar;
import org.sonar.plugins.swift.lang.lexer.SwiftLexer;

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
