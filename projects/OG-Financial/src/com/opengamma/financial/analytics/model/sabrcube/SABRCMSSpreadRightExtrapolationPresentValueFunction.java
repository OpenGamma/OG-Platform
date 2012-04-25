/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRExtrapolationCalculator;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class SABRCMSSpreadRightExtrapolationPresentValueFunction extends SABRCMSSpreadRightExtrapolationFunction {
  private static final PresentValueCalculator CALCULATOR = PresentValueSABRExtrapolationCalculator.getInstance();

  @Override
  protected Object getResult(final InstrumentDerivative derivative, final SABRInterestRateDataBundle data) {
    return CALCULATOR.visit(derivative, data);
  }

  @Override
  protected String getValueRequirement() {
    return ValueRequirementNames.PRESENT_VALUE;
  }
}
