/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * Function to shift a yield curve, implemented using properties and constraints.
 */
public class YieldCurveShiftFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveShiftFunction.class);

  /**
   * Property to shift a yield curve.
   */
  protected static final String SHIFT = "SHIFT";

  @Override
  public String getShortName() {
    return "YieldCurveShift";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return true;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> shift = constraints.getValues(SHIFT);
    if ((shift == null) || shift.isEmpty() || constraints.isOptional(SHIFT)) {
      return null;
    }
    final ValueProperties properties = desiredValue.getConstraints().copy().withoutAny(SHIFT).with(SHIFT, "0").withOptional(SHIFT).get();
    return Collections.singleton(new ValueRequirement(desiredValue.getValueName(), desiredValue.getTargetSpecification(), properties));
  }

  private ValueProperties.Builder createValueProperties(final ValueSpecification input) {
    return input.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = inputs.keySet().iterator().next();
    final ValueProperties properties = createValueProperties(input).withAny(SHIFT).get();
    return Collections.singleton(new ValueSpecification(input.getValueName(), input.getTargetSpecification(), properties));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ComputedValue input = inputs.getAllValues().iterator().next();
    final ValueSpecification inputSpec = input.getSpecification();
    YieldAndDiscountCurve curve = (YieldAndDiscountCurve) input.getValue();
    final ValueProperties.Builder properties = createValueProperties(inputSpec);
    final String shift = desiredValues.iterator().next().getConstraint(SHIFT);
    properties.with(SHIFT, shift);
    // TODO: apply the shift operation to the yield curve
    s_logger.error("TODO: apply shift {} to yield curve {}", shift, curve);
    return Collections.singleton(new ComputedValue(new ValueSpecification(inputSpec.getValueName(), inputSpec.getTargetSpecification(), properties.get()), curve));
  }

}
