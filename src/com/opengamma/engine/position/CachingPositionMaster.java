/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.EHCacheUtils;

/**
 * A position master implementation that caches another.
 */
public class CachingPositionMaster implements PositionMaster {

  /**
   * Cache key for portfolios.
   */
  private static final String PORTFOLIO_CACHE = "portfolio";
  /**
   * Cache key for nodes.
   */
  private static final String PORTFOLIONODE_CACHE = "portfolioNode";
  /**
   * Cache key for positions.
   */
  private static final String POSITION_CACHE = "position";

  /**
   * The underlying position master.
   */
  private final PositionMaster _underlying;
  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;
  /**
   * The portfolio cache.
   */
  private final Cache _portfolio;
  /**
   * The node cache.
   */
  private final Cache _portfolioNode;
  /**
   * The position cache.
   */
  private final Cache _position;

  /**
   * Creates the cache around an underlying position master.
   * @param underlying  the underlying data, not null
   */
  public CachingPositionMaster(final PositionMaster underlying) {
    this (underlying, EHCacheUtils.createCacheManager());
  }

  /**
   * Creates the cache around an underlying position master.
   * @param underlying  the underlying data, not null
   * @param cacheManager  the cache manager, not null
   */
  public CachingPositionMaster(final PositionMaster underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying Position Master");
    ArgumentChecker.notNull(cacheManager, "EH cache manager");
    _underlying = underlying;
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, PORTFOLIO_CACHE);
    EHCacheUtils.addCache(cacheManager, PORTFOLIONODE_CACHE);
    EHCacheUtils.addCache(cacheManager, POSITION_CACHE);
    _portfolio = EHCacheUtils.getCacheFromManager(cacheManager, PORTFOLIO_CACHE);
    _portfolioNode = EHCacheUtils.getCacheFromManager(cacheManager, PORTFOLIONODE_CACHE);
    _position = EHCacheUtils.getCacheFromManager(cacheManager, POSITION_CACHE);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying position master.
   * @return the underlying position master, not null
   */
  protected PositionMaster getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache manager.
   * @return the cache manager, not null
   */
  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    return getUnderlying().getPortfolioIds();
  }

  @Override
  public Portfolio getPortfolio(UniqueIdentifier identifier) {
    if (identifier.isLatest()) {
      return getUnderlying().getPortfolio(identifier);
    }
    Element e = _portfolio.get(identifier);
    if (e != null) {
      return (Portfolio) e.getValue();
    } else {
      Portfolio p = getUnderlying().getPortfolio(identifier);
      if (p != null) {
        _portfolio.put(new Element(identifier, p));
      }
      return p;
    }
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier identifier) {
    if (identifier.isLatest()) {
      return getUnderlying().getPortfolioNode(identifier);
    }
    Element e = _portfolioNode.get(identifier);
    if (e != null) {
      return (PortfolioNode) e.getValue();
    } else {
      PortfolioNode pn = getUnderlying().getPortfolioNode(identifier);
      if (pn != null) {
        _portfolioNode.put(new Element(identifier, pn));
      }
      return pn;
    }
  }

  @Override
  public Position getPosition(UniqueIdentifier identifier) {
    if (identifier.isLatest()) {
      return getUnderlying().getPosition(identifier);
    }
    Element e = _position.get(identifier);
    if (e != null) {
      return (Position) e.getValue();
    } else {
      Position p = getUnderlying().getPosition(identifier);
      if (p != null) {
        _position.put(new Element(identifier, p));
      }
      return p;
    }
  }

}
