/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;

/**
 * 
 */
public abstract class ForexForwardMultiValuedFunction extends ForexForwardFunction {

  public ForexForwardMultiValuedFunction(final String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(PAY_CURVE_CALC_CONFIG)
        .withAny(RECEIVE_CURVE_CALC_CONFIG);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String payCurveName, final String receiveCurveName, final String payCurveCalculationConfig, final String receiveCurveCalculationConfig,
      final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(PAY_CURVE_CALC_CONFIG, payCurveCalculationConfig)
        .with(RECEIVE_CURVE_CALC_CONFIG, receiveCurveCalculationConfig);
  }
}
