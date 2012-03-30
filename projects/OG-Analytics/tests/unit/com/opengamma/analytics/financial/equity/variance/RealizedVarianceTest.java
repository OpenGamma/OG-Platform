/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.analytics.financial.equity.variance.pricing.RealizedVariance;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class RealizedVarianceTest {

  // -------------------------------- SETUP ------------------------------------------

  final RealizedVariance realVariance = new RealizedVariance();

  // The derivative
  final double varStrike = 0.05;
  final double varNotional = 3150;
  final double expiry = 5;
  final int nObsExpected = 750;
  final int noMktDisruptions = 0;
  final double annualizationFactor = 252;

  double[] noObs = {};
  double[] defaultWeights = {};
  final VarianceSwap swapNull = new VarianceSwap(0, expiry, expiry, varStrike, varNotional, Currency.EUR, annualizationFactor, nObsExpected, noMktDisruptions, noObs, defaultWeights);
  double[] oneObs = {100.0 };
  final VarianceSwap swapOneObs = new VarianceSwap(0, expiry, expiry, varStrike, varNotional, Currency.EUR, annualizationFactor, nObsExpected, noMktDisruptions, oneObs, defaultWeights);

  double[] twoObs = {100.0, 150.0 };
  final VarianceSwap swapTwoObs = new VarianceSwap(0, expiry, expiry, varStrike, varNotional, Currency.EUR, annualizationFactor, 1, noMktDisruptions, twoObs, defaultWeights);

  double[] threeObs = {100.0, 150.0, 100.0 };
  final VarianceSwap swapThreeObs = new VarianceSwap(0, expiry, expiry, varStrike, varNotional, Currency.EUR, annualizationFactor, 2, noMktDisruptions, threeObs, defaultWeights);

  double[] ObsWithZero = {100.0, 150.0, 0.0 };
  final VarianceSwap swapWithZeroObs = new VarianceSwap(0, expiry, expiry, varStrike, varNotional, Currency.EUR, annualizationFactor, 2, noMktDisruptions, ObsWithZero, defaultWeights);

  // -------------------------------- TESTS ------------------------------------------

  @Test
  public void testNullObs() {
    assertEquals(realVariance.evaluate(swapNull), 0.0, 1e-9);
  }

  @Test
  public void testOneObs() {
    assertEquals(realVariance.evaluate(swapOneObs), 0.0, 1e-9);
  }

  @Test
  public void testTwoObs() {
    assertEquals(realVariance.evaluate(swapTwoObs), annualizationFactor * FunctionUtils.square(Math.log(1.5)), 1e-9);
  }

  @Test
  public void testThreeObs() {
    assertEquals(realVariance.evaluate(swapThreeObs), annualizationFactor * FunctionUtils.square(Math.log(1.5)), 1e-9);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroInTimeSeries() {
    realVariance.evaluate(swapWithZeroObs);
  }
}
