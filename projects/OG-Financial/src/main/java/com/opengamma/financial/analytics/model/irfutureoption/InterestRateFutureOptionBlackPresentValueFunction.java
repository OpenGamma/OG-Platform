/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingPVIRFutureOptionFunction;

/**
 * Calculates the present value of interest rate future options using the Black formula.
 * @deprecated Use {@link BlackDiscountingPVIRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackPresentValueFunction extends InterestRateFutureOptionBlackFunction {
  /** The present value calculator */
  private static final PresentValueBlackCalculator CALCULATOR = PresentValueBlackCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#PRESENT_VALUE}
   */
  public InterestRateFutureOptionBlackPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE, true);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec,
      final Set<ValueRequirement> desiredValues) {
    final double pv = irFutureOption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, pv));
  }

}
