/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.production.tool.portfolio;

import java.math.BigDecimal;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.production.tool.AbstractProductionTool;

/**
 * Loads a demo bond future portfolio.
 */
public class DemoBondFuturePortfolioLoader extends AbstractProductionTool {

  /**
   * The name of the portfolio.
   */
  private static final String PORTFOLIO_NAME = "Test Bond Future Portfolio";
  /**
   * The security id.
   */
  private static final ExternalIdBundle SECURITY1 = ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USZ0 Comdty"));
  /**
   * The security id.
   */
  private static final ExternalIdBundle SECURITY2 = ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USH1 Comdty"));
  /**
   * The security id.
   */
  private static final ExternalIdBundle SECURITY3 = ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USM1 Comdty"));

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new DemoBondFuturePortfolioLoader().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the portfolio.
   */
  @Override 
  protected void doRun() {
    // create shell portfolio
    ManageablePortfolio portfolio = new ManageablePortfolio(PORTFOLIO_NAME);
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    rootNode.setName("Root");
    ManageablePortfolioNode childNode = new ManageablePortfolioNode("LONG BOND");
    rootNode.addChildNode(childNode);
    
    // add position
    ManageablePosition position1 = new ManageablePosition(BigDecimal.valueOf(1), SECURITY1);
    PositionDocument addedPosition1 = getToolContext().getPositionMaster().add(new PositionDocument(position1));
    ManageablePosition position2 = new ManageablePosition(BigDecimal.valueOf(1), SECURITY2);
    PositionDocument addedPosition2 = getToolContext().getPositionMaster().add(new PositionDocument(position2));
    ManageablePosition position3 = new ManageablePosition(BigDecimal.valueOf(1), SECURITY3);
    PositionDocument addedPosition3 = getToolContext().getPositionMaster().add(new PositionDocument(position3));
    
    // add position reference to portfolio
    childNode.addPosition(addedPosition1.getUniqueId());
    childNode.addPosition(addedPosition2.getUniqueId());
    childNode.addPosition(addedPosition3.getUniqueId());
    getToolContext().getPortfolioMaster().add(new PortfolioDocument(portfolio));
  }

}
