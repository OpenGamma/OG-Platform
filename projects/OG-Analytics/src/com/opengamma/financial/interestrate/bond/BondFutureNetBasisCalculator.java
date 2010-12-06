/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import java.util.List;

import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.future.definition.BondFuture;

/**
 * 
 */
public class BondFutureNetBasisCalculator extends BondFutureCalculator {
  private static final BondForwardCalculator BOND_FORWARD_CALCULATOR = new BondForwardDirtyPriceCalculator();
  private static final BondCalculator DIRTY_PRICE_CALCULATOR = BondCalculatorFactory.getBondCalculator(BondCalculatorFactory.BOND_DIRTY_PRICE);

  @Override
  public double[] calculate(final BondFuture bondFuture, final List<Double> deliveryDates, final List<Double> cleanPrices, final List<Double> accruedInterest, final List<Double> repoRates,
      final double futurePrice) {
    final Bond[] deliverableBonds = bondFuture.getBonds();
    final double[] conversionFactors = bondFuture.getConversionFactors();
    final int n = deliverableBonds.length;
    final double[] result = new double[n];
    Bond deliverable;
    BondForward forward;
    for (int i = 0; i < n; i++) {
      deliverable = deliverableBonds[i];
      forward = new BondForward(deliverable, deliveryDates.get(i));
      final double invoicePrice = futurePrice * conversionFactors[i] + accruedInterest.get(i);
      final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(deliverable, cleanPrices.get(i));
      final double fdp = BOND_FORWARD_CALCULATOR.calculate(forward, dirtyPrice, repoRates.get(i));
      result[i] = fdp - invoicePrice;
    }
    return result;
  }

}
