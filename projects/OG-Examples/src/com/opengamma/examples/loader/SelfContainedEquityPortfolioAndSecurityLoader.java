/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.PlatformConfigUtils.RunMode;
import com.opengamma.util.money.Currency;

/**
 * Example code to load a simple equity portfolio.
 * <p>
 * This loads all equity securities previously stored in the master and
 * categorizes them by GICS code.
 */
public class SelfContainedEquityPortfolioAndSecurityLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SelfContainedEquityPortfolioAndSecurityLoader.class);

  /**
   * The name of the portfolio.
   */
  private static final String PORTFOLIO_NAME = "Self Contained Equity Portfolio";

  /**
   * The context.
   */
  private LoaderContext _loaderContext;

  //-------------------------------------------------------------------------
  /**
   * Sets the loader context.
   * <p>
   * This initializes this bean, typically from Spring.
   * 
   * @param loaderContext  the context, not null
   */
  public void setLoaderContext(final LoaderContext loaderContext) {
    _loaderContext = loaderContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the test portfolio into the position master.
   */
  public void createExamplePortfolio() {
    // load all equity securities
    final Collection<EquitySecurity> securities = createAndPersistEquitySecurities();
    
    // create shell portfolio
    final ManageablePortfolio portfolio = createEmptyPortfolio();
    final ManageablePortfolioNode rootNode = portfolio.getRootNode();
    
    // add each security to the portfolio
    for (EquitySecurity security : securities) {
      // create the position and add it to the master
      final ManageablePosition position = createPositionAndTrade(security);
      final PositionDocument addedPosition = addPosition(position);
      
      // add the position reference (the unique identifier) to portfolio
      rootNode.addPosition(addedPosition.getUniqueId());
    }
    
    // adds the complete tree structure to the master
    addPortfolio(portfolio);
  }

  protected EquitySecurity createEquitySecurity(String companyName, Currency currency, String exchange, String exchangeCode, int gicsCode, Identifier... identifiers) {
    EquitySecurity equitySecurity = new EquitySecurity(exchange, exchangeCode, companyName, currency);
    equitySecurity.setGicsCode(GICSCode.getInstance(gicsCode));
    equitySecurity.setIdentifiers(IdentifierBundle.of(identifiers));
    return equitySecurity;
  }
  /**
   * Creates securities and adds them to the master.
   * 
   * @return a collection of all securities that have been persisted, not null
   */
  protected Collection<EquitySecurity> createAndPersistEquitySecurities() {
    SecurityMaster secMaster = _loaderContext.getSecurityMaster();
    Collection<EquitySecurity> securities = new ArrayList<EquitySecurity>();
    securities.add(createEquitySecurity("Apple Inc", Currency.USD, "Nasdaq NGS", "XNGS", 45202010, 
                                        Identifier.of(SecurityUtils.ISIN, "US0378331005"), 
                                        Identifier.of(SecurityUtils.CUSIP, "037833100"), 
                                        Identifier.of(SecurityUtils.OG_SYNTHETIC_TICKER, "AAPL")));
    securities.add(createEquitySecurity("Microsoft Corp", Currency.USD, "Nasdaq NGS", "XNGS", 45103020, 
                                        Identifier.of(SecurityUtils.ISIN, "US594918104"), 
                                        Identifier.of(SecurityUtils.CUSIP, "594918104"), 
                                        Identifier.of(SecurityUtils.OG_SYNTHETIC_TICKER, "MSFT")));
    securities.add(createEquitySecurity("Google Inc", Currency.USD, "Nasdaq NGS", "XNGS", 45101010, 
                                        Identifier.of(SecurityUtils.ISIN, "US38259P5089"), 
                                        Identifier.of(SecurityUtils.CUSIP, "38259P508"), 
                                        Identifier.of(SecurityUtils.OG_SYNTHETIC_TICKER, "GOOG")));
    securities.add(createEquitySecurity("ARM Holdings", Currency.GBP, "London Stock Exchange", "XLON", 45301020, 
                                        Identifier.of(SecurityUtils.ISIN, "GB0000595859"), 
                                        Identifier.of(SecurityUtils.OG_SYNTHETIC_TICKER, "ARM")));
    for (EquitySecurity security : securities) {
      SecurityDocument doc = new SecurityDocument(security);
      secMaster.add(doc);
    }
    return securities;
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
    s_logger.warn("Creating position {}", security);
    int shares = (RandomUtils.nextInt(490) + 10) * 10;
    
    IdentifierBundle bundle = security.getIdentifiers(); // we could add an identifier pointing back to the original source database if we're doing an ETL.

    ManageablePosition position = new ManageablePosition(BigDecimal.valueOf(shares), bundle);
    
    // create random trades that add up in shares to the position they're under (this is not enforced by the system)
    if (shares <= 2000) {
      ManageableTrade trade = new ManageableTrade(BigDecimal.valueOf(shares), bundle, LocalDate.of(2010, 12, 3), null, Identifier.of("CPARTY", "BACS"));
      position.addTrade(trade);
    } else {
      ManageableTrade trade1 = new ManageableTrade(BigDecimal.valueOf(2000), bundle, LocalDate.of(2010, 12, 1), null, Identifier.of("CPARTY", "BACS"));
      position.addTrade(trade1);
      ManageableTrade trade2 = new ManageableTrade(BigDecimal.valueOf(shares - 2000), bundle, LocalDate.of(2010, 12, 2), null, Identifier.of("CPARTY", "BACS"));
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
    return _loaderContext.getPositionMaster().add(new PositionDocument(position));
  }

  /**
   * Adds the portfolio to the master.
   * 
   * @param portfolio  the portfolio to add, not null
   * @return the added document, not null
   */
  protected PortfolioDocument addPortfolio(ManageablePortfolio portfolio) {
    return _loaderContext.getPortfolioMaster().add(new PortfolioDocument(portfolio));
  }

  //-------------------------------------------------------------------------
  /**
   * Sets up and loads the database.
   * <p>
   * This loader requires a Spring configuration file that defines the security,
   * position and portfolio masters, together with an instance of this bean
   * under the name "demoEquityPortfolioLoader".
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    try {
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.reset(); 
      configurator.doConfigure("src/com/opengamma/examples/server/logback.xml");
      
      PlatformConfigUtils.configureSystemProperties(RunMode.SHAREDDEV);
      System.out.println("Starting connections");
      AbstractApplicationContext appContext = new ClassPathXmlApplicationContext("demoPortfolioLoader.xml");
      appContext.start();
      
      try {
        SelfContainedEquityPortfolioAndSecurityLoader loader = (SelfContainedEquityPortfolioAndSecurityLoader) appContext.getBean("selfContainedEquityPortfolioAndSecurityLoader");
        System.out.println("Loading data");
        loader.createExamplePortfolio();
      } finally {
        appContext.close();
      }
      System.out.println("Finished");
      
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    System.exit(0);
  }

}
