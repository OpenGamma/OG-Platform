/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.black.BlackDiscountingFXOptionFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.util.money.Currency;

/**
 * Base class for FX option functions that use the Black method. The results
 * set the {@link ValuePropertyNames#CURRENCY} property.
 * @deprecated Use classes that descend from {@link BlackDiscountingFXOptionFunction}
 */
@Deprecated
public abstract class FXOptionBlackSingleValuedFunction extends FXOptionBlackFunction {

  /**
   * @param valueRequirementName The value requirement name, not null
   */
  public FXOptionBlackSingleValuedFunction(final String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .withAny(PUT_CURVE)
        .withAny(PUT_CURVE_CALC_CONFIG)
        .withAny(CALL_CURVE)
        .withAny(CALL_CURVE_CALC_CONFIG)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .withAny(ValuePropertyNames.CURRENCY);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String putCurve, final String putCurveCalculationConfig,
      final String callCurve, final String callCurveCalculationConfig, final CurrencyPair baseQuotePair, final ValueProperties optionalProperties) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .with(PUT_CURVE, putCurve)
        .with(PUT_CURVE_CALC_CONFIG, putCurveCalculationConfig)
        .with(CALL_CURVE, callCurve)
        .with(CALL_CURVE_CALC_CONFIG, callCurveCalculationConfig)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .with(ValuePropertyNames.CURRENCY, getResultCurrency(target, baseQuotePair));
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue, final CurrencyPair baseQuotePair) {
    final String putCurveName = desiredValue.getConstraint(PUT_CURVE);
    final String callCurveName = desiredValue.getConstraint(CALL_CURVE);
    final String putCurveConfig = desiredValue.getConstraint(PUT_CURVE_CALC_CONFIG);
    final String callCurveConfig = desiredValue.getConstraint(CALL_CURVE_CALC_CONFIG);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String interpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String leftExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String rightExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .with(PUT_CURVE, putCurveName)
        .with(PUT_CURVE_CALC_CONFIG, putCurveConfig)
        .with(CALL_CURVE, callCurveName)
        .with(CALL_CURVE_CALC_CONFIG, callCurveConfig)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName)
        .with(ValuePropertyNames.CURRENCY, getResultCurrency(target, baseQuotePair));
  }

  /**
   * Gets the currency of the result. This is necessary to set before the calculation is performed
   * because the engine tries to do a currency conversion if the currency is not set, leading to
   * mismatched result specifications.
   * @param target The target
   * @param baseQuotePair The base quote pair
   * @return The currency of the result
   */
  protected String getResultCurrency(final ComputationTarget target, final CurrencyPair baseQuotePair) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    if (security instanceof FXDigitalOptionSecurity) {
      return ((FXDigitalOptionSecurity) target.getSecurity()).getPaymentCurrency().getCode();
    } else if (security instanceof NonDeliverableFXDigitalOptionSecurity) {
      return ((NonDeliverableFXDigitalOptionSecurity) target.getSecurity()).getPaymentCurrency().getCode();
    }
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    if (baseQuotePair.getBase().equals(putCurrency)) {
      return callCurrency.getCode();
    }
    return putCurrency.getCode();
  }
}
