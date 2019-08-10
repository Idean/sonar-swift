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
package com.backelite.sonarqube.objectivec.lang;/*
 * Sonar Objective-C Plugin
 * Copyright (C) 2012 OCTO Technology, Backelite
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

import com.backelite.sonarqube.objectivec.lang.api.ObjectiveCMetric;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.sonar.squidbridge.api.SourceFile;

import java.io.File;

public class ObjectiveCAstScannerTest {

    @Test
    public void lines() {
        SourceFile file = ObjectiveCAstScanner.scanSingleFile(new File("src/test/resources/objcSample.h"));
        Assert.assertThat(file.getInt(ObjectiveCMetric.LINES), Matchers.is(17));
    }

    @Test
    public void lines_of_code() {
        SourceFile file = ObjectiveCAstScanner.scanSingleFile(new File("src/test/resources/objcSample.h"));
        Assert.assertThat(file.getInt(ObjectiveCMetric.LINES_OF_CODE), Matchers.is(5));
    }

    @Test
    public void comments() {
        SourceFile file = ObjectiveCAstScanner.scanSingleFile(new File("src/test/resources/objcSample.h"));
        Assert.assertThat(file.getInt(ObjectiveCMetric.COMMENT_LINES), Matchers.is(4));
        Assert.assertThat(file.getNoSonarTagLines(), Matchers.hasItem(10));
        Assert.assertThat(file.getNoSonarTagLines().size(), Matchers.is(1));
    }

}
