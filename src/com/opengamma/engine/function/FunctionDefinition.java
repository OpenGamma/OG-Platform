/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

// REVIEW kirk 2009-12-30 -- When we're done with the great refactor, we can actually
// collapse everything down to this interface and FunctionInvoker since we have a
// singularity in terms of ComputationTarget and ComputationTargetSpecification.
// We no longer need different interfaces.

/**
 * A single unit of work capable of operating on inputs to produce results. 
 *
 * @author kirk
 */
public interface FunctionDefinition {
  
  void init(FunctionCompilationContext context);
  /**
   * The unique identifier for an {@code AnalyticFunction} is the handle
   * through which its {@link FunctionInvoker} can be identified
   * from the {@link FunctionRepository} which sourced the function.
   * In general, functions will not specify this themselves, but the repository
   * will provide a unique identifier for them.
   * 
   * @return The unique identifier for this function.
   */
  String getUniqueIdentifier();
  
  String getShortName();
  
  boolean buildsOwnSubGraph();
  
  /**
   * While this can be determined by the subgraph, it is provided at this
   * level for ease of programming.
   *  
   * @return
   */
  ComputationTargetType getTargetType();
 
  boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target);
  
  Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target);
  
  Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Set<ValueRequirement> requirements);
}
