/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.id.UniqueId;
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
   * Cache key for trades.
   */
  private static final String TRADE_CACHE = "trade";

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
  private final Cache _portfolioCache;
  /**
   * The node cache.
   */
  private final Cache _portfolioNodeCache;
  /**
   * The position cache.
   */
  private final Cache _positionCache;
  /**
   * The trade cache.
   */
  private final Cache _tradeCache;
  /**
   * Listens for changes in the underlying position source.
   */
  private final ChangeListener _changeListener;
  /**
   * Local change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates the cache around an underlying position source.
   * 
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
    EHCacheUtils.addCache(cacheManager, TRADE_CACHE);
    _portfolioCache = EHCacheUtils.getCacheFromManager(cacheManager, PORTFOLIO_CACHE);
    _portfolioNodeCache = EHCacheUtils.getCacheFromManager(cacheManager, PORTFOLIONODE_CACHE);
    _positionCache = EHCacheUtils.getCacheFromManager(cacheManager, POSITION_CACHE);
    _tradeCache = EHCacheUtils.getCacheFromManager(cacheManager, TRADE_CACHE);
    _changeManager = new BasicChangeManager();
    _changeListener = new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        if (event.getBeforeId() != null) {
          cleanCaches(event.getBeforeId());
        }
        if (event.getAfterId() != null) {
          cleanCaches(event.getAfterId());
        }
        changeManager().entityChanged(event.getType(), event.getBeforeId(), event.getAfterId(), event.getVersionInstant());
      }
    };
    underlying.changeManager().addChangeListener(_changeListener);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying source of positions.
   * 
   * @return the underlying source of positions, not null
   */
  protected PositionSource getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache manager.
   * 
   * @return the cache manager, not null
   */
  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getPortfolio(UniqueId identifier) {
    if (identifier.isLatest()) {
      return getUnderlying().getPortfolio(identifier);
    }
    Element e = _portfolioCache.get(identifier);
    if (e != null) {
      return (Portfolio) e.getValue();
    } else {
      Portfolio p = getUnderlying().getPortfolio(identifier);
      if (p != null) {
        _portfolioCache.put(new Element(identifier, p));
      }
      return p;
    }
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueId identifier) {
    if (identifier.isLatest()) {
      return getUnderlying().getPortfolioNode(identifier);
    }
    Element e = _portfolioNodeCache.get(identifier);
    if (e != null) {
      return (PortfolioNode) e.getValue();
    } else {
      PortfolioNode pn = getUnderlying().getPortfolioNode(identifier);
      if (pn != null) {
        _portfolioNodeCache.put(new Element(identifier, pn));
      }
      return pn;
    }
  }

  @Override
  public Position getPosition(UniqueId identifier) {
    if (identifier.isLatest()) {
      return getUnderlying().getPosition(identifier);
    }
    Element e = _positionCache.get(identifier);
    if (e != null) {
      return (Position) e.getValue();
    } else {
      Position p = getUnderlying().getPosition(identifier);
      if (p != null) {
        _positionCache.put(new Element(identifier, p));
      }
      return p;
    }
  }

  @Override
  public Trade getTrade(UniqueId identifier) {
    if (identifier.isLatest()) {
      return getUnderlying().getTrade(identifier);
    }
    Element e = _tradeCache.get(identifier);
    if (e != null) {
      return (Trade) e.getValue();
    } else {
      Trade t = getUnderlying().getTrade(identifier);
      if (t != null) {
        _tradeCache.put(new Element(identifier, t));
      }
      return t;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache.
   * It should not be part of a generic lifecycle method.
   */
  protected void shutdown() {
    _underlying.changeManager().removeChangeListener(_changeListener);
    _cacheManager.removeCache(PORTFOLIO_CACHE);
    _cacheManager.removeCache(PORTFOLIONODE_CACHE);
    _cacheManager.removeCache(POSITION_CACHE);
    _cacheManager.removeCache(TRADE_CACHE);
    _cacheManager.shutdown();
  }

  //-------------------------------------------------------------------------
  private void cleanCaches(UniqueId id) {
    // Only care where the unversioned ID has been cached since it now represents something else
    UniqueId latestId = id.toLatest();
    _portfolioNodeCache.remove(latestId);
    _portfolioCache.remove(latestId);
    _positionCache.remove(latestId);
    _tradeCache.remove(latestId);
  }

}
