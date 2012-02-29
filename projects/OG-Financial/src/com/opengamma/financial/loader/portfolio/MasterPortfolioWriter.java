/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.portfolio;

import javax.time.Instant;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.VersionCorrection;
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
import com.opengamma.master.security.SecuritySearchSortOrder;

/**
 * A class that facilitates writing securities and portfolio positions and trades
 */
public class MasterPortfolioWriter implements PortfolioWriter {

  private PortfolioMaster _portfolioMaster;
  private PositionMaster _positionMaster;
  private SecurityMaster _securityMaster;
  
  private PortfolioDocument _portfolioDocument;
  private ManageablePortfolioNode _currentNode;
  
  public MasterPortfolioWriter(String portfolioName, ToolContext toolContext) {
    _portfolioMaster = toolContext.getPortfolioMaster();
    _positionMaster = toolContext.getPositionMaster();
    _securityMaster = toolContext.getSecurityMaster();
    
    _portfolioDocument = createOrOpenPortfolio(portfolioName);
  }
  
  public MasterPortfolioWriter(String portfolioName, PortfolioMaster portfolioMaster, PositionMaster positionMaster, SecurityMaster securityMaster) {
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;
    
    _portfolioDocument = createOrOpenPortfolio(portfolioName);

  }

  // Alternative implementation for adding a security, with alternative comparison method and updating/adding
  // Not tested, might not work as external IDs probably won't be populated in the new security
  @Override
  public ManageableSecurity writeSecurity(ManageableSecurity security) {
    SecuritySearchRequest secReq = new SecuritySearchRequest();
    ExternalIdSearch idSearch = new ExternalIdSearch(security.getExternalIdBundle());  // match any one of the IDs
    secReq.setVersionCorrection(VersionCorrection.ofVersionAsOf(ZonedDateTime.now())); // valid now
    secReq.setExternalIdSearch(idSearch);
    secReq.setFullDetail(true);
    secReq.setSortOrder(SecuritySearchSortOrder.VERSION_FROM_INSTANT_DESC);
    SecuritySearchResult searchResult = _securityMaster.search(secReq);
    if (searchResult.getDocuments().size() > 0) {
      SecurityDocument firstDocument = searchResult.getFirstDocument();
      ManageableSecurity foundSecurity = firstDocument.getSecurity();
      if (weakEquals(foundSecurity, security)) {
        // It's already there, don't update or add it
        return foundSecurity;
      } else {
        // Found it but contents differ, so update it
        SecurityDocument updateDoc = new SecurityDocument(security);
        updateDoc.setUniqueId(foundSecurity.getUniqueId());
        updateDoc.setVersionFromInstant(firstDocument.getVersionFromInstant());
        updateDoc.setVersionToInstant(firstDocument.getVersionToInstant());
        updateDoc.setCorrectionFromInstant(Instant.now());
        SecurityDocument result = _securityMaster.update(updateDoc);
        return result.getSecurity();
      }
    } else {
      // Not found, so add it
      SecurityDocument addDoc = new SecurityDocument(security);
      SecurityDocument result = _securityMaster.add(addDoc);
      return result.getSecurity();
    }
  }
  
  // This weak equals does not actually compare the security's fields, just the type, external ids and attributes :(
  private boolean weakEquals(ManageableSecurity sec1, ManageableSecurity sec2) {
    return sec1.getName().equals(sec2.getName()) &&
           sec1.getSecurityType().equals(sec2.getSecurityType()) &&
           sec1.getExternalIdBundle().equals(sec2.getExternalIdBundle()) &&
           sec1.getAttributes().equals(sec2.getAttributes());
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
  
  @Override
  public void close() {
    flush();
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
      portfolioDoc = _portfolioMaster.add(portfolioDoc);
    }

    // Set current node to the root node
    _currentNode = portfolioDoc.getPortfolio().getRootNode();
    
    return portfolioDoc;
  }

}
