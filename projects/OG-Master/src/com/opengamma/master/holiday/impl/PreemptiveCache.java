/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PLAT-1015: A cache which refreshes values preemptively, to try and avoid _any_ blocking of the get threads
 * @param <TKey> the key type
 * @param <TValue> the value type
 */
public abstract class PreemptiveCache<TKey, TValue> {
  private static final Logger s_logger = LoggerFactory.getLogger(PreemptiveCache.class);
  
  /*
   * NOTE: this is only attempted
   */
  private final Duration _timeout;
  private final long _timeoutMillis;
  private final Timer _timer;
  
  public PreemptiveCache(Duration timeout) {
    _timeout = timeout;
    _timeoutMillis =  _timeout.toMillisLong();
    _timer = new Timer("PreemptiveCache refresh " + this.getClass().getName(), true);
    _timer.schedule(new TimerTask() {
      @Override
      public void run() {
        refreshCache();
      }
    }, _timeoutMillis, _timeoutMillis / 2); //try and avoid gets ever seeing stale values.
  }
  
  private void refreshCache() {
    for (java.util.Map.Entry<TKey, Entry> entry : _cache.entrySet()) {
      //TODO: expire some keys
      loadKey(entry.getKey(), entry.getValue(), false);
    }
  }
  
  
  private class Entry {
    final long expiry; // CSIGNORE
    final TValue value; // CSIGNORE
    
    public Entry(TValue value) {
      this(System.currentTimeMillis() + _timeoutMillis, value);
    }
    
    public Entry(long expiry, TValue value) {
      super();
      this.expiry = expiry;
      this.value = value;
    }
  }
  
  private ConcurrentHashMap<TKey, Entry> _cache = new ConcurrentHashMap<TKey, Entry>();
  
  public TValue get(TKey key) {
    Entry entry = _cache.get(key);
    if (entry == null || !isValid(entry)) {
      if (entry != null) {
        s_logger.warn("Blocking get to reload {} previous {}", key, entry); //Shouldn't happen
      }
      entry = loadKey(key, entry);
    }
    return entry.value;
  }

  private Object _freshLoadKey = new Object();
  
  private Entry loadKey(TKey key, Entry previous) {
    return loadKey(key, previous, true);
  }

  private Entry loadKey(TKey key, Entry previous, boolean skipIfValid) {
    Object lock = previous == null ? _freshLoadKey : previous;
    synchronized (lock) { //TODO: Stop a storm of request on the underlying by locking on previous?
      if (skipIfValid) {
        //check for concurrent update and the work
        Entry newOldEntry = _cache.get(key);
        if (newOldEntry != null && isValid(newOldEntry)) {
          return newOldEntry;
        }
      }
      s_logger.debug("Loading key {}", key);
      return loadKeyImpl(key);
    }
  }

  private Entry loadKeyImpl(TKey key) {
    TValue valueImpl = getValueImpl(key);
    Entry entry = new Entry(valueImpl);
    _cache.put(key, entry);
    return entry;
  }
  
  protected abstract TValue getValueImpl(TKey key);

  private boolean isValid(Entry entry) {
    //NOTE: when this returns false we can have several thread hitting the database at once, which is a bit stupid
    long expiry = entry.expiry;
    long current = System.currentTimeMillis();
    boolean valid = expiry >= current;
    if (!valid) {
      s_logger.debug("Expired element current {} expiry {}", current, expiry);
    }
    return valid;
  }
  
  
  public void stop() {
    _timer.cancel();
  }
}
