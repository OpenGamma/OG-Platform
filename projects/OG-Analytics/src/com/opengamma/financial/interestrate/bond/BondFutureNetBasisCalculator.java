/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.future.definition.BondFuture;

/**
 * 
 */
public final class BondFutureNetBasisCalculator extends BondFutureCalculator {
  private static final BondForwardCalculator BOND_FORWARD_CALCULATOR = BondForwardDirtyPriceCalculator.getInstance();
  private static final BondCalculator DIRTY_PRICE_CALCULATOR = BondCalculatorFactory.getBondCalculator(BondCalculatorFactory.BOND_DIRTY_PRICE);
  private static final BondFutureNetBasisCalculator INSTANCE = new BondFutureNetBasisCalculator();

  private BondFutureNetBasisCalculator() {
  }

  public static BondFutureNetBasisCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public double[] calculate(final BondFuture bondFuture, final List<Double> deliveryDates, final List<Double> cleanPrices, final List<Double> accruedInterest, final List<Double> repoRates,
      final double futurePrice) {
    Validate.notNull(bondFuture, "bond future");
    Validate.noNullElements(deliveryDates, "delivery dates");
    Validate.noNullElements(cleanPrices, "clean prices");
    Validate.noNullElements(accruedInterest, "accrued interest");
    Validate.noNullElements(repoRates, "repo rates");
    Validate.isTrue(futurePrice > 0, "future price must be positive");
    final Bond[] deliverableBonds = bondFuture.getBonds();
    final int n = deliverableBonds.length;
    Validate.isTrue(deliveryDates.size() == n, "there must be a delivery date for each bond");
    Validate.isTrue(cleanPrices.size() == n, "there must be a clean price for each bond");
    Validate.isTrue(accruedInterest.size() == n, "there must be a value for accrued interest for each bond");
    Validate.isTrue(repoRates.size() == n, "there must be a repo rate for each bond");
    final double[] conversionFactors = bondFuture.getConversionFactors();
    final double[] result = new double[n];
    Bond deliverable;
    BondForward forward;
    for (int i = 0; i < n; i++) {
      deliverable = deliverableBonds[i];
      forward = new BondForward(deliverable, deliveryDates.get(i));
      final double invoicePrice = futurePrice * conversionFactors[i] + accruedInterest.get(i);
      final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(deliverable, cleanPrices.get(i));
      final double forwardDirtyPrice = BOND_FORWARD_CALCULATOR.calculate(forward, dirtyPrice, repoRates.get(i));
      result[i] = forwardDirtyPrice - invoicePrice;
    }
    return result;
  }

}
