/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * Tests related to the construction of the data required to describe a delta dependent smile from ATM, risk reversal and strangle as used in Forex market.
 */
public class SmileDeltaParameterTest {

  private static final double TIME_TO_EXPIRY = 2.0;
  private static final double FORWARD = 1.40;
  private static final double ATM = 0.185;
  private static final double[] DELTA = new double[] {0.10, 0.25};
  private static final double[] RISK_REVERSAL = new double[] {-0.0130, -0.0050};
  private static final double[] STRANGLE = new double[] {0.0300, 0.0100};

  private static final SmileDeltaParameter SMILE = new SmileDeltaParameter(TIME_TO_EXPIRY, ATM, DELTA, RISK_REVERSAL, STRANGLE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDelta() {
    new SmileDeltaParameter(TIME_TO_EXPIRY, ATM, null, RISK_REVERSAL, STRANGLE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRRLength() {
    new SmileDeltaParameter(TIME_TO_EXPIRY, ATM, DELTA, new double[3], STRANGLE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStrangleLength() {
    new SmileDeltaParameter(TIME_TO_EXPIRY, ATM, DELTA, RISK_REVERSAL, new double[3]);
  }

  @Test
  /**
   * Tests the constructor directly from volatilities (not RR and S).
   */
  public void constructorVolatility() {
    double[] volatility = SMILE.getVolatility();
    SmileDeltaParameter smileFromVolatility = new SmileDeltaParameter(TIME_TO_EXPIRY, DELTA, volatility);
    assertEquals("Smile by delta: constructor", SMILE, smileFromVolatility);
  }

  @Test
  /**
   * Tests the getters.
   */
  public void getter() {
    assertEquals("Smile by delta: time to expiry", TIME_TO_EXPIRY, SMILE.getTimeToExpiry());
    assertEquals("Smile by delta: delta", DELTA, SMILE.getDelta());
    SmileDeltaParameter smile2 = new SmileDeltaParameter(TIME_TO_EXPIRY, DELTA, SMILE.getVolatility());
    assertEquals("Smile by delta: volatility", SMILE.getVolatility(), smile2.getVolatility());
  }

  @Test
  /**
   * Tests the volatility computations.
   */
  public void volatility() {
    double[] volatility = SMILE.getVolatility();
    int nbDelta = DELTA.length;
    assertEquals("Volatility: ATM", ATM, volatility[nbDelta]);
    for (int loopdelta = 0; loopdelta < nbDelta; loopdelta++) {
      assertEquals("Volatility: Risk Reversal " + loopdelta, RISK_REVERSAL[loopdelta], volatility[2 * nbDelta - loopdelta] - volatility[loopdelta], 1.0E-8);
      assertEquals("Volatility: Strangle " + loopdelta, STRANGLE[loopdelta], (volatility[2 * nbDelta - loopdelta] + volatility[loopdelta]) / 2 - volatility[nbDelta], 1.0E-8);
    }
  }

  @Test
  /**
   * Tests the strikes computations.
   */
  public void strike() {
    double[] strike = SMILE.getStrike(FORWARD);
    BlackPriceFunction function = new BlackPriceFunction();
    double[] volatility = SMILE.getVolatility();
    int nbDelta = DELTA.length;
    for (int loopdelta = 0; loopdelta < nbDelta; loopdelta++) {
      BlackFunctionData dataPut = new BlackFunctionData(FORWARD, 1.0, volatility[loopdelta]);
      EuropeanVanillaOption optionPut = new EuropeanVanillaOption(strike[loopdelta], TIME_TO_EXPIRY, false);
      double[] dPut = function.getPriceAdjoint(optionPut, dataPut);
      assertEquals("Strike: Put " + loopdelta, dPut[1], -DELTA[loopdelta], 1.0E-8);
      BlackFunctionData dataCall = new BlackFunctionData(FORWARD, 1.0, volatility[2 * nbDelta - loopdelta]);
      EuropeanVanillaOption optionCall = new EuropeanVanillaOption(strike[2 * nbDelta - loopdelta], TIME_TO_EXPIRY, true);
      double[] dCall = function.getPriceAdjoint(optionCall, dataCall);
      assertEquals("Strike: Call " + loopdelta, dCall[1], DELTA[loopdelta], 1.0E-8);
    }
    BlackFunctionData data = new BlackFunctionData(FORWARD, 1.0, volatility[nbDelta]);
    EuropeanVanillaOption optionPut = new EuropeanVanillaOption(strike[nbDelta], TIME_TO_EXPIRY, false);
    double[] dPut = function.getPriceAdjoint(optionPut, data);
    EuropeanVanillaOption optionCall = new EuropeanVanillaOption(strike[nbDelta], TIME_TO_EXPIRY, true);
    double[] dCall = function.getPriceAdjoint(optionCall, data);
    assertEquals("Strike: ATM", dCall[1] + dPut[1], 0.0, 1.0E-8);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    int nbDelta = DELTA.length;
    long startTime, endTime;
    final int nbTest = 1000;
    SmileDeltaParameter[] smile = new SmileDeltaParameter[nbTest];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      smile[looptest] = new SmileDeltaParameter(TIME_TO_EXPIRY, ATM, DELTA, RISK_REVERSAL, STRANGLE);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " smile from ATM/RR/S in delta term: " + (endTime - startTime) + " ms");
    startTime = System.currentTimeMillis();
    double[][] strikes = new double[nbTest][2 * nbDelta + 1];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      strikes[looptest] = SMILE.getStrike(FORWARD);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " smile from ATM/RR/S in delta term: " + (endTime - startTime) + " ms");
  }

}
