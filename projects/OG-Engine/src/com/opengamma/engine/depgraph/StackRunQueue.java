/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Run queue implementation based on a stack. This is a LIFO queue which may give good performance as computation targets often end up grouped together.
 */
/* package */final class StackRunQueue implements RunQueue {

  private static final class Element {

    private volatile ContextRunnable _runnable;
    private volatile Element _next;

    private Element(final ContextRunnable runnable, final Element next) {
      _runnable = runnable;
      _next = next;
    }

  }

  private final AtomicReference<Element> _head = new AtomicReference<Element>();
  private final AtomicReference<Element> _free = new AtomicReference<Element>();

  @Override
  public boolean isEmpty() {
    return _head.get() == null;
  }

  @Override
  public int size() {
    Element e = _head.get();
    int count = 0;
    while (e != null) {
      count++;
      e = e._next;
    }
    return count;
  }

  @Override
  public Iterator<ContextRunnable> iterator() {
    return new Iterator<ContextRunnable>() {

      private Element _ptr = _head.get();

      @Override
      public boolean hasNext() {
        return _ptr != null;
      }

      @Override
      public ContextRunnable next() {
        Element ptr = _ptr;
        _ptr = ptr._next;
        return ptr._runnable;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  @Override
  public void add(final ContextRunnable runnable) {
    Element e = _free.get();
    if (e != null) {
      if (_free.compareAndSet(e, e._next)) {
        e._next = _head.get();
        e._runnable = runnable;
      } else {
        e = new Element(runnable, _head.get());
      }
    } else {
      e = new Element(runnable, _head.get());
    }
    while (!_head.compareAndSet(e._next, e)) {
      e._next = _head.get();
    }
  }

  @Override
  public ContextRunnable take() {
    Element e;
    do {
      e = _head.get();
      if (e == null) {
        return null;
      }
    } while (!_head.compareAndSet(e, e._next));
    final ContextRunnable runnable = e._runnable;
    e._runnable = null;
    e._next = _free.get();
    _free.compareAndSet(e._next, e);
    return runnable;
  }
}
