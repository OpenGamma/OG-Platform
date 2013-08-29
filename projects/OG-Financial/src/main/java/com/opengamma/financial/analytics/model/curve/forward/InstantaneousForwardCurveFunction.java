/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.INSTANTANEOUS_FORWARD_CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class InstantaneousForwardCurveFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) inputs.getValue(YIELD_CURVE);
    final int n = 1000;
    final double[] xData = new double[n];
    final double[] yData = new double[n];
    for (int i = 0; i < n; i++) {
      final double t = i / 40.;
      xData[i] = t;
      yData[i] = curve.getForwardRate(t);
    }
    final NodalDoublesCurve forwardCurve = NodalDoublesCurve.from(xData, yData);
    final ValueProperties properties = Iterables.getOnlyElement(desiredValues).getConstraints();
    final ValueSpecification spec = new ValueSpecification(INSTANTANEOUS_FORWARD_CURVE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, forwardCurve));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(CURVE)
        .withAny(PROPERTY_CURVE_TYPE)
        .withAny(CURVE_CONSTRUCTION_CONFIG)
        .get();
    return Collections.singleton(new ValueSpecification(INSTANTANEOUS_FORWARD_CURVE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveNames = constraints.getValues(CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      return null;
    }
    final Set<String> curveTypes = constraints.getValues(PROPERTY_CURVE_TYPE);
    if (curveTypes == null || curveTypes.size() != 1) {
      return null;
    }
    final Set<String> curveCalculationConfiguration = constraints.getValues(CURVE_CONSTRUCTION_CONFIG);
    if (curveCalculationConfiguration == null || curveCalculationConfiguration.size() != 1) {
      return null;
    }
    final ValueProperties curveProperties = ValueProperties
        .with(CURVE, curveNames)
        .with(PROPERTY_CURVE_TYPE, curveTypes)
        .with(CURVE_CONSTRUCTION_CONFIG, curveCalculationConfiguration)
        .get();
    return Collections.singleton(new ValueRequirement(YIELD_CURVE, target.toSpecification(), curveProperties));
  }

}
