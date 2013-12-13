/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
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
  public void testAtmPrices() {
    final BlackFormula call = new BlackFormula(FORWARD, FORWARD, T, SIGMA, null, true);
    final BlackFormula put = new BlackFormula(FORWARD, FORWARD, T, SIGMA, null, false);

    assertEquals(call.computePrice(), put.computePrice(), EPS);
    // System.err.println("atm put price = " + put.computePrice());
  }

  @Test
  public void testAtmForwardDeltas() {
    final double deltaneutralstrike = FORWARD * Math.exp(0.5 * SIGMA * SIGMA * T);
    final BlackFormula deltaneutralcall = new BlackFormula(FORWARD, deltaneutralstrike, T, SIGMA, null, true);
    final BlackFormula deltaneutralput = new BlackFormula(FORWARD, deltaneutralstrike, T, SIGMA, null, false);

    assertEquals(0.0, deltaneutralcall.computeForwardDelta() + deltaneutralput.computeForwardDelta(), EPS);

    final BlackFormula atmcall = new BlackFormula(FORWARD, FORWARD, T, SIGMA, null, true);
    final BlackFormula atmput = new BlackFormula(FORWARD, FORWARD, T, SIGMA, null, false);

    assertEquals(atmcall.computeForwardDelta() - 0.5, atmput.computeForwardDelta() + 0.5, EPS);

    /* System.err.println("deltaneutralstrike = " + deltaneutralstrike);
    System.err.println("delta neutral put = " + deltaneutralput.computeForwardDelta());
    System.err.println("delta neutral call = " + deltaneutralcall.computeForwardDelta());
    System.err.println("delta of atmcall = " + atmcall.computeForwardDelta());
    System.err.println("delta of atmput = " + atmput.computeForwardDelta());
    System.err.println("atmcall - 0.5 = " + (atmcall.computeForwardDelta()-0.5));
    System.err.println("atmput - 0.5 = " + (0.5 + atmput.computeForwardDelta()));
     */
  }

  @Test
  public void testStrikeImpliedFromFwdDelta() {

    final double strike50Call = FORMULA.computeStrikeImpliedByForwardDelta(0.5, true);
    assertEquals(FORWARD * Math.exp(0.5 * SIGMA * SIGMA * T), strike50Call);

    final double strike50Put = FORMULA.computeStrikeImpliedByForwardDelta(-0.5, false);
    assertEquals(strike50Call, strike50Put, EPS);

    final double strike10Call = FORMULA.computeStrikeImpliedByForwardDelta(0.10, true);
    final double strike25Call = FORMULA.computeStrikeImpliedByForwardDelta(0.25, true);
    final double strike75Call = FORMULA.computeStrikeImpliedByForwardDelta(0.75, true);
    final double strike90Call = FORMULA.computeStrikeImpliedByForwardDelta(0.90, true);
    // System.err.println("strike90Call = " + strike90Call);

    final double strike90Put = FORMULA.computeStrikeImpliedByForwardDelta(-0.90, false);
    final double strike75Put = FORMULA.computeStrikeImpliedByForwardDelta(-0.75, false);
    final double strike25Put = FORMULA.computeStrikeImpliedByForwardDelta(-0.25, false);
    final double strike10Put = FORMULA.computeStrikeImpliedByForwardDelta(-0.10, false);

    assertEquals(strike10Call, strike90Put, EPS);
    assertEquals(strike25Call, strike75Put, EPS);
    assertEquals(strike75Call, strike25Put, EPS);
    assertEquals(strike90Call, strike10Put, EPS);

    assertTrue((strike10Call > strike25Call) && (strike25Call > strike50Call) && (strike50Call > strike75Call) && (strike75Call > strike90Call));

  }

  @Test
  public void testStrikeImpliedFromFwdDeltaEasy() {
    final double strike25PutEasy = FORMULA.computeStrikeImpliedByForwardDelta(-0.25, false);
    final double strike25PutHard = FORMULA.computeStrikeImpliedByDeltaViaRootFinding(0.25, false);
    assertEquals(strike25PutEasy, strike25PutHard, EPS);

    final double strike75PutEasy = FORMULA.computeStrikeImpliedByForwardDelta(-0.75, false);
    final double strike75PutHard = FORMULA.computeStrikeImpliedByDeltaViaRootFinding(0.75, false);
    assertEquals(strike75PutEasy, strike75PutHard, EPS);
  }

}
