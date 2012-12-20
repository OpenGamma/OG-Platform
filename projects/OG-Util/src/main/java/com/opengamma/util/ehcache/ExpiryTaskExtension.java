/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.ehcache;

import java.util.Timer;
import java.util.TimerTask;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.extension.CacheExtension;

import com.opengamma.util.ArgumentChecker;

/**
 * A simple Ehcache {@link CacheExtension} which forces eviction of expired elements by calling
 * {@link Ehcache#evictExpiredElements()} periodically, using a timer. This can be necessary since Ehcache does not
 * have an expiry/eviction thread for its MemoryStore. The task will run every period whether or not there are elements
 * in the cache.
 */
public class ExpiryTaskExtension implements CacheExtension {

  private final Ehcache _cache;
  private final Timer _timer;
  private final long _expiryCheckPeriodMillis;
  private ExpiryTask _expiryTask;
  private Status _status;
  
  private class ExpiryTask extends TimerTask {

    @Override
    public void run() {
      _cache.evictExpiredElements();
    }
    
  }
  
  /**
   * Constructs a new {@link ExpiryTaskExtension} for the specified cache.
   * 
   * @param cache  the cache on which to evict expired elements
   * @param timer  the timer to use internally for scheduling the task
   * @param expiryCheckPeriodMillis  the period of the eviction task
   */
  public ExpiryTaskExtension(Ehcache cache, Timer timer, long expiryCheckPeriodMillis) {
    ArgumentChecker.notNull(cache, "cache");
    ArgumentChecker.notNull(timer, "timer");
    
    _cache = cache;
    _timer = timer;
    _expiryCheckPeriodMillis = expiryCheckPeriodMillis;
    _status = Status.STATUS_UNINITIALISED;
  }
  
  @Override
  public void init() {
    if (_expiryTask == null) {
      _expiryTask = new ExpiryTask();
      _timer.scheduleAtFixedRate(_expiryTask, _expiryCheckPeriodMillis, _expiryCheckPeriodMillis);
    }
    _status = Status.STATUS_ALIVE;
  }
  
  @Override
  public CacheExtension clone(Ehcache cache) throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  @Override
  public void dispose() throws CacheException {
    if (_expiryTask != null) {
      _expiryTask.cancel();
      _expiryTask = null;
    }
    _status = Status.STATUS_SHUTDOWN;
  }

  @Override
  public Status getStatus() {
    return _status;
  }

}
