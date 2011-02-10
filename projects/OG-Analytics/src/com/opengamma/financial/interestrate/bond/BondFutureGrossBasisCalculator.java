/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.bond.definition.BondForward;
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
  public double[] calculate(final BondFuture bondFuture, final BondFutureDeliverableBasketDataBundle basketData) {
    Validate.notNull(bondFuture, "bond future");
    Validate.notNull(basketData, "basket data");
    final BondForward[] deliverableBonds = bondFuture.getBondForwards();
    final int n = deliverableBonds.length;
    Validate.isTrue(n == basketData.getBasketSize());
    final double[] conversionFactors = bondFuture.getConversionFactors();
    final double[] result = new double[n];
    final double[] cleanPrices = basketData.getCleanPrices();
    for (int i = 0; i < n; i++) {
      result[i] = cleanPrices[i] - bondFuture.getPrice() * conversionFactors[i];
    }
    return result;
  }

}
