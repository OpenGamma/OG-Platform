/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.InterestRateDerivativeWithRate;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class Bond implements InterestRateDerivativeWithRate {

  private final GenericAnnuity<FixedCouponPayment> _coupons;
  private final FixedPayment _principle;
  private final double _accruedInterestFraction;

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
    _accruedInterestFraction = 0.0;
  }

  public Bond(final double[] paymentTimes, final double couponRate, final double yearFraction, final double accrualFraction, final String yieldCurveName) {
    Validate.notNull(paymentTimes, "payment times");
    ArgumentChecker.notEmpty(paymentTimes, "payment times");
    Validate.notNull(yieldCurveName, "yield curve name");
    final int n = paymentTimes.length;

    final FixedCouponPayment[] payments = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      payments[i] = new FixedCouponPayment(paymentTimes[i], yearFraction, couponRate, yieldCurveName);
    }

    _principle = new FixedPayment(paymentTimes[n - 1], 1.0, yieldCurveName);
    _coupons = new GenericAnnuity<FixedCouponPayment>(payments);
    _accruedInterestFraction = accrualFraction;
  }

  public Bond(final double[] paymentTimes, final double[] coupons, final double[] yearFractions, final double accrualFraction, final String yieldCurveName) {
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
    _accruedInterestFraction = accrualFraction;
  }

  /**
   * Full set of payments - coupons and principle 
   * @return the annuity
   */
  public GenericAnnuity<FixedPayment> getAnnuity() {
    final int n = _coupons.getNumberOfpayments();
    final FixedPayment[] temp = new FixedPayment[n + 1];

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

    final int n = _coupons.getNumberOfpayments();
    final FixedCouponPayment[] temp = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      temp[i] = _coupons.getNthPayment(i).withUnitCoupon();
    }

    return new GenericAnnuity<FixedCouponPayment>(temp);
  }

  @Override
  public InterestRateDerivativeWithRate withRate(final double rate) {
    final FixedCouponPayment[] payments = _coupons.getPayments();
    final int n = payments.length;
    final double[] times = new double[n];
    final double[] coupons = new double[n];
    final double[] yearFrac = new double[n];
    for (int i = 0; i < n; i++) {
      final FixedCouponPayment temp = payments[i];
      times[i] = temp.getPaymentTime();
      coupons[i] = rate;
      yearFrac[i] = temp.getYearFraction();
    }

    return new Bond(times, coupons, yearFrac, getAccruedInterestFraction(), payments[0].getFundingCurveName());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_accruedInterestFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_coupons == null) ? 0 : _coupons.hashCode());
    result = prime * result + ((_principle == null) ? 0 : _principle.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Bond other = (Bond) obj;
    if (Double.doubleToLongBits(_accruedInterestFraction) != Double.doubleToLongBits(other._accruedInterestFraction)) {
      return false;
    }
    if (!ObjectUtils.equals(this._coupons, other._coupons)) {
      return false;
    }
    if (!ObjectUtils.equals(this._principle, other._principle)) {
      return false;
    }
    return true;
  }

  /**
   * Gets the accruedInterestFraction field.
   * @return the accruedInterestFraction
   */
  public double getAccruedInterestFraction() {
    return _accruedInterestFraction;
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitBond(this, data);
  }

}
