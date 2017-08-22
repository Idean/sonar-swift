/*
 * SonarQube Swift Plugin
 * Copyright (C) 2015 Backelite
 * sonarqube@googlegroups.com
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
package org.sonar.plugins.swift.lang.api;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.TokenType;


public enum SwiftKeyword implements TokenType {

    // Annotations
    UI_APPLICATION_MAIN_ANNOTATION("@UIApplicationMain"),
    IBACTION_ANNOTATION("@IBAction"),
    IBOUTLET_ANNOTATION("@IBOutlet"),

    // Declarations
    CLASS("class"),
    DEINIT("deinit"),
    ENUM("enum"),
    EXTENSION("extension"),
    FUNC("func"),
    IMPORT("import"),
    INIT("init"),
    LET("let"),
    PROTOCOL("protocol"),
    STATIC("static"),
    STRUCT("struct"),
    SUBSCRIPT("subscript"),
    TYPEALIAS("typealias"),
    VAR("var"),

    // Statments
    BREAK("break"),
    CASE("case"),
    CONTINUE("continue"),
    DEFAULT("default"),
    DO("do"),
    ELSE("else"),
    FALLTHROUGH("fallthrough"),
    IF("if"),
    IN("in"),
    FOR("for"),
    RETURN("return"),
    SWITCH("switch"),
    WHERE("where"),
    WHILE("while"),

    // Expressions and types
    AS("as"),
    DYNAMIC_TYPE("dynamicType"),
    IS("is"),
    NEW("new"),
    SUPER("super"),
    SELF("self"),
    SELF_UPPERCASE("Self"),
    TYPE("Type"),
    COLUMN("__COLUMN__"),
    FILE("__FILE__"),
    FUNCTION("__FUNCTION__"),
    AND("and"),
    LINE("__LINE__"),

    // Specials
    ASSOCIATIVITY("associativity"),
    DID_SET("didSet"),
    GET("get"),
    INFIX("infix"),
    INOUT("inout"),
    LEFT("left"),
    MUTATING("mutating"),
    NONE("none"),
    NONMUTATING("nonmutating"),
    OPERATOR("operator"),
    OVERRIDE("override"),
    POSTFIX("postfix"),
    PRECEDENCE("precedence"),
    PREFIX("prefix"),
    RIGHT("right"),
    SET("set"),
    UNOWNED("unowned"),
    UNOWNED_SAFE("unowned(safe)"),
    UNOWNED_UNSAFE("unowned(unsafe)"),
    WEAK("weak"),
    WILL_SET("willSet"),

    TRUE("true"),
    FALSE("true");

    private final String value;

    SwiftKeyword(String value) {
        this.value = value;
    }

    public String getName() {
        return name();
    }

    public String getValue() {
        return value;
    }

    public boolean hasToBeSkippedFromAst(AstNode node) {
        return false;
    }

    public static String[] keywordValues() {
        SwiftKeyword[] keywordsEnum = SwiftKeyword.values();
        String[] keywords = new String[keywordsEnum.length];
        for (int i = 0; i < keywords.length; i++) {
            keywords[i] = keywordsEnum[i].getValue();
        }
        return keywords;
    }
}
