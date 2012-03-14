/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.production.tool.portfolio;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.production.tool.AbstractProductionTool;

/**
 * Loads a demo equity portfolio.
 */
public class DemoEquityPortfolioLoader extends AbstractProductionTool {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DemoEquityPortfolioLoader.class);
  /** Logger. */
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
  private static final String PORTFOLIO_NAME = "Test Equity Portfolio";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new DemoEquityPortfolioLoader().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the portfolio.
   */
  @Override 
  protected void doRun() {
    // load all equity securities
    SecuritySearchRequest secSearch = new SecuritySearchRequest();
    secSearch.setFullDetail(false);
    secSearch.setSecurityType(EquitySecurity.SECURITY_TYPE);
    SecuritySearchResult securities = getToolContext().getSecurityMaster().search(secSearch);
    s_logger.info("Found {} securities", securities.getDocuments().size());
    
    // create shell portfolio
    ManageablePortfolio portfolio = new ManageablePortfolio(PORTFOLIO_NAME);
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    rootNode.setName("Root");
    for (SecurityDocument shellDoc : securities.getDocuments()) {
      s_logger.warn("Loading security {} {}", shellDoc.getUniqueId(), shellDoc.getSecurity().getName());
      SecurityDocument doc = getToolContext().getSecurityMaster().get(shellDoc.getUniqueId());
      EquitySecurity sec = (EquitySecurity) doc.getSecurity();
      
      GICSCode gics = sec.getGicsCode();
      if (gics == null) {
        continue;
      }
      String sector = SECTORS.get(gics.getSectorCode());
      String industryGroup = gics.getIndustryGroupCode();
      String industry = gics.getIndustryCode();
      String subIndustry = gics.getSubIndustryCode();
      
      // create portfolio structure
      ManageablePortfolioNode sectorNode = rootNode.findNodeByName(sector);
      if (sectorNode == null) {
        s_logger.warn("Creating node for sector {}", sector);
        sectorNode = new ManageablePortfolioNode(sector);
        rootNode.addChildNode(sectorNode);
      }
      ManageablePortfolioNode groupNode = sectorNode.findNodeByName("Group " + industryGroup);
      if (groupNode == null) {
        s_logger.warn("Creating node for industry group {}", industryGroup);
        groupNode = new ManageablePortfolioNode("Group " + industryGroup);
        sectorNode.addChildNode(groupNode);
      }
      ManageablePortfolioNode industryNode = groupNode.findNodeByName("Industry " + industry);
      if (industryNode == null) {
        s_logger.warn("Creating node for industry {}", industry);
        industryNode = new ManageablePortfolioNode("Industry " + industry);
        groupNode.addChildNode(industryNode);
      }
      ManageablePortfolioNode subIndustryNode = industryNode.findNodeByName("Sub industry " + subIndustry);
      if (subIndustryNode == null) {
        s_logger.warn("Creating node for sub industry {}", subIndustry);
        subIndustryNode = new ManageablePortfolioNode("Sub industry " + subIndustry);
        industryNode.addChildNode(subIndustryNode);
      }
      
      // add position
      s_logger.warn("Creating position {}", sec);
      int shares = (RandomUtils.nextInt(490) + 10) * 10;
      ExternalId buid = sec.getExternalIdBundle().getExternalId(SecurityUtils.BLOOMBERG_BUID);
      ExternalId ticker = sec.getExternalIdBundle().getExternalId(SecurityUtils.BLOOMBERG_TICKER);
      ExternalIdBundle bundle;
      if (buid != null && ticker != null) {
        bundle = ExternalIdBundle.of(buid, ticker);
      } else {
        bundle = sec.getExternalIdBundle();
      }
      ManageablePosition position = new ManageablePosition(BigDecimal.valueOf(shares), bundle);
      if (shares <= 2000) {
        ManageableTrade trade = new ManageableTrade(BigDecimal.valueOf(shares), bundle, LocalDate.of(2010, 12, 3), null, ExternalId.of("CPARTY", "BACS"));
        position.addTrade(trade);
      } else {
        ManageableTrade trade1 = new ManageableTrade(BigDecimal.valueOf(2000), bundle, LocalDate.of(2010, 12, 1), null, ExternalId.of("CPARTY", "BACS"));
        position.addTrade(trade1);
        ManageableTrade trade2 = new ManageableTrade(BigDecimal.valueOf(shares - 2000), bundle, LocalDate.of(2010, 12, 2), null, ExternalId.of("CPARTY", "BACS"));
        position.addTrade(trade2);
      }
      PositionDocument addedPosition = getToolContext().getPositionMaster().add(new PositionDocument(position));
      
      // add position reference to portfolio
      subIndustryNode.addPosition(addedPosition.getUniqueId());
    }
    getToolContext().getPortfolioMaster().add(new PortfolioDocument(portfolio));
  }

}
