/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward.deprecated;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.analytics.model.forex.forward.ForexForwardMultiValuedFunction;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see ForexForwardMultiValuedFunction
 */
@Deprecated
public abstract class ForexForwardMultiValuedFunctionDeprecated extends ForexForwardFunctionDeprecated {

  public ForexForwardMultiValuedFunctionDeprecated(final String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ForexForwardFunctionDeprecated.PROPERTY_PAY_FORWARD_CURVE)
        .withAny(ForexForwardFunctionDeprecated.PROPERTY_PAY_CURVE_CALCULATION_METHOD)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(ForexForwardFunctionDeprecated.PROPERTY_RECEIVE_FORWARD_CURVE)
        .withAny(ForexForwardFunctionDeprecated.PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String payCurveName, final String payForwardCurveName, final String payCurveCalculationMethod,
      final String receiveCurveName, final String receiveForwardCurveName, final String receiveCurveCalculationMethod, final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ForexForwardFunctionDeprecated.PROPERTY_PAY_FORWARD_CURVE, payForwardCurveName)
        .with(ForexForwardFunctionDeprecated.PROPERTY_PAY_CURVE_CALCULATION_METHOD, payCurveCalculationMethod)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ForexForwardFunctionDeprecated.PROPERTY_RECEIVE_FORWARD_CURVE, receiveForwardCurveName)
        .with(ForexForwardFunctionDeprecated.PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD, receiveCurveCalculationMethod);
  }
}
