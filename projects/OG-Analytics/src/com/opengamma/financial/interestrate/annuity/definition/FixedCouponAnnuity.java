/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.util.ArgumentChecker;

/**
 * A wrapper class for a GenericAnnuity containing FixedCouponPayment
 */
public class FixedCouponAnnuity extends GenericAnnuity<FixedCouponPayment> {

  public FixedCouponAnnuity(final FixedCouponPayment[] payments) {
    super(payments);
  }

  public FixedCouponAnnuity(final double[] paymentTimes, final double couponRate, final String yieldCurveName) {
    this(paymentTimes, 1.0, couponRate, yieldCurveName);
  }

  public FixedCouponAnnuity(final double[] paymentTimes, final double notional, final double couponRate, final String yieldCurveName) {
    this(paymentTimes, notional, couponRate, setupBasisyearFrac(paymentTimes), yieldCurveName);
  }

  public FixedCouponAnnuity(final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions, final String yieldCurveName) {
    super(setup(paymentTimes, notional, couponRate, yearFractions, yieldCurveName));
  }

  public double getCouponRate() {
    return getNthPayment(0).getCoupon(); // all coupons are the same value
  }

  private static FixedCouponPayment[] setup(final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions, final String yieldCurveName) {
    Validate.notNull(paymentTimes);
    ArgumentChecker.notEmpty(paymentTimes, "payment times");
    Validate.notNull(yearFractions);
    ArgumentChecker.notEmpty(yearFractions, "year fraction");
    Validate.notNull(yieldCurveName);
    int n = paymentTimes.length;
    Validate.isTrue(yearFractions.length == n);

    FixedCouponPayment[] temp = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      temp[i] = new FixedCouponPayment(paymentTimes[i], notional, yearFractions[i], couponRate, yieldCurveName);
    }
    return temp;
  }

  private static double[] setupBasisyearFrac(final double[] paymentTimes) {
    int n = paymentTimes.length;
    double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = (i == 0 ? paymentTimes[0] : paymentTimes[i] - paymentTimes[i - 1]);
    }
    return res;
  }

  @Override
  public FixedCouponAnnuity withRate(double rate) {
    FixedCouponPayment[] payments = getPayments();
    int n = payments.length;
    FixedCouponPayment[] temp = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      temp[i] = payments[i].withRate(rate);
    }
    return new FixedCouponAnnuity(temp);
  }

}
