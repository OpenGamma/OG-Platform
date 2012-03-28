/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.loader;

import static com.opengamma.util.functional.Functional.map;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.loader.BloombergBulkSecurityLoader;
import com.opengamma.bbg.loader.BloombergSecurityLoader;
import com.opengamma.bbg.tool.BloombergToolContext;
import com.opengamma.bloombergexample.tool.AbstractExampleTool;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.functional.Function1;

/**
 * Example code to load a very simple equity portfolio.
 * <p>
 * This code is kept deliberately as simple as possible.
 * There are no checks for the securities or portfolios already existing, so if you run it
 * more than once you will get multiple copies portfolios and securities with the same names.
 * It is designed to run against the HSQLDB example database.
 */
public class ExampleEquityPortfolioLoader extends AbstractExampleTool {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleEquityPortfolioLoader.class);

  private static final Map<String, String> SECTORS = new HashMap<String, String>();

  static {
    SECTORS.put("10", "10 Energy");
    SECTORS.put("15", "15 Materials");
    SECTORS.put("20", "20 Industrials");
    SECTORS.put("25", "25 Consumer discretionary");
    SECTORS.put("30", "30 Consumer staples");
    SECTORS.put("35", "35 Health care");
    SECTORS.put("40", "40 Financials");
    SECTORS.put("45", "45 Information technology");
    SECTORS.put("50", "50 Telecommunication");
    SECTORS.put("55", "55 Utilities");
  }

  /**
   * The name of the portfolio.
   */
  public static final String PORTFOLIO_NAME = "Example Equity Portfolio";

  //-------------------------------------------------------------------------

  /**
   * Main method to run the tool.
   * No arguments are needed.
   *
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new ExampleEquityPortfolioLoader().initAndRun(args);
    System.exit(0);
  }
  
  protected void createPortfolio(Collection<EquitySecurity> securities) {

    // create shell portfolio
    final ManageablePortfolio portfolio = createEmptyPortfolio();
    final ManageablePortfolioNode rootNode = portfolio.getRootNode();

    loadSecurities(
      map(securities, new Function1<EquitySecurity, ExternalIdBundle>() {
        @Override
        public ExternalIdBundle execute(EquitySecurity security) {
          return security.getExternalIdBundle();
        }
      }));

    // add each security to the portfolio
    for (EquitySecurity security : securities) {
      GICSCode gics = security.getGicsCode();
      if (gics == null || gics.isPartial()) {
        continue;
      }
      String sector = SECTORS.get(gics.getSectorCode());
      String industryGroup = gics.getIndustryGroupCode();
      String industry = gics.getIndustryCode();
      String subIndustry = gics.getSubIndustryCode();

      // create portfolio structure
      ManageablePortfolioNode sectorNode = rootNode.findNodeByName(sector);
      if (sectorNode == null) {
        s_logger.debug("Creating node for sector {}", sector);
        sectorNode = new ManageablePortfolioNode(sector);
        rootNode.addChildNode(sectorNode);
      }
      ManageablePortfolioNode groupNode = sectorNode.findNodeByName("Group " + industryGroup);
      if (groupNode == null) {
        s_logger.debug("Creating node for industry group {}", industryGroup);
        groupNode = new ManageablePortfolioNode("Group " + industryGroup);
        sectorNode.addChildNode(groupNode);
      }
      ManageablePortfolioNode industryNode = groupNode.findNodeByName("Industry " + industry);
      if (industryNode == null) {
        s_logger.debug("Creating node for industry {}", industry);
        industryNode = new ManageablePortfolioNode("Industry " + industry);
        groupNode.addChildNode(industryNode);
      }
      ManageablePortfolioNode subIndustryNode = industryNode.findNodeByName("Sub industry " + subIndustry);
      if (subIndustryNode == null) {
        s_logger.debug("Creating node for sub industry {}", subIndustry);
        subIndustryNode = new ManageablePortfolioNode("Sub industry " + subIndustry);
        industryNode.addChildNode(subIndustryNode);
      }

      // create the position and add it to the master
      final ManageablePosition position = createPositionAndTrade(security);
      final PositionDocument addedPosition = addPosition(position);

      // add the position reference (the unique identifier) to portfolio
      subIndustryNode.addPosition(addedPosition.getUniqueId());
    }

    // adds the complete tree structure to the master
    addPortfolio(portfolio);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    // load all equity securities
    final Collection<EquitySecurity> securities = readEquitySecurities();
    createPortfolio(securities);    
  }

  private void loadSecurities(Collection<ExternalIdBundle> identifiers) {
    SecurityMaster secMaster = getToolContext().getSecurityMaster();
    ReferenceDataProvider referenceDataProvider = ((BloombergToolContext) getToolContext()).getBloombergReferenceDataProvider();
    ExchangeDataProvider exchangeDataProvider = new DefaultExchangeDataProvider();
    BloombergBulkSecurityLoader bulkSecurityLoader = new BloombergBulkSecurityLoader(referenceDataProvider, exchangeDataProvider);
    BloombergSecurityLoader securityLoader = new BloombergSecurityLoader(secMaster, bulkSecurityLoader);
    securityLoader.loadSecurity(identifiers);
  }

  /**
   * Create a empty portfolio.
   * <p>
   * This creates the portfolio and the root of the tree structure that holds the positions.
   * Subsequent methods then populate the tree.
   *
   * @return the emoty portfolio, not null
   */
  protected ManageablePortfolio createEmptyPortfolio() {
    ManageablePortfolio portfolio = new ManageablePortfolio(PORTFOLIO_NAME);
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    rootNode.setName("Root");
    return portfolio;
  }

  /**
   * Create a position of a random number of shares.
   * <p>
   * This creates the position using a random number of units and create one or two trades making up the position.
   *
   * @param security  the security to add a position for, not null
   * @return the position, not null
   */
  protected ManageablePosition createPositionAndTrade(EquitySecurity security) {
    s_logger.debug("Creating position {}", security);
    int shares = (RandomUtils.nextInt(490) + 10) * 10;

    ExternalIdBundle bundle = security.getExternalIdBundle(); // we could add an identifier pointing back to the original source database if we're doing an ETL.

    ManageablePosition position = new ManageablePosition(BigDecimal.valueOf(shares), bundle);

    // create random trades that add up in shares to the position they're under (this is not enforced by the system)
    if (shares <= 2000) {
      ManageableTrade trade = new ManageableTrade(BigDecimal.valueOf(shares), bundle, LocalDate.of(2010, 12, 3), null, ExternalId.of("CPARTY", "BACS"));
      position.addTrade(trade);
    } else {
      ManageableTrade trade1 = new ManageableTrade(BigDecimal.valueOf(2000), bundle, LocalDate.of(2010, 12, 1), null, ExternalId.of("CPARTY", "BACS"));
      position.addTrade(trade1);
      ManageableTrade trade2 = new ManageableTrade(BigDecimal.valueOf(shares - 2000), bundle, LocalDate.of(2010, 12, 2), null, ExternalId.of("CPARTY", "BACS"));
      position.addTrade(trade2);
    }
    return position;
  }

  /**
   * Adds the position to the master.
   *
   * @param position  the position to add, not null
   * @return the added document, not null
   */
  protected PositionDocument addPosition(ManageablePosition position) {
    return getToolContext().getPositionMaster().add(new PositionDocument(position));
  }

  /**
   * Adds the portfolio to the master.
   *
   * @param portfolio  the portfolio to add, not null
   * @return the added document, not null
   */
  protected PortfolioDocument addPortfolio(ManageablePortfolio portfolio) {
    return getToolContext().getPortfolioMaster().add(new PortfolioDocument(portfolio));
  }

}
