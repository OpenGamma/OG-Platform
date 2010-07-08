/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class DoubleCurveFinderTest {
  private static final List<InterestRateDerivative> DERIVATIVES;
  private static final double[] MARKET_RATES;
  private static final double SPOT_RATE = 0.03;
  private static final double[] FORWARD_NODES;
  private static final double[] FUNDING_NODES;
  private static final YieldAndDiscountCurve FORWARD_CURVE = new ConstantYieldCurve(SPOT_RATE);
  private static final YieldAndDiscountCurve FUNDING_CURVE = new ConstantYieldCurve(SPOT_RATE);
  private static final Interpolator1D<Interpolator1DDataBundle, InterpolationResult> INTERPOLATOR = new LinearInterpolator1D();
  private static final int N = 10;
  private static final DoubleCurveFinder FINDER;

  static {
    DERIVATIVES = new ArrayList<InterestRateDerivative>();
    MARKET_RATES = new double[2 * N];
    FORWARD_NODES = new double[N];
    FUNDING_NODES = new double[N];
    for (int i = 0; i < 2 * N; i++) {
      DERIVATIVES.add(new Cash(((double) i) / 2));
      MARKET_RATES[i] = SPOT_RATE + 1;
      if (i % 2 == 0) {
        FORWARD_NODES[i / 2] = i / 2 + 1;
      }
      FUNDING_NODES[i / 2] = FORWARD_NODES[i / 2];
    }
    FINDER = new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, SPOT_RATE, FORWARD_NODES, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDerivatives() {
    new DoubleCurveFinder(null, MARKET_RATES, SPOT_RATE, FORWARD_NODES, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMarketRates() {
    new DoubleCurveFinder(DERIVATIVES, null, SPOT_RATE, FORWARD_NODES, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullForwardInterpolator() {
    new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, SPOT_RATE, FORWARD_NODES, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, null, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingInterpolator() {
    new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, SPOT_RATE, FORWARD_NODES, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyForwardNodes() {
    new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, SPOT_RATE, new double[0], FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyFundingNodes() {
    new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, SPOT_RATE, FORWARD_NODES, new double[0], FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyDerivatives() {
    new DoubleCurveFinder(new ArrayList<InterestRateDerivative>(), MARKET_RATES, SPOT_RATE, FORWARD_NODES, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMismatchingData() {
    new DoubleCurveFinder(DERIVATIVES, new double[] {1, 2, 3, 4, 5}, SPOT_RATE, FORWARD_NODES, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMismatchingNodes() {
    new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, SPOT_RATE, new double[] {1}, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullVector() {
    FINDER.evaluate((DoubleMatrix1D) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongVector() {
    FINDER.evaluate(new DoubleMatrix1D(new double[] {1, 2, 3}));
  }

  @Test
  public void test() {
    final DoubleMatrix1D x = new DoubleMatrix1D(MARKET_RATES);
    DoubleMatrix1D result = FINDER.evaluate(x);
    assertEquals(result.getNumberOfElements(), 2 * N);
    assertEquals(result.getEntry(0), SPOT_RATE - MARKET_RATES[0], 0);
    assertEquals(result.getEntry(1), (SPOT_RATE - MARKET_RATES[1]) / 2, 0);
    final double[] nodes = new double[2 * N];
    for (int i = 0; i < 2 * N; i++) {
      nodes[i] = i;
    }
    DoubleCurveFinder finder = new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, SPOT_RATE, nodes, null, FORWARD_CURVE, null, INTERPOLATOR, null);
    result = finder.evaluate(x);
    for (int i = 0; i < 2 * N; i++) {
      assertEquals(result.getEntry(i), SPOT_RATE - MARKET_RATES[i], 0);
    }
    finder = new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, SPOT_RATE, null, nodes, null, FUNDING_CURVE, null, INTERPOLATOR);
    result = finder.evaluate(x);
    for (int i = 0; i < 2 * N; i++) {
      assertEquals(result.getEntry(i), 0, 0);
    }
    finder = new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, SPOT_RATE, FORWARD_NODES, FUNDING_NODES, null, null, INTERPOLATOR, INTERPOLATOR);
    result = finder.evaluate(x);
    assertEquals(result.getNumberOfElements(), 2 * N);
    assertEquals(result.getEntry(0), SPOT_RATE - MARKET_RATES[0], 0);
    assertEquals(result.getEntry(1), (SPOT_RATE - MARKET_RATES[1]) / 2, 0);
    for (int i = 2; i < 2 * N; i++) {
      assertEquals(result.getEntry(i), 0, 0);
    }
  }
}
