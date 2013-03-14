/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.resource.EngineResourceManager;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;

/**
 * Mock of the view processor for testing.
 */
/* package */class MockViewProcessor implements ViewProcessor {

  private final AtomicInteger _nextId = new AtomicInteger();

  @Override
  public ViewClient createViewClient(final UserPrincipal clientUser) {
    MockViewClient mvc = new MockViewClient(UniqueId.of("Test", Integer.toString(_nextId.getAndIncrement())));
    mvc.setViewProcessor(this);
    return mvc;
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ViewClient getViewClient(final UniqueId clientId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public EngineResourceManager<? extends ViewCycle> getViewCycleManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ConfigSource getConfigSource() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ViewProcess getViewProcess(final UniqueId viewProcessId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public NamedMarketDataSpecificationRepository getNamedMarketDataSpecificationRepository() {
    throw new UnsupportedOperationException();
  }
  
}
