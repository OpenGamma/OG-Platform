/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class BondZSpreadFromMarketCleanPriceFunction extends BondZSpreadFunction {

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> riskFreeCurves = desiredValue.getConstraints().getValues(BondFunction.PROPERTY_RISK_FREE_CURVE);
    if (riskFreeCurves == null || riskFreeCurves.size() != 1) {
      return null;
    }
    final Set<String> curves = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if (curves == null || curves.size() != 1) {
      return null;
    }
    final Set<String> creditCurves = desiredValue.getConstraints().getValues(BondFunction.PROPERTY_CREDIT_CURVE);
    if (creditCurves == null || creditCurves.size() != 1) {
      return null;
    }
    final String riskFreeCurveName = riskFreeCurves.iterator().next();
    final String curveName = curves.iterator().next();
    return Sets.newHashSet(getCurveRequirement(target, riskFreeCurveName), getCurveRequirement(target, curveName), getCleanPriceRequirement(target, desiredValue));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String curveName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (ValueRequirementNames.YIELD_CURVE.equals(input.getKey().getValueName())) {
        curveName = input.getKey().getProperty(ValuePropertyNames.CURVE);
      }
    }
    assert curveName != null;
    final String riskFreeCurveName = curveName;
    final String creditCurveName = riskFreeCurveName;
    final ValueProperties.Builder properties = getResultProperties(riskFreeCurveName, creditCurveName, curveName);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.Z_SPREAD, target.toSpecification(), properties.get()));
  }

  @Override
  protected ValueRequirement getCleanPriceRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    return new ValueRequirement(ValueRequirementNames.MARKET_CLEAN_PRICE, ComputationTargetType.SECURITY, target.getSecurity().getUniqueId());
  }

  @Override
  protected String getCalculationMethodName() {
    return BondFunction.FROM_CLEAN_PRICE_METHOD;
  }

}
