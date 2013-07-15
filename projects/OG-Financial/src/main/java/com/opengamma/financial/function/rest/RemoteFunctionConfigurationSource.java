/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.function.rest;

import java.net.URI;

import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to a {@link FunctionConfigurationSource}.
 */
public class RemoteFunctionConfigurationSource extends AbstractRemoteClient implements FunctionConfigurationSource {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteFunctionConfigurationSource(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public FunctionConfigurationBundle getFunctionConfiguration() {
    URI uri = DataRepositoryConfigurationSourceResource.uriGetAll(getBaseUri());
    return accessRemote(uri).get(FunctionConfigurationBundle.class);
  }

}
