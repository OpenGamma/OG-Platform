/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;


/**
 * The interface through which an Analytic Function can actually be invoked.
 *
 * @author kirk
 */
public interface FunctionInvoker {

  Set<ComputedValue> execute(
      FunctionExecutionContext executionContext,
      FunctionInputs inputs,
      ComputationTarget target,
      Set<ValueRequirement> desiredValues);
}
