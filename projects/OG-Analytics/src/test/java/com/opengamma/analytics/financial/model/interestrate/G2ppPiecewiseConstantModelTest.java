/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;

import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * Tests related to the construction of the G2++ model with piecewise constant volatility. The computation of several model related factors are also tested.
 */
@Test(groups = TestGroup.UNIT)
public class G2ppPiecewiseConstantModelTest {

  private static final double[] MEAN_REVERSION = new double[] {0.01, 0.30 };
  private static final double[][] VOLATILITY = new double[][] { {0.01, 0.011, 0.012, 0.013, 0.014 }, {0.01, 0.009, 0.008, 0.007, 0.006 } };
  private static final double[] VOLATILITY_TIME = new double[] {0.5, 1.0, 2.0, 5.0 };
  private static final double CORRELATION = -0.50;
  private static final G2ppPiecewiseConstantParameters MODEL_PARAMETERS = new G2ppPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY, VOLATILITY_TIME, CORRELATION);
  private static final G2ppPiecewiseConstantModel MODEL_G2PP = new G2ppPiecewiseConstantModel();
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL_HW = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  private static final double[] DCF_FIXED = new double[] {1.0, 1.0 };
  private static final double[] T_FIXED = new double[] {3.0, 4.0 };
  private static final double[] DCF_IBOR = new double[] {1.001, 0.001, 0.001, 0.001, -1.0 };
  private static final double[] T_IBOR = new double[] {2.0, 2.5, 3.0, 3.5, 4.0 };

  private static final double TOLERANCE_FACTOR = 1.0E-6;
  private static final double TOLERANCE_RATE_DELTA = 1.0E-8;
  private static final double TOLERANCE_RATE_DELTA2 = 1.0E-7;

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertArrayEquals(MEAN_REVERSION, MODEL_PARAMETERS.getMeanReversion(), 1e-10);
    assertEquals(CORRELATION, MODEL_PARAMETERS.getCorrelation());
    for (int loopperiod = 0; loopperiod < VOLATILITY[0].length; loopperiod++) {
      assertEquals(VOLATILITY[0][loopperiod], MODEL_PARAMETERS.getVolatility()[0].get(loopperiod));
      assertEquals(VOLATILITY[1][loopperiod], MODEL_PARAMETERS.getVolatility()[1].get(loopperiod));
    }
    final double[] volTime = MODEL_PARAMETERS.getVolatilityTime();
    for (int loopperiod = 0; loopperiod < VOLATILITY_TIME.length; loopperiod++) {
      assertEquals(VOLATILITY_TIME[loopperiod], volTime[loopperiod + 1]);
    }
  }

  @Test
  /**
   * Tests the class setters.
   */
  public void setter() {
    final double[] volReplaced = new double[] {0.02, 0.01 };
    MODEL_PARAMETERS.setLastVolatilities(volReplaced);
    ArrayAsserts.assertArrayEquals("G2++: setter", volReplaced, MODEL_PARAMETERS.getLastVolatilities(), 1.0E-10);
    MODEL_PARAMETERS.setLastVolatilities(new double[] {VOLATILITY[0][VOLATILITY[0].length - 1], VOLATILITY[1][VOLATILITY[0].length - 1] });
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
    final double u = 0.75;
    final double[] v = new double[] {2.0, 4.0, 7.0 };
    final double[][] h = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, u, v);
    final double[][] hExpected = new double[][] { {1.232938151, 3.173861567, 6.013423491 }, {0.832348609, 1.657740023, 2.253532635 } };
    ArrayAsserts.assertArrayEquals("G2++: maturity dependent volatility part", hExpected[0], h[0], 1.0E-7);
    ArrayAsserts.assertArrayEquals("G2++: maturity dependent volatility part", hExpected[1], h[1], 1.0E-7);
    final double[] hVector = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, u, v[0]);
    assertEquals("G2++: maturity dependent volatility part", hExpected[0][0], hVector[0], 1.0E-7);
    assertEquals("G2++: maturity dependent volatility part", hExpected[1][0], hVector[1], 1.0E-7);
  }

  @Test
  /**
   * Tests the volatilityMaturityPart method for arrays.
   */
  public void volatilityMaturityPartArray() {
    final double u = 0.75;
    final double[][] v = new double[][] { {2.0, 4.0, 7.0 }, {5.0, 3.0 } };
    final double[][][] h = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, u, v);
    final double[][] h0 = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, u, v[0]);
    final double[][] h1 = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, u, v[1]);
    for (int loop = 0; loop < v[0].length; loop++) {
      assertEquals("G2++: maturity dependent volatility part", h[0][0][loop], h0[0][loop], 1.0E-7);
      assertEquals("G2++: maturity dependent volatility part", h[1][0][loop], h0[1][loop], 1.0E-7);
    }
    for (int loop = 0; loop < v[1].length; loop++) {
      assertEquals("G2++: maturity dependent volatility part", h[0][1][loop], h1[0][loop], 1.0E-7);
      assertEquals("G2++: maturity dependent volatility part", h[1][1][loop], h1[1][loop], 1.0E-7);
    }
  }

  @Test
  /**
   * Tests the gamma method.
   */
  public void gamma() {
    final double theta0 = 0.25;
    final double theta1 = 2.5;
    final double[][] gamma = MODEL_G2PP.gamma(MODEL_PARAMETERS, theta0, theta1);
    final double[][] gammaExpected = new double[][] { {0.00032338, 0.000335543 }, {0.000335543, 0.000349741 } };
    ArrayAsserts.assertArrayEquals("G2++: gamma", gammaExpected[0], gamma[0], 1.0E-7);
    ArrayAsserts.assertArrayEquals("G2++: gamma", gammaExpected[1], gamma[1], 1.0E-7);
  }

  @Test
  /**
   * Tests swap rate first derivative
   */
  public void swapRateD1and2() {
    final double theta = 1.99;

    final double rhog2pp = MODEL_PARAMETERS.getCorrelation();
    final double[][] gamma = MODEL_G2PP.gamma(MODEL_PARAMETERS, 0, theta);
    double[][] alphaFixed = new double[T_FIXED.length][2];
    double[] tau2Fixed = new double[T_FIXED.length];
    final double[][] alphaIbor = new double[T_IBOR.length][2];
    final double[] tau2Ibor = new double[T_IBOR.length];
    final double[][] hthetaFixed = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, theta, T_FIXED);
    alphaFixed = new double[2][T_FIXED.length];
    tau2Fixed = new double[T_FIXED.length];
    for (int loopcf = 0; loopcf < T_FIXED.length; loopcf++) {
      alphaFixed[loopcf][0] = Math.sqrt(gamma[0][0]) * hthetaFixed[0][loopcf];
      alphaFixed[loopcf][1] = Math.sqrt(gamma[1][1]) * hthetaFixed[1][loopcf];
      tau2Fixed[loopcf] = alphaFixed[loopcf][0] * alphaFixed[loopcf][0] + alphaFixed[loopcf][1] * alphaFixed[loopcf][1] + 2 * rhog2pp * gamma[0][1] * hthetaFixed[0][loopcf] * hthetaFixed[1][loopcf];
    }
    final double[][] hthetaIbor = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, theta, T_IBOR);
    for (int loopcf = 0; loopcf < T_IBOR.length; loopcf++) {
      alphaIbor[loopcf][0] = Math.sqrt(gamma[0][0]) * hthetaIbor[0][loopcf];
      alphaIbor[loopcf][1] = Math.sqrt(gamma[1][1]) * hthetaIbor[1][loopcf];
      tau2Ibor[loopcf] = alphaIbor[loopcf][0] * alphaIbor[loopcf][0] + alphaIbor[loopcf][1] * alphaIbor[loopcf][1] + 2 * rhog2pp * gamma[0][1] * hthetaIbor[0][loopcf] * hthetaIbor[1][loopcf];
    }
    // First derivative
    final double[][] x = new double[][] { {0.0, 0.0 }, {1.0, 0.0 }, {0.5, -0.5 } };
    final double shift = 1.0E-4;
    final double[] swapRateD1 = new double[2];
    for (final double[] element : x) {
      MODEL_G2PP.swapRate(element, DCF_FIXED, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor, swapRateD1);
      for (int loop = 0; loop < 2; loop++) {
        final double[] xP = element.clone();
        xP[loop] += shift;
        final double[] xM = element.clone();
        xM[loop] -= shift;
        final double swapRateP1 = MODEL_G2PP.swapRate(xP, DCF_FIXED, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor);
        final double swapRateM1 = MODEL_G2PP.swapRate(xM, DCF_FIXED, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor);
        assertEquals("G2++: swap rate", (swapRateP1 - swapRateM1) / (2 * shift), swapRateD1[loop], 1.0E-7);
      }
    }

    // Second derivative
    final double[] swapRateD1Expected = new double[2];
    final double[][] swapRateD2 = new double[2][2];
    for (final double[] element : x) {
      MODEL_G2PP.swapRate(element, DCF_FIXED, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor, swapRateD1Expected);
      MODEL_G2PP.swapRate(element, DCF_FIXED, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor, swapRateD1, swapRateD2);
      final double[][] swapRateD2Expected = new double[2][2];
      for (int loop1 = 0; loop1 < 2; loop1++) {
        for (int loop2 = 0; loop2 < 2; loop2++) {
          final double[] xPP = element.clone();
          final double[] xMM = element.clone();
          final double[] xMP = element.clone();
          final double[] xPM = element.clone();
          xPP[loop1] += shift;
          xPP[loop2] += shift;
          xMM[loop1] -= shift;
          xMM[loop2] -= shift;
          xMP[loop1] -= shift;
          xMP[loop2] += shift;
          xPM[loop1] += shift;
          xPM[loop2] -= shift;
          final double swapRatePP = MODEL_G2PP.swapRate(xPP, DCF_FIXED, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor);
          final double swapRateMM = MODEL_G2PP.swapRate(xMM, DCF_FIXED, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor);
          final double swapRateMP = MODEL_G2PP.swapRate(xMP, DCF_FIXED, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor);
          final double swapRatePM = MODEL_G2PP.swapRate(xPM, DCF_FIXED, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor);
          swapRateD2Expected[loop1][loop2] = (swapRatePP + swapRateMM - swapRateMP - swapRatePM) / (4 * shift * shift);
          assertEquals("G2++: swap rate", swapRateD2Expected[loop1][loop2], swapRateD2[loop1][loop2], 1.0E-7);
        }
      }
      ArrayAsserts.assertArrayEquals("G2++: gamma", swapRateD1Expected, swapRateD1, 1.0E-7);
    }
  }

  @Test
  public void swapRateDdcf() {
    final double theta = 1.99;
    final double rhog2pp = MODEL_PARAMETERS.getCorrelation();
    final double[][] gamma = MODEL_G2PP.gamma(MODEL_PARAMETERS, 0, theta);
    double[][] alphaFixed = new double[T_FIXED.length][2];
    double[] tau2Fixed = new double[T_FIXED.length];
    final double[][] alphaIbor = new double[T_IBOR.length][2];
    final double[] tau2Ibor = new double[T_IBOR.length];
    final double[][] hthetaFixed = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, theta, T_FIXED);
    alphaFixed = new double[2][T_FIXED.length];
    tau2Fixed = new double[T_FIXED.length];
    for (int loopcf = 0; loopcf < T_FIXED.length; loopcf++) {
      alphaFixed[loopcf][0] = Math.sqrt(gamma[0][0]) * hthetaFixed[0][loopcf];
      alphaFixed[loopcf][1] = Math.sqrt(gamma[1][1]) * hthetaFixed[1][loopcf];
      tau2Fixed[loopcf] = alphaFixed[loopcf][0] * alphaFixed[loopcf][0] + alphaFixed[loopcf][1] * alphaFixed[loopcf][1] + 2 * rhog2pp * gamma[0][1] * hthetaFixed[0][loopcf] * hthetaFixed[1][loopcf];
    }
    final double[][] hthetaIbor = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, theta, T_IBOR);
    for (int loopcf = 0; loopcf < T_IBOR.length; loopcf++) {
      alphaIbor[loopcf][0] = Math.sqrt(gamma[0][0]) * hthetaIbor[0][loopcf];
      alphaIbor[loopcf][1] = Math.sqrt(gamma[1][1]) * hthetaIbor[1][loopcf];
      tau2Ibor[loopcf] = alphaIbor[loopcf][0] * alphaIbor[loopcf][0] + alphaIbor[loopcf][1] * alphaIbor[loopcf][1] + 2 * rhog2pp * gamma[0][1] * hthetaIbor[0][loopcf] * hthetaIbor[1][loopcf];
    }

    final double shift = 1.0E-8;
    final double[] x = {0.0, 0.1 };
    final double[] ddcffExpected = new double[DCF_FIXED.length];
    for (int loopcf = 0; loopcf < DCF_FIXED.length; loopcf++) {
      final double[] dsf_bumped = DCF_FIXED.clone();
      dsf_bumped[loopcf] += shift;
      final double swapRatePlus = MODEL_G2PP.swapRate(x, dsf_bumped, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor);
      dsf_bumped[loopcf] -= 2 * shift;
      final double swapRateMinus = MODEL_G2PP.swapRate(x, dsf_bumped, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor);
      ddcffExpected[loopcf] = (swapRatePlus - swapRateMinus) / (2 * shift);
    }
    final double[] ddcffComputed = MODEL_G2PP.swapRateDdcff1(x, DCF_FIXED, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor);
    ArrayAsserts.assertArrayEquals("Hull-White model: swap rate", ddcffExpected, ddcffComputed, TOLERANCE_RATE_DELTA);

    final double[] ddcfiExpected = new double[DCF_IBOR.length];
    for (int loopcf = 0; loopcf < DCF_IBOR.length; loopcf++) {
      final double[] dsf_bumped = DCF_IBOR.clone();
      dsf_bumped[loopcf] += shift;
      final double swapRatePlus = MODEL_G2PP.swapRate(x, DCF_FIXED, alphaFixed, tau2Fixed, dsf_bumped, alphaIbor, tau2Ibor);
      dsf_bumped[loopcf] -= 2 * shift;
      final double swapRateMinus = MODEL_G2PP.swapRate(x, DCF_FIXED, alphaFixed, tau2Fixed, dsf_bumped, alphaIbor, tau2Ibor);
      ddcfiExpected[loopcf] = (swapRatePlus - swapRateMinus) / (2 * shift);
    }
    final double[] ddcfiComputed = MODEL_G2PP.swapRateDdcfi1(x, DCF_FIXED, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor);
    ArrayAsserts.assertArrayEquals("Hull-White model: swap rate", ddcfiExpected, ddcfiComputed, TOLERANCE_RATE_DELTA);
  }

  @Test(enabled = true)
  public void swapRateDx2Ddcf() {
    final double theta = 1.99;
    final double rhog2pp = MODEL_PARAMETERS.getCorrelation();
    final double[][] gamma = MODEL_G2PP.gamma(MODEL_PARAMETERS, 0, theta);
    double[][] alphaFixed = new double[T_FIXED.length][2];
    double[] tau2Fixed = new double[T_FIXED.length];
    final double[][] alphaIbor = new double[T_IBOR.length][2];
    final double[] tau2Ibor = new double[T_IBOR.length];
    final double[][] hthetaFixed = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, theta, T_FIXED);
    alphaFixed = new double[2][T_FIXED.length];
    tau2Fixed = new double[T_FIXED.length];
    for (int loopcf = 0; loopcf < T_FIXED.length; loopcf++) {
      alphaFixed[loopcf][0] = Math.sqrt(gamma[0][0]) * hthetaFixed[0][loopcf];
      alphaFixed[loopcf][1] = Math.sqrt(gamma[1][1]) * hthetaFixed[1][loopcf];
      tau2Fixed[loopcf] = alphaFixed[loopcf][0] * alphaFixed[loopcf][0] + alphaFixed[loopcf][1] * alphaFixed[loopcf][1] + 2 * rhog2pp * gamma[0][1] * hthetaFixed[0][loopcf] * hthetaFixed[1][loopcf];
    }
    final double[][] hthetaIbor = MODEL_G2PP.volatilityMaturityPart(MODEL_PARAMETERS, theta, T_IBOR);
    for (int loopcf = 0; loopcf < T_IBOR.length; loopcf++) {
      alphaIbor[loopcf][0] = Math.sqrt(gamma[0][0]) * hthetaIbor[0][loopcf];
      alphaIbor[loopcf][1] = Math.sqrt(gamma[1][1]) * hthetaIbor[1][loopcf];
      tau2Ibor[loopcf] = alphaIbor[loopcf][0] * alphaIbor[loopcf][0] + alphaIbor[loopcf][1] * alphaIbor[loopcf][1] + 2 * rhog2pp * gamma[0][1] * hthetaIbor[0][loopcf] * hthetaIbor[1][loopcf];
    }

    final double shift = 1.0E-7;
    final double[] x = {0.0, 0.1 };
    final Pair<double[][][], double[][][]> dx2ddcfComputed = MODEL_G2PP.swapRateDdcfDx2(x, DCF_FIXED, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor);
    final double[][][] dx2DdcffExpected = new double[DCF_FIXED.length][2][2];
    for (int loopcf = 0; loopcf < DCF_FIXED.length; loopcf++) {
      final double[] dsf_bumped = DCF_FIXED.clone();
      dsf_bumped[loopcf] += shift;
      final double[] d1Plus = new double[2];
      final double[][] d2Plus = new double[2][2];
      MODEL_G2PP.swapRate(x, dsf_bumped, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor, d1Plus, d2Plus);
      dsf_bumped[loopcf] -= 2 * shift;
      final double[] d1Minus = new double[2];
      final double[][] d2Minus = new double[2][2];
      MODEL_G2PP.swapRate(x, dsf_bumped, alphaFixed, tau2Fixed, DCF_IBOR, alphaIbor, tau2Ibor, d1Minus, d2Minus);
      for (int loopd1 = 0; loopd1 < 2; loopd1++) {
        for (int loopd2 = loopd1; loopd2 < 2; loopd2++) {
          dx2DdcffExpected[loopcf][loopd1][loopd2] = (d2Plus[loopd1][loopd2] - d2Minus[loopd1][loopd2]) / (2 * shift);
          assertEquals("Hull-White model: swap rate", dx2DdcffExpected[loopcf][loopd1][loopd2], dx2ddcfComputed.getFirst()[loopcf][loopd1][loopd2], TOLERANCE_RATE_DELTA2);
        }
      }
    }
    final double[][][] dx2DdcfiExpected = new double[DCF_IBOR.length][2][2];
    for (int loopcf = 0; loopcf < DCF_IBOR.length; loopcf++) {
      final double[] dsf_bumped = DCF_IBOR.clone();
      dsf_bumped[loopcf] += shift;
      final double[] d1Plus = new double[2];
      final double[][] d2Plus = new double[2][2];
      MODEL_G2PP.swapRate(x, DCF_FIXED, alphaFixed, tau2Fixed, dsf_bumped, alphaIbor, tau2Ibor, d1Plus, d2Plus);
      dsf_bumped[loopcf] -= 2 * shift;
      final double[] d1Minus = new double[2];
      final double[][] d2Minus = new double[2][2];
      MODEL_G2PP.swapRate(x, DCF_FIXED, alphaFixed, tau2Fixed, dsf_bumped, alphaIbor, tau2Ibor, d1Minus, d2Minus);
      for (int loopd1 = 0; loopd1 < 2; loopd1++) {
        for (int loopd2 = loopd1; loopd2 < 2; loopd2++) {
          dx2DdcfiExpected[loopcf][loopd1][loopd2] = (d2Plus[loopd1][loopd2] - d2Minus[loopd1][loopd2]) / (2 * shift);
          assertEquals("Hull-White model: swap rate", dx2DdcfiExpected[loopcf][loopd1][loopd2], dx2ddcfComputed.getSecond()[loopcf][loopd1][loopd2], TOLERANCE_RATE_DELTA2);
        }
      }
    }
  }

  @Test
  public void futuresConvexityFactor() {
    // Compare with Hull-White one-factor
    final double[] volTime = new double[0];
    final double[][] vol10 = new double[][] { {0.01 }, {0.00 } };
    final double[][] vol01 = new double[][] { {0.00 }, {0.01 } };
    final double expiryTime = 1.0;
    final double u = 1.02;
    final double v = 1.27;
    final HullWhiteOneFactorPiecewiseConstantParameters hw10 = new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION[0], vol10[0], volTime);
    final G2ppPiecewiseConstantParameters g2pp10 = new G2ppPiecewiseConstantParameters(MEAN_REVERSION, vol10, volTime, CORRELATION);
    final double adjHW10 = MODEL_HW.futuresConvexityFactor(hw10, expiryTime, u, v);
    final double adjG210 = MODEL_G2PP.futuresConvexityFactor(g2pp10, expiryTime, u, v);
    assertEquals("G2++: swap rate", adjHW10, adjG210, TOLERANCE_FACTOR);
    final HullWhiteOneFactorPiecewiseConstantParameters hw01 = new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION[1], vol01[1], volTime);
    final G2ppPiecewiseConstantParameters g2pp01 = new G2ppPiecewiseConstantParameters(MEAN_REVERSION, vol01, volTime, CORRELATION);
    final double adjHW01 = MODEL_HW.futuresConvexityFactor(hw01, expiryTime, u, v);
    final double adjG201 = MODEL_G2PP.futuresConvexityFactor(g2pp01, expiryTime, u, v);
    assertEquals("G2++: swap rate", adjHW01, adjG201, TOLERANCE_FACTOR);
    // TODO: full two-factor test
  }

}
