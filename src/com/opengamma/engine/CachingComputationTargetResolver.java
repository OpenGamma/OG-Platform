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
 * A computation target resolver implementation that caches another.
 */
public class CachingComputationTargetResolver implements ComputationTargetResolver {
  
  private static final String COMPUTATIONTARGET_CACHE = "computationTarget";
  
  private final ComputationTargetResolver _underlying;
  private final CacheManager _cacheManager;
  private final Cache _computationTarget;
  
  public CachingComputationTargetResolver (final ComputationTargetResolver underlying) {
    this (underlying, EHCacheUtils.createCacheManager ());
  }
  
  public CachingComputationTargetResolver (final ComputationTargetResolver underlying, CacheManager cacheManager) {
    ArgumentChecker.notNull (underlying, "underlying computation target resolver");
    ArgumentChecker.notNull (cacheManager, "cache manager");
    _underlying = underlying;
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, COMPUTATIONTARGET_CACHE);
    _computationTarget = EHCacheUtils.getCacheFromManager(cacheManager, COMPUTATIONTARGET_CACHE);
    if (underlying instanceof DefaultComputationTargetResolver) {
      ((DefaultComputationTargetResolver)underlying).setRecursiveResolver (this);
    }
  }
  
  protected ComputationTargetResolver getUnderlying () {
    return _underlying;
  }
  
  protected CacheManager getCacheManager () {
    return _cacheManager;
  }

  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification) {
    switch (specification.getType ()) {
      case POSITION :
      case MULTIPLE_POSITIONS :
        final Element e = _computationTarget.get (specification);
        if (e != null) {
          return (ComputationTarget)e.getValue ();
        } else {
          final ComputationTarget ct = getUnderlying ().resolve (specification);
          if (ct != null) {
            _computationTarget.put (new Element (specification, ct));
          }
          return ct;
        }
      default :
        return getUnderlying ().resolve (specification);
    }
  }
   
}
