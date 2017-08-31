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
package org.sonar.plugins.swift.lang;

import org.junit.Test;
import org.sonar.plugins.swift.lang.api.SwiftMetric;
import org.sonar.squidbridge.api.SourceFile;

import java.io.File;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

public class SwiftAstScannerTest {

    @Test
    public void testTotalLineCount() {
        SourceFile file = SwiftAstScanner.scanSingleFile(new File("src/test/resources/Test.swift"));
        assertThat(file.getInt(SwiftMetric.LINES), is(21));
    }

    @Test
    public void testLineOfCodeCount() {
        SourceFile file = SwiftAstScanner.scanSingleFile(new File("src/test/resources/Test.swift"));
        assertThat(file.getInt(SwiftMetric.LINES_OF_CODE), is(11));
    }

    @Test
    public void testCommentLineCount() {
        SourceFile file = SwiftAstScanner.scanSingleFile(new File("src/test/resources/Test.swift"));
        assertThat(file.getInt(SwiftMetric.COMMENT_LINES), is(3));
    }
}
