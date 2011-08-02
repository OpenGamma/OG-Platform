/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.depgraph.DependencyGraphBuilder.GraphBuildingContext;
import com.opengamma.util.Cancellable;

/**
 * Deferred source of a {@link ResolvedValue}.
 */
/* package */interface ResolvedValueProducer {

  /**
   * Register a callback for notification when the value is produced. If the value has already
   * been produced it may be called immediately.
   * 
   * @param context graph building context
   * @param callback callback object to receive the notifications, not {@code null}
   * @return a handle for removing the callback, not {@code null}
   */
  Cancellable addCallback(GraphBuildingContext context, ResolvedValueCallback callback);

}
