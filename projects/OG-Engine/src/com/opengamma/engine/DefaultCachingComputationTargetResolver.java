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
import com.opengamma.engine.target.ComputationTargetResolverUtils;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.lazy.LazyResolveContext;
import com.opengamma.engine.target.lazy.LazyResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A computation target resolver implementation that caches another implementation.
 */
public class DefaultCachingComputationTargetResolver extends DelegatingComputationTargetResolver implements CachingComputationTargetResolver {

  // [PLAT-444]: move to com.opengamma.engine.target

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
   * The cache of "live" target values that have already been resolved. These are keyed by unique identifier so that target specifications which specify different scopes can be satisfied by the same
   * object.
   * <p>
   * EHCache doesn't like being hammered repeatedly for the same objects. Also, if the window of objects being requested is bigger than the in memory window then new objects get created as the on-disk
   * values get deserialized. The solution is to maintain a soft referenced buffer so that all the while the objects we have previously returned are in use we won't requery EHCache for them.
   */
  private final ConcurrentMap<UniqueId, UniqueIdentifiable> _frontObjectCache = new MapMaker().softValues().makeMap();

  /**
   * The cache of "live" targets that have already been resolved. These are keyed by their exact target specifications.
   * <p>
   * EHCache doesn't like being hammered repeatedly for the same objects. Also, if the window of objects being requested is bigger than the in memory window then new objects get created as the on-disk
   * values get deserialized. The solution is to maintain a soft referenced buffer so that all the while the objects we have previously returned are in use we won't requery EHCache for them.
   */
  private final ConcurrentMap<ComputationTargetSpecification, ComputationTarget> _frontTargetCache = new MapMaker().softValues().makeMap();

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
    _frontTargetCache.put(ComputationTargetSpecification.NULL, ComputationTarget.NULL);
  }

  public void clear() {
    _frontObjectCache.clear();
    _frontTargetCache.clear();
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

  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification) {
    ComputationTarget result = _frontTargetCache.get(specification);
    if (result != null) {
      return result;
    }
    final UniqueId uid = specification.getUniqueId();
    UniqueIdentifiable target = _frontObjectCache.get(uid);
    if (target != null) {
      // The cached object may be from an earlier lookup with a different resolution strategy. For example
      // CTSpec[PRIMITIVE, Foo~Bar] will store the UniqueId object in the cache which is not suitable to
      // return for CTSpec[SECURITY, Foo~Bar].
      if (specification.getType().isCompatible(target)) {
        return ComputationTargetResolverUtils.createResolvedTarget(specification, target);
      }
    }
    final Element e = _computationTarget.get(uid);
    if (e != null) {
      target = (UniqueIdentifiable) e.getValue();
      if (specification.getType().isCompatible(target)) {
        final UniqueIdentifiable existing = _frontObjectCache.putIfAbsent(uid, target);
        if (existing != null) {
          result = ComputationTargetResolverUtils.createResolvedTarget(specification, existing);
        } else {
          result = ComputationTargetResolverUtils.createResolvedTarget(specification, target);
        }
        final ComputationTarget newResult = _frontTargetCache.putIfAbsent(specification, result);
        if (newResult != null) {
          return newResult;
        } else {
          return result;
        }
      }
    }
    result = super.resolve(specification);
    if (result != null) {
      final UniqueIdentifiable existing = _frontObjectCache.putIfAbsent(uid, result.getValue());
      if (existing == null) {
        addToCacheImpl(uid, result.getValue());
      }
      final ComputationTarget newResult = _frontTargetCache.putIfAbsent(specification, result);
      if (newResult != null) {
        result = newResult;
      }
    }
    return result;
  }

  @Override
  public ComputationTargetType simplifyType(final ComputationTargetType type) {
    return super.simplifyType(type);
  }

  @Override
  public void cachePositions(Collection<Position> positions) {
    cacheTargets(positions);
  }

  @Override
  public void cacheTrades(Collection<Trade> trades) {
    cacheTargets(trades);
  }

  @Override
  public void cacheSecurities(Collection<Security> securities) {
    cacheTargets(securities);
  }

  @Override
  public void cachePortfolioNodes(final Collection<PortfolioNode> nodes) {
    cacheTargets(nodes);
  }

  private void addToCacheImpl(final UniqueId identifier, final UniqueIdentifiable target) {
    // Don't allow re-entrance to the cache; serialization of a LazyResolver can try to write entries to the
    // cache. Put them into the frontCache only so that we can do a quick lookup if they stay in memory. The
    // problem is that spooling a big root portfolio node to disk can try to resolve and cache all of the
    // child nodes and positions.
    if (LazyResolveContext.isWriting()) {
      return;
    }
    Element e = new Element(identifier, target);
    // If the cache is going to write to disk and two threads hit this then one gets blocked; better to let the
    // second one carry on with something else and the first can do "cache writing" duties until the queue is
    // clear.
    if (_cachePutLock.compareAndSet(false, true)) {
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
    } else {
      _pendingCachePuts.add(e);
    }
  }

  protected void cacheTarget(final UniqueIdentifiable target) {
    final UniqueId uid = target.getUniqueId();
    if (_frontObjectCache.putIfAbsent(uid, target) == null) {
      addToCacheImpl(uid, target);
    }
  }

  @Override
  public void cacheTargets(final Collection<? extends UniqueIdentifiable> targets) {
    for (UniqueIdentifiable target : targets) {
      cacheTarget(target);
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
