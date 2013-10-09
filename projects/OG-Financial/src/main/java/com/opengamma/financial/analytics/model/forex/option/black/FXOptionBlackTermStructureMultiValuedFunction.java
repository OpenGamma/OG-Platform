/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction.CALL_CURVE;
import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG;
import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction.PUT_CURVE;
import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.currency.CurrencyPair;

/**
 * Base class for FX option functions that use the Black method. The results do
 * not set the {@link ValuePropertyNames#CURRENCY} property.
 * @deprecated The parent class is deprecated
 */
@Deprecated
public abstract class FXOptionBlackTermStructureMultiValuedFunction extends FXOptionBlackTermStructureFunction {

  public FXOptionBlackTermStructureMultiValuedFunction(final String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, BLACK_TERM_STRUCTURE_METHOD)
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
        .with(ValuePropertyNames.CALCULATION_METHOD, BLACK_TERM_STRUCTURE_METHOD)
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
