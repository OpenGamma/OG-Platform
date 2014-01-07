/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.discounting.DiscountingFunction;
import com.opengamma.financial.currency.CurrencyPair;

/**
 * Base class for functions that calculate values for FX forward where the currency
 * property is not set.
 * @deprecated Use {@link DiscountingFunction}
 */
@Deprecated
public abstract class FXForwardMultiValuedFunction extends FXForwardFunction {

  public FXForwardMultiValuedFunction(final String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String payCurveName, final String receiveCurveName,
      final String payCurveCalculationConfig, final String receiveCurveCalculationConfig, final CurrencyPair baseQuotePair) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final String payCurveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    final String receiveCurveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig);
  }
}
