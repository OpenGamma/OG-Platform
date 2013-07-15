/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.cache.MissingOutput;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * A no-op function. This will never be selected during graph construction, but can be present in an execution plan as a placeholder for a suppressed function.
 * <p>
 * This should be present in all function repositories with its preferred identifier.
 */
public final class NoOpFunction extends IntrinsicFunction {

  /**
   * Shared instance.
   */
  public static final NoOpFunction INSTANCE = new NoOpFunction();

  /**
   * Preferred identifier this function will be available in a repository as.
   */
  public static final String UNIQUE_ID = "No-op";

  public NoOpFunction() {
    super(UNIQUE_ID);
  }

  // FunctionInvoker

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Set<ComputedValue> result = Sets.newHashSetWithExpectedSize(desiredValues.size());
    for (ValueRequirement desiredValue : desiredValues) {
      result.add(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), target.toSpecification(), desiredValue.getConstraints()), MissingOutput.SUPPRESSED));
    }
    return result;
  }

}
