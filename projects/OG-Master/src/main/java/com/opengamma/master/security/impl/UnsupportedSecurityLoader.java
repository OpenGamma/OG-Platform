/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import com.opengamma.master.security.SecurityLoaderRequest;
import com.opengamma.master.security.SecurityLoaderResult;

/**
 * Simple implementation of a loader that is unsupported.
 */
public class UnsupportedSecurityLoader extends AbstractSecurityLoader {

  /**
   * Creates an instance.
   */
  public UnsupportedSecurityLoader() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected SecurityLoaderResult doBulkLoad(SecurityLoaderRequest request) {
    throw new UnsupportedOperationException("Security loading is not supported");
  }

}
