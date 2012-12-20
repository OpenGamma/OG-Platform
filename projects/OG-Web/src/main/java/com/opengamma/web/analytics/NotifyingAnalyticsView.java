/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.push.UpdateListener;

/**
 * {@link AnalyticsView} that decorates another view and notifies a listener when any items are updated in the
 * underlying view. This class allows the underlying view to be protected by a lock without having to make an
 * external call to the listener whilst holding the lock.
 * @see SimpleAnalyticsView
 */
/* package */ class NotifyingAnalyticsView implements AnalyticsView {

  private final AnalyticsView _delegate;
  private final UpdateListener _listener;

  /* package */ NotifyingAnalyticsView(AnalyticsView delegate, UpdateListener listener) {
    ArgumentChecker.notNull(delegate, "delegate");
    ArgumentChecker.notNull(listener, "listener");
    _delegate = delegate;
    _listener = listener;
  }

  @Override
  public List<String> updateStructure(CompiledViewDefinition compiledViewDefinition) {
    List<String> callbackIds = _delegate.updateStructure(compiledViewDefinition);
    _listener.itemsUpdated(callbackIds);
    return callbackIds;
  }

  @Override
  public List<String> updateResults(ViewResultModel results, ViewCycle viewCycle) {
    List<String> callbackIds = _delegate.updateResults(results, viewCycle);
    _listener.itemsUpdated(callbackIds);
    return callbackIds;
  }

  @Override
  public GridStructure getGridStructure(GridType gridType) {
    return _delegate.getGridStructure(gridType);
  }

  @Override
  public boolean createViewport(int requestId, GridType gridType, int viewportId, String callbackId, ViewportDefinition viewportDefinition) {
    boolean hasData = _delegate.createViewport(requestId, gridType, viewportId, callbackId, viewportDefinition);
    ImmutableMap<String, Object> callbackMap = ImmutableMap.<String, Object>of("id", requestId, "message", callbackId);
    if (hasData) {
      _listener.itemsUpdated(ImmutableList.of(callbackMap, callbackId));
    } else {
      _listener.itemUpdated(callbackMap);
    }
    return hasData;
  }

  @Override
  public String updateViewport(GridType gridType, int viewportId, ViewportDefinition viewportDefinition) {
    String callbackId = _delegate.updateViewport(gridType, viewportId, viewportDefinition);
    if (callbackId != null) {
      _listener.itemUpdated(callbackId);
    }
    return callbackId;
  }

  @Override
  public void deleteViewport(GridType gridType, int viewportId) {
    _delegate.deleteViewport(gridType, viewportId);
  }

  @Override
  public ViewportResults getData(GridType gridType, int viewportId) {
    return _delegate.getData(gridType, viewportId);
  }

  @Override
  public void openDependencyGraph(int requestId, GridType gridType, int graphId, String callbackId, int row, int col) {
    _delegate.openDependencyGraph(requestId, gridType, graphId, callbackId, row, col);
    ImmutableMap<String, Object> callbackMap = ImmutableMap.<String, Object>of("id", requestId, "message", callbackId);
    _listener.itemUpdated(callbackMap);
  }

  @Override
  public void closeDependencyGraph(GridType gridType, int graphId) {
    _delegate.closeDependencyGraph(gridType, graphId);
  }

  @Override
  public GridStructure getGridStructure(GridType gridType, int graphId) {
    return _delegate.getGridStructure(gridType, graphId);
  }

  @Override
  public boolean createViewport(int requestId, GridType gridType, int graphId, int viewportId, String callbackId, ViewportDefinition viewportDefinition) {
    boolean hasData = _delegate.createViewport(requestId, gridType, graphId, viewportId, callbackId, viewportDefinition);
    ImmutableMap<String, Object> callbackMap = ImmutableMap.<String, Object>of("id", requestId, "message", callbackId);
    if (hasData) {
      _listener.itemsUpdated(ImmutableList.of(callbackMap, callbackId));
    } else {
      _listener.itemUpdated(callbackMap);
    }
    return hasData;
  }

  @Override
  public String updateViewport(GridType gridType, int graphId, int viewportId, ViewportDefinition viewportDefinition) {
    String callbackId = _delegate.updateViewport(gridType, graphId, viewportId, viewportDefinition);
    if (callbackId != null) {
      _listener.itemUpdated(callbackId);
    }
    return callbackId;
  }

  @Override
  public void deleteViewport(GridType gridType, int graphId, int viewportId) {
    _delegate.deleteViewport(gridType, graphId, viewportId);
  }

  @Override
  public ViewportResults getData(GridType gridType, int graphId, int viewportId) {
    return _delegate.getData(gridType, graphId, viewportId);
  }
}
