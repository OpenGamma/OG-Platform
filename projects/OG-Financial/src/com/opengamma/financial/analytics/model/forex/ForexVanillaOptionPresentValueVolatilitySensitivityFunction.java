/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueVolatilitySensitivityBlackCalculator;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;

/**
 * 
 */
public class ForexVanillaOptionPresentValueVolatilitySensitivityFunction extends ForexVanillaOptionFunction {
  private static final PresentValueVolatilitySensitivityBlackCalculator CALCULATOR = PresentValueVolatilitySensitivityBlackCalculator.getInstance();

  public ForexVanillaOptionPresentValueVolatilitySensitivityFunction(final String putCurveName, final String callCurveName, final String surfaceName) {
    super(putCurveName, callCurveName, surfaceName, ValueRequirementNames.FX_VOLATILITY_SENSITIVITIES);
  }

  @Override
  protected Object getResult(final ForexDerivative fxOption, final SmileDeltaTermStructureDataBundle data) {
    return 0.2;
    //return CALCULATOR.visit(fxOption, data);
  }
}
