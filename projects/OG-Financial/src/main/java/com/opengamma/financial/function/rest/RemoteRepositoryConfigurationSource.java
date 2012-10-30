/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.function.rest;

import java.net.URI;

import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to a {@link RepositoryConfigurationSource}.
 */
public class RemoteRepositoryConfigurationSource extends AbstractRemoteClient implements RepositoryConfigurationSource {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteRepositoryConfigurationSource(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public RepositoryConfiguration getRepositoryConfiguration() {
    URI uri = DataRepositoryConfigurationSourceResource.uriGetAll(getBaseUri());
    return accessRemote(uri).get(RepositoryConfiguration.class);
  }

}
