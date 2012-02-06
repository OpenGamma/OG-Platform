/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader;

import com.opengamma.financial.portfolio.loader.LoaderContext;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;

/**
 * A class that facilitates writing securities and portfolio positions and trades
 */
public class MasterPortfolioWriter implements PortfolioWriter {

  private PortfolioMaster _portfolioMaster;
  private PositionMaster _positionMaster;
  private SecurityMaster _securityMaster;
  
  private PortfolioDocument _portfolioDocument;
  private ManageablePortfolioNode _currentNode;
  
  public MasterPortfolioWriter(String portfolioName, LoaderContext loaderContext) {
    _portfolioMaster = loaderContext.getPortfolioMaster();
    _positionMaster = loaderContext.getPositionMaster();
    _securityMaster = loaderContext.getSecurityMaster();
    
    _portfolioDocument = createOrOpenPortfolio(portfolioName);
  }
  
  public MasterPortfolioWriter(String portfolioName, PortfolioMaster portfolioMaster, PositionMaster positionMaster, SecurityMaster securityMaster) {
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;
    
    _portfolioDocument = createOrOpenPortfolio(portfolioName);

  }
  
  @Override
  public ManageableSecurity writeSecurity(ManageableSecurity security) {
    
    // If security already exists don't write the new one, but return the original instead
    SecuritySearchRequest nameRequest = new SecuritySearchRequest();
    nameRequest.setName(security.getName());
    SecuritySearchResult searchResult = _securityMaster.search(nameRequest);
    ManageableSecurity origSecurity = searchResult.getFirstSecurity();
    if (origSecurity != null) { 
      security = origSecurity;
    } else {
      _securityMaster.add(new SecurityDocument(security));
    }

    return security;
  }

  @Override
  public ManageablePosition writePosition(ManageablePosition position) {
    
    // Add the position to the position master
    PositionDocument addedDoc = _positionMaster.add(new PositionDocument(position));

    // Add the position to the, new or existing, portfolio
    _currentNode.addPosition(addedDoc.getUniqueId());

    return addedDoc.getPosition();
  }
  
  @Override
  public ManageablePortfolio getPortfolio() {
    return _portfolioDocument.getPortfolio();
  }
  
  @Override
  public ManageablePortfolioNode getCurrentNode() {
    return _currentNode;
  }
  
  @Override
  public ManageablePortfolioNode setCurrentNode(ManageablePortfolioNode node) {
    _currentNode = node;
    return _currentNode;
  }
    
  @Override
  public void flush() {
    _portfolioMaster.update(_portfolioDocument);
  }
    
  private PortfolioDocument createOrOpenPortfolio(String portfolioName) {
        
    // Check to see whether the portfolio already exists
    PortfolioSearchRequest portSearchRequest = new PortfolioSearchRequest();
    portSearchRequest.setName(portfolioName);
    PortfolioSearchResult portSearchResult = _portfolioMaster.search(portSearchRequest);
    ManageablePortfolio portfolio = portSearchResult.getFirstPortfolio();
    PortfolioDocument portfolioDoc = portSearchResult.getFirstDocument();

    // If it doesn't, add it
    if (portfolio == null) {
      ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
      portfolio = new ManageablePortfolio(portfolioName, rootNode);
      portfolioDoc = new PortfolioDocument();
      portfolioDoc.setPortfolio(portfolio);
      _portfolioMaster.add(portfolioDoc);
    }

    // Set current node to the root node
    _currentNode = portfolio.getRootNode();
    
    return portfolioDoc;
  }

}
