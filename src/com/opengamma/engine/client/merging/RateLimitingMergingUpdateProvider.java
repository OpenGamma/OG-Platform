/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.client.merging;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import com.opengamma.util.ArgumentChecker;

/**
 * Merges updates to satisfy a specified maximum downstream update rate (given in terms of a minimum period between
 * updates). This maximum rate can be adjusted on-the-fly.
 * 
 * @param <T>  the type of the updates
 */
public class RateLimitingMergingUpdateProvider<T> extends MergingUpdateProvider<T> {

  private static final long MIN_PERIOD = 50;
  
  private final Timer _timer;
  private ReentrantLock _taskSetupLock = new ReentrantLock();
  private TimerTask _asyncUpdateCheckerTask;
  
  private boolean _isPaused;
  
  private AtomicLong _minimumUpdatePeriodMillis;
  
  /**
   * The time at which an update was last triggered.
   */
  private AtomicLong _lastUpdateTimeMillis = new AtomicLong();
  
  /**
   * Constructs an instance, with rate-limiting disabled initially.
   * 
   * @param merger  the merger, not null
   * @param timer  the timer with which to schedule tasks, not null
   */
  public RateLimitingMergingUpdateProvider(IncrementalMerger<T> merger, Timer timer) {
    this(merger, 0, timer);
  }
  
  /**
   * Constructs an instance.
   * 
   * @param merger  the merger, not null
   * @param minimumUpdatePeriodMillis  the minimum period which must have elapsed since the last update before an
   *                                   update is triggered, in milliseconds. If 0, updates will be passed to listeners
   *                                   immediately and synchronously (unless paused).
   */
  public RateLimitingMergingUpdateProvider(IncrementalMerger<T> merger, long minimumUpdatePeriodMillis) {
    this(merger, minimumUpdatePeriodMillis, new Timer());
  }
  
  /**
   * Constructs an instance.
   * 
   * @param merger  the merger, not null
   * @param minimumUpdatePeriodMillis  the minimum period which must have elapsed since the last update before an
   *                                   update is triggered, in milliseconds. If 0, updates will be passed to listeners
   *                                   immediately and synchronously (unless paused).
   * @param timer  the timer with which to schedule tasks, not null
   */  
  public RateLimitingMergingUpdateProvider(IncrementalMerger<T> merger, long minimumUpdatePeriodMillis, Timer timer) {
    super(merger);
    ArgumentChecker.notNull(timer, "timer");
    _timer = timer;
    _minimumUpdatePeriodMillis = new AtomicLong();
    setMinimumUpdatePeriodMillis(minimumUpdatePeriodMillis);
  }
  
  public void destroy() {
    _taskSetupLock.lock();
    try {
      cancelTimerTask();
    } finally {
      _taskSetupLock.unlock();
    }
  }
  
  //-------------------------------------------------------------------------
  public boolean isPaused() {
    return _isPaused;
  }
  
  public void setPaused(boolean isPaused) {
    _taskSetupLock.lock();
    try {
      if (_isPaused == isPaused) {
        return;
      }
      _isPaused = isPaused;
      updateConfiguration();
    } finally {
      _taskSetupLock.unlock();
    }
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the minimum period which must have elapsed since the last update before an update is triggered.
   * 
   * @return the minimum period which must have elapsed since the last update before an update is triggered, in
   *         milliseconds
   */
  public long getMinimumUpdatePeriodMillis() {
    return _minimumUpdatePeriodMillis.get();
  }
  
  /**
   * Sets the minimum period which must have elapsed since the last update before an update is triggered. The value
   * given is only a minimum, and the actual period between updates may be higher. If more frequent updates are
   * required then consider using a pass-through provider instead.
   * 
   * @param minimumUpdatePeriodMillis  the minimum period which must have elapsed since the last update before an
   *                                   update is triggered, in milliseconds. If 0, updates will be passed to listeners
   *                                   immediately and synchronously (unless paused).
   */
  public void setMinimumUpdatePeriodMillis(long minimumUpdatePeriodMillis) {
    _taskSetupLock.lock();
    try {
      if (minimumUpdatePeriodMillis <= 0) {
        // No merging
        minimumUpdatePeriodMillis = 0;
      } else {
        // Merging at no less than the minimum period
        minimumUpdatePeriodMillis = Math.max(MIN_PERIOD, minimumUpdatePeriodMillis);
      }
      _minimumUpdatePeriodMillis.set(minimumUpdatePeriodMillis);
      updateConfiguration();
    } finally {
      _taskSetupLock.unlock();
    }
  }
  
  //-------------------------------------------------------------------------
  private boolean triggerUpdateIfRequired() {    
    long currentTime = System.currentTimeMillis();
    long lastUpdateTime = _lastUpdateTimeMillis.get();
    long lastResultTime = getLastResultTimeMillis();
    if (lastResultTime < lastUpdateTime) {
      // No more results since the last output
      return false;
    }
    
    long minimumUpdatePeriodMillis = getMinimumUpdatePeriodMillis();
    if (currentTime - lastUpdateTime < minimumUpdatePeriodMillis) {
      return false;
    }
    
    if (!_lastUpdateTimeMillis.compareAndSet(lastUpdateTime, currentTime)) {
      // Another thread has got there before us
      return false;
    }
    
    triggerUpdate();
    return true;
  }
  
  private void updateConfiguration() {
    long minimumUpdatePeriodMillis = getMinimumUpdatePeriodMillis();
    cancelTimerTask();
    setPassThrough(minimumUpdatePeriodMillis == 0 && !isPaused());
    if (!isPaused() && !isPassThrough()) {
      _asyncUpdateCheckerTask = new TimerTask() {
        @Override
        public void run() {
          triggerUpdateIfRequired();
        }
      };
      _timer.schedule(_asyncUpdateCheckerTask, minimumUpdatePeriodMillis, minimumUpdatePeriodMillis);
    } 
  }

  private void cancelTimerTask() {
    if (_asyncUpdateCheckerTask != null) {
      _asyncUpdateCheckerTask.cancel();
      _asyncUpdateCheckerTask = null;
    }
  }
}
