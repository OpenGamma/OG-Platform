/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class FixedAnnuity implements Annuity {
  private double[] _paymentAmounts;
  private final double[] _yearFractions;
  private final double[] _paymentTimes;
  private final double[] _coupons;
  private final double _notional;
  private final int _n;
  private final String _curveName;

  // /**
  // * @param paymentTimes time in years from now where payments are made
  // * @param paymentAmounts actual cash amounts paid on paymentTimes time
  // * @param yieldCurveName name of curve to take discount factors off
  // */
  // public FixedAnnuity(final double[] paymentTimes, final double[] paymentAmounts, final String yieldCurveName) {
  // Validate.notNull(paymentTimes);
  // ArgumentChecker.notEmpty(paymentTimes, "payment times");
  // Validate.notNull(paymentAmounts);
  // ArgumentChecker.notEmpty(paymentAmounts, "payment amounts");
  // Validate.notNull(yieldCurveName);
  // _n = paymentTimes.length;
  // Validate.isTrue(paymentAmounts.length == _n);
  // _paymentTimes = paymentTimes;
  // _paymentAmounts = paymentAmounts;
  // _yearFractions = setupActualActualYearFractions(paymentTimes);
  // _curveName = yieldCurveName;
  // }
  //
  // /**
  // *
  // * @param paymentTimes time in years from now where payments are made
  // * @param paymentAmounts actual cash amounts paid on paymentTimes time
  // * @param yearFractions year fractions between payments - <b>note</b> this has no effect on the paymentAmounts
  // * @param yieldCurveName name of curve to take discount factors off
  // */
  // public FixedAnnuity(final double[] paymentTimes, final double[] paymentAmounts, final double[] yearFractions, final String yieldCurveName) {
  // Validate.notNull(paymentTimes);
  // ArgumentChecker.notEmpty(paymentTimes, "payment times");
  // Validate.notNull(paymentAmounts);
  // ArgumentChecker.notEmpty(paymentAmounts, "payment amounts");
  // Validate.notNull(yieldCurveName);
  // Validate.notNull(yearFractions);
  // ArgumentChecker.notEmpty(yearFractions, "year fraction");
  // _n = paymentTimes.length;
  // Validate.isTrue(paymentAmounts.length == _n);
  // Validate.isTrue(yearFractions.length == _n);
  // _paymentTimes = paymentTimes;
  // _paymentAmounts = paymentAmounts;
  // _yearFractions = yearFractions;
  // _curveName = yieldCurveName;
  // }

  /**
   * 
   * @param paymentTimes paymentTimes time in years from now where payments are made 
   * @param notional amount that actual cash payments are calculated off 
   * @param couponRate the fixed rate of the payment - actual cash amounts paid at PaymentTimes is notional*couponRate*yearFraction - where yearFraction is the ACT/ACT time between paymentTimes 
   * @param yieldCurveName  name of curve to take discount factors off 
   */
  public FixedAnnuity(final double[] paymentTimes, final double notional, final double couponRate, final String yieldCurveName) {
    Validate.notNull(paymentTimes);
    ArgumentChecker.notEmpty(paymentTimes, "payment times");

    Validate.notNull(yieldCurveName);
    _n = paymentTimes.length;

    _paymentTimes = paymentTimes;
    _yearFractions = setupActualActualYearFractions(paymentTimes);
    _coupons = new double[_n];
    for (int i = 0; i < _n; i++) {
      _coupons[i] = couponRate;
    }
    _notional = notional;
    _curveName = yieldCurveName;
  }

  /**
   * 
   * @param paymentTimes paymentTimes paymentTimes time in years from now where payments are made 
   * @param notional amount that actual cash payments are calculated off 
   * @param coupons  the fixed rate of the payment - actual cash amounts paid at PaymentTimes is notional*coupon*yearFraction - where yearFraction is the ACT/ACT time between paymentTimes 
   * @param yieldCurveName name of curve to take discount factors off 
   */
  public FixedAnnuity(final double[] paymentTimes, final double notional, final double[] coupons, final String yieldCurveName) {
    Validate.notNull(paymentTimes);
    ArgumentChecker.notEmpty(paymentTimes, "payment times");
    Validate.notNull(coupons);
    ArgumentChecker.notEmpty(coupons, "coupons");
    Validate.notNull(yieldCurveName);
    _n = paymentTimes.length;
    Validate.isTrue(coupons.length == _n);
    _paymentTimes = paymentTimes;
    _yearFractions = setupActualActualYearFractions(paymentTimes);
    _coupons = coupons;
    _notional = notional;
    _curveName = yieldCurveName;
  }

  /**
   * 
   * @param paymentTimes paymentTimes paymentTimes time in years from now where payments are made 
   * @param notional amount that actual cash payments are calculated off 
   * @param couponRate the fixed rate of the payment - actual cash amounts paid at PaymentTimes is notional*couponRate*yearFraction
   * @param yearFractions year fractions between payments 
   * @param yieldCurveName name of curve to take discount factors off 
   */
  public FixedAnnuity(final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions, final String yieldCurveName) {
    Validate.notNull(paymentTimes);
    ArgumentChecker.notEmpty(paymentTimes, "payment times");
    Validate.notNull(yearFractions);
    ArgumentChecker.notEmpty(yearFractions, "year fraction");
    Validate.notNull(yieldCurveName);
    _n = paymentTimes.length;
    Validate.isTrue(yearFractions.length == _n);
    _paymentTimes = paymentTimes;
    _yearFractions = yearFractions;

    _coupons = new double[_n];
    for (int i = 0; i < _n; i++) {
      _coupons[i] = couponRate;
    }
    _notional = notional;
    _curveName = yieldCurveName;
  }

  /**
   * 
   * @param paymentTimes paymentTimes paymentTimes paymentTimes time in years from now where payments are made 
   * @param notional amount that actual cash payments are calculated off 
   * @param coupons the fixed rate of the payment - actual cash amounts paid at PaymentTimes is notional*coupon*yearFraction
   * @param yearFractions year fractions between payments 
   * @param yieldCurveName name of curve to take discount factors off 
   */
  public FixedAnnuity(final double[] paymentTimes, final double notional, final double[] coupons, final double[] yearFractions, final String yieldCurveName) {
    Validate.notNull(paymentTimes);
    ArgumentChecker.notEmpty(paymentTimes, "payment times");
    Validate.notNull(coupons);
    ArgumentChecker.notEmpty(coupons, "coupons");
    Validate.notNull(yearFractions);
    ArgumentChecker.notEmpty(yearFractions, "year fraction");
    Validate.notNull(yieldCurveName);
    _n = paymentTimes.length;
    Validate.isTrue(coupons.length == _n);
    Validate.isTrue(yearFractions.length == _n);
    _paymentTimes = paymentTimes;
    _yearFractions = yearFractions;

    _coupons = coupons;
    _notional = notional;
    _curveName = yieldCurveName;
  }

  protected static double[] setupActualActualYearFractions(final double[] paymentTimes) {
    final int n = paymentTimes.length;
    final double[] res = new double[n];
    res[0] = paymentTimes[0];
    for (int i = 1; i < n; i++) {
      res[i] = paymentTimes[i] - paymentTimes[i - 1];
    }
    return res;
  }

  public double[] getPaymentAmounts() {
    if (_paymentAmounts == null) {
      _paymentAmounts = new double[_n];
      for (int i = 0; i < _n; i++) {
        _paymentAmounts[i] = _notional * _coupons[i] * _yearFractions[i];
      }
    }
    return _paymentAmounts;
  }

  /**
   * used for calculating swap rates 
   * @return A fixed annuity with all coupons set to zero
   */
  @Override
  public FixedAnnuity withUnitCoupons() {
    final double[] coupons = new double[getNumberOfPayments()];
    for (int i = 0; i < getNumberOfPayments(); i++) {
      coupons[i] = 1.0;
    }
    return new FixedAnnuity(getPaymentTimes(), getNotional(), coupons, getYearFractions(), getFundingCurveName());
  }

  /**
   * fixed annuity does not have a spread
   * @return this
   */
  @Override
  public Annuity withZeroSpread() {
    return this;
  }

  @Override
  public String getFundingCurveName() {
    return _curveName;
  }

  @Override
  public double[] getPaymentTimes() {
    return _paymentTimes;
  }

  @Override
  public double[] getYearFractions() {
    return _yearFractions;
  }

  @Override
  public double getNotional() {
    return _notional;
  }

  @Override
  public int getNumberOfPayments() {
    return _n;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_coupons);
    result = prime * result + ((_curveName == null) ? 0 : _curveName.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_paymentTimes);
    result = prime * result + Arrays.hashCode(_yearFractions);
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
    FixedAnnuity other = (FixedAnnuity) obj;
    if (!Arrays.equals(_coupons, other._coupons)) {
      return false;
    }
    if (_curveName == null) {
      if (other._curveName != null) {
        return false;
      }
    } else if (!_curveName.equals(other._curveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (!Arrays.equals(_paymentTimes, other._paymentTimes)) {
      return false;
    }
    if (!Arrays.equals(_yearFractions, other._yearFractions)) {
      return false;
    }
    return true;
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<T> visitor, final YieldCurveBundle curves) {
    return visitor.visitFixedAnnuity(this, curves);
  }

}
