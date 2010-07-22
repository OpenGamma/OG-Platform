/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;


/**
 * 
 */
public class DoubleCurveFinderTest {
  // private static final List<InterestRateDerivative> DERIVATIVES;
  // private static final double[] MARKET_RATES;
  // private static final double[] FORWARD_NODES;
  // private static final double[] FUNDING_NODES;
  // private static final double FLAT_RATE = 0.05;
  // private static final YieldAndDiscountCurve FORWARD_CURVE = new ConstantYieldCurve(FLAT_RATE);
  // private static final YieldAndDiscountCurve FUNDING_CURVE = new ConstantYieldCurve(FLAT_RATE);
  // private static final Interpolator1D<Interpolator1DDataBundle, InterpolationResult> INTERPOLATOR = new LinearInterpolator1D();
  // private static final int N = 10;
  // private static final DoubleCurveFinder FINDER;
  //
  // static {
  // DERIVATIVES = new ArrayList<InterestRateDerivative>();
  // MARKET_RATES = new double[2 * N];
  // FORWARD_NODES = new double[N];
  // FUNDING_NODES = new double[N];
  // for (int i = 0; i < 2 * N; i++) {
  // DERIVATIVES.add(new Cash(((double) i) / 2));
  // MARKET_RATES[i] = FLAT_RATE + 1.0;
  // if (i % 2 == 0) {
  // FORWARD_NODES[i / 2] = i / 2 + 1;
  // }
  // FUNDING_NODES[i / 2] = FORWARD_NODES[i / 2];
  // }
  // FINDER = new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, FORWARD_NODES, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullDerivatives() {
  // new DoubleCurveFinder(null, MARKET_RATES, FORWARD_NODES, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullMarketRates() {
  // new DoubleCurveFinder(DERIVATIVES, null, FORWARD_NODES, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullForwardInterpolator() {
  // new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, FORWARD_NODES, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, null, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullFundingInterpolator() {
  // new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, FORWARD_NODES, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, null);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testEmptyForwardNodes() {
  // new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, new double[0], FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testEmptyFundingNodes() {
  // new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, FORWARD_NODES, new double[0], FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testEmptyDerivatives() {
  // new DoubleCurveFinder(new ArrayList<InterestRateDerivative>(), MARKET_RATES, FORWARD_NODES, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testMismatchingData() {
  // new DoubleCurveFinder(DERIVATIVES, new double[] {1, 2, 3, 4, 5}, FORWARD_NODES, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testMismatchingNodes() {
  // new DoubleCurveFinder(DERIVATIVES, MARKET_RATES, new double[] {1}, FUNDING_NODES, FORWARD_CURVE, FUNDING_CURVE, INTERPOLATOR, INTERPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullVector() {
  // FINDER.evaluate((DoubleMatrix1D) null);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testWrongVector() {
  // FINDER.evaluate(new DoubleMatrix1D(new double[] {1, 2, 3}));
  // }

}
