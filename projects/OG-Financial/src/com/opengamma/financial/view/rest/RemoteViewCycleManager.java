/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import com.opengamma.engine.view.calc.ViewCycleManager;
import com.opengamma.engine.view.calc.ViewCycleReference;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.rest.FudgeRestClient;

import com.sun.jersey.api.client.ClientResponse;

/**
 * Remote implementation of {@link ViewCycleManager}.
 */
public class RemoteViewCycleManager implements ViewCycleManager {

  private final URI _baseUri;
  private final ScheduledExecutorService _scheduler;
  private final FudgeRestClient _client; 
  
  public RemoteViewCycleManager(URI baseUri, ScheduledExecutorService scheduler) {
    _baseUri = baseUri;
    _scheduler = scheduler;
    _client = FudgeRestClient.create();
  }
  
  @Override
  public ViewCycleReference createReference(UniqueIdentifier cycleId) {
    ClientResponse response = _client.access(_baseUri).post(ClientResponse.class);
    return new RemoteViewCycleReference(response.getLocation(), _scheduler);
  }
    
}
