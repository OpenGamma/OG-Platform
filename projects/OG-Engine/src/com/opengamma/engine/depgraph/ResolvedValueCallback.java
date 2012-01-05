/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.depgraph.DependencyGraphBuilder.GraphBuildingContext;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Callback interface to receive the results of a resolution.
 */
/* package */interface ResolvedValueCallback {

  /**
   * Notifies the implementer of a successful resolution.
   * 
   * @param context graph building context
   * @param valueRequirement requirement resolved
   * @param resolvedValue the resolved specification
   * @param pump a pump callback for providing the next possible resolution (or a failure)
   */
  void resolved(GraphBuildingContext context, ValueRequirement valueRequirement, ResolvedValue resolvedValue, ResolutionPump pump);

  /**
   * Notifies the implementer of a failed resolution (or no more successful ones).
   * 
   * @param context graph building context
   * @param value requirement that couldn't be resolved
   * @param failure description of the failure
   */
  void failed(GraphBuildingContext context, ValueRequirement value, ResolutionFailure failure);

  interface ResolvedValueCallbackChain extends ResolvedValueCallback, ResolvedValueProducer.Chain {

  }

}
