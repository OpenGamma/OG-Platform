/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.net.URI;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityLoaderRequest;
import com.opengamma.master.security.SecurityLoaderResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides access to a remote security loader.
 * <p>
 * This is a client that connects to a security loader at a remote URI.
 */
public class RemoteSecurityLoader extends AbstractRemoteClient implements SecurityLoader {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteSecurityLoader(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  // delegate convenience methods to request/result method
  // code copied from AbstractSecurityLoader due to lack of multiple inheritance
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

  //-------------------------------------------------------------------------
  @Override
  public SecurityLoaderResult loadSecurities(SecurityLoaderRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    URI uri = DataSecurityLoaderResource.uriGet(getBaseUri());
    return accessRemote(uri).post(SecurityLoaderResult.class, request);
  }

}
