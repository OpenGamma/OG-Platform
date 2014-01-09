/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.calculator.ConvexityFromCleanPriceCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the convexity from bond clean price.
 * @deprecated The parent of this class is deprecated.
 * Use {@link com.opengamma.financial.analytics.model.bondcleanprice.BondConvexityFromCleanPriceFunction}
 */
@Deprecated
public class BondConvexityFromCleanPriceFunction extends BondFromCleanPriceFunction {

  @Override
  protected InstrumentDerivativeVisitorAdapter<Double, Double> getCalculator() {
    return ConvexityFromCleanPriceCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.CONVEXITY;
  }

}
