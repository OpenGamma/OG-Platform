/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * A <code>DistributionSpecificationResolver</code> that tries to find
 * the distribution spec in a cache. If it doesn't find it, it will 
 * delegate to an underlying <code>DistributionSpecificationResolver</code>.
 *
 * @author pietari
 */
public class CachingDistributionSpecificationResolver implements DistributionSpecificationResolver {
  
  private final DistributionSpecificationResolver _underlying;
  private final Cache _cache;
  
  /**
   * The cache will contain a maximum of 100 entries, which do not expire.
   */
  public CachingDistributionSpecificationResolver(DistributionSpecificationResolver underlying) {
    this(underlying, new Cache("Distribution specification cache", 100, false, true, -1, -1));
  }
  
  public CachingDistributionSpecificationResolver(DistributionSpecificationResolver underlying, Cache cache) {
    ArgumentChecker.checkNotNull(underlying, "Underlying DistributionSpecificationResolver");
    ArgumentChecker.checkNotNull(cache, "Cache");
    _underlying = underlying;     
    _cache = cache;
    if (_cache.getStatus().equals(Status.STATUS_UNINITIALISED)) {
      _cache.initialise();
    }
  }
  

  @Override
  public String getDistributionSpecification(
      LiveDataSpecification fullyResolvedSpec) {
    
    Element cachedDistSpec = _cache.get(new LiveDataSpecification(fullyResolvedSpec));
    if (cachedDistSpec == null) {
      String distSpec = _underlying.getDistributionSpecification(fullyResolvedSpec);
      cachedDistSpec = new Element(new LiveDataSpecification(fullyResolvedSpec), distSpec);
      _cache.put(cachedDistSpec);
    }
    return (String) cachedDistSpec.getValue();
  }

}
