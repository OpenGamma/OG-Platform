/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.engine.marketdata.live.LiveMarketDataSourceRegistry;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.calc.EngineResourceManager;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
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
  public UniqueId getUniqueId() {
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
  public ViewDefinitionRepository getViewDefinitionRepository() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ViewProcess getViewProcess(final UniqueId viewProcessId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LiveMarketDataSourceRegistry getLiveMarketDataSourceRegistry() {
    throw new UnsupportedOperationException();
  }
}
