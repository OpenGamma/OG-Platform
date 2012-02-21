/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader.portfolio;

import java.util.Map;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.loader.LoaderContext;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;

public class MasterPortfolioReader implements PortfolioReader {

  private PortfolioMaster _portfolioMaster;
  private PositionMaster _positionMaster;
  private SecurityMaster _securityMaster;
  private SecuritySource _securitySource;
  
  private PortfolioDocument _portfolioDocument;
  private ManageablePortfolioNode _currentNode;

  public MasterPortfolioReader(String portfolioName, LoaderContext loaderContext) {
    _portfolioMaster = loaderContext.getPortfolioMaster();
    _positionMaster = loaderContext.getPositionMaster();
    _securityMaster = loaderContext.getSecurityMaster();
    _securitySource = loaderContext.getSecuritySource();
    
    _portfolioDocument = openPortfolio(portfolioName);
  }
  
  public MasterPortfolioReader(String portfolioName, PortfolioMaster portfolioMaster, 
      PositionMaster positionMaster, SecurityMaster securityMaster, SecuritySource securitySource) {
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;
    _securitySource = securitySource;
    
    _portfolioDocument = openPortfolio(portfolioName);
  }

  @Override
  public void writeTo(PortfolioWriter portfolioWriter) {
    recursiveTraversePortfolioNodes(_portfolioDocument.getPortfolio().getRootNode(), portfolioWriter);
  }

  private void recursiveTraversePortfolioNodes(ManageablePortfolioNode node, PortfolioWriter portfolioWriter) {
    
    // Extract and write rows for the current node's positions
    for (ObjectId positionId : node.getPositionIds()) {
      ManageablePosition position = _positionMaster.get(positionId, VersionCorrection.LATEST).getPosition();
      
      // get securities here?
      
      portfolioWriter.writePosition(position);      
    }
    
    // Recursively traverse the child nodes
    for (ManageablePortfolioNode child : node.getChildNodes()) {
      
      // Find or create corresponding sub-node in destination portfolio and change to it
      ManageablePortfolioNode writeNode = portfolioWriter.getCurrentNode();
      ManageablePortfolioNode newNode = null;
      for (ManageablePortfolioNode n : writeNode.getChildNodes()) {
        if (n.getName() == node.getName()) {
          newNode = n;
          break;
        }
      }
      if (newNode == null) {
        newNode = new ManageablePortfolioNode(node.getName());
        writeNode.addChildNode(newNode);
      }
      portfolioWriter.setCurrentNode(newNode);
      
      // Recursive call
      recursiveTraversePortfolioNodes(child, portfolioWriter);
      
      // Change back up to parent node in destination portfolio
      portfolioWriter.setCurrentNode(writeNode);
    }
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
