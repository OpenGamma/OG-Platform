/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Run queue implementation based on a {@link LinkedList}. Not very efficient because of the synchronization but useful for debugging.
 */
/* package */abstract class LinkedListRunQueue implements RunQueue {

  private final LinkedList<ContextRunnable> _list = new LinkedList<ContextRunnable>();

  public static final class FIFO extends LinkedListRunQueue {

    @Override
    public synchronized void add(final ContextRunnable runnable) {
      getList().addLast(runnable);
    }

  }

  public static final class LIFO extends LinkedListRunQueue {

    @Override
    public synchronized void add(final ContextRunnable runnable) {
      getList().addFirst(runnable);
    }

  }

  protected LinkedList<ContextRunnable> getList() {
    return _list;
  }

  @Override
  public synchronized boolean isEmpty() {
    return getList().isEmpty();
  }

  @Override
  public synchronized int size() {
    return getList().size();
  }

  @Override
  public Iterator<ContextRunnable> iterator() {
    final int elements = getList().size();
    return new Iterator<ContextRunnable>() {

      private int _count;

      @Override
      public boolean hasNext() {
        return _count < elements;
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
  public synchronized ContextRunnable take() {
    return getList().pollFirst();
  }
}
