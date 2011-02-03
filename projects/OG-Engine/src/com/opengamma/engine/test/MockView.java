/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantLock;

import javax.time.InstantProvider;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.DeltaComputationResultListener;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewInternal;
import com.opengamma.engine.view.ViewProcessingContext;
import com.opengamma.engine.view.calc.SingleComputationCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.ViewEvaluationModel;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;

/**
 * Mock view providing some view behavior for testing downstream components, without any engine dependencies.
 */
public class MockView implements ViewInternal {

  private final String _name;

  private final Set<ComputationResultListener> _resultListeners = new CopyOnWriteArraySet<ComputationResultListener>();
  private final Set<DeltaComputationResultListener> _deltaListeners = new CopyOnWriteArraySet<DeltaComputationResultListener>();

  private ReentrantLock _resultLock = new ReentrantLock();
  private ViewComputationResultModel _latestResult;
  private ViewProcessingContext _processingContext;
  private ViewEvaluationModel _viewEvaluationModel;

  private boolean _isRunning;

  public MockView(String name) {
    _name = name;
  }

  public void newResult(ViewComputationResultModel result, ViewDeltaResultModel delta) {
    _resultLock.lock();
    try {
      ViewComputationResultModel previousResult = _latestResult;
      _latestResult = result;
      for (ComputationResultListener resultListener : _resultListeners) {
        resultListener.computationResultAvailable(result);
      }
      if (!_deltaListeners.isEmpty() && previousResult != null && delta != null) {
        for (DeltaComputationResultListener deltaListener : _deltaListeners) {
          deltaListener.deltaResultAvailable(delta);
        }
      }
    } finally {
      _resultLock.unlock();
    }
  }

  @Override
  public String getName() {
    return _name;
  }

  public boolean addResultListener(ComputationResultListener resultListener) {
    return _resultListeners.add(resultListener);
  }

  public boolean removeResultListener(ComputationResultListener resultListener) {
    return _resultListeners.remove(resultListener);
  }

  public boolean addDeltaResultListener(DeltaComputationResultListener deltaListener) {
    return _deltaListeners.add(deltaListener);
  }

  public boolean removeDeltaResultLister(DeltaComputationResultListener deltaListener) {
    return _deltaListeners.remove(deltaListener);
  }

  @Override
  public void assertAccessToLiveDataRequirements(UserPrincipal user) {
  }

  @Override
  public Set<ValueRequirement> getRequiredLiveData() {
    return Collections.emptySet();
  }

  @Override
  public boolean isLiveComputationRunning() {
    return _isRunning;
  }

  public boolean isRunning() {
    return _isRunning;
  }

  public void setRunning(boolean isRunning) {
    _isRunning = isRunning;
  }

  @Override
  public ViewComputationResultModel getLatestResult() {
    _resultLock.lock();
    try {
      return _latestResult;
    } finally {
      _resultLock.unlock();
    }
  }

  @Override
  public void recalculationPerformed(ViewComputationResultModel result) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void runOneCycle() {
  }
  
  @Override
  public void runOneCycle(long valuationTime) {
  }

  @Override
  public void runOneCycle(long valuationTime, LiveDataSnapshotProvider snapshotProvider, ComputationResultListener listener) {
  }

  @Override
  public SingleComputationCycle createCycle() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ViewDefinition getDefinition() {
    throw new UnsupportedOperationException();
  }

  @Override
  public LiveDataInjector getLiveDataOverrideInjector() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Portfolio getPortfolio() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ViewProcessingContext getProcessingContext() {
    if (_processingContext != null) {
      return _processingContext;
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void setProcessingContext(final ViewProcessingContext processingContext) {
    _processingContext = processingContext;
  }

  @Override
  public ViewEvaluationModel getViewEvaluationModel() {
    if (_viewEvaluationModel != null) {
      return _viewEvaluationModel;
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void setViewEvaluationModel(final ViewEvaluationModel viewEvaluationModel) {
    _viewEvaluationModel = viewEvaluationModel;
  }

  @Override
  public ViewClient createClient(UserPrincipal credentials) {
    return null;
  }

  @Override
  public Set<String> getAllSecurityTypes() {
    return null;
  }

  @Override
  public ViewClient getClient(UniqueIdentifier id) {
    return null;
  }

  @Override
  public void init() {
  }

  @Override
  public ViewPermissionProvider getPermissionProvider() {
    return null;
  }

  @Override
  public void init(InstantProvider initializationInstant) {
  }

  @Override
  public void reinit() {
  }

  @Override
  public void reinit(InstantProvider initializationInstant) {
  }

  @Override
  public void suspend() {
  }

  @Override
  public void resume() {
  }

  @Override
  public LiveDataSnapshotProvider getLiveDataSnapshotProvider() {
    return null;
  }
  
}
