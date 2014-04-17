/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.Arrays;
import java.util.BitSet;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public abstract class LeastSquareSmileFitterTestCase {
  //  static final double VARIANCE = 0.3;
  static final double T = 1.3;
  static final double SIGMA = 0.3;// Math.sqrt(VARIANCE / T);
  static final double ERROR = 0.00001; //0.1 bps error
  static final EuropeanVanillaOption[] OPTIONS;
  static final double FORWARD = 100;
  static final double DF = 1;
  static final BlackFunctionData[] FLAT_DATA;
  static final double[] ERRORS;
  static final BitSet FIXED = new BitSet();
  static final double EPS = 1e-3;

  static {
    final int n = 10;
    final double kStart = 50;
    final double delta = 100 / (n - 1);
    OPTIONS = new EuropeanVanillaOption[n];
    FLAT_DATA = new BlackFunctionData[n];
    ERRORS = new double[n];
    for (int i = 0; i < n; i++) {
      OPTIONS[i] = new EuropeanVanillaOption(kStart + i * delta, T, true);
      FLAT_DATA[i] = new BlackFunctionData(FORWARD, DF, SIGMA);
      ERRORS[i] = ERROR;
    }
  }

  protected abstract LeastSquareSmileFitter getFitter();

  protected abstract double[] getInitialValues();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOptions() {
    getFitter().getFitResult(null, FLAT_DATA, ERRORS, getInitialValues(), FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyOptions() {
    getFitter().getFitResult(new EuropeanVanillaOption[0], FLAT_DATA, ERRORS, getInitialValues(), FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    getFitter().getFitResult(OPTIONS, null, ERRORS, getInitialValues(), FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthData() {
    getFitter().getFitResult(OPTIONS, new BlackFunctionData[] {new BlackFunctionData(FORWARD, DF, SIGMA) }, ERRORS, getInitialValues(), FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthErrors() {
    getFitter().getFitResult(OPTIONS, FLAT_DATA, new double[] {1, 1, 1, 1 }, getInitialValues(), FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInitialValues() {
    getFitter().getFitResult(OPTIONS, FLAT_DATA, ERRORS, null, FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthInitialValues() {
    getFitter().getFitResult(OPTIONS, FLAT_DATA, ERRORS, new double[] {1, 1, 1, 1, 1, 1, 1 }, FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixed() {
    getFitter().getFitResult(OPTIONS, FLAT_DATA, ERRORS, getInitialValues(), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongMaturity() {
    final EuropeanVanillaOption[] options = Arrays.copyOf(OPTIONS, OPTIONS.length);
    options[5] = new EuropeanVanillaOption(OPTIONS[5].getStrike(), OPTIONS[5].getTimeToExpiry() + 0.05, OPTIONS[5].isCall());
    getFitter().getFitResult(options, FLAT_DATA, ERRORS, getInitialValues(), FIXED);
  }

}
