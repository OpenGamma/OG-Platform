/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.id.VersionCorrectionUtils;
import com.opengamma.id.VersionCorrectionUtils.VersionCorrectionLockListener;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.WeakInstanceCache;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.map.HashMap2;
import com.opengamma.util.map.Map2;
import com.opengamma.util.map.WeakValueHashMap2;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A cache decorating a {@code PositionSource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 * <p>
 * Any requests with a "latest" version/correction or unversioned unique identifier are not cached and will always hit the underlying. This should not be an issue in practice as the engine components
 * which use the position source will always specify an exact version/correction and versioned unique identifiers.
 */
public class EHCachingPositionSource implements PositionSource {

  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingPositionSource.class);

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

  private static class CachedPortfolio implements Portfolio {

    private final Map<String, String> _attributes;
    private final UniqueId _uniqueId;
    private final PortfolioNode _rootNode;
    private final String _name;

    public CachedPortfolio(final Portfolio original, final PortfolioNode replacementRoot) {
      _attributes = original.getAttributes();
      _uniqueId = original.getUniqueId();
      _rootNode = replacementRoot;
      _name = original.getName();
    }

    @Override
    public Map<String, String> getAttributes() {
      return _attributes;
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addAttribute(String key, String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public UniqueId getUniqueId() {
      return _uniqueId;
    }

    @Override
    public PortfolioNode getRootNode() {
      return _rootNode;
    }

    @Override
    public String getName() {
      return _name;
    }

  }

  private final ConcurrentMap<UniqueId, Object> _frontPositionOrTradeCache = new MapMaker().weakValues().makeMap();
  private final Map2<VersionCorrection, UniqueId, Object> _frontCacheByUID = new WeakValueHashMap2<VersionCorrection, UniqueId, Object>(HashMap2.STRONG_KEYS);
  private final Map2<VersionCorrection, ObjectId, Object> _frontCacheByOID = new WeakValueHashMap2<VersionCorrection, ObjectId, Object>(HashMap2.STRONG_KEYS);

  private final VersionCorrectionLockListener _frontCacheCleaner = new VersionCorrectionLockListener() {
    @Override
    public void versionCorrectionUnlocked(final VersionCorrection unlocked, final Collection<VersionCorrection> stillLocked) {
      _frontCacheByUID.retainAllKey1(stillLocked);
      _frontCacheByOID.retainAllKey1(stillLocked);
    }
  };

  private final WeakInstanceCache<PortfolioNode> _nodes = new WeakInstanceCache<PortfolioNode>();

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
    VersionCorrectionUtils.addVersionCorrectionLockListener(_frontCacheCleaner);
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

  protected Position addToFrontCache(Position position, final VersionCorrection versionCorrection) {
    final Object f = _frontPositionOrTradeCache.putIfAbsent(position.getUniqueId(), position);
    if (f instanceof Position) {
      position = (Position) f;
    }
    _frontCacheByOID.put(versionCorrection, position.getUniqueId().getObjectId(), position);
    return position;
  }

  protected PortfolioNode addToFrontCache(PortfolioNode node, final VersionCorrection versionCorrection) {
    final List<Position> nodePositions = node.getPositions();
    List<Position> newPositions = null;
    for (int i = 0; i < nodePositions.size(); i++) {
      final Position nodePosition = nodePositions.get(i);
      final Position newPosition = addToFrontCache(nodePosition, versionCorrection);
      if (newPosition != nodePosition) {
        if (newPositions == null) {
          newPositions = new ArrayList<Position>(nodePositions.size());
          for (int j = 0; j < i; j++) {
            newPositions.add(nodePositions.get(j));
          }
        }
        newPositions.add(newPosition);
      } else {
        if (newPositions != null) {
          newPositions.add(nodePosition);
        }
      }
    }
    final List<PortfolioNode> nodeChildren = node.getChildNodes();
    List<PortfolioNode> newChildren = null;
    for (int i = 0; i < nodeChildren.size(); i++) {
      final PortfolioNode nodeChild = nodeChildren.get(i);
      final PortfolioNode newChild = addToFrontCache(nodeChild, versionCorrection);
      if (newChild != nodeChild) {
        if (newChildren == null) {
          newChildren = new ArrayList<PortfolioNode>(nodeChildren.size());
          for (int j = 0; j < i; j++) {
            newChildren.add(nodeChildren.get(j));
          }
        }
        newChildren.add(newChild);
      } else {
        if (newChildren != null) {
          newChildren.add(nodeChild);
        }
      }
    }
    if ((newPositions != null) || (newChildren != null)) {
      final SimplePortfolioNode newNode = new SimplePortfolioNode(node.getUniqueId(), node.getName());
      newNode.setParentNodeId(node.getParentNodeId());
      newNode.addPositions((newPositions != null) ? newPositions : node.getPositions());
      newNode.addChildNodes((newChildren != null) ? newChildren : node.getChildNodes());
      node = newNode;
    }
    node = _nodes.get(node);
    final Object f = _frontCacheByUID.putIfAbsent(versionCorrection, node.getUniqueId(), node);
    if (f instanceof PortfolioNode) {
      node = (PortfolioNode) f;
    }
    return node;
  }

  protected Portfolio addToFrontCache(Portfolio portfolio, final VersionCorrection versionCorrection) {
    final PortfolioNode newRoot = addToFrontCache(portfolio.getRootNode(), versionCorrection);
    if (newRoot != portfolio.getRootNode()) {
      final Portfolio newPortfolio = new CachedPortfolio(portfolio, newRoot);
      return newPortfolio;
    } else {
      return portfolio;
    }
  }

  @Override
  public Portfolio getPortfolio(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    if (versionCorrection.containsLatest()) {
      s_logger.debug("getPortfolioByUniqueId: Skipping cache for {}/{}", uniqueId, versionCorrection);
      return getUnderlying().getPortfolio(uniqueId, versionCorrection);
    }
    Object f;
    final Pair<UniqueId, VersionCorrection> key;
    if (uniqueId.isVersioned()) {
      f = _frontCacheByUID.get(versionCorrection, uniqueId);
      if (f instanceof Portfolio) {
        s_logger.debug("getPortfolioByUniqueId: Front cache hit on {}/{}", uniqueId, versionCorrection);
        return (Portfolio) f;
      }
      key = Pairs.of(uniqueId, versionCorrection);
      final Element e = _portfolioCache.get(key);
      if (e != null) {
        s_logger.debug("getPortfolioByUniqueId: EHCache hit on {}/{}", uniqueId, versionCorrection);
        Portfolio portfolio = (Portfolio) e.getObjectValue();
        f = _frontCacheByUID.putIfAbsent(versionCorrection, uniqueId, portfolio);
        if (f instanceof Portfolio) {
          s_logger.debug("getPortfolioByUniqueId: Late front cache hit on {}/{}", uniqueId, versionCorrection);
          return (Portfolio) f;
        }
        portfolio = addToFrontCache(portfolio, versionCorrection);
        return portfolio;
      } else {
        s_logger.debug("getPortfolioByUniqueId: Cache miss on {}/{}", uniqueId, versionCorrection);
      }
    } else {
      s_logger.debug("getPortfolioByUniqueId: Pass through on {}/{}", uniqueId, versionCorrection);
      key = null;
    }
    Portfolio portfolio = getUnderlying().getPortfolio(uniqueId, versionCorrection);
    f = _frontCacheByUID.putIfAbsent(versionCorrection, portfolio.getUniqueId(), portfolio);
    if (f instanceof Portfolio) {
      s_logger.debug("getPortfolioByUniqueId: Late front cache hit on {}/{}", uniqueId, versionCorrection);
      return (Portfolio) f;
    }
    portfolio = addToFrontCache(portfolio, versionCorrection);
    if (key != null) {
      _portfolioCache.put(new Element(key, portfolio));
    } else {
      _portfolioCache.put(new Element(Pairs.of(portfolio.getUniqueId(), versionCorrection), portfolio));
    }
    return portfolio;
  }

  @Override
  public Portfolio getPortfolio(final ObjectId objectId, final VersionCorrection versionCorrection) {
    if (versionCorrection.containsLatest()) {
      s_logger.debug("getPortfolioByObjectId: Skipping cache for {}/{}", objectId, versionCorrection);
      return getUnderlying().getPortfolio(objectId, versionCorrection);
    }
    Object f = _frontCacheByOID.get(versionCorrection, objectId);
    if (f instanceof Portfolio) {
      s_logger.debug("getPortfolioByObjectId: Front cache hit on {}/{}", objectId, versionCorrection);
      return (Portfolio) f;
    }
    final Pair<ObjectId, VersionCorrection> key = Pairs.of(objectId, versionCorrection);
    final Element e = _portfolioCache.get(key);
    if (e != null) {
      s_logger.debug("getPortfolioByObjectId: EHCache hit on {}/{}", objectId, versionCorrection);
      Portfolio portfolio = (Portfolio) e.getObjectValue();
      f = _frontCacheByUID.putIfAbsent(versionCorrection, portfolio.getUniqueId(), portfolio);
      if (f instanceof Portfolio) {
        s_logger.debug("getPortfolioByObjectId: Late front cache hit on {}/{}", objectId, versionCorrection);
        portfolio = (Portfolio) f;
        _frontCacheByOID.put(versionCorrection, objectId, portfolio);
        return portfolio;
      } else {
        portfolio = addToFrontCache(portfolio, versionCorrection);
        _frontCacheByOID.put(versionCorrection, objectId, portfolio);
        return portfolio;
      }
    } else {
      s_logger.debug("getPortfolioByObjectId: Cache miss on {}/{}", objectId, versionCorrection);
      Portfolio portfolio = getUnderlying().getPortfolio(objectId, versionCorrection);
      f = _frontCacheByUID.putIfAbsent(versionCorrection, portfolio.getUniqueId(), portfolio);
      if (f instanceof Portfolio) {
        s_logger.debug("getPortfolioByObjectId: Late front cache hit on {}/{}", objectId, versionCorrection);
        portfolio = (Portfolio) f;
        _frontCacheByOID.put(versionCorrection, objectId, portfolio);
        return portfolio;
      } else {
        portfolio = addToFrontCache(portfolio, versionCorrection);
        _frontCacheByOID.put(versionCorrection, objectId, portfolio);
        _portfolioCache.put(new Element(key, portfolio));
        _portfolioCache.put(new Element(Pairs.of(portfolio.getUniqueId(), versionCorrection), portfolio));
        return portfolio;
      }
    }
  }

  @Override
  public PortfolioNode getPortfolioNode(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    if (versionCorrection.containsLatest()) {
      s_logger.debug("getPortfolioNode: Skipping cache for {}/{}", uniqueId, versionCorrection);
      return getUnderlying().getPortfolioNode(uniqueId, versionCorrection);
    }
    Object f;
    final Pair<UniqueId, VersionCorrection> key;
    if (uniqueId.isVersioned()) {
      f = _frontCacheByUID.get(versionCorrection, uniqueId);
      if (f instanceof PortfolioNode) {
        s_logger.debug("getPortfolioNode: Front cache hit on {}/{}", uniqueId, versionCorrection);
        return (PortfolioNode) f;
      }
      key = Pairs.of(uniqueId, versionCorrection);
      final Element e = _portfolioNodeCache.get(key);
      if (e != null) {
        s_logger.debug("getPortfolioNode: EHCache hit on {}/{}", uniqueId, versionCorrection);
        final PortfolioNode node = (PortfolioNode) e.getObjectValue();
        return addToFrontCache(node, versionCorrection);
      } else {
        s_logger.debug("getPortfolioNode: EHCache miss on {}/{}", uniqueId, versionCorrection);
      }
    } else {
      s_logger.debug("getPortfolioNode: Pass through on {}/{}", uniqueId, versionCorrection);
      key = null;
    }
    final PortfolioNode node = getUnderlying().getPortfolioNode(uniqueId, versionCorrection);
    f = addToFrontCache(node, versionCorrection);
    if (f != node) {
      s_logger.debug("getPortfolioNode: Late front cache hit on {}/{}", uniqueId, versionCorrection);
      return (PortfolioNode) f;
    }
    if (key != null) {
      _portfolioNodeCache.put(new Element(key, node));
    } else {
      _portfolioNodeCache.put(new Element(Pairs.of(node.getUniqueId(), versionCorrection), node));
    }
    return node;
  }

  @Override
  public Position getPosition(final UniqueId uniqueId) {
    Object f;
    if (uniqueId.isVersioned()) {
      f = _frontPositionOrTradeCache.get(uniqueId);
      if (f instanceof Position) {
        s_logger.debug("getPositionByUniqueId: Front cache hit on {}", uniqueId);
        return (Position) f;
      }
      final Element e = _positionCache.get(uniqueId);
      if (e != null) {
        s_logger.debug("getPositionByUniqueId: EHCache hit on {}", uniqueId);
        final Position position = (Position) e.getObjectValue();
        f = _frontPositionOrTradeCache.putIfAbsent(uniqueId, position);
        if (f instanceof Position) {
          s_logger.debug("getPositionByUniqueId: Late front cache hit on {}", uniqueId);
          return (Position) f;
        } else {
          return position;
        }
      }
    } else {
      s_logger.debug("getPositionByUniqueId: Pass through on {}", uniqueId);
    }
    final Position position = getUnderlying().getPosition(uniqueId);
    f = _frontPositionOrTradeCache.putIfAbsent(position.getUniqueId(), position);
    if (f instanceof Position) {
      s_logger.debug("getPositionByUniqueId: Late front cache hit on {}", uniqueId);
      return (Position) f;
    }
    _positionCache.put(new Element(position.getUniqueId(), position));
    return position;
  }

  @Override
  public Position getPosition(final ObjectId positionId, final VersionCorrection versionCorrection) {
    if (versionCorrection.containsLatest()) {
      s_logger.debug("getPositionByObjectId: Skipping cache for {}/{}", positionId, versionCorrection);
      return getUnderlying().getPosition(positionId, versionCorrection);
    }
    Object f = _frontCacheByOID.get(versionCorrection, positionId);
    if (f instanceof Position) {
      s_logger.debug("getPositionByObjectId: Front cache hit on {}/{}", positionId, versionCorrection);
      return (Position) f;
    }
    final Pair<ObjectId, VersionCorrection> key = Pairs.of(positionId, versionCorrection);
    final Element e = _positionCache.get(key);
    if (e != null) {
      s_logger.debug("getPositionByObjectId: EHCache hit on {}/{}", positionId, versionCorrection);
      final Position position = (Position) e.getObjectValue();
      f = _frontCacheByOID.putIfAbsent(versionCorrection, positionId, position);
      if (f instanceof Position) {
        s_logger.debug("getPositionByObjectId: Late front cache hit on {}/{}", positionId, versionCorrection);
        return (Position) f;
      } else {
        return position;
      }
    } else {
      s_logger.debug("getPositionByObjectId: Cache miss on {}/{}", positionId, versionCorrection);
      final Position position = getUnderlying().getPosition(positionId, versionCorrection);
      f = _frontPositionOrTradeCache.putIfAbsent(position.getUniqueId(), position);
      if (f instanceof Position) {
        s_logger.debug("getPositionByObjectId: Late front cache hit on {}/{}", positionId, versionCorrection);
        _frontCacheByOID.put(versionCorrection, positionId, f);
        return (Position) f;
      } else {
        _frontCacheByOID.put(versionCorrection, positionId, position);
        _positionCache.put(new Element(key, position));
        _positionCache.put(new Element(position.getUniqueId(), position));
        return position;
      }
    }
  }

  @Override
  public Trade getTrade(final UniqueId uniqueId) {
    Object f;
    if (uniqueId.isVersioned()) {
      f = _frontPositionOrTradeCache.get(uniqueId);
      if (f instanceof Trade) {
        s_logger.debug("getTradeByUniqueId: Front cache hit on {}", uniqueId);
        return (Trade) f;
      }
      final Element e = _tradeCache.get(uniqueId);
      if (e != null) {
        s_logger.debug("getTradeByUniqueId: EHCache hit on {}", uniqueId);
        final Trade trade = (Trade) e.getObjectValue();
        f = _frontPositionOrTradeCache.putIfAbsent(uniqueId, trade);
        if (f instanceof Trade) {
          s_logger.debug("getTradeByUniqueId: Late front cache hit on {}", uniqueId);
          return (Trade) f;
        } else {
          return trade;
        }
      } else {
        s_logger.debug("getTradeByUniqueId: Cache miss on {}", uniqueId);
      }
    } else {
      s_logger.debug("getTradeByUniqueId: Pass through on {}", uniqueId);
    }
    final Trade trade = getUnderlying().getTrade(uniqueId);
    f = _frontPositionOrTradeCache.putIfAbsent(trade.getUniqueId(), trade);
    if (f instanceof Trade) {
      s_logger.debug("getTradeByUniqueId: Late front cache hit on {}", uniqueId);
      return (Trade) f;
    }
    _tradeCache.put(new Element(trade.getUniqueId(), trade));
    return trade;
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache. It should not be part of a generic lifecycle method.
   */
  protected void shutdown() {
    _cacheManager.removeCache(PORTFOLIO_CACHE);
    _cacheManager.removeCache(PORTFOLIONODE_CACHE);
    _cacheManager.removeCache(POSITION_CACHE);
    _cacheManager.removeCache(TRADE_CACHE);
    _frontCacheByUID.clear();
    _frontCacheByOID.clear();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUnderlying() + "]";
  }

}
