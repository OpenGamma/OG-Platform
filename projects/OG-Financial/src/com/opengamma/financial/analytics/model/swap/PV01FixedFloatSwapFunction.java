/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.interestrate.PV01Calculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class PV01FixedFloatSwapFunction extends FixedFloatSwapFunction {

  private static final PV01Calculator CALCULATOR = PV01Calculator.getInstance();

  public PV01FixedFloatSwapFunction() {
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final FunctionInputs inputs, final Security security, final Swap<?, ?> swap, final YieldCurveBundle bundle, final String forwardCurveName,
      final String fundingCurveName) {
    final Map<String, Double> pv01ForCurve = CALCULATOR.visit(swap, bundle);
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    if (!(pv01ForCurve.containsKey(forwardCurveName) && pv01ForCurve.containsKey(fundingCurveName))) {
      throw new NullPointerException("Could not get PV01 for " + forwardCurveName + " and " + fundingCurveName);
    }
    final ValueProperties props = createValueProperties().with(ValuePropertyNames.CURRENCY, getCurrencyForTarget(security).getCode()).with(YieldCurveFunction.PROPERTY_FORWARD_CURVE,
        forwardCurveName).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).get();
    for (final Map.Entry<String, Double> entry : pv01ForCurve.entrySet()) {
      result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PV01, security), props.copy().with(ValuePropertyNames.CURVE, entry.getKey()).get()), entry
          .getValue()));
    }
    return result;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(context, desiredValue);
    if (curveNames.getFirst().equals(curveNames.getSecond())) {
      return Collections.singleton(getCurveRequirement(target, curveNames.getFirst(), null, null));
    }
    return Sets.newHashSet(getCurveRequirement(target, curveNames.getFirst(), curveNames.getFirst(), curveNames.getSecond()), getCurveRequirement(target, curveNames.getSecond(),
        curveNames.getFirst(), curveNames.getSecond()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PV01, target.toSpecification(), createValueProperties().with(ValuePropertyNames.CURRENCY,
        getCurrencyForTarget(target).getCode()).withAny(ValuePropertyNames.CURVE).withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE).withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE).get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Pair<String, String> curveNames = YieldCurveFunction.getInputCurveNames(inputs);
    final ValueProperties props = createValueProperties().with(ValuePropertyNames.CURRENCY, getCurrencyForTarget(target).getCode()).with(YieldCurveFunction.PROPERTY_FORWARD_CURVE,
        curveNames.getFirst()).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveNames.getSecond()).get();
    if (curveNames.getFirst().equals(curveNames.getSecond())) {
      return Collections.singleton(new ValueSpecification(ValueRequirementNames.PV01, target.toSpecification(), props.copy().with(ValuePropertyNames.CURVE, curveNames.getFirst()).get()));
    } else {
      return Sets.newHashSet(
          new ValueSpecification(ValueRequirementNames.PV01, target.toSpecification(), props.copy().with(ValuePropertyNames.CURVE, curveNames.getFirst()).get()),
          new ValueSpecification(ValueRequirementNames.PV01, target.toSpecification(), props.copy().with(ValuePropertyNames.CURVE, curveNames.getSecond()).get()));
    }
  }

  @Override
  public String getShortName() {
    return "PV01FixedFloatSwapFunction";
  }

}
