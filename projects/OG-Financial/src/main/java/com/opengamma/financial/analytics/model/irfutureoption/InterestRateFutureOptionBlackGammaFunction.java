/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackGammaCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingGammaIRFutureOptionFunction;

/**
 * Function computes the {@link ValueRequirementNames#GAMMA}, second order derivative of position price with respect to the futures rate,
 * for interest rate future options in the Black world.
 * @deprecated Use {@link BlackDiscountingGammaIRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackGammaFunction extends InterestRateFutureOptionBlackFunction {

  /**
   * The calculator to compute the gamma value.
   */
  private static final PresentValueBlackGammaCalculator CALCULATOR = PresentValueBlackGammaCalculator.getInstance();

  public InterestRateFutureOptionBlackGammaFunction() {
    super(ValueRequirementNames.GAMMA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOptionTransaction, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec,
      final Set<ValueRequirement> desiredValues) {
    final InstrumentDerivative irFutureOptionSecurity = ((InterestRateFutureOptionMarginTransaction) irFutureOptionTransaction).getUnderlyingOption();
    final double gamma = irFutureOptionSecurity.accept(CALCULATOR, data);  
    //final double gamma = irFutureOptionTransaction.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, gamma));
  }
}
