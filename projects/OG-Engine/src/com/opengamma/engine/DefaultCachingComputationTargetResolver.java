/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.google.common.collect.MapMaker;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A computation target resolver implementation that caches another implementation.
 */
public class DefaultCachingComputationTargetResolver extends DelegatingComputationTargetResolver implements CachingComputationTargetResolver {

  /** The cache key. */
  private static final String COMPUTATIONTARGET_CACHE = "computationTarget";

  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;
  /**
   * The cache.
   */
  private final Cache _computationTarget;
  /**
   * EHCache doesn't like being hammered repeatedly for the same objects. Also, if the window of objects being requested is bigger than the in memory window then new objects get created as the on-disk
   * values get deserialized. The solution is to maintain a soft referenced buffer so that all the while the objects we have previously returned are in use we won't requery EHCache for them.
   */
  private final ConcurrentMap<ComputationTargetSpecification, ComputationTarget> _frontCache = new MapMaker().softValues().makeMap();

  /**
   * Creates an instance using the specified cache manager.
   * 
   * @param underlying  the underlying resolver, not null
   * @param cacheManager  the cache manager, not null
   */
  public DefaultCachingComputationTargetResolver(final ComputationTargetResolver underlying, final CacheManager cacheManager) {
    super(underlying);
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, COMPUTATIONTARGET_CACHE);
    _computationTarget = EHCacheUtils.getCacheFromManager(cacheManager, COMPUTATIONTARGET_CACHE);
    if (underlying instanceof DefaultComputationTargetResolver) {
      ((DefaultComputationTargetResolver) underlying).setRecursiveResolver(this);
    }
  }

  //-------------------------------------------------------------------------
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
  public ComputationTarget resolve(final ComputationTargetSpecification specification) {
    switch (specification.getType()) {
      case POSITION:
      case TRADE:
      case PORTFOLIO_NODE:
      case SECURITY:
        ComputationTarget target = _frontCache.get(specification);
        if (target != null) {
          return target;
        }
        final Element e = _computationTarget.get(specification);
        if (e != null) {
          target = (ComputationTarget) e.getValue();
          final ComputationTarget existing = _frontCache.putIfAbsent(specification, target);
          if (existing != null) {
            target = existing;
          }
        } else {
          target = super.resolve(specification);
          if (target != null) {
            final ComputationTarget existing = _frontCache.putIfAbsent(specification, target);
            if (existing != null) {
              target = existing;
            }
            addToCache(specification, target);
          }
        }
        return target;
      default:
        return super.resolve(specification);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void cachePositions(Collection<Position> positions) {
    addToCache(positions, ComputationTargetType.POSITION);
  }

  @Override
  public void cacheTrades(Collection<Trade> trades) {
    addToCache(trades, ComputationTargetType.TRADE);
  }

  @Override
  public void cacheSecurities(Collection<Security> securities) {
    addToCache(securities, ComputationTargetType.SECURITY);
  }

  @Override
  public void cachePortfolioNodeHierarchy(PortfolioNode root) {
    addToCache(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, root.getUniqueId()), new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, root));
    for (PortfolioNode child : root.getChildNodes()) {
      cachePortfolioNodeHierarchy(child);
    }
  }

  //-------------------------------------------------------------------------
  private void addToCache(ComputationTargetSpecification specification, ComputationTarget ct) {
    _computationTarget.put(new Element(specification, ct));
    _frontCache.put(MemoryUtils.instance(specification), ct);
  }

  private void addToCache(Collection<? extends UniqueIdentifiable> targets, ComputationTargetType targetType) {
    for (UniqueIdentifiable target : targets) {
      addToCache(new ComputationTargetSpecification(targetType, target.getUniqueId()), new ComputationTarget(targetType, target));
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string suitable for debugging.
   * 
   * @return the string, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[underlying=" + getUnderlying() + "]";
  }

}
