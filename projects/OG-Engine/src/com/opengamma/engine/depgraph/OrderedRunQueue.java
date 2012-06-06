/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueId;

/**
 * Run queue implementation based on sorting the runnable tasks. When the run queue is small, this is comparable to {@link ConcurrentLinkedQueueRunQueue} or {@link StackRunQueue} implementations as
 * the sorting operation is not performed. As the run queue length grows and exceeds the capacity of the target resolution cache, the cost of ordering the operations may be offset by better use of the
 * cache. Some function repositories and graph structures may still benefit from the simpler {@link StackRunQueue} implementation which can give good cache performance without any ordering costs.
 */
/* package */final class OrderedRunQueue implements RunQueue, Comparator<ContextRunnable> {

  // TODO: Note that this might not be correct with regard to the Java Memory Model. 

  private static final int MAX_UNSORTED = 2048;
  private static final int BUFFER_INCREMENT = 4096;

  private ContextRunnable[] _buffer = new ContextRunnable[BUFFER_INCREMENT];
  private int _length;
  private int _sorted;
  private boolean _sorting;
  private final Object _sortingLock = new Object();

  @Override
  public synchronized boolean isEmpty() {
    return _length == 0;
  }

  @Override
  public synchronized int size() {
    return _length;
  }

  @Override
  public Iterator<ContextRunnable> iterator() {
    return new Iterator<ContextRunnable>() {

      private int _count;

      @Override
      public boolean hasNext() {
        return _count < size();
      }

      @Override
      public ContextRunnable next() {
        _count++;
        return null;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public void add(final ContextRunnable runnable) {
    int sorted;
    synchronized (this) {
      if (_length >= _buffer.length) {
        // Can't enlarge the buffer if there is a sort operation outstanding
        synchronized (_sortingLock) {
          final ContextRunnable[] newBuffer = new ContextRunnable[_length + BUFFER_INCREMENT];
          System.arraycopy(_buffer, 0, newBuffer, 0, _length);
          _buffer = newBuffer;
        }
      }
      _buffer[_length++] = runnable;
      if ((_sorting) || (_length - _sorted < MAX_UNSORTED)) {
        return;
      }
      sorted = _sorted;
      _sorted = (_length + _sorted) >> 1;
      _sorting = true;
    }
    synchronized (_sortingLock) {
      if (sorted < _sorted) {
        Arrays.sort(_buffer, sorted, _sorted, this);
        if (sorted > 0) {
          // We now have two sorted fragments; merge them together
          final ContextRunnable[] newBuffer = new ContextRunnable[_sorted];
          int i = 0, j = sorted, n = 0;
          while ((i < sorted) && (j < _sorted)) {
            final int c = compare(_buffer[i], _buffer[j]);
            if (c < 0) {
              newBuffer[n++] = _buffer[i++];
            } else if (c > 0) {
              newBuffer[n++] = _buffer[j++];
            } else {
              newBuffer[n++] = _buffer[i++];
              newBuffer[n++] = _buffer[j++];
            }
          }
          if (i < sorted) {
            System.arraycopy(_buffer, i, newBuffer, n, sorted - i);
            assert n + sorted - i == _sorted;
          } else if (j < _sorted) {
            System.arraycopy(_buffer, j, newBuffer, n, _sorted - j);
            assert n + _sorted - j == _sorted;
          }
          System.arraycopy(newBuffer, 0, _buffer, 0, _sorted);
        }
      }
    }
    synchronized (this) {
      _sorting = false;
    }
  }

  @Override
  public synchronized ContextRunnable take() {
    if (_length == 0) {
      return null;
    }
    final int index = --_length;
    final ContextRunnable runnable;
    if (!_sorting && (index < _sorted)) {
      _sorted = index;
    }
    if (index >= _sorted) {
      runnable = _buffer[index];
      _buffer[index] = null;
    } else {
      // Need to acquire the "sorting" lock to read into the sorted data
      synchronized (_sortingLock) {
        runnable = _buffer[index];
        _buffer[index] = null;
        if (index < _sorted) {
          _sorted = index;
        }
      }
    }
    return runnable;
  }

  private static int compareUID(final ComputationTargetSpecification cts1, final ComputationTargetSpecification cts2) {
    final UniqueId uid1 = cts1.getUniqueId();
    final UniqueId uid2 = cts2.getUniqueId();
    if (uid1 != null) {
      if (uid2 != null) {
        return uid1.compareTo(uid2);
      } else {
        return 1;
      }
    } else {
      if (uid2 != null) {
        return -1;
      } else {
        return 1;
      }
    }
  }

  /**
   * Runnable task comparison. In runnable priority order (most preferable to run first):
   * <ul>
   * <li>PORTFOLIO_NODE</li>
   * <li>PRIMITIVE</li>
   * <li>SECURITY</li>
   * <li>TRADE</li>
   * <li>POSITION</li>
   * </ul>
   * Within a given computation target type, ordering is based on the unique identifier. The aim is to get tasks that will "complete" running sooner (i.e. the primitive and security level functions)
   * to reduce the live memory footprint during a graph build. Portfolio node targets are performed at a high priority so that if individual positions are also requested then the graph build for both
   * should run in parallel if the node function is a basic aggregation. Ordering the unique identifiers should group values on the same target to give better utilization of the computation target
   * resolver cache.
   * <p>
   * Note this sorts into reverse order so that the most preferable to run are at the end of the array.
   */
  @Override
  public int compare(final ContextRunnable r1, final ContextRunnable r2) {
    if (r1 instanceof ResolveTask) {
      if (r2 instanceof ResolveTask) {
        final ResolveTask rt1 = (ResolveTask) r1;
        final ResolveTask rt2 = (ResolveTask) r2;
        final ComputationTargetSpecification cts1 = rt1.getValueRequirement().getTargetSpecification();
        final ComputationTargetSpecification cts2 = rt2.getValueRequirement().getTargetSpecification();
        switch (cts1.getType()) {
          case PORTFOLIO_NODE:
            switch (cts2.getType()) {
              case PORTFOLIO_NODE:
                return compareUID(cts1, cts2);
              default:
                return 1;
            }
          case POSITION:
            switch (cts2.getType()) {
              case POSITION:
                return compareUID(cts1, cts2);
              default:
                return -1;
            }
          case TRADE:
            switch (cts2.getType()) {
              case POSITION:
                return 1;
              case TRADE:
                return compareUID(cts1, cts2);
              default:
                return -1;
            }
          case SECURITY:
            switch (cts2.getType()) {
              case POSITION:
              case TRADE:
                return 1;
              case SECURITY:
                return compareUID(cts1, cts2);
              default:
                return -1;
            }
          case PRIMITIVE:
            switch (cts2.getType()) {
              case POSITION:
              case TRADE:
              case SECURITY:
                return 1;
              case PRIMITIVE:
                return compareUID(cts1, cts2);
              default:
                return -1;
            }
          default:
            throw new IllegalStateException();
        }
      } else {
        // Do non-ResolveTask (r2) first
        return -1;
      }
    } else {
      if (r2 instanceof ResolveTask) {
        // Do non-ResolveTask (r1) first
        return 1;
      } else {
        // Don't care
        return 0;
      }
    }
  }

}
