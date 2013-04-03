/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.Set;

import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientState;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.tuple.Pair;

/**
 * Mock of the ViewClient for testing.
 */
/* package */class MockViewClient implements ViewClient {

  private ViewProcessor _viewProcessor;
  private final UniqueId _identifier;
  private UniqueId _attachedViewDefinitionId;
  private boolean _shutdown;
  private MarketDataInjector _marketDataInjector;
  private ViewResultListener _resultListener;

  public MockViewClient(final UniqueId identifier) {
    _identifier = identifier;
  }

  public UniqueId getAttachedViewDefinitionId() {
    return _attachedViewDefinitionId;
  }

  public boolean isShutdown() {
    return _shutdown;
  }

  @Override
  public void attachToViewProcess(final UniqueId definitionId, final ViewExecutionOptions executionOptions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void attachToViewProcess(final UniqueId definitionId, final ViewExecutionOptions executionOptions, final boolean newPrivateProcess) {
    _attachedViewDefinitionId = definitionId;
  }

  @Override
  public void attachToViewProcess(final UniqueId processId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public EngineResourceReference<? extends ViewCycle> createCycleReference(final UniqueId cycleId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public EngineResourceReference<? extends ViewCycle> createLatestCycleReference() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void detachFromViewProcess() {
    throw new UnsupportedOperationException();
  }

  @Override
  public CompiledViewDefinition getLatestCompiledViewDefinition() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ViewComputationResultModel getLatestResult() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ViewDefinition getLatestViewDefinition() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MarketDataInjector getLiveDataOverrideInjector() {
    if (_marketDataInjector != null) {
      return _marketDataInjector;
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void setLiveDataOverrideInjector(final MarketDataInjector marketDataInjector) {
    _marketDataInjector = marketDataInjector;
  }

  @Override
  public ViewResultMode getResultMode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ViewResultMode getFragmentResultMode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setFragmentResultMode(ViewResultMode fragmentResultMode) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ViewClientState getState() {
    throw new UnsupportedOperationException();
  }

  @Override
  public UniqueId getUniqueId() {
    return _identifier;
  }

  @Override
  public UserPrincipal getUser() {
    throw new UnsupportedOperationException();
  }

  public void setViewProcessor(ViewProcessor vp) {
    _viewProcessor = vp;
  }

  @Override
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  @Override
  public boolean isAttached() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isCompleted() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isResultAvailable() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isViewCycleAccessSupported() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void pause() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void resume() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setResultListener(final ViewResultListener resultListener) {
    _resultListener = resultListener;
  }

  public ViewResultListener getResultListener() {
    return _resultListener;
  }

  @Override
  public void setResultMode(final ViewResultMode viewResultMode) {
    assertEquals(viewResultMode, ViewResultMode.DELTA_ONLY);
  }

  @Override
  public void setUpdatePeriod(final long periodMillis) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setViewCycleAccessSupported(final boolean isViewCycleAccessSupported) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void setMinimumLogMode(ExecutionLogMode minimumLogMode, Set<Pair<String, ValueSpecification>> targets) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void shutdown() {
    assertFalse(_shutdown);
    _shutdown = true;
  }

  @Override
  public void triggerCycle() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void waitForCompletion() throws InterruptedException {
    throw new UnsupportedOperationException();
  }

}
