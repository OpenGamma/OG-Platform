/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.BondFutureDeliverableBasketDataBundle;
import com.opengamma.financial.interestrate.payments.CouponFixed;

/**
 * 
 */
public final class BondFutureImpliedRepoRateCalculator extends BondFutureCalculator {
  private static final BondCalculator DIRTY_PRICE_CALCULATOR = BondCalculatorFactory.getBondCalculator(BondCalculatorFactory.BOND_DIRTY_PRICE);
  private static final BondFutureImpliedRepoRateCalculator INSTANCE = new BondFutureImpliedRepoRateCalculator();

  private BondFutureImpliedRepoRateCalculator() {
  }

  public static BondFutureImpliedRepoRateCalculator getInstance() {
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
      final double deliveryDate = deliverableBonds[i].getForwardTime();
      final GenericAnnuity<CouponFixed> coupons = deliverableBonds[i].getBond().getCouponAnnuity();
      double sum1 = 0.0;
      double sum2 = 0.0;
      for (final CouponFixed payments : coupons.getPayments()) {
        final double ti = payments.getPaymentTime();
        if (ti > deliveryDate) {
          break;
        }
        sum1 += payments.getAmount();
        sum2 += payments.getAmount() * (deliveryDate - ti); //TODO are the times right?
      }
      final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(deliverableBonds[i].getBond(), cleanPrices[i]);
      final double invoicePrice = bondFuture.getPrice() * conversionFactors[i] + deliverableBonds[i].getAccruedInterestAtDelivery();
      result[i] = (invoicePrice - dirtyPrice + sum1) / (dirtyPrice * deliveryDate - sum2);
    }
    return result;
  }
}
