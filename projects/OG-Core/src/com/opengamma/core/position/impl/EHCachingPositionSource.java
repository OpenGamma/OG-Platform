/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cache decorating a {@code PositionSource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingPositionSource implements PositionSource {

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
   * The underlying position source.
   */
  private final PositionSource _underlying;
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
   * Creates the cache around an underlying position source.
   * @param underlying  the underlying data, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingPositionSource(final PositionSource underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
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
   * Gets the underlying source of positions.
   * @return the underlying source of positions, not null
   */
  protected PositionSource getUnderlying() {
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
