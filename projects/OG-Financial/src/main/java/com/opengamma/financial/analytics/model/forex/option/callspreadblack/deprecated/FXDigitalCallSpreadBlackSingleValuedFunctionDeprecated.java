/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack.deprecated;

import static com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackFunctionDeprecated.PROPERTY_CALL_CURVE;
import static com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackFunctionDeprecated.PROPERTY_CALL_CURVE_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackFunctionDeprecated.PROPERTY_CALL_FORWARD_CURVE;
import static com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackFunctionDeprecated.PROPERTY_PUT_CURVE;
import static com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackFunctionDeprecated.PROPERTY_PUT_CURVE_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackFunctionDeprecated.PROPERTY_PUT_FORWARD_CURVE;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.FXUtils;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.option.callspreadblack.FXDigitalCallSpreadBlackSingleValuedFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.util.money.Currency;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see FXDigitalCallSpreadBlackSingleValuedFunction
 */
@Deprecated
public abstract class FXDigitalCallSpreadBlackSingleValuedFunctionDeprecated extends FXDigitalCallSpreadBlackFunctionDeprecated {

  public FXDigitalCallSpreadBlackSingleValuedFunctionDeprecated(final String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CALL_SPREAD_BLACK_METHOD)
        .withAny(PROPERTY_PUT_CURVE)
        .withAny(PROPERTY_PUT_FORWARD_CURVE)
        .withAny(PROPERTY_PUT_CURVE_CALCULATION_METHOD)
        .withAny(PROPERTY_CALL_CURVE)
        .withAny(PROPERTY_CALL_FORWARD_CURVE)
        .withAny(PROPERTY_CALL_CURVE_CALCULATION_METHOD)
        .withAny(PROPERTY_CALL_SPREAD_VALUE)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .withAny(ValuePropertyNames.SURFACE)
        .with(ValuePropertyNames.CURRENCY, getResultCurrency(target));
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String putCurveName, final String putForwardCurveName, final String putCurveCalculationMethod, final String callCurveName,
      final String callForwardCurveName, final String callCurveCalculationMethod, final String surfaceName, final String spread,
      final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName, final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CALL_SPREAD_BLACK_METHOD)
        .with(PROPERTY_PUT_CURVE, putCurveName)
        .with(PROPERTY_PUT_FORWARD_CURVE, putForwardCurveName)
        .with(PROPERTY_PUT_CURVE_CALCULATION_METHOD, putCurveCalculationMethod)
        .with(PROPERTY_CALL_CURVE, callCurveName)
        .with(PROPERTY_CALL_FORWARD_CURVE, callForwardCurveName)
        .with(PROPERTY_CALL_CURVE_CALCULATION_METHOD, callCurveCalculationMethod)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(PROPERTY_CALL_SPREAD_VALUE, spread)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName)
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
