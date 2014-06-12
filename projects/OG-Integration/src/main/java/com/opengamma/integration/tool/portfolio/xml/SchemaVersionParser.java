/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

import java.io.Reader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Performs a parse of an XML document (via the provided Reader), in order to extract the
 * schema version. A StAX parser {@see http://docs.oracle.com/javase/tutorial/jaxp/stax/login.html} is
 * used so that the whole document is not read into memory when all the should be required is the
 * first few lines of the document.
 */
public class SchemaVersionParser {

  private static final String EXPECTED_ROOT_ELEMENT = "og-portfolio";

  private static final Logger s_logger = LoggerFactory.getLogger(SchemaVersionParser.class);

  private static final QName SCHEMA_VERSION_QNAME = new QName("schemaVersion");

  private final Reader _reader;

  /**
   * Initialise the parser with a reader holding an XML document. The reader is
   * expected to be managed by the client, and should be initialised to the start
   * of the document.
   *
   * @param reader reader to read the XML document from
   */
  public SchemaVersionParser(Reader reader) {
    _reader = reader;
  }

  /**
   * Attempt to read the schema version from the XML document. The root element
   * is found, checked and the version number extracted. If any of these operations
   * fail then an {@link OpenGammaRuntimeException} will be thrown.
   *
   * @return the schema version from the xml document
   * @throws OpenGammaRuntimeException if parsing the schema version fails
   */
  public SchemaVersion parseSchemaVersion() {

    try {
      StartElement element = findRootElement();
      checkRootElement(element, EXPECTED_ROOT_ELEMENT);
      return parseVersionFromElement(element);

    } catch (XMLStreamException e) {

      throw new OpenGammaRuntimeException("Exception whilst trying to parse XML file", e);
    }
  }

  private StartElement findRootElement() throws XMLStreamException {

    s_logger.debug("Attempting to find root element for document");

    // Work through the elements in the document until we hit a start element
    for (XMLEventReader eventReader = createXmlEventReader(); eventReader.hasNext(); ) {
      XMLEvent event = eventReader.nextEvent();
      if (event.isStartElement()) {

        // We've found the first proper element in the document, it may be
        // what we're looking for or it may be incorrect but either way we
        // don't need to read any more of the file
        s_logger.debug("Found root element: [{}]", event);
        return (StartElement) event;
      }
    }
    throw new OpenGammaRuntimeException("No root element was found - unable to parse file");
  }

  private void checkRootElement(StartElement element, String expectedName) {
    String elementName = element.getName().getLocalPart();
    if (!elementName.equals(expectedName)) {
      throw new OpenGammaRuntimeException("Root element should have name [" + expectedName +
                                              "] but instead found [" + elementName +
                                              "] - unable to parse file");
    }
  }

  private SchemaVersion parseVersionFromElement(StartElement element) {

    Attribute schemaVersion = element.getAttributeByName(SCHEMA_VERSION_QNAME);
    if (schemaVersion != null) {
      return new SchemaVersion(schemaVersion.getValue());
    } else {
      throw new OpenGammaRuntimeException("No schema version was found - unable to parse file");
    }
  }

  private XMLEventReader createXmlEventReader() throws XMLStreamException {
    XMLInputFactory factory = XMLInputFactory.newFactory();
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    return factory.createXMLEventReader(_reader);
  }
}
