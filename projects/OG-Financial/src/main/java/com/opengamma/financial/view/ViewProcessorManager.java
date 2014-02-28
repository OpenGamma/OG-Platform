/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.view.impl.ViewProcessorInternal;
import com.opengamma.financial.timeseries.HistoricalTimeSeriesSourceChangeProvider;
import com.opengamma.id.ObjectId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;

/**
 * Manages a set of view processors that share a function repository. When function configuration changes (or the objects the functions are based on change) all active view processes are paused so
 * that the reinitialization can occur without giving inconsistent results for a running view cycle.
 * <p>
 * Note that previous behavior was capable of "latching" the sources so that the "get-latest" methods could always be used by functions. This has been removed as relying on such methods is problematic
 * for other system traits and a valid version-correction timestamp is available at all times.
 */
public class ViewProcessorManager implements Lifecycle {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessorManager.class);

  private final Set<ViewProcessorInternal> _viewProcessors = new HashSet<ViewProcessorInternal>();
  private final Set<CompiledFunctionService> _functions = new HashSet<CompiledFunctionService>();
  private final Collection<ChangeProvider> _masters = new ArrayList<ChangeProvider>();
  private final ReentrantLock _lifecycleLock = new ReentrantLock();
  private final ReentrantLock _changeLock = new ReentrantLock();
  private final ExecutorService _executor = NamedThreadPoolFactory.newCachedThreadPool("ViewProcessorManager", true);
  private final Set<ObjectId> _watchSet = new HashSet<ObjectId>();
  private final Set<WatchSetProvider> _watchSetProviders = new HashSet<WatchSetProvider>();
  private final ChangeListener _masterListener = new ChangeListener() {
    @Override
    public void entityChanged(ChangeEvent event) {
      if (_watchSet.contains(event.getObjectId())) {
        ViewProcessorManager.this.onMasterChanged(event.getObjectId());
      }
    }
  };
  private Set<ObjectId> _pendingChanges = new HashSet<ObjectId>();
  private boolean _isRunning;

  public ViewProcessorManager() {
  }

  private void assertNotRunning() {
    if (isRunning()) {
      throw new IllegalStateException("Already running");
    }
  }

  public void setViewProcessor(final ViewProcessorInternal viewProcessor) {
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    assertNotRunning();
    _viewProcessors.clear();
    _viewProcessors.add(viewProcessor);
  }

  public void setViewProcessors(final Collection<ViewProcessorInternal> viewProcessors) {
    ArgumentChecker.notNull(viewProcessors, "viewProcessors");
    assertNotRunning();
    _viewProcessors.clear();
    _viewProcessors.addAll(viewProcessors);
  }

  public Set<ViewProcessorInternal> getViewProcessors() {
    return Collections.unmodifiableSet(_viewProcessors);
  }

  /**
   * @param master the master, not null
   * @param source for Spring compatibility only - ignored
   * @deprecated Use {@link #setMaster} instead
   */
  @Deprecated
  public void setMasterAndSource(final ChangeProvider master, final Object source) {
    setMaster(master);
  }

  public void setMaster(final ChangeProvider master) {
    ArgumentChecker.notNull(master, "master");
    assertNotRunning();
    _masters.clear();
    _masters.add(master);
  }

  /**
   * @param masterToSource the masters, the values of the map are ignored
   * @deprecated Use {@link #setMasters} instead
   */
  @Deprecated
  public void setMastersAndSources(final Map<ChangeProvider, ?> masterToSource) {
    ArgumentChecker.notNull(masterToSource, "masterToSource");
    setMasters(masterToSource.keySet());
  }

  public void setMasters(final Collection<? extends ChangeProvider> masters) {
    ArgumentChecker.notNull(masters, "masters");
    assertNotRunning();
    _masters.clear();
    _masters.addAll(masters);
  }

  public void setWatchSetProviders(final Set<WatchSetProvider> watchSetProviders) {
    ArgumentChecker.notNull(watchSetProviders, "watchSetProviders");
    assertNotRunning();
    _watchSetProviders.clear();
    _watchSetProviders.addAll(watchSetProviders);
  }

  @Override
  public boolean isRunning() {
    _lifecycleLock.lock();
    try {
      return _isRunning;
    } finally {
      _lifecycleLock.unlock();
    }
  }

  @Override
  public void start() {
    _lifecycleLock.lock();
    try {
      if (!_isRunning) {
        _changeLock.lock();
        try {
          for (ChangeProvider master : _masters) {
            master.changeManager().addChangeListener(_masterListener);
          }
          for (ViewProcessorInternal viewProcessor : _viewProcessors) {
            viewProcessor.getFunctionCompilationService().getFunctionRepositoryFactory().changeManager().addChangeListener(_masterListener);
          }
        } finally {
          _changeLock.unlock();
        }
        _functions.clear();
        for (ViewProcessorInternal viewProcessor : _viewProcessors) {
          _functions.add(viewProcessor.getFunctionCompilationService());
        }
        s_logger.info("Initializing functions");
        for (CompiledFunctionService function : _functions) {
          final Set<ObjectId> watch = function.initialize();
          _watchSet.addAll(watch);
          addAlternateWatchSet(watch);
        }
        reinitializeWatchSet();
        s_logger.debug("WatchSet = {}", _watchSet);
        s_logger.info("Starting view processors");
        for (ViewProcessorInternal viewProcessor : _viewProcessors) {
          viewProcessor.start();
        }
        _isRunning = true;
      }
    } finally {
      _lifecycleLock.unlock();
    }
  }

  @Override
  public void stop() {
    _lifecycleLock.lock();
    try {
      if (_isRunning) {
        for (ViewProcessorInternal viewProcessor : _viewProcessors) {
          viewProcessor.stop();
          viewProcessor.getFunctionCompilationService().getFunctionRepositoryFactory().changeManager().removeChangeListener(_masterListener);
        }
        for (ChangeProvider master : _masters) {
          master.changeManager().removeChangeListener(_masterListener);
        }
        _masters.clear();
        _isRunning = false;
      }
    } finally {
      _lifecycleLock.unlock();
    }
  }

  private void onMasterChanged(final ObjectId objectIdentifier) {
    s_logger.debug("Change from {}", objectIdentifier);
    _changeLock.lock();
    try {
      if (_pendingChanges.isEmpty()) {
        s_logger.debug("Starting reinitializer job");
        // Kick off a reinitialization; this may take some time if the view processors must wait for their views to finish calculating first
        _executor.submit(new Runnable() {
          @Override
          public void run() {
            try {
              reinitializeFunctions();
            } catch (Throwable t) {
              s_logger.error("Error reinitializing", t);
            }
          }
        });
      } else {
        s_logger.debug("Reinitializing job already active for {} other changes", _pendingChanges.size());
      }
      _pendingChanges.add(objectIdentifier);
    } finally {
      _changeLock.unlock();
    }
  }

  private void reinitializeFunctions() {
    _lifecycleLock.lock();
    s_logger.info("Begin configuration change");
    try {
      final List<Runnable> resumes = new ArrayList<Runnable>(_viewProcessors.size());
      final List<Future<Runnable>> suspends = new ArrayList<Future<Runnable>>(_viewProcessors.size());
      s_logger.debug("Suspending view processors");
      for (ViewProcessorInternal viewProcessor : _viewProcessors) {
        suspends.add(viewProcessor.suspend(_executor));
      }
      while (!suspends.isEmpty()) {
        final Future<Runnable> future = suspends.remove(suspends.size() - 1);
        try {
          resumes.add(future.get(3000, TimeUnit.MILLISECONDS));
        } catch (TimeoutException e) {
          s_logger.warn("Timeout waiting for view to suspend");
          suspends.add(future);
        } catch (Throwable t) {
          s_logger.warn("Couldn't suspend view", t);
        }
      }
      Set<ObjectId> pendingChanges;
      _changeLock.lock();
      try {
        pendingChanges = _pendingChanges;
        _pendingChanges = new HashSet<ObjectId>();
      } finally {
        _changeLock.unlock();
      }
      s_logger.trace("Pending changed = {}", pendingChanges);
      s_logger.debug("Re-initializing functions");
      _watchSet.clear();
      for (CompiledFunctionService functions : _functions) {
        try {
          final Set<ObjectId> watch = functions.reinitialize();
          _watchSet.addAll(watch);
          addAlternateWatchSet(watch);
        } catch (Throwable t) {
          s_logger.error("Error reinitializing functions", t);
        }
      }
      reinitializeWatchSet();
      s_logger.trace("WatchSet = {}", _watchSet);
      s_logger.debug("Resuming view processors");
      for (Runnable resume : resumes) {
        resume.run();
      }
      s_logger.info("Configuration change complete");
    } finally {
      _lifecycleLock.unlock();
    }
  }

  private void reinitializeWatchSet() {
    _watchSet.add(FunctionConfigurationSource.OBJECT_ID);
    _watchSet.add(HistoricalTimeSeriesSourceChangeProvider.ALL_HISTORICAL_TIME_SERIES);
  }

  private void addAlternateWatchSet(final Set<ObjectId> watchSet) {
    for (WatchSetProvider provider : _watchSetProviders) {
      final Set<ObjectId> additional = provider.getAdditionalWatchSet(watchSet);
      if (additional != null) {
        _watchSet.addAll(additional);
      }
    }
  }

}
