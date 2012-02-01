/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 * 
 */
public class InterestRateInstrumentPresentValueFunction extends InterestRateInstrumentFunction {

  /**
   * The value name calculated by this function.
   */
  public static final String VALUE_REQUIREMENT = ValueRequirementNames.PRESENT_VALUE;

  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();

  public InterestRateInstrumentPresentValueFunction() {
    super(VALUE_REQUIREMENT);
  }

  @Override
  public Set<ComputedValue> getComputedValues(final InstrumentDerivative derivative, final YieldCurveBundle bundle,
      final FinancialSecurity security, final ComputationTarget target, final String forwardCurveName, final String fundingCurveName) {
    Double presentValue = CALCULATOR.visit(derivative, bundle);
    if (security instanceof BondSecurity) {
      BondSecurity bondSec = (BondSecurity) security;
      presentValue = presentValue * bondSec.getParAmount();
    }
    return Collections.singleton(new ComputedValue(getResultSpec(target, forwardCurveName, fundingCurveName), presentValue));
  }

}
