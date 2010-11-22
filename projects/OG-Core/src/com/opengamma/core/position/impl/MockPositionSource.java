/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
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
  private final Map<UniqueIdentifier, Portfolio> _portfolios = new ConcurrentHashMap<UniqueIdentifier, Portfolio>();
  /**
   * A cache of nodes by identifier.
   */
  private final Map<UniqueIdentifier, PortfolioNode> _nodes = new ConcurrentHashMap<UniqueIdentifier, PortfolioNode>();
  /**
   * A cache of positions by identifier.
   */
  private final Map<UniqueIdentifier, Position> _positions = new ConcurrentHashMap<UniqueIdentifier, Position>();
  /**
   * The next index for the identifier.
   */
  private final UniqueIdentifierSupplier _uidSupplier;

  /**
   * Creates an instance using the default scheme for each {@link UniqueIdentifier} created.
   */
  public MockPositionSource() {
    _uidSupplier = new UniqueIdentifierSupplier("Mock");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the list of all portfolio identifiers.
   * @return the portfolio identifiers, unmodifiable, not null
   */
  public Set<UniqueIdentifier> getPortfolioIds() {
    return _portfolios.keySet();
  }

  /**
   * Gets a specific root portfolio by name.
   * @param identifier  the identifier, null returns null
   * @return the portfolio, null if not found
   */
  public Portfolio getPortfolio(UniqueIdentifier identifier) {
    return identifier == null ? null : _portfolios.get(identifier);
  }

  /**
   * Finds a specific node from any portfolio by identifier.
   * @param identifier  the identifier, null returns null
   * @return the node, null if not found
   */
  public PortfolioNode getPortfolioNode(UniqueIdentifier identifier) {
    return identifier == null ? null : _nodes.get(identifier);
  }

  /**
   * Finds a specific position from any portfolio by identifier.
   * @param identifier  the identifier, null returns null
   * @return the position, null if not found
   */
  public Position getPosition(UniqueIdentifier identifier) {
    return identifier == null ? null : _positions.get(identifier);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a portfolio to the master.
   * @param portfolio  the portfolio to add, not null
   */
  public void addPortfolio(Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");

    _portfolios.put(portfolio.getUniqueIdentifier(), portfolio);
    addToCache(portfolio.getUniqueIdentifier().getValue(), null, portfolio.getRootNode());
  }

  /**
   * Adds a node to the cache.
   * @param portfolioId  the id, not null
   * @param node  the node to add, not null
   */
  private void addToCache(String portfolioId, UniqueIdentifier parentNode, PortfolioNode node) {
    // node
    if (node instanceof PortfolioNodeImpl) {
      PortfolioNodeImpl nodeImpl = (PortfolioNodeImpl) node;
      nodeImpl.setUniqueIdentifier(_uidSupplier.getWithValuePrefix(portfolioId + "-"));
      nodeImpl.setParentNode(parentNode);
    }
    _nodes.put(node.getUniqueIdentifier(), node);

    // position
    for (Position position : node.getPositions()) {
      if (position instanceof PositionImpl) {
        PositionImpl positionImpl = (PositionImpl) position;
        positionImpl.setUniqueIdentifier(_uidSupplier.getWithValuePrefix(portfolioId + "-"));
        positionImpl.setPortfolioNode(node.getUniqueIdentifier());
      }
      _positions.put(position.getUniqueIdentifier(), position);
    }

    // recurse
    for (PortfolioNode child : node.getChildNodes()) {
      addToCache(portfolioId, node.getUniqueIdentifier(), child);
    }
  }

  /**
   * Removes a portfolio from the master.
   * 
   * @param portfolio  the portfolio to add, not null
   */
  public void removePortfolio(Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    _portfolios.remove(portfolio.getUniqueIdentifier());
    removeFromCache(portfolio.getRootNode());
  }

  /**
   * Removes a node from the cache
   * 
   * @param node  the node to remove, not null
   */
  private void removeFromCache(PortfolioNode node) {
    _nodes.remove(node.getUniqueIdentifier());
    for (Position position : node.getPositions()) {
      _positions.remove(position.getUniqueIdentifier());
    }
    for (PortfolioNode child : node.getChildNodes()) {
      removeFromCache(child);
    }
  }

}
