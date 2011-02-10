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
public final class BondFutureNetBasisCalculator extends BondFutureCalculator {
  private static final BondForwardCalculator BOND_FORWARD_CALCULATOR = BondForwardDirtyPriceCalculator.getInstance();
  private static final BondFutureNetBasisCalculator INSTANCE = new BondFutureNetBasisCalculator();

  private BondFutureNetBasisCalculator() {
  }

  public static BondFutureNetBasisCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public double[] calculate(final BondFuture bondFuture, final BondFutureDeliverableBasketDataBundle basketData) {
    Validate.notNull(bondFuture, "bond future");
    Validate.notNull(basketData, "basket data");
    final BondForward[] deliverableBonds = bondFuture.getBondForwards();
    final int n = deliverableBonds.length;
    Validate.isTrue(n == basketData.getBasketSize());
    final double[] cleanPrices = basketData.getCleanPrices();
    final double[] repoRates = basketData.getRepoRates();
    final double[] conversionFactors = bondFuture.getConversionFactors();
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      final double invoicePrice = bondFuture.getPrice() * conversionFactors[i] + deliverableBonds[i].getAccruedInterestAtDelivery();
      final double forwardDirtyPrice = BOND_FORWARD_CALCULATOR.calculate(deliverableBonds[i], cleanPrices[i], repoRates[i]);
      result[i] = forwardDirtyPrice - invoicePrice;
    }
    return result;
  }

}
