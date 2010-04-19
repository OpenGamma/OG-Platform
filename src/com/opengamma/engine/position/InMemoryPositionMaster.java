/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.opengamma.id.Identifier;

/**
 * 
 *
 * @author kirk
 */
public class InMemoryPositionMaster implements PositionMaster {
  private final Map<String, Portfolio> _portfoliosByName = new ConcurrentHashMap<String, Portfolio>();
  private final Map<Identifier, Position> _positionsByIdentityKey = new ConcurrentHashMap<Identifier, Position>();
  private final Map<Identifier, PortfolioNode> _portfolioNodesByIdentityKey = new ConcurrentHashMap<Identifier, PortfolioNode>();
  private final AtomicLong _nextIdentityKey = new AtomicLong(1l);

  @Override
  public Portfolio getRootPortfolio(String portfolioName) {
    return _portfoliosByName.get(portfolioName);
  }

  @Override
  public Collection<String> getRootPortfolioNames() {
    return new TreeSet<String>(_portfoliosByName.keySet());
  }
  
  public void addPortfolio(String portfolioName, Portfolio rootPortfolio) {
    _portfoliosByName.put(portfolioName, rootPortfolio);
    addPortfolioNode(portfolioName, rootPortfolio);
  }

  /**
   * @param portfolioName
   * @param rootPortfolio
   */
  private void addPortfolioNode(String portfolioName, PortfolioNode node) {
    if(node instanceof PortfolioNodeImpl) {
      PortfolioNodeImpl nodeImpl = (PortfolioNodeImpl)node;
      String identityKey = portfolioName + "-" + _nextIdentityKey.getAndIncrement();
      nodeImpl.setIdentityKey(identityKey);
    }
    _portfolioNodesByIdentityKey.put(node.getIdentityKey(), node);
    for(Position position : node.getPositions()) {
      if(position instanceof PositionBean) {
        PositionBean positionBean = (PositionBean) position;
        String identityKey = portfolioName + "-" + _nextIdentityKey.getAndIncrement();
        positionBean.setIdentityKey(identityKey);
      }
      _positionsByIdentityKey.put(position.getIdentityKey(), position);
    }
    for(PortfolioNode subNode : node.getSubNodes()) {
      addPortfolioNode(portfolioName, subNode);
    }
  }

  @Override
  public Position getPosition(Identifier identityKey) {
    return _positionsByIdentityKey.get(identityKey);
  }

  @Override
  public PortfolioNode getPortfolioNode(Identifier identityKey) {
    return _portfolioNodesByIdentityKey.get(identityKey);
  }

}
