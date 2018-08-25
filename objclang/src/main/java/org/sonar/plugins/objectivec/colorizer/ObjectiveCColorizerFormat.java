/**
 * Objective-C Language - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package org.sonar.plugins.objectivec.colorizer;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.web.CodeColorizerFormat;
import org.sonar.colorizer.Tokenizer;
import com.sonar.objectivec.api.ObjectiveCKeyword;
import org.sonar.plugins.objectivec.core.ObjectiveC;
import com.google.common.collect.ImmutableList;

public class ObjectiveCColorizerFormat extends CodeColorizerFormat {

    public ObjectiveCColorizerFormat() {
        super(ObjectiveC.KEY);
    }

    @Override
    public List<Tokenizer> getTokenizers() {
        /*return ImmutableList.of(
                new StringTokenizer("<span class=\"s\">", "</span>"),
                new CDocTokenizer("<span class=\"cd\">", "</span>"),
                new JavadocTokenizer("<span class=\"cppd\">", "</span>"),
                new CppDocTokenizer("<span class=\"cppd\">", "</span>"),
                new KeywordsTokenizer("<span class=\"k\">", "</span>", ObjectiveCKeyword.keywordValues()));*/
        return new ArrayList<Tokenizer>();
    }

}
