/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;

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
        .withAny(FXOptionBlackFunction.PUT_CURVE)
        .withAny(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG)
        .withAny(FXOptionBlackFunction.CALL_CURVE)
        .withAny(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .with(ValuePropertyNames.CURRENCY, getResultCurrency(target))
        .withAny(PROPERTY_CALL_SPREAD_VALUE);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    return desiredValue.getConstraints().copy()
        .withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId());
  }

  protected static String getResultCurrency(final ComputationTarget target) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    if (security instanceof FXDigitalOptionSecurity) {
      return ((FXDigitalOptionSecurity) target.getSecurity()).getPaymentCurrency().getCode();
    } else if (security instanceof NonDeliverableFXDigitalOptionSecurity) {
      return ((NonDeliverableFXDigitalOptionSecurity) target.getSecurity()).getPaymentCurrency().getCode();
    }
    throw new OpenGammaRuntimeException("Can only handle FX digitals and non-deliverable FX digitals; should never happen");
  }
}
