/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;

import com.opengamma.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;

/**
 * Tests related to the construction of the G2++ model with piecewise constant volatility. The computation of several model related factors are also tested.
 */
public class G2ppPiecewiseConstantModelTest {

  private static final double[] MEAN_REVERSION = new double[] {0.01, 0.30};
  private static final double[][] VOLATILITY = new double[][] { {0.01, 0.011, 0.012, 0.013, 0.014}, {0.01, 0.009, 0.008, 0.007, 0.006}};
  private static final double[] VOLATILITY_TIME = new double[] {0.5, 1.0, 2.0, 5.0};
  private static final double CORRELATION = -0.50;
  private static final G2ppPiecewiseConstantParameters MODEL_PARAMETERS = new G2ppPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY, VOLATILITY_TIME, CORRELATION);
  private static final G2ppPiecewiseConstantModel MODEL_G2PP = new G2ppPiecewiseConstantModel();

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals(MEAN_REVERSION, MODEL_PARAMETERS.getMeanReversion());
    assertEquals(CORRELATION, MODEL_PARAMETERS.getCorrelation());
    for (int loopperiod = 0; loopperiod < VOLATILITY[0].length; loopperiod++) {
      assertEquals(VOLATILITY[0][loopperiod], MODEL_PARAMETERS.getVolatility()[0].get(loopperiod));
      assertEquals(VOLATILITY[1][loopperiod], MODEL_PARAMETERS.getVolatility()[1].get(loopperiod));
    }
    double[] volTime = MODEL_PARAMETERS.getVolatilityTime();
    for (int loopperiod = 0; loopperiod < VOLATILITY_TIME.length; loopperiod++) {
      assertEquals(VOLATILITY_TIME[loopperiod], volTime[loopperiod + 1]);
    }
  }

  @Test
  /**
   * Tests the class setters.
   */
  public void setter() {
    double[] volReplaced = new double[] {0.02, 0.01};
    MODEL_PARAMETERS.setLastVolatilities(volReplaced);
    ArrayAsserts.assertArrayEquals("G2++: setter", volReplaced, MODEL_PARAMETERS.getLastVolatilities(), 1.0E-10);
    MODEL_PARAMETERS.setLastVolatilities(new double[] {VOLATILITY[0][VOLATILITY[0].length - 1], VOLATILITY[1][VOLATILITY[0].length - 1]});
    for (int loopperiod = 0; loopperiod < VOLATILITY[0].length; loopperiod++) {
      assertEquals("G2++: setter " + loopperiod, VOLATILITY[0][loopperiod], MODEL_PARAMETERS.getVolatility()[0].get(loopperiod));
      assertEquals("G2++: setter " + loopperiod, VOLATILITY[1][loopperiod], MODEL_PARAMETERS.getVolatility()[1].get(loopperiod));
    }
  }

  @Test
  /**
   * Tests the volatilityMaturityPart method.
   */
  public void volatilityMaturityPart() {
    double u = 0.75;
    double[] v = new double[] {2.0, 4.0, 7.0};
    double[][] h = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, u, v);
    double[][] hExpected = new double[][] { {1.232938151, 3.173861567, 6.013423491}, {0.832348609, 1.657740023, 2.253532635}};
    ArrayAsserts.assertArrayEquals("G2++: maturity dependent volatility part", hExpected[0], h[0], 1.0E-7);
    ArrayAsserts.assertArrayEquals("G2++: maturity dependent volatility part", hExpected[1], h[1], 1.0E-7);
    double[] hVector = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, u, v[0]);
    assertEquals("G2++: maturity dependent volatility part", hExpected[0][0], hVector[0], 1.0E-7);
    assertEquals("G2++: maturity dependent volatility part", hExpected[1][0], hVector[1], 1.0E-7);
  }

  @Test
  /**
   * Tests the gamma method.
   */
  public void gamma() {
    double theta0 = 0.25;
    double theta1 = 2.5;
    double[][] gamma = MODEL_G2PP.gamma(MODEL_PARAMETERS, theta0, theta1);
    double[][] gammaExpected = new double[][] { {0.00032338, 0.000335543}, {0.000335543, 0.000349741}};
    ArrayAsserts.assertArrayEquals("G2++: gamma", gammaExpected[0], gamma[0], 1.0E-7);
    ArrayAsserts.assertArrayEquals("G2++: gamma", gammaExpected[1], gamma[1], 1.0E-7);
  }

}
