/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.async.AsynchronousOperation;
import com.opengamma.util.async.ResultCallback;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A {@link ViewComputationCache} that supports an asynchronous write behind update of the underlying cache.
 */
public class WriteBehindViewComputationCache implements DeferredViewComputationCache {

  private static final Logger s_logger = LoggerFactory.getLogger(WriteBehindViewComputationCache.class);

  private static final class Entry {

    private final ComputedValue _value;
    private final DeferredStatistics _statistics;
    private final PendingLock _owner;

    public Entry(final ComputedValue value, final DeferredStatistics statistics, final PendingLock owner) {
      _value = value;
      _statistics = statistics;
      _owner = owner;
    }

    public ComputedValue getValue() {
      return _value;
    }

    public DeferredStatistics getStatistics() {
      return _statistics;
    }

    public PendingLock getOwner() {
      return _owner;
    }

  }

  private static final class ComputedValueCollection extends AbstractCollection<ComputedValue> {

    private final Collection<Entry> _underlying;

    public ComputedValueCollection(final Collection<Entry> underlying) {
      _underlying = underlying;
    }

    @Override
    public Iterator<ComputedValue> iterator() {
      return new Iterator<ComputedValue>() {

        private final Iterator<Entry> _iterator = _underlying.iterator();

        @Override
        public boolean hasNext() {
          return _iterator.hasNext();
        }

        @Override
        public ComputedValue next() {
          return _iterator.next().getValue();
        }

        @Override
        public void remove() {
          _iterator.remove();
        }

      };
    }

    @Override
    public int size() {
      return _underlying.size();
    }

  }

  private static final class PendingLock {

    private int _count;
    private ResultCallback<Void> _callback;

    public PendingLock(final int count) {
      _count = count;
    }

    public synchronized boolean isZero() {
      return _count == 0;
    }

    public synchronized void increment(final int count) {
      if (_count >= 0) {
        _count += count;
      }
    }

    public void decrement() {
      ResultCallback<Void> callback = null;
      synchronized (this) {
        final int newCount = --_count;
        assert newCount >= 0;
        if (newCount == 0) {
          callback = _callback;
          _callback = null;
        }
      }
      if (callback != null) {
        callback.setResult(null);
      }
    }

    public void decrement(final int count) {
      ResultCallback<Void> callback = null;
      synchronized (this) {
        if (_count > 0) {
          _count -= count;
          assert _count >= 0;
          if (_count == 0) {
            callback = _callback;
            _callback = null;
          }
        }
      }
      if (callback != null) {
        callback.setResult(null);
      }
    }

    public void fail(final RuntimeException e) {
      ResultCallback<Void> callback;
      synchronized (this) {
        _count = -1;
        callback = _callback;
        _callback = null;
      }
      if (callback != null) {
        callback.setException(e);
      }
    }

    public boolean setCallback(final ResultCallback<Void> callback) {
      synchronized (this) {
        if (_count > 0) {
          _callback = callback;
          return true;
        } else if (_count < 0) {
          return false;
        }
      }
      callback.setResult(null);
      return true;
    }

  }

  private final ViewComputationCache _underlying;
  private final ExecutorService _executorService;
  /**
   * The in memory map contains all entries that are in the queues and have not been written to the underlying yet as well as values that have been retrieved from the underlying. Use of a soft
   * reference on the values will keep the values in memory for as long as possible so that the serialization/deserialization overhead of the underlying can probably be avoided for frequently used
   * objects.
   */
  private final Map<ValueSpecification, Object> _buffer = new MapMaker().softValues().makeMap();
  private final Queue<Entry> _pendingPrivateValues;
  private final Queue<Entry> _pendingSharedValues;
  private final ConcurrentMap<Thread, PendingLock> _pendingWrites = new MapMaker().weakKeys().makeMap();

  private static final int VALUE_WRITER_IDLE = 0;
  private static final int VALUE_WRITER_RUNNING = 1;
  private static final int VALUE_WRITER_FAILED = 2;

  private final AtomicInteger _valueWriterActive = new AtomicInteger(VALUE_WRITER_IDLE);
  private volatile RuntimeException _valueWriterFault;
  private final Runnable _valueWriterRunnable = new Runnable() {

    private List<Entry> drain(final Queue<Entry> source) {
      // Make the list bigger than the source queue as writes will probably occur while we're draining it.
      final List<Entry> dest = new ArrayList<Entry>(source.size() * 2);
      Entry value = source.poll();
      while (value != null) {
        dest.add(value);
        value = source.poll();
      }
      return dest;
    }

    private void valueWritten(final Entry entry) {
      final ComputedValue value = entry.getValue();
      final DeferredStatistics statistics = entry.getStatistics();
      if (statistics != null) {
        statistics.reportEstimatedSize(value, estimateValueSize(value));
      }
      entry.getOwner().decrement();
    }

    private void valuesWritten(final List<Entry> values) {
      for (final Entry entry : values) {
        valueWritten(entry);
      }
    }

    private void valuesFailed(final List<Entry> values) {
      for (final Entry entry : values) {
        entry.getOwner().fail(_valueWriterFault);
      }
    }

    @Override
    public void run() {
      List<Entry> values = null;
      int count = 0;
      try {
        do {
          s_logger.info("Write-behind thread running for {}", getUnderlying());
          do {
            if ((_pendingSharedValues != null) && !_pendingSharedValues.isEmpty()) {
              values = drain(_pendingSharedValues);
              if (values.size() > 1) {
                getUnderlying().putSharedValues(new ComputedValueCollection(values));
                valuesWritten(values);
                count += values.size();
              } else {
                final Entry entry = values.get(0);
                getUnderlying().putSharedValue(entry.getValue());
                valueWritten(entry);
                count++;
              }
              values = null;
            }
            if ((_pendingPrivateValues != null) && !_pendingPrivateValues.isEmpty()) {
              values = drain(_pendingPrivateValues);
              if (values.size() > 1) {
                getUnderlying().putPrivateValues(new ComputedValueCollection(values));
                valuesWritten(values);
                count += values.size();
              } else {
                final Entry entry = values.get(0);
                getUnderlying().putPrivateValue(entry.getValue());
                valueWritten(entry);
                count++;
              }
              values = null;
            }
          } while (((_pendingSharedValues != null) && !_pendingSharedValues.isEmpty()) || ((_pendingPrivateValues != null) && !_pendingPrivateValues.isEmpty()));
          _valueWriterActive.set(VALUE_WRITER_IDLE);
        } while ((((_pendingSharedValues != null) && !_pendingSharedValues.isEmpty()) || ((_pendingPrivateValues != null) && !_pendingPrivateValues.isEmpty())) &&
            _valueWriterActive.compareAndSet(VALUE_WRITER_IDLE, VALUE_WRITER_RUNNING));
        s_logger.info("Write-behind thread terminated after {} operations", count);
      } catch (final RuntimeException e) {
        s_logger.warn("Write-behind thread failed after {} operations: {}", count, e.getMessage());
        _valueWriterFault = e;
        _valueWriterActive.set(VALUE_WRITER_FAILED);
        if (values != null) {
          valuesFailed(values);
        }
        failAll();
      }
    }

  };

  public WriteBehindViewComputationCache(final ViewComputationCache underlying, final ExecutorService executorService, final boolean useShared, final boolean usePrivate) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(executorService, "executorService");
    _underlying = underlying;
    _executorService = executorService;
    _pendingPrivateValues = usePrivate ? new ConcurrentLinkedQueue<Entry>() : null;
    _pendingSharedValues = useShared ? new ConcurrentLinkedQueue<Entry>() : null;
  }

  protected ViewComputationCache getUnderlying() {
    return _underlying;
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  protected Object getBuffered(final ValueSpecification specification) {
    return _buffer.get(specification);
  }

  protected void putPending(final ComputedValue value) {
    putBuffered(value.getSpecification(), value.getValue());
  }

  protected void putBuffered(final ValueSpecification specification, final Object value) {
    _buffer.put(specification, value);
  }

  protected void clearBuffer() {
    _buffer.clear();
  }

  protected PendingLock pending(final int count) {
    PendingLock pending = _pendingWrites.get(Thread.currentThread());
    if (pending == null) {
      pending = new PendingLock(count);
      final PendingLock existing = _pendingWrites.putIfAbsent(Thread.currentThread(), pending);
      if (existing != null) {
        existing.increment(count);
        return existing;
      } else {
        return pending;
      }
    } else {
      pending.increment(count);
      return pending;
    }
  }

  private void failAll() {
    if (_pendingSharedValues != null) {
      Entry e = _pendingSharedValues.poll();
      while (e != null) {
        e.getOwner().fail(_valueWriterFault);
        e = _pendingSharedValues.poll();
      }
    }
    if (_pendingPrivateValues != null) {
      Entry e = _pendingPrivateValues.poll();
      while (e != null) {
        e.getOwner().fail(_valueWriterFault);
        e = _pendingPrivateValues.poll();
      }
    }
    _buffer.clear();
  }

  @Override
  public Object getValue(final ValueSpecification specification) {
    Object value = getBuffered(specification);
    if (value != null) {
      return value;
    } else {
      value = getUnderlying().getValue(specification);
      if (value != null) {
        putBuffered(specification, value);
      }
      return value;
    }
  }

  @Override
  public Object getValue(final ValueSpecification specification, final CacheSelectHint filter) {
    Object value = getBuffered(specification);
    if (value != null) {
      return value;
    } else {
      value = getUnderlying().getValue(specification, filter);
      if (value != null) {
        putBuffered(specification, value);
      }
      return value;
    }
  }

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications) {
    Collection<Pair<ValueSpecification, Object>> result = null;
    for (final ValueSpecification specification : specifications) {
      final Object object = getBuffered(specification);
      if (object != null) {
        // Found one in the pending set, so split the set into hits & misses
        int size = specifications.size();
        final List<ValueSpecification> cacheMisses = new ArrayList<ValueSpecification>(size - 1);
        result = new ArrayList<Pair<ValueSpecification, Object>>(size);
        result.add(Pairs.of(specification, object));
        for (final ValueSpecification specification2 : specifications) {
          // Note the check below; if the write has since completed, the specification we originally hit may now be a miss
          if (specification != specification2) {
            final Object object2 = getBuffered(specification2);
            if (object2 != null) {
              result.add(Pairs.of(specification2, object2));
            } else {
              cacheMisses.add(specification2);
            }
          }
        }
        size = cacheMisses.size();
        s_logger.debug("{} pending cache hit(s), {} miss(es)", result.size(), size);
        if (size == 1) {
          final ValueSpecification specification2 = cacheMisses.get(0);
          final Object value = getUnderlying().getValue(specification2);
          result.add(Pairs.of(specification2, value));
          if (value != null) {
            putBuffered(specification2, value);
          }
        } else if (size > 1) {
          final Collection<Pair<ValueSpecification, Object>> values = getUnderlying().getValues(cacheMisses);
          result.addAll(values);
          for (final Pair<ValueSpecification, Object> value : values) {
            if (value.getSecond() != null) {
              putBuffered(value.getFirst(), value.getSecond());
            }
          }
        }
        return result;
      }
    }
    // No pending cache hits
    return getUnderlying().getValues(specifications);
  }

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications, final CacheSelectHint filter) {
    Collection<Pair<ValueSpecification, Object>> result = null;
    for (final ValueSpecification specification : specifications) {
      final Object object = getBuffered(specification);
      if (object != null) {
        // Found one in the pending set, so split the set into hits & misses
        int size = specifications.size();
        final List<ValueSpecification> cacheMisses = new ArrayList<ValueSpecification>(size - 1);
        result = new ArrayList<Pair<ValueSpecification, Object>>(size);
        result.add(Pairs.of(specification, object));
        for (final ValueSpecification specification2 : specifications) {
          // Note the check below; if the write has since completed, the specification we originally hit may now be a miss
          if (specification != specification2) {
            final Object object2 = getBuffered(specification2);
            if (object2 != null) {
              result.add(Pairs.of(specification2, object2));
            } else {
              cacheMisses.add(specification2);
            }
          }
        }
        size = cacheMisses.size();
        s_logger.debug("{} pending cache hit(s), {} miss(es)", result.size(), size);
        if (size == 1) {
          final ValueSpecification specification2 = cacheMisses.get(0);
          final Object value = getUnderlying().getValue(specification2, filter);
          result.add(Pairs.of(specification2, value));
          if (value != null) {
            putBuffered(specification2, value);
          }
        } else if (size > 1) {
          final Collection<Pair<ValueSpecification, Object>> values = getUnderlying().getValues(cacheMisses, filter);
          result.addAll(values);
          for (final Pair<ValueSpecification, Object> value : values) {
            if (value.getSecond() != null) {
              putBuffered(value.getFirst(), value.getSecond());
            }
          }
        }
        return result;
      }
    }
    // No pending cache hits
    return getUnderlying().getValues(specifications, filter);
  }

  /**
   * Starts the background writing thread to pass values to the underlying if it is not already started.
   */
  protected void startWriter() {
    if (_valueWriterActive.compareAndSet(VALUE_WRITER_IDLE, VALUE_WRITER_RUNNING)) {
      // Start a writer
      getExecutorService().submit(_valueWriterRunnable);
    } else if (_valueWriterActive.get() == VALUE_WRITER_FAILED) {
      // An existing writer has already failed. Catch anything posted by the caller that was after the writing thread terminated or was missed by the thread
      failAll();
    }
  }

  @Override
  public void putSharedValue(final ComputedValue value) {
    putSharedValue(value, null);
  }

  @Override
  public void putPrivateValue(final ComputedValue value) {
    putPrivateValue(value, null);
  }

  @Override
  public void putValue(final ComputedValue value, final CacheSelectHint filter) {
    putValue(value, filter, null);
  }

  @Override
  public void putSharedValues(final Collection<? extends ComputedValue> values) {
    putSharedValues(values, null);
  }

  @Override
  public void putPrivateValues(final Collection<? extends ComputedValue> values) {
    putPrivateValues(values, null);
  }

  @Override
  public void putValues(final Collection<? extends ComputedValue> values, final CacheSelectHint filter) {
    putValues(values, filter, null);
  }

  @Override
  public Integer estimateValueSize(final ComputedValue value) {
    return getUnderlying().estimateValueSize(value);
  }

  @Override
  public void putSharedValue(final ComputedValue value, final DeferredStatistics statistics) {
    putPending(value);
    if (_pendingSharedValues != null) {
      _pendingSharedValues.add(new Entry(value, statistics, pending(1)));
      startWriter();
    } else {
      getUnderlying().putSharedValue(value);
      if (statistics != null) {
        statistics.reportEstimatedSize(value, estimateValueSize(value));
      }
    }
  }

  @Override
  public void putPrivateValue(final ComputedValue value, final DeferredStatistics statistics) {
    putPending(value);
    if (_pendingPrivateValues != null) {
      _pendingPrivateValues.add(new Entry(value, statistics, pending(1)));
      startWriter();
    } else {
      getUnderlying().putPrivateValue(value);
      if (statistics != null) {
        statistics.reportEstimatedSize(value, estimateValueSize(value));
      }
    }
  }

  @Override
  public void putValue(final ComputedValue value, final CacheSelectHint filter, final DeferredStatistics statistics) {
    if (filter.isPrivateValue(value.getSpecification())) {
      putPrivateValue(value, statistics);
    } else {
      putSharedValue(value, statistics);
    }
  }

  @Override
  public void putSharedValues(final Collection<? extends ComputedValue> values, final DeferredStatistics statistics) {
    if (_pendingSharedValues != null) {
      final PendingLock lock = pending(values.size());
      for (final ComputedValue value : values) {
        putPending(value);
        _pendingSharedValues.add(new Entry(value, statistics, lock));
      }
      startWriter();
    } else {
      // TODO: ought to putBuffered
      getUnderlying().putSharedValues(values);
      if (statistics != null) {
        for (final ComputedValue value : values) {
          statistics.reportEstimatedSize(value, estimateValueSize(value));
        }
      }
    }
  }

  @Override
  public void putPrivateValues(final Collection<? extends ComputedValue> values, final DeferredStatistics statistics) {
    if (_pendingPrivateValues != null) {
      final PendingLock lock = pending(values.size());
      for (final ComputedValue value : values) {
        putPending(value);
        _pendingPrivateValues.add(new Entry(value, statistics, lock));
      }
      startWriter();
    } else {
      // TODO: ought to putBuffered
      getUnderlying().putPrivateValues(values);
      if (statistics != null) {
        for (final ComputedValue value : values) {
          statistics.reportEstimatedSize(value, estimateValueSize(value));
        }
      }
    }
  }

  @Override
  public void putValues(final Collection<? extends ComputedValue> values, final CacheSelectHint filter, final DeferredStatistics statistics) {
    final PendingLock lock = pending(values.size());
    int undo = 0;
    for (final ComputedValue value : values) {
      putPending(value);
      if (filter.isPrivateValue(value.getSpecification())) {
        if (_pendingPrivateValues != null) {
          _pendingPrivateValues.add(new Entry(value, statistics, lock));
        } else {
          getUnderlying().putPrivateValue(value);
          if (statistics != null) {
            statistics.reportEstimatedSize(value, estimateValueSize(value));
          }
          undo++;
        }
      } else {
        if (_pendingSharedValues != null) {
          _pendingSharedValues.add(new Entry(value, statistics, lock));
        } else {
          getUnderlying().putSharedValue(value);
          if (statistics != null) {
            statistics.reportEstimatedSize(value, estimateValueSize(value));
          }
          undo++;
        }
      }
    }
    if (undo > 0) {
      lock.decrement(undo);
    }
    startWriter();
  }

  @Override
  public void flush() throws AsynchronousExecution {
    switch (_valueWriterActive.get()) {
      case VALUE_WRITER_IDLE:
        // Writer is not running, so we must be done
        s_logger.debug("Writer is idle");
        return;
      case VALUE_WRITER_FAILED:
        s_logger.error("Writer already failed at flush: {}", _valueWriterFault.getMessage());
        throw _valueWriterFault;
    }
    final PendingLock write = _pendingWrites.remove(Thread.currentThread());
    if ((write == null) || write.isZero()) {
      // Haven't written anything (null) or have written everything asked for (zero)
      s_logger.debug("Writer flushed");
      return;
    }
    // Block until the writes from this thread have finished or failed
    s_logger.debug("Deferring to asynchronous thread");
    final AsynchronousOperation<Void> async = AsynchronousOperation.create(Void.class);
    if (write.setCallback(async.getCallback())) {
      // TODO: How far from completion are we? Will it be worth the asynchronous exception overhead or just block?
      async.getResult();
    } else {
      s_logger.error("Writer already failed at flush: {}", _valueWriterFault.getMessage());
      throw _valueWriterFault;
    }
  }

}
