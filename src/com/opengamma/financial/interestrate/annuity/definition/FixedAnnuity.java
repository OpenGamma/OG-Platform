/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class FixedAnnuity implements Annuity {

  private double[] _paymentAmounts;
  private final double[] _paymentTimes;
  private final double[] _yearFractions;
  private final double[] _coupons;
  private final double _notional;
  private final int _n;
  private final String _curveName;

  public FixedAnnuity(final double[] paymentTimes, final String yieldCurveName) {
    this(paymentTimes, 1.0, yieldCurveName);
  }

  public FixedAnnuity(final double[] paymentTimes, final double notional, final String yieldCurveName) {
    Validate.notNull(paymentTimes);
    Validate.notNull(yieldCurveName);
    _notional = notional;
    _paymentTimes = paymentTimes;
    _n = paymentTimes.length;
    _yearFractions = new double[_n];
    _coupons = new double[_n];
    _yearFractions[0] = paymentTimes[0];
    _coupons[0] = 1.0;
    for (int i = 1; i < _n; i++) {
      _coupons[i] = 1.0;
      _yearFractions[i] = (paymentTimes[i] - paymentTimes[i - 1]);
    }
    _curveName = yieldCurveName;
  }

  public FixedAnnuity(final double[] paymentTimes, final double notional, final double[] coupons, final String yieldCurveName) {
    Validate.notNull(paymentTimes);
    Validate.notNull(coupons);
    Validate.notNull(yieldCurveName);
    _n = paymentTimes.length;
    Validate.isTrue(coupons.length == _n);
    _paymentTimes = paymentTimes;
    _coupons = coupons;
    _yearFractions = new double[_n];
    _yearFractions[0] = paymentTimes[0];
    for (int i = 1; i < _n; i++) {
      _yearFractions[i] = (paymentTimes[i] - paymentTimes[i - 1]);
    }
    _notional = notional;
    _curveName = yieldCurveName;
  }

  public FixedAnnuity(final double[] paymentTimes, final double notional, final double[] coupons, final double[] yearFractions, final String yieldCurveName) {
    Validate.notNull(paymentTimes);
    Validate.notNull(coupons);
    Validate.notNull(yearFractions);
    Validate.notNull(yieldCurveName);
    _n = paymentTimes.length;
    Validate.isTrue(coupons.length == _n);
    Validate.isTrue(yearFractions.length == _n);
    _paymentTimes = paymentTimes;
    _coupons = coupons;
    _yearFractions = yearFractions;
    _curveName = yieldCurveName;
    _notional = notional;
  }

  public double[] getPaymentAmounts() {
    if (_paymentAmounts == null) {
      _paymentAmounts = new double[_n];
      for (int i = 0; i < _n; i++) {
        _paymentAmounts[i] = _notional * _yearFractions[i] * _coupons[i];
      }
    }
    return _paymentAmounts;
  }

  public String getFundingCurveName() {
    return _curveName;
  }

  public double[] getCoupons() {
    return _coupons;
  }

  @Override
  public double[] getPaymentTimes() {
    return _paymentTimes;
  }

  @Override
  public int getNumberOfPayments() {
    return _n;
  }

  @Override
  public double getNotional() {
    return _notional;
  }

  @Override
  public double[] getYearFractions() {
    return _yearFractions;
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
    result = prime * result + Arrays.hashCode(_paymentAmounts);
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
    if (!Arrays.equals(_paymentAmounts, other._paymentAmounts)) {
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

}
