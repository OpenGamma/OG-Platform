/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Portfolio reader.
 */
public class MasterPositionReader implements PositionReader {

  private static final Logger s_logger = LoggerFactory.getLogger(PositionReader.class);

  private PortfolioMaster _portfolioMaster;
  private PositionMaster _positionMaster;
  private SecuritySource _securitySource;
  
  private PortfolioDocument _portfolioDocument;

  private ManageablePortfolioNode _currentNode;
  private Stack<Iterator<ManageablePortfolioNode>> _nodeIteratorStack;
  private Iterator<ManageablePortfolioNode> _nodeIterator;
  private Iterator<ObjectId> _positionIdIterator;
  
  
  public MasterPositionReader(String portfolioName, PortfolioMaster portfolioMaster,
                              PositionMaster positionMaster, SecuritySource securitySource) {
    
    ArgumentChecker.notEmpty(portfolioName, "portfolioName");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(securitySource, "securitySource");

    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _securitySource = securitySource;   
    _portfolioDocument = openPortfolio(portfolioName);
    
    if (_portfolioDocument == null) {
      throw new OpenGammaRuntimeException("Portfolio " + portfolioName + " could not be opened");
    }
    _currentNode = _portfolioDocument.getPortfolio().getRootNode();

    List<ManageablePortfolioNode> rootNodeList = new ArrayList<ManageablePortfolioNode>(); 
    rootNodeList.add(_portfolioDocument.getPortfolio().getRootNode());
    
    _nodeIterator = rootNodeList.iterator();
    _nodeIteratorStack = new Stack<Iterator<ManageablePortfolioNode>>();
    _positionIdIterator = _nodeIterator.next().getPositionIds().iterator();
  }
  
  @Override
  public ObjectsPair<ManageablePosition, ManageableSecurity[]> readNext() {
    
    ObjectId positionId = getNextPositionId();    
    if (positionId == null) {
      return null;
    } else {
      ManageablePosition position;
      try {
        position = _positionMaster.get(positionId, VersionCorrection.LATEST).getPosition();
      } catch (Throwable t) {
        return ObjectsPair.of(null, null);
      }

      // Write the related security(ies)
      ManageableSecurityLink sLink = position.getSecurityLink();
      Security security = sLink.resolveQuiet(_securitySource);
      if ((security != null) && (security instanceof ManageableSecurity)) {
        
        // Find underlying security
        // TODO support multiple underlyings; unfortunately the system does not provide a standard way
        // to retrieve underlyings
        if (((ManageableSecurity) security).propertyNames().contains("underlyingId")) {
          ExternalId id = (ExternalId) ((ManageableSecurity) security).property("underlyingId").get();
        
          Security underlying;
          try {
            underlying = _securitySource.getSingle(id.toBundle());
            if (underlying != null) {
              return ObjectsPair.of(position, 
                  new ManageableSecurity[] {(ManageableSecurity) security, (ManageableSecurity) underlying});
            } else {
              s_logger.warn("Could not resolve underlying " + id + " for security " + security.getName());
            }
          } catch (Throwable e) {
            // Underlying not found
            s_logger.warn("Error trying to resolve underlying " + id + " for security " + security.getName());
          }
        }
        return ObjectsPair.of(position, 
              new ManageableSecurity[] {(ManageableSecurity) security});

      } else {
        s_logger.warn("Could not resolve security relating to position " + position.getName());
        return ObjectsPair.of(null, null);
      }
    }
  }

  @Override
  public String[] getCurrentPath() {
    Stack<ManageablePortfolioNode> stack = 
        _portfolioDocument.getPortfolio().getRootNode().findNodeStackByObjectId(_currentNode.getUniqueId());
    stack.remove(0);
    String[] result = new String[stack.size()];
    int i = stack.size();
    while (!stack.isEmpty()) {
      result[--i] = stack.pop().getName();
    }
    return result;
  }

  @Override
  public void close() {
    // Nothing to close
  }

  @Override
  public String getPortfolioName() {
    return null;
  }

  /**
   * Walks the tree, depth-first, and returns the next position's id. Uses _positionIdIterator, 
   * _nodeIterator and _nodeIteratorStack to maintain location state across calls.
   * @return
   */
  private ObjectId getNextPositionId() {
    
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
        
  private PortfolioDocument openPortfolio(String portfolioName) {
    
    PortfolioSearchRequest portSearchRequest = new PortfolioSearchRequest();
    portSearchRequest.setName(portfolioName);
    PortfolioSearchResult portSearchResult = _portfolioMaster.search(portSearchRequest);
    PortfolioDocument portfolioDoc = portSearchResult.getFirstDocument();
    
    return portfolioDoc;
  }

}
