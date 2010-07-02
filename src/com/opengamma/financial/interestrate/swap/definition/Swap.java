/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class Swap {
  private final double[] _fixedPaymentTimes;
  private final double[] _floatPaymentTimes;
  private final double[] _deltaStart;
  private final double[] _deltaEnd;
  private final double[] _fixedYearFractions;
  private final double[] _floatYearFractions;
  private final double[] _referenceRateYearFractions;
  private final int _nFix;
  private final int _nFloat;

  public Swap(final double[] fixedPaymentTimes, final double[] floatingPaymentTimes, final double[] fwdStartOffsets, final double[] fwdEndOffsets) {
    Validate.notNull(fixedPaymentTimes);
    Validate.notNull(floatingPaymentTimes);
    Validate.notNull(fwdStartOffsets);
    Validate.notNull(fwdEndOffsets);
    ArgumentChecker.notEmpty(fixedPaymentTimes, "fixedPaymentTime");
    ArgumentChecker.notEmpty(floatingPaymentTimes, "floatingPaymentTime");
    ArgumentChecker.notEmpty(fwdStartOffsets, "fwdStartOffsets");
    ArgumentChecker.notEmpty(fwdEndOffsets, "fwdEndOffsets");
    _nFix = fixedPaymentTimes.length;
    _nFloat = floatingPaymentTimes.length;
    if (fwdStartOffsets.length != _nFloat) {
      throw new IllegalArgumentException("list of floatingPaymentTimes not the same length as start offsets");
    }
    if (fwdEndOffsets.length != _nFloat) {
      throw new IllegalArgumentException("list of floatingPaymentTimes not the same length as end offsets");
    }
    _fixedPaymentTimes = fixedPaymentTimes;
    _floatPaymentTimes = floatingPaymentTimes;
    _deltaStart = fwdStartOffsets;
    _deltaEnd = fwdEndOffsets;
    Arrays.sort(_fixedPaymentTimes);
    Arrays.sort(_floatPaymentTimes);
    _fixedYearFractions = new double[_nFix];
    _fixedYearFractions[0] = _fixedPaymentTimes[0];
    for (int i = 1; i < _nFix; i++) {
      _fixedYearFractions[i] = _fixedPaymentTimes[i] - _fixedPaymentTimes[i - 1];
    }
    _floatYearFractions = new double[_nFloat];
    _floatYearFractions[0] = _floatPaymentTimes[0];
    for (int i = 1; i < _nFloat; i++) {
      _floatYearFractions[i] = _floatPaymentTimes[i] - _floatPaymentTimes[i - 1];
    }
    _referenceRateYearFractions = Arrays.copyOf(_floatYearFractions, _nFloat);
  }

  public double[] getFixedPaymentTimes() {
    return _fixedPaymentTimes;
  }

  public double[] getFixedYearFractions() {
    return _fixedYearFractions;
  }

  public double[] getFloatingPaymentTimes() {
    return _floatPaymentTimes;
  }

  public double[] getFloatingYearFractions() {
    return _floatYearFractions;
  }

  public double[] getDeltaStart() {
    return _deltaStart;
  }

  public double[] getDeltaEnd() {
    return _deltaEnd;
  }

  public double[] getReferenceYearFractions() {
    return _referenceRateYearFractions;
  }

  public int getNumberOfFixedPayments() {
    return _nFix;
  }

  public int getNumberOfFloatingPayments() {
    return _nFloat;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_deltaEnd);
    result = prime * result + Arrays.hashCode(_deltaStart);
    result = prime * result + Arrays.hashCode(_fixedPaymentTimes);
    result = prime * result + Arrays.hashCode(_floatPaymentTimes);
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
    final Swap other = (Swap) obj;
    if (!Arrays.equals(_deltaEnd, other._deltaEnd)) {
      return false;
    }
    if (!Arrays.equals(_deltaStart, other._deltaStart)) {
      return false;
    }
    if (!Arrays.equals(_fixedPaymentTimes, other._fixedPaymentTimes)) {
      return false;
    }
    if (!Arrays.equals(_floatPaymentTimes, other._floatPaymentTimes)) {
      return false;
    }
    return true;
  }

}
