/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class Swap {
  private final double[] _fixedPaymentTimes;
  private final double[] _floatPaymentTimes;
  private final double[] _deltaStart;
  private final double[] _deltaEnd;
  private double[] _fixedYearFractions;
  private double[] _floatYearFractions;
  private double[] _liborYearFractions;
  private final int _nFix;
  private final int _nFloat;

  public Swap(final double[] fixedPaymentTimes, final double[] floatingPaymentTimes, final double[] fwdStartOffsets, final double[] fwdEndOffsets) {
    ArgumentChecker.notEmpty(fixedPaymentTimes, "fixedPaymentTime must not be null or zero length");
    ArgumentChecker.notEmpty(floatingPaymentTimes, "flaotingPaymentTimes must not be null or zero length");
    ArgumentChecker.notEmpty(fwdStartOffsets, "fwdStartOffsets must not be null or zero length");
    ArgumentChecker.notEmpty(fwdEndOffsets, "fwdEndOffsets must not be null or zero length");

    _nFix = fixedPaymentTimes.length;
    _nFloat = floatingPaymentTimes.length;

    if (fwdStartOffsets.length != _nFloat || fwdEndOffsets.length != _nFloat) {
      throw new IllegalArgumentException("list of floatingPaymentTimes not the same length as Offsets");
    }

    _fixedPaymentTimes = fixedPaymentTimes;
    _floatPaymentTimes = floatingPaymentTimes;
    _deltaStart = fwdStartOffsets;
    _deltaEnd = fwdEndOffsets;

    setupDefaultYearfractions();

  }

  private void setupDefaultYearfractions() {
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
    _liborYearFractions = Arrays.copyOf(_floatYearFractions, _nFloat);
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

  public double[] getLiborYearFractions() {
    return _liborYearFractions;
  }

  public int getNumberOfFixedPayments() {
    return _nFix;
  }

  public int getNumberOfFloatingPayments() {
    return _nFloat;
  }

  /*  public double getLastUsedTime() {
      return Math.max(_fixedPaymentTimes[_nFix - 1], _floatPaymentTimes[_nFloat - 1] + _deltaEnd[_nFloat - 1]);
    }*/

}
