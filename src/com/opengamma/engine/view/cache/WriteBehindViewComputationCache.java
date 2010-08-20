/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * A {@link ViewComputationCache} that supports a write behind update of the underlying cache.
 */
public class WriteBehindViewComputationCache implements ViewComputationCache {

  private static final Logger s_logger = LoggerFactory.getLogger(WriteBehindViewComputationCache.class);

  private final ViewComputationCache _underlying;
  private final ExecutorService _executorService;
  private final Map<ValueSpecification, Object> _pending = new ConcurrentHashMap<ValueSpecification, Object>();

  private List<ComputedValue> _values;
  private Future<?> _valueWriter;

  public WriteBehindViewComputationCache(final ViewComputationCache underlying, final ExecutorService executorService) {
    _underlying = underlying;
    _executorService = executorService;
  }

  // TODO this should be protected, but is public as a hack for diagnostics in AbstractCalculationNode
  public ViewComputationCache getUnderlying() {
    return _underlying;
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  protected Map<ValueSpecification, Object> getPending() {
    return _pending;
  }

  @Override
  public Object getValue(final ValueSpecification specification) {
    final Object object = getPending().get(specification);
    if (object != null) {
      s_logger.debug("Pending cache hit");
      return object;
    } else {
      return getUnderlying().getValue(specification);
    }
  }

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications) {
    Collection<Pair<ValueSpecification, Object>> result = null;
    for (ValueSpecification specification : specifications) {
      final Object object = getPending().get(specification);
      if (object != null) {
        // Found one in the pending set, so split the set into hits & misses
        int size = specifications.size();
        final List<ValueSpecification> cacheMisses = new ArrayList<ValueSpecification>(size - 1);
        result = new ArrayList<Pair<ValueSpecification, Object>>(size);
        result.add(Pair.of(specification, object));
        for (ValueSpecification specification2 : specifications) {
          // Note the check below; if the write has since completed, the specification we originally hit may now be a miss
          if (specification != specification2) {
            final Object object2 = getPending().get(specification2);
            if (object2 != null) {
              result.add(Pair.of(specification2, object2));
            } else {
              cacheMisses.add(specification2);
            }
          }
        }
        size = cacheMisses.size();
        s_logger.debug("{} pending cache hit(s), {} miss(es)", result.size(), size);
        if (size == 1) {
          final ValueSpecification specification2 = cacheMisses.get(0);
          result.add(Pair.of(specification2, getUnderlying().getValue(specification2)));
        } else if (size > 1) {
          result.addAll(getUnderlying().getValues(cacheMisses));
        }
        return result;
      }
    }
    // No pending cache hits
    return getUnderlying().getValues(specifications);
  }

  @Override
  public void putValue(final ComputedValue value) {
    getPending().put(value.getSpecification(), value.getValue());
    synchronized (this) {
      if (_values != null) {
        _values.add(value);
        return;
      } else {
        _values = new LinkedList<ComputedValue>();
        _values.add(value);
      }
      startWriter();
    }
  }

  @Override
  public void putValues(final Collection<ComputedValue> values) {
    for (ComputedValue value : values) {
      getPending().put(value.getSpecification(), value.getValue());
    }
    synchronized (this) {
      if (_values != null) {
        _values.addAll(values);
        return;
      } else {
        _values = new LinkedList<ComputedValue>(values);
      }
      startWriter();
    }
  }

  // caller already owns the monitor
  private void startWriter() {
    s_logger.info("Starting write-behind thread");
    _valueWriter = getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        List<ComputedValue> emptyList = new LinkedList<ComputedValue>();
        int count = 0;
        do {
          final List<ComputedValue> valuesToWrite;
          synchronized (WriteBehindViewComputationCache.this) {
            valuesToWrite = _values;
            if (valuesToWrite.isEmpty()) {
              _values = null;
              _valueWriter = null;
              s_logger.info("Write-behind thread finished after {} value write(s)", count);
              return;
            } else {
              _values = emptyList;
            }
          }
          final int size = valuesToWrite.size();
          count += size;
          s_logger.debug("Write-behind thread with {} value(s) for underlying", size);
          if (size == 1) {
            getUnderlying().putValue(valuesToWrite.get(0));
          } else if (size > 1) {
            getUnderlying().putValues(valuesToWrite);
          }
          for (ComputedValue value : valuesToWrite) {
            getPending().remove(value.getSpecification());
          }
          valuesToWrite.clear();
          emptyList = valuesToWrite;
        } while (true);
        // Note that if there is a failure anywhere in here, the writer task will die and things will
        // accumulate on the list until synchronize is called at which point the exception gets
        // propogated. Is this wasteful of compute cycles - should we fail the job sooner ?
      }
    });
  }

  // TODO [ENG-181] put this into the ViewComputationCache interface & get rid of the cast below
  public void cacheValueSpecifications(final Collection<ValueSpecification> valueSpecifications) {
    ((DefaultViewComputationCache) getUnderlying()).getIdentifierMap().getIdentifiers(valueSpecifications);
  }

  /**
   * Block until all "write-behind" operations have completed. Do not call this concurrently with
   * {@link #putValue} or {@link #putValues}.
   */
  public void waitForPendingWrites() {
    final Future<?> valueWriter;
    synchronized (this) {
      valueWriter = _valueWriter;
      _valueWriter = null;
    }
    if (valueWriter != null) {
      s_logger.info("Waiting for write-behind thread to complete");
      try {
        valueWriter.get();
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Error synchronising write-behind cache", e);
      }
      s_logger.info("Write-behind cache flushed");
    } else {
      s_logger.debug("No pending writes");
    }
  }

}
