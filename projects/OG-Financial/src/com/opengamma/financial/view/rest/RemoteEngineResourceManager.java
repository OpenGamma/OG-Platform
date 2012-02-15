/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import com.opengamma.engine.view.calc.EngineResourceManager;
import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.rest.FudgeRestClient;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Remote implementation of {@link EngineResourceManager}.
 * 
 * @param <T>  the type of resource
 */
public abstract class RemoteEngineResourceManager<T extends UniqueIdentifiable> implements EngineResourceManager<T> {

  private final URI _baseUri;
  private final ScheduledExecutorService _scheduler;
  private final FudgeRestClient _client; 
  
  public RemoteEngineResourceManager(URI baseUri, ScheduledExecutorService scheduler) {
    _baseUri = baseUri;
    _scheduler = scheduler;
    _client = FudgeRestClient.create();
  }
  
  @Override
  public EngineResourceReference<T> createReference(UniqueId cycleId) {
    ClientResponse response = _client.accessFudge(_baseUri).post(ClientResponse.class);
    URI baseUri = response.getLocation();
    return getRemoteReference(baseUri, _scheduler);
  }
    
  protected abstract EngineResourceReference<T> getRemoteReference(URI baseUri, ScheduledExecutorService scheduler);
  
}
