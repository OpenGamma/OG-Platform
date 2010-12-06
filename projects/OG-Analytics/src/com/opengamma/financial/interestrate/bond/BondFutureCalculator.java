/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;

/**
 * 
 */
public class BondFutureCalculator {
  private static final BondCalculator DIRTY_PRICE_CALCULATOR = BondCalculatorFactory.getCalculator(BondCalculatorFactory.DIRTY_PRICE);

  /**
   * For reasons best known to themselves, repo rates are quoted on a simple interest basis 
   * @param bond The bond (from a bond deliverable basket)
   * @param deliveryDate The date on which the bond is to be delivered 
   * @param cleanPrice  The clean price of the bond
   * @param futurePrice The price of the bond future
   * @param conversionFactor The conversion factor of the bond
   * @param accruedInterest The accrued interest of the bond
   * @return The repo rate (simple interest as a fraction)
   */
  public static double impliedRepoRate(final Bond bond, final double deliveryDate, final double cleanPrice, final double futurePrice, final double conversionFactor, final double accruedInterest) {
    final GenericAnnuity<FixedCouponPayment> coupons = bond.getCouponAnnuity();
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

    final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(bond, cleanPrice);
    final double invoicePrice = futurePrice * conversionFactor + accruedInterest;
    return (invoicePrice - dirtyPrice + sum1) / (dirtyPrice * deliveryDate - sum2);
  }

  public static double netBasis(final Bond bond, final double deliveryDate, final double cleanPrice, final double futurePrice, final double conversionFactor, final double accruedInterest,
      final double repoRate) {
    final double invoicePrice = futurePrice * conversionFactor + accruedInterest;
    final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(bond, cleanPrice);
    final double fdp = BondPriceCalculator.forwardDirtyPrice(bond, dirtyPrice, deliveryDate, repoRate);
    return fdp - invoicePrice;
  }

  /**
   * The gross basis (for deliverable bonds only) is defined as gross basis = clean bond price - (futures price * conversion factor). See Choudhry M, Bond & Money Markets, p726 
   * @param cleanPrice The current quoted price of the deliverable bond
   * @param futurePrice The current quoted price of the bond futures contract 
   * @param conversionFactor The fixed convention factor for the bond 
   * @return The gross basis 
   */
  public static double grossBasis(final double cleanPrice, final double futurePrice, final double conversionFactor) {
    return cleanPrice - futurePrice * conversionFactor;
  }

}
