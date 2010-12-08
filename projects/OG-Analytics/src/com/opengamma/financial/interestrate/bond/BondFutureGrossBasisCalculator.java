/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.BondFutureDeliverableBasketDataBundle;

/**
 * 
 */
public final class BondFutureGrossBasisCalculator extends BondFutureCalculator {
  private static final BondFutureGrossBasisCalculator INSTANCE = new BondFutureGrossBasisCalculator();

  private BondFutureGrossBasisCalculator() {
  }

  public static BondFutureGrossBasisCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public double[] calculate(final BondFuture bondFuture, final BondFutureDeliverableBasketDataBundle basketData, final double futurePrice) {
    Validate.notNull(bondFuture, "bond future");
    Validate.notNull(basketData, "basket data");
    Validate.isTrue(futurePrice > 0, "future price must be positive");
    final Bond[] deliverableBonds = bondFuture.getBonds();
    final int n = deliverableBonds.length;
    Validate.isTrue(n == basketData.getSize());
    final double[] conversionFactors = bondFuture.getConversionFactors();
    final double[] result = new double[n];
    final List<Double> cleanPrices = basketData.getCleanPrices();
    for (int i = 0; i < n; i++) {
      result[i] = cleanPrices.get(i) - futurePrice * conversionFactors[i];
    }
    return result;
  }

}
