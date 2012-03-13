/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.production.tool.portfolio;

import java.math.BigDecimal;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.production.tool.AbstractProductionTool;

/**
 * Loads a set of demo positions.
 */
public class DemoEquityOptionPortfolioLoader extends AbstractProductionTool {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DemoEquityOptionPortfolioLoader.class);

  /**
   * The number to add per equity.
   */
  private static final int THRESHOLD_PER_EQUITY = 10;
  /**
   * The name of the portfolio.
   */
  private static final String PORTFOLIO_NAME = "Test Equity Option Portfolio";
//  private static final String PORTFOLIO_NAME = "Test Multi-Currency Equity Option Portfolio";
  /**
   * The equities to add.
   */
  private static final String[] TARGET_EQUITIES = new String[] {
    "AAPL US Equity", "MSFT US Equity",
    "CSCO US Equity", "C US Equity", "BAC US Equity",
  //"T US Equity", "FNM US Equity",
  //"F US Equity", "S US Equity", "WFC US Equity", "INTC US Equity",
  //"PFE US Equity", "MU US Equity", "JPM US Equity", "DIS US Equity", "XOM US Equity",
  //"YHOO US Equity", "AMAT US Equity", "FRE US Equity", "AA US Equity", "LVS US Equity",
  // Can't include this one as it causes BBG-14 to be raised:
  //"UNG US Equity", 
    // multi-currency
    //"ARM LN Equity" // ARM LN 17-12-2010
  };

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new DemoEquityOptionPortfolioLoader().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the portfolio.
   */
  @Override 
  protected void doRun() {
    ManageablePortfolio portfolio = new ManageablePortfolio(PORTFOLIO_NAME);
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    rootNode.setName(PORTFOLIO_NAME);
    
    // create portfolio structure
    for (String targetEquity : TARGET_EQUITIES) {
      s_logger.warn("Adding {} options on {}", THRESHOLD_PER_EQUITY, targetEquity);
      MasterSecuritySource masterSecuritySource = new MasterSecuritySource(getToolContext().getSecurityMaster());
      
      ManageablePortfolioNode node = new ManageablePortfolioNode("Options on " + targetEquity);
      rootNode.addChildNode(node);
      
      // add position
      Set<ExternalId> optionChain = BloombergDataUtils.getOptionChain(getToolContext().getReferenceDataProvider(), targetEquity);
      int nAdded = 0;
      for (ExternalId option : optionChain) {
        final ExternalIdBundle bundle = ExternalIdBundle.of(option);
        Security optionSecurity = masterSecuritySource.getSecurity(bundle);
        if (optionSecurity == null) {
          throw new OpenGammaRuntimeException("Could not resolve security for " + bundle);
        }
        EquityOptionSecurity equityOptionSecurity = (EquityOptionSecurity) optionSecurity;
        // has to be more than 25 hours in the future due to FIN-70
        ZonedDateTime expiry = equityOptionSecurity.getExpiry().getExpiry();
        if (expiry.isBefore(ZonedDateTime.now().plusHours(25))) {
          s_logger.info("Option {} in future, so passing on it.", equityOptionSecurity);
          continue;
        }
        s_logger.warn("Adding option {}", equityOptionSecurity);
        ManageablePosition position = new ManageablePosition(BigDecimal.valueOf(10), equityOptionSecurity.getExternalIdBundle());
        PositionDocument addedPosition = getToolContext().getPositionMaster().add(new PositionDocument(position));
        node.addPosition(addedPosition.getUniqueId());
        nAdded++;
        if (nAdded >= THRESHOLD_PER_EQUITY) {
          break;
        }
      }
    }
    getToolContext().getPortfolioMaster().add(new PortfolioDocument(portfolio));
  }

}
