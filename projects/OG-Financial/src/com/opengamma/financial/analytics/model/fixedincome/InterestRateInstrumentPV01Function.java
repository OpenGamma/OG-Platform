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
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PV01Calculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InterestRateInstrumentPV01Function extends InterestRateInstrumentFunction {
  private static final PV01Calculator CALCULATOR = PV01Calculator.getInstance();
  private static final String VALUE_REQUIREMENT = ValueRequirementNames.PV01;

  public InterestRateInstrumentPV01Function(final String forwardCurveName, final String fundingCurveName) {
    super(forwardCurveName, fundingCurveName, VALUE_REQUIREMENT);
  }

  @Override
  public Set<ComputedValue> getComputedValues(final InstrumentDerivative derivative, final YieldCurveBundle bundle,
      final FinancialSecurity security, final ComputationTarget target) {
    final Map<String, Double> pv01 = CALCULATOR.visit(derivative, bundle);
    final Currency ccy = FinancialSecurityUtils.getCurrency(security);
    Set<ComputedValue> result = new HashSet<ComputedValue>();
    result.add(new ComputedValue(getFundingCurveSpec(ccy, target), pv01.containsKey(getFundingCurveName()) ? pv01.get(getFundingCurveName()) : 0));
    result.add(new ComputedValue(getForwardCurveSpec(ccy, target), pv01.containsKey(getForwardCurveName()) ? pv01.get(getForwardCurveName()) : 0));
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    return Sets.newHashSet(getFundingCurveSpec(ccy, target), getForwardCurveSpec(ccy, target));
  }

  private ValueSpecification getFundingCurveSpec(final Currency ccy, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
      .with(ValuePropertyNames.CURVE, getFundingCurveName())
      .with(ValuePropertyNames.CURRENCY, ccy.getCode())
      .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode()).get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), properties);
  }

  private ValueSpecification getForwardCurveSpec(final Currency ccy, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
      .with(ValuePropertyNames.CURVE, getForwardCurveName())
      .with(ValuePropertyNames.CURRENCY, ccy.getCode())
      .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode()).get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), properties);
  }
}
