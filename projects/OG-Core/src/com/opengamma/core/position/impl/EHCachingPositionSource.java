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
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.map.Map2;
import com.opengamma.util.map.WeakValueHashMap2;
import com.opengamma.util.tuple.Pair;

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

  private final Map2<UniqueId, VersionCorrection, Object> _frontCacheByUID = new WeakValueHashMap2<UniqueId, VersionCorrection, Object>();
  private final Map2<ObjectId, VersionCorrection, Object> _frontCacheByOID = new WeakValueHashMap2<ObjectId, VersionCorrection, Object>();

  /**
   * Creates the cache around an underlying position source.
   * 
   * @param underlying the underlying data, not null
   * @param cacheManager the cache manager, not null
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
        // Only care where the unversioned ID has been cached since it now represents something else
        UniqueId latestId = null;
        if (event.getBeforeId() != null) {
          latestId = event.getBeforeId().toLatest();
        }
        if (event.getAfterId() != null) {
          latestId = event.getAfterId().toLatest();
        }
        if (latestId != null) {
          cleanCaches(latestId);
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

  private PortfolioNode addToFrontCache(final PortfolioNode node, final VersionCorrection versionCorrection) {
    Object f = _frontCacheByUID.putIfAbsent(node.getUniqueId(), versionCorrection, node);
    if (f instanceof PortfolioNode) {
      return (PortfolioNode) f;
    } else {
      for (PortfolioNode childNode : node.getChildNodes()) {
        addToFrontCache(childNode, versionCorrection);
      }
      for (Position position : node.getPositions()) {
        f = _frontCacheByUID.putIfAbsent(position.getUniqueId(), VersionCorrection.LATEST, position);
        if (f instanceof Position) {
          position = (Position) f;
        }
        _frontCacheByOID.put(position.getUniqueId().getObjectId(), versionCorrection, position);
      }
      return node;
    }
  }

  @Override
  public Portfolio getPortfolio(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    Object f = _frontCacheByUID.get(uniqueId, versionCorrection);
    if (f instanceof Portfolio) {
      return (Portfolio) f;
    }
    final Pair<UniqueId, VersionCorrection> key = Pair.of(uniqueId, versionCorrection);
    final Element e = _portfolioCache.get(key);
    if (e != null) {
      final Portfolio portfolio = (Portfolio) e.getObjectValue();
      f = _frontCacheByUID.putIfAbsent(uniqueId, versionCorrection, portfolio);
      if (f instanceof Portfolio) {
        return (Portfolio) f;
      } else {
        addToFrontCache(portfolio.getRootNode(), versionCorrection);
        return portfolio;
      }
    } else {
      final Portfolio portfolio = getUnderlying().getPortfolio(uniqueId, versionCorrection);
      f = _frontCacheByUID.putIfAbsent(uniqueId, versionCorrection, portfolio);
      if (f instanceof Portfolio) {
        return (Portfolio) f;
      } else {
        _portfolioCache.put(new Element(key, portfolio));
        return portfolio;
      }
    }
  }

  @Override
  public Portfolio getPortfolio(final ObjectId objectId, final VersionCorrection versionCorrection) {
    Object f = _frontCacheByOID.get(objectId, versionCorrection);
    if (f instanceof Portfolio) {
      return (Portfolio) f;
    }
    final Pair<ObjectId, VersionCorrection> key = Pair.of(objectId, versionCorrection);
    final Element e = _portfolioCache.get(key);
    if (e != null) {
      final Portfolio portfolio = (Portfolio) e.getObjectValue();
      f = _frontCacheByUID.putIfAbsent(portfolio.getUniqueId(), versionCorrection, portfolio);
      if (f instanceof Portfolio) {
        return (Portfolio) f;
      } else {
        _frontCacheByOID.put(objectId, versionCorrection, portfolio);
        addToFrontCache(portfolio.getRootNode(), versionCorrection);
        return portfolio;
      }
    } else {
      final Portfolio portfolio = getUnderlying().getPortfolio(objectId, versionCorrection);
      f = _frontCacheByUID.putIfAbsent(portfolio.getUniqueId(), versionCorrection, portfolio);
      if (f instanceof Portfolio) {
        return (Portfolio) f;
      } else {
        _frontCacheByOID.put(objectId, versionCorrection, portfolio);
        addToFrontCache(portfolio.getRootNode(), versionCorrection);
        _portfolioCache.put(new Element(key, portfolio));
        return portfolio;
      }
    }
  }

  @Override
  public PortfolioNode getPortfolioNode(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    Object f = _frontCacheByUID.get(uniqueId, versionCorrection);
    if (f instanceof PortfolioNode) {
      return (PortfolioNode) f;
    }
    final Pair<UniqueId, VersionCorrection> key = Pair.of(uniqueId, versionCorrection);
    final Element e = _portfolioNodeCache.get(key);
    if (e != null) {
      final PortfolioNode node = (PortfolioNode) e.getObjectValue();
      return addToFrontCache(node, versionCorrection);
    } else {
      final PortfolioNode node = getUnderlying().getPortfolioNode(uniqueId, versionCorrection);
      f = addToFrontCache(node, versionCorrection);
      if (f instanceof PortfolioNode) {
        return (PortfolioNode) f;
      } else {
        _portfolioNodeCache.put(new Element(key, node));
        return node;
      }
    }
  }

  @Override
  public Position getPosition(final UniqueId uniqueId) {
    Object f = _frontCacheByUID.get(uniqueId, VersionCorrection.LATEST);
    if (f instanceof Position) {
      return (Position) f;
    }
    final Element e = _positionCache.get(uniqueId);
    if (e != null) {
      final Position position = (Position) e.getObjectValue();
      f = _frontCacheByUID.putIfAbsent(uniqueId, VersionCorrection.LATEST, position);
      if (f instanceof Position) {
        return (Position) f;
      } else {
        return position;
      }
    } else {
      final Position position = getUnderlying().getPosition(uniqueId);
      f = _frontCacheByUID.putIfAbsent(uniqueId, VersionCorrection.LATEST, position);
      if (f instanceof Position) {
        return (Position) f;
      } else {
        _positionCache.put(new Element(uniqueId, position));
        return position;
      }
    }
  }

  @Override
  public Position getPosition(final ObjectId positionId, final VersionCorrection versionCorrection) {
    Object f = _frontCacheByOID.get(positionId, versionCorrection);
    if (f instanceof Position) {
      return (Position) f;
    }
    final Pair<ObjectId, VersionCorrection> key = Pair.of(positionId, versionCorrection);
    final Element e = _positionCache.get(key);
    if (e != null) {
      final Position position = (Position) e.getObjectValue();
      f = _frontCacheByOID.putIfAbsent(positionId, versionCorrection, position);
      if (f instanceof Position) {
        return (Position) f;
      } else {
        return position;
      }
    } else {
      final Position position = getUnderlying().getPosition(positionId, versionCorrection);
      f = _frontCacheByUID.putIfAbsent(position.getUniqueId(), VersionCorrection.LATEST, position);
      if (f instanceof Position) {
        _frontCacheByOID.put(positionId, versionCorrection, f);
        return (Position) f;
      } else {
        _frontCacheByOID.putIfAbsent(positionId, versionCorrection, position);
        _positionCache.put(new Element(key, position));
        return position;
      }
    }
  }

  @Override
  public Trade getTrade(final UniqueId uniqueId) {
    Object f = _frontCacheByUID.get(uniqueId, VersionCorrection.LATEST);
    if (f instanceof Trade) {
      return (Trade) f;
    }
    final Element e = _tradeCache.get(uniqueId);
    if (e != null) {
      final Trade trade = (Trade) e.getObjectValue();
      f = _frontCacheByUID.putIfAbsent(uniqueId, VersionCorrection.LATEST, trade);
      if (f instanceof Trade) {
        return (Trade) f;
      } else {
        return trade;
      }
    } else {
      final Trade trade = getUnderlying().getTrade(uniqueId);
      f = _frontCacheByUID.putIfAbsent(uniqueId, VersionCorrection.LATEST, trade);
      if (f instanceof Trade) {
        return (Trade) f;
      } else {
        _tradeCache.put(new Element(uniqueId, trade));
        return trade;
      }
    }
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache. It should not be part of a generic lifecycle method.
   */
  protected void shutdown() {
    _underlying.changeManager().removeChangeListener(_changeListener);
    _cacheManager.removeCache(PORTFOLIO_CACHE);
    _cacheManager.removeCache(PORTFOLIONODE_CACHE);
    _cacheManager.removeCache(POSITION_CACHE);
    _cacheManager.removeCache(TRADE_CACHE);
    _frontCacheByUID.clear();
    _frontCacheByOID.clear();
  }

  //-------------------------------------------------------------------------
  private void cleanCaches(UniqueId latestId) {
    _portfolioNodeCache.remove(latestId);
    _portfolioCache.remove(latestId);
    _positionCache.remove(latestId);
    _tradeCache.remove(latestId);
    _frontCacheByUID.removeAllKey1(latestId);
    _frontCacheByOID.removeAllKey1(latestId.getObjectId());
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUnderlying() + "]";
  }

}
