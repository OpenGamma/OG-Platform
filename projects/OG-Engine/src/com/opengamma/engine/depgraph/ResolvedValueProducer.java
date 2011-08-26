/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.depgraph.DependencyGraphBuilderPLAT1049.GraphBuildingContext;

/**
 * Deferred source of a {@link ResolvedValue}.
 */
/* package */interface ResolvedValueProducer {

  interface Cancellable {
    boolean cancel(GraphBuildingContext context);
  }

  /**
   * Register a callback for notification when the value is produced. If the value has already
   * been produced it may be called immediately.
   * 
   * @param context graph building context
   * @param callback callback object to receive the notifications, not {@code null}
   * @return a handle for removing the callback, or {@code null} if there is nothing to cancel (e.g. a failure call was made
   *         inline) or a cancellation can't be supported. 
   */
  Cancellable addCallback(GraphBuildingContext context, ResolvedValueCallback callback);

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

}
