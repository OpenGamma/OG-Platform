/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Watchdog for dealing with job items that run for too long. Detected at this level, recovery actions are limited.
 */
public class MaximumJobItemExecutionWatchdog {

  private static final Logger s_logger = LoggerFactory.getLogger(MaximumJobItemExecutionWatchdog.class);

  /**
   * Callback for the action to take when the watchdog is triggered.
   */
  public interface Action {

    /**
     * The time limit for job item execution has been exceeded.
     * 
     * @param jobItem the job item involved
     * @param thread the thread that is running the job item
     */
    void jobItemExecutionLimitExceeded(CalculationJobItem jobItem, Thread thread);

  }

  private static final class ThreadInfo {

    private long _startTime;
    private CalculationJobItem _jobItem;
    private int _fault;

    public ThreadInfo(final CalculationJobItem jobItem) {
      setJobItem(jobItem);
    }

    public long getElapsed(final long timeNow) {
      return timeNow - _startTime;
    }

    public CalculationJobItem getJobItem() {
      return _jobItem;
    }

    public void setJobItem(final CalculationJobItem jobItem) {
      _startTime = System.nanoTime();
      _jobItem = jobItem;
      _fault = 0;
    }

    public int getFault() {
      return _fault;
    }

    public void incrementFault() {
      _fault++;
    }

  }

  private final ConcurrentMap<Thread, ThreadInfo> _state = new ConcurrentHashMap<Thread, ThreadInfo>();
  private long _maxExecutionTime;
  private Action _action = new Action() {
    @Override
    public void jobItemExecutionLimitExceeded(final CalculationJobItem jobItem, final Thread thread) {
      s_logger.error("Job item execution limit exceeded on {} by {}", jobItem, thread);
      thread.interrupt();
    }
  };
  private ScheduledExecutorService _scheduler;
  private volatile Future<?> _task;

  public void setMaxJobItemExecutionTime(final long milliseconds) {
    _maxExecutionTime = milliseconds * 1000000L;
  }

  public long getMaxJobItemExecutionTime() {
    return _maxExecutionTime / 1000000L;
  }

  public void setTimeoutAction(final Action action) {
    ArgumentChecker.notNull(action, "action");
    _action = action;
  }

  public Action getTimeoutAction() {
    return _action;
  }

  public void setScheduler(final ScheduledExecutorService scheduler) {
    _scheduler = scheduler;
  }

  public ScheduledExecutorService getScheduler() {
    return _scheduler;
  }

  private final class CheckThreads implements Runnable {

    @Override
    public void run() {
      final long time = System.nanoTime();
      final long limit = _maxExecutionTime;
      final Iterator<Map.Entry<Thread, ThreadInfo>> itr = _state.entrySet().iterator();
      while (itr.hasNext()) {
        final Map.Entry<Thread, ThreadInfo> thread = itr.next();
        if (thread.getKey().isAlive()) {
          if (thread.getValue().getJobItem() == null) {
            s_logger.debug("Thread {} alive but not executing any job items", thread.getKey());
          } else {
            final long elapsed = thread.getValue().getElapsed(time);
            if (elapsed > limit) {
              s_logger.warn("Thread {} has been executing {} for {}ms", new Object[] {thread.getKey(), thread.getValue().getJobItem(), (double) elapsed / 1e6 });
              thread.getValue().incrementFault();
              getTimeoutAction().jobItemExecutionLimitExceeded(thread.getValue().getJobItem(), thread.getKey());
            } else {
              s_logger.debug("Thread {} within job limit", thread.getKey());
            }
          }
        } else {
          s_logger.info("Removed terminated thread {} from watchlist", thread.getKey());
          itr.remove();
        }
      }
      synchronized (this) {
        if (_state.isEmpty()) {
          _task.cancel(false);
          _task = null;
        }
      }
    }

  }

  /**
   * The calling thread is about to start executing the job item. This call must be paired with a call to {@link #jobExecutionStopped} when the thread has finished, before the time limit elapses, to
   * avoid the watchdog triggering.
   * 
   * @param jobItem the item
   */
  protected void jobExecutionStarted(final CalculationJobItem jobItem) {
    if (getMaxJobItemExecutionTime() > 0) {
      final Thread t = Thread.currentThread();
      ThreadInfo info = _state.get(t);
      if (info == null) {
        info = new ThreadInfo(jobItem);
        _state.put(t, info);
        if (_task == null) {
          synchronized (this) {
            if (_task == null) {
              if (getScheduler() == null) {
                setScheduler(Executors.newSingleThreadScheduledExecutor());
              }
              _task = getScheduler().scheduleWithFixedDelay(new CheckThreads(), getMaxJobItemExecutionTime(), getMaxJobItemExecutionTime(), TimeUnit.MILLISECONDS);
            }
          }
        }
      } else {
        info.setJobItem(jobItem);
      }
    }
  }

  /**
   * The calling thread has finished executing the job item from the previous call to {@link #jobExecutionStarted}.
   */
  protected void jobExecutionStopped() {
    if (getMaxJobItemExecutionTime() > 0) {
      ThreadInfo info = _state.get(Thread.currentThread());
      if (info != null) {
        info.setJobItem(null);
      }
    }
  }

  public boolean areThreadsAlive() {
    for (Map.Entry<Thread, ThreadInfo> thread : _state.entrySet()) {
      if ((thread.getValue().getFault() == 0) && thread.getKey().isAlive()) {
        return true;
      }
    }
    return false;
  }

}
