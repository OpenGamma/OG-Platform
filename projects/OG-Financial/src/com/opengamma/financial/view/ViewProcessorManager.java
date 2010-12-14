/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.master.AbstractMaster;
import com.opengamma.master.VersionedSource;
import com.opengamma.util.ArgumentChecker;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Manages a set of view processors that share common resources, making system configuration appear atomic to them.
 * <p>
 * If given a set of masters and the sources that expose them to the rest of the system, update notifications from
 * the masters can be used to "latch" the sources between view processor suspensions to create an atomic view.
 */
public class ViewProcessorManager implements Lifecycle {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessorManager.class);

  private final Set<ViewProcessorImpl> _viewProcessors;
  private final Map<AbstractMaster<?>, VersionedSource> _masterToSource = new HashMap<AbstractMaster<?>, VersionedSource>();
  private Map<VersionedSource, Instant> _latchInstants = new HashMap<VersionedSource, Instant>();
  private final ReentrantLock _lifecycleLock = new ReentrantLock();
  private final ReentrantLock _changeLock = new ReentrantLock();
  private boolean _isRunning;

  public ViewProcessorManager() {
    _viewProcessors = new HashSet<ViewProcessorImpl>();
  }

  private void assertNotRunning() {
    if (isRunning()) {
      throw new IllegalStateException("Already running");
    }
  }

  public void setViewProcessor(final ViewProcessorImpl viewProcessor) {
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    assertNotRunning();
    _viewProcessors.clear();
    _viewProcessors.add(viewProcessor);
  }

  public void setViewProcessors(final Collection<ViewProcessorImpl> viewProcessors) {
    ArgumentChecker.notNull(viewProcessors, "viewProcessors");
    assertNotRunning();
    _viewProcessors.clear();
    _viewProcessors.addAll(viewProcessors);
  }

  @SuppressWarnings("unchecked")
  public Set<ViewProcessorImpl> getViewProcessors() {
    return Collections.unmodifiableSet(_viewProcessors);
  }

  public void setMasterAndSource(final AbstractMaster<?> master, final VersionedSource source) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(source, "source");
    assertNotRunning();
    _masterToSource.clear();
    _masterToSource.put(master, source);
  }

  public void setMastersAndSources(final Map<AbstractMaster<?>, VersionedSource> masterToSource) {
    ArgumentChecker.notNull(masterToSource, "masterToSource");
    assertNotRunning();
    _masterToSource.clear();
    _masterToSource.putAll(masterToSource);
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
        for (ViewProcessorImpl viewProcessor : _viewProcessors) {
          viewProcessor.start();
        }
        _changeLock.lock();
        try {
          final Instant now = Instant.now();
          for (Map.Entry<AbstractMaster<?>, VersionedSource> entry : _masterToSource.entrySet()) {
            final AbstractMaster<?> master = entry.getKey();
            final VersionedSource source = entry.getValue();
            s_logger.error("TODO: register with {} as a listener", master);
            s_logger.debug("Latching {} to {}", source, now);
            // TODO this isn't ideal if there is clock drift between nodes - the time needs to be the system time at the master
            source.setVersionAsOfInstant(now);
          }
        } finally {
          _changeLock.unlock();
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
        for (ViewProcessorImpl viewProcessor : _viewProcessors) {
          viewProcessor.stop();
        }
        for (AbstractMaster<?> master : _masterToSource.keySet()) {
          s_logger.error("TODO: unregister with {} as a listener", master);
        }
        _isRunning = false;
      }
    } finally {
      _lifecycleLock.unlock();
    }
  }

  protected void onMasterChanged(final InstantProvider latchInstantProvider, final AbstractMaster<?> master) {
    final Instant latchInstant = Instant.of(latchInstantProvider);
    final VersionedSource source = _masterToSource.get(master);
    if (source == null) {
      s_logger.error("Master {} not registered", master);
      throw new IllegalArgumentException();
    }
    s_logger.debug("Change timestamp {}", latchInstant);
    _changeLock.lock();
    try {
      if (_latchInstants.isEmpty()) {
        // Kick off a latch; this may take some time if the view processors must wait for their views to finish calculating first
        _viewProcessors.iterator().next().getFunctionCompilationService().getExecutorService().submit(new Runnable() {
          @Override
          public void run() {
            latchSources();
          }
        });
      }
      final Instant previousInstant = _latchInstants.get(source);
      if ((previousInstant == null) || previousInstant.isBefore(latchInstant)) {
        _latchInstants.put(source, latchInstant);
      }
    } finally {
      _changeLock.unlock();
    }
  }

  private void latchSources() {
    _lifecycleLock.lock();
    s_logger.info("Begin configuration change");
    try {
      final List<Runnable> resumes = new ArrayList<Runnable>(_viewProcessors.size());
      final List<Future<Runnable>> suspends = new ArrayList<Future<Runnable>>(_viewProcessors.size());
      s_logger.debug("Suspending view processors");
      for (ViewProcessorImpl viewProcessor : _viewProcessors) {
        suspends.add(viewProcessor.suspend());
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
      Map<VersionedSource, Instant> latchInstants;
      _changeLock.lock();
      try {
        latchInstants = _latchInstants;
        _latchInstants = new HashMap<VersionedSource, Instant>();
      } finally {
        _changeLock.unlock();
      }
      for (Map.Entry<VersionedSource, Instant> entry : latchInstants.entrySet()) {
        s_logger.debug("Latching {} to {}", entry.getKey(), entry.getValue());
        entry.getKey().setVersionAsOfInstant(entry.getValue());
      }
      s_logger.debug("Resuming view processors");
      for (Runnable resume : resumes) {
        resume.run();
      }
      s_logger.info("Configuration change complete");
    } finally {
      _lifecycleLock.unlock();
    }
  }

}
