/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicSPI;

/**
 * A single unit of work capable of operating on inputs to produce results, configured and ready to execute as at a particular time.
 */
@PublicSPI
public interface CompiledFunctionDefinition {

  /**
   * Returns the underlying {@link FunctionDefinition} that was used to create this instance.
   * 
   * @return the original definition
   */
  FunctionDefinition getFunctionDefinition();

  /**
   * Obtain the most restrictive {@link ComputationTargetType} that this function instance is configured to support. It is always valid to return {@link ComputationTargetType#ANYTHING} as any object
   * can be cast down to a primitive, however returning more restrictive types allows functions to be grouped and improves the performance of the dependency graph build as only the subset supporting a
   * given type need be considered.
   * <p>
   * Some intrinsic functions used in dependency graphs with special meaning to the engine may return null from this method. Any function which returns null here (not the
   * {@link ComputationTargetType#NULL} type) will never be considered as a valid application to satisfy a value requirement but will exist in the repository for cases where it gets selected into a
   * graph by other means.
   * 
   * @return the target type to which this instance can apply, should not be null
   */
  ComputationTargetType getTargetType();

  /**
   * Determine whether this function instance is capable of operating on the specified target. It is only valid to call this with a type such that {@code target.getType()} is compatible with the value
   * returned by {@link #getTargetType}, its behavior is otherwise undefined. It is always valid to return true and validate the target during the call to {@link #getResults}, however this is used
   * during the dependency graph build as a fast check to add or exclude the function from the set to be considered to satisfy a requirement. If a function cannot apply to a target it must not throw
   * an exception; throwing an exception (instead of returning false) should be used in serious cases only (for example the target is generally illegal or inconsistent) as it will abort the dependency
   * graph build for the target.
   * 
   * @param context the compilation context with view-specific parameters and configurations
   * @param target the target for which calculation is desired
   * @return true if this function can produce results for the specified target, false otherwise
   */
  boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target);

  /**
   * Determine which result values can be produced by this function when applied to the specified target assuming no input constraints. Should return the <b>maximal</b> set of potential outputs.
   * <b>Actual</b> computed values will be trimmed. It is only valid to call this on a function which has previously returned true to {@link #canApplyTo} for the given target, its behavior is
   * otherwise undefined. Functions must return null and not throw exceptions if they cannot apply to the target; throwing an exception should be used in serious cases only (for example the target is
   * generally illegal or inconsistent) as it will abort the dependency graph build for that target.
   * <p>
   * If the validity check on the target is costly, it should be performed here and null returned if the target is not suitable. If the validity check on the target is cheap (for example checking an
   * asset class' type) then performance will be improved if the check is performed within {@link #canApplyTo} or a more restrictive type returned by {@link #getTargetType}.
   * 
   * @param context the compilation context with view-specific parameters and configurations
   * @param target the target for which calculation is desired
   * @return All results <b>possible</b> to be computed by this function for this target, null or the empty set if no values are possible.
   */
  Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target);

  /**
   * Obtain all input requirements necessary for the operation of this function at execution time. The target requirement is available to allow property constraints on input requirements to be
   * specified if necessary. It is only valid to call this on a function which has previously returned a value from {@link #getResults(FunctionCompilationContext,ComputationTarget)} that satisfies the
   * desired value, its behavior is otherwise undefined.
   * 
   * @param context the compilation context with view-specific parameters and configurations
   * @param target the target for which calculation is desired
   * @param desiredValue the output the function has been selected to satisfy; i.e. one of the values returned by {@link #getResults} satisfies it
   * @return All input requirements to execute this function on the specified target with the specified configuration. A return value of null indicates that the function cannot produce the desired
   *         value.
   */
  Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue);

  /**
   * Tests whether the function may be able to execute correctly when one or more of its declared requirements are not available. If the function may, the call to
   * {@link #getResults(FunctionCompilationContext, ComputationTarget, Map)} will only include the matched subset. If only certain subsets of inputs are valid for execution, the function may reject
   * invalid ones during the second call to {@code getResults} by returning null. Note that such rejection can increase the cost of graph building; it may be more efficient to create multiple function
   * instances with mandatory requirements for the viable subsets.
   * <p>
   * Note the difference between this and {@link FunctionInvoker#canHandleMissingInputs}. The method here controls graph building; whether a dependency graph will be built that does not produce values
   * satisfying the original requirements. The invoker method controls graph execution; whether the function should be called when values that the dependency graph should have produced have not been
   * because of error or lack of market data.
   * 
   * @return true to continue graph building if one or more requirements are not available, false otherwise
   */
  boolean canHandleMissingRequirements();

  /**
   * Determine which result values can be produced by this function when applied to the specified target given the resolved inputs. Should return the <b>maximal</b> set of potential outputs.
   * <b>Actual</b> computed values will be trimmed. The default implementation from {@link AbstractFunction} will return the same value as
   * {@link #getResults (FunctionCompilationContext, ComputationTarget)}. If a function specified both its outputs and inputs using a wildcard, with the outputs depending on the inputs, it should
   * override this to implement that dependency.
   * <p>
   * If it is not possible to generate any results using the inputs given, an empty set must be returned. It is only valid to call this on a function which has previously returned true to
   * {@link #canApplyTo} for the given target, its behavior is otherwise undefined.
   * 
   * @param context the compilation context with view-specific parameters and configurations
   * @param target the target for which calculation is desired
   * @param inputs the resolved inputs to the function, mapped to the originally requested requirements
   * @return All results <b>possible</b> to be computed by this function for this target with these parameters. A return value of null or the empty set indicates that the function cannot operate on
   *         the resolved inputs.
   */
  Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs);

  /**
   * Determine any additional input requirements needed as a result of input and output resolution. In general, implementations <b>should not</b> override the implementation in
   * {@link AbstractFunction}. It is only valid to call this on a function which has previously returned true to {@link #canApplyTo} for the given target, its behavior is otherwise undefined.
   * 
   * @param context the compilation context with view-specific parameters and configurations
   * @param target the target for which calculation is desired
   * @param inputs the fully resolved input specifications
   * @param outputs the fully resolved output specifications
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
   * If the function is not available at this node, for example because it requires a native library, null may be returned. It is not necessary for an implementation to cache the invoker objects.
   * 
   * @return the function invoker, null if not available at this node
   */
  FunctionInvoker getFunctionInvoker();

}
