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
import com.opengamma.analytics.financial.interestrate.PresentValueBlackGammaCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingPositionGammaIRFutureOptionFunction;

/**
 * Function computes the {@link ValueRequirementNames#POSITION_GAMMA}, second order derivative of position price with respect to the futures rate,
 * for interest rate future options in the Black world.
 * @deprecated Use {@link BlackDiscountingPositionGammaIRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackPositionGammaFunction extends InterestRateFutureOptionBlackFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionBlackPositionGammaFunction.class);
  /** The calculator to compute the gamma value. */
  private static final PresentValueBlackGammaCalculator CALCULATOR = PresentValueBlackGammaCalculator.getInstance();

  /**
   * Sets the value requirement names to {@link ValueRequirementNames#POSITION_GAMMA}
   */
  public InterestRateFutureOptionBlackPositionGammaFunction() {
    super(ValueRequirementNames.POSITION_GAMMA, true);
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
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec,
      final Set<ValueRequirement> desiredValues) {
    // Compute gamma with unit scaling. Remember that future price will be quoted like 0.9965, not 99.65
    final Double gamma = irFutureOption.accept(CALCULATOR, data);
    // Add scaling and adjust properties to reflect
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Set<String> scaleValue = desiredValue.getConstraints().getValues(ValuePropertyNames.SCALE);
    final Double scaleFactor;
    final String scaleProperty;
    if (scaleValue == null || scaleValue.isEmpty()) {
      scaleProperty = Double.toString(1);
      scaleFactor = 1.0;
    } else {
      scaleProperty = Iterables.getOnlyElement(scaleValue);
      scaleFactor = Double.parseDouble(scaleProperty);
    }
    final ValueProperties properties = spec.getProperties().copy().withoutAny(ValuePropertyNames.SCALE).with(ValuePropertyNames.SCALE, scaleProperty).get();
    final ValueSpecification specWithScale = new ValueSpecification(spec.getValueName(), spec.getTargetSpecification(), properties);

    return Collections.singleton(new ComputedValue(specWithScale, gamma * scaleFactor * scaleFactor));
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String currency) {
    return super.getResultProperties(currency)
        .withAny(ValuePropertyNames.SCALE);
  }

}
