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
import com.opengamma.util.PublicSPI;

// REVIEW kirk 2009-12-30 -- When we're done with the great refactor, we can actually
// collapse everything down to this interface and FunctionInvoker since we have a
// singularity in terms of ComputationTarget and ComputationTargetSpecification.
// We no longer need different interfaces.

/**
 * A single unit of work capable of operating on inputs to produce results. 
 *
 */
@PublicSPI
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
   * The unique identifier for an {@code FunctionDefinition} is the handle
   * through which its {@link FunctionInvoker} can be identified
   * from the {@link FunctionRepository} which sourced the function.
   * In general, functions will not specify this themselves, but the repository
   * will provide a unique identifier for them.
   * <p/>
   * In general, implementations should <b>not</b> override the implementation in
   * {@link AbstractFunction}.
   * 
   * @return The unique identifier for this function.
   */
  String getUniqueIdentifier();
  
  /**
   * Obtain a short name of the function, suitable for display to the user.
   * Examples: "Yield-Curve Bootstrap", "Black-Scholes-Merton Option Pricing",
   * "Volatility Surface Fitting".
   * 
   * @return A user-friendly short name for this function.
   */
  String getShortName();
  
  /**
   * Obtain the core {@link ComputationTargetType} that this function instance is configured
   * to support.
   * While this can be determined by the subgraph, it is provided at this
   * level for ease of programming, and for performance purposes.
   *  
   * @return The target type to which this instance can apply.
   */
  ComputationTargetType getTargetType();

  /**
   * Determine whether this function instance is capable of operating on the specified target.
   * 
   * @param context The compilation context with view-specific parameters and configurations.
   * @param target The target for which calculation is desired.
   * @return {@code true} iff this function can produce results for the specified target.
   */
  boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target);

  /**
   * Obtain all input requirements necessary for the operation of this function at execution time.
   * 
   * @param context The compilation context with view-specific parameters and configurations.
   * @param target The target for which calculation is desired.
   * @return All input requirements to execute this function on the specified target with the specified configuration.
   */
  Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target);
  
  // See ENG-216
  /**
   * Determine the known-to-be live data inputs to this function.
   * In general, implementations <b>should not</b> override the implementation
   * in {@link AbstractFunction}. This method is deprecated and will be removed.
   * @return Required live data for this function.
   */
  Set<ValueSpecification> getRequiredLiveData();
  
  /**
   * Determine which result values can be produced by this function when applied to the
   * specified target.
   * Should return the <b>maximal</b> set of potential outputs. <b>Actual</b> computed values
   * will be trimmed.
   * 
   * @param context The compilation context with view-specific parameters and configurations.
   * @param target The target for which calculation is desired.
   * @return All results <b>possible</b> to be computed by this node for this target with these parameters.
   */
  Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target);
  
  /**
   * Obtain the default parameters for this function. Where these are not overridden in the
   * {@link FunctionCompilationContext}, the values provided by this method will be used internally.
   * 
   * @return The default parameters for this function
   */
  FunctionParameters getDefaultParameters();
}
