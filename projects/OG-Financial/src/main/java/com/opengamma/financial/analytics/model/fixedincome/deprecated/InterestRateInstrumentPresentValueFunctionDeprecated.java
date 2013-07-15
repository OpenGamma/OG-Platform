/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentPresentValueFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see InterestRateInstrumentPresentValueFunction
 */
@Deprecated
public class InterestRateInstrumentPresentValueFunctionDeprecated extends InterestRateInstrumentFunctionDeprecated {
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();

  public InterestRateInstrumentPresentValueFunctionDeprecated() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  public Set<ComputedValue> getComputedValues(final InstrumentDerivative derivative, final YieldCurveBundle bundle, final FinancialSecurity security,
      final ComputationTarget target, final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethod, final String currency) {
    Double presentValue = derivative.accept(CALCULATOR, bundle);
    if (security instanceof BondSecurity) {
      final BondSecurity bondSec = (BondSecurity) security;
      presentValue *= bondSec.getParAmount();
    }
    return Collections.singleton(new ComputedValue(getResultSpec(target, forwardCurveName, fundingCurveName, curveCalculationMethod, currency), presentValue));
  }

}
