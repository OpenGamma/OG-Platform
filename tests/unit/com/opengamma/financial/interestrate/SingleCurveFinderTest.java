/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;


/**
 * 
 */
public class SingleCurveFinderTest {
  // private static final List<InterestRateDerivative> DERIVATIVES;
  // private static final double[] MARKET_RATES;
  // private static final double[] NODES;
  // private static final Interpolator1D<Interpolator1DDataBundle, InterpolationResult> INTERPOLATOR = new LinearInterpolator1D();
  // private static final SingleCurveFinder FINDER;
  // static {
  // final int n = 10;
  // DERIVATIVES = new ArrayList<InterestRateDerivative>();
  // MARKET_RATES = new double[n];
  // NODES = new double[n];
  // for (int i = 0; i < n; i++) {
  // DERIVATIVES.add(new Cash(i));
  // MARKET_RATES[i] = Math.random() / .9 * 0.05;
  // NODES[i] = i;
  // }
  // FINDER = new SingleCurveFinder(DERIVATIVES, MARKET_RATES, NODES, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullDerivatives() {
  // new SingleCurveFinder(null, MARKET_RATES, NODES, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullMarketRates() {
  // new SingleCurveFinder(DERIVATIVES, null, NODES, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullTimes() {
  // new SingleCurveFinder(DERIVATIVES, MARKET_RATES, null, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullInterpolator() {
  // new SingleCurveFinder(DERIVATIVES, MARKET_RATES, NODES, null);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testEmptyDerivatives() {
  // new SingleCurveFinder(new ArrayList<InterestRateDerivative>(), MARKET_RATES, NODES, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testEmptyRates() {
  // new SingleCurveFinder(DERIVATIVES, new double[0], NODES, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testEmptyTimes() {
  // new SingleCurveFinder(DERIVATIVES, MARKET_RATES, new double[0], INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testMismatchingData() {
  // new SingleCurveFinder(DERIVATIVES, new double[] {1, 2}, NODES, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullVector() {
  // FINDER.evaluate((DoubleMatrix1D) null);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testMismatchingVector() {
  // FINDER.evaluate(new DoubleMatrix1D(new double[] {1, 2, 3, 4, 5, 6, 7, 8}));
  // }
  //
  // @Test
  // public void test() {
  // final DoubleMatrix1D results = FINDER.evaluate(new DoubleMatrix1D(MARKET_RATES));
  // for (final double r : results.getData()) {
  // assertEquals(r, 0, 0);
  // }
  // }
}
