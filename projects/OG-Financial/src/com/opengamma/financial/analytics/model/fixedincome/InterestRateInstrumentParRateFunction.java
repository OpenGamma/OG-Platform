/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InterestRateInstrumentParRateFunction extends InterestRateInstrumentFunction {
  private static final ParRateCalculator CALCULATOR = ParRateCalculator.getInstance();
  private static final String VALUE_REQUIREMENT = ValueRequirementNames.PAR_RATE;

  public InterestRateInstrumentParRateFunction(String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  public Set<ComputedValue> getComputedValues(InterestRateDerivative derivative, YieldCurveBundle bundle,
      FinancialSecurity security, Pair<String, String> curveNames) {
    final Double parRate = derivative.accept(CALCULATOR, bundle);
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(
        VALUE_REQUIREMENT, security), FixedIncomeInstrumentCurveExposureHelper.getValuePropertiesForSecurity(security,
        curveNames.getSecond(), curveNames.getFirst()));
    return Collections.singleton(new ComputedValue(specification, parRate));
  }

  @Override
  public String getShortName() {
    return "InterestRateInstrumentParRateFunction";
  }
}
