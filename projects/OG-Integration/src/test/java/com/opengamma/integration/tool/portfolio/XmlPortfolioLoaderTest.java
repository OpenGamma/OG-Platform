package com.opengamma.integration.tool.portfolio;

import static org.testng.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Test the loading of an xml portfolio file
 */
public class XmlPortfolioLoaderTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLoadingWithNullFileFails() {
    new XmlPortfolioLoader().load(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLoadingWithNonExistentFileFails() {
    attemptLoad("this is not the file you are looking for");
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testLoadingFileWithWrongRootElementFails() {
    attemptLoad("wrong_root_element.xml");
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testLoadingFileWithNoSchemaVersionFails() {
    attemptLoad("empty_portfolio_no_version.xml");
  }

  @Test
  public void testEmptyPortfolioCanBeParsed() {
    PortfolioExtractor extractor = attemptLoad("empty_portfolio.xml");
    assertEquals(extractor.size(), 0);
  }

  @Test
  public void testSimplePortfolioCanBeParsed() {
    PortfolioExtractor extractor = attemptLoad("simple_irs_portfolio.xml");
    assertEquals(extractor.size(), 1);
  }

  private PortfolioExtractor attemptLoad(final String fileName) {
    String fileLocation = "src/test/resources/xml_portfolios/";
    return new XmlPortfolioLoader().load(new File(fileLocation + fileName));
  }

  private static class XmlPortfolioLoader {

    private static final Logger s_logger = LoggerFactory.getLogger(XmlPortfolioLoader.class);

    public PortfolioExtractor load(File file) {

      ArgumentChecker.notNull(file, "file");
      ArgumentChecker.isTrue(file.exists(), "File: {} does not exist", file.getAbsolutePath());

      SchemaVersion version = parseSchemaVersion(file);

      // Use the schema version to lookup appropriate schema
      String schemaLocation = lookupSchema(version);

      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();


      try {


        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        return new PortfolioExtractor(document);
      } catch (ParserConfigurationException | SAXException | IOException e) {
        throw new OpenGammaRuntimeException("Error parsing xml document", e);
      }
    }

    private String lookupSchema(SchemaVersion version) {
      return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private SchemaVersion parseSchemaVersion(File file) {
      try(Reader reader = new BufferedReader(new FileReader(file))) {
        return new SchemaVersionParser(reader).parseSchemaVersion();
      } catch (IOException e) {
        throw new OpenGammaRuntimeException("File: " + file.getAbsolutePath() + " could not be opened", e);
      }
    }

    private class SchemaVersionParser {

      private final Reader _reader;

      public SchemaVersionParser(Reader reader) {
        _reader = reader;
      }

      public SchemaVersion parseSchemaVersion() {

        try {
          StartElement element = findRootElement();
          checkRootElement(element, "og-portfolio");
          return parseVersionFromElement(element);

        } catch (XMLStreamException e) {

          throw new OpenGammaRuntimeException("Exception whilst trying to parse XML file", e);
        }
      }

      private StartElement findRootElement() throws XMLStreamException {

        // Work through the elements in the document until we hit a start element
        for (XMLEventReader eventReader = createXmlEventReader(); eventReader.hasNext();) {
          XMLEvent event = eventReader.nextEvent();
          if (event.isStartElement()) {

            // We've found the first proper element in the document, it may be
            // what we're looking for or it may be incorrect but either way we
            // don't need to read any more of the file
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

          Attribute schemaVersion = element.getAttributeByName(new QName("schemaVersion"));
          if (schemaVersion != null) {
            return new SchemaVersion(schemaVersion.getValue());
          }
          else {
            throw new OpenGammaRuntimeException("No schema version was found - unable to parse file");
          }
      }

      private XMLEventReader createXmlEventReader() throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        return factory.createXMLEventReader(_reader);
      }
    }
  }

  private static class PortfolioExtractor {

    private static final Logger s_logger = LoggerFactory.getLogger(PortfolioExtractor.class);

    private final int _size;

    public PortfolioExtractor(Document document) {

      XPathFactory xpf = XPathFactory.newInstance();
      XPath xpath = xpf.newXPath();
      try {
        XPathExpression expression = xpath.compile("/og-portfolio/trades/trade");
        NodeList nodeList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
        _size = nodeList.getLength();
      } catch (XPathExpressionException e) {
        throw new OpenGammaRuntimeException("Error parsing xml document", e);
      }
    }

    public int size() {
      return _size;
    }
  }

}
