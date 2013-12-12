/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.calculator.YieldFromCleanPriceCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the yield from bond clean price.
 * @deprecated The parent of this class is deprecated.
 * Use {@link com.opengamma.financial.analytics.model.bondcleanprice.BondYieldFromCleanPriceFunction}
 */
@Deprecated
public class BondYieldFromCleanPriceFunction extends BondFromCleanPriceFunction {

  @Override
  protected InstrumentDerivativeVisitorAdapter<Double, Double> getCalculator() {
    return YieldFromCleanPriceCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.YTM;
  }

}
