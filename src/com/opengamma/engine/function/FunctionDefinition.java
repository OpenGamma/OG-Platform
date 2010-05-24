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
  
  /**
   * Initialize the function definition with an example {@link FunctionCompilationContext}.
   * This method will be called during the {@link ViewProcessor} startup call, and will be done
   * exactly once per {@link ViewProcessor}'s lifetime.
   * Because this is done on a per-processor basis rather than a per-view basis, the
   * {@link FunctionCompilationContext} provided will only contain details that are generic
   * to the processor being initialized, rather than the {@link View} being compiled.
   * 
   * @param context The full compilation context to be provided during graph construction.
   */
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
   * @return The target type to which this instance can apply.
   */
  ComputationTargetType getTargetType();
 
  boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target);
  
  Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target);
  
  /**
   * Should return the <b>maximal</b> set of potential outputs. <b>Actual</b> computed values
   * will be trimmed.
   * @param context Key parameters useful during compilation.
   * @param target The target to which the function should be applied.
   * @return All results <b>possible</b> to be computed by this node for this target with these parameters.
   */
  Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target);
}
