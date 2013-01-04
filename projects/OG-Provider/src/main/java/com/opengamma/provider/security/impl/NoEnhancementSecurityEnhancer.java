/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.security.impl;

import com.opengamma.provider.security.SecurityEnhancerRequest;
import com.opengamma.provider.security.SecurityEnhancerResult;

/**
 * Simple implementation of an enhancer that performs no enhancement.
 */
public class NoEnhancementSecurityEnhancer extends AbstractSecurityEnhancer {

  /**
   * Creates an instance.
   */
  public NoEnhancementSecurityEnhancer() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected SecurityEnhancerResult doBulkEnhance(SecurityEnhancerRequest request) {
    return new SecurityEnhancerResult(request.getSecurities());
  }

}
