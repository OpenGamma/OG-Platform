/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.BondFutureDeliverableBasketDataBundle;

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
  public double[] calculate(final BondFuture bondFuture, final BondFutureDeliverableBasketDataBundle basketData, final double futurePrice) {
    Validate.notNull(bondFuture, "bond future");
    Validate.notNull(basketData, "basket data");
    Validate.isTrue(futurePrice > 0, "future price must be positive");
    final Bond[] deliverableBonds = bondFuture.getBonds();
    final int n = deliverableBonds.length;
    Validate.isTrue(n == basketData.getSize());
    final List<Double> deliveryDates = basketData.getDeliveryDates();
    final List<Double> cleanPrices = basketData.getCleanPrices();
    final List<Double> accruedInterest = basketData.getAccruedInterest();
    final List<Double> repoRates = basketData.getRepoRates();
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
