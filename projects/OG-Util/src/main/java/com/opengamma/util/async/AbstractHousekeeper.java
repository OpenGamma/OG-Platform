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

  protected AbstractHousekeeper(final T target) {
    s_logger.debug("Created housekeeper {} for {}", this, target);
    _target = new WeakReference<T>(target);
  }

  public synchronized void start() {
    if (_startCount++ == 0) {
      s_logger.info("Starting housekeeper {} for {}", this, _target);
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
      }, PERIOD, PERIOD, TimeUnit.SECONDS);
    }
  }

  public synchronized void stop() {
    if (_startCount > 0) {
      if (--_startCount == 0) {
        cancel();
      }
    }
  }

  protected synchronized void cancel() {
    s_logger.info("Stopping housekeeper {} for {}", this, _target);
    if (_cancel != null) {
      _cancel.cancel(false);
      _cancel = null;
    }
    _startCount = 0;
  }

  protected abstract boolean housekeep(T target);

  protected boolean housekeep() {
    final T target = _target.get();
    if (target != null) {
      s_logger.debug("Tick {} for {}", this, target);
      return housekeep(target);
    } else {
      s_logger.info("Target discarded, releasing callback {}", this);
      return false;
    }
  }

}
