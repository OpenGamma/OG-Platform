/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

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

  private final Queue<ComputedValue> _privateValues = new ConcurrentLinkedQueue<ComputedValue>();
  private final Queue<ComputedValue> _sharedValues = new ConcurrentLinkedQueue<ComputedValue>();
  private final AtomicReference<Runnable> _valueWriter = new AtomicReference<Runnable>();
  private final Runnable _valueWriterRunnable = new Runnable() {

    /**
     * The length of the drain list is made a touch bigger than the source in-case a write occurs while we're
     * draining it.
     */
    private static final int DRAIN_BUFFER = 2;

    private List<ComputedValue> drain(final Queue<ComputedValue> source) {
      final List<ComputedValue> dest = new ArrayList<ComputedValue>(source.size() + DRAIN_BUFFER);
      ComputedValue value = source.poll();
      while (value != null) {
        dest.add(value);
        value = source.poll();
      }
      return dest;
    }

    @Override
    public void run() {
      int sharedCount = 0;
      int privateCount = 0;
      do {
        s_logger.info("Write-behind thread running for {}", WriteBehindViewComputationCache.this.hashCode());
        do {
          // Commit the shared values to the underlying
          if (_sharedValues.size() > 1) {
            final Collection<ComputedValue> values = drain(_sharedValues);
            getUnderlying().putSharedValues(values);
            for (ComputedValue value : values) {
              getPending().remove(value.getSpecification());
            }
            sharedCount += values.size();
          } else {
            ComputedValue value = _sharedValues.poll();
            while (value != null) {
              getUnderlying().putSharedValue(value);
              getPending().remove(value.getSpecification());
              value = _sharedValues.poll();
              sharedCount++;
            }
          }
          // Commit the private values to the underlying
          if (_privateValues.size() > 1) {
            final Collection<ComputedValue> values = drain(_privateValues);
            getUnderlying().putSharedValues(values);
            for (ComputedValue value : values) {
              getPending().remove(value.getSpecification());
            }
            privateCount += values.size();
          } else {
            ComputedValue value = _privateValues.poll();
            while (value != null) {
              getUnderlying().putSharedValue(value);
              getPending().remove(value.getSpecification());
              value = _privateValues.poll();
              privateCount++;
            }
          }
        } while (!_privateValues.isEmpty() || !_sharedValues.isEmpty());
        _valueWriter.set(null);
        // Values might have been written to the lists before we set valueWriter to null, so
        // check to see if we should carry on rather than terminate.
      } while ((!_privateValues.isEmpty() || !_sharedValues.isEmpty()) && _valueWriter.compareAndSet(null, this));
      // Note that if there is a failure anywhere in here, the writer task will die and things will
      // accumulate on the list until synchronize is called at which point the exception gets
      // propogated. Is this wasteful of compute cycles - should we fail the job sooner ?
      s_logger.info("Write-behind thread terminated after {} shared, and {} private operations", sharedCount, privateCount);
    }

  };

  private volatile Future<?> _valueWriterFuture;

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

  private void startWriterIfNotRunning() {
    if (_valueWriter.getAndSet(_valueWriterRunnable) == null) {
      s_logger.info("Starting write-behind thread for {}", WriteBehindViewComputationCache.this.hashCode());
      _valueWriterFuture = getExecutorService().submit(_valueWriterRunnable);
    }
  }

  private void putValue(final ComputedValue value, final Queue<ComputedValue> values) {
    getPending().put(value.getSpecification(), value.getValue());
    values.add(value);
    startWriterIfNotRunning();
  }

  @Override
  public void putPrivateValue(final ComputedValue value) {
    putValue(value, _privateValues);
  }

  @Override
  public void putSharedValue(final ComputedValue value) {
    putValue(value, _sharedValues);
  }

  private void putValues(final Collection<ComputedValue> values, final Queue<ComputedValue> writeQueue) {
    for (ComputedValue value : values) {
      getPending().put(value.getSpecification(), value.getValue());
    }
    writeQueue.addAll(values);
    startWriterIfNotRunning();
  }

  @Override
  public void putPrivateValues(final Collection<ComputedValue> values) {
    putValues(values, _privateValues);
  }

  @Override
  public void putSharedValues(final Collection<ComputedValue> values) {
    putValues(values, _sharedValues);
  }

  /**
   * Block until all "write-behind" operations have completed. Do not call this concurrently with
   * {@link #putValue} or {@link #putValues}.
   */
  public void waitForPendingWrites() {
    final Future<?> valueWriter = _valueWriterFuture;
    _valueWriterFuture = null;
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
