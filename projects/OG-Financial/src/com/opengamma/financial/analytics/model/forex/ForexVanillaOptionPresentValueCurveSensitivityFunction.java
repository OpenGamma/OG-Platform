/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueCurveSensitivityBlackForexCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;

/**
 * 
 */
public class ForexVanillaOptionPresentValueCurveSensitivityFunction extends ForexVanillaOptionFunction {
  private static final PresentValueCurveSensitivityBlackForexCalculator CALCULATOR = PresentValueCurveSensitivityBlackForexCalculator.getInstance();

  public ForexVanillaOptionPresentValueCurveSensitivityFunction(final String putCurveName, final String callCurveName, final String surfaceName) {
    super(putCurveName, callCurveName, surfaceName, ValueRequirementNames.FX_CURVE_SENSITIVITIES);
  }

  @Override
  protected Object getResult(final ForexDerivative fxOption, final SmileDeltaTermStructureDataBundle data) {
    final PresentValueSensitivity result = CALCULATOR.visit(fxOption, data);
    return 0.7;
  }
}
