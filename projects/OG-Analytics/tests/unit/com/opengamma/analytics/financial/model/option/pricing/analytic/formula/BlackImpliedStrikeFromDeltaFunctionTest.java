/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackImpliedStrikeFromDeltaFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * Tests the strike computation from a delta in the Black formula.
 */
public class BlackImpliedStrikeFromDeltaFunctionTest {

  private static final double TIME_TO_EXPIRY = 4.5;
  private static final double FORWARD = 104;
  private static final double[] STRIKES_INPUT = new double[] {85.0, 90.0, 95.0, 100.0, 103.0, 108.0, 120.0, 150.0};
  private static final double[] VOLS = new double[] {0.30, 0.29, 0.28, 0.20, 0.24, 0.27, 0.28, 0.40};

  @Test
  /**
   * Tests the strikes in a range of strikes, volatilities and call/put.
   */
  public void strike() {
    BlackPriceFunction function = new BlackPriceFunction();
    int nbStrike = STRIKES_INPUT.length;
    double[][] delta = new double[2][nbStrike];
    double[][] strikeOutput = new double[2][nbStrike];
    boolean callput = false;
    for (int loopcall = 0; loopcall < 2; loopcall++) {
      callput = !callput;
      for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
        EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKES_INPUT[loopstrike], TIME_TO_EXPIRY, callput);
        BlackFunctionData data = new BlackFunctionData(FORWARD, 1.0, VOLS[loopstrike]);
        double[] d = function.getPriceAdjoint(option, data);
        delta[loopcall][loopstrike] = d[1];
        strikeOutput[loopcall][loopstrike] = BlackImpliedStrikeFromDeltaFunction.impliedStrike(delta[loopcall][loopstrike], callput, FORWARD, TIME_TO_EXPIRY, VOLS[loopstrike]);
        assertEquals("Implied strike: (data " + loopstrike + " / " + callput + ")", STRIKES_INPUT[loopstrike], strikeOutput[loopcall][loopstrike], 1.0E-8);
      }
    }
  }
}
