/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeWithRate;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;

/**
 * A wrapper class for a GenericAnnuity containing FixedCouponPayment
 */
public class FixedCouponAnnuity extends GenericAnnuity<FixedCouponPayment> implements InterestRateDerivativeWithRate {

  public FixedCouponAnnuity(final FixedCouponPayment[] payments) {
    super(payments);
  }

  public FixedCouponAnnuity(final double[] paymentTimes, final double couponRate, final String yieldCurveName) {
    this(paymentTimes, 1.0, couponRate, yieldCurveName);
  }

  public FixedCouponAnnuity(final double[] paymentTimes, final double notional, final double couponRate, final String yieldCurveName) {
    this(paymentTimes, notional, couponRate, setupBasisYearFraction(paymentTimes), yieldCurveName);
  }

  public FixedCouponAnnuity(final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions, final String yieldCurveName) {
    super(setup(paymentTimes, notional, couponRate, yearFractions, yieldCurveName));
  }

  public double getCouponRate() {
    return getNthPayment(0).getCoupon(); // all coupons are the same value
  }

  private static FixedCouponPayment[] setup(final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions, final String yieldCurveName) {
    Validate.notNull(paymentTimes);
    Validate.isTrue(paymentTimes.length > 0, "payment times array is empty");
    Validate.notNull(yearFractions);
    Validate.isTrue(yearFractions.length > 0, "year fraction array is empty");
    Validate.notNull(yieldCurveName);
    final int n = paymentTimes.length;
    Validate.isTrue(yearFractions.length == n);
    final FixedCouponPayment[] temp = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      temp[i] = new FixedCouponPayment(paymentTimes[i], notional, yearFractions[i], couponRate, yieldCurveName);
    }
    return temp;
  }

  private static double[] setupBasisYearFraction(final double[] paymentTimes) {
    Validate.notNull(paymentTimes);
    Validate.isTrue(paymentTimes.length > 0, "payment times array is empty");
    final int n = paymentTimes.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = (i == 0 ? paymentTimes[0] : paymentTimes[i] - paymentTimes[i - 1]);
    }
    return res;
  }

  @Override
  public FixedCouponAnnuity withRate(final double rate) {
    final FixedCouponPayment[] payments = getPayments();
    final int n = payments.length;
    final FixedCouponPayment[] temp = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      temp[i] = payments[i].withRate(rate);
    }
    return new FixedCouponAnnuity(temp);
  }

}
