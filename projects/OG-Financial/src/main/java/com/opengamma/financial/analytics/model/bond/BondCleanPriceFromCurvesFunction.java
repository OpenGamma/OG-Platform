/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.calculator.CleanPriceFromCurvesCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class BondCleanPriceFromCurvesFunction extends BondFromCurvesFunction {

  @Override
  protected InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> getCalculator() {
    return CleanPriceFromCurvesCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.CLEAN_PRICE;
  }

  @Override
  protected double getScaleFactor() {
    return 100;
  }
}
