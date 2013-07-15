/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.async;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.NamedThreadPoolFactory;

/**
 * Abstraction of an aysnchronous housekeeping thread/service.
 * <p>
 * Once started the housekeeper will run until the target is garbage collected or it is explicitly stopped.
 * 
 * @param <T> the target of the housekeeping action
 */
public abstract class AbstractHousekeeper<T> {

  private static final ScheduledThreadPoolExecutor s_executor = new ScheduledThreadPoolExecutor(2, new NamedThreadPoolFactory("Housekeeper"));
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractHousekeeper.class);

  private static final int PERIOD = Integer.parseInt(System.getProperty("Housekeeper.period", "3"));

  private final Reference<T> _target;
  private int _startCount;
  private ScheduledFuture<?> _cancel;

  /**
   * Constructs a new instance.
   * 
   * @param target the target to perform the action on, not null
   */
  protected AbstractHousekeeper(final T target) {
    s_logger.debug("Created housekeeper {} for {}", this, target);
    _target = new WeakReference<T>(target);
  }

  /**
   * Returns the time between housekeeping operations in seconds, the default is controlled by a system property. Only override this if there is a specific requirement to run at a particular
   * frequency, or multiple of the default.
   * 
   * @return the period in seconds, or 0 for no operations
   */
  protected int getPeriodSeconds() {
    return PERIOD;
  }

  /**
   * Begins scheduling of the housekeeper. This must be matched by an equal number of calls to {@link #stop} to cancel the scheduling.
   */
  public synchronized void start() {
    if (_startCount++ == 0) {
      s_logger.info("Starting housekeeper {} for {}", this, _target);
      final int period = getPeriodSeconds();
      if (period > 0) {
        _cancel = s_executor.scheduleWithFixedDelay(new Runnable() {
          @Override
          public void run() {
            try {
              if (housekeep()) {
                return;
              } else {
                s_logger.info("Housekeeper {} for {} returned false", this, _target);
              }
            } catch (Throwable t) {
              s_logger.error("Cancelling errored {} for {}", this, _target);
              s_logger.warn("Caught exception", t);
            }
            cancel();
          }
        }, period, period, TimeUnit.SECONDS);
      }
    }
  }

  /**
   * Decrements the count maintained by {@link #start} to cancel the scheduling when the original call is paired.
   */
  public synchronized void stop() {
    if (_startCount > 0) {
      if (--_startCount == 0) {
        cancel();
      }
    }
  }

  /**
   * Cancels the scheduling immediately, regardless of how many times {@link #start} was called.
   */
  protected synchronized void cancel() {
    s_logger.info("Stopping housekeeper {} for {}", this, _target);
    if (_cancel != null) {
      _cancel.cancel(false);
      _cancel = null;
    }
    _startCount = 0;
  }

  /**
   * Returns the target of the housekeeper, possibly null if it has already been garbage collected.
   * 
   * @return the target, possibly null
   */
  protected T getTarget() {
    return _target.get();
  }

  /**
   * Performs the housekeeping action.
   * 
   * @param target the housekeeping target, not null
   * @return true to carry on scheduling, or false to stop the scheduling
   */
  protected abstract boolean housekeep(T target);

  /**
   * Handles a scheduled tick from the underlying executor. If the target has not been garbage collected, calls the user {@link #housekeep} operation, otherwise ceases the scheduling.
   * 
   * @return true to carry on scheduling, or false to stop the scheduling
   */
  protected boolean housekeep() {
    final T target = getTarget();
    if (target != null) {
      s_logger.debug("Tick {} for {}", this, target);
      return housekeep(target);
    } else {
      s_logger.info("Target discarded, releasing callback {}", this);
      return false;
    }
  }

}
