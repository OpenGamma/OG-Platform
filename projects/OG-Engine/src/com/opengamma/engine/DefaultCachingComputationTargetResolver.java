/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.google.common.collect.MapMaker;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.target.LazyResolveContext;
import com.opengamma.engine.target.LazyResolver;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A computation target resolver implementation that caches another implementation.
 */
public class DefaultCachingComputationTargetResolver extends DelegatingComputationTargetResolver implements CachingComputationTargetResolver {

  // TODO: move to com.opengamma.engine.target

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

  private final LazyResolveContext _lazyResolveContext;

  private final Queue<Element> _pendingCachePuts = new ConcurrentLinkedQueue<Element>();

  private final AtomicBoolean _cachePutLock = new AtomicBoolean();

  /**
   * Creates an instance using the specified cache manager.
   * 
   * @param underlying the underlying resolver, not null
   * @param cacheManager the cache manager, not null
   */
  public DefaultCachingComputationTargetResolver(final ComputationTargetResolver underlying, final CacheManager cacheManager) {
    super(underlying);
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, COMPUTATIONTARGET_CACHE);
    _computationTarget = EHCacheUtils.getCacheFromManager(cacheManager, COMPUTATIONTARGET_CACHE);
    if (underlying instanceof LazyResolver) {
      final LazyResolver lazyUnderlying = (LazyResolver) underlying;
      final LazyResolveContext context = lazyUnderlying.getLazyResolveContext();
      _lazyResolveContext = new LazyResolveContext(context.getSecuritySource(), this);
      lazyUnderlying.setLazyResolveContext(_lazyResolveContext);
    } else {
      _lazyResolveContext = new LazyResolveContext(underlying.getSecuritySource(), this);
    }
  }

  public void clear() {
    _frontCache.clear();
    _computationTarget.removeAll();
  }

  protected LazyResolveContext getLazyResolveContext() {
    return _lazyResolveContext;
  }

  /**
   * Gets the cache manager.
   * 
   * @return the cache manager, not null
   */
  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  private ComputationTarget resolveImpl(ComputationTargetSpecification specification) {
    ComputationTarget target = _frontCache.get(specification);
    if (target != null) {
      return target;
    }
    final Element e = _computationTarget.get(specification);
    if (e != null) {
      target = (ComputationTarget) e.getValue();
      final ComputationTarget existing = _frontCache.putIfAbsent(MemoryUtils.instance(specification), target);
      if (existing != null) {
        target = existing;
      }
    } else {
      target = super.resolve(specification);
      if (target != null) {
        specification = MemoryUtils.instance(specification);
        final ComputationTarget existing = _frontCache.putIfAbsent(specification, target);
        if (existing != null) {
          target = existing;
        }
        addToCache(specification, target);
      }
    }
    return target;
  }

  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification) {
    final ComputationTarget target = resolveImpl(specification);
    if (target == null) {
      return null;
    }
    switch (specification.getType()) {
      case POSITION:
        cachePosition(target.getPosition());
        break;
      case TRADE:
        cacheTrade(target.getTrade());
        break;
      case PORTFOLIO_NODE:
        cachePortfolioNode(target.getPortfolioNode());
        break;
      case SECURITY:
        cacheSecurity(target.getSecurity());
        break;
    }
    return target;
  }

  private void cachePosition(final Position position) {
    addToCache(position, ComputationTargetType.POSITION);
  }

  @Override
  public void cachePositions(Collection<Position> positions) {
    addToCache(positions, ComputationTargetType.POSITION);
  }

  private void cacheTrade(final Trade trade) {
    addToCache(trade, ComputationTargetType.TRADE);
  }

  @Override
  public void cacheTrades(Collection<Trade> trades) {
    addToCache(trades, ComputationTargetType.TRADE);
  }

  private void cacheSecurity(final Security security) {
    addToCache(security, ComputationTargetType.SECURITY);
  }

  @Override
  public void cacheSecurities(Collection<Security> securities) {
    addToCache(securities, ComputationTargetType.SECURITY);
  }

  private void cachePortfolioNode(PortfolioNode node) {
    addToCache(node, ComputationTargetType.PORTFOLIO_NODE);
  }

  @Override
  public void cachePortfolioNodes(final Collection<PortfolioNode> nodes) {
    addToCache(nodes, ComputationTargetType.PORTFOLIO_NODE);
  }

  private void addToCache(ComputationTargetSpecification specification, final ComputationTarget ct) {
    specification = MemoryUtils.instance(specification);
    _frontCache.put(specification, ct);
    // Don't allow re-entrance to the cache; part of the serialization of a LazyResolver can try to write entries to the
    // cache.
    Element e = new Element(specification, ct);
    if (_cachePutLock.compareAndSet(false, true)) {
      if (getLazyResolveContext().isWriting()) {
        // Being called from serialization logic; hope another thread does an 'addToCache' at some point
        // TODO: could we ask a helper thread to do the pending puts later
        _pendingCachePuts.add(e);
        _cachePutLock.set(false);
        System.err.println("Deferring write from serialization thread " + Thread.currentThread());
      } else {
        int count = 1;
        _computationTarget.put(e);
        do {
          e = _pendingCachePuts.poll();
          while (e != null) {
            _computationTarget.put(e);
            e = _pendingCachePuts.poll();
            count++;
          }
          _cachePutLock.set(false);
        } while (!_pendingCachePuts.isEmpty() && _cachePutLock.compareAndSet(false, true));
      }
    } else {
      _pendingCachePuts.add(e);
    }
  }

  private void addToCache(final UniqueIdentifiable target, final ComputationTargetType targetType) {
    addToCache(new ComputationTargetSpecification(targetType, target.getUniqueId()), new ComputationTarget(targetType, target));
  }

  private void addToCache(final Collection<? extends UniqueIdentifiable> targets, final ComputationTargetType targetType) {
    for (UniqueIdentifiable target : targets) {
      addToCache(target, targetType);
    }
  }

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
