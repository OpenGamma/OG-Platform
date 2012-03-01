/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityMaster;

/**
 * Portfolio reader.
 */
public class MasterPortfolioReader implements PortfolioReader {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioReader.class);

  private PortfolioMaster _portfolioMaster;
  private PositionMaster _positionMaster;
  private SecuritySource _securitySource;
  
  private PortfolioDocument _portfolioDocument;

  public MasterPortfolioReader(String portfolioName, ToolContext toolContext) {
    _portfolioMaster = toolContext.getPortfolioMaster();
    _positionMaster = toolContext.getPositionMaster();
    _securitySource = toolContext.getSecuritySource();
    _portfolioDocument = openPortfolio(portfolioName);
  }
  
  public MasterPortfolioReader(String portfolioName, PortfolioMaster portfolioMaster, 
      PositionMaster positionMaster, SecurityMaster securityMaster, SecuritySource securitySource) {
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
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
      
      // Write the related security(ies) TODO handle writing multiple, for underlying securities
      ManageableSecurityLink sLink = position.getSecurityLink();
      Security security = sLink.resolveQuiet(_securitySource);
      if ((security != null) && (security instanceof ManageableSecurity)) {
        portfolioWriter.writeSecurity((ManageableSecurity) security);
        
        // write the current position (this will 'flush' the current row)
        portfolioWriter.writePosition(position);
      } else {
        //throw new OpenGammaRuntimeException("Could not resolve security relating to position " + position.getName());
        s_logger.warn("Could not resolve security relating to position " + position.getName());
      }
      
    }
    
    // Recursively traverse the child nodes
    for (ManageablePortfolioNode child : node.getChildNodes()) {
      
      // Find or create corresponding sub-node in destination portfolio and change to it
      ManageablePortfolioNode writeNode = portfolioWriter.getCurrentNode();
      ManageablePortfolioNode newNode = null;
      for (ManageablePortfolioNode n : writeNode.getChildNodes()) {
        if (n.getName().equals(child.getName())) {
          newNode = n;
          break;
        }
      }
      if (newNode == null) {
        newNode = new ManageablePortfolioNode(child.getName());
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
    PortfolioDocument portfolioDoc = portSearchResult.getFirstDocument();
   
    return portfolioDoc;
  }

}
