/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Iterator;

/**
 * Marker interface for the run queue used by the dependency graph builder. A run queue must be safe for use by multiple threads.
 */
/* package */interface RunQueue {

  /**
   * Tests if the queue is empty.
   * 
   * @return true if the queue is empty, false otherwise
   */
  boolean isEmpty();

  /**
   * Returns an approximate size of the queue. This may be a costly operation such as O(N).
   * 
   * @return the approximate size
   */
  int size();

  /**
   * Returns an approximate iterator over the queue elements.
   * 
   * @return the iterator
   */
  Iterator<ContextRunnable> iterator();

  /**
   * Adds a task to the run queue.
   * 
   * @param runnable the task to add, never null
   */
  void add(ContextRunnable runnable);

  /**
   * Removes and returns a task from the run queue.
   * 
   * @return a task or null if the queue is empty
   */
  ContextRunnable take();

}
