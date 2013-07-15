/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Run queue implementation based on a stack. This is a LIFO queue which may give good performance as computation targets often end up grouped together.
 */
/* package */final class StackRunQueue implements RunQueue {

  private final Deque<ContextRunnable> _deque = new ConcurrentLinkedDeque<ContextRunnable>();

  @Override
  public boolean isEmpty() {
    return _deque.isEmpty();
  }

  @Override
  public int size() {
    return _deque.size();
  }

  @Override
  public Iterator<ContextRunnable> iterator() {
    return _deque.iterator();
  }

  @Override
  public void add(final ContextRunnable runnable) {
    _deque.addLast(runnable);
  }

  @Override
  public ContextRunnable take() {
    return _deque.pollLast();
  }
}
