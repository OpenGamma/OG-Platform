/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.calculator.ModifiedDurationFromYieldCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the modified duration from bond yield.
 * @deprecated The parent class of this function is deprecated
 */
@Deprecated
public class BondModifiedDurationFromYieldFunction extends BondFromYieldFunction {

  @Override
  protected InstrumentDerivativeVisitorAdapter<Double, Double> getCalculator() {
    return ModifiedDurationFromYieldCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.MODIFIED_DURATION;
  }
}
