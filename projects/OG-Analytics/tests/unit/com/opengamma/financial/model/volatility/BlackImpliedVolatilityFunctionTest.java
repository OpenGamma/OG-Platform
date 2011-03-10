/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * 
 */
public class BlackImpliedVolatilityFunctionTest {
  private static final double FORWARD = 134.5;
  private static final double DF = 0.87;
  private static final double T = 4.5;
  private static final double SIGMA = 0.2;
  private static final BlackFunctionData[] DATA;
  private static final EuropeanVanillaOption[] OPTIONS;
  private static final double[] PRICES;
  private static final double[] STRIKES;
  private static final BlackPriceFunction FORMULA = new BlackPriceFunction();
  private static final int N = 10;

  static {
    PRICES = new double[N];
    STRIKES = new double[N];
    DATA = new BlackFunctionData[N];
    OPTIONS = new EuropeanVanillaOption[N];
    for (int i = 0; i < 10; i++) {
      STRIKES[i] = 50 + 2 * i;
      DATA[i] = new BlackFunctionData(FORWARD, DF, SIGMA);
      OPTIONS[i] = new EuropeanVanillaOption(STRIKES[i], T, true);
      PRICES[i] = FORMULA.getPriceFunction(OPTIONS[i]).evaluate(DATA[i]);
    }
  }

  @Test
  public void test() {
    final BlackImpliedVolatilityFormula formula = new BlackImpliedVolatilityFormula();
    for (int i = 0; i < N; i++) {
      final double vol = formula.getImpliedVolatility(DATA[i], OPTIONS[i], PRICES[i]);
      assertEquals(SIGMA, vol, 1e-6);
    }
  }
}
