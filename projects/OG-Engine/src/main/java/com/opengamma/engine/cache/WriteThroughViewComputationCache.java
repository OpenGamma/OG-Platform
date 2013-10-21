/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A wrapper around an existing {@link ViewComputationCache} implementation that will attempt to buffer data in memory to speed up writes rapidly followed by a read.
 */
public class WriteThroughViewComputationCache implements ViewComputationCache {

  private static final Logger s_logger = LoggerFactory.getLogger(WriteThroughViewComputationCache.class);

  private static final Object NULL = new Object();

  private static final Object PENDING = new Object();

  private static final ConcurrentMap<ViewComputationCache, WriteThroughViewComputationCache> s_instances = new MapMaker().weakKeys().weakValues().makeMap();

  /* package */static final class Pending {

    private final ValueSpecification _specification;
    private Object _value;

    public Pending(final ValueSpecification specification) {
      _specification = specification;
    }

    public synchronized Object waitFor() {
      try {
        s_logger.debug("Waiting for {}", _specification);
        while (_value == null) {
          wait();
        }
        return _value;
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("Interrupted", e);
      }
    }

    public Pair<ValueSpecification, Object> waitForPair() {
      final Object value = waitFor();
      if (value == NULL) {
        return Pairs.of(_specification, null);
      } else {
        return Pairs.of(_specification, value);
      }
    }

    public synchronized void post(final Object value) {
      s_logger.debug("Posting result for {}", _specification);
      _value = value;
      notifyAll();
    }

  }

  private final ViewComputationCache _underlying;

  private final ConcurrentMap<ValueSpecification, Object> _readCache = new MapMaker().softValues().makeMap();

  private final ConcurrentMap<ValueSpecification, Pending> _pending = new MapMaker().weakValues().makeMap();

  protected WriteThroughViewComputationCache(final ViewComputationCache underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  public static WriteThroughViewComputationCache of(final ViewComputationCache underlying) {
    WriteThroughViewComputationCache cached = s_instances.get(underlying);
    if (cached != null) {
      return cached;
    }
    cached = new WriteThroughViewComputationCache(underlying);
    final WriteThroughViewComputationCache existing = s_instances.putIfAbsent(underlying, cached);
    if (existing == null) {
      return cached;
    } else {
      return existing;
    }
  }

  /**
   * This method will clear all instances of this class. It should be called after confirming with OpenGamma support that it is necessary to handle certain memory situations regarding custom View
   * Processor configurations.
   */
  public static void clearAllWriteThroughCaches() {
    for (WriteThroughViewComputationCache cache : s_instances.values()) {
      cache.clear();
    }
    s_instances.clear();
  }

  public void clear() {
    _readCache.clear();
  }

  protected ViewComputationCache getUnderlying() {
    return _underlying;
  }

  protected Pending waitFor(final ValueSpecification specification) {
    final Pending newPending = new Pending(specification);
    final Pending existingPending = _pending.putIfAbsent(specification, newPending);
    if (existingPending == null) {
      return newPending;
    } else {
      return existingPending;
    }
  }

  protected void post(final ValueSpecification specification, Object value) {
    if (value == null) {
      value = NULL;
    }
    _readCache.put(specification, value);
    Pending pending = _pending.remove(specification);
    if (pending != null) {
      pending.post(value);
    }
  }

  @Override
  public Object getValue(final ValueSpecification specification) {
    Object value = _readCache.putIfAbsent(specification, PENDING);
    if (value == PENDING) {
      final Pending pending = waitFor(specification);
      value = _readCache.get(specification);
      if (value == PENDING) {
        value = pending.waitFor();
      }
    }
    if (value == NULL) {
      //s_logger.debug("Cached NULL for {}", specification);
      value = null;
    } else if (value == null) {
      //s_logger.debug("Cached miss for {}", specification);
      value = getUnderlying().getValue(specification);
      post(specification, value);
    } else {
      s_logger.debug("Cache hit for {}", specification);
    }
    return value;
  }

  @Override
  public Object getValue(final ValueSpecification specification, final CacheSelectHint filter) {
    Object value = _readCache.putIfAbsent(specification, PENDING);
    if (value == PENDING) {
      final Pending pending = waitFor(specification);
      value = _readCache.get(specification);
      if (value == PENDING) {
        value = pending.waitFor();
      }
    }
    if (value == NULL) {
      //s_logger.debug("Cached NULL for {}", specification);
      value = null;
    } else if (value == null) {
      //s_logger.debug("Cached miss for {}", specification);
      value = getUnderlying().getValue(specification, filter);
      post(specification, value);
    } else {
      s_logger.debug("Cache hit for {}", specification);
    }
    return value;
  }

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications) {
    final Collection<Pair<ValueSpecification, Object>> result = new ArrayList<Pair<ValueSpecification, Object>>(specifications.size());
    Collection<Pending> pending = null;
    Collection<ValueSpecification> query = null;
    for (ValueSpecification specification : specifications) {
      Object value = _readCache.putIfAbsent(specification, PENDING);
      if (value == PENDING) {
        final Pending handle = waitFor(specification);
        value = _readCache.get(specification);
        if (value == PENDING) {
          if (pending == null) {
            pending = new ArrayList<Pending>(specifications.size());
          }
          pending.add(handle);
          continue;
        }
      }
      if (value == NULL) {
        //s_logger.debug("Cached NULL for {}", specification);
        result.add(Pairs.of(specification, null));
      } else if (value == null) {
        //s_logger.debug("Cache miss for {}", specification);
        if (query == null) {
          query = Sets.<ValueSpecification>newHashSetWithExpectedSize(specifications.size());
        }
        query.add(specification);
      } else {
        s_logger.debug("Cache hit for {}", specification);
        result.add(Pairs.of(specification, value));
      }
    }
    if (query != null) {
      final Collection<Pair<ValueSpecification, Object>> values = getUnderlying().getValues(query);
      for (Pair<ValueSpecification, Object> value : values) {
        final ValueSpecification valueSpec = value.getFirst();
        post(valueSpec, value.getSecond());
        query.remove(valueSpec);
      }
      result.addAll(values);
      if (!query.isEmpty()) {
        for (ValueSpecification value : query) {
          post(value, null);
        }
      }
    }
    if (pending != null) {
      for (Pending handle : pending) {
        result.add(handle.waitForPair());
      }
    }
    return result;
  }

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications, final CacheSelectHint filter) {
    final Collection<Pair<ValueSpecification, Object>> result = new ArrayList<Pair<ValueSpecification, Object>>(specifications.size());
    Collection<Pending> pending = null;
    Collection<ValueSpecification> query = null;
    for (ValueSpecification specification : specifications) {
      Object value = _readCache.putIfAbsent(specification, PENDING);
      if (value == PENDING) {
        final Pending handle = waitFor(specification);
        value = _readCache.get(specification);
        if (value == PENDING) {
          if (pending == null) {
            pending = new ArrayList<Pending>(specifications.size());
          }
          pending.add(handle);
          continue;
        }
      }
      if (value == NULL) {
        //s_logger.debug("Cached NULL for {}", specification);
        result.add(Pairs.of(specification, null));
      } else if (value == null) {
        //s_logger.debug("Cache miss for {}", specification);
        if (query == null) {
          query = Sets.<ValueSpecification>newHashSetWithExpectedSize(specifications.size());
        }
        query.add(specification);
      } else {
        s_logger.debug("Cache hit for {}", specification);
        result.add(Pairs.of(specification, value));
      }
    }
    if (query != null) {
      final Collection<Pair<ValueSpecification, Object>> values = getUnderlying().getValues(query, filter);
      for (Pair<ValueSpecification, Object> value : values) {
        final ValueSpecification valueSpec = value.getFirst();
        post(valueSpec, value.getSecond());
        query.remove(valueSpec);
      }
      result.addAll(values);
      if (!query.isEmpty()) {
        for (ValueSpecification value : query) {
          post(value, null);
        }
      }
    }
    if (pending != null) {
      for (Pending handle : pending) {
        result.add(handle.waitForPair());
      }
    }
    return result;
  }

  @Override
  public void putSharedValue(final ComputedValue value) {
    _readCache.putIfAbsent(value.getSpecification(), value.getValue());
    getUnderlying().putSharedValue(value);
  }

  @Override
  public void putPrivateValue(final ComputedValue value) {
    _readCache.putIfAbsent(value.getSpecification(), value.getValue());
    getUnderlying().putPrivateValue(value);
  }

  @Override
  public void putValue(final ComputedValue value, final CacheSelectHint filter) {
    _readCache.putIfAbsent(value.getSpecification(), value.getValue());
    getUnderlying().putValue(value, filter);
  }

  @Override
  public void putSharedValues(final Collection<? extends ComputedValue> values) {
    for (ComputedValue value : values) {
      _readCache.putIfAbsent(value.getSpecification(), value.getValue());
    }
    getUnderlying().putSharedValues(values);
  }

  @Override
  public void putPrivateValues(final Collection<? extends ComputedValue> values) {
    for (ComputedValue value : values) {
      _readCache.putIfAbsent(value.getSpecification(), value.getValue());
    }
    getUnderlying().putPrivateValues(values);
  }

  @Override
  public void putValues(final Collection<? extends ComputedValue> values, final CacheSelectHint filter) {
    for (ComputedValue value : values) {
      _readCache.putIfAbsent(value.getSpecification(), value.getValue());
    }
    getUnderlying().putValues(values, filter);
  }

  @Override
  public Integer estimateValueSize(final ComputedValue value) {
    return getUnderlying().estimateValueSize(value);
  }

}
