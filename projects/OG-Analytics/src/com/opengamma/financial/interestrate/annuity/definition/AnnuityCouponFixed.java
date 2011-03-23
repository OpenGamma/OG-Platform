/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.payments.CouponFixed;

/**
 * A wrapper class for a GenericAnnuity containing FixedCouponPayment.
 */
public class AnnuityCouponFixed extends GenericAnnuity<CouponFixed> {

  //TODO: How do we make sure that all the coupons have the same rate and notional.
  // This may not be true if the constructor from payments is used.
  /**
   * Constructor from an array of fixed coupons.
   * @param payments The payments array.
   */
  public AnnuityCouponFixed(final CouponFixed[] payments) {
    super(payments);
  }

  public AnnuityCouponFixed(final double[] paymentTimes, final double couponRate, final String yieldCurveName, boolean isPayer) {
    this(paymentTimes, 1.0, couponRate, yieldCurveName, isPayer);
  }

  public AnnuityCouponFixed(final double[] paymentTimes, final double notional, final double couponRate, final String yieldCurveName, boolean isPayer) {
    this(paymentTimes, notional, couponRate, initBasisYearFraction(paymentTimes), yieldCurveName, isPayer);
  }

  /**
   * Constructor from payment times and year fractions and unique notional and rate. 
   * @param paymentTimes The times (in year) of payment.
   * @param notional The common notional.
   * @param couponRate The common coupon rate.
   * @param yearFractions The year fraction of each payment.
   * @param yieldCurveName The discounting curve name.
   * @param isPayer TODO
   */
  public AnnuityCouponFixed(final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions, final String yieldCurveName, boolean isPayer) {
    super(init(paymentTimes, notional * (isPayer ? -1.0 : 1.0), couponRate, yearFractions, yieldCurveName));
  }

  public double getCouponRate() {
    return getNthPayment(0).getFixedRate(); // all coupons are the same value
  }

  /**
   * A list of fixed coupon from payment times and year fractions and unique notional and rate. 
   * @param paymentTimes The times (in year) of payment.
   * @param notional The common notional.
   * @param couponRate The common coupon rate.
   * @param yearFractions The year fraction of each payment.
   * @param yieldCurveName The discounting curve name.
   * @return The array of fixed coupons.
   */
  private static CouponFixed[] init(final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions, final String yieldCurveName) {
    Validate.notNull(paymentTimes);
    Validate.isTrue(paymentTimes.length > 0, "payment times array is empty");
    Validate.notNull(yearFractions);
    Validate.isTrue(yearFractions.length > 0, "year fraction array is empty");
    Validate.notNull(yieldCurveName);
    final int n = paymentTimes.length;
    Validate.isTrue(yearFractions.length == n);
    final CouponFixed[] temp = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      temp[i] = new CouponFixed(paymentTimes[i], yieldCurveName, yearFractions[i], notional, couponRate);
    }
    return temp;
  }

  private static double[] initBasisYearFraction(final double[] paymentTimes) {
    Validate.notNull(paymentTimes);
    Validate.isTrue(paymentTimes.length > 0, "payment times array is empty");
    final int n = paymentTimes.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = (i == 0 ? paymentTimes[0] : paymentTimes[i] - paymentTimes[i - 1]); // TODO ????????? so the payment year fractions could be 2.5, 0.5, 0.5, 0.5?
    }
    return res;
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitFixedCouponAnnuity(this, data);
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitFixedCouponAnnuity(this);
  }

}
