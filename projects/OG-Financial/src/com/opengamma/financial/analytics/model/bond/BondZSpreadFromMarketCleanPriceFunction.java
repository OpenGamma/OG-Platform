/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

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
    final String riskFreeCurveName = riskFreeCurves.iterator().next();
    final String curveName = curves.iterator().next();
    return Sets.newHashSet(getCurveRequirement(target, riskFreeCurveName), getCurveRequirement(target, curveName), getCleanPriceRequirement(target, desiredValue));
  }

  @Override
  protected ValueRequirement getCleanPriceRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, target.getSecurity().getUniqueId());
  }

  @Override
  protected ValueProperties.Builder getResultProperties() {
    return createValueProperties()
        .withAny(BondFunction.PROPERTY_RISK_FREE_CURVE)
        //.withAny(BondFunction.PROPERTY_CREDIT_CURVE)
        .withAny(ValuePropertyNames.CURVE)
        .with(ValuePropertyNames.CALCULATION_METHOD, BondFunction.FROM_CLEAN_PRICE_METHOD);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String riskFreeCurveName, final String curveName) {
    return createValueProperties()
        .with(BondFunction.PROPERTY_RISK_FREE_CURVE, riskFreeCurveName)
        //.withAny(BondFunction.PROPERTY_CREDIT_CURVE)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CALCULATION_METHOD, BondFunction.FROM_CLEAN_PRICE_METHOD);
  }
}
