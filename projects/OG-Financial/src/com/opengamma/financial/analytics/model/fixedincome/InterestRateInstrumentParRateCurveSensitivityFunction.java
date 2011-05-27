/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InterestRateInstrumentParRateCurveSensitivityFunction extends InterestRateInstrumentFunction {
  private static final ParRateCurveSensitivityCalculator CALCULATOR = ParRateCurveSensitivityCalculator.getInstance();
  private static final String VALUE_REQUIREMENT = ValueRequirementNames.PAR_RATE_CURVE_SENSITIVITY;

  public InterestRateInstrumentParRateCurveSensitivityFunction(String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  public Set<ComputedValue> getComputedValues(InterestRateDerivative derivative, YieldCurveBundle bundle,
      FinancialSecurity security, Pair<String, String> curveNames) {
    Map<String, List<DoublesPair>> results = derivative.accept(CALCULATOR, bundle);
    return null;
  }

}
