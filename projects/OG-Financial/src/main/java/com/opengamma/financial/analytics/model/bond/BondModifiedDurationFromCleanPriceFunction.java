/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.calculator.ModifiedDurationFromCleanPriceCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the modified duration from bond clean price.
 * @deprecated The parent of this class is deprecated.
 * Use {@link com.opengamma.financial.analytics.model.bondcleanprice.BondModifiedDurationFromCleanPriceFunction}
 */
@Deprecated
public class BondModifiedDurationFromCleanPriceFunction extends BondFromCleanPriceFunction {

  @Override
  protected InstrumentDerivativeVisitorAdapter<Double, Double> getCalculator() {
    return ModifiedDurationFromCleanPriceCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.MODIFIED_DURATION;
  }
}
