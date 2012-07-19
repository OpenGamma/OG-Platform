/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

public class BlackFormulaRepositotyTest {

  private static final double TIME_TO_EXPIRY = 4.5;
  private static final double FORWARD = 104;
  private static final double[] STRIKES_INPUT = new double[] {85.0, 90.0, 95.0, 100.0, 103.0, 108.0, 120.0, 150.0};
  private static final double[] VOLS = new double[] {0.30, 0.29, 0.28, 0.20, 0.24, 0.27, 0.28, 0.40};

  @Test
  /**
   * Tests the strikes in a range of strikes, volatilities and call/put.
   */
  public void impliedStrike() {
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
        strikeOutput[loopcall][loopstrike] = BlackFormulaRepository.impliedStrike(delta[loopcall][loopstrike], callput, FORWARD, TIME_TO_EXPIRY, VOLS[loopstrike]);
        assertEquals("Implied strike: (data " + loopstrike + " / " + callput + ")", STRIKES_INPUT[loopstrike], strikeOutput[loopcall][loopstrike], 1.0E-8);
      }
    }
  }

  // TODO: test the conditions.

  @Test
  /**
   * Tests the strikes in a range of strikes, volatilities and call/put.
   */
  public void impliedStrikeDerivatives() {
    double[] delta = new double[] {0.25, -0.25, 0.49};
    boolean[] cap = new boolean[] {true, false, true};
    double[] forward = new double[] {104, 100, 10};
    double[] time = new double[] {2.5, 5.0, 0.5};
    double[] vol = new double[] {0.25, 0.10, 0.50};
    double shift = 0.000001;
    double shiftF = 0.001;
    double[] derivatives = new double[4];
    for (int loop = 0; loop < delta.length; loop++) {
      double strike = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop], time[loop], vol[loop], derivatives);
      double strikeD = BlackFormulaRepository.impliedStrike(delta[loop] + shift, cap[loop], forward[loop], time[loop], vol[loop]);
      assertEquals("Implied strike: derivative delta", (strikeD - strike) / shift, derivatives[0], 1.0E-3);
      double strikeF = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop] + shiftF, time[loop], vol[loop]);
      assertEquals("Implied strike: derivative forward", (strikeF - strike) / shiftF, derivatives[1], 1.0E-5);
      double strikeT = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop], time[loop] + shift, vol[loop]);
      assertEquals("Implied strike: derivative time", (strikeT - strike) / shift, derivatives[2], 1.0E-4);
      double strikeV = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop], time[loop], vol[loop] + shift);
      assertEquals("Implied strike: derivative volatility", (strikeV - strike) / shift, derivatives[3], 1.0E-3);
    }
  }

  @Test(enabled = false)
  /**
   * Assess the performance of the derivatives computation.
   */
  public void impliedStrikePerformanceDerivatives() {
    double[] delta = new double[] {0.25, -0.25, 0.49};
    boolean[] cap = new boolean[] {true, false, true};
    double[] forward = new double[] {104, 100, 10};
    double[] time = new double[] {2.5, 5.0, 0.5};
    double[] vol = new double[] {0.25, 0.10, 0.50};
    double[] derivatives = new double[4];

    long startTime, endTime;
    int nbTest = 100000;
    @SuppressWarnings("unused")
    double strike;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      for (int loop = 0; loop < delta.length; loop++) {
        strike = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop], time[loop], vol[loop]);
      }
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " implied strike: " + (endTime - startTime) + " ms");
    // Performance note: strike: 18-Jul-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 70 ms for 100000.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      for (int loop = 0; loop < delta.length; loop++) {
        strike = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop], time[loop], vol[loop], derivatives);
      }
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " implied strike + derivatives : " + (endTime - startTime) + " ms");
    // Performance note: strike+derivatives: 18-Jul-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 80 ms for 100000.
  }

}
