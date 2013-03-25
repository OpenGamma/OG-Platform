/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import org.threeten.bp.Instant;

import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.util.PublicSPI;

// REVIEW kirk 2009-12-30 -- When we're done with the great refactor, we can actually
// collapse everything down to this interface and FunctionInvoker since we have a
// singularity in terms of ComputationTarget and ComputationTargetSpecification.
// We no longer need different interfaces.

/**
 * A single unit of work capable of operating on inputs to produce results.
 */
@PublicSPI
public interface FunctionDefinition {

  /**
   * <p>
   * Initialize the function definition with a {@link FunctionCompilationContext}. This method will be called during {@link ViewProcessor} startup if the JVM contains one. Otherwise it will be called
   * by the calculation node container startup. After the initial invocation it may be called in the future if changes to the compilation context, e.g. the configuration data, has changed.
   * </p>
   * <p>
   * Because this is done on a per-processor basis rather than a per-view basis, the {@link FunctionCompilationContext} provided will only contain details that are generic to the processor being
   * initialized, rather than the {@link ViewProcess} being compiled.
   * </p>
   * <p>
   * This should perform any one-off initializations required. Any operations that will change over time should be performed as part of compilation, see {@link #compile}.
   * </p>
   * 
   * @param context The full compilation context to be provided during graph construction.
   * @deprecated See [PLAT-2240]. Sub-classes should declare this as a no-op.
   */
  @Deprecated
  void init(FunctionCompilationContext context);

  /**
   * Initialize the function definition for execution at the given instant. The compiled function may be valid for a much greater range. It is not necessary for an implementation to cache compiled
   * instances - the repository managers will do that.
   * <p>
   * This is done on a per-processor basis rather than a per-view bases so the {@link FunctionCompilationContext} provided will only contain details that are generic to the processor being initialized
   * rather than a {@link ViewProcess} that may be executed following the compilation.
   * <p>
   * This may perform one-off initializations and return a compiled definition with an unbounded execution time frame. If the initialization process or compilation makes assumptions about the
   * valuation time then it may give bounded execution times; this method will be called again with different snapshot times if execution is required outside of the range satisfied by a previous
   * compilation.
   * <p>
   * During compilation, any state that may change yet must be consistent across a view cycle regardless of distribution considerations should be requested via the target resolver attached to the
   * compilation context. Any changes in the resolution of objects returned by this service will trigger recompilation of the function such that any changes to configuration data will be propagated
   * throughout an executing system.
   * 
   * @param context The full compilation context to be provided during graph constructor.
   * @param atInstant The snapshot time for which the function will be used.
   * @return The compiled function.
   */
  CompiledFunctionDefinition compile(FunctionCompilationContext context, Instant atInstant);

  /**
   * The unique identifier for a {@code FunctionDefinition} is the handle through which its {@link FunctionInvoker} can be identified from the {@link FunctionRepository} which sourced the function. In
   * general, functions will not specify this themselves, but the repository will provide a unique identifier for them.
   * <p/>
   * In general, implementations should <b>not</b> override the implementation in {@link AbstractFunction}.
   * 
   * @return The unique identifier for this function.
   */
  String getUniqueId();

  /**
   * Obtain a short name of the function, suitable for display to the user. Examples: "Yield-Curve Bootstrap", "Black-Scholes-Merton Option Pricing", "Volatility Surface Fitting".
   * 
   * @return A user-friendly short name for this function.
   */
  String getShortName();

  /**
   * Obtain the default parameters for this function. Where these are not overridden in the {@link FunctionCompilationContext}, the values provided by this method will be used internally.
   * 
   * @return The default parameters for this function
   */
  FunctionParameters getDefaultParameters();

}
