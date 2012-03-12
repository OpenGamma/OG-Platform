/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.production.tool.portfolio;

import java.math.BigDecimal;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.production.tool.AbstractProductionTool;

/**
 * Loads a demo bond portfolio.
 */
public class DemoTwoBondPortfolioLoader extends AbstractProductionTool {

  /**
   * The name of the portfolio.
   */
  private static final String PORTFOLIO_NAME = "Test Two Bond Portfolio";
  /**
   * The security id.
   */
  private static final ExternalIdBundle SECURITY1 = ExternalIdBundle.of(SecurityUtils.bloombergBuidSecurityId("GV912828ES5"));
  /**
   * The security id.
   */
  private static final ExternalIdBundle SECURITY2 = ExternalIdBundle.of(SecurityUtils.bloombergBuidSecurityId("GV912810EB0"));

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new DemoTwoBondPortfolioLoader().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the portfolio.
   */
  @Override 
  protected void doRun() {
    // load all bond securities
    SecuritySearchRequest secSearch = new SecuritySearchRequest();
    secSearch.setFullDetail(true);
    secSearch.setSecurityType(BondSecurity.SECURITY_TYPE);
    secSearch.addExternalIds(SECURITY1);
    secSearch.addExternalIds(SECURITY2);
    SecuritySearchResult securities = getToolContext().getSecurityMaster().search(secSearch);
    if (securities.getDocuments().size() != 2) {
      throw new IllegalArgumentException();
    }
    BondSecurity sec1 = (BondSecurity) securities.getSecurities().get(0);
    BondSecurity sec2 = (BondSecurity) securities.getSecurities().get(1);
    
    // create shell portfolio
    ManageablePortfolio portfolio = new ManageablePortfolio(PORTFOLIO_NAME);
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    rootNode.setName("Root");
    
    // add position
    ManageablePosition position1 = new ManageablePosition(BigDecimal.valueOf(1), sec1.getExternalIdBundle());
    PositionDocument addedPosition1 = getToolContext().getPositionMaster().add(new PositionDocument(position1));
    ManageablePosition position2 = new ManageablePosition(BigDecimal.valueOf(1), sec2.getExternalIdBundle());
    PositionDocument addedPosition2 = getToolContext().getPositionMaster().add(new PositionDocument(position2));
    
    // add position reference to portfolio
    rootNode.addPosition(addedPosition1.getUniqueId());
    rootNode.addPosition(addedPosition2.getUniqueId());
    getToolContext().getPortfolioMaster().add(new PortfolioDocument(portfolio));
  }

}
