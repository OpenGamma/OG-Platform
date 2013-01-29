package com.opengamma.integration.tool.portfolio;

import static org.testng.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
@Test
public class XmlPortfolioLoaderTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSchemaLocatorFails() {
    new XmlPortfolioLoader(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLoadingWithNullFileFails() {
    new XmlPortfolioLoader(createSchemaLocator()).load(null);
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
    return new XmlPortfolioLoader(createSchemaLocator()).load(new File(fileLocation + fileName));
  }

  private SchemaLocator createSchemaLocator() {
    return new SchemaLocator() {
      @Override
      public File lookupSchema(SchemaVersion version) {
        return null;
      }
    };
  }

  /**
   * Loads a set of trades/positions/portfolios from an XML format. The XML required
   * is defined in a set of versioned schema files. When the loader is invoked, the
   * schema version is parsed from the file. If the schema exists then the
   * XML document is loaded and validated against it. If no schema exists then an
   * attempt is made to find a compatible schema. If this fails, loading of the XML
   * document is not possible.
   */
  private static class XmlPortfolioLoader {

    private static final String SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    private static final String SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

    private static final Logger s_logger = LoggerFactory.getLogger(XmlPortfolioLoader.class);

    private final SchemaLocator _schemaLocator;

    private XmlPortfolioLoader(SchemaLocator schemaLocator) {
      ArgumentChecker.notNull(schemaLocator, "schemaLocator");
      _schemaLocator = schemaLocator;
    }


    public PortfolioExtractor load(File file) {

      ArgumentChecker.notNull(file, "file");
      ArgumentChecker.isTrue(file.exists(), "File: {} does not exist", file.getAbsolutePath());

      SchemaVersion version = parseSchemaVersion(file);

      // Use the schema version to lookup appropriate schema
      File schema = _schemaLocator.lookupSchema(version);

      try {
        // This is going to use DOM to parse the document which has memory/speed implications
        // if the file is large. However, it makes processing the file much easier. If we need
        // a smaller memory footprint, we should consider a StAX parser implementation.
        DocumentBuilder documentBuilder = createDocumentBuilder(schema);
        Document document = documentBuilder.parse(file);

        return new PortfolioExtractor(document);
      } catch (ParserConfigurationException | SAXException | IOException e) {
        throw new OpenGammaRuntimeException("Error parsing xml document", e);
      }
    }

    private DocumentBuilder createDocumentBuilder(File schema) throws ParserConfigurationException {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      documentBuilderFactory.setAttribute(SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
      documentBuilderFactory.setValidating(true);
      documentBuilderFactory.setAttribute(SCHEMA_SOURCE, schema);
      return documentBuilderFactory.newDocumentBuilder();
    }

    private SchemaVersion parseSchemaVersion(File file) {
      try(Reader reader = new BufferedReader(new FileReader(file))) {
        return new SchemaVersionParser(reader).parseSchemaVersion();
      } catch (IOException e) {
        throw new OpenGammaRuntimeException("File: " + file.getAbsolutePath() + " could not be opened", e);
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
