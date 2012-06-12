/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
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

/**
 * A {@link ViewComputationCache} that supports an asynchronous write behind update of the underlying cache.
 */
public class WriteBehindViewComputationCache2 implements DeferredViewComputationCache2 {

  private static final Logger s_logger = LoggerFactory.getLogger(WriteBehindViewComputationCache2.class);

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

    public boolean setCallback(ResultCallback<Void> callback) {
      synchronized (this) {
        if (_count > 0) {
          callback = _callback;
          _callback = null;
        } else if (_count < 0) {
          return false;
        }
      }
      if (callback != null) {
        callback.setResult(null);
      }
      return true;
    }

  }

  private final ViewComputationCache _underlying;
  private final ExecutorService _executorService;
  private final Map<ValueSpecification, Object> _pending = new ConcurrentHashMap<ValueSpecification, Object>();
  private final Queue<Entry> _pendingPrivateValues = new ConcurrentLinkedQueue<Entry>();
  private final Queue<Entry> _pendingSharedValues = new ConcurrentLinkedQueue<Entry>();
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
      entry.getStatistics().reportEstimatedSize(value, estimateValueSize(value));
      entry.getOwner().decrement();
      _pending.remove(value.getSpecification());
    }

    private void valuesWritten(final List<Entry> values) {
      for (Entry entry : values) {
        valueWritten(entry);
      }
    }

    private void valuesFailed(final List<Entry> values) {
      for (Entry entry : values) {
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
            if (!_pendingSharedValues.isEmpty()) {
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
            if (!_pendingPrivateValues.isEmpty()) {
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
          } while (!_pendingSharedValues.isEmpty() || !_pendingPrivateValues.isEmpty());
          _valueWriterActive.set(VALUE_WRITER_IDLE);
        } while ((!_pendingSharedValues.isEmpty() || !_pendingPrivateValues.isEmpty()) && _valueWriterActive.compareAndSet(VALUE_WRITER_IDLE, VALUE_WRITER_RUNNING));
        s_logger.info("Write-behind thread terminated after {} operations", count);
      } catch (RuntimeException e) {
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

  public WriteBehindViewComputationCache2(final ViewComputationCache underlying, final ExecutorService executorService) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(executorService, "executorService");
    _underlying = underlying;
    _executorService = executorService;
  }

  protected ViewComputationCache getUnderlying() {
    return _underlying;
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  protected Object getPending(final ValueSpecification specification) {
    return _pending.get(specification);
  }

  protected void putPending(final ComputedValue value) {
    _pending.put(value.getSpecification(), value.getValue());
  }

  protected PendingLock pending(final int count) {
    PendingLock pending = _pendingWrites.get(Thread.currentThread());
    if (pending == null) {
      pending = new PendingLock(count);
      pending = _pendingWrites.putIfAbsent(Thread.currentThread(), pending);
      if (pending != null) {
        pending.increment(count);
      }
    } else {
      pending.increment(count);
    }
    return pending;
  }

  private void failAll() {
    Entry e = _pendingSharedValues.poll();
    while (e != null) {
      e.getOwner().fail(_valueWriterFault);
      e = _pendingSharedValues.poll();
    }
    e = _pendingPrivateValues.poll();
    while (e != null) {
      e.getOwner().fail(_valueWriterFault);
      e = _pendingPrivateValues.poll();
    }
    _pending.clear();
  }

  @Override
  public Object getValue(final ValueSpecification specification) {
    final Object value = getPending(specification);
    if (value != null) {
      return value;
    } else {
      return getUnderlying().getValue(specification);
    }
  }

  @Override
  public Object getValue(final ValueSpecification specification, final CacheSelectHint filter) {
    final Object value = getPending(specification);
    if (value != null) {
      return value;
    } else {
      return getUnderlying().getValue(specification, filter);
    }
  }

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications) {
    Collection<Pair<ValueSpecification, Object>> result = null;
    for (ValueSpecification specification : specifications) {
      final Object object = getPending(specification);
      if (object != null) {
        // Found one in the pending set, so split the set into hits & misses
        int size = specifications.size();
        final List<ValueSpecification> cacheMisses = new ArrayList<ValueSpecification>(size - 1);
        result = new ArrayList<Pair<ValueSpecification, Object>>(size);
        result.add(Pair.of(specification, object));
        for (ValueSpecification specification2 : specifications) {
          // Note the check below; if the write has since completed, the specification we originally hit may now be a miss
          if (specification != specification2) {
            final Object object2 = getPending(specification2);
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
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications, final CacheSelectHint filter) {
    Collection<Pair<ValueSpecification, Object>> result = null;
    for (ValueSpecification specification : specifications) {
      final Object object = getPending(specification);
      if (object != null) {
        // Found one in the pending set, so split the set into hits & misses
        int size = specifications.size();
        final List<ValueSpecification> cacheMisses = new ArrayList<ValueSpecification>(size - 1);
        result = new ArrayList<Pair<ValueSpecification, Object>>(size);
        result.add(Pair.of(specification, object));
        for (ValueSpecification specification2 : specifications) {
          // Note the check below; if the write has since completed, the specification we originally hit may now be a miss
          if (specification != specification2) {
            final Object object2 = getPending(specification2);
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
          result.add(Pair.of(specification2, getUnderlying().getValue(specification2, filter)));
        } else if (size > 1) {
          result.addAll(getUnderlying().getValues(cacheMisses, filter));
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
      // An existing writer has already failed. Ccatch anything posted by the caller that was after the writing thread terminated or was missed by the thread  
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
  public void putSharedValues(final Collection<ComputedValue> values) {
    putSharedValues(values, null);
  }

  @Override
  public void putPrivateValues(final Collection<ComputedValue> values) {
    putPrivateValues(values, null);
  }

  @Override
  public void putValues(final Collection<ComputedValue> values, final CacheSelectHint filter) {
    putValues(values, filter, null);
  }

  @Override
  public Integer estimateValueSize(final ComputedValue value) {
    return getUnderlying().estimateValueSize(value);
  }

  @Override
  public void putSharedValue(final ComputedValue value, final DeferredStatistics statistics) {
    putPending(value);
    _pendingSharedValues.add(new Entry(value, statistics, pending(1)));
    startWriter();
  }

  @Override
  public void putPrivateValue(final ComputedValue value, final DeferredStatistics statistics) {
    putPending(value);
    _pendingPrivateValues.add(new Entry(value, statistics, pending(1)));
    startWriter();
  }

  @Override
  public void putValue(final ComputedValue value, final CacheSelectHint filter, final DeferredStatistics statistics) {
    putPending(value);
    (filter.isPrivateValue(value.getSpecification()) ? _pendingPrivateValues : _pendingSharedValues).add(new Entry(value, statistics, pending(1)));
    startWriter();
  }

  @Override
  public void putSharedValues(final Collection<ComputedValue> values, final DeferredStatistics statistics) {
    final PendingLock lock = pending(values.size());
    for (ComputedValue value : values) {
      putPending(value);
      _pendingSharedValues.add(new Entry(value, statistics, lock));
    }
    startWriter();
  }

  @Override
  public void putPrivateValues(final Collection<ComputedValue> values, final DeferredStatistics statistics) {
    final PendingLock lock = pending(values.size());
    for (ComputedValue value : values) {
      putPending(value);
      _pendingPrivateValues.add(new Entry(value, statistics, lock));
    }
    startWriter();
  }

  @Override
  public void putValues(final Collection<ComputedValue> values, final CacheSelectHint filter, final DeferredStatistics statistics) {
    final PendingLock lock = pending(values.size());
    for (ComputedValue value : values) {
      putPending(value);
      (filter.isPrivateValue(value.getSpecification()) ? _pendingPrivateValues : _pendingSharedValues).add(new Entry(value, statistics, lock));
    }
    startWriter();
  }

  @Override
  public void flush() throws AsynchronousExecution {
    switch (_valueWriterActive.get()) {
      case VALUE_WRITER_IDLE:
        // Writer is not running, so we must be done
        return;
      case VALUE_WRITER_FAILED:
        throw _valueWriterFault;
    }
    final PendingLock write = _pendingWrites.remove(Thread.currentThread());
    if ((write == null) || write.isZero()) {
      // Haven't written anything (null) or have written everything asked for (zero)
      return;
    }
    // Block until the writes from this thread have finished or failed
    final AsynchronousOperation<Void> async = new AsynchronousOperation<Void>();
    if (write.setCallback(async.getCallback())) {
      async.getResult();
    } else {
      throw _valueWriterFault;
    }
  }

}
