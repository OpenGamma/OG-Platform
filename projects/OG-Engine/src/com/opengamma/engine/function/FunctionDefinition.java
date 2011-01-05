/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import javax.time.InstantProvider;

import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewProcessor;
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
   * 
   * Because this is done on a per-processor basis rather than a per-view basis, the
   * {@link FunctionCompilationContext} provided will only contain details that are generic
   * to the processor being initialized, rather than the {@link View} being compiled.
   * 
   * This should perform any one-off initialisations required. Any operations that will change
   * over time should be performed as part of compilation, see {@link #compile}.
   * 
   * @param context The full compilation context to be provided during graph construction.
   */
  void init(FunctionCompilationContext context);

  /**
   * Initialize the function definition for execution at the given instant. The compiled function
   * may be valid for a much greater range. It is not necessary for an implementation to cache
   * compiled instances - the repository managers will do that.
   * 
   * @param context The full compilation context to be provided during graph constructor.
   * @param atInstant The snapshot time for which the function will be used.
   * @return The compiled function.
   */
  CompiledFunctionDefinition compile(FunctionCompilationContext context, InstantProvider atInstant);

  /**
   * The unique identifier for a {@code FunctionDefinition} is the handle
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
  String getUniqueId();

  /**
   * Obtain a short name of the function, suitable for display to the user.
   * Examples: "Yield-Curve Bootstrap", "Black-Scholes-Merton Option Pricing",
   * "Volatility Surface Fitting".
   * 
   * @return A user-friendly short name for this function.
   */
  String getShortName();

  /**
   * Obtain the default parameters for this function. Where these are not overridden in the
   * {@link FunctionCompilationContext}, the values provided by this method will be used internally.
   * 
   * @return The default parameters for this function
   */
  FunctionParameters getDefaultParameters();
  
}
