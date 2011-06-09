/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PV01Calculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.security.FinancialSecurity;

/**
 * 
 */
public class InterestRateInstrumentPV01Function extends InterestRateInstrumentFunction {
  private static final PV01Calculator CALCULATOR = PV01Calculator.getInstance();
  private static final String VALUE_REQUIREMENT = ValueRequirementNames.PV01;

  public InterestRateInstrumentPV01Function(String fundingCurveName, String forwardCurveName) {
    super(VALUE_REQUIREMENT, fundingCurveName, forwardCurveName);
  }

  @Override
  public Set<ComputedValue> getComputedValues(InterestRateDerivative derivative, YieldCurveBundle bundle,
      FinancialSecurity security, String forwardCurveName, String fundingCurveName) {
    Map<String, Double> pv01 = CALCULATOR.visit(derivative, bundle);
    String[] relevantCurves = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, fundingCurveName, forwardCurveName);
    if (relevantCurves.length < pv01.size()) {
      throw new OpenGammaRuntimeException("Have more curves in calculation result than in the list of relevant curves: should never happen");
    }
    Set<ComputedValue> result = new HashSet<ComputedValue>();
    for (String relevantCurve : relevantCurves) {
      final ValueSpecification specification = new ValueSpecification(new ValueRequirement(VALUE_REQUIREMENT, security),
          FixedIncomeInstrumentCurveExposureHelper.getValuePropertiesForSecurity(
              security, fundingCurveName, forwardCurveName, createValueProperties()));
      result.add(new ComputedValue(specification, pv01.get(relevantCurve)));
    }
    return result;
  }
}
