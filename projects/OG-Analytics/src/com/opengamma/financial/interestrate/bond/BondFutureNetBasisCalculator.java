/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
  public double[] calculate(final BondFuture bondFuture, final BondFutureDeliverableBasketDataBundle basketData, final double futurePrice) {
    Validate.notNull(bondFuture, "bond future");
    Validate.notNull(basketData, "basket data");
    Validate.isTrue(futurePrice > 0, "future price must be positive");
    final BondForward[] deliverableBonds = bondFuture.getBondForwards();
    final int n = deliverableBonds.length;
    Validate.isTrue(n == basketData.getBasketSize());
    final double[] cleanPrices = basketData.getCleanPrices();
    final double[] repoRates = basketData.getRepoRates();
    final double[] conversionFactors = bondFuture.getConversionFactors();
    final double[] result = new double[n];
    System.out.println("NET BASIS CALCULATOR");
    for (int i = 0; i < n; i++) {
      final double invoicePrice = futurePrice * conversionFactors[i] + deliverableBonds[i].getAccruedInterestAtDelivery();
      final double forwardDirtyPrice = BOND_FORWARD_CALCULATOR.calculate(deliverableBonds[i], cleanPrices[i], repoRates[i]);
      System.out.println("future price\t" + futurePrice);
      System.out.println("conversion factor\t" + conversionFactors[i]);
      System.out.println("bond forward accrued interest\t" + deliverableBonds[i].getAccruedInterestAtDelivery());
      System.out.println("bond accrued interest\t" + deliverableBonds[i].getBond().getAccruedInterest());
      System.out.println("forward dirty price\t" + forwardDirtyPrice);
      System.out.println("--------------------------------------------------------------------");
      result[i] = forwardDirtyPrice - invoicePrice;
    }
    return result;
  }

}
