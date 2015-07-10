/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.function.rest;

import java.net.URI;

import org.threeten.bp.Instant;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
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
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteFunctionConfigurationSource(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public FunctionConfigurationBundle getFunctionConfiguration(final Instant version) {
    URI uri = DataRepositoryConfigurationSourceResource.uriGetAll(getBaseUri(), version);
    return accessRemote(uri).get(FunctionConfigurationBundle.class);
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

}
