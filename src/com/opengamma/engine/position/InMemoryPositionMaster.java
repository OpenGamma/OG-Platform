/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.opengamma.id.Identifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of {@code PositionMaster}.
 */
public class InMemoryPositionMaster implements PositionMaster {

  /**
   * The portfolios.
   */
  private final Map<Identifier, Portfolio> _portfolios = new ConcurrentHashMap<Identifier, Portfolio>();
  /**
   * A cache of nodes by identity key.
   */
  private final Map<Identifier, PortfolioNode> _portfolioNodesByIdentityKey = new ConcurrentHashMap<Identifier, PortfolioNode>();
  /**
   * A cache of positions by identity key.
   */
  private final Map<Identifier, Position> _positionsByIdentityKey = new ConcurrentHashMap<Identifier, Position>();
  /**
   * The next index for the identity key.
   */
  private final AtomicLong _nextIdentityKey = new AtomicLong(1l);

  /**
   * Creates an empty position master.
   */
  public InMemoryPositionMaster() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the list of all portfolio identifiers.
   * @return the portfolio identifiers, unmodifiable, never null
   */
  public Set<Identifier> getPortfolioIds() {
    return _portfolios.keySet();
  }

  /**
   * Gets a specific root portfolio by name.
   * @param identifier  the identifier, null returns null
   * @return the portfolio, null if not found
   */
  public Portfolio getPortfolio(Identifier identifier) {
    return _portfolios.get(identifier);
  }

  /**
   * Finds a specific node from any portfolio by identity key.
   * @param identityKey  the identity key, null returns null
   * @return the node, null if not found
   */
  public PortfolioNode getPortfolioNode(Identifier identityKey) {
    return _portfolioNodesByIdentityKey.get(identityKey);
  }

  /**
   * Finds a specific position from any portfolio by identity key.
   * @param identityKey  the identity key, null returns null
   * @return the position, null if not found
   */
  public Position getPosition(Identifier identityKey) {
    return _positionsByIdentityKey.get(identityKey);
  }

  //-------------------------------------------------------------------------`
  /**
   * Adds a portfolio to the master.
   * @param portfolio  the portfolio to add, not null
   */
  public void addPortfolio(Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    _portfolios.put(portfolio.getIdentityKey(), portfolio);
    addToCache(portfolio.getIdentityKey().getValue(), portfolio.getRootNode());
  }

  /**
   * Adds a node to the cache.
   * @param portfolioId  the id, not null
   * @param node  the node to add, not null
   */
  private void addToCache(String portfolioId, PortfolioNode node) {
    // node
    if (node instanceof PortfolioNodeImpl) {
      PortfolioNodeImpl nodeImpl = (PortfolioNodeImpl) node;
      String identityKey = portfolioId + "-" + _nextIdentityKey.getAndIncrement();
      nodeImpl.setIdentityKey(identityKey);
    }
    _portfolioNodesByIdentityKey.put(node.getIdentityKey(), node);
    
    // position
    for (Position position : node.getPositions()) {
      if (position instanceof PositionBean) {
        PositionBean positionBean = (PositionBean) position;
        String identityKey = portfolioId + "-" + _nextIdentityKey.getAndIncrement();
        positionBean.setIdentityKey(identityKey);
      }
      _positionsByIdentityKey.put(position.getIdentityKey(), position);
    }
    
    // recurse
    for (PortfolioNode child : node.getChildNodes()) {
      addToCache(portfolioId, child);
    }
  }

}
