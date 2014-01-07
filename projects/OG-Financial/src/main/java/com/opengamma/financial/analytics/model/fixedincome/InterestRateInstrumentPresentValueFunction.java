/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.discounting.DiscountingPVFunction;
import com.opengamma.financial.security.FinancialSecurity;

/**
 * Function that calculated the present value.
 * @deprecated Use {@link DiscountingPVFunction}
 */
@Deprecated
public class InterestRateInstrumentPresentValueFunction extends InterestRateInstrumentFunction {
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();

  public InterestRateInstrumentPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  public Set<ComputedValue> getComputedValues(final InstrumentDerivative derivative, final YieldCurveBundle bundle, final FinancialSecurity security, final ComputationTarget target,
      final String curveCalculationConfigName, final String currency) {
    final Double presentValue = derivative.accept(CALCULATOR, bundle);
    return Collections.singleton(new ComputedValue(getResultSpec(target, curveCalculationConfigName, currency), presentValue));
  }

}
