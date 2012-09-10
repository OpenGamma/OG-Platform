/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;


/**
 * Variation of the {@link Runnable} interface that accepts a {@link GraphBuildingContext}. This is the
 * basic unit of execution for the graph building algorithm.
 */
/* package */interface ContextRunnable {

  // TODO: We should really be using an external execution framework

  /**
   * Tries to execute this code. If a contention limit gets hit it may return false to request it be deferred.
   * 
   * @param context the building context
   * @return code if the operation completed, false if the operation must be deferred.
   */
  boolean tryRun(GraphBuildingContext context);

}
