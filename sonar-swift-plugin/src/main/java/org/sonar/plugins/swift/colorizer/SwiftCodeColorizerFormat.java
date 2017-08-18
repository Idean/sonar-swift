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
package org.sonar.plugins.swift.colorizer;

import com.google.common.collect.ImmutableList;
import org.sonar.api.web.CodeColorizerFormat;
import org.sonar.colorizer.*;
import org.sonar.plugins.swift.lang.api.SwiftKeyword;
import org.sonar.plugins.swift.lang.core.Swift;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gillesgrousset on 27/10/2015.
 */
public class SwiftCodeColorizerFormat extends CodeColorizerFormat {

    public SwiftCodeColorizerFormat() {
        super(Swift.KEY);
    }

    @Override
    public List<Tokenizer> getTokenizers() {
        /*return ImmutableList.of(
                new StringTokenizer("<span class=\"s\">", "</span>"),
                new CDocTokenizer("<span class=\"cd\">", "</span>"),
                new JavadocTokenizer("<span class=\"cppd\">", "</span>"),
                new CppDocTokenizer("<span class=\"cppd\">", "</span>"),
                new KeywordsTokenizer("<span class=\"k\">", "</span>", SwiftKeyword.keywordValues()));*/
        return new ArrayList<Tokenizer>();

    }
}
