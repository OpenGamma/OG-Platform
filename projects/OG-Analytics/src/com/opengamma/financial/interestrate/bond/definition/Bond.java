/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class Bond implements InterestRateDerivative {

  private final GenericAnnuity<FixedCouponPayment> _coupons;
  private final FixedPayment _principle;

  public Bond(final double[] paymentTimes, final double couponRate, final String yieldCurveName) {
    Validate.notNull(paymentTimes, "payment times");
    ArgumentChecker.notEmpty(paymentTimes, "payment times");
    Validate.notNull(yieldCurveName, "yield curve name");
    final int n = paymentTimes.length;

    double yearFraction;
    final FixedCouponPayment[] payments = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      yearFraction = paymentTimes[i] - (i == 0 ? 0.0 : paymentTimes[i - 1]);
      payments[i] = new FixedCouponPayment(paymentTimes[i], yearFraction, couponRate, yieldCurveName);
    }
    _principle = new FixedPayment(paymentTimes[n - 1], 1.0, yieldCurveName);
    _coupons = new GenericAnnuity<FixedCouponPayment>(payments);
  }

  public Bond(final double[] paymentTimes, final double couponRate, final double[] yearFractions, final String yieldCurveName) {
    Validate.notNull(paymentTimes, "payment times");
    Validate.notNull(yearFractions, "year fractions");
    ArgumentChecker.notEmpty(paymentTimes, "payment times");
    ArgumentChecker.notEmpty(yearFractions, "year fractions");
    Validate.notNull(yieldCurveName, "yield curve name");
    final int n = paymentTimes.length;
    if (n != yearFractions.length) {
      throw new IllegalArgumentException("Must have a year fraction for each payment time");
    }

    final FixedCouponPayment[] payments = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      payments[i] = new FixedCouponPayment(paymentTimes[i], yearFractions[i], couponRate, yieldCurveName);
    }

    _principle = new FixedPayment(paymentTimes[n - 1], 1.0, yieldCurveName);
    _coupons = new GenericAnnuity<FixedCouponPayment>(payments);
  }

  public Bond(final double[] paymentTimes, final double[] coupons, final String yieldCurveName) {
    Validate.notNull(paymentTimes, "payment times");
    Validate.notNull(coupons, "coupons");
    ArgumentChecker.notEmpty(paymentTimes, "payment times");
    ArgumentChecker.notEmpty(coupons, "coupons");
    Validate.notNull(yieldCurveName, "yield curve name");
    if (paymentTimes.length != coupons.length) {
      throw new IllegalArgumentException("Must have a payment for each payment time");
    }
    final int n = paymentTimes.length;
    double yearFraction;
    final FixedCouponPayment[] payments = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      yearFraction = paymentTimes[i] - (i == 0 ? 0.0 : paymentTimes[i - 1]);
      payments[i] = new FixedCouponPayment(paymentTimes[i], yearFraction, coupons[i], yieldCurveName);
    }
    _principle = new FixedPayment(paymentTimes[n - 1], 1.0, yieldCurveName);
    _coupons = new GenericAnnuity<FixedCouponPayment>(payments);
  }

  public Bond(final double[] paymentTimes, final double[] coupons, final double[] yearFractions, final String yieldCurveName) {
    Validate.notNull(paymentTimes, "payment times");
    Validate.notNull(coupons, "coupons");
    Validate.notNull(yearFractions, "year fractions");
    ArgumentChecker.notEmpty(paymentTimes, "payment times");
    ArgumentChecker.notEmpty(coupons, "coupons");
    ArgumentChecker.notEmpty(yearFractions, "year fractions");
    Validate.notNull(yieldCurveName, "yield curve name");
    final int n = paymentTimes.length;
    if (n != coupons.length) {
      throw new IllegalArgumentException("Must have a payment for each payment time");
    }
    if (n != yearFractions.length) {
      throw new IllegalArgumentException("Must have a year fraction for each payment time");
    }
    final FixedCouponPayment[] payments = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      payments[i] = new FixedCouponPayment(paymentTimes[i], yearFractions[i], coupons[i], yieldCurveName);
    }
    _principle = new FixedPayment(paymentTimes[n - 1], 1.0, yieldCurveName);
    _coupons = new GenericAnnuity<FixedCouponPayment>(payments);
  }

  /**
   * Full set of payments - coupons and principle 
   * @return the annuity
   */
  public GenericAnnuity<FixedPayment> getAnnuity() {
    int n = _coupons.getNumberOfpayments();
    FixedPayment[] temp = new FixedPayment[n + 1];

    // temp = Arrays.copyOf(_coupons.getPayments(), n + 1);
    for (int i = 0; i < n; i++) {
      temp[i] = _coupons.getNthPayment(i);
    }

    temp[n] = _principle;
    return new GenericAnnuity<FixedPayment>(temp);
  }

  /**
   * 
   * @return The final return of principle payment
   */
  public FixedPayment getPrinciplePayment() {
    return _principle;
  }

  /**
   * 
   * @return Coupons only Payments (i.e. excluding the principle payment)
   */
  public GenericAnnuity<FixedCouponPayment> getCouponAnnuity() {
    return _coupons;
  }

  /**
   * 
   * @return Coupons only Payments (i.e. excluding the principle payment) where the coupon is set to 1
   */
  public GenericAnnuity<FixedCouponPayment> getUnitCouponAnnuity() {

    int n = _coupons.getNumberOfpayments();
    FixedCouponPayment[] temp = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      temp[i] = _coupons.getNthPayment(i).withUnitCoupon();
    }

    return new GenericAnnuity<FixedCouponPayment>(temp);
  }

  @Override
  public InterestRateDerivative withRate(double rate) {
    FixedCouponPayment[] payments = _coupons.getPayments();
    int n = payments.length;
    double[] times = new double[n];
    double[] yearFrac = new double[n];
    for (int i = 0; i < n; i++) {
      FixedCouponPayment temp = payments[i];
      times[i] = temp.getPaymentTime();
      yearFrac[i] = temp.getYearFraction();
    }

    return new Bond(times, rate, yearFrac, payments[0].getFundingCurveName());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_coupons == null) ? 0 : _coupons.hashCode());
    result = prime * result + ((_principle == null) ? 0 : _principle.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Bond other = (Bond) obj;
    if (!ObjectUtils.equals(this._coupons, other._coupons)) {
      return false;
    }
    if (!ObjectUtils.equals(this._principle, other._principle)) {
      return false;
    }

    return true;
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitBond(this, data);
  }

}
