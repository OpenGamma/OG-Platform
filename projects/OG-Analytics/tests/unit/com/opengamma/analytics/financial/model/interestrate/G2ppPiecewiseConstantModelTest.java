/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;

import com.opengamma.analytics.financial.model.interestrate.G2ppPiecewiseConstantModel;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;

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

  @Test
  /**
   * Tests swap rate first derivative
   */
  public void swapRateD1and2() {

    double theta = 1.99;
    double[] discountedCashFlowFixed = new double[] {1.0, 1.0};
    double[] tFixed = new double[] {3.0, 4.0};
    double[] discountedCashFlowIbor = new double[] {1.001, 0.001, 0.001, 0.001, -1.0};
    double[] tIbor = new double[] {2.0, 2.5, 3.0, 3.5, 4.0};

    double rhog2pp = MODEL_PARAMETERS.getCorrelation();
    double[][] gamma = MODEL_G2PP.gamma(MODEL_PARAMETERS, 0, theta);
    double[][] alphaFixed = new double[tFixed.length][2];
    double[] tau2Fixed = new double[tFixed.length];
    double[][] alphaIbor = new double[tIbor.length][2];
    double[] tau2Ibor = new double[tIbor.length];
    double[][] hthetaFixed = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, theta, tFixed);
    alphaFixed = new double[2][tFixed.length];
    tau2Fixed = new double[tFixed.length];
    for (int loopcf = 0; loopcf < tFixed.length; loopcf++) {
      alphaFixed[loopcf][0] = Math.sqrt(gamma[0][0]) * hthetaFixed[0][loopcf];
      alphaFixed[loopcf][1] = Math.sqrt(gamma[1][1]) * hthetaFixed[1][loopcf];
      tau2Fixed[loopcf] = alphaFixed[loopcf][0] * alphaFixed[loopcf][0] + alphaFixed[loopcf][1] * alphaFixed[loopcf][1] + 2 * rhog2pp * gamma[0][1] * hthetaFixed[0][loopcf] * hthetaFixed[1][loopcf];
    }
    double[][] hthetaIbor = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, theta, tIbor);
    for (int loopcf = 0; loopcf < tIbor.length; loopcf++) {
      alphaIbor[loopcf][0] = Math.sqrt(gamma[0][0]) * hthetaIbor[0][loopcf];
      alphaIbor[loopcf][1] = Math.sqrt(gamma[1][1]) * hthetaIbor[1][loopcf];
      tau2Ibor[loopcf] = alphaIbor[loopcf][0] * alphaIbor[loopcf][0] + alphaIbor[loopcf][1] * alphaIbor[loopcf][1] + 2 * rhog2pp * gamma[0][1] * hthetaIbor[0][loopcf] * hthetaIbor[1][loopcf];
    }
    // First derivative
    double[][] x = new double[][] { {0.0, 0.0}, {1.0, 0.0}, {0.5, -0.5}};
    double shift = 1.0E-4;
    double[] swapRateD1 = new double[2];
    for (int looptest = 0; looptest < x.length; looptest++) {
      MODEL_G2PP.swapRate(x[looptest], discountedCashFlowFixed, alphaFixed, tau2Fixed, discountedCashFlowIbor, alphaIbor, tau2Ibor, swapRateD1);
      for (int loop = 0; loop < 2; loop++) {
        double[] xP = x[looptest].clone();
        xP[loop] += shift;
        double[] xM = x[looptest].clone();
        xM[loop] -= shift;
        double swapRateP1 = MODEL_G2PP.swapRate(xP, discountedCashFlowFixed, alphaFixed, tau2Fixed, discountedCashFlowIbor, alphaIbor, tau2Ibor);
        double swapRateM1 = MODEL_G2PP.swapRate(xM, discountedCashFlowFixed, alphaFixed, tau2Fixed, discountedCashFlowIbor, alphaIbor, tau2Ibor);
        assertEquals("G2++: swap rate", (swapRateP1 - swapRateM1) / (2 * shift), swapRateD1[loop], 1.0E-7);
      }
    }

    // Second derivative
    double[] swapRateD1Expected = new double[2];
    double[][] swapRateD2 = new double[2][2];
    for (int looptest = 0; looptest < x.length; looptest++) {
      MODEL_G2PP.swapRate(x[looptest], discountedCashFlowFixed, alphaFixed, tau2Fixed, discountedCashFlowIbor, alphaIbor, tau2Ibor, swapRateD1Expected);
      MODEL_G2PP.swapRate(x[looptest], discountedCashFlowFixed, alphaFixed, tau2Fixed, discountedCashFlowIbor, alphaIbor, tau2Ibor, swapRateD1, swapRateD2);
      double[][] swapRateD2Expected = new double[2][2];
      for (int loop1 = 0; loop1 < 2; loop1++) {
        for (int loop2 = 0; loop2 < 2; loop2++) {
          double[] xPP = x[looptest].clone();
          double[] xMM = x[looptest].clone();
          double[] xMP = x[looptest].clone();
          double[] xPM = x[looptest].clone();
          xPP[loop1] += shift;
          xPP[loop2] += shift;
          xMM[loop1] -= shift;
          xMM[loop2] -= shift;
          xMP[loop1] -= shift;
          xMP[loop2] += shift;
          xPM[loop1] += shift;
          xPM[loop2] -= shift;
          double swapRatePP = MODEL_G2PP.swapRate(xPP, discountedCashFlowFixed, alphaFixed, tau2Fixed, discountedCashFlowIbor, alphaIbor, tau2Ibor);
          double swapRateMM = MODEL_G2PP.swapRate(xMM, discountedCashFlowFixed, alphaFixed, tau2Fixed, discountedCashFlowIbor, alphaIbor, tau2Ibor);
          double swapRateMP = MODEL_G2PP.swapRate(xMP, discountedCashFlowFixed, alphaFixed, tau2Fixed, discountedCashFlowIbor, alphaIbor, tau2Ibor);
          double swapRatePM = MODEL_G2PP.swapRate(xPM, discountedCashFlowFixed, alphaFixed, tau2Fixed, discountedCashFlowIbor, alphaIbor, tau2Ibor);
          swapRateD2Expected[loop1][loop2] = (swapRatePP + swapRateMM - swapRateMP - swapRatePM) / (4 * shift * shift);
          assertEquals("G2++: swap rate", swapRateD2Expected[loop1][loop2], swapRateD2[loop1][loop2], 1.0E-7);
        }
      }
      ArrayAsserts.assertArrayEquals("G2++: gamma", swapRateD1Expected, swapRateD1, 1.0E-7);
    }
  }

}
