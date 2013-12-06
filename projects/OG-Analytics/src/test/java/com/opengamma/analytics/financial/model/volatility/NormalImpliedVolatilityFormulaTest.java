/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.util.test.TestGroup;

/**
 * Test related to the implied volatility from the price in a normally distributed asset price world.
 */
@Test(groups = TestGroup.UNIT)
public class NormalImpliedVolatilityFormulaTest {
  private static final double FORWARD = 100.0;
  private static final double DF = 0.87;
  private static final double T = 4.5;
  private static final NormalFunctionData[] DATA;
  private static final EuropeanVanillaOption[] OPTIONS;
  private static final double[] PRICES;
  private static final double[] STRIKES;
  private static final double[] SIGMA;
  private static final NormalPriceFunction FUNCTION = new NormalPriceFunction();
  private static final int N = 10;

  static {
    PRICES = new double[N];
    STRIKES = new double[N];
    SIGMA = new double[N];
    DATA = new NormalFunctionData[N];
    OPTIONS = new EuropeanVanillaOption[N];
    for (int i = 0; i < N; i++) {
      STRIKES[i] = FORWARD + (-N / 2 + i) * 10;
      SIGMA[i] = FORWARD * (0.05 + 4.0 * i / 100.0);
      DATA[i] = new NormalFunctionData(FORWARD, DF, SIGMA[i]);
      OPTIONS[i] = new EuropeanVanillaOption(STRIKES[i], T, true);
      PRICES[i] = FUNCTION.getPriceFunction(OPTIONS[i]).evaluate(DATA[i]);
    }
  }

  @Test
  public void testImpliedVolatility() {
    final NormalImpliedVolatilityFormula formula = new NormalImpliedVolatilityFormula();
    double[] impliedVolatility = new double[N];
    for (int i = 0; i < N; i++) {
      impliedVolatility[i] = formula.getImpliedVolatility(DATA[i], OPTIONS[i], PRICES[i]);
      assertEquals(SIGMA[i], impliedVolatility[i], 1e-6);
    }
  }
}
