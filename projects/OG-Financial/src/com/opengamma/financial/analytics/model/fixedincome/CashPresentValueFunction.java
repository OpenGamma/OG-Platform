/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class CashPresentValueFunction extends FixedIncomeInstrumentFunction {
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof CashSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target,
      ValueRequirement desiredValue) {
    final String forwardCurveName = YieldCurveFunction.getForwardCurveName(context, desiredValue);
    final String fundingCurveName = YieldCurveFunction.getFundingCurveName(context, desiredValue);
    if (forwardCurveName.equals(fundingCurveName)) {
      return Collections.singleton(getCurveRequirement(target, forwardCurveName, null, null));
    }
    return Sets.newHashSet(getCurveRequirement(target, forwardCurveName, forwardCurveName, fundingCurveName),
        getCurveRequirement(target, fundingCurveName, forwardCurveName, fundingCurveName));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(
        new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), createValueProperties()
            .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
            .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE).withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
            .get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    final Pair<String, String> curveNames = YieldCurveFunction.getInputCurveNames(inputs);
    return Collections
        .singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(),
            createValueProperties().with(ValuePropertyNames.CURRENCY,
                FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
                .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, curveNames.getFirst())
                .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveNames.getSecond())
                .get()));
  }

  @Override
  protected InterestRateDerivative getDerivative(
      FinancialSecurityVisitorAdapter<FixedIncomeInstrumentConverter<?>> visitor,
      FinancialSecurity security, ZonedDateTime now, String fundingCurveName, String forwardCurveName) {
    return security.accept(visitor).toDerivative(now, fundingCurveName);
  }

  @Override
  protected Set<ComputedValue> getComputedValues(FunctionInputs inputs, Security security,
      InterestRateDerivative derivative, YieldCurveBundle bundle, String forwardCurveName,
      String fundingCurveName) {
    final Double presentValue = CALCULATOR.visit(derivative, bundle);
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(
        ValueRequirementNames.PRESENT_VALUE, security), createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(security).getCode())
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).get());
    return Collections.singleton(new ComputedValue(specification, presentValue));
  }

  @Override
  public String getShortName() {
    return "CashPresentValueFunction";
  }
}
