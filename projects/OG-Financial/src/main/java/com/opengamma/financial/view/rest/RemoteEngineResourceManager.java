/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import com.opengamma.engine.resource.EngineResourceManager;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.rest.FudgeRestClient;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Remote implementation of {@link EngineResourceManager}.
 * 
 * @param <T> the type of resource
 */
public abstract class RemoteEngineResourceManager<T extends UniqueIdentifiable> implements EngineResourceManager<T> {

  private final URI _baseUri;
  private final ScheduledExecutorService _scheduler;
  private final FudgeRestClient _client;

  public RemoteEngineResourceManager(URI baseUri, ScheduledExecutorService scheduler) {
    this(baseUri, scheduler, FudgeRestClient.create());
  }

  public RemoteEngineResourceManager(URI baseUri, ScheduledExecutorService scheduler, FudgeRestClient client) {
    _baseUri = baseUri;
    _scheduler = scheduler;
    _client = client;
  }

  protected FudgeRestClient getClient() {
    return _client;
  }

  @Override
  public EngineResourceReference<T> createReference(UniqueId cycleId) {
    ClientResponse response = getClient().accessFudge(_baseUri).post(ClientResponse.class);
    URI baseUri = response.getLocation();
    return getRemoteReference(baseUri, _scheduler);
  }

  protected abstract EngineResourceReference<T> getRemoteReference(URI baseUri, ScheduledExecutorService scheduler);

}
