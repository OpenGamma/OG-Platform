/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * Tests {@link NormalFromBlackImpliedVolatilityFormula}.
 */
@Test(groups = TestGroup.UNIT)
public class NormalFromBlackImpliedVolatilityFormulaTest {
  
  private static final double FORWARD = 100.0;
  private static final double DF = 0.87;
  private static final double T = 4.5;
  private static final int N = 10;
  private static final double[] STRIKES = new double[N];
  private static final double[] STRIKES_ATM = new double[N];
  private static final EuropeanVanillaOption[] OPTIONS = new EuropeanVanillaOption[N];
  private static final double[] SIGMA_BLACK = new double[N];
  private static final NormalPriceFunction FUNCTION_PRICE_NORMAL = new NormalPriceFunction();
  static {
    for (int i = 0; i < N; i++) {
      STRIKES[i] = FORWARD + (-0.5d * N + i) * 10.0d;
      STRIKES_ATM[i] = FORWARD + (-0.5d * N + i) / 100.0d;
      SIGMA_BLACK[i] = 0.20 + i / 100.0d;
      OPTIONS[i] = new EuropeanVanillaOption(STRIKES[i], T, true);
    }
  }
  private static final double TOLERANCE_PRICE = 1.0E-4;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrong_strike() {
    NormalImpliedVolatilityFormula.impliedVolatilityFromBlackApproximated(FORWARD, -1.0d, T, 0.20d);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrong_forward() {
    NormalImpliedVolatilityFormula.impliedVolatilityFromBlackApproximated(-1.0d, FORWARD, T, 0.20d);
  }

  @Test
  public void price_comparison() {
    priceCheck(STRIKES);
    priceCheck(STRIKES_ATM);
  }

  private void priceCheck(double[] strikes) {
    for (int i = 0; i < N; i++) {
      double ivNormalComputed = NormalImpliedVolatilityFormula
          .impliedVolatilityFromBlackApproximated(FORWARD, strikes[i], T, SIGMA_BLACK[i]);
      EuropeanVanillaOption o = new EuropeanVanillaOption(strikes[i], T, true);
      NormalFunctionData d = new NormalFunctionData(FORWARD, DF, ivNormalComputed);
      double priceNormalComputed = FUNCTION_PRICE_NORMAL.getPriceFunction(o).evaluate(d);
      double priceBlack = BlackFormulaRepository.price(FORWARD, strikes[i], T, SIGMA_BLACK[i], true) * DF;
      assertEquals(priceBlack, priceNormalComputed, TOLERANCE_PRICE);
    }
  }
  
}
