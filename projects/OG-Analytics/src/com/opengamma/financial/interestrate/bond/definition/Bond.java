/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.util.money.Currency;

/**
 * 
 */
// TODO Because of the way that accrued interest is calculated (i.e. Julian days, so that whole numbers of days are used), we need extra fields that can contain the accrued interest
// up to the time today (fractions of a day) as well as the "official" accrued interest.
public class Bond implements InterestRateDerivative {
  private final GenericAnnuity<CouponFixed> _coupons;
  private final PaymentFixed _principle;
  private final double _accruedInterest;

  public Bond(Currency currency, final double[] paymentTimes, final double couponRate, final String yieldCurveName) {
    Validate.notNull(paymentTimes, "payment times");
    Validate.isTrue(paymentTimes.length > 0, "payment times array is empty");
    Validate.notNull(yieldCurveName, "yield curve name");
    final int n = paymentTimes.length;
    double yearFraction;
    final CouponFixed[] payments = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      yearFraction = paymentTimes[i] - (i == 0 ? 0.0 : paymentTimes[i - 1]);
      payments[i] = new CouponFixed(currency, paymentTimes[i], yieldCurveName, yearFraction, couponRate);
    }
    _principle = new PaymentFixed(currency, paymentTimes[n - 1], 1.0, yieldCurveName);
    _coupons = new GenericAnnuity<CouponFixed>(payments);
    _accruedInterest = 0.0;
  }

  // TODO don't need this constructor
  public Bond(Currency currency, final double[] paymentTimes, final double couponRate, final double yearFraction, final double accruedInterest, final String yieldCurveName) {
    Validate.notNull(paymentTimes, "payment times");
    Validate.isTrue(paymentTimes.length > 0, "payment times array is empty");
    Validate.notNull(yieldCurveName, "yield curve name");
    final int n = paymentTimes.length;
    final CouponFixed[] payments = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      payments[i] = new CouponFixed(currency, paymentTimes[i], yieldCurveName, yearFraction, couponRate);
    }
    _principle = new PaymentFixed(currency, paymentTimes[n - 1], 1.0, yieldCurveName);
    _coupons = new GenericAnnuity<CouponFixed>(payments);
    _accruedInterest = accruedInterest;
  }

  public Bond(Currency currency, final double[] paymentTimes, final double[] coupons, final double[] yearFractions, final double accruedInterest, final String yieldCurveName) {
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
    final CouponFixed[] payments = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      payments[i] = new CouponFixed(currency, paymentTimes[i], yieldCurveName, yearFractions[i], coupons[i]);
    }
    _principle = new PaymentFixed(currency, paymentTimes[n - 1], 1.0, yieldCurveName);
    _coupons = new GenericAnnuity<CouponFixed>(payments);
    _accruedInterest = accruedInterest;
  }

  /**
   * Gets the _currency field.
   * @return the _currency
   */
  public Currency getCurrency() {
    return _coupons.getCurrency();
  }

  /**
   * Full set of payments - coupons and principle 
   * @return the annuity
   */
  public GenericAnnuity<PaymentFixed> getAnnuity() {
    final int n = _coupons.getNumberOfPayments();
    final PaymentFixed[] temp = new PaymentFixed[n + 1];
    for (int i = 0; i < n; i++) {
      temp[i] = _coupons.getNthPayment(i);
    }
    temp[n] = _principle;
    return new GenericAnnuity<PaymentFixed>(temp);
  }

  /**
   * 
   * @return The final return of principle payment
   */
  public PaymentFixed getPrinciplePayment() {
    return _principle;
  }

  /**
   * 
   * @return Coupons only Payments (i.e. excluding the principle payment)
   */
  public GenericAnnuity<CouponFixed> getCouponAnnuity() {
    return _coupons;
  }

  /**
   * 
   * @return Coupons only Payments (i.e. excluding the principle payment) where the coupon is set to 1
   */
  public GenericAnnuity<CouponFixed> getUnitCouponAnnuity() {
    final int n = _coupons.getNumberOfPayments();
    final CouponFixed[] temp = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      temp[i] = _coupons.getNthPayment(i).withUnitCoupon();
    }
    return new GenericAnnuity<CouponFixed>(temp);
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
