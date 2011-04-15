/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import java.math.BigDecimal;

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
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.PlatformConfigUtils.RunMode;

/**
 * Example code to load a simple equity portfolio.
 * <p>
 * This loads all equity securities previously stored in the master and
 * categorizes them by GICS code.
 */
public class DemoEquityPortfolioLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DemoEquityPortfolioLoader.class);

  /**
   * The name of the portfolio.
   */
  private static final String PORTFOLIO_NAME = "Test Equity Portfolio";

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
  public void loadTestPortfolio() {
    // load all equity securities
    final SecuritySearchResult securityShells = loadAllEquitySecurities();
    
    // create shell portfolio
    final ManageablePortfolio portfolio = createPortfolio();
    final ManageablePortfolioNode rootNode = portfolio.getRootNode();
    
    // add each security to the portfolio
    for (SecurityDocument shellDoc : securityShells.getDocuments()) {
      // load the full detail of the security
      final EquitySecurity security = loadFullSecurity(shellDoc);
      
      // build the tree structure using the GICS code
      final GICSCode gics = security.getGicsCode();
      if (gics == null) {
        continue;
      }
      final ManageablePortfolioNode subIndustryNode = buildPortfolioTree(rootNode, gics);
      
      // create the position and add it to the master
      final ManageablePosition position = createPosition(security);
      final PositionDocument addedPosition = addPosition(position);
      
      // add the position reference (the unique identifier) to portfolio
      subIndustryNode.addPosition(addedPosition.getUniqueId());
    }
    
    // adds the complete tree structure to the master
    addPortfolio(portfolio);
  }

  /**
   * Loads all securities from the master.
   * <p>
   * This loads all the securities into memory.
   * However, by setting "full detail" to false, only minimal information is loaded.
   * <p>
   * An alternate approach to scalability would be to batch the results using the
   * paging controls of the search request.
   * 
   * @return all securities in the security master, not null
   */
  protected SecuritySearchResult loadAllEquitySecurities() {
    SecuritySearchRequest secSearch = new SecuritySearchRequest();
    secSearch.setFullDetail(false);
    secSearch.setSecurityType(EquitySecurity.SECURITY_TYPE);
    SecuritySearchResult securities = _loaderContext.getSecurityMaster().search(secSearch);
    s_logger.info("Found {} securities", securities.getDocuments().size());
    return securities;
  }

  /**
   * Loads the full detail of the security.
   * <p>
   * The search used an optimization where the "full detail" of the security was not loaded.
   * It is thus necessary to load the full information about the security before processing.
   * The unique identifier is the key to loading the security.
   * 
   * @param shellDoc  the document to load, not null
   * @return the equity security, not null
   */
  protected EquitySecurity loadFullSecurity(SecurityDocument shellDoc) {
    s_logger.warn("Loading security {} {}", shellDoc.getUniqueId(), shellDoc.getSecurity().getName());
    SecurityDocument doc = _loaderContext.getSecurityMaster().get(shellDoc.getUniqueId());
    EquitySecurity sec = (EquitySecurity) doc.getSecurity();
    return sec;
  }

  /**
   * Create a shell portfolio.
   * <p>
   * This creates the portfolio and the root of the tree structure that holds the positions.
   * Subsequent methods then populate the tree.
   * 
   * @return the shell portfolio, not null
   */
  protected ManageablePortfolio createPortfolio() {
    ManageablePortfolio portfolio = new ManageablePortfolio(PORTFOLIO_NAME);
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    rootNode.setName("Root");
    return portfolio;
  }

  /**
   * Create the portfolio tree structure based.
   * <p>
   * This uses the GICS code to create a tree structure.
   * The position will be added to the lowest child node, which is returned.
   * 
   * @param rootNode  the root node of the tree, not null
   * @param gics  the GICS representation, not null
   * @return the lowest child node, not null
   */
  protected ManageablePortfolioNode buildPortfolioTree(ManageablePortfolioNode rootNode, GICSCode gics) {
    String sector = Integer.toString(gics.getSectorCode());
    ManageablePortfolioNode sectorNode = rootNode.findNodeByName(sector);
    if (sectorNode == null) {
      s_logger.warn("Creating node for sector {}", sector);
      sectorNode = new ManageablePortfolioNode(sector);
      rootNode.addChildNode(sectorNode);
    }
    
    String industryGroup = Integer.toString(gics.getIndustryGroupCode());
    ManageablePortfolioNode groupNode = sectorNode.findNodeByName("Group " + industryGroup);
    if (groupNode == null) {
      s_logger.warn("Creating node for industry group {}", industryGroup);
      groupNode = new ManageablePortfolioNode("Group " + industryGroup);
      sectorNode.addChildNode(groupNode);
    }
    
    String industry = Integer.toString(gics.getIndustryCode());
    ManageablePortfolioNode industryNode = groupNode.findNodeByName("Industry " + industry);
    if (industryNode == null) {
      s_logger.warn("Creating node for industry {}", industry);
      industryNode = new ManageablePortfolioNode("Industry " + industry);
      groupNode.addChildNode(industryNode);
    }
    
    String subIndustry = Integer.toString(gics.getSubIndustryCode());
    ManageablePortfolioNode subIndustryNode = industryNode.findNodeByName("Sub industry " + subIndustry);
    if (subIndustryNode == null) {
      s_logger.warn("Creating node for sub industry {}", subIndustry);
      subIndustryNode = new ManageablePortfolioNode("Sub industry " + subIndustry);
      industryNode.addChildNode(subIndustryNode);
    }
    return subIndustryNode;
  }

  /**
   * Create a position of a random number of shares.
   * <p>
   * This creates the position using a random number of units.
   * 
   * @param security  the security to add a position for, not null
   * @return the position, not null
   */
  protected ManageablePosition createPosition(EquitySecurity security) {
    s_logger.warn("Creating position {}", security);
    int shares = (RandomUtils.nextInt(490) + 10) * 10;
    String buid = security.getIdentifiers().getIdentifier(SecurityUtils.BLOOMBERG_BUID);
    String ticker = security.getIdentifiers().getIdentifier(SecurityUtils.BLOOMBERG_TICKER);
    IdentifierBundle bundle;
    if (buid != null && ticker != null) {
      bundle = IdentifierBundle.of(SecurityUtils.bloombergBuidSecurityId(buid), SecurityUtils.bloombergTickerSecurityId(ticker));
    } else {
      bundle = security.getIdentifiers();
    }
    ManageablePosition position = new ManageablePosition(BigDecimal.valueOf(shares), bundle);
    
    // create random trades
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
      configurator.doConfigure("src/com/opengamma/integration/server/logback.xml");
      
      PlatformConfigUtils.configureSystemProperties(RunMode.SHAREDDEV);
      System.out.println("Starting connections");
      AbstractApplicationContext appContext = new ClassPathXmlApplicationContext("demoPortfolioLoader.xml");
      appContext.start();
      
      try {
        DemoEquityPortfolioLoader loader = (DemoEquityPortfolioLoader) appContext.getBean("demoEquityPortfolioLoader");
        System.out.println("Loading data");
        loader.loadTestPortfolio();
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
