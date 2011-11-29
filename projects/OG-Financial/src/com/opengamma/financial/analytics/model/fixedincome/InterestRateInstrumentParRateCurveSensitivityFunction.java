/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class InterestRateInstrumentParRateCurveSensitivityFunction extends InterestRateInstrumentFunction {

  private static final ParRateCurveSensitivityCalculator CALCULATOR = ParRateCurveSensitivityCalculator.getInstance();

  public InterestRateInstrumentParRateCurveSensitivityFunction() {
    super(ValueRequirementNames.PAR_RATE_CURVE_SENSITIVITY);
  }

  @Override
  public Set<ComputedValue> getComputedValues(final InstrumentDerivative derivative, final YieldCurveBundle bundle,
      final FinancialSecurity security, final ComputationTarget target, final String forwardCurveName, final String fundingCurveName) {
    final Map<String, List<DoublesPair>> sensitivities = CALCULATOR.visit(derivative, bundle);
    return Collections.singleton(new ComputedValue(getResultSpec(target, forwardCurveName, fundingCurveName), sensitivities));

  }

}
