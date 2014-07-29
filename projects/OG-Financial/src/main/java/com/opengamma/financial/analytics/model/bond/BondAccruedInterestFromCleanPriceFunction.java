/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.calculator.AccruedInterestFromCleanPriceCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the accrued interest from bond yield.
 * @deprecated The parent of this class is deprecated.
 * Use {@link com.opengamma.financial.analytics.model.bondcleanprice.BondAccruedInterestFromCleanPriceFunction}
 */
@Deprecated
public class BondAccruedInterestFromCleanPriceFunction extends BondFromCleanPriceFunction {

  @Override
  protected InstrumentDerivativeVisitorAdapter<Double, Double> getCalculator() {
    return AccruedInterestFromCleanPriceCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.ACCRUED_INTEREST;
  }
}
