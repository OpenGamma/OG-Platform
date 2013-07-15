/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.util.Map;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityLoaderRequest;
import com.opengamma.master.security.SecurityLoaderResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract implementation of a loader of security information.
 * <p>
 * This provides default implementations of the interface methods that delegate to a
 * protected method that subclasses must implement.
 */
public abstract class AbstractSecurityLoader implements SecurityLoader {

  /**
   * Creates an instance.
   */
  protected AbstractSecurityLoader() {
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId loadSecurity(ExternalIdBundle externalIdBundle) {
    SecurityLoaderRequest request = SecurityLoaderRequest.create(externalIdBundle);
    SecurityLoaderResult result = loadSecurities(request);
    if (result.getResultMap().size() == 0) {
      throw new OpenGammaRuntimeException("Unable to load security: " + externalIdBundle);
    }
    return Iterables.getOnlyElement(result.getResultMap().values());
  }

  @Override
  public Map<ExternalIdBundle, UniqueId> loadSecurities(Iterable<ExternalIdBundle> externalIdBundles) {
    SecurityLoaderRequest request = SecurityLoaderRequest.create(externalIdBundles);
    SecurityLoaderResult result = loadSecurities(request);
    return result.getResultMap();
  }

  @Override
  public SecurityLoaderResult loadSecurities(SecurityLoaderRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    // short-cut empty case
    if (request.getExternalIdBundles().isEmpty()) {
      return new SecurityLoaderResult();
    }
    
    // get securities
    return doBulkLoad(request);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the securities.
   * 
   * @param request  the request, with a non-empty list of bundles, not null
   * @return the result, not null
   */
  protected abstract SecurityLoaderResult doBulkLoad(SecurityLoaderRequest request);

}
