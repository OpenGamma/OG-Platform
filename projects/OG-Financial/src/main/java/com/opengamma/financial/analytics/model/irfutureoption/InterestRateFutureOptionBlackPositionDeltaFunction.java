/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackDeltaForTransactionCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingPositionDeltaIRFutureOptionFunction;

/**
 * Function computes the {@link ValueRequirementNames#POSITION_DELTA}, first order derivative of {@link Position} price with respect to the futures price,
 * for interest rate future options in the Black world. <p>
 * @deprecated Use {@link BlackDiscountingPositionDeltaIRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackPositionDeltaFunction extends InterestRateFutureOptionBlackFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionBlackPositionDeltaFunction.class);
  /** The calculator to compute the delta value */
  private static final PresentValueBlackDeltaForTransactionCalculator CALCULATOR = PresentValueBlackDeltaForTransactionCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#POSITION_DELTA}
   */
  public InterestRateFutureOptionBlackPositionDeltaFunction() {
    super(ValueRequirementNames.POSITION_DELTA, true);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    // First, confirm Scale is set, by user or by default
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> scale = constraints.getValues(ValuePropertyNames.SCALE);
    if (scale == null || scale.size() != 1) {
      s_logger.info("Could not find {} requirement. Looking for a default..", ValuePropertyNames.SCALE);
      return null;
    }
    // Then get typical requirements
    return super.getRequirements(context, target, desiredValue);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle curveBundle,
                                          final ValueSpecification spec, final Set<ValueRequirement> desiredValues) {
    // Compute delta with unit scaling. Remember that future price will be quoted like 0.9965, not 99.65
    final double delta = irFutureOption.accept(CALCULATOR, curveBundle);
    // Add scaling and adjust properties to reflect
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Set<String> scaleValue = desiredValue.getConstraints().getValues(ValuePropertyNames.SCALE);
    String scaleProperty = Double.toString(1);
    Double scaleFactor = 1.0;
    if (scaleValue != null && scaleValue.size() > 0) {
      scaleProperty = Iterables.getOnlyElement(scaleValue);
      scaleFactor = Double.parseDouble(scaleProperty);
    }
    final ValueProperties properties = spec.getProperties().copy().withoutAny(ValuePropertyNames.SCALE).with(ValuePropertyNames.SCALE, scaleProperty).get();
    final ValueSpecification specWithScale = new ValueSpecification(spec.getValueName(), spec.getTargetSpecification(), properties);

    return Collections.singleton(new ComputedValue(specWithScale, delta * scaleFactor));
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String currency) {
    return super.getResultProperties(currency)
        .withAny(ValuePropertyNames.SCALE);
  }

}
