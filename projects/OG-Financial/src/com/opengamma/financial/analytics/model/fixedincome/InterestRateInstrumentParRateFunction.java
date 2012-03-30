/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.FinancialSecurity;

/**
 * 
 */
public class InterestRateInstrumentParRateFunction extends InterestRateInstrumentFunction {
  private static final ParRateCalculator CALCULATOR = ParRateCalculator.getInstance();

  public InterestRateInstrumentParRateFunction() {
    super(ValueRequirementNames.PAR_RATE);
  }

  @Override
  public Set<ComputedValue> getComputedValues(final InstrumentDerivative derivative, final YieldCurveBundle bundle,
      final FinancialSecurity security, final ComputationTarget target, final String forwardCurveName, final String fundingCurveName,
      final String curveCalculationMethod, final String currency) {
    final Double parRate = CALCULATOR.visit(derivative, bundle);
    return Collections.singleton(new ComputedValue(getResultSpec(target, forwardCurveName, fundingCurveName, curveCalculationMethod,
        currency), parRate));
  }

}
