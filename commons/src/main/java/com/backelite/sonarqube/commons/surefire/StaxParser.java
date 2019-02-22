/**
 * commons - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.commons.surefire;

import org.codehaus.staxmate.SMInputFactory;
import com.ctc.wstx.stax.WstxInputFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.in.SMHierarchicCursor;

public class StaxParser {

    private SMInputFactory inf;
    private SurefireStaxHandler streamHandler;

    public StaxParser(UnitTestIndex index) {
        this.streamHandler = new SurefireStaxHandler(index);
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        if (xmlInputFactory instanceof WstxInputFactory) {
            WstxInputFactory wstxInputfactory = (WstxInputFactory) xmlInputFactory;
            wstxInputfactory.configureForLowMemUsage();
            wstxInputfactory.getConfig().setUndeclaredEntityResolver((String publicID, String systemID, String baseURI, String namespace) -> namespace);
        }
        this.inf = new SMInputFactory(xmlInputFactory);
    }

    public void parse(File xmlFile) throws XMLStreamException {
        try(FileInputStream input = new FileInputStream(xmlFile)) {
            parse(inf.rootElementCursor(input));
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    private void parse(SMHierarchicCursor rootCursor) throws XMLStreamException {
        try {
            streamHandler.stream(rootCursor);
        } finally {
            rootCursor.getStreamReader().closeCompletely();
        }
    }
}
