/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see SABRNoExtrapolationPVCurveSensitivityFunction
 */
@Deprecated
public class SABRNoExtrapolationPVCurveSensitivityFunctionDeprecated extends SABRNoExtrapolationFunctionDeprecated {
  private static final PresentValueCurveSensitivitySABRCalculator CALCULATOR = PresentValueCurveSensitivitySABRCalculator.getInstance();

  @Override
  protected String getValueRequirement() {
    return ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY;
  }

  @Override
  protected Object getResult(final InstrumentDerivative derivative, final SABRInterestRateDataBundle data, final ValueRequirement desiredValue) {
    return derivative.accept(CALCULATOR, data);
  }

}
