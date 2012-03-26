/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.loadsave.portfolio.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.loadsave.portfolio.writer.PortfolioWriter;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Portfolio reader.
 */
public class MasterPortfolioReader implements PortfolioReader {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioReader.class);

  private PortfolioMaster _portfolioMaster;
  private PositionMaster _positionMaster;
  private SecuritySource _securitySource;
  
  private PortfolioDocument _portfolioDocument;

  private ManageablePortfolioNode _currentNode;
  private Stack<Iterator<ManageablePortfolioNode>> _nodeIteratorStack;
  private Iterator<ManageablePortfolioNode> _nodeIterator;
  private Iterator<ObjectId> _positionIdIterator;
  
  
  public MasterPortfolioReader(String portfolioName, PortfolioMaster portfolioMaster, 
      PositionMaster positionMaster, SecuritySource securitySource) {
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _securitySource = securitySource;   
    _portfolioDocument = openPortfolio(portfolioName);
    
    _currentNode = _portfolioDocument.getPortfolio().getRootNode();

    List<ManageablePortfolioNode> rootNodeList = new ArrayList<ManageablePortfolioNode>(); 
    rootNodeList.add(_portfolioDocument.getPortfolio().getRootNode());
    
    _nodeIterator = rootNodeList.iterator();

    _nodeIteratorStack = new Stack<Iterator<ManageablePortfolioNode>>();

    _positionIdIterator = _nodeIterator.next().getPositionIds().iterator();
  }

//  @Override
//  public void writeTo(PortfolioWriter portfolioWriter) {
//    recursiveTraversePortfolioNodes(_portfolioDocument.getPortfolio().getRootNode(), portfolioWriter);
//  }

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
  @Override
  public ObjectsPair<ManageablePosition, ManageableSecurity[]> readNext() {
    
    ObjectId positionId = getNextPositionId();    
    if (positionId == null) {
      return null;
    } else {
      ManageablePosition position = _positionMaster.get(positionId, VersionCorrection.LATEST).getPosition();
      
      // Write the related security(ies) TODO handle writing multiple, for underlying securities
      ManageableSecurityLink sLink = position.getSecurityLink();
      Security security = sLink.resolveQuiet(_securitySource);
      if ((security != null) && (security instanceof ManageableSecurity)) {
        return new ObjectsPair<ManageablePosition, ManageableSecurity[]>(
            position, 
            new ManageableSecurity[] {(ManageableSecurity) security});
      } else {
        s_logger.warn("Could not resolve security relating to position " + position.getName());
        return new ObjectsPair<ManageablePosition, ManageableSecurity[]>(null, null);
      }
    }
  }

  public ObjectId getNextPositionId() {
    
    while (true) {
      // Return the next position in the current portfolio node's list, if any there
      if (_positionIdIterator.hasNext()) {
        return _positionIdIterator.next();
        
      // Current node's positions exhausted, find another node
      } else {
        
        // Go down to current node's child nodes to find more positions (depth-first)
        _nodeIteratorStack.push(_nodeIterator);
        _nodeIterator = _currentNode.getChildNodes().iterator();
  
        // If there are no more nodes here pop back up until a node is available
        while (!_nodeIterator.hasNext()) {
          if (!_nodeIteratorStack.isEmpty()) {
            _nodeIterator = _nodeIteratorStack.pop();
          } else {
            return null;
          }
        }
        
        // Go to the next node and start fetching positions there
        _currentNode = _nodeIterator.next();
        _positionIdIterator = _currentNode.getPositionIds().iterator();
      }
    }
  }
        
  @Override
  public String[] getCurrentPath() {
    Stack<ManageablePortfolioNode> stack = 
        _portfolioDocument.getPortfolio().getRootNode().findNodeStackByObjectId(_currentNode.getUniqueId());
    String[] result = new String[stack.size()];
    int i = stack.size();
    while (!stack.isEmpty()) {
      result[--i] = stack.pop().getName();
    }
    return result;
  }

  
  private PortfolioDocument openPortfolio(String portfolioName) {
    
    // Check to see whether the portfolio already exists
    PortfolioSearchRequest portSearchRequest = new PortfolioSearchRequest();
    portSearchRequest.setName(portfolioName);
    PortfolioSearchResult portSearchResult = _portfolioMaster.search(portSearchRequest);
    PortfolioDocument portfolioDoc = portSearchResult.getFirstDocument();
   
    return portfolioDoc;
  }

  @Override
  public void close() {
  }

}
