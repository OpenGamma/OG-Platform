/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Run queue implementation based on a {@link ConcurrentLinkedQueue}. This is a FIFO queue which may not give good caching performance for some function repositories and portfolios.
 */
/* package */final class ConcurrentLinkedQueueRunQueue implements RunQueue {

  private final ConcurrentLinkedQueue<ContextRunnable> _list = new ConcurrentLinkedQueue<ContextRunnable>();

  @Override
  public boolean isEmpty() {
    return _list.isEmpty();
  }

  @Override
  public int size() {
    return _list.size();
  }

  @Override
  public Iterator<ContextRunnable> iterator() {
    return _list.iterator();
  }

  @Override
  public void add(final ContextRunnable runnable) {
    _list.add(runnable);
  }

  @Override
  public ContextRunnable take() {
    return _list.poll();
  }
}
