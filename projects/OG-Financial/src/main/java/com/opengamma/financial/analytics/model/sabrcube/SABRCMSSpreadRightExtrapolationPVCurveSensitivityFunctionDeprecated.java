/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivitySABRExtrapolationCalculator;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see SABRCMSSpreadRightExtrapolationPVCurveSensitivityFunction
 */
@Deprecated
public class SABRCMSSpreadRightExtrapolationPVCurveSensitivityFunctionDeprecated extends SABRCMSSpreadRightExtrapolationFunctionDeprecated {

  @Override
  protected String getValueRequirement() {
    return ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY;
  }

  @Override
  protected Object getResult(final InstrumentDerivative derivative, final SABRInterestRateDataBundle data, final ValueRequirement desiredValue) {
    final Double cutoff = Double.parseDouble(desiredValue.getConstraint(SABRRightExtrapolationFunctionDeprecated.PROPERTY_CUTOFF_STRIKE));
    final Double mu = Double.parseDouble(desiredValue.getConstraint(SABRRightExtrapolationFunctionDeprecated.PROPERTY_TAIL_THICKNESS_PARAMETER));
    final PresentValueCurveSensitivitySABRExtrapolationCalculator calculator = new PresentValueCurveSensitivitySABRExtrapolationCalculator(cutoff, mu);
    return derivative.accept(calculator, data);
  }

}
