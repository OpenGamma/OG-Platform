/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.loader;

import static com.opengamma.util.functional.Functional.map;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.time.calendar.LocalDate;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.loader.BloombergSecurityLoader;
import com.opengamma.bloombergexample.tool.AbstractExampleTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.util.functional.Function1;
import com.opengamma.util.generate.scripts.Scriptable;

/**
 * Example code to load a very simple equity portfolio.
 * <p>
 * This code is kept deliberately as simple as possible.
 * There are no checks for the securities or portfolios already existing, so if you run it
 * more than once you will get multiple copies portfolios and securities with the same names.
 * It is designed to run against the HSQLDB example database.
 */
@Scriptable
public class ExampleEquityPortfolioLoader extends AbstractExampleTool {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleEquityPortfolioLoader.class);

  private static final Map<String, String> SECTORS = new HashMap<String, String>();
  
  private static final String EXAMPLE_EQUITY_FILE = "example-equity.csv";

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
  public static final String PORTFOLIO_NAME = "Equity Portfolio";

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
  
  protected void createPortfolio(Collection<ExternalId> tickers) {

    // create shell portfolio
    final ManageablePortfolio portfolio = createEmptyPortfolio();
    final ManageablePortfolioNode rootNode = portfolio.getRootNode();

    Collection<UniqueId> loadSecurities = loadSecurities(tickers);
    SecurityMaster secMaster = getToolContext().getSecurityMaster();
    for (UniqueId uniqueId : loadSecurities) {
      SecurityDocument securityDocument = secMaster.get(uniqueId);
      EquitySecurity security = (EquitySecurity) securityDocument.getSecurity();
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
    final Collection<ExternalId> tickers = readEquityTickers();
    createPortfolio(tickers);    
  }

  private Collection<UniqueId> loadSecurities(Collection<ExternalId> identifiers) {
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    SecurityProvider securityProvider = getToolContext().getSecurityProvider();
    BloombergSecurityLoader securityLoader = new BloombergSecurityLoader(securityProvider, securityMaster);
    
    final Map<ExternalIdBundle, UniqueId> loadedSecurities = securityLoader.loadSecurity(map(identifiers, new Function1<ExternalId, ExternalIdBundle>() {
      @Override
      public ExternalIdBundle execute(ExternalId ticker) {
        return ExternalIdBundle.of(ticker);
      }
    }));
    return loadedSecurities.values();
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
  
  protected Collection<ExternalId> readEquityTickers() {
    Collection<ExternalId> result = new ArrayList<ExternalId>();
    InputStream inputStream = ExampleEquityPortfolioLoader.class.getResourceAsStream("example-equity.csv");
    try {
      if (inputStream != null) {
        List<String> equityTickers = IOUtils.readLines(inputStream);
        for (String idStr : equityTickers) {
          idStr = StringUtils.trimToNull(idStr);
          if (idStr != null && !idStr.startsWith("#")) {
            result.add(ExternalSchemes.bloombergTickerSecurityId(idStr));
          }
        }
      } else {
        throw new OpenGammaRuntimeException("File '" + EXAMPLE_EQUITY_FILE + "' could not be found");
      }
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("An error occurred while reading file '" + EXAMPLE_EQUITY_FILE + "'");
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
   
    StringBuilder sb = new StringBuilder();
    sb.append("Parsed ").append(result.size()).append(" equities:\n");
    for (ExternalId equityId : result) {
      sb.append("\t").append(equityId.getValue()).append("\n");
    }
    s_logger.info(sb.toString());
    return result;
  }

}
