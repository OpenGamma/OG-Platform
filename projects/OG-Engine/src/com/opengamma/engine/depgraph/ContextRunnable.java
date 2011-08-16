/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.depgraph.DependencyGraphBuilderPLAT1049.GraphBuildingContext;

/**
 * Variation of the {@link Runnable} interface that accepts a {@link GraphBuildingContext}. This is the
 * basic unit of execution for the graph building algorithm.
 */
public interface ContextRunnable {

  // TODO: We should really be using an external execution framework

  void run(GraphBuildingContext context);

}
