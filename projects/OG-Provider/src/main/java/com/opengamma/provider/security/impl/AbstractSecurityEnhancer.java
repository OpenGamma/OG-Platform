/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.security.impl;

import java.util.List;

import com.google.common.collect.Iterables;
import com.opengamma.core.security.Security;
import com.opengamma.provider.security.SecurityEnhancer;
import com.opengamma.provider.security.SecurityEnhancerRequest;
import com.opengamma.provider.security.SecurityEnhancerResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract implementation of an enhancer of security information.
 * <p>
 * This provides default implementations of the interface methods that delegate to a
 * protected method that subclasses must implement.
 */
public abstract class AbstractSecurityEnhancer implements SecurityEnhancer {

  /**
   * Creates an instance.
   */
  protected AbstractSecurityEnhancer() {
  }

  //-------------------------------------------------------------------------
  @Override
  public Security enhanceSecurity(Security security) {
    SecurityEnhancerRequest request = SecurityEnhancerRequest.create(security);
    SecurityEnhancerResult result = enhanceSecurities(request);
    return Iterables.getOnlyElement(result.getResultList());
  }

  @Override
  public List<Security> enhanceSecurities(List<Security> securities) {
    SecurityEnhancerRequest request = SecurityEnhancerRequest.create(securities);
    SecurityEnhancerResult result = enhanceSecurities(request);
    return result.getResultList();
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityEnhancerResult enhanceSecurities(SecurityEnhancerRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    // short-cut empty case
    if (request.getSecurities().isEmpty()) {
      return new SecurityEnhancerResult();
    }
    
    // get securities
    return doBulkEnhance(request);
  }

  /**
   * Enhances the securities.
   * 
   * @param request  the request, with a non-empty list of securities, not null
   * @return the result, not null
   */
  protected abstract SecurityEnhancerResult doBulkEnhance(SecurityEnhancerRequest request);

}
