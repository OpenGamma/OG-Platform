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

  @Test
  public void testPriceAdjoint2Call() {
    // Price
    double[] priceAdjoint = FUNCTION.getPriceAdjoint(ITM_CALL, ATM_DATA);
    double[] bsD = new double[3];
    double[][] bsD2 = new double[3][3];
    double bs = FUNCTION.getPriceAdjoint2(ITM_CALL, ATM_DATA, bsD, bsD2);
    assertEquals(priceAdjoint[0], bs, 1E-10);
    // First derivative
    for (int loopder = 0; loopder < 3; loopder++) {
      assertEquals(priceAdjoint[loopder + 1], bsD[loopder], 1E-10);
    }
    // Second derivative
    // Derivative forward-forward.
    double deltaF = 0.001;
    BlackFunctionData dataFP = new BlackFunctionData(F + deltaF, DF, SIGMA);
    BlackFunctionData dataFM = new BlackFunctionData(F - deltaF, DF, SIGMA);
    double priceFP = FUNCTION.getPriceFunction(ITM_CALL).evaluate(dataFP);
    double priceFM = FUNCTION.getPriceFunction(ITM_CALL).evaluate(dataFM);
    double derivativeFF_FD = (priceFP + priceFM - 2 * bs) / (deltaF * deltaF);
    assertEquals(derivativeFF_FD, bsD2[0][0], 1E-7);
    // Derivative volatility-volatility.
    double deltaV = 0.00001;
    BlackFunctionData dataVP = new BlackFunctionData(F, DF, SIGMA + deltaV);
    BlackFunctionData dataVM = new BlackFunctionData(F, DF, SIGMA - deltaV);
    double priceVP = FUNCTION.getPriceFunction(ITM_CALL).evaluate(dataVP);
    double priceVM = FUNCTION.getPriceFunction(ITM_CALL).evaluate(dataVM);
    double derivativeVV_FD = (priceVP + priceVM - 2 * bs) / (deltaV * deltaV);
    assertEquals(derivativeVV_FD, bsD2[1][1], 1E-3);
    // Derivative forward-volatility.
    BlackFunctionData dataFPVP = new BlackFunctionData(F + deltaF, DF, SIGMA + deltaV);
    double priceFPVP = FUNCTION.getPriceFunction(ITM_CALL).evaluate(dataFPVP);
    double derivativeFV_FD = (priceFPVP + bs - priceFP - priceVP) / (deltaF * deltaV);
    assertEquals(derivativeFV_FD, bsD2[1][0], 1E-5);
    assertEquals(bsD2[0][1], bsD2[1][0], 1E-10);
    // Derivative strike-strike.
    double deltaK = 0.001;
    EuropeanVanillaOption optionKP = new EuropeanVanillaOption(F - DELTA + deltaK, T, true);
    EuropeanVanillaOption optionKM = new EuropeanVanillaOption(F - DELTA - deltaK, T, true);
    double priceKP = FUNCTION.getPriceFunction(optionKP).evaluate(ATM_DATA);
    double priceKM = FUNCTION.getPriceFunction(optionKM).evaluate(ATM_DATA);
    double derivativeKK_FD = (priceKP + priceKM - 2 * bs) / (deltaK * deltaK);
    assertEquals(derivativeKK_FD, bsD2[2][2], 1E-8);
    // Derivative forward-strike.
    double priceFPKP = FUNCTION.getPriceFunction(optionKP).evaluate(dataFP);
    double derivativeFK_FD = (priceFPKP + bs - priceFP - priceKP) / (deltaF * deltaK);
    assertEquals(derivativeFK_FD, bsD2[2][0], 1E-7);
    assertEquals(bsD2[0][2], bsD2[2][0], 1E-10);
    // Derivative strike-volatility.
    double priceKPVP = FUNCTION.getPriceFunction(optionKP).evaluate(dataVP);
    double derivativeKV_FD = (priceKPVP + bs - priceKP - priceVP) / (deltaV * deltaK);
    assertEquals(derivativeKV_FD, bsD2[2][1], 1E-4);
    assertEquals(bsD2[1][2], bsD2[2][1], 1E-10);
  }

  @Test
  public void testPriceAdjoint2Put() {
    // Price
    double[] priceAdjoint = FUNCTION.getPriceAdjoint(OTM_PUT, ATM_DATA);
    double[] bsD = new double[3];
    double[][] bsD2 = new double[3][3];
    double bs = FUNCTION.getPriceAdjoint2(OTM_PUT, ATM_DATA, bsD, bsD2);
    assertEquals(priceAdjoint[0], bs, 1E-10);
    // First derivative
    for (int loopder = 0; loopder < 3; loopder++) {
      assertEquals(priceAdjoint[loopder + 1], bsD[loopder], 1E-10);
    }
    // Second derivative
    // Derivative forward-forward.
    double deltaF = 0.001;
    BlackFunctionData dataFP = new BlackFunctionData(F + deltaF, DF, SIGMA);
    BlackFunctionData dataFM = new BlackFunctionData(F - deltaF, DF, SIGMA);
    double priceFP = FUNCTION.getPriceFunction(OTM_PUT).evaluate(dataFP);
    double priceFM = FUNCTION.getPriceFunction(OTM_PUT).evaluate(dataFM);
    double derivativeFF_FD = (priceFP + priceFM - 2 * bs) / (deltaF * deltaF);
    assertEquals(derivativeFF_FD, bsD2[0][0], 1E-7);
    // Derivative volatility-volatility.
    double deltaV = 0.00001;
    BlackFunctionData dataVP = new BlackFunctionData(F, DF, SIGMA + deltaV);
    BlackFunctionData dataVM = new BlackFunctionData(F, DF, SIGMA - deltaV);
    double priceVP = FUNCTION.getPriceFunction(OTM_PUT).evaluate(dataVP);
    double priceVM = FUNCTION.getPriceFunction(OTM_PUT).evaluate(dataVM);
    double derivativeVV_FD = (priceVP + priceVM - 2 * bs) / (deltaV * deltaV);
    assertEquals(derivativeVV_FD, bsD2[1][1], 1E-3);
    // Derivative forward-volatility.
    BlackFunctionData dataFPVP = new BlackFunctionData(F + deltaF, DF, SIGMA + deltaV);
    double priceFPVP = FUNCTION.getPriceFunction(OTM_PUT).evaluate(dataFPVP);
    double derivativeFV_FD = (priceFPVP + bs - priceFP - priceVP) / (deltaF * deltaV);
    assertEquals(derivativeFV_FD, bsD2[1][0], 1E-5);
    assertEquals(bsD2[0][1], bsD2[1][0], 1E-10);
    // Derivative strike-strike.
    double deltaK = 0.001;
    EuropeanVanillaOption optionKP = new EuropeanVanillaOption(F - DELTA + deltaK, T, false);
    EuropeanVanillaOption optionKM = new EuropeanVanillaOption(F - DELTA - deltaK, T, false);
    double priceKP = FUNCTION.getPriceFunction(optionKP).evaluate(ATM_DATA);
    double priceKM = FUNCTION.getPriceFunction(optionKM).evaluate(ATM_DATA);
    double derivativeKK_FD = (priceKP + priceKM - 2 * bs) / (deltaK * deltaK);
    assertEquals(derivativeKK_FD, bsD2[2][2], 1E-8);
    // Derivative forward-strike.
    double priceFPKP = FUNCTION.getPriceFunction(optionKP).evaluate(dataFP);
    double derivativeFK_FD = (priceFPKP + bs - priceFP - priceKP) / (deltaF * deltaK);
    assertEquals(derivativeFK_FD, bsD2[2][0], 1E-7);
    assertEquals(bsD2[0][2], bsD2[2][0], 1E-10);
    // Derivative strike-volatility.
    double priceKPVP = FUNCTION.getPriceFunction(optionKP).evaluate(dataVP);
    double derivativeKV_FD = (priceKPVP + bs - priceKP - priceVP) / (deltaV * deltaK);
    assertEquals(derivativeKV_FD, bsD2[2][1], 1E-4);
    assertEquals(bsD2[1][2], bsD2[2][1], 1E-10);
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
