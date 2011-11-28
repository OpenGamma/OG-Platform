/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PV01Calculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.security.FinancialSecurity;

/**
 * 
 */
public class InterestRateInstrumentPV01Function extends InterestRateInstrumentFunction {

  /**
   * The value name calculated by this function.
   */
  public static final String VALUE_REQUIREMENT = ValueRequirementNames.PV01;

  private static final PV01Calculator CALCULATOR = PV01Calculator.getInstance();

  public InterestRateInstrumentPV01Function() {
    super(VALUE_REQUIREMENT);
  }

  @Override
  public Set<ComputedValue> getComputedValues(final InstrumentDerivative derivative, final YieldCurveBundle bundle,
      final FinancialSecurity security, final ComputationTarget target, final String forwardCurveName, final String fundingCurveName) {
    final Map<String, Double> pv01 = CALCULATOR.visit(derivative, bundle);
    Set<ComputedValue> result = new HashSet<ComputedValue>();
    result.add(new ComputedValue(getResultSpec(target, fundingCurveName), pv01.containsKey(fundingCurveName) ? pv01.get(fundingCurveName) : 0));
    result.add(new ComputedValue(getResultSpec(target, forwardCurveName), pv01.containsKey(forwardCurveName) ? pv01.get(forwardCurveName) : 0));
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName) {
    return Sets.newHashSet(getResultSpec(target, forwardCurveName), getResultSpec(target, fundingCurveName));
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final String curveName) {
    final ValueProperties properties = createValueProperties(target).with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties);
  }

}
