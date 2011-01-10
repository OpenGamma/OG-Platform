/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * A <code>DistributionSpecificationResolver</code> that tries to find
 * the distribution spec in a cache. If it doesn't find it, it will 
 * delegate to an underlying <code>DistributionSpecificationResolver</code>.
 *
 */
public class CachingDistributionSpecificationResolver implements DistributionSpecificationResolver {
  
  private final DistributionSpecificationResolver _underlying;
  private final Cache _cache;
  
  /**
   * The cache will contain a maximum of 100 entries, which do not expire.
   * 
   * @param underlying Underlying DistributionSpecificationResolver
   */
  public CachingDistributionSpecificationResolver(DistributionSpecificationResolver underlying) {
    this(underlying, new Cache("Distribution specification cache", 100, false, true, -1, -1));
  }
  
  public CachingDistributionSpecificationResolver(DistributionSpecificationResolver underlying, Cache cache) {
    ArgumentChecker.notNull(underlying, "Underlying DistributionSpecificationResolver");
    ArgumentChecker.notNull(cache, "Cache");
    _underlying = underlying;     
    _cache = cache;
    if (_cache.getStatus().equals(Status.STATUS_UNINITIALISED)) {
      _cache.initialise();
    }
  }
  

  @Override
  public DistributionSpecification getDistributionSpecification(
      LiveDataSpecification liveDataSpecificationFromClient) {
    
    Element cachedDistSpec = _cache.get(liveDataSpecificationFromClient);
    if (cachedDistSpec == null) {
      DistributionSpecification distSpec = _underlying.getDistributionSpecification(liveDataSpecificationFromClient);
      cachedDistSpec = new Element(liveDataSpecificationFromClient, distSpec);
      _cache.put(cachedDistSpec);
    }
    return (DistributionSpecification) cachedDistSpec.getObjectValue();
  }

}
