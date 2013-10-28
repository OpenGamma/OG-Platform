/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackVegaCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.core.position.Position;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingPositionVegaIRFutureOptionFunction;

/**
 * Function computes the {@link ValueRequirementNames#POSITION_VEGA}, first order derivative of {@link Position} price with respect to the Black Lognormal Implied Volatility,
 * for interest rate future options in the Black world. <p>
 * @deprecated Use {@link BlackDiscountingPositionVegaIRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackPositionVegaFunction extends InterestRateFutureOptionBlackFunction {

  /** The calculator to compute the vega value */
  private static final PresentValueBlackVegaCalculator CALCULATOR = PresentValueBlackVegaCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#POSITION_VEGA}
   */
  public InterestRateFutureOptionBlackPositionVegaFunction() {
    super(ValueRequirementNames.POSITION_VEGA, true);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle curveBundle, final ValueSpecification spec,
      final Set<ValueRequirement> desiredValues) {
    final double vega = irFutureOption.accept(CALCULATOR, curveBundle);
    return Collections.singleton(new ComputedValue(spec, vega));
  }

}
