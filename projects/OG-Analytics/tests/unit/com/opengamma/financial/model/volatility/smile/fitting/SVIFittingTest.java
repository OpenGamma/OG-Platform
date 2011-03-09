/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.BitSet;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;

/**
 * 
 */
public class SVIFittingTest {
  private static final double VARIANCE = 0.3;
  private static final double T = 1.3;
  private static final double SIGMA = Math.sqrt(VARIANCE / T);
  private static final double ERROR = 0.00001;
  private static final EuropeanVanillaOption[] OPTIONS;
  private static final BlackFunctionData DATA = new BlackFunctionData(100, 1, 0);
  private static final double[] BLACK_VOLS;
  private static final double[] ERRORS;
  private static final double[] INITIAL_VALUES;
  private static final BitSet FIXED = new BitSet();
  private static final SVILeastSquaresFitter FITTER = new SVILeastSquaresFitter();
  private static final double EPS = 1e-9;

  static {
    final int n = 100;
    final double kStart = 50;
    final double delta = kStart / n;
    OPTIONS = new EuropeanVanillaOption[n];
    BLACK_VOLS = new double[n];
    ERRORS = new double[n];
    for (int i = 0; i < n; i++) {
      OPTIONS[i] = new EuropeanVanillaOption(kStart + i * delta, T, true);
      BLACK_VOLS[i] = SIGMA;
      ERRORS[i] = ERROR;
    }
    INITIAL_VALUES = new double[] {0.01, 0.01, 0.01, 0.01, 0.01};
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullOptions() {
    FITTER.getFitResult(null, DATA, BLACK_VOLS, ERRORS, INITIAL_VALUES, FIXED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyOptions() {
    FITTER.getFitResult(new EuropeanVanillaOption[0], DATA, BLACK_VOLS, ERRORS, INITIAL_VALUES, FIXED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    FITTER.getFitResult(OPTIONS, null, BLACK_VOLS, ERRORS, INITIAL_VALUES, FIXED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullBlackVols() {
    FITTER.getFitResult(OPTIONS, DATA, null, ERRORS, INITIAL_VALUES, FIXED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLengthBlackVols() {
    FITTER.getFitResult(OPTIONS, DATA, new double[] {1, 1, 1}, ERRORS, INITIAL_VALUES, FIXED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullErrors() {
    FITTER.getFitResult(OPTIONS, DATA, BLACK_VOLS, null, INITIAL_VALUES, FIXED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLengthErrors() {
    FITTER.getFitResult(OPTIONS, DATA, BLACK_VOLS, new double[] {1, 1, 1, 1}, INITIAL_VALUES, FIXED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullInitialValues() {
    FITTER.getFitResult(OPTIONS, DATA, BLACK_VOLS, ERRORS, null, FIXED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLengthInitialValues() {
    FITTER.getFitResult(OPTIONS, DATA, BLACK_VOLS, ERRORS, new double[] {1, 1, 1, 1, 1, 1, 1}, FIXED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFixed() {
    FITTER.getFitResult(OPTIONS, DATA, BLACK_VOLS, ERRORS, INITIAL_VALUES, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongMaturity() {
    final EuropeanVanillaOption[] options = Arrays.copyOf(OPTIONS, OPTIONS.length);
    options[5] = new EuropeanVanillaOption(OPTIONS[5].getStrike(), OPTIONS[5].getTimeToExpiry() + 0.05, OPTIONS[5].isCall());
    FITTER.getFitResult(options, DATA, BLACK_VOLS, ERRORS, INITIAL_VALUES, FIXED);
  }

  // should have a = SIGMA, the variance level parameter, b = 0 and the others staying near their initial values and chiSq = 0
  @Test
  public void testSolutionFlatSurface() {
    final LeastSquareResults results = FITTER.getFitResult(OPTIONS, DATA, BLACK_VOLS, ERRORS, INITIAL_VALUES, FIXED);
    final DoubleMatrix1D parameters = results.getParameters();
    //assertEquals(parameters.getEntry(0), SIGMA, EPS);
    assertEquals(parameters.getEntry(1), 0, EPS);
    //    assertEquals(parameters.getEntry(2), INITIAL_VALUES[2], 1e-3);
    //    assertEquals(parameters.getEntry(3), INITIAL_VALUES[3], 1e-3);
    //    assertEquals(parameters.getEntry(4), INITIAL_VALUES[4], 1e-3);
    assertEquals(results.getChiSq(), 0, EPS);
  }
}
