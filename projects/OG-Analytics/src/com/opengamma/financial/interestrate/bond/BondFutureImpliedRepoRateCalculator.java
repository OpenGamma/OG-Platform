/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.BondFutureDeliverableBasketDataBundle;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;

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
  public double[] calculate(final BondFuture bondFuture, final BondFutureDeliverableBasketDataBundle basketData, final double futurePrice) {
    Validate.notNull(bondFuture, "bond future");
    Validate.notNull(basketData, "basket data");
    Validate.isTrue(futurePrice > 0, "future price must be positive");
    final Bond[] deliverableBonds = bondFuture.getBonds();
    final int n = deliverableBonds.length;
    Validate.isTrue(n == basketData.getSize());
    final double[] conversionFactors = bondFuture.getConversionFactors();
    final double[] result = new double[n];
    final List<Double> deliveryDates = basketData.getDeliveryDates();
    final List<Double> cleanPrices = basketData.getCleanPrices();
    final List<Double> accruedInterest = basketData.getAccruedInterest();
    Bond deliverable;
    for (int i = 0; i < n; i++) {
      deliverable = deliverableBonds[i];
      final double deliveryDate = deliveryDates.get(i);
      final GenericAnnuity<FixedCouponPayment> coupons = deliverable.getCouponAnnuity();
      double sum1 = 0.0;
      double sum2 = 0.0;
      for (final FixedCouponPayment payments : coupons.getPayments()) {
        final double ti = payments.getPaymentTime();
        if (ti > deliveryDate) {
          break;
        }
        sum1 += payments.getAmount();
        sum2 += payments.getAmount() * (deliveryDate - ti); // TODO this should be done on a daycount basis
      }
      final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(deliverable, cleanPrices.get(i));
      final double invoicePrice = futurePrice * conversionFactors[i] + accruedInterest.get(i);
      result[i] = (invoicePrice - dirtyPrice + sum1) / (dirtyPrice * deliveryDate - sum2);
    }
    return result;
  }
}
