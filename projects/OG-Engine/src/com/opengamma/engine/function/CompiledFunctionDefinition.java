/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Map;
import java.util.Set;

import javax.time.Instant;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicSPI;

/**
 * A single unit of work capable of operating on inputs to produce results, configured and 
 * ready to execute as at a particular time.
 */
@PublicSPI
public interface CompiledFunctionDefinition {

  /**
   * Returns the underlying {@link FunctionDefinition} that was used to create this
   * instance.
   * 
   * @return the original definition
   */
  FunctionDefinition getFunctionDefinition();

  /**
   * Obtain the core {@link ComputationTargetType} that this function instance is configured
   * to support.
   * While this can be determined by the subgraph, it is provided at this
   * level for ease of programming, and for performance purposes.
   *  
   * @return the target type to which this instance can apply, not null
   */
  ComputationTargetType getTargetType();

  /**
   * Determine whether this function instance is capable of operating on the specified target.
   * 
   * @param context  the compilation context with view-specific parameters and configurations
   * @param target  the target for which calculation is desired
   * @return true iff this function can produce results for the specified target
   */
  boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target);

  /**
   * Determine which result values can be produced by this function when applied to the
   * specified target assuming no input constraints. Should return the <b>maximal</b> set of potential outputs.
   * <b>Actual</b> computed values will be trimmed. It is only valid to call this on a function which has
   * previously returned true to {@link #canApplyTo} for the given target, its behavior is otherwise
   * undefined.
   * 
   * @param context  the compilation context with view-specific parameters and configurations
   * @param target  the target for which calculation is desired
   * @return All results <b>possible</b> to be computed by this function for this target, null or the empty set
   *         if no values are possible.
   */
  Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target);

  /**
   * Obtain all input requirements necessary for the operation of this function at execution time.
   * The target requirement is available to allow property constraints on input requirements to be
   * specified if necessary. It is only valid to call this on a function which has previously
   * returned true to {@link #canApplyTo} for the given target, its behavior is otherwise
   * undefined.
   * 
   * @param context  the compilation context with view-specific parameters and configurations
   * @param target  the target for which calculation is desired
   * @param desiredValue  the output the function has been selected to satisfy; i.e. one of the
   * values returned by {@link #getResults} satisfies it
   * @return All input requirements to execute this function on the specified target with the specified configuration. A return
   *         value of null indicates that the function cannot produce the desired value.
   */
  Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue);

  /**
   * Tests whether the function may be able to execute correctly when one or more of its declared
   * requirements are not available. If the function may, the call to {@link #getResults(FunctionCompilationContext, ComputationTarget, Map)}
   * will only include the matched subset. If only certain subsets of inputs are valid for
   * execution, the function may reject invalid ones during the second call to {@code getResults}
   * by returning null. Note that such rejection can increase the cost of graph building; it may
   * be more efficient to create multiple function instances with mandatory requirements for the
   * viable subsets.
   * <p>
   * Note the difference between this and {@link FunctionInvoker#canHandleMissingInputs}. The
   * method here controls graph building; whether a dependency graph will be built that does not
   * produce values satisfying the original requirements. The invoker method controls graph
   * execution; whether the function should be called when values that the dependency graph should
   * have produced have not been because of error or lack of market data.
   * 
   * @return true to continue graph building if one or more requirements are not available, false otherwise
   */
  boolean canHandleMissingRequirements();

  /**
   * Determine which result values can be produced by this function when applied to the
   * specified target given the resolved inputs. Should return the <b>maximal</b> set of potential outputs.
   * <b>Actual</b> computed values will be trimmed. The default implementation from {@link AbstractFunction}
   * will return the same value as {@link #getResults (FunctionCompilationContext, ComputationTarget)}. If
   * a function specified both its outputs and inputs using a wildcard, with the outputs depending on the
   * inputs, it should override this to implement that dependency.
   * <p>
   * If it is not possible to generate any results using the inputs given, an empty set must be returned.
   * It is only valid to call this on a function which has previously returned true to {@link #canApplyTo}
   * for the given target, its behavior is otherwise undefined.
   * 
   * @param context  the compilation context with view-specific parameters and configurations
   * @param target  the target for which calculation is desired
   * @param inputs  the resolved inputs to the function, mapped to the originally requested requirements
   * @return All results <b>possible</b> to be computed by this function for this target with these parameters.
   *  A return value of null or the empty set indicates that the function cannot operate on the resolved inputs.
   */
  Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs);

  /**
   * Determine any additional input requirements needed as a result of input and output resolution.
   * In general, implementations <b>should not</b> override the implementation in {@link AbstractFunction}. It
   * is only valid to call this on a function which has previously returned true to {@link #canApplyTo}
   * for the given target, its behavior is otherwise undefined.
   * 
   * @param context  the compilation context with view-specific parameters and configurations
   * @param target  the target for which calculation is desired
   * @param inputs  the fully resolved input specifications
   * @param outputs  the fully resolved output specifications
   * @return Any additional input requirements to satisfy execution on the given inputs to deliver the given outputs.
   */
  Set<ValueRequirement> getAdditionalRequirements(FunctionCompilationContext context, ComputationTarget target, Set<ValueSpecification> inputs, Set<ValueSpecification> outputs);

  /**
   * States the earliest time that this metadata and invoker will be valid for.
   * <p>
   * If the definition is always valid returns null.
   * 
   * @return the earliest timestamp, null if definition always valid
   */
  Instant getEarliestInvocationTime();

  /**
   * States the latest time that this metadata and invoker will be valid for.
   * <p>
   * If the definition is always valid returns null.
   * 
   * @return the latest timestamp, null if definition always valid
   */
  Instant getLatestInvocationTime();

  /**
   * Returns an invocation handle to the compiled function.
   * <p>
   * If the function is not available at this node, for example because it requires a native library,
   * null may be returned. It is not necessary for an implementation to cache the invoker objects.
   * 
   * @return the function invoker, null if not available at this node
   */
  FunctionInvoker getFunctionInvoker();

}
