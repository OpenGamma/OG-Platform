/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.forward.FXForwardFunction;
import com.opengamma.financial.currency.PnlSeriesCurrencyConversionFunction;
import com.opengamma.financial.security.fx.FXForwardSecurity;

/**
 *
 */
public class FXForwardYCNSPnLCurrencyConversionFunction extends PnlSeriesCurrencyConversionFunction {

  public FXForwardYCNSPnLCurrencyConversionFunction(final String currencyMatrixName) {
    super(currencyMatrixName);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return super.canApplyTo(context, target) && (target.getPositionOrTrade().getSecurity() instanceof FXForwardSecurity);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(FXForwardFunction.PAY_CURVE_CALC_CONFIG)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(FXForwardFunction.RECEIVE_CURVE_CALC_CONFIG)
        .withAny(ValuePropertyNames.CURRENCY)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .with(YieldCurveNodePnLFunction.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
  }

}
