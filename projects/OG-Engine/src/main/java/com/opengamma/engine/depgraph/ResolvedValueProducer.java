/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Map;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Deferred source of a {@link ResolvedValue}.
 */
/* package */interface ResolvedValueProducer {

  // TODO: rename to ContextCancelable to match the ContextRunnable, better still use an external execution framework

  interface Cancelable {

    boolean cancel(GraphBuildingContext context);

  }

  /**
   * Queries the requirement this is producing a resolved value for.
   * 
   * @return the value requirement
   */
  ValueRequirement getValueRequirement();

  /**
   * Register a callback for notification when the value is produced. If the value has already been produced it may be called immediately.
   * 
   * @param context graph building context
   * @param callback callback object to receive the notifications, not null
   * @return a handle for removing the callback, or null if there is nothing to cancel (e.g. a failure call was made inline) or a cancellation can't be supported.
   */
  Cancelable addCallback(GraphBuildingContext context, ResolvedValueCallback callback);

  /**
   * Increment the reference count on the object.
   * 
   * @return true if the reference count was incremented, false if the object has already been discarded
   */
  boolean addRef();

  /**
   * Decrement the reference count on the object. An implementation may perform cleanup actions on the count reaching zero.
   * 
   * @param context graph building context
   * @return the updated reference count
   */
  int release(GraphBuildingContext context);

  /**
   * Returns the current reference count.
   * 
   * @return the reference count
   */
  int getRefCount();

  interface Chain {

    enum LoopState {
      CHECKING,
      IN_LOOP,
      NOT_IN_LOOP
    }

    /**
     * Detects any recursion in the onward chain of callbacks, canceling one or more of them to break the recursion by allowing a failure to propagate outwards.
     * 
     * @param context the graph building context
     * @param visited the map of visited callbacks to their loop detection state, no entry in the map if not visited
     * @return the number of callbacks that were canceled
     */
    int cancelLoopMembers(GraphBuildingContext context, Map<Chain, LoopState> visited);

  }

}
