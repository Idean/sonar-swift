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
