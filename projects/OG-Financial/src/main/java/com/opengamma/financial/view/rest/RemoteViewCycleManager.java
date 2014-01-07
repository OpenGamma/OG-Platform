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
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Remote implementation of {@link EngineResourceManager} on {@link ViewCycle}.
 */
public class RemoteViewCycleManager extends RemoteEngineResourceManager<ViewCycle> {

  public RemoteViewCycleManager(URI baseUri, ScheduledExecutorService scheduler) {
    super(baseUri, scheduler);
  }

  public RemoteViewCycleManager(URI baseUri, ScheduledExecutorService scheduler, FudgeRestClient client) {
    super(baseUri, scheduler, client);
  }

  @Override
  protected EngineResourceReference<ViewCycle> getRemoteReference(URI baseUri, ScheduledExecutorService scheduler) {
    return new RemoteViewCycleReference(baseUri, scheduler, getClient());
  }

}
