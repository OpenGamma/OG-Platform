/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.covariance;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;

/**
 * Converts a covariance matrix to a correlation matrix.
 */
public class CorrelationMatrixFunction extends AbstractFunction.NonCompiledInvoker {

  // CompiledFunctionDefinition

  @Override
  public ComputationTargetType getTargetType() {
    return SampledCovarianceMatrixFunction.TYPE;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.CORRELATION_MATRIX, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.COVARIANCE_MATRIX, target.toSpecification(), desiredValue.getConstraints().withoutAny(ValuePropertyNames.FUNCTION)));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = inputs.keySet().iterator().next();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.CORRELATION_MATRIX, target.toSpecification(), input.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, getUniqueId()).get()));
  }

  // FunctionInvoker

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext context, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final DoubleLabelledMatrix2D input = (DoubleLabelledMatrix2D) inputs.getValue(ValueRequirementNames.COVARIANCE_MATRIX);
    final DoubleLabelledMatrix2D output = new DoubleLabelledMatrix2D(input.getXKeys(), input.getXLabels(), input.getYKeys(), input.getYLabels(), input.getValues());
    // TODO: This is a really dumb way to do this. There should be something in OG-Analytics or OG-Maths that will do this faster. This is a crude mechanism to
    // transform the covariance matrix to something that is more easily displayed
    final double[][] values = output.getValues();
    final double[] stddev = new double[values.length];
    for (int i = 0; i < values.length; i++) {
      stddev[i] = Math.sqrt(values[i][i]);
    }
    for (int i = 0; i < values.length; i++) {
      final double[] v = values[i];
      final double a = stddev[i];
      for (int j = 0; j < v.length; j++) {
        v[j] /= a * stddev[j];
      }
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.CORRELATION_MATRIX, target.toSpecification(), desiredValue.getConstraints()), output));
  }

}
