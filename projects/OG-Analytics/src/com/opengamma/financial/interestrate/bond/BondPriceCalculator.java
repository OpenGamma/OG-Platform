/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.payments.FixedPayment;

/**
 * 
 */
public class BondPriceCalculator {

  public static double dirtyPrice(final Bond bond, final YieldCurveBundle curves) {
    return PresentValueCalculator.getInstance().visitBond(bond, curves);
  }

  public static double dirtyPrice(final Bond bond, final double cleanPrice) {
    return cleanPrice + bond.getAccruedInterest();
  }

  public static double cleanPrice(final Bond bond, final YieldCurveBundle curves) {
    return cleanPrice(bond, dirtyPrice(bond, curves));
  }

  public static double cleanPrice(final Bond bond, final double dirtyPrice) {
    return dirtyPrice - bond.getAccruedInterest();
  }

  public static double forwardDirtyPrice(final Bond bond, final double dirtyPrice, final double forwardTime, final double fundingRate) {
    final FixedPayment principle = bond.getPrinciplePayment();
    Validate.isTrue(forwardTime < principle.getPaymentTime(), "future time beyond maturity of bond");
    final GenericAnnuity<FixedCouponPayment> coupons = bond.getCouponAnnuity();

    BondYieldCalculator byc = new BondYieldCalculator();
    // double yield = byc.calculate(bond, dirtyPrice);

    double valueOfExpiredCoupons = 0.0;
    for (final FixedCouponPayment payments : coupons.getPayments()) {
      final double ti = payments.getPaymentTime();
      if (ti > forwardTime) {
        break;
      }
      valueOfExpiredCoupons += payments.getAmount() * (1 + fundingRate * (forwardTime - ti));// Math.exp(fundingRate * (forwardTime - ti));
    }

    return dirtyPrice * (1 + fundingRate * forwardTime) - valueOfExpiredCoupons;
  }

  public static double forwardDirtyPrice(final Bond bond, final YieldCurveBundle curves, final double forwardTime, final double fundingRate) {
    final double dirtyPrice = dirtyPrice(bond, curves);
    return forwardDirtyPrice(bond, dirtyPrice, forwardTime, fundingRate);
  }

  // public static double forwardCleanPrice(final Bond bond, final double forwardDirtyPrice, final double forwardTime) {
  // final GenericAnnuity<FixedCouponPayment> coupons = bond.getCouponAnnuity();
  // int n = 0;
  // while (forwardTime > coupons.getNthPayment(n).getPaymentTime() && n < coupons.getNumberOfPayments()) {
  // n++;
  // }
  //
  // final FixedCouponPayment payment = coupons.getNthPayment(n);
  // double w;
  //
  // if (n == 0) {
  // w = forwardTime * (1 - bond.getAccruedInterestFraction()) / payment.getPaymentTime() + bond.getAccruedInterestFraction();
  // } else {
  // final double ta = coupons.getNthPayment(n - 1).getPaymentTime();
  // final double tb = payment.getPaymentTime();
  // w = (forwardTime - ta) / (tb - ta);
  // }
  //
  // return forwardDirtyPrice - w * payment.getAmount();
  // }

  // public static double forwardCleanPrice(final Bond bond, final YieldCurveBundle curves, final double forwardTime, final double fundingRate) {
  // final double forwardDirtyPrice = forwardDirtyPrice(bond, curves, forwardTime, fundingRate);
  // return forwardCleanPrice(bond, forwardDirtyPrice, forwardTime);
  // }

}
