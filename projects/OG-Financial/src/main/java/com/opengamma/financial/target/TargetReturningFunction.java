/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.target;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Function that returns the target object (UniqueIdentifiable).
 */
public class TargetReturningFunction extends AbstractFunction.NonCompiledInvoker {

  // REVIEW 2013-03-06 Andrew -- Candidate to move into OG-Engine as "TargetSourcingFunction"

  /**
   *
   */
  public TargetReturningFunction() {
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    //final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), properties);
    ValueProperties outputProperties = createValueProperties().get();
    ValueSpecification spec = new ValueSpecification(ValueRequirementNames.TARGET, target.toSpecification(), outputProperties);
    //return new ValueSpecification(_newValueName, inputSpec.getTargetSpecification(), outputProperties);
    return ImmutableSet.of(new ComputedValue(spec, target.getValue()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.ANYTHING;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return true;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.TARGET, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    return Collections.EMPTY_SET;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    if (!inputs.isEmpty()) { // doesn't take any inputs
      return null;
    }
    ValueProperties outputProperties = createValueProperties().get();
    ValueSpecification spec = new ValueSpecification(ValueRequirementNames.TARGET, target.toSpecification(), outputProperties);
    return ImmutableSet.of(spec);
    //ValueSpecification inputSpec = Iterables.getOnlyElement(inputs.keySet());
    //return ImmutableSet.of(getOutputSpec(inputSpec));
  }

  protected ValueSpecification getOutputSpec(ValueSpecification inputSpec) {
    ValueProperties outputProperties = inputSpec.getProperties().copy()
        .withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    return new ValueSpecification(ValueRequirementNames.TARGET, inputSpec.getTargetSpecification(), outputProperties);
  }

}
