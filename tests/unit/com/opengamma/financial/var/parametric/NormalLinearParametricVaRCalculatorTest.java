/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * @author emcleod
 * 
 */
public class NormalLinearParametricVaRCalculatorTest {
  private static final ParametricVaRCalculator VAR = new NormalLinearParametricVaRCalculator();
  private static final DoubleMatrix2D M = DoubleFactory2D.dense.make(new double[][] { { 3, 4 }, { 1, 5 } });
  private static final DoubleMatrix1D V = DoubleFactory1D.dense.make(new double[] { 0.4, 0.6 });
  private static final double HORIZON = 10;
  private static final double PERIOD = 250;
  private static final double STD = 3;
  private static final double QUANTILE = new NormalDistribution(0, 1).getCDF(STD);

  @Test(expected = IllegalArgumentException.class)
  public void testMatrix() {
    VAR.evaluate(null, V, 1, 1, 0.9);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testVector() {
    VAR.evaluate(M, null, 1, 1, 0.9);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHorizon() {
    VAR.evaluate(M, V, -1, HORIZON, QUANTILE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPeriod() {
    VAR.evaluate(M, V, PERIOD, -1, QUANTILE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowQuantile() {
    VAR.evaluate(M, V, PERIOD, HORIZON, -0.5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighQuantile() {
    VAR.evaluate(M, V, PERIOD, HORIZON, 1.3);
  }

  @Test
  public void test() {
    assertEquals(VAR.evaluate(M, V, PERIOD, HORIZON, QUANTILE), Math.sqrt(3.48 * HORIZON / PERIOD) * STD, 1e-9);
  }
}
