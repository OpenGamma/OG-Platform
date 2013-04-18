/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PoolExecutor;

// REVIEW kirk 2010-01-02 -- One reason this class exists is so that you can parallel-apply
// many of the operations if they happen to be expensive. This isn't done yet, but the
// more code that depends on this class the easier it is to port to that system.

/**
 * A manager for traversing the tree of nodes.
 */
public abstract class PortfolioNodeTraverser {

  /**
   * The callback.
   */
  private final PortfolioNodeTraversalCallback _callback;

  /**
   * Creates a traverser using depth-first searching. If you don't know whether to use depth-first or breadth-first, then use depth-first.
   * 
   * @param callback the callback to invoke, not null
   * @return the traverser, not null
   */
  public static PortfolioNodeTraverser depthFirst(final PortfolioNodeTraversalCallback callback) {
    return new DepthFirstPortfolioNodeTraverser(callback);
  }

  /**
   * Creates a traverser using breadth-first searching. If you don't know whether to use depth-first or breadth-first, then use depth-first.
   * 
   * @param callback the callback to invoke, not null
   * @return the traverser, not null
   */
  public static PortfolioNodeTraverser breadthFirst(final PortfolioNodeTraversalCallback callback) {
    return new BreadthFirstPortfolioNodeTraverser(callback);
  }

  /**
   * Creates a traverser using a parallel searching approach. The ordering of nodes and positions to the callback is non-deterministic. If you don't know whether to use this or not use depth-first.
   * 
   * @param callback the callback to invoke, not null
   * @param executorService the executor service for parallel execution, not null
   * @return the traverser, not null
   */
  public static PortfolioNodeTraverser parallel(final PortfolioNodeTraversalCallback callback, final PoolExecutor executorService) {
    return new ParallelPortfolioNodeTraverser(callback, executorService);
  }

  /**
   * Creates a traverser.
   * 
   * @param callback the callback to invoke, not null
   */
  protected PortfolioNodeTraverser(final PortfolioNodeTraversalCallback callback) {
    ArgumentChecker.notNull(callback, "callback");
    _callback = callback;
  }

  /**
   * Gets the callback to be used.
   * 
   * @return the callback, not null
   */
  public PortfolioNodeTraversalCallback getCallback() {
    return _callback;
  }

  /**
   * Traverse the nodes notifying using the callback.
   * 
   * @param portfolioNode the node to start from, null does nothing
   */
  public abstract void traverse(PortfolioNode portfolioNode);

}
