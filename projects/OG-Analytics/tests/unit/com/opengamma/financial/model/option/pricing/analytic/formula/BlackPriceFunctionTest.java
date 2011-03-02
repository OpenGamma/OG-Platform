/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class BlackPriceFunctionTest {
  private static final double T = 4.5;
  private static final double F = 104;
  private static final double DELTA = 10;
  private static final EuropeanVanillaOption ATM_CALL = new EuropeanVanillaOption(F, T, true);
  private static final EuropeanVanillaOption ITM_CALL = new EuropeanVanillaOption(F - DELTA, T, true);
  private static final EuropeanVanillaOption OTM_CALL = new EuropeanVanillaOption(F + DELTA, T, true);
  private static final EuropeanVanillaOption ITM_PUT = new EuropeanVanillaOption(F + DELTA, T, false);
  private static final EuropeanVanillaOption OTM_PUT = new EuropeanVanillaOption(F - DELTA, T, false);
  private static final double DF = 0.9;
  private static final double SIGMA = 0.5;
  private static final BlackFunctionData ATM_DATA = new BlackFunctionData(F, DF, SIGMA);
  private static final BlackFunctionData ZERO_VOL_DATA = new BlackFunctionData(F, DF, 0);
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final BlackPriceFunction FUNCTION = new BlackPriceFunction();

  @Test(expected = IllegalArgumentException.class)
  public void testNullOption1() {
    FUNCTION.getPriceFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullOption2() {
    FUNCTION.getPriceFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData1() {
    FUNCTION.getPriceFunction(ATM_CALL).evaluate((BlackFunctionData) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData2() {
    FUNCTION.getVegaFunction(ATM_CALL).evaluate((BlackFunctionData) null);
  }

  @Test
  public void testATMPrice() {
    final double sigmaRootT = ATM_DATA.getSigma() * Math.sqrt(ATM_CALL.getT());
    assertEquals(DF * F * (2 * NORMAL.getCDF(sigmaRootT / 2) - 1), FUNCTION.getPriceFunction(ATM_CALL).evaluate(ATM_DATA), 1e-15);
  }

  @Test
  public void testZeroVolPrice() {
    assertEquals(DF * DELTA, FUNCTION.getPriceFunction(ITM_CALL).evaluate(ZERO_VOL_DATA), 1e-15);
    assertEquals(0, FUNCTION.getPriceFunction(OTM_CALL).evaluate(ZERO_VOL_DATA), 1e-15);
    assertEquals(DF * DELTA, FUNCTION.getPriceFunction(ITM_PUT).evaluate(ZERO_VOL_DATA), 1e-15);
    assertEquals(0, FUNCTION.getPriceFunction(OTM_PUT).evaluate(ZERO_VOL_DATA), 1e-15);
  }
}
