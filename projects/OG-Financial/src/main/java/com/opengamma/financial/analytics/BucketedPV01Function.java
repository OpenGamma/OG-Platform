/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static com.google.common.collect.Sets.newHashSet;
import static com.opengamma.engine.function.dsl.Function.function;
import static com.opengamma.engine.function.dsl.Function.input;
import static com.opengamma.engine.function.dsl.Function.output;
import static com.opengamma.engine.function.dsl.TargetSpecificationReference.originalTarget;
import static com.opengamma.engine.function.dsl.properties.RecordingValueProperties.copyFrom;
import static com.opengamma.engine.value.ValueRequirementNames.BUCKETED_PV01;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
import static com.opengamma.lambdava.streams.Lambdava.functional;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.dsl.FunctionSignature;
import com.opengamma.engine.function.dsl.functions.BaseNonCompiledInvoker;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.lambdava.functions.Function3;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Generic function to compute the bucketed PV01 of XXX from the YieldCurveNodeSensitivity (scaling to 1 bp).
 */
public class BucketedPV01Function extends BaseNonCompiledInvoker {

  private static final double RESCALE_FACTOR = 10000.0;

  @Override
  protected FunctionSignature functionSignature() {

    return function("Bucketed PV01", ComputationTargetType.POSITION_OR_TRADE)
        .outputs(
            output(BUCKETED_PV01)
                .targetSpec(originalTarget())
                .properties(copyFrom(YIELD_CURVE_NODE_SENSITIVITIES)
                    .withOptional(ValuePropertyNames.SCALING_FACTOR)
                    .withReplacement(ValuePropertyNames.FUNCTION, getUniqueId()))
        )
        .inputs(
            input(YIELD_CURVE_NODE_SENSITIVITIES)
                .properties(copyFrom(BUCKETED_PV01).withoutAny(ValuePropertyNames.SCALING_FACTOR))
                .targetSpec(originalTarget())
        );
  }

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

}
