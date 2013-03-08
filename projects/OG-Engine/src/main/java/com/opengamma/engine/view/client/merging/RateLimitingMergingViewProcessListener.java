/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import com.opengamma.engine.resource.EngineResourceManagerInternal;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.util.ArgumentChecker;

/**
 * Merges view process results to satisfy a specified maximum downstream update rate (given in terms of a minimum
 * period between updates). This maximum rate can be adjusted on-the-fly.
 */
public class RateLimitingMergingViewProcessListener extends MergingViewProcessListener {

  private static final long MIN_PERIOD = 50;
  
  private final Timer _timer;
  private ReentrantLock _taskSetupLock = new ReentrantLock();
  private TimerTask _asyncUpdateCheckerTask;
  
  private boolean _isPaused;
  
  private AtomicLong _minimumUpdatePeriodMillis = new AtomicLong(0);
  
  /**
   * The time at which an update was last triggered.
   */
  private AtomicLong _lastUpdateTimeMillis = new AtomicLong();
  
  public RateLimitingMergingViewProcessListener(ViewResultListener underlying, EngineResourceManagerInternal<?> cycleManager, Timer timer) {
    super(underlying, cycleManager);
    ArgumentChecker.notNull(timer, "timer");
    _timer = timer;
  }
  
  public void terminate() {
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
  
  /**
   * Sets whether output from the provider is paused. While it is paused, updates are merged into a single update which
   * is released when the provider is resumed.
   * 
   * @param isPaused  <code>true</code> to indicate that output should be paused, or <code>false</code> to indicate
   *                  that output should flow normally according to the update rate.
   */
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
  private boolean drainIfRequired() {    
    
    long currentTime = System.currentTimeMillis();
    long lastUpdateTime = _lastUpdateTimeMillis.get();
    long lastResultTime = getLastUpdateTimeMillis();
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
    
    drain();
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
          drainIfRequired();
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
