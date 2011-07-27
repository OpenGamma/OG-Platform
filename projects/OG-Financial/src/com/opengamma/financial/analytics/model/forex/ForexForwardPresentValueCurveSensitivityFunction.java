/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueCurveSensitivityForexCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * 
 */
public class ForexForwardPresentValueCurveSensitivityFunction extends ForexForwardFunction {
  private static final PresentValueCurveSensitivityForexCalculator CALCULATOR = PresentValueCurveSensitivityForexCalculator.getInstance();

  public ForexForwardPresentValueCurveSensitivityFunction(final String payCurveName, final String receiveCurveName) {
    super(payCurveName, receiveCurveName, ValueRequirementNames.FX_CURVE_SENSITIVITIES);
  }

  @Override
  protected Object getResult(final ForexDerivative fxForward, final YieldCurveBundle data) {
    return CALCULATOR.visit(fxForward, data);
  }

}
