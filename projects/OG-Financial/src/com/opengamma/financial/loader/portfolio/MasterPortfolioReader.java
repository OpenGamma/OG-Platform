/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.portfolio;

import java.util.Map;

import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;

/**
 * Portfolio reader.
 */
public class MasterPortfolioReader implements PortfolioReader {

  private PortfolioMaster _portfolioMaster;
  private PositionMaster _positionMaster;
  private SecurityMaster _securityMaster;
  
  private PortfolioDocument _portfolioDocument;
  private ManageablePortfolioNode _currentNode;

  public MasterPortfolioReader(String portfolioName, ToolContext toolContext) {
    _portfolioMaster = toolContext.getPortfolioMaster();
    _positionMaster = toolContext.getPositionMaster();
    _securityMaster = toolContext.getSecurityMaster();
    
    _portfolioDocument = openPortfolio(portfolioName);
  }

  public MasterPortfolioReader(String portfolioName, PortfolioMaster portfolioMaster, PositionMaster positionMaster, SecurityMaster securityMaster) {
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;
    
    _portfolioDocument = openPortfolio(portfolioName);
  }

  @Override
  public void writeTo(PortfolioWriter portfolioWriter) {
    Map<String, String> row;

    // TODO to implement recursive portfolio traversal, outputting positions, trades and securities
    
//    // Get the next position/trade from the portfolio
//    while ((row = getSheet().loadNextRow()) != null) {
//      
//      // Attempt to write securities and obtain the correct security (either newly written or original)
//      // Write array in reverse order as underlying is at position 0
//      for (int i = security.length - 1; i >= 0; i--) {
//        security[i] = portfolioWriter.writeSecurity(security[i]);        
//      }
//
//      // Build the position and trade(s) using security[0] (underlying)
//      ManageablePosition position = _rowParser.constructPosition(row, security[0]);
//      
//      ManageableTrade trade = _rowParser.constructTrade(row, security[0], position);
//      if (trade != null) {
//        position.addTrade(trade);
//      }
//      
//      // Write positions/trade(s) to masters (trades are implicitly written with the position)
//      portfolioWriter.writePosition(position);
//    }
  }

  private PortfolioDocument openPortfolio(String portfolioName) {
    
    // Check to see whether the portfolio already exists
    PortfolioSearchRequest portSearchRequest = new PortfolioSearchRequest();
    portSearchRequest.setName(portfolioName);
    PortfolioSearchResult portSearchResult = _portfolioMaster.search(portSearchRequest);
    ManageablePortfolio portfolio = portSearchResult.getFirstPortfolio();
    PortfolioDocument portfolioDoc = portSearchResult.getFirstDocument();

    if (portfolio == null || portfolioDoc == null) {
      _currentNode = null;
      return null;
    }

    // Set current node to the root node
    _currentNode = portfolio.getRootNode();
    
    return portfolioDoc;
  }

}
