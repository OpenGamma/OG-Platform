/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;

/**
 * 
 */
public abstract class FXOptionBlackMultiValuedFunction extends FXOptionBlackFunction {

  public FXOptionBlackMultiValuedFunction(final String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, BLACK_METHOD)
        .withAny(PUT_CURVE)
        .withAny(PUT_CURVE_CALC_CONFIG)
        .withAny(CALL_CURVE)
        .withAny(CALL_CURVE_CALC_CONFIG)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String putCurveName, final String callCurveName, final String putCurveConfig, final String callCurveConfig,
      final String surfaceName, final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName, final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, BLACK_METHOD)
        .with(PUT_CURVE, putCurveName)
        .with(PUT_CURVE_CALC_CONFIG, putCurveConfig)
        .with(CALL_CURVE, callCurveName)
        .with(CALL_CURVE_CALC_CONFIG, callCurveConfig)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName);
  }

}
