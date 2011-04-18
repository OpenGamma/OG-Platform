/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;

/**
 * Remote implementation of {@link EngineResourceManager<ViewCycle>}
 */
public class RemoteViewCycleManager extends RemoteEngineResourceManager<ViewCycle> {

  public RemoteViewCycleManager(URI baseUri, ScheduledExecutorService scheduler) {
    super(baseUri, scheduler);
  }

  @Override
  protected EngineResourceReference<ViewCycle> getRemoteReference(URI baseUri, ScheduledExecutorService scheduler) {
    return new RemoteViewCycleReference(baseUri, scheduler);
  }

}
