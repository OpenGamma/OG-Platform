/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.calculator.DirtyPriceFromYieldCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the dirty price from bond yield.
 * @deprecated This class uses deprecated analytics functions.
 */
@Deprecated
public class BondDirtyPriceFromYieldFunction extends BondFromYieldFunction {

  @Override
  protected InstrumentDerivativeVisitorAdapter<Double, Double> getCalculator() {
    return DirtyPriceFromYieldCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.DIRTY_PRICE;
  }

  @Override
  protected double getScaleFactor() {
    return 100;
  }

}
