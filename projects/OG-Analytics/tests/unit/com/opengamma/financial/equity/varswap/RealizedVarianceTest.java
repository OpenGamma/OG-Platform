/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap;

import static org.testng.AssertJUnit.assertEquals;

import com.opengamma.financial.equity.varswap.derivative.VarianceSwap;
import com.opengamma.financial.equity.varswap.pricing.RealizedVariance;
import com.opengamma.math.FunctionUtils;
import com.opengamma.util.money.Currency;

import org.testng.annotations.Test;

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
  final double annualizationFactor = 252;

  final VarianceSwap swapNull = new VarianceSwap(0, expiry, expiry, varStrike, varNotional, Currency.EUR, annualizationFactor, nObsExpected, null, null);
  Double[] oneObs = {100.0 };
  final VarianceSwap swapOneObs = new VarianceSwap(0, expiry, expiry, varStrike, varNotional, Currency.EUR, annualizationFactor, nObsExpected, oneObs, null);

  Double[] twoObs = {100.0, 150.0 };
  final VarianceSwap swapTwoObs = new VarianceSwap(0, expiry, expiry, varStrike, varNotional, Currency.EUR, annualizationFactor, 1, twoObs, null);

  Double[] threeObs = {100.0, 150.0, 100.0 };
  final VarianceSwap swapThreeObs = new VarianceSwap(0, expiry, expiry, varStrike, varNotional, Currency.EUR, annualizationFactor, 2, threeObs, null);

  Double[] ObsWithZero = {100.0, 150.0, 0.0 };
  final VarianceSwap swapWithZeroObs = new VarianceSwap(0, expiry, expiry, varStrike, varNotional, Currency.EUR, annualizationFactor, 2, ObsWithZero, null);

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
  // TODO How come I don't see the string in Validate.isTrue when I run this?
  public void testZeroInTimeSeries() {
    realVariance.evaluate(swapWithZeroObs);
  }
}
