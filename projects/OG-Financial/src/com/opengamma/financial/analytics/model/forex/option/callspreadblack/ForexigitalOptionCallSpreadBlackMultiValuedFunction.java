/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack;


import static com.opengamma.financial.analytics.model.forex.option.black.ForexOptionBlackFunction.PROPERTY_CALL_CURVE;
import static com.opengamma.financial.analytics.model.forex.option.black.ForexOptionBlackFunction.PROPERTY_CALL_CURVE_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.forex.option.black.ForexOptionBlackFunction.PROPERTY_CALL_FORWARD_CURVE;
import static com.opengamma.financial.analytics.model.forex.option.black.ForexOptionBlackFunction.PROPERTY_PUT_CURVE;
import static com.opengamma.financial.analytics.model.forex.option.black.ForexOptionBlackFunction.PROPERTY_PUT_CURVE_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.forex.option.black.ForexOptionBlackFunction.PROPERTY_PUT_FORWARD_CURVE;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.analytics.model.InterpolatedCurveAndSurfaceProperties;

/**
 * 
 */
public abstract class ForexigitalOptionCallSpreadBlackMultiValuedFunction extends ForexDigitalOptionCallSpreadBlackFunction {

  public ForexigitalOptionCallSpreadBlackMultiValuedFunction(final String valueRequirementName) {
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
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(InterpolatedCurveAndSurfaceProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME);
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
        .with(InterpolatedCurveAndSurfaceProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName);
  }

}
