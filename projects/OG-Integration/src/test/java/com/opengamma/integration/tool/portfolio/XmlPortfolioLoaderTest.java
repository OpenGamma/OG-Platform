package com.opengamma.integration.tool.portfolio;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatters;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.beust.jcommander.internal.Maps;
import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.integration.tool.portfolio.xml.FilesystemPortfolioSchemaLocator;
import com.opengamma.integration.tool.portfolio.xml.v1_0.IdRefResolver;
import com.opengamma.integration.tool.portfolio.xml.SchemaLocator;
import com.opengamma.integration.tool.portfolio.xml.SchemaVersion;
import com.opengamma.integration.tool.portfolio.xml.SchemaVersionParser;
import com.opengamma.integration.tool.portfolio.xml.TradePositionResolver;
import com.opengamma.integration.tool.portfolio.xml.v1_0.PortfolioDocumentV1_0;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.sun.xml.internal.bind.IDResolver;

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
    new XmlPortfolioLoader(createSchemaLocatorStub()).load(null);
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
    PortfolioExtractor extractor = attemptLoadWithStandardSchema("empty_portfolio.xml");
    checkNoErrors(extractor);
    assertEquals(extractor.size(), 0);
  }

  @Test
  public void testSimplePortfolioCanBeParsed() {
    PortfolioExtractor extractor = attemptLoadWithStandardSchema("single_irs.xml");
    checkNoErrors(extractor);
    assertEquals(Iterables.size(extractor.extractPortfolioNames()), 1);
  }

  @Test
  public void test() throws XPathExpressionException {
    PortfolioExtractor extractor = attemptLoadWithStandardSchema("single_irs.xml");
    new XmlPortfolioReader(extractor).readNext();
  }


  @Test
  public void testJaxbLoad() throws JAXBException {

    JAXBContext jc = JAXBContext.newInstance(PortfolioDocumentV1_0.class);

    Unmarshaller unmarshaller = jc.createUnmarshaller();

    // Output parsing info to System.out
    unmarshaller.setEventHandler(new DefaultValidationEventHandler());

    // Resolver allows for to differentiate between trades and positions
    // that have the same id. With this a trade and position can both have
    // id = 1, yet be resolved correctly based on their context.
    unmarshaller.setProperty(IDResolver.class.getName(), new IdRefResolver());
    String fileLocation = "src/test/resources/xml_portfolios/single_irs.xml";
    PortfolioDocumentV1_0 pd = (PortfolioDocumentV1_0) unmarshaller.unmarshal(new File(fileLocation));



  }

  private PortfolioExtractor attemptLoadWithStandardSchema(String fileName) {
    return attemptLoad(new FilesystemPortfolioSchemaLocator(new File("src/main/resources/portfolio-schemas")),
                       fileName);
  }

  private void checkNoErrors(PortfolioExtractor extractor) {
    List<String> errors = extractor.getErrors();
    assertTrue(errors.isEmpty(), "Errors parsing document: " + errors);
  }

  private PortfolioExtractor attemptLoad(SchemaLocator schemaLocator, String fileName) {
    String fileLocation = "src/test/resources/xml_portfolios/";
    return new XmlPortfolioLoader(schemaLocator).load(new File(fileLocation + fileName));
  }

  private PortfolioExtractor attemptLoad(final String fileName) {
    return attemptLoad(createSchemaLocatorStub(), fileName);
  }

  private SchemaLocator createSchemaLocatorStub() {
    return new SchemaLocator() {
      @Override
      public Schema lookupSchema(SchemaVersion version) {
        return null;
      }
    };
  }

  /**
   * Loads a set of trades/_positions/portfolios from an XML format. The XML required
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
      Schema schema = _schemaLocator.lookupSchema(version);

      try {
        // This is going to use DOM to parse the document which has memory/speed implications
        // if the file is large. However, it makes processing the file much easier. If we need
        // a smaller memory footprint, we should consider a StAX parser implementation.
        ParseReporter parseReporter = new ParseReporter();
        DocumentBuilder documentBuilder = createDocumentBuilder(schema, parseReporter);
        Document document = documentBuilder.parse(file);

        if (!parseReporter.hasErrors()) {

          PortfolioExtractor extractor = new PortfolioExtractor(version, parseReporter, document);
          if (!parseReporter.hasErrors()) {
            return extractor;
          }
        }
        throw new OpenGammaRuntimeException("Error parsing xml document: " + parseReporter.errors());

      } catch (ParserConfigurationException | SAXException | IOException e) {
        throw new OpenGammaRuntimeException("Error parsing xml document", e);
      }
    }

    private DocumentBuilder createDocumentBuilder(Schema schema,
                                                  ErrorHandler errorHandler) throws ParserConfigurationException {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      documentBuilderFactory.setValidating(true);
      documentBuilderFactory.setAttribute(SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
      documentBuilderFactory.setAttribute(SCHEMA_SOURCE, schema);

      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      documentBuilder.setErrorHandler(errorHandler);
      return documentBuilder;
    }

    private SchemaVersion parseSchemaVersion(File file) {
      try(Reader reader = new BufferedReader(new FileReader(file))) {
        return new SchemaVersionParser(reader).parseSchemaVersion();
      } catch (IOException e) {
        throw new OpenGammaRuntimeException("File: " + file.getAbsolutePath() + " could not be opened", e);
      }
    }

    private static class ParseReporter implements ErrorHandler {

      private final List<String> _errors = Lists.newArrayList();

      @Override
      public void warning(SAXParseException exception) throws SAXException {
        //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override
      public void error(SAXParseException exception) throws SAXException {
        _errors.add(exception.getLocalizedMessage());
      }

      public void error(Exception exception) {
        _errors.add(exception.getLocalizedMessage());
      }

      @Override
      public void fatalError(SAXParseException exception) throws SAXException {
        _errors.add(exception.getLocalizedMessage());
      }

      public List<String> errors() {
        return _errors;
      }

      public boolean hasErrors() {
        return !_errors.isEmpty();
      }

      public void error(String error) {
        _errors.add(error);
      }
    }
  }

  private static class PortfolioExtractor {

    private static final Logger s_logger = LoggerFactory.getLogger(PortfolioExtractor.class);

    private final SchemaVersion _version;
    private final XmlPortfolioLoader.ParseReporter _parseReporter;
    private final Document _document;
    private final XPath _xpath;
    private int _size;
    private TradePositionResolver _resolver;

    public PortfolioExtractor(SchemaVersion version,
                              XmlPortfolioLoader.ParseReporter parseReporter,
                              Document document) {
      _version = version;
      _parseReporter = parseReporter;
      _document = document;

      XPathFactory xpf = XPathFactory.newInstance();
      _xpath = xpf.newXPath();

      try {
        // Retrieve all trade ids regardless of trade type
        Set<String> tradeIds = extractTradeIds(_xpath);
        _size = tradeIds.size();

        Set<String> positionIds = extractPositionIds(_xpath);

        _resolver = new TradePositionResolver(tradeIds);

        for (String positionId : positionIds) {

          Set<String> tradesForPosn = extractPositionTradeIds(_xpath, positionId);
          for (String tradeId : tradesForPosn) {
            _resolver.addToPosition(positionId, tradeId);
          }
        }

        _resolver.resolve();

        for (String unknown : _resolver.getUnknownTrades()) {
          parseReporter.error("File contained position with trade id: " + unknown + " but no matching trade");
        }

        for (Map.Entry<String, Collection<String>> entry : _resolver.getDuplicateTrades().asMap().entrySet()) {
          parseReporter.error("Trade id: [" + entry.getKey() +
                                  "] appears in multiple _positions: [" + entry.getValue() +
                                  "] but should only appear in one");
        }
      } catch (XPathExpressionException e) {
        parseReporter.error(e);
      }
    }

    private Set<String> extractPositionTradeIds(XPath xpath, String positionId) throws XPathExpressionException {
      XPathExpression expression = xpath.compile("/og-portfolio/positions/position[@id='" + positionId + "']/tradeRefs/tradeRef/@ref");
      NodeList positionList = (NodeList) expression.evaluate(_document, XPathConstants.NODESET);
      Set<String> tradeIds = Sets.newHashSet();
      for (int i = 0; i < positionList.getLength(); i++) {
        tradeIds.add(positionList.item(i).getNodeValue());
      }
      return tradeIds;
    }

    private Set<String> extractPositionIds(XPath xpath) throws XPathExpressionException {
      XPathExpression expression = xpath.compile("/og-portfolio/positions/position/@id");
      NodeList positionList = (NodeList) expression.evaluate(_document, XPathConstants.NODESET);
      Set<String> positionIds = Sets.newHashSet();
      for (int i = 0; i < positionList.getLength(); i++) {
        positionIds.add(positionList.item(i).getNodeValue());
      }
      return positionIds;
    }

    private Set<String> extractTradeIds(XPath xpath) throws XPathExpressionException {
      XPathExpression expression = xpath.compile("/og-portfolio/trades/child::*/@id");
      NodeList tradeList = (NodeList) expression.evaluate(_document, XPathConstants.NODESET);
      Set<String> tradeIds = Sets.newHashSet();
      for (int i = 0; i < tradeList.getLength(); i++) {
        tradeIds.add(tradeList.item(i).getNodeValue());
      }
      return tradeIds;
    }

    private void handleTrade(Node item) {

      String tradeType = item.getLocalName();
      switch (tradeType) {
        case "swapTrade":
          s_logger.debug("Handling a swap trade");
          break;
        default:
          s_logger.warn("unable to handle trade with type: {}", tradeType);
      }
    }

    public int size() {
      return _size;
    }

    public boolean hasErrors() {
      return !_parseReporter.errors().isEmpty();
    }

    public List<String> getErrors() {
      return _parseReporter.errors();
    }

    public Iterable<String> extractPortfolioNames() {

      //_resolver.getPortfolios()

      String portfolioName = "my_first_portfolio";
      ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
      ManageablePortfolio pf = new ManageablePortfolio(portfolioName, rootNode);

      //rootNode.addPosition();



      // Positions not referenced by a portfolio will all be added to a default portfolio

      // Positions can validly be referenced by multiple portfolios

      return null;  //To change body of created methods use File | Settings | File Templates.
    }


    public Set<Node> getPositionNodes() throws XPathExpressionException {

      XPathExpression expression = _xpath.compile("/og-portfolio/positions/position");
      NodeList positionList = (NodeList) expression.evaluate(_document, XPathConstants.NODESET);
      Set<Node> positionNodes = Sets.newHashSet();
      for (int i = 0; i < positionList.getLength(); i++) {
        positionNodes.add(positionList.item(i));
      }
      return positionNodes;
    }

    public List<Node> getTradeNodes(String positionId) throws XPathExpressionException {

      //XPathExpression expression = _xpath.compile("/og-portfolio/trades/child::*[id=x|y|z]");

      List<Node> tradeNodes = Lists.newArrayList();
      Collection<String> tradeIds =_resolver.getPositions().get(positionId);

      for (String tradeId : tradeIds) {
        // Doing this inside a loop may well be horribly inefficient!!
        XPathExpression expression = _xpath.compile("/og-portfolio/trades/child::*[@id='" + tradeId + "']");
        NodeList tradeList = (NodeList) expression.evaluate(_document, XPathConstants.NODESET);

        tradeNodes.add(tradeList.item(0));
      }

      return tradeNodes;
    }
  }

  public static class XmlPortfolioReader implements PortfolioReader {

    private static final Logger s_logger = LoggerFactory.getLogger(XmlPortfolioReader.class);

    private final PortfolioExtractor _portfolioExtractor;

    private final Iterator<Node> _positions;

    public XmlPortfolioReader(PortfolioExtractor portfolioExtractor) throws XPathExpressionException {
      _portfolioExtractor = portfolioExtractor;
      this._positions = _portfolioExtractor.getPositionNodes().iterator();
    }


    @Override
    public ObjectsPair<ManageablePosition, ManageableSecurity[]> readNext() {

      if (_positions.hasNext()) {

        Node posn = _positions.next();

        List<Node> trades = null;
        try {
          trades = _portfolioExtractor.getTradeNodes(posn.getAttributes().getNamedItem("id").getNodeValue());
        } catch (XPathExpressionException e) {
          e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        // Build the underlying security
        ManageableSecurity[] securities = constructSecurity(posn, trades);

        if (securities != null && securities.length > 0 && securities[0] != null) {

          // Build the position and trade(s) using security[0] (underlying)
          ManageablePosition position = null;//constructPosition(posn, securities[0]);
          if (position != null) {
            ManageableTrade trade = null;//constructTrade(posn, securities[0], position);
            if (trade != null) {
              position.addTrade(trade);
            }
          }
          return new ObjectsPair<>(position, securities);

        } else {
          s_logger.warn("Unable to construct a security from data: " + posn);
          return new ObjectsPair<>(null, null);
        }
      }
      else {
        return null;
      }
    }

    private ManageableSecurity[] constructSecurity(Node position, List<Node> trades) {

      // Security data may be on the position, if not we need to look on the trade
      ManageableSecurity[] securities = deriveSecurityFromPosition(position);
      return securities != null ? securities : deriveSecuritiesFromTrade(trades.get(0));
    }

    private ManageableSecurity[] deriveSecuritiesFromTrade(Node trade) {


      TradeAttributes tradeAttributes = new TradeAttributes(trade);

      switch (trade.getLocalName()) {

        case "swapTrade":

          SwapAttributes swapLegs = new SwapAttributes(trade);

          SwapSecurity security = new SwapSecurity(tradeAttributes.getTradeDate(),
                                                   tradeAttributes.getEffectiveDate(),
                                                   tradeAttributes.getMaturityDate(),
                                                   tradeAttributes.getCounterparty(),
                                                   swapLegs.getPayLeg(), swapLegs.getReceiveLeg());

          //security.setExternalIdBundle(ExternalIdBundle.of(TradeLoadUtils.generateSecurityId()));

          return new ManageableSecurity[]{security};

        default:

      }


      return new ManageableSecurity[0];  //To change body of created methods use File | Settings | File Templates.
    }

    private ManageableSecurity[] deriveSecurityFromPosition(Node position) {
      return null;
    }

    @Override
    public String[] getCurrentPath() {
      return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getPortfolioName() {
      return null;
    }

    /**
     * Parse a trade XML node, extracting the trade attributes required.
     */
    private static class TradeAttributes {

      private static final DateTimeFormatter _DATE_FORMATTER = DateTimeFormatters.pattern("yyyy-MM-dd");

      private ZonedDateTime _tradeDate;
      private ZonedDateTime _effectiveDate;
      private ZonedDateTime _maturityDate;
      private String _counterparty;

      public TradeAttributes(Node trade) {

        NodeList childNodes = trade.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {

          Node child = childNodes.item(i);

          if (child.getNodeType() == Node.ELEMENT_NODE) {

            String nodeValue = child.getNodeValue();

            switch (child.getLocalName()) {

              case "tradeDate":
                _tradeDate = parseDate(child.getFirstChild().getNodeValue());
                break;
              case "effectiveDate":
                _effectiveDate = parseDate(child.getFirstChild().getNodeValue());
                break;
              case "maturityDate":
                _maturityDate = parseDate(child.getFirstChild().getNodeValue());
                break;
              case "counterparty":
                _counterparty = child.getFirstChild().getNodeValue();
                break;
              default:
                // Ignore this element
            }
          }
        }
      }

      private ZonedDateTime parseDate(String nodeValue) {
        return _DATE_FORMATTER.parse(nodeValue, LocalDate.class).atStartOfDay(ZoneOffset.UTC);
      }

      public ZonedDateTime getTradeDate() {
        return _tradeDate;
      }

      public ZonedDateTime getEffectiveDate() {
        return _effectiveDate;
      }

      public ZonedDateTime getMaturityDate() {
        return _maturityDate;
      }

      public String getCounterparty() {
        return _counterparty;
      }
    }

    private static class SwapAttributes {

      private SwapLeg _receiveLeg;
      private SwapLeg _payLeg;

      public SwapAttributes(Node trade) {

        NodeList childNodes = trade.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {

          Node child = childNodes.item(i);

          switch (child.getLocalName()) {

            case "fixedLeg":
              parseFixedLeg(child);
              break;
            case "floatingLeg":
              parseFloatingLeg(child);
              break;
            default:
              // Do nothing
          }

        }
      }

      private void parseFloatingLeg(Node leg) {


        Map<String, Node> mappedChildren = mapChildren(leg);




          /*
          <frequency></frequency>
          <dayCount></dayCount>
          <businessDayAdjustment></businessDayAdjustment>
          <interestCalculation>Adjusted</interestCalculation>
          <scheduleGenerationDirection>Backward</scheduleGenerationDirection>
          <endOfMonth>True</endOfMonth>
          <isIMM>True</isIMM>
          <paymentCalendars>
            <calendar type="Bank"> <!-- default=BANK  -->
              <id scheme="CountryISO2">FD</id>
            </calendar>
            <calendar type="Bank"> <!-- default=BANK  -->
              <id scheme="CountryISO2">EN</id>
            </calendar>
          </paymentCalendars>
          <shortLongCoupon>Long</shortLongCoupon>
          <rate>105.25</rate>
           */

        boolean isPayLeg = mappedChildren.get("payReceive").getNodeValue().equals("Pay");

        Currency currency = Currency.of(mappedChildren.get("currency").getNodeValue());
        double notionalAmount = Double.parseDouble(mappedChildren.get("notional").getNodeValue());
        Notional notional = new InterestRateNotional(currency, notionalAmount);

        Node calendars = mappedChildren.get("paymentCalendars");
        List<String>  calendarRegions = extractCalendarRegions(calendars);

        ExternalId region = ExternalSchemes.financialRegionId(StringUtils.join(calendarRegions, "+"));
        DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(mappedChildren.get("dayCount").getNodeValue());
        Frequency frequency = SimpleFrequencyFactory.INSTANCE.getFrequency(mappedChildren.get("frequency").getNodeValue());
        BusinessDayConvention businessDayConvention =
            BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(mappedChildren.get("businessDayConvention").getNodeValue());
        boolean isEndOfMonth = Boolean.valueOf(mappedChildren.get("endOfMonth").getNodeValue());

        Node fixingIndex = mappedChildren.get("fixingIndex");
        Map<String, Node> fixingChildren = mapChildren(fixingIndex);
        ExternalId referenceRate = parseExternalId(fixingChildren.get("id"));
        FloatingRateType rateType = FloatingRateType.valueOf(fixingChildren.get("rateType").getNodeValue());


        SwapLeg floatingLeg = new FloatingInterestRateLeg(dayCount, frequency, region, businessDayConvention,
                                                          notional, isEndOfMonth, referenceRate,
                                                          rateType);
        if (isPayLeg) {
          _payLeg = floatingLeg;
        }
        else {
          _receiveLeg = floatingLeg;
        }

      }

      private List<String> extractCalendarRegions(Node calendars) {

        List<String> regions = Lists.newArrayList();
        for (Node child = calendars.getFirstChild(); child != null; child.getNextSibling()) {

          regions.add(child.getFirstChild().getNodeValue());
        }
        return regions;
      }

      private Map<String, Node> mapChildren(Node node) {

        Map<String, Node> children = Maps.newHashMap();

        // As internally the nodes are held in a linked list, iterating by
        // sibling is better than attempting index access
        for (Node child = node.getFirstChild(); child != null; child.getNextSibling()) {

          children.put(child.getNodeName(), child);
        }

        return children;
      }

      private void parseFixedLeg(Node leg) {
        //To change body of created methods use File | Settings | File Templates.
      }

      private ExternalId parseExternalId(Node identifierNode) {
        /*
        <identifierNode scheme="BLOOMBERG_TICKER">US0003M Curncy</identifierNode>
        */

        String scheme = identifierNode.getAttributes().getNamedItem("scheme").getNodeValue();
        String identifier = identifierNode.getFirstChild().getNodeValue();

        return ExternalId.of(scheme, identifier);
      }


      public SwapLeg getReceiveLeg() {
        return _receiveLeg;
      }

      public SwapLeg getPayLeg() {
        return _payLeg;
      }
    }
  }
}
