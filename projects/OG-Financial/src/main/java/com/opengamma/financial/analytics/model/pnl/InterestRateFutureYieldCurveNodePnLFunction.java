/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.UniqueId;

/**
 *
 */
public class InterestRateFutureYieldCurveNodePnLFunction extends YieldCurveNodePnLFunction {

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getPosition().getSecurity() instanceof InterestRateFutureSecurity;
  }

  @Override
  protected ValueRequirement getYCNSRequirement(final String currencyString, final String curveCalculationConfigName, final String yieldCurveName, final ComputationTarget target,
      final ValueProperties desiredValueProperties) {
    final UniqueId uniqueId = target.getPosition().getTrades().iterator().next().getUniqueId();
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURRENCY, currencyString)
        .with(ValuePropertyNames.CURVE_CURRENCY, currencyString)
        .with(ValuePropertyNames.CURVE, yieldCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, ComputationTargetType.TRADE, uniqueId, properties);
  }

//  protected ValueRequirement getCurrencyConversionTSRequirement(final Position position, final String currencyString, final Set<String> resultCurrencies) {
//    final String resultCurrency = Iterables.getOnlyElement(resultCurrencies);
//    if (!resultCurrency.equals(currencyString)) {
//      final ValueProperties.Builder properties = ValueProperties.builder();
//      properties.with(ValuePropertyNames.CURRENCY, resultCurrencies);
//      final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(position.getTrade().getSecurity());
//      return new ValueRequirement(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES, targetSpec, properties.get());
//    }
//    return null;
//  }
}
