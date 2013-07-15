/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.covariance;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
 * Acts on a target of {@link ComputationTarget#NULL} to mean the view's portfolio. This is a convenience function to allow this to be more easily added as a specific requirement to a view.
 */
public class DefaultTargetCovarianceMatrixFunction extends AbstractFunction.NonCompiledInvoker {

  // CompiledFunctionDefinition

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return context.getPortfolio() != null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.COVARIANCE_MATRIX, ComputationTargetSpecification.NULL, ValueProperties.all()),
        new ValueSpecification(ValueRequirementNames.CORRELATION_MATRIX, ComputationTargetSpecification.NULL, ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.singleton(new ValueRequirement(desiredValue.getValueName(), new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO, context.getPortfolio()
        .getUniqueId()), desiredValue.getConstraints().withoutAny(ValuePropertyNames.FUNCTION)));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = inputs.keySet().iterator().next();
    final ValueProperties.Builder properties = createValueProperties();
    for (String property : input.getProperties().getProperties()) {
      if (!ValuePropertyNames.FUNCTION.equals(property)) {
        final Set<String> values = input.getProperties().getValues(property);
        if (values.isEmpty()) {
          properties.withAny(property);
        } else {
          properties.with(property, values);
        }
      }
    }
    return Collections.singleton(new ValueSpecification(input.getValueName(), ComputationTargetSpecification.NULL, properties.get()));
  }

  // FunctionInvoker

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext context, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Object value = inputs.getValue(desiredValue.getValueName());
    final ValueRequirement resultReq = desiredValues.iterator().next();
    final ValueSpecification resultSpec = new ValueSpecification(desiredValue.getValueName(), ComputationTargetSpecification.NULL, resultReq.getConstraints());
    return Collections.singleton(new ComputedValue(resultSpec, value));
  }

}
