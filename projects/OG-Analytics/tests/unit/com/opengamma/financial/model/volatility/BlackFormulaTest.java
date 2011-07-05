/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

/**
 * 
 */
public class BlackFormulaTest {
  private static final double FORWARD = 134.5;
  private static final double T = 4.5;
  private static final double SIGMA = 0.2;
  private static final boolean ISCALL = false;
  // Construct an at-the-money BlackFormula with null price. Set params as required thereafter 
  private static final BlackFormula FORMULA = new BlackFormula(FORWARD, FORWARD, T, SIGMA, null, ISCALL);
  private static final double EPS = 1.0E-9;
  private static final int N = 10;
  private static final double[] PRICES;
  private static final double[] STRIKES;

  static {
    PRICES = new double[N];
    STRIKES = new double[N];
    for (int i = 0; i < N; i++) {
      STRIKES[i] = FORWARD * (0.5 + i / (N - 1));
      FORMULA.setStrike(STRIKES[i]);
      PRICES[i] = FORMULA.computePrice();
    }
  }

  @Test
  public void testImpliedVols() {
    for (int i = 0; i < N; i++) {
      FORMULA.setStrike(STRIKES[i]);
      FORMULA.setMtm(PRICES[i]);
      final double vol = FORMULA.computeImpliedVolatility();
      assertEquals(SIGMA, vol, 1e-6);
    }
  }

  @Test
  public void testStrikeImpliedFromFwdDelta() {

    double strike50Call = FORMULA.computeStrikeImpliedByForwardDelta(0.5, true);
    assertEquals(FORWARD * Math.exp(0.5 * SIGMA * SIGMA * T), strike50Call);

    double strike50Put = FORMULA.computeStrikeImpliedByForwardDelta(0.5, false);
    assertEquals(strike50Call, strike50Put, EPS);

    double strike10Call = FORMULA.computeStrikeImpliedByForwardDelta(0.10, true);
    double strike25Call = FORMULA.computeStrikeImpliedByForwardDelta(0.25, true);
    double strike75Call = FORMULA.computeStrikeImpliedByForwardDelta(0.75, true);
    double strike90Call = FORMULA.computeStrikeImpliedByForwardDelta(0.90, true);
    // System.err.println("strike90Call = " + strike90Call);

    double strike90Put = FORMULA.computeStrikeImpliedByForwardDelta(0.90, false);
    double strike75Put = FORMULA.computeStrikeImpliedByForwardDelta(0.75, false);
    double strike25Put = FORMULA.computeStrikeImpliedByForwardDelta(0.25, false);
    double strike10Put = FORMULA.computeStrikeImpliedByForwardDelta(0.10, false);

    assertEquals(strike10Call, strike90Put, EPS);
    assertEquals(strike25Call, strike75Put, EPS);
    assertEquals(strike75Call, strike25Put, EPS);
    assertEquals(strike90Call, strike10Put, EPS);

    assertTrue((strike10Call > strike25Call) && (strike25Call > strike50Call) && (strike50Call > strike75Call) && (strike75Call > strike90Call));

  }

  @Test
  public void testStrikeImpliedFromFwdDeltaEasy() {
    double strike25PutEasy = FORMULA.computeStrikeImpliedByForwardDelta(0.25, false);
    double strike25PutHard = FORMULA.computeStrikeImpliedByDeltaViaRootFinding(0.25, false);
    assertEquals(strike25PutEasy, strike25PutHard, EPS);

    double strike75PutEasy = FORMULA.computeStrikeImpliedByForwardDelta(0.75, false);
    double strike75PutHard = FORMULA.computeStrikeImpliedByDeltaViaRootFinding(0.75, false);
    assertEquals(strike75PutEasy, strike75PutHard, EPS);
  }

}
