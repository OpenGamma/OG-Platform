/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.security.impl;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.provider.security.SecurityProviderRequest;
import com.opengamma.provider.security.SecurityProviderResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides access to a remote security provider.
 * <p>
 * This is a client that connects to a security provider at a remote URI.
 */
public class RemoteSecurityProvider extends AbstractRemoteClient implements SecurityProvider {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteSecurityProvider(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  // delegate convenience methods to request/result method
  // code copied from AbstractSecurityProvider due to lack of multiple inheritance
  @Override
  public Security getSecurity(ExternalIdBundle externalIdBundle) {
    SecurityProviderRequest request = SecurityProviderRequest.createGet(externalIdBundle, null);
    SecurityProviderResult result = getSecurities(request);
    return result.getResultMap().get(externalIdBundle);
  }

  @Override
  public Map<ExternalIdBundle, Security> getSecurities(Collection<ExternalIdBundle> externalIdBundles) {
    SecurityProviderRequest request = SecurityProviderRequest.createGet(externalIdBundles, null);
    SecurityProviderResult result = getSecurities(request);
    return result.getResultMap();
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityProviderResult getSecurities(SecurityProviderRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    URI uri = DataSecurityProviderResource.uriGet(getBaseUri());
    return accessRemote(uri).post(SecurityProviderResult.class, request);
  }

}
