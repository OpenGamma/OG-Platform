/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.concurrent.ConcurrentMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.google.common.collect.MapMaker;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
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

  private final ConcurrentMap<UniqueId, Object> _frontCache = new MapMaker().weakValues().makeMap();

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
        cleanCaches(event.getObjectId());        
        changeManager().entityChanged(event.getType(), event.getObjectId(), event.getVersionFrom(), event.getVersionTo(), event.getVersionInstant());
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
  public Portfolio getPortfolio(UniqueId uniqueId) {
    Object f = _frontCache.get(uniqueId);
    if (f instanceof Portfolio) {
      return (Portfolio) f;
    }
    if (uniqueId.isLatest()) {
      Portfolio portfolio = getUnderlying().getPortfolio(uniqueId);
      f = _frontCache.putIfAbsent(portfolio.getUniqueId(), portfolio);
      if (f instanceof Portfolio) {
        return (Portfolio) f;
      } else {
        _portfolioCache.put(new Element(portfolio.getUniqueId(), portfolio));
        return portfolio;
      }
    }
    Element e = _portfolioCache.get(uniqueId);
    if (e != null) {
      final Portfolio portfolio = (Portfolio) e.getObjectValue();
      f = _frontCache.putIfAbsent(uniqueId, portfolio);
      if (f instanceof Portfolio) {
        return (Portfolio) f;
      } else {
        return portfolio;
      }
    } else {
      Portfolio portfolio = getUnderlying().getPortfolio(uniqueId);
      f = _frontCache.putIfAbsent(uniqueId, portfolio);
      if (f instanceof Portfolio) {
        return (Portfolio) f;
      } else {
        _portfolioCache.put(new Element(portfolio.getUniqueId(), portfolio));
        return portfolio;
      }
    }
  }

  @Override
  public Portfolio getPortfolio(ObjectId objectId, VersionCorrection versionCorrection) {
    Portfolio portfolio = getUnderlying().getPortfolio(objectId, versionCorrection);
    _portfolioCache.put(new Element(portfolio.getUniqueId(), portfolio));
    return portfolio;
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueId uniqueId) {
    Object f = _frontCache.get(uniqueId);
    if (f instanceof PortfolioNode) {
      return (PortfolioNode) f;
    }
    if (uniqueId.isLatest()) {
      return getUnderlying().getPortfolioNode(uniqueId);
    }
    Element e = _portfolioNodeCache.get(uniqueId);
    if (e != null) {
      final PortfolioNode node = (PortfolioNode) e.getObjectValue();
      f = _frontCache.putIfAbsent(uniqueId, node);
      if (f instanceof PortfolioNode) {
        return (PortfolioNode) f;
      } else {
        return (PortfolioNode) e.getObjectValue();
      }
    } else {
      PortfolioNode node = getUnderlying().getPortfolioNode(uniqueId);
      f = _frontCache.putIfAbsent(uniqueId, node);
      if (f instanceof PortfolioNode) {
        return (PortfolioNode) f;
      } else {
        _portfolioNodeCache.put(new Element(node.getUniqueId(), node));
        return node;
      }
    }
  }

  @Override
  public Position getPosition(UniqueId uniqueId) {
    Object f = _frontCache.get(uniqueId);
    if (f instanceof Position) {
      return (Position) f;
    }
    if (uniqueId.isLatest()) {
      return getUnderlying().getPosition(uniqueId);
    }
    Element e = _positionCache.get(uniqueId);
    if (e != null) {
      final Position position = (Position) e.getObjectValue();
      f = _frontCache.putIfAbsent(uniqueId, position);
      if (f instanceof Position) {
        return (Position) f;
      } else {
        return position;
      }
    } else {
      final Position position = getUnderlying().getPosition(uniqueId);
      f = _frontCache.putIfAbsent(uniqueId, position);
      if (f instanceof Position) {
        return (Position) f;
      } else {
        _positionCache.put(new Element(position.getUniqueId(), position));
        return position;
      }
    }
  }

  @Override
  public Trade getTrade(UniqueId uniqueId) {
    Object f = _frontCache.get(uniqueId);
    if (f instanceof Trade) {
      return (Trade) f;
    }
    if (uniqueId.isLatest()) {
      return getUnderlying().getTrade(uniqueId);
    }
    Element e = _tradeCache.get(uniqueId);
    if (e != null) {
      final Trade trade = (Trade) e.getObjectValue();
      f = _frontCache.putIfAbsent(uniqueId, trade);
      if (f instanceof Trade) {
        return (Trade) f;
      } else {
        return trade;
      }
    } else {
      final Trade trade = getUnderlying().getTrade(uniqueId);
      f = _frontCache.putIfAbsent(uniqueId, trade);
      if (f instanceof Trade) {
        return (Trade) f;
      } else {
        _tradeCache.put(new Element(trade.getUniqueId(), trade));
        return trade;
      }
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
  }

  //-------------------------------------------------------------------------
  private void cleanCaches(ObjectId oid) {    
    _portfolioNodeCache.remove(oid);
    _portfolioCache.remove(oid);
    _positionCache.remove(oid);
    _tradeCache.remove(oid);
    _frontCache.remove(oid);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUnderlying() + "]";
  }

}
