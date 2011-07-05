/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class NotApplicableYieldCurveNodeSensitivitiesFunction extends InterestRateInstrumentYieldCurveNodeSensitivitiesFunction {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(desiredValues);
    final String forwardCurveName = curveNames.getFirst();
    final String fundingCurveName = curveNames.getSecond();
    if (fundingCurveName.equals(forwardCurveName)) {
      return getSensitivitiesForSingleCurve(security, curveNames.getFirst());
    }
    return getSensitivitiesForMultipleCurves(security, forwardCurveName, fundingCurveName);
  }

  private Set<ComputedValue> getSensitivitiesForSingleCurve(final FinancialSecurity security, final String curveName) {
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, security),
        FixedIncomeInstrumentCurveExposureHelper.getValuePropertiesForSecurity(security, curveName, curveName, createValueProperties()));
    return Collections.singleton(new ComputedValue(specification, new DoubleLabelledMatrix1D(new Double[0], new double[0])));
  }

  //TODO at some point this needs to deal with more than two curves
  private Set<ComputedValue> getSensitivitiesForMultipleCurves(final FinancialSecurity security, final String forwardCurveName, final String fundingCurveName) {
    final String[] relevantCurvesForDerivative = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security,
        fundingCurveName, forwardCurveName);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    for (final String curveName : relevantCurvesForDerivative) {
      results.addAll(getSensitivitiesForSingleCurve(security, curveName));
    }
    return results;
  }
}
