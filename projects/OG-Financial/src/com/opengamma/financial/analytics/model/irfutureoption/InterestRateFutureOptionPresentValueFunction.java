/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 * 
 */
public class InterestRateFutureOptionPresentValueFunction extends InterestRateFutureOptionFunction {
  private static final PresentValueSABRCalculator CALCULATOR = PresentValueSABRCalculator.getInstance();

  @Override
  protected Set<ComputedValue> getResults(final InstrumentDerivative irFutureOption, final SABRInterestRateDataBundle data,
      final ComputationTarget target, final FunctionInputs inputs, final String forwardCurveName,
      final String fundingCurveName, final String surfaceName) {
    final double presentValue = CALCULATOR.visit(irFutureOption, data);
    return Collections.singleton(new ComputedValue(getSpecification(target, forwardCurveName, fundingCurveName, surfaceName), presentValue));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(getSpecification(target));
  }

  private ValueSpecification getSpecification(final ComputationTarget target) {
    return new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(),
        createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.SURFACE)
        .with(ValuePropertyNames.SMILE_FITTING_METHOD, SURFACE_FITTING_NAME).get());
  }

  private ValueSpecification getSpecification(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName,
      final String surfaceName) {
    return new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(),
        createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.SMILE_FITTING_METHOD, SURFACE_FITTING_NAME).get());
  }
}
