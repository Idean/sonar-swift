/**
 * Swift Language - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.swift.lang.lexer;

import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.test.lexer.LexerMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.sonar.sslr.test.lexer.LexerMatchers.hasComment;
import static com.sonar.sslr.test.lexer.LexerMatchers.hasToken;
import static org.junit.Assert.assertThat;

public class SwiftLexerTest {

    private static Lexer lexer;

    @BeforeClass
    public static void init() {
        lexer = SwiftLexer.create();
    }

    @Test
    public void testLexMultiLinesComment() {
        assertThat(lexer.lex("/* My Comment \n*/"), hasComment("/* My Comment \n*/"));
        assertThat(lexer.lex("/**/"), hasComment("/**/"));
    }

    @Test
    public void testLexInlineComment() {
        assertThat(lexer.lex("// My Comment \n new line"), hasComment("// My Comment "));
        assertThat(lexer.lex("//"), hasComment("//"));
    }

    @Test
    public void testLexEndOflineComment() {
        Assert.assertThat(lexer.lex("self.hello() // My Comment end of line"), LexerMatchers.hasComment("// My Comment end of line"));
        Assert.assertThat(lexer.lex("self.hello() //"), LexerMatchers.hasComment("//"));
    }

    @Test
    public void testLexLineOfCode() {
        Assert.assertThat(lexer.lex("self.hello()"), LexerMatchers.hasToken("self.hello()", GenericTokenType.LITERAL));
    }

    @Test
    public void testLexEmptyLine() {
        List<Token> tokens = lexer.lex("\n");
        Assert.assertThat(tokens.size(), Matchers.equalTo(1));
        Assert.assertThat(tokens, LexerMatchers.hasToken(GenericTokenType.EOF));
    }

    @Test
    public void testLexSampleFile() {
        List<Token> tokens = lexer.lex(new File("src/test/resources/Test.swift"));
        Assert.assertThat(tokens.size(), Matchers.equalTo(34));
        Assert.assertThat(tokens, LexerMatchers.hasToken(GenericTokenType.EOF));
    }
}
