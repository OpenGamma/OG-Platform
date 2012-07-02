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
import com.opengamma.financial.analytics.model.forex.option.black.ForexOptionBlackFunction;

/**
 * 
 */
public abstract class FXDigitalCallSpreadBlackMultiValuedFunction extends FXDigitalCallSpreadBlackFunction {

  public FXDigitalCallSpreadBlackMultiValuedFunction(final String valueRequirementName) {
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
        .with(PROPERTY_CALL_SPREAD_VALUE, spread);
  }

}
