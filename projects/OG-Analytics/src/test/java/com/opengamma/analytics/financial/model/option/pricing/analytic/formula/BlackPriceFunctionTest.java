/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BlackPriceFunctionTest {
  private static final double T = 4.5;
  private static final double F = 104;
  private static final double DELTA = 10;
  private static final EuropeanVanillaOption ATM_CALL = new EuropeanVanillaOption(F, T, true);
  private static final EuropeanVanillaOption ITM_CALL = new EuropeanVanillaOption(F - DELTA, T, true);
  private static final EuropeanVanillaOption OTM_CALL = new EuropeanVanillaOption(F + DELTA, T, true);
  private static final EuropeanVanillaOption CALL_0 = new EuropeanVanillaOption(0.0, T, true);
  private static final EuropeanVanillaOption ITM_PUT = new EuropeanVanillaOption(F + DELTA, T, false);
  private static final EuropeanVanillaOption OTM_PUT = new EuropeanVanillaOption(F - DELTA, T, false);
  private static final double DF = 0.9;
  private static final double SIGMA = 0.5;
  private static final BlackFunctionData ATM_DATA = new BlackFunctionData(F, DF, SIGMA);
  private static final BlackFunctionData ZERO_VOL_DATA = new BlackFunctionData(F, DF, 0);
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final BlackPriceFunction FUNCTION = new BlackPriceFunction();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOption1() {
    FUNCTION.getPriceFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOption2() {
    FUNCTION.getPriceFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData1() {
    FUNCTION.getPriceFunction(ATM_CALL).evaluate((BlackFunctionData) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData2() {
    FUNCTION.getVegaFunction(ATM_CALL).evaluate((BlackFunctionData) null);
  }

  @Test
  public void testATMPrice() {
    final double sigmaRootT = ATM_DATA.getBlackVolatility() * Math.sqrt(ATM_CALL.getTimeToExpiry());
    assertEquals(DF * F * (2 * NORMAL.getCDF(sigmaRootT / 2) - 1), FUNCTION.getPriceFunction(ATM_CALL).evaluate(ATM_DATA), 1e-14);
  }

  @Test
  public void testZeroVolPrice() {
    assertEquals(DF * DELTA, FUNCTION.getPriceFunction(ITM_CALL).evaluate(ZERO_VOL_DATA), 1e-15);
    assertEquals(0, FUNCTION.getPriceFunction(OTM_CALL).evaluate(ZERO_VOL_DATA), 1e-15);
    assertEquals(DF * DELTA, FUNCTION.getPriceFunction(ITM_PUT).evaluate(ZERO_VOL_DATA), 1e-15);
    assertEquals(0, FUNCTION.getPriceFunction(OTM_PUT).evaluate(ZERO_VOL_DATA), 1e-15);
  }

  @Test
  public void priceAdjoint() {
    // Price
    double price = FUNCTION.getPriceFunction(ITM_CALL).evaluate(ATM_DATA);
    double[] priceAdjoint = FUNCTION.getPriceAdjoint(ITM_CALL, ATM_DATA);
    assertEquals(price, priceAdjoint[0], 1E-10);
    // Price with 0 volatility
    double price0 = FUNCTION.getPriceFunction(ITM_CALL).evaluate(ZERO_VOL_DATA);
    double[] price0Adjoint = FUNCTION.getPriceAdjoint(ITM_CALL, ZERO_VOL_DATA);
    assertEquals(price0, price0Adjoint[0], 1E-10);
    // Derivative forward.
    double deltaF = 0.01;
    BlackFunctionData dataFP = new BlackFunctionData(F + deltaF, DF, SIGMA);
    BlackFunctionData dataFM = new BlackFunctionData(F - deltaF, DF, SIGMA);
    double priceFP = FUNCTION.getPriceFunction(ITM_CALL).evaluate(dataFP);
    double priceFM = FUNCTION.getPriceFunction(ITM_CALL).evaluate(dataFM);
    double derivativeF_FD = (priceFP - priceFM) / (2 * deltaF);
    assertEquals(derivativeF_FD, priceAdjoint[1], 1E-7);
    // Derivative strike.
    double deltaK = 0.01;
    EuropeanVanillaOption optionKP = new EuropeanVanillaOption(F - DELTA + deltaK, T, true);
    EuropeanVanillaOption optionKM = new EuropeanVanillaOption(F - DELTA - deltaK, T, true);
    double priceKP = FUNCTION.getPriceFunction(optionKP).evaluate(ATM_DATA);
    double priceKM = FUNCTION.getPriceFunction(optionKM).evaluate(ATM_DATA);
    double derivativeK_FD = (priceKP - priceKM) / (2 * deltaK);
    assertEquals(derivativeK_FD, priceAdjoint[3], 1E-7);
    // Derivative volatility.
    double deltaV = 0.0001;
    BlackFunctionData dataVP = new BlackFunctionData(F, DF, SIGMA + deltaV);
    BlackFunctionData dataVM = new BlackFunctionData(F, DF, SIGMA - deltaV);
    double priceVP = FUNCTION.getPriceFunction(ITM_CALL).evaluate(dataVP);
    double priceVM = FUNCTION.getPriceFunction(ITM_CALL).evaluate(dataVM);
    double derivativeV_FD = (priceVP - priceVM) / (2 * deltaV);
    assertEquals(derivativeV_FD, priceAdjoint[2], 1E-6);
  }

  @Test(enabled = false)
  /**
   * Tests the numerical stability of a finite difference approach to derivativ computation.
   */
  public void priceADStability() {
    final double forward = 1.0;
    final double df = 1.0; // 0 rate
    final double sigma = 0.20;
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, sigma);
    final double expiration = 7.0d / 365.0d; // 1 week
    final EuropeanVanillaOption atmCall = new EuropeanVanillaOption(forward, expiration, true);
    final double price = FUNCTION.getPriceFunction(atmCall).evaluate(dataBlack);
    double startingShift = 1.0E-4;
    double ratio = Math.sqrt(2.0);
    final int nbShift = 75;
    final double[] eps = new double[nbShift + 1];
    final double[] priceAdjoint = FUNCTION.getPriceAdjoint(atmCall, dataBlack);
    final double[] derivativeF_FD = new double[nbShift];
    final double[] diff = new double[nbShift];
    eps[0] = startingShift;
    for (int loopshift = 0; loopshift < nbShift; loopshift++) {
      final BlackFunctionData dataBlackShifted = new BlackFunctionData(forward + eps[loopshift], df, sigma);
      final double priceShifted = FUNCTION.getPriceFunction(atmCall).evaluate(dataBlackShifted);
      derivativeF_FD[loopshift] = (priceShifted - price) / eps[loopshift];
      diff[loopshift] = derivativeF_FD[loopshift] - priceAdjoint[1];
      eps[loopshift + 1] = eps[loopshift] / ratio;
    }
    //    int t = 0;
    //    t++;
  }

  @Test
  public void testPriceAdjointStrike0() {
    // Price
    double price = FUNCTION.getPriceFunction(CALL_0).evaluate(ATM_DATA);
    double[] priceAdjoint = FUNCTION.getPriceAdjoint(CALL_0, ATM_DATA);
    assertEquals(price, priceAdjoint[0], 1E-10);
    // Derivative forward.
    double deltaF = 0.01;
    BlackFunctionData dataFP = new BlackFunctionData(F + deltaF, DF, SIGMA);
    BlackFunctionData dataFM = new BlackFunctionData(F - deltaF, DF, SIGMA);
    double priceFP = FUNCTION.getPriceFunction(CALL_0).evaluate(dataFP);
    double priceFM = FUNCTION.getPriceFunction(CALL_0).evaluate(dataFM);
    double derivativeF_FD = (priceFP - priceFM) / (2 * deltaF);
    assertEquals(derivativeF_FD, priceAdjoint[1], 1E-7);
    // Derivative strike.
    double deltaK = 0.01;
    EuropeanVanillaOption optionKP = new EuropeanVanillaOption(0.0 + deltaK, T, true);
    double priceKP = FUNCTION.getPriceFunction(optionKP).evaluate(ATM_DATA);
    double derivativeK_FD = (priceKP - price) / (deltaK);
    assertEquals(derivativeK_FD, priceAdjoint[3], 1E-7);
    // Derivative volatility.
    double deltaV = 0.0001;
    BlackFunctionData dataVP = new BlackFunctionData(F, DF, SIGMA + deltaV);
    BlackFunctionData dataVM = new BlackFunctionData(F, DF, SIGMA - deltaV);
    double priceVP = FUNCTION.getPriceFunction(CALL_0).evaluate(dataVP);
    double priceVM = FUNCTION.getPriceFunction(CALL_0).evaluate(dataVM);
    double derivativeV_FD = (priceVP - priceVM) / (2 * deltaV);
    assertEquals(derivativeV_FD, priceAdjoint[2], 1E-6);
  }

  private static final double TOLERANCE_1 = 1.0E-10;
  private static final double TOLERANCE_2_FWD_FWD = 1.0E-6;
  private static final double TOLERANCE_2_VOL_VOL = 1.0E-6;
  private static final double TOLERANCE_2_STR_STR = 1.0E-6;
  private static final double TOLERANCE_2_FWD_VOL = 1.0E-7;
  private static final double TOLERANCE_2_FWD_STR = 1.0E-6;
  private static final double TOLERANCE_2_STR_VOL = 1.0E-6;
  
  /** Tests second order Algorithmic Differentiation version of BlackFunction with several data sets. */
  @Test
  public void testPriceAdjoint2() {
    // forward, numeraire, sigma, strike, time
    double[][] testData = {
      {104.0d, 0.9d, 0.50d, 94.0d, 4.5d},
      {104.0d, 0.9d, 0.50d, 124.0d, 4.5d},
      {104.0d, 0.9d, 0.50d, 104.0d, 4.5d},
      {0.0250d, 1000.0d, 0.25d, 0.0150d, 10.0d},
      {0.0250d, 1000.0d, 0.25d, 0.0400d, 10.0d},
      {1700.0d, 0.9d, 1.00d, 1500.0d, 0.01d},
      {1700.0d, 0.9d, 1.00d, 1900.0d, 20.0d}
    };
    int nbTest = testData.length;
    for(int i=0; i<nbTest; i++) {
      testPriceAdjointSecondOrder(testData[i][0],testData[i][1],testData[i][2],testData[i][3],testData[i][4], true, i);
      testPriceAdjointSecondOrder(testData[i][0],testData[i][1],testData[i][2],testData[i][3],testData[i][4], false, i);
    }
  }
  
  private void testPriceAdjointSecondOrder(double forward, double numeraire, double sigma, double strike, double time,
      boolean isCall, int i) {
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, time, isCall);
    BlackFunctionData data = new BlackFunctionData(forward, numeraire, sigma);
    // Price
    double[] priceAdjoint = FUNCTION.getPriceAdjoint(option, data);
    double[] bsD = new double[3];
    double[][] bsD2 = new double[3][3];
    double bs = FUNCTION.getPriceAdjoint2(option, data, bsD, bsD2);
    assertEquals("AD Second order: price", priceAdjoint[0], bs, TOLERANCE_1);
    // First derivative
    for (int loopder = 0; loopder < 3; loopder++) {
      assertEquals("AD Second order: 1st", priceAdjoint[loopder + 1], bsD[loopder], TOLERANCE_1);
    }
    // Second derivative
    // Derivative forward-forward.
    double deltaF = 1.0E-3 * forward;
    BlackFunctionData dataFP = new BlackFunctionData(forward + deltaF, numeraire, sigma);
    BlackFunctionData dataFM = new BlackFunctionData(forward - deltaF, numeraire, sigma);
    double[] priceAdjointFP = FUNCTION.getPriceAdjoint(option, dataFP);
    double[] priceAdjointFM = FUNCTION.getPriceAdjoint(option, dataFM);
    double derivativeFF_FD = (priceAdjointFP[1] - priceAdjointFM[1]) / (2 * deltaF);
    assertEquals("AD Second order: 2nd - fwd-fwd " + i, 
        derivativeFF_FD, bsD2[0][0], TOLERANCE_2_FWD_FWD * Math.abs(bs / (deltaF * deltaF)));
    // Derivative volatility-volatility.
    double deltaV = 0.00001;
    double deltaV2 = (deltaV * deltaV);
    BlackFunctionData dataVP = new BlackFunctionData(forward, numeraire, sigma + deltaV);
    BlackFunctionData dataVM = new BlackFunctionData(forward, numeraire, sigma - deltaV);
    double[] priceAdjointVP = FUNCTION.getPriceAdjoint(option, dataVP);
    double[] priceAdjointVM = FUNCTION.getPriceAdjoint(option, dataVM);
    double derivativeVV_FD = (priceAdjointVP[2] - priceAdjointVM[2]) / (2 * deltaV);
    assertEquals("AD Second order: 2nd - vol-vol " + i,
        derivativeVV_FD, bsD2[1][1], TOLERANCE_2_VOL_VOL * Math.abs(bs / deltaV2));
    // Derivative forward-volatility.
    double derivativeFV_FD = (priceAdjointVP[1] - priceAdjointVM[1]) / (2 * deltaV);
    assertEquals("AD Second order: 2nd - fwd-vol " + i,
        derivativeFV_FD, bsD2[1][0], TOLERANCE_2_FWD_VOL * Math.abs(bs / (deltaF * deltaV)));
    assertEquals("AD Second order: 2nd - fwd-vol", bsD2[0][1], bsD2[1][0], TOLERANCE_1);
    // Derivative strike-strike.
    double deltaK = 1.0E-4 * strike;
    EuropeanVanillaOption optionKP = new EuropeanVanillaOption(strike + deltaK, time, isCall);
    EuropeanVanillaOption optionKM = new EuropeanVanillaOption(strike - deltaK, time, isCall);
    double[] priceAdjointKP = FUNCTION.getPriceAdjoint(optionKP, data);
    double[] priceAdjointKM = FUNCTION.getPriceAdjoint(optionKM, data);
    double derivativeKK_FD = (priceAdjointKP[3] - priceAdjointKM[3]) / (2 * deltaK);
    assertEquals("AD Second order: 2nd - strike-strike " + i,
        derivativeKK_FD, bsD2[2][2], TOLERANCE_2_STR_STR * Math.abs(derivativeKK_FD));
    // Derivative forward-strike.
    double derivativeFK_FD = (priceAdjointKP[1] - priceAdjointKM[1]) / (2 * deltaK);
    assertEquals("AD Second order: 2nd - fwd-str " + i,
        derivativeFK_FD, bsD2[2][0], TOLERANCE_2_FWD_STR * Math.abs(bs / (deltaF * deltaK)));
    assertEquals("AD Second order: 2nd - fwd-str", bsD2[0][2], bsD2[2][0], TOLERANCE_1);
    // Derivative strike-volatility.
    double derivativeKV_FD = (priceAdjointVP[3] - priceAdjointVM[3]) / (2 * deltaV);
    assertEquals("AD Second order: 2nd - str-vol " + i,
        derivativeKV_FD, bsD2[2][1], TOLERANCE_2_STR_VOL * Math.abs(bs / (deltaV * deltaK)));
    assertEquals("AD Second order: 2nd - str-vol", bsD2[1][2], bsD2[2][1], TOLERANCE_1);    
  }

  @Test(enabled = false)
  /**
   * Assess the performance of the adjoint implementation against a finite difference and a non-optimized adjoint implementation.
   */
  public void performanceAdjoint() {
    // Used only to assess performance
    double[] bsD = new double[3];
    double[][] bsD2 = new double[3][3];
    @SuppressWarnings("unused")
    double bsP;
    @SuppressWarnings("unused")
    double[] bsPD = new double[4];

    long startTime, endTime;
    int nbTest = 1000000;
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      bsP = FUNCTION.getPriceFunction(ITM_CALL).evaluate(ATM_DATA);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " Black price : " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      bsPD = FUNCTION.getPriceAdjoint(ITM_CALL, ATM_DATA);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " Black price + first order adjoint: " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      bsP = FUNCTION.getPriceAdjoint2(ITM_CALL, ATM_DATA, bsD, bsD2);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " Black price + adjoint (first and second order): " + (endTime - startTime) + " ms");
    // Performance note: price: 14-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 175 ms for 1000000.
    // Performance note: price+1st order derivatives: 14-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 195 ms for 1000000.
    // Performance note: price+1st and 2nd order derivatives: 14-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 250 ms for 1000000.
  }

}
