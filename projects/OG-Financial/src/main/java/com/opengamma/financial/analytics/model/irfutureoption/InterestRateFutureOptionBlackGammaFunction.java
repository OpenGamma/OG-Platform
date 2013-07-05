/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackGammaCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Function computes the {@link ValueRequirementNames#GAMMA}, second order derivative of position price with respect to the futures rate,
 * for interest rate future options in the Black world.
 */
public class InterestRateFutureOptionBlackGammaFunction extends InterestRateFutureOptionBlackFunction {

  /**
   * The calculator to compute the gamma value.
   */
  private static final PresentValueBlackGammaCalculator CALCULATOR = PresentValueBlackGammaCalculator.getInstance();

  public InterestRateFutureOptionBlackGammaFunction() {
    super(ValueRequirementNames.GAMMA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec) {
    final Double gamma = irFutureOption.accept(CALCULATOR, data) / 1e8; // REVIEW: jim 31-Aug-2012 - represents change in PV01 per basis-point now.
    return Collections.singleton(new ComputedValue(spec, gamma));
  }

}
