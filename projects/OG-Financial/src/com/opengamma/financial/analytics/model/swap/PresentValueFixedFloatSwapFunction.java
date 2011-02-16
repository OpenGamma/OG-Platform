/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class PresentValueFixedFloatSwapFunction extends FixedFloatSwapFunction {
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();

  public PresentValueFixedFloatSwapFunction() {
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final FunctionInputs inputs, final Security security, final Swap<?, ?> swap, final YieldCurveBundle bundle, final String forwardCurveName,
      final String fundingCurveName) {
    final Double presentValue = CALCULATOR.visit(swap, bundle);
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE, security), createValueProperties().with(ValuePropertyNames.CURRENCY,
        getCurrencyForTarget(security).getISOCode()).with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).get());
    return Collections.singleton(new ComputedValue(specification, presentValue));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String forwardCurveName = YieldCurveFunction.getForwardCurveName(context, desiredValue);
    final String fundingCurveName = YieldCurveFunction.getFundingCurveName(context, desiredValue);
    if (forwardCurveName.equals(fundingCurveName)) {
      return Collections.singleton(getCurveRequirement(target, forwardCurveName, null, null));
    }
    return Sets.newHashSet(getCurveRequirement(target, forwardCurveName, forwardCurveName, fundingCurveName), getCurveRequirement(target, fundingCurveName, forwardCurveName, fundingCurveName));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(
        new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), createValueProperties().with(ValuePropertyNames.CURRENCY, getCurrencyForTarget(target).getISOCode())
            .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE).withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE).get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Pair<String, String> curveNames = YieldCurveFunction.getInputCurveNames(inputs);
    return Collections
        .singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), createValueProperties().with(ValuePropertyNames.CURRENCY,
            getCurrencyForTarget(target).getISOCode()).with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, curveNames.getFirst()).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveNames.getSecond())
            .get()));
  }

  @Override
  public String getShortName() {
    return "PresentValueFixedFloatSwapFunction";
  }

}
