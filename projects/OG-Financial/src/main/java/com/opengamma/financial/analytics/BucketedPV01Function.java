/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static com.google.common.collect.Sets.newHashSet;
import static com.opengamma.engine.value.ValueRequirementNames.BUCKETED_PV01;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
import static com.opengamma.lambdava.streams.Lambdava.functional;

import java.util.Collections;
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
import com.opengamma.lambdava.functions.Function3;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Generic function to compute the bucketed PV01 of XXX from the YieldCurveNodeSensitivity (scaling to 1 bp).
 */
public class BucketedPV01Function extends AbstractFunction.NonCompiledInvoker {

  private static final double RESCALE_FACTOR = 10000.0;

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext,
      final FunctionInputs inputs,
      ComputationTarget target,
      Set<ValueRequirement> desiredValues) throws AsynchronousExecution {

    DoubleLabelledMatrix1D matrix = (DoubleLabelledMatrix1D) inputs.getComputedValue(YIELD_CURVE_NODE_SENSITIVITIES).getValue();

    ValueRequirement desiredValue = functional(desiredValues).first();

    final double rescaleFactor;
    if (desiredValue.getConstraints().getSingleValue(ValuePropertyNames.SCALING_FACTOR) != null) {
      double scalingFactor = Double.parseDouble(desiredValue.getConstraint(ValuePropertyNames.SCALING_FACTOR));
      rescaleFactor = RESCALE_FACTOR / scalingFactor;
    } else {
      rescaleFactor = RESCALE_FACTOR;
    }
    LabelledMatrix1D<Double, Double> matrixDividedBy10k = matrix.mapValues(new Function3<Double, Double, Object, Double>() {
      @Override
      public Double execute(Double notUsed, Double value, Object notUsed2) {
        return value / rescaleFactor;
      }
    });

    ValueSpecification valueSpecification = ValueSpecification.of(desiredValue.getValueName(),
        target.toSpecification(),
        desiredValue.getConstraints());

    return newHashSet(new ComputedValue(valueSpecification, matrixDividedBy10k));
  }

  @Override
  public String getUniqueId() {
    return "Bucketed PV01 Function";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION_OR_TRADE;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    ValueProperties.Builder builder = ValueProperties.builder();
    builder.withOptional(ValuePropertyNames.SCALING_FACTOR);
    builder.with(ValuePropertyNames.FUNCTION, getUniqueId());
    ValueProperties valueProperties = builder.get();
    return Collections.singleton(
        new ValueSpecification(BUCKETED_PV01, target.toSpecification(), valueProperties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    ValueProperties.Builder builder = desiredValue.getConstraints().copy();
    builder.withoutAny(ValuePropertyNames.SCALING_FACTOR);
    ValueProperties valueProperties = builder.get();
    return Collections.singleton(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        target.toSpecification(), valueProperties));
  }

}
