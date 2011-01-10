/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.util.ehcache.EHCacheUtils;

/* package */class ExecutionPlanCache {

  private static final String CACHE_NAME = "executionPlans";

  private final Cache _cache;

  public ExecutionPlanCache(final CacheManager manager, final int cacheSize) {
    if (cacheSize > 0) {
      EHCacheUtils.addCache(manager, CACHE_NAME, cacheSize, MemoryStoreEvictionPolicy.LRU, false, null, true, 1800, 300, false, 0, null);
      _cache = EHCacheUtils.getCacheFromManager(manager, CACHE_NAME);
    } else {
      _cache = null;
    }
  }

  public void clear() {
    if (_cache != null) {
      _cache.removeAll();
    }
  }

  public RootGraphFragment getCachedExecutionPlan(final DependencyGraph graph) {
    if (_cache != null) {
      final Element element = _cache.get(graph.getDependencyNodes());
      if (element != null) {
        return (RootGraphFragment) element.getObjectValue();
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public void cacheExecutionPlan(final DependencyGraph graph, final RootGraphFragment plan) {
    if (_cache != null) {
      _cache.put(new Element(graph.getDependencyNodes(), plan));
    }
  }

  // TODO [ENG-269] If the function costs change significantly, invalidate the execution plan cache.

}
