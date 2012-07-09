/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Wraps another {@link AnalyticsView} and protects it from concurrent access.
 */
/* package */ class LockingAnalyticsView implements AnalyticsView {

  private final AnalyticsView _delegate;
  private final ReadWriteLock _lock = new ReentrantReadWriteLock();

  LockingAnalyticsView(AnalyticsView delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  @Override
  public void updateStructure(CompiledViewDefinition compiledViewDefinition) {
    try {
      _lock.writeLock().lock();
      _delegate.updateStructure(compiledViewDefinition);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public void updateResults(ViewComputationResultModel fullResult, ViewCycle viewCycle) {
    try {
      _lock.writeLock().lock();
      _delegate.updateResults(fullResult, viewCycle);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public GridStructure getGridStructure(GridType gridType) {
    try {
      _lock.readLock().lock();
      return _delegate.getGridStructure(gridType);
    } finally {
      _lock.readLock().unlock();
    }
  }

  @Override
  public void createViewport(GridType gridType, String viewportId, String dataId, ViewportSpecification viewportSpec) {
    try {
      _lock.writeLock().lock();
      _delegate.createViewport(gridType, viewportId, dataId, viewportSpec);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public void updateViewport(GridType gridType, String viewportId, ViewportSpecification viewportSpec) {
    try {
      _lock.writeLock().lock();
      _delegate.updateViewport(gridType, viewportId, viewportSpec);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public void deleteViewport(GridType gridType, String viewportId) {
    try {
      _lock.writeLock().lock();
      _delegate.deleteViewport(gridType, viewportId);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public ViewportResults getData(GridType gridType, String viewportId) {
    try {
      _lock.readLock().lock();
      return _delegate.getData(gridType, viewportId);
    } finally {
      _lock.readLock().unlock();
    }
  }

  @Override
  public void openDependencyGraph(GridType gridType, String graphId, String gridId, int row, int col) {
    try {
      _lock.writeLock().lock();
      _delegate.openDependencyGraph(gridType, graphId, gridId, row, col);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public void closeDependencyGraph(GridType gridType, String graphId) {
    try {
      _lock.writeLock().lock();
      _delegate.closeDependencyGraph(gridType, graphId);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public GridStructure getGridStructure(GridType gridType, String graphId) {
    try {
      _lock.readLock().lock();
      return _delegate.getGridStructure(gridType, graphId);
    } finally {
      _lock.readLock().unlock();
    }
  }

  @Override
  public void createViewport(GridType gridType, String graphId, String viewportId, String dataId, ViewportSpecification viewportSpec) {
    try {
      _lock.writeLock().lock();
      _delegate.createViewport(gridType, graphId, viewportId, dataId, viewportSpec);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public void updateViewport(GridType gridType, String graphId, String viewportId, ViewportSpecification viewportSpec) {
    try {
      _lock.writeLock().lock();
      _delegate.updateViewport(gridType, graphId, viewportId, viewportSpec);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public void deleteViewport(GridType gridType, String graphId, String viewportId) {
    try {
      _lock.writeLock().lock();
      _delegate.deleteViewport(gridType, graphId, viewportId);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public ViewportResults getData(GridType gridType, String graphId, String viewportId) {
    try {
      _lock.readLock().lock();
      return _delegate.getData(gridType, graphId, viewportId);
    } finally {
      _lock.readLock().unlock();
    }
  }
}
