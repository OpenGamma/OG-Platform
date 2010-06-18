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

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of {@code PositionMaster}.
 */
public class InMemoryPositionMaster implements PositionMaster {

  /**
   * The default scheme used for any {@link UniqueIdentifier}s created by this {@link PositionMaster}.
   */
  public static final String DEFAULT_UID_SCHEME = "Memory";
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
  private final AtomicLong _nextIdentityKey = new AtomicLong();
  /**
   * The scheme to use in any UniqueIdentifiers created by this {@link PositionMaster}.
   */
  private final String _scheme;

  /**
   * Creates an empty position master using the default scheme for any {@link UniqueIdentifier}s created.
   */
  public InMemoryPositionMaster() {
    this(DEFAULT_UID_SCHEME);
  }
  
  /**
   * Creates an empty position master using the specified scheme for any {@link UniqueIdentifier}s created.
   * 
   * @param uidScheme  the scheme to use for any {@link UniqueIdentifier}s created
   */
  public InMemoryPositionMaster(String uidScheme) {
    ArgumentChecker.notNull(uidScheme, "Scheme");
    _scheme = uidScheme;
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

  //-------------------------------------------------------------------------`
  /**
   * Adds a portfolio to the master.
   * @param portfolio  the portfolio to add, not null
   */
  public void addPortfolio(Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    _portfolios.put(portfolio.getUniqueIdentifier(), portfolio);
    addToCache(portfolio.getUniqueIdentifier().getValue(), portfolio.getRootNode());
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
      UniqueIdentifier identifier = UniqueIdentifier.of(_scheme, portfolioId + "-" + _nextIdentityKey.incrementAndGet());
      nodeImpl.setUniqueIdentifier(identifier);
    }
    _nodes.put(node.getUniqueIdentifier(), node);
    
    // position
    for (Position position : node.getPositions()) {
      if (position instanceof PositionImpl) {
        PositionImpl positionImpl = (PositionImpl) position;
        UniqueIdentifier identifier = UniqueIdentifier.of(_scheme, portfolioId + "-" + _nextIdentityKey.incrementAndGet());
        positionImpl.setUniqueIdentifier(identifier);
      }
      _positions.put(position.getUniqueIdentifier(), position);
    }
    
    // recurse
    for (PortfolioNode child : node.getChildNodes()) {
      addToCache(portfolioId, child);
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
