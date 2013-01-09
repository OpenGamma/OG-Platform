/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.security.impl;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.security.SecurityProviderGetRequest;
import com.opengamma.provider.security.SecurityProviderGetResult;

/**
 * Simple implementation of a provider of securities that finds nothing.
 */
public class NoneFoundSecurityProvider extends AbstractSecurityProvider {

  /**
   * Creates an instance.
   */
  public NoneFoundSecurityProvider() {
    super(".*");
  }

  //-------------------------------------------------------------------------
  @Override
  protected SecurityProviderGetResult doBulkGet(SecurityProviderGetRequest request) {
    SecurityProviderGetResult result = new SecurityProviderGetResult();
    for (ExternalIdBundle bundle : request.getExternalIdBundles()) {
      result.getResultMap().put(bundle, null);
    }
    return result;
  }

}
