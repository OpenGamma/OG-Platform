/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import static com.opengamma.engine.value.ValuePropertyNames.FUNCTION;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.curve.NodalObjectsCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class ISDACreditSpreadCurveShiftFunction extends AbstractFunction.NonCompiledInvoker {
  private static final String PREFIX = "Shifted ";
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueSpecification input = Iterables.getOnlyElement(inputs.getAllValues()).getSpecification();
    final Object creditSpreadCurveObject = inputs.getValue(ValueRequirementNames.CREDIT_SPREAD_CURVE);
    if (creditSpreadCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get credit spread curve");
    }
    @SuppressWarnings("unchecked")
    final NodalObjectsCurve<Tenor, Double> creditSpreadCurve = (NodalObjectsCurve<Tenor, Double>) creditSpreadCurveObject;
    final String shiftProperty = desiredValue.getConstraint(PROPERTY_SPREAD_CURVE_SHIFT);
    final double shift = Double.parseDouble(shiftProperty);
    final Tenor[] xs = creditSpreadCurve.getXData();
    final Double[] ys = creditSpreadCurve.getYData();
    final Double[] shiftedYs = new Double[ys.length];
    for (int i = 0; i < ys.length; i++) {
      shiftedYs[i] = ys[i] + shift;
    }
    final String name = PREFIX + creditSpreadCurve.getName();
    final NodalObjectsCurve<Tenor, Double> shiftedCurve = NodalObjectsCurve.from(xs, shiftedYs, name);
    final ValueProperties properties = createValueProperties(input).with(PROPERTY_SPREAD_CURVE_SHIFT, shiftProperty).get();
    return Collections.singleton(new ComputedValue(new ValueSpecification(input.getValueName(), input.getTargetSpecification(), properties), shiftedCurve));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.CREDIT_SPREAD_CURVE, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> shifts = constraints.getValues(PROPERTY_SPREAD_CURVE_SHIFT);
    if (shifts == null || shifts.isEmpty()) {
      return null;
    }
    final ValueProperties properties = constraints.copy()
        .withoutAny(PROPERTY_SPREAD_CURVE_SHIFT)
        .get();
    return Collections.singleton(new ValueRequirement(desiredValue.getValueName(), target.toSpecification(), properties));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = Iterables.getOnlyElement(inputs.keySet());
    final ValueProperties properties = createValueProperties(input).withAny(PROPERTY_SPREAD_CURVE_SHIFT).get();
    return Collections.singleton(new ValueSpecification(input.getValueName(), input.getTargetSpecification(), properties));
  }

  private ValueProperties.Builder createValueProperties(final ValueSpecification input) {
    return input.getProperties().copy().withoutAny(FUNCTION).with(FUNCTION, getUniqueId());
  }

}
