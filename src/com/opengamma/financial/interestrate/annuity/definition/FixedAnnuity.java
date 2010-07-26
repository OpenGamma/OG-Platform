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

  private final double[] _paymentAmounts;
  private final double[] _paymentTimes;
  private final int _n;
  private final String _curveName;

  public FixedAnnuity(final double[] paymentTimes, final String yieldCurveName) {
    this(paymentTimes, 1.0, yieldCurveName);
  }

  public FixedAnnuity(final double[] paymentTimes, final double notional, final String yieldCurveName) {
    Validate.notNull(paymentTimes);
    Validate.notNull(yieldCurveName);
    _paymentTimes = paymentTimes;
    _n = paymentTimes.length;
    _paymentAmounts = new double[_n];
    _paymentAmounts[0] = notional * paymentTimes[0];
    for (int i = 1; i < _n; i++) {
      _paymentAmounts[i] = notional * (paymentTimes[i] - paymentTimes[i - 1]);
    }
    _curveName = yieldCurveName;
  }

  public FixedAnnuity(final double[] paymentTimes, final double[] paymentAmounts, final String yieldCurveName) {
    Validate.notNull(paymentTimes);
    Validate.notNull(paymentAmounts);
    Validate.notNull(yieldCurveName);
    _n = paymentTimes.length;
    Validate.isTrue(paymentAmounts.length == _n);
    _paymentTimes = paymentTimes;
    _paymentAmounts = paymentAmounts;
    _curveName = yieldCurveName;
  }

  public FixedAnnuity(final double[] paymentTimes, final double notional, final double[] coupons, final String yieldCurveName) {
    Validate.notNull(paymentTimes);
    Validate.notNull(coupons);
    Validate.notNull(yieldCurveName);
    _n = paymentTimes.length;
    Validate.isTrue(coupons.length == _n);
    _paymentTimes = paymentTimes;

    _paymentAmounts = new double[_n];
    _paymentAmounts[0] = notional * coupons[0] * paymentTimes[0];
    for (int i = 1; i < _n; i++) {
      _paymentAmounts[i] = notional * coupons[i] * (paymentTimes[i] - paymentTimes[i - 1]);
    }
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
    _paymentAmounts = new double[_n];

    for (int i = 0; i < _n; i++) {
      _paymentAmounts[i] = notional * coupons[i] * yearFractions[i];
    }
    _curveName = yieldCurveName;
  }

  public double[] getPaymentAmounts() {
    return _paymentAmounts;
  }

  public String getFundingCurveName() {
    return _curveName;
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_curveName == null) ? 0 : _curveName.hashCode());
    result = prime * result + Arrays.hashCode(_paymentAmounts);
    result = prime * result + Arrays.hashCode(_paymentTimes);
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
    if (_curveName == null) {
      if (other._curveName != null) {
        return false;
      }
    } else if (!_curveName.equals(other._curveName)) {
      return false;
    }
    if (!Arrays.equals(_paymentAmounts, other._paymentAmounts)) {
      return false;
    }
    if (!Arrays.equals(_paymentTimes, other._paymentTimes)) {
      return false;
    }
    return true;
  }

}
