/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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

/**
 * 
 */
// TODO Because of the way that accrued interest is calculated (i.e. Julian days, so that whole numbers of days are used), we need extra fields that can contain the accrued interest
// up to the time today (fractions of a day) as well as the "official" accrued interest.
public class Bond implements InterestRateDerivativeWithRate {
  private final GenericAnnuity<FixedCouponPayment> _coupons;
  private final FixedPayment _principle;
  private final double _accruedInterest;

  public Bond(final double[] paymentTimes, final double couponRate, final String yieldCurveName) {
    Validate.notNull(paymentTimes, "payment times");
    Validate.isTrue(paymentTimes.length > 0, "payment times array is empty");
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
    _accruedInterest = 0.0;
  }

  public Bond(final double[] paymentTimes, final double couponRate, final double yearFraction, final double accruedInterest, final String yieldCurveName) {
    Validate.notNull(paymentTimes, "payment times");
    Validate.isTrue(paymentTimes.length > 0, "payment times array is empty");
    Validate.notNull(yieldCurveName, "yield curve name");
    final int n = paymentTimes.length;
    final FixedCouponPayment[] payments = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      payments[i] = new FixedCouponPayment(paymentTimes[i], yearFraction, couponRate, yieldCurveName);
    }
    _principle = new FixedPayment(paymentTimes[n - 1], 1.0, yieldCurveName);
    _coupons = new GenericAnnuity<FixedCouponPayment>(payments);
    _accruedInterest = accruedInterest;
  }

  public Bond(final double[] paymentTimes, final double[] coupons, final double[] yearFractions, final double accruedInterest, final String yieldCurveName) {
    Validate.notNull(paymentTimes, "payment times");
    Validate.notNull(coupons, "coupons");
    Validate.notNull(yearFractions, "year fractions");
    Validate.isTrue(paymentTimes.length > 0, "payment times array is empty");
    Validate.isTrue(coupons.length > 0, "coupons array is empty");
    Validate.isTrue(yearFractions.length > 0, "year fractions array is empty");
    Validate.notNull(yieldCurveName, "yield curve name");
    final int n = paymentTimes.length;
    Validate.isTrue(coupons.length == n, "Must have a payment for each payment time");
    Validate.isTrue(yearFractions.length == n, "Must have a payment for each payment time");
    final FixedCouponPayment[] payments = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      payments[i] = new FixedCouponPayment(paymentTimes[i], yearFractions[i], coupons[i], yieldCurveName);
    }
    _principle = new FixedPayment(paymentTimes[n - 1], 1.0, yieldCurveName);
    _coupons = new GenericAnnuity<FixedCouponPayment>(payments);
    _accruedInterest = accruedInterest;
  }

  /**
   * Full set of payments - coupons and principle 
   * @return the annuity
   */
  public GenericAnnuity<FixedPayment> getAnnuity() {
    final int n = _coupons.getNumberOfPayments();
    final FixedPayment[] temp = new FixedPayment[n + 1];
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
    final int n = _coupons.getNumberOfPayments();
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

    return new Bond(times, coupons, yearFrac, getAccruedInterest(), payments[0].getFundingCurveName());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_accruedInterest);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _coupons.hashCode();
    result = prime * result + _principle.hashCode();
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
    if (Double.doubleToLongBits(_accruedInterest) != Double.doubleToLongBits(other._accruedInterest)) {
      return false;
    }
    if (!ObjectUtils.equals(this._coupons, other._coupons)) {
      return false;
    }
    return ObjectUtils.equals(this._principle, other._principle);
  }

  /**
   * @return the accruedInterest
   */
  public double getAccruedInterest() {
    return _accruedInterest;
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitBond(this, data);
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitBond(this);
  }

}
