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
package com.backelite.sonarqube.commons;

import com.ctc.wstx.stax.WstxInputFactory;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.InputStream;

public class StaxParser {

    private final XmlStreamHandler xmlStreamHandler;
    private final boolean isoControlCharsAwareParser;

    private static final SMInputFactory FACTORY = new SMInputFactory(new WstxInputFactory());

    static {
        //https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#XMLInputFactory_.28a_StAX_parser.29
        FACTORY.getStaxFactory().setProperty(XMLInputFactory.SUPPORT_DTD, false); // This disables DTDs entirely for that factory
        FACTORY.getStaxFactory().setProperty("javax.xml.stream.isSupportingExternalEntities", false); // disable external entities
    }

    public StaxParser(XmlStreamHandler xmlStreamHandler) {
        this.xmlStreamHandler = xmlStreamHandler;
        this.isoControlCharsAwareParser = false;
    }

    public StaxParser(XmlStreamHandler xmlStreamHandler, boolean isoControlCharsAwareParser) {
        this.xmlStreamHandler = xmlStreamHandler;
        this.isoControlCharsAwareParser = isoControlCharsAwareParser;
    }

    public void parse(File file) throws XMLStreamException {

        SMHierarchicCursor rootCursor = FACTORY.rootElementCursor(file);
        xmlStreamHandler.stream(rootCursor);
    }

    public void parse(InputStream xmlInput)  throws XMLStreamException {
        SMHierarchicCursor rootCursor = FACTORY.rootElementCursor(xmlInput);
        xmlStreamHandler.stream(rootCursor);

    }

    public interface XmlStreamHandler {
        void stream(final SMHierarchicCursor rootCursor) throws XMLStreamException;
    }
}
