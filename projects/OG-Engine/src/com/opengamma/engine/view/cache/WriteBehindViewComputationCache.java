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
import com.opengamma.engine.view.calcnode.DeferredInvocationStatistics;
import com.opengamma.util.tuple.Pair;

/**
 * A {@link ViewComputationCache} that supports a write behind update of the underlying cache.
 */
public class WriteBehindViewComputationCache extends FilteredViewComputationCache {

  private static final Logger s_logger = LoggerFactory.getLogger(WriteBehindViewComputationCache.class);

  private final ExecutorService _executorService;
  private final Map<ValueSpecification, Object> _pending = new ConcurrentHashMap<ValueSpecification, Object>();

  private final Queue<ComputedValue> _pendingValues = new ConcurrentLinkedQueue<ComputedValue>();
  private final Queue<DeferredInvocationStatistics> _pendingStatistics = new ConcurrentLinkedQueue<DeferredInvocationStatistics>();
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

    private void valueWritten(final ComputedValue value) {
      getPending().remove(value.getSpecification());
      final DeferredInvocationStatistics statistics = _pendingStatistics.peek();
      if (statistics != null) {
        final Integer bytes = estimateValueSize(value);
        if (statistics.addDataOutputBytes(bytes)) {
          _pendingStatistics.poll();
        }
      }
    }

    @Override
    public void run() {
      int count = 0;
      do {
        s_logger.info("Write-behind thread running for {}", WriteBehindViewComputationCache.this.hashCode());
        do {
          if (_pendingValues.size() > 1) {
            final Collection<ComputedValue> values = drain(_pendingValues);
            WriteBehindViewComputationCache.super.putValues(values);
            for (ComputedValue value : values) {
              valueWritten(value);
            }
            count += values.size();
          } else {
            ComputedValue value = _pendingValues.poll();
            while (value != null) {
              WriteBehindViewComputationCache.super.putValue(value);
              valueWritten(value);
              value = _pendingValues.poll();
              count++;
            }
          }
        } while (!_pendingValues.isEmpty());
        _valueWriter.set(null);
        // Values might have been written to the lists before we set valueWriter to null, so
        // check to see if we should carry on rather than terminate.
      } while (!_pendingValues.isEmpty() && _valueWriter.compareAndSet(null, this));
      // Note that if there is a failure anywhere in here, the writer task will die and things will
      // accumulate on the list until synchronize is called at which point the exception gets
      // propagated. Is this wasteful of compute cycles - should we fail the job sooner ?
      s_logger.info("Write-behind thread terminated after {} operations", count);
    }

  };

  private volatile Future<?> _valueWriterFuture;

  public WriteBehindViewComputationCache(final ViewComputationCache underlying, final CacheSelectHint filter, final ExecutorService executorService) {
    super(underlying, filter);
    _executorService = executorService;
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
      return super.getValue(specification);
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
          result.add(Pair.of(specification2, super.getValue(specification2)));
        } else if (size > 1) {
          result.addAll(super.getValues(cacheMisses));
        }
        return result;
      }
    }
    // No pending cache hits
    return super.getValues(specifications);
  }

  private void startWriterIfNotRunning() {
    if (_valueWriter.getAndSet(_valueWriterRunnable) == null) {
      s_logger.info("Starting write-behind thread for {}", WriteBehindViewComputationCache.this.hashCode());
      _valueWriterFuture = getExecutorService().submit(_valueWriterRunnable);
    }
  }

  @Override
  public void putValue(final ComputedValue value) {
    getPending().put(value.getSpecification(), value.getValue());
    _pendingValues.add(value);
    startWriterIfNotRunning();
  }

  @Override
  public void putValues(final Collection<ComputedValue> values) {
    for (ComputedValue value : values) {
      getPending().put(value.getSpecification(), value.getValue());
    }
    _pendingValues.addAll(values);
    startWriterIfNotRunning();
  }

  public void putValues(final Collection<ComputedValue> values, final DeferredInvocationStatistics statistics) {
    _pendingStatistics.add(statistics);
    putValues(values);
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
        s_logger.info("Write-behind cache flushed");
      } catch (InterruptedException e) {
        s_logger.warn("Interrupted during flush");
        _pendingValues.clear();
        valueWriter.cancel(true);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Error synchronising write-behind cache", e);
      }
    } else {
      s_logger.debug("No pending writes");
    }
  }

}
