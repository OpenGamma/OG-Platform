/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class VariableAnnuity implements Annuity {

  private final double[] _paymentTimes;
  private final double[] _yearFractions;
  private final double[] _deltaStart;
  private final double[] _deltaEnd;
  private final double _notional;
  private final int _n;

  private final String _fundingCurveName;
  private final String _liborCurveName;

  public VariableAnnuity(final double[] paymentTimes, final String fundingCurveName, final String liborCurveName) {
    this(paymentTimes, 1.0, fundingCurveName, liborCurveName);
  }

  public VariableAnnuity(final double[] paymentTimes, double notional, final String fundingCurveName, final String liborCurveName) {
    Validate.notNull(paymentTimes);
    Validate.notNull(fundingCurveName);
    Validate.notNull(liborCurveName);
    ArgumentChecker.notEmpty(paymentTimes, "paymentTime");
    _notional = notional;
    _n = paymentTimes.length;
    _paymentTimes = paymentTimes;
    _deltaStart = new double[_n];
    _deltaEnd = new double[_n];
    _yearFractions = new double[_n];
    _yearFractions[0] = paymentTimes[0];
    for (int i = 1; i < _n; i++) {
      _yearFractions[i] = (paymentTimes[i] - paymentTimes[i - 1]);
    }
    _fundingCurveName = fundingCurveName;
    _liborCurveName = liborCurveName;
  }

  public VariableAnnuity(final double[] paymentTimes, double notional, final double[] fwdStartOffsets, final double[] fwdEndOffsets, final String fundingCurveName, final String liborCurveName) {
    Validate.notNull(paymentTimes);
    Validate.notNull(fwdStartOffsets);
    Validate.notNull(fwdEndOffsets);
    Validate.notNull(fundingCurveName);
    Validate.notNull(liborCurveName);
    ArgumentChecker.notEmpty(paymentTimes, "paymentTime");
    ArgumentChecker.notEmpty(fwdStartOffsets, "fwdStartOffsets");
    ArgumentChecker.notEmpty(fwdEndOffsets, "fwdStartOffsets");
    _notional = notional;
    _n = paymentTimes.length;
    Validate.isTrue(fwdStartOffsets.length == _n);
    Validate.isTrue(fwdEndOffsets.length == _n);
    _paymentTimes = paymentTimes;
    _deltaStart = fwdStartOffsets;
    _deltaEnd = fwdEndOffsets;
    _yearFractions = new double[_n];
    _yearFractions[0] = paymentTimes[0];
    for (int i = 1; i < _n; i++) {
      _yearFractions[i] = (paymentTimes[i] - paymentTimes[i - 1]);
    }
    _fundingCurveName = fundingCurveName;
    _liborCurveName = liborCurveName;
  }

  public double[] getDeltaStart() {
    return _deltaStart;
  }

  public double[] getDeltaEnd() {
    return _deltaEnd;
  }

  public String getFundingCurveName() {
    return _fundingCurveName;
  }

  public String getLiborCurveName() {
    return _liborCurveName;
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
  public int getNumberOfPayments() {
    return _n;
  }

  @Override
  public double getNotional() {
    return _notional;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_deltaEnd);
    result = prime * result + Arrays.hashCode(_deltaStart);
    result = prime * result + ((_fundingCurveName == null) ? 0 : _fundingCurveName.hashCode());
    result = prime * result + ((_liborCurveName == null) ? 0 : _liborCurveName.hashCode());
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
    VariableAnnuity other = (VariableAnnuity) obj;
    if (!Arrays.equals(_deltaEnd, other._deltaEnd)) {
      return false;
    }
    if (!Arrays.equals(_deltaStart, other._deltaStart)) {
      return false;
    }
    if (_fundingCurveName == null) {
      if (other._fundingCurveName != null) {
        return false;
      }
    } else if (!_fundingCurveName.equals(other._fundingCurveName)) {
      return false;
    }
    if (_liborCurveName == null) {
      if (other._liborCurveName != null) {
        return false;
      }
    } else if (!_liborCurveName.equals(other._liborCurveName)) {
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

}
