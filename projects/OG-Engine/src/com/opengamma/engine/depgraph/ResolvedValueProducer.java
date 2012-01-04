/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Set;

import com.opengamma.engine.depgraph.DependencyGraphBuilder.GraphBuildingContext;

/**
 * Deferred source of a {@link ResolvedValue}.
 */
/* package */interface ResolvedValueProducer {

  interface Cancelable {
    boolean cancel(GraphBuildingContext context);
  }

  /**
   * Register a callback for notification when the value is produced. If the value has already
   * been produced it may be called immediately.
   * 
   * @param context graph building context
   * @param callback callback object to receive the notifications, not null
   * @return a handle for removing the callback, or null if there is nothing to cancel (e.g. a failure call was made
   *         inline) or a cancellation can't be supported. 
   */
  Cancelable addCallback(GraphBuildingContext context, ResolvedValueCallback callback);

  /**
   * Increment the reference count on the object.
   */
  void addRef();

  /**
   * Decrement the reference count on the object. An implementation may perform cleanup actions on the count reaching zero.
   * 
   * @param context graph building context
   * @return the updated reference count
   */
  int release(GraphBuildingContext context);

  interface Chain {

    /**
     * Detects any recursion in the onward chain of callbacks, canceling one or more
     * of them to break the recursion by allowing a failure to propagate outwards.
     * 
     * @param context the graph building context
     * @param visited the set of visited callbacks
     * @return the number of callbacks that were canceled
     */
    int cancelLoopMembers(GraphBuildingContext context, Set<Object> visited);

  }

}
