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

/**
 * 
 */
public class BondFutureGrossBasisCalculator extends BondFutureCalculator {

  @Override
  public double[] calculate(final BondFuture bondFuture, final List<Double> deliveryDates, final List<Double> cleanPrices, final List<Double> accruedInterest, final List<Double> repoRates,
      final double futurePrice) {
    final Bond[] deliverableBonds = bondFuture.getBonds();
    final double[] conversionFactors = bondFuture.getConversionFactors();
    final int n = deliverableBonds.length;
    final double[] result = new double[n];
    Validate.isTrue(n == cleanPrices.size());
    for (int i = 0; i < n; i++) {
      result[i] = cleanPrices.get(i) - futurePrice * conversionFactors[i];
    }
    return result;
  }

}
