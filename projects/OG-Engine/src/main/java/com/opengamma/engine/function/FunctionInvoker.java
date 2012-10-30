/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.async.AsynchronousExecution;


/**
 * The interface through which a Function will be invoked on a calculation node during
 * view execution.
 * This is separate from the metadata for the function (available through {@link FunctionDefinition} and
 * {@link CompiledFunctionDefinition}) as it's entirely possible that certain functions may require
 * native libraries that are not supported on every node.
 */
@PublicSPI
public interface FunctionInvoker {

  /**
   * Execute on the specified target, producing the values desired.
   * Exceptions thrown will result in a failure of this node.
   * 
   * @param executionContext The execution-time configuration for this invocation.
   * @param inputs All required inputs pre-packaged for this function invocation.
   * @param target The target on which calculation should be performed.
   * @param desiredValues The only values that should be computed by this invocation.
   * @return All values that were computed by this invocation.
   */
  Set<ComputedValue> execute(
      FunctionExecutionContext executionContext,
      FunctionInputs inputs,
      ComputationTarget target,
      //Set<ValueSpecification> outputs // PLAT-2290
      Set<ValueRequirement> desiredValues) throws AsynchronousExecution;

  /**
   * Tests whether the function should be executed when one or more of its expected input values
   * have not been calculated (perhaps due to an execution fault or missing market data).
   * 
   * @return true to always execute the function, false to execute even if there are missing
   *         input values
   */
  boolean canHandleMissingInputs();

}
