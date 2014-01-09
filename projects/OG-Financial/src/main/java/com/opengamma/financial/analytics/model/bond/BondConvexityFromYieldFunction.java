/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.calculator.ConvexityFromYieldCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the convexity from bond yield.
 * @deprecated The parent of this class is deprecated.
 */
@Deprecated
public class BondConvexityFromYieldFunction extends BondFromYieldFunction {

  @Override
  protected InstrumentDerivativeVisitorAdapter<Double, Double> getCalculator() {
    return ConvexityFromYieldCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.CONVEXITY;
  }
}
