/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.option.black.ForexOptionBlackFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class FXDigitalCallSpreadBlackSingleValuedFunction extends FXDigitalCallSpreadBlackFunction {

  public FXDigitalCallSpreadBlackSingleValuedFunction(final String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CALL_SPREAD_BLACK_METHOD)
        .withAny(ForexOptionBlackFunction.PUT_CURVE)
        .withAny(ForexOptionBlackFunction.PUT_CURVE_CALC_CONFIG)
        .withAny(ForexOptionBlackFunction.CALL_CURVE)
        .withAny(ForexOptionBlackFunction.CALL_CURVE_CALC_CONFIG)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .with(ValuePropertyNames.CURRENCY, getResultCurrency(target))
        .withAny(PROPERTY_CALL_SPREAD_VALUE);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String putCurveName, final String callCurveName, final String putCurveConfig, final String callCurveConfig,
      final String surfaceName, final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName, final String spread, final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CALL_SPREAD_BLACK_METHOD)
        .with(ForexOptionBlackFunction.PUT_CURVE, putCurveName)
        .with(ForexOptionBlackFunction.PUT_CURVE_CALC_CONFIG, putCurveConfig)
        .with(ForexOptionBlackFunction.CALL_CURVE, callCurveName)
        .with(ForexOptionBlackFunction.CALL_CURVE_CALC_CONFIG, callCurveConfig)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName)
        .with(PROPERTY_CALL_SPREAD_VALUE, spread)
        .with(ValuePropertyNames.CURRENCY, getResultCurrency(target));
  }

  protected static String getResultCurrency(final ComputationTarget target) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    if (security instanceof FXDigitalOptionSecurity) {
      return ((FXDigitalOptionSecurity) target.getSecurity()).getPaymentCurrency().getCode();
    }
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    Currency ccy;
    if (FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency)) {
      ccy = callCurrency;
    } else {
      ccy = putCurrency;
    }
    return ccy.getCode();
  }
}
