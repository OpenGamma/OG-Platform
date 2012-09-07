/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Wraps another {@link AnalyticsView} and protects it from concurrent access. The methods that can mutate the state of
 * the underlying view are locked with a write lock, the getters are locked with a read lock.
 * @see com.opengamma.web.server.push.analytics Package concurrency notes
 */
/* package */ class LockingAnalyticsView implements AnalyticsView {

  private final AnalyticsView _delegate;
  private final ReadWriteLock _lock = new ReentrantReadWriteLock();

  /**
   * @param delegate The delegate view (presumably not a thread safe implementation)
   */
  /* package */ LockingAnalyticsView(AnalyticsView delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  @Override
  public List<String> updateStructure(CompiledViewDefinition compiledViewDefinition) {
    try {
      _lock.writeLock().lock();
      return _delegate.updateStructure(compiledViewDefinition);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public List<String> updateResults(ViewResultModel results, ViewCycle viewCycle) {
    try {
      _lock.writeLock().lock();
      return _delegate.updateResults(results, viewCycle);
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
  public Pair<Long, String> createViewport(GridType gridType, int viewportId, String callbackId, ViewportSpecification viewportSpec) {
    try {
      _lock.writeLock().lock();
      return _delegate.createViewport(gridType, viewportId, callbackId, viewportSpec);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public Pair<Long, String> updateViewport(GridType gridType, int viewportId, ViewportSpecification viewportSpec) {
    try {
      _lock.writeLock().lock();
      return _delegate.updateViewport(gridType, viewportId, viewportSpec);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public void deleteViewport(GridType gridType, int viewportId) {
    try {
      _lock.writeLock().lock();
      _delegate.deleteViewport(gridType, viewportId);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public ViewportResults getData(GridType gridType, int viewportId) {
    try {
      _lock.readLock().lock();
      return _delegate.getData(gridType, viewportId);
    } finally {
      _lock.readLock().unlock();
    }
  }

  @Override
  public String openDependencyGraph(GridType gridType, int graphId, String callbackId, int row, int col) {
    try {
      _lock.writeLock().lock();
      return _delegate.openDependencyGraph(gridType, graphId, callbackId, row, col);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public void closeDependencyGraph(GridType gridType, int graphId) {
    try {
      _lock.writeLock().lock();
      _delegate.closeDependencyGraph(gridType, graphId);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public GridStructure getGridStructure(GridType gridType, int graphId) {
    try {
      _lock.readLock().lock();
      return _delegate.getGridStructure(gridType, graphId);
    } finally {
      _lock.readLock().unlock();
    }
  }

  @Override
  public Pair<Long, String> createViewport(GridType gridType, int graphId, int viewportId, String callbackId, ViewportSpecification viewportSpec) {
    try {
      _lock.writeLock().lock();
      return _delegate.createViewport(gridType, graphId, viewportId, callbackId, viewportSpec);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public Pair<Long, String> updateViewport(GridType gridType, int graphId, int viewportId, ViewportSpecification viewportSpec) {
    try {
      _lock.writeLock().lock();
      return _delegate.updateViewport(gridType, graphId, viewportId, viewportSpec);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public void deleteViewport(GridType gridType, int graphId, int viewportId) {
    try {
      _lock.writeLock().lock();
      _delegate.deleteViewport(gridType, graphId, viewportId);
    } finally {
      _lock.writeLock().unlock();
    }
  }

  @Override
  public ViewportResults getData(GridType gridType, int graphId, int viewportId) {
    try {
      _lock.readLock().lock();
      return _delegate.getData(gridType, graphId, viewportId);
    } finally {
      _lock.readLock().unlock();
    }
  }
}
