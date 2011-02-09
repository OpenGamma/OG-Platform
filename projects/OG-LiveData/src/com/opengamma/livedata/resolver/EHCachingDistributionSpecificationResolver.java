/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A <code>DistributionSpecificationResolver</code> that tries to find
 * the distribution spec in a cache. If it doesn't find it, it will 
 * delegate to an underlying <code>DistributionSpecificationResolver</code>.
 *
 */
public class EHCachingDistributionSpecificationResolver
  extends AbstractResolver<LiveDataSpecification, DistributionSpecification> 
  implements DistributionSpecificationResolver {
  
  /**
   * Cache key for distribution specs.
   */
  private static final String DISTRIBUTION_SPEC_CACHE = "distributionSpecification";
  
  private final DistributionSpecificationResolver _underlying;
  
  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;
  
  /**
   * The reference data cache.
   */
  private final Cache _cache;
  
  public EHCachingDistributionSpecificationResolver(final DistributionSpecificationResolver underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "Underlying DistributionSpecificationResolver");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, DISTRIBUTION_SPEC_CACHE);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, DISTRIBUTION_SPEC_CACHE);
  }
  
  public CacheManager getCacheManager() {
    return _cacheManager;
  }
  
  @Override
  public Map<LiveDataSpecification, DistributionSpecification> resolve(
      Collection<LiveDataSpecification> liveDataSpecificationFromClient) {
    
    Map<LiveDataSpecification, DistributionSpecification> returnValue = new HashMap<LiveDataSpecification, DistributionSpecification>();
    
    Collection<LiveDataSpecification> notFound = new ArrayList<LiveDataSpecification>();
    
    for (LiveDataSpecification spec : liveDataSpecificationFromClient) {
      Element cachedDistSpec = _cache.get(spec);
      if (cachedDistSpec != null) {
        returnValue.put(spec, (DistributionSpecification) cachedDistSpec.getObjectValue());        
      } else {
        notFound.add(spec);
      }
    }
    
    if (!notFound.isEmpty()) {
      Map<LiveDataSpecification, DistributionSpecification> underlyingResult = 
        _underlying.resolve(notFound);
      
      returnValue.putAll(underlyingResult);
      
      for (Map.Entry<LiveDataSpecification, DistributionSpecification> entry : underlyingResult.entrySet()) {
        Element cachedDistSpec = new Element(entry.getKey(), entry.getValue());
        _cache.put(cachedDistSpec);
      }
    }
    
    return returnValue;
  }

}
