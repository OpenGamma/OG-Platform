/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.EHCacheUtils;

/**
 * A computation target resolver implementation that caches another implementation.
 */
public class CachingComputationTargetResolver extends ForwardingComputationTargetResolver {

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
   * Creates an instance using the default cache manager.
   * @param underlying  the underlying resolver, not null
   */
  public CachingComputationTargetResolver(final ComputationTargetResolver underlying) {
    this (underlying, EHCacheUtils.createCacheManager());
  }

  /**
   * Creates an instance using the specified cache manager.
   * @param underlying  the underlying resolver, not null
   * @param cacheManager  the cache manager, not null
   */
  public CachingComputationTargetResolver(final ComputationTargetResolver underlying, final CacheManager cacheManager) {
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
   * @return the cache manager, not null
   */
  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification) {
    switch (specification.getType()) {
      case POSITION :
      case PORTFOLIO_NODE :
        final Element e = _computationTarget.get(specification);
        if (e != null) {
          return (ComputationTarget) e.getValue();
        } else {
          final ComputationTarget ct = super.resolve(specification);
          if (ct != null) {
            _computationTarget.put(new Element(specification, ct));
          }
          return ct;
        }
      default :
        return super.resolve(specification);
    }
  }

}
