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

import com.ctc.wstx.stax.WstxInputFactory;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import java.io.*;

public class StaxParser {
    private SMInputFactory inf;
    private StaxParser.XmlStreamHandler streamHandler;

    public StaxParser(StaxParser.XmlStreamHandler streamHandler) {
        this.streamHandler = streamHandler;
        XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
        if (xmlFactory instanceof WstxInputFactory) {
            WstxInputFactory wstxInputfactory = (WstxInputFactory) xmlFactory;
            wstxInputfactory.configureForLowMemUsage();
            wstxInputfactory.getConfig().setUndeclaredEntityResolver(new StaxParser.UndeclaredEntitiesXMLResolver());
        }

        xmlFactory.setProperty("javax.xml.stream.isValidating", false);
        xmlFactory.setProperty("javax.xml.stream.supportDTD", false);
        xmlFactory.setProperty("javax.xml.stream.isNamespaceAware", false);
        this.inf = new SMInputFactory(xmlFactory);
    }

    public void parse(File xmlFile) throws XMLStreamException {
        try(InputStream input = new FileInputStream(xmlFile)) {
            this.parse(input);
        } catch (FileNotFoundException ex) {
            throw new XMLStreamException(ex);
        } catch (IOException ex){
            throw new XMLStreamException(ex);
        }
    }

    public void parse(InputStream xml) throws XMLStreamException {
        SMHierarchicCursor rootCursor = this.inf.rootElementCursor(xml);
        try {
            this.streamHandler.stream(rootCursor);
        } finally {
            rootCursor.getStreamReader().closeCompletely();
        }
    }

    public interface XmlStreamHandler {
        void stream(SMHierarchicCursor var1) throws XMLStreamException;
    }

    private static class UndeclaredEntitiesXMLResolver implements XMLResolver {
        private UndeclaredEntitiesXMLResolver() { }

        public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace) throws XMLStreamException {
            if (namespace != null && namespace.toLowerCase().startsWith("u") && namespace.length() == 5) {
                int unicodeCharHexValue = Integer.parseInt(namespace.substring(1), 16);
                if (Character.isDefined(unicodeCharHexValue)) {
                    namespace = new String(new char[]{(char) unicodeCharHexValue});
                }
            }
            return namespace;
        }
    }
}
