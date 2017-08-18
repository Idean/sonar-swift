/**
 * backelite-sonar-swift-plugin - Enables analysis of Swift projects into SonarQube.
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
package org.sonar.plugins.swift.cpd;

import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokens;
import org.sonar.plugins.swift.lang.SwiftConfiguration;
import org.sonar.plugins.swift.lang.lexer.SwiftLexer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;


public class SwiftTokenizer implements Tokenizer {

    private final Charset charset;

    public SwiftTokenizer(Charset charset) {
        this.charset = charset;
    }

    public void tokenize(SourceCode source, Tokens cpdTokens) throws IOException {

        Lexer lexer = SwiftLexer.create(new SwiftConfiguration(charset));
        String fileName = source.getFileName();
        List<Token> tokens = lexer.lex(new File(fileName));
        for (Token token : tokens) {
            TokenEntry cpdToken = new TokenEntry(getTokenImage(token), fileName, token.getLine());
            cpdTokens.add(cpdToken);
        }
        cpdTokens.add(TokenEntry.getEOF());
    }

    private String getTokenImage(Token token) {
        return token.getValue();
    }
}
