/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.TypeFormatter;
import com.opengamma.web.analytics.push.UpdateListener;

/**
 * View implementation that delegates operations to another view, catches any exceptions and notifies the client.
 */
/* package */ class CatchingAnalyticsView implements AnalyticsView {

  private final AnalyticsView _delegate;
  private final ErrorManager _errorManager;
  private final UpdateListener _listener;

  /* package */ CatchingAnalyticsView(AnalyticsView delegate, ErrorManager errorManager, UpdateListener listener) {
    ArgumentChecker.notNull(delegate, "delegate");
    ArgumentChecker.notNull(errorManager, "errorManager");
    ArgumentChecker.notNull(listener, "listener");
    _errorManager = errorManager;
    _listener = listener;
    _delegate = delegate;
  }

  @Override
  public List<String> updateStructure(CompiledViewDefinition compiledViewDefinition, Portfolio resolvedPortfolio) {
    try {
      return _delegate.updateStructure(compiledViewDefinition, resolvedPortfolio);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public String viewCompilationFailed(Throwable ex) {
    try {
      return _delegate.viewCompilationFailed(ex);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public List<String> updateResults(ViewResultModel results, ViewCycle viewCycle) {
    try {
      return _delegate.updateResults(results, viewCycle);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public GridStructure getGridStructure(GridType gridType, int viewportId) {
    try {
      return _delegate.getGridStructure(gridType, viewportId);
    } catch (Exception e) {
      throw e;
    }
  }

  @Override
  public GridStructure getInitialGridStructure(GridType gridType) {
    try {
      return _delegate.getInitialGridStructure(gridType);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public boolean createViewport(int requestId,
                                GridType gridType,
                                int viewportId,
                                String callbackId,
                                String structureCallbackId,
                                ViewportDefinition viewportDefinition) {
    try {
      return _delegate.createViewport(requestId,
                                      gridType,
                                      viewportId,
                                      callbackId,
                                      structureCallbackId,
                                      viewportDefinition);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public String updateViewport(GridType gridType, int viewportId, ViewportDefinition viewportDefinition) {
    try {
      return _delegate.updateViewport(gridType, viewportId, viewportDefinition);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public void deleteViewport(GridType gridType, int viewportId) {
    try {
      _delegate.deleteViewport(gridType, viewportId);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public ViewportResults getData(GridType gridType, int viewportId) {
    try {
      return _delegate.getData(gridType, viewportId);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public GridStructure getGridStructure(GridType gridType, int graphId, int viewportId) {
    try {
      return _delegate.getGridStructure(gridType, graphId, viewportId);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public GridStructure getInitialGridStructure(GridType gridType, int graphId) {
    try {
      return _delegate.getInitialGridStructure(gridType, graphId);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public void openDependencyGraph(int requestId, GridType gridType, int graphId, String callbackId, int row, int col) {
    try {
      _delegate.openDependencyGraph(requestId, gridType, graphId, callbackId, row, col);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public void openDependencyGraph(int requestId,
                                  GridType gridType,
                                  int graphId,
                                  String callbackId,
                                  String calcConfigName,
                                  ValueRequirement valueRequirement) {
    try {
      _delegate.openDependencyGraph(requestId, gridType, graphId, callbackId, calcConfigName, valueRequirement);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public void closeDependencyGraph(GridType gridType, int graphId) {
    try {
      _delegate.closeDependencyGraph(gridType, graphId);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public boolean createViewport(int requestId,
                                GridType gridType,
                                int graphId,
                                int viewportId,
                                String callbackId,
                                String structureCallbackId,
                                ViewportDefinition viewportDefinition) {
    try {
      return _delegate.createViewport(requestId,
                                      gridType,
                                      graphId,
                                      viewportId,
                                      callbackId,
                                      structureCallbackId,
                                      viewportDefinition);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public String updateViewport(GridType gridType, int graphId, int viewportId, ViewportDefinition viewportDefinition) {
    try {
      return _delegate.updateViewport(gridType, graphId, viewportId, viewportDefinition);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public void deleteViewport(GridType gridType, int graphId, int viewportId) {
    try {
      _delegate.deleteViewport(gridType, graphId, viewportId);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public ViewportResults getData(GridType gridType, int graphId, int viewportId) {
    try {
      return _delegate.getData(gridType, graphId, viewportId);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public List<String> entityChanged(MasterChangeNotification<?> notification) {
    try {
      return _delegate.entityChanged(notification);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public List<String> portfolioChanged() {
    try {
      return _delegate.portfolioChanged();
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public ViewportResults getAllGridData(GridType gridType, TypeFormatter.Format format) {
    try {
      return _delegate.getAllGridData(gridType, format);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public UniqueId getViewDefinitionId() {
    try {
      return _delegate.getViewDefinitionId();
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public List<ErrorInfo> getErrors() {
    try {
      return _delegate.getErrors();
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public void deleteError(long errorId) {
    try {
      _delegate.deleteError(errorId);
    } catch (Exception e) {
      String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }
}
