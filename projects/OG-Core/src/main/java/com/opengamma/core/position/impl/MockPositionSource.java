/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of a source of positions.
 * <p>
 * This class is intended for testing scenarios.
 * It is not thread-safe and must not be used in production.
 */
public class MockPositionSource implements PositionSource {
  // this is currently public for indirect use by another project via ViewTestUtils

  /**
   * The portfolios.
   */
  private final Map<ObjectId, Portfolio> _portfolios = new ConcurrentHashMap<ObjectId, Portfolio>();
  /**
   * A cache of nodes by identifier.
   */
  private final Map<ObjectId, PortfolioNode> _nodes = new ConcurrentHashMap<ObjectId, PortfolioNode>();
  /**
   * A cache of positions by identifier.
   */
  private final Map<ObjectId, Position> _positions = new ConcurrentHashMap<ObjectId, Position>();
  /**
   * A cache of trades by identifier.
   */
  private final Map<ObjectId, Trade> _trades = new ConcurrentHashMap<ObjectId, Trade>();
  /**
   * The suppler of unique identifiers.
   */
  private final UniqueIdSupplier _uniqueIdSupplier;

  /**
   * Creates an instance using the default scheme for each {@link UniqueId} created.
   */
  public MockPositionSource() {
    _uniqueIdSupplier = new UniqueIdSupplier("Mock");
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getPortfolio(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    Portfolio portfolio = _portfolios.get(uniqueId.getObjectId());
    if (portfolio == null) {
      throw new DataNotFoundException("Portfolio not found: " + uniqueId);
    }
    return portfolio;
  }

  @Override
  public Portfolio getPortfolio(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    Portfolio portfolio = _portfolios.get(objectId);
    if (portfolio == null) {
      throw new DataNotFoundException("Portfolio not found: " + objectId);
    }
    return portfolio;
  }

  @Override
  public PortfolioNode getPortfolioNode(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    PortfolioNode node = _nodes.get(uniqueId.getObjectId());
    if (node == null) {
      throw new DataNotFoundException("PortfolioNode not found: " + uniqueId);
    }
    return node;
  }

  @Override
  public Position getPosition(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    Position position = _positions.get(uniqueId.getObjectId());
    if (position == null) {
      throw new DataNotFoundException("Position not found: " + uniqueId);
    }
    return position;
  }

  @Override
  public Position getPosition(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    Position position = _positions.get(objectId);
    if (position == null) {
      throw new DataNotFoundException("Position not found: " + objectId);
    }
    return position;
  }

  @Override
  public Trade getTrade(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    Trade trade = _trades.get(uniqueId.getObjectId());
    if (trade == null) {
      throw new DataNotFoundException("Trade not found: " + uniqueId);
    }
    return trade;
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the list of all portfolio identifiers.
   * 
   * @return the portfolio identifiers, unmodifiable, not null
   */
  public Set<ObjectId> getPortfolioIds() {
    return _portfolios.keySet();
  }

  /**
   * Adds a portfolio to the master.
   * 
   * @param portfolio  the portfolio to add, not null
   */
  public void addPortfolio(Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    
    _portfolios.put(portfolio.getUniqueId().getObjectId(), portfolio);
    addToCache(portfolio.getUniqueId().getValue(), null, portfolio.getRootNode());
  }

  /**
   * Adds a node to the cache.
   * 
   * @param portfolioId  the id, not null
   * @param node  the node to add, not null
   */
  private void addToCache(String portfolioId, UniqueId parentNode, PortfolioNode node) {
    // node
    if (node instanceof SimplePortfolioNode) {
      SimplePortfolioNode nodeImpl = (SimplePortfolioNode) node;
      nodeImpl.setUniqueId(_uniqueIdSupplier.getWithValuePrefix(portfolioId + "-"));
      nodeImpl.setParentNodeId(parentNode);
    }
    _nodes.put(node.getUniqueId().getObjectId(), node);
    
    // position
    for (Position position : node.getPositions()) {
      if (position instanceof SimplePosition) {
        SimplePosition positionImpl = (SimplePosition) position;
        positionImpl.setUniqueId(_uniqueIdSupplier.getWithValuePrefix(portfolioId + "-"));
        
        //add trades
        for (Trade trade : positionImpl.getTrades()) {
          IdUtils.setInto(trade, _uniqueIdSupplier.getWithValuePrefix(portfolioId + "-"));
          _trades.put(trade.getUniqueId().getObjectId(), trade);
        }
      }
      _positions.put(position.getUniqueId().getObjectId(), position);
    }
    
    // recurse
    for (PortfolioNode child : node.getChildNodes()) {
      addToCache(portfolioId, node.getUniqueId(), child);
    }
  }

  /**
   * Removes a portfolio from the master.
   * 
   * @param portfolio  the portfolio to remove, not null
   */
  public void removePortfolio(Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    _portfolios.remove(portfolio.getUniqueId().getObjectId());
    removeFromCache(portfolio.getRootNode());
  }

  /**
   * Removes a node from the cache
   * 
   * @param node  the node to remove, not null
   */
  private void removeFromCache(PortfolioNode node) {
    _nodes.remove(node.getUniqueId().getObjectId());
    for (Position position : node.getPositions()) {
      for (Trade trade : position.getTrades()) {
        _trades.remove(trade.getUniqueId().getObjectId());
      }
      _positions.remove(position.getUniqueId().getObjectId());
    }
    for (PortfolioNode child : node.getChildNodes()) {
      removeFromCache(child);
    }
  }

}
