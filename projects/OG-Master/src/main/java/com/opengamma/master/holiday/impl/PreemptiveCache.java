/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;

import com.google.common.collect.Lists;
import com.opengamma.util.async.AbstractHousekeeper;

/**
 * PLAT-1015: A cache which refreshes values preemptively, to try and avoid _any_ blocking of the get threads Currently only checked for low cardinality.
 * 
 * @param <TKey> the key type
 * @param <TValue> the value type
 */
public abstract class PreemptiveCache<TKey, TValue> {
  private static final Logger s_logger = LoggerFactory.getLogger(PreemptiveCache.class);
  private static final double TIMEOUTS_BETWEEN_EVICTION = 20.0;

  /*
   * NOTE: this is only attempted
   */
  private final Duration _timeout;
  private final long _timeoutMillis;
  private final Housekeeper _housekeeper;

  private static final class Housekeeper extends AbstractHousekeeper<PreemptiveCache<?, ?>> {

    private final Random _r = new Random();

    public Housekeeper(final PreemptiveCache<?, ?> target) {
      super(target);
    }

    private <K, V> void evictElements(final Map<K, V> cache) {
      double expectedItemsToEvict = cache.size() / (2.0 * TIMEOUTS_BETWEEN_EVICTION); // 2.0 from frequency of timer
      double cutoff = _r.nextDouble();
      if (cutoff < expectedItemsToEvict) {
        if (expectedItemsToEvict > 1.0) {
          s_logger.warn("In high cardinality cases eviction is broken, and refresh is suboptimal {}", cache.size());
        }
        expireAKey(cache);
      }
    }

    private <K, V> void expireAKey(final Map<K, V> cache) {
      ArrayList<K> keys = Lists.newArrayList(cache.keySet());
      if (keys.size() == 0) {
        return;
      }
      int ki = _r.nextInt(keys.size());
      K k = keys.remove(ki);
      s_logger.debug("Evicted key {}", k);
      cache.remove(k);
    }

    @Override
    protected int getPeriodSeconds() {
      final PreemptiveCache<?, ?> cache = getTarget();
      if (cache != null) {
        return (int) cache._timeout.getSeconds();
      } else {
        return -1;
      }
    }

    @Override
    protected boolean housekeep(final PreemptiveCache<?, ?> target) {
      evictElements(target._cache);
      target.refreshCache();
      return true;
    }

  }

  public PreemptiveCache(final Duration timeout) {
    _timeout = timeout;
    _timeoutMillis = _timeout.toMillis();
    //TODO: As cardinality goes up may want to spread the reloads. See also expiry
    _housekeeper = new Housekeeper(this);
    _housekeeper.start();
  }

  private void refreshCache() {
    for (java.util.Map.Entry<TKey, Entry> entry : _cache.entrySet()) {
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
        s_logger.warn("Blocking get to reload {} previous {}, {} entries in cache", new Object[] {key, entry, _cache.size() }); //Shouldn't happen [PLAT-1718]
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
    _housekeeper.stop();
  }
}
