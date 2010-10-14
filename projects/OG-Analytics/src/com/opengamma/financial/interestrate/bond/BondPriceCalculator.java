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

  public static double dirtyPrice(Bond bond, YieldCurveBundle curves) {
    return PresentValueCalculator.getInstance().visitBond(bond, curves);
  }

  public static double dirtyPrice(Bond bond, double cleanPrice) {
    FixedCouponPayment firstPayement = bond.getCouponAnnuity().getNthPayment(0);
    return cleanPrice + bond.getAccruedInterestFraction() * firstPayement.getAmount();
  }

  public static double cleanPrice(Bond bond, YieldCurveBundle curves) {
    return cleanPrice(bond, dirtyPrice(bond, curves));
  }

  public static double cleanPrice(Bond bond, double dirtyPrice) {
    FixedCouponPayment firstPayement = bond.getCouponAnnuity().getNthPayment(0);
    return dirtyPrice - bond.getAccruedInterestFraction() * firstPayement.getAmount();
  }

  public static double forwardDirtyPrice(Bond bond, double dirtyPrice, double forwardTime, double fundingRate) {
    FixedPayment priniple = bond.getPrinciplePayment();
    Validate.isTrue(forwardTime < priniple.getPaymentTime(), "future time beyond maturity of bond");
    GenericAnnuity<FixedCouponPayment> coupons = bond.getCouponAnnuity();

    double valueOfExpiredCoupons = 0.0;
    for (FixedCouponPayment payments : coupons.getPayments()) {
      double ti = payments.getPaymentTime();
      if (ti > forwardTime) {
        break;
      }
      valueOfExpiredCoupons += payments.getAmount() * Math.exp(fundingRate * (forwardTime - ti));
    }

    return dirtyPrice * Math.exp(fundingRate * forwardTime) - valueOfExpiredCoupons;
  }

  public static double forwardDirtyPrice(Bond bond, YieldCurveBundle curves, double forwardTime, double fundingRate) {
    double dirtyPrice = dirtyPrice(bond, curves);
    return forwardDirtyPrice(bond, dirtyPrice, forwardTime, fundingRate);
  }

  public static double forwardCleanPrice(Bond bond, double forwardDirtyPrice, double forwardTime) {
    GenericAnnuity<FixedCouponPayment> coupons = bond.getCouponAnnuity();
    int n = 0;
    while (forwardTime > coupons.getNthPayment(n).getPaymentTime() && n < coupons.getNumberOfpayments()) {
      n++;
    }

    FixedCouponPayment payment = coupons.getNthPayment(n);
    double w;

    if (n == 0) {
      w = forwardTime * (1 - bond.getAccruedInterestFraction()) / payment.getPaymentTime() + bond.getAccruedInterestFraction();
    } else {
      double ta = coupons.getNthPayment(n - 1).getPaymentTime();
      double tb = payment.getPaymentTime();
      w = (forwardTime - ta) / (tb - ta);
    }

    return forwardDirtyPrice - w * payment.getAmount();
  }

  public static double forwardCleanPrice(Bond bond, YieldCurveBundle curves, double forwardTime, double fundingRate) {
    double forwardDirtyPrice = forwardDirtyPrice(bond, curves, forwardTime, fundingRate);
    return forwardCleanPrice(bond, forwardDirtyPrice, forwardTime);
  }

}
