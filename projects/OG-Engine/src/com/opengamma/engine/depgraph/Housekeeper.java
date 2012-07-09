/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Housekeeper thread/service for the dependency graph builders.
 */
public final class Housekeeper {

  private static final ScheduledThreadPoolExecutor s_executor = new ScheduledThreadPoolExecutor(1);
  private static final Logger s_logger = LoggerFactory.getLogger(Housekeeper.class);

  /**
   * Callback for receiving housekeeping notifications.
   * <p>
   * Note that the data object that is registered with the housekeeper must not have a strong reference to the dependency graph builder. The data will be held from the timer thread which can prevent
   * garbage collection of the graph builder.
   */
  public interface Callback<D> {

    boolean tick(DependencyGraphBuilder builder, D data);

    boolean cancelled(DependencyGraphBuilder builder, D data);

    boolean completed(DependencyGraphBuilder builder, D data);

  }

  private final WeakReference<DependencyGraphBuilder> _builder;
  private final Callback<Object> _callback;
  private final Object _data;
  private int _startCount;
  private volatile ScheduledFuture<?> _cancel;

  @SuppressWarnings("unchecked")
  private <D> Housekeeper(final DependencyGraphBuilder builder, final Callback<D> callback, final D data) {
    s_logger.debug("Created housekeeper {} for {}", callback, builder);
    _builder = new WeakReference<DependencyGraphBuilder>(builder);
    _callback = (Callback<Object>) callback;
    _data = data;
  }

  public static <D> Housekeeper of(final DependencyGraphBuilder builder, final Callback<D> callback, final D data) {
    return new Housekeeper(builder, callback, data);
  }

  public static Housekeeper of(final DependencyGraphBuilder builder, final Callback<Void> callback) {
    return new Housekeeper(builder, callback, null);
  }

  public synchronized void start() {
    if (_startCount++ == 0) {
      s_logger.info("Starting housekeeper {} for {}", getCallback(), _builder);
      _cancel = s_executor.scheduleWithFixedDelay(new Runnable() {
        @Override
        public void run() {
          try {
            if (housekeep()) {
              return;
            } else {
              s_logger.info("Housekeeper {} for {} returned false", getCallback(), _builder);
            }
          } catch (Throwable t) {
            s_logger.error("Cancelling errored {} for {}", getCallback(), _builder);
            s_logger.warn("Caught exception", t);
          }
          cancel();
        }
      }, 1, 1, TimeUnit.SECONDS);
    }
  }

  public synchronized void stop() {
    if (_startCount > 0) {
      if (--_startCount == 0) {
        cancel();
      }
    }
  }

  private DependencyGraphBuilder getBuilder() {
    return _builder.get();
  }

  private Callback<Object> getCallback() {
    return _callback;
  }

  private Object getData() {
    return _data;
  }

  private synchronized void cancel() {
    s_logger.info("Stopping housekeeper {} for {}", getCallback(), _builder);
    if (_cancel != null) {
      _cancel.cancel(false);
      _cancel = null;
    }
    _startCount = 0;
  }

  private boolean housekeep() {
    final DependencyGraphBuilder builder = getBuilder();
    if (builder != null) {
      s_logger.debug("Tick {} for {}", getCallback(), builder);
      if (builder.isGraphBuilt()) {
        if (builder.getScheduledSteps() > 0) {
          return getCallback().completed(builder, getData());
        } else {
          // Hasn't started yet -- issue as a normal tick
          return getCallback().tick(builder, getData());
        }
      } else if (builder.isCancelled()) {
        return getCallback().cancelled(builder, getData());
      } else {
        return getCallback().tick(builder, getData());
      }
    } else {
      s_logger.info("Dependency graph builder discarded, releasing callback");
      return false;
    }
  }

}
