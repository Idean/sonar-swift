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
package com.backelite.sonarqube.objectivec.lang;

import org.sonar.squidbridge.api.SquidConfiguration;

import java.nio.charset.Charset;

public class ObjectiveCConfiguration extends SquidConfiguration {

    private boolean ignoreHeaderComments;

    public ObjectiveCConfiguration() {
    }

    public ObjectiveCConfiguration(Charset charset) {
        super(charset);
    }

    public boolean getIgnoreHeaderComments() {
        return ignoreHeaderComments;
    }

    public void setIgnoreHeaderComments(boolean ignoreHeaderComments) {
        this.ignoreHeaderComments = ignoreHeaderComments;
    }

}
