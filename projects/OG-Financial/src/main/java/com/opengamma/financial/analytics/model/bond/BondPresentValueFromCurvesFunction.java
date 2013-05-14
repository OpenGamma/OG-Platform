/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Bond present value from curves.
 */
public class BondPresentValueFromCurvesFunction extends BondFromCurvesFunction {

  @Override
  protected InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> getCalculator() {
    return PresentValueCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.PRESENT_VALUE;
  }

}
