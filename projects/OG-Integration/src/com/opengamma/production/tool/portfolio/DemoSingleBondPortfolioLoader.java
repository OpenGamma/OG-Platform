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
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.production.tool.AbstractProductionTool;

/**
 * Loads a demo bond portfolio.
 */
public class DemoSingleBondPortfolioLoader extends AbstractProductionTool {

  /**
   * The name of the portfolio.
   */
  private static final String PORTFOLIO_NAME = "Test Single Bond Portfolio";
  /**
   * The security id.
   */
  private static final ExternalIdBundle SECURITY = ExternalIdBundle.of(SecurityUtils.bloombergBuidSecurityId("GV912828ES5"));

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new DemoSingleBondPortfolioLoader().initAndRun(args);
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
    secSearch.addExternalIds(SECURITY);
    SecuritySearchResult securities = getToolContext().getSecurityMaster().search(secSearch);
    ManageableSecurity manSec = securities.getSingleSecurity();
    BondSecurity sec = (BondSecurity) manSec;
    
    // create shell portfolio
    ManageablePortfolio portfolio = new ManageablePortfolio(PORTFOLIO_NAME);
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    rootNode.setName("Root");
    
    // add position
    ManageablePosition position = new ManageablePosition(BigDecimal.valueOf(1450), sec.getExternalIdBundle());
    PositionDocument addedPosition = getToolContext().getPositionMaster().add(new PositionDocument(position));
    
    // add position reference to portfolio
    rootNode.addPosition(addedPosition.getUniqueId());
    getToolContext().getPortfolioMaster().add(new PortfolioDocument(portfolio));
  }

}
