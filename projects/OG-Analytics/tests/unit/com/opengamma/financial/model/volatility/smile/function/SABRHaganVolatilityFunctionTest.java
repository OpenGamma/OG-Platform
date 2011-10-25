/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.MathException;
import com.opengamma.math.differentiation.FiniteDifferenceType;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * Tests related to the Hagan et al. approximation of the SABR implied volatility.
 */
public class SABRHaganVolatilityFunctionTest extends SABRVolatilityFunctionTestCase {

  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private static final SABRHaganVolatilityFunction FUNCTION = new SABRHaganVolatilityFunction();

  private static final double ALPHA = 0.05;
  private static final double BETA = 0.50;
  private static final double RHO = -0.25;
  private static final double NU = 0.4;
  private static final double FORWARD = 0.05;
  private static final SABRFormulaData DATA = new SABRFormulaData(FORWARD, ALPHA, BETA, NU, RHO);
  private static final double T = 4.5;
  private static final double STRIKE = 0.0450;
  private static final double STRIKE_0 = 0.00;
  private static final EuropeanVanillaOption CALL_ATM = new EuropeanVanillaOption(FORWARD, T, true);
  private static final EuropeanVanillaOption CALL_STRIKE = new EuropeanVanillaOption(STRIKE, T, true);
  private static final EuropeanVanillaOption CALL_STRIKE_0 = new EuropeanVanillaOption(STRIKE_0, T, true);

  @Override
  protected VolatilityFunctionProvider<SABRFormulaData> getFunction() {
    return FUNCTION;
  }

  @Test
  /**
   * Test if the Hagan volatility function implementation around ATM is numerically stable enough (the finite difference slope should be small enough).
   */
  public void testATMSmoothness() {
    double timeToExpiry = 1;
    boolean isCall = true;
    EuropeanVanillaOption option;
    double alpha = 0.05;
    double beta = 0.5;
    double nu = 0.50;
    double rho = -0.25;
    int nbPoints = 100;
    double forward = 0.05;
    double[] sabrVolatilty = new double[2 * nbPoints + 1];
    double range = 5E-9;
    double strike[] = new double[2 * nbPoints + 1];
    for (int looppts = -nbPoints; looppts <= nbPoints; looppts++) {
      strike[looppts + nbPoints] = forward + ((double) looppts) / nbPoints * range;
      option = new EuropeanVanillaOption(strike[looppts + nbPoints], timeToExpiry, isCall);
      SABRFormulaData SabrData = new SABRFormulaData(forward, alpha, beta, nu, rho);
      sabrVolatilty[looppts + nbPoints] = FUNCTION.getVolatilityFunction(option).evaluate(SabrData);
    }
    for (int looppts = -nbPoints; looppts < nbPoints; looppts++) {
      assertTrue(Math.abs(sabrVolatilty[looppts + nbPoints + 1] - sabrVolatilty[looppts + nbPoints]) / (strike[looppts + nbPoints + 1] - strike[looppts + nbPoints]) < 20.0);
    }

  }

  @Test
  /**
   * Tests the first order adjoint derivatives for the SABR Hagan volatility function. The derivatives with respect to the forward, strike, alpha, rho and nu are provided.
   */
  public void testVolatilityAdjoint() {
    // Price
    double volatility = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(DATA);
    double[] volatilityAdjoint = FUNCTION.getVolatilityAdjoint(CALL_STRIKE, DATA);
    assertEquals(volatility, volatilityAdjoint[0], 1E-6);
    // Price ATM
    double volatilityATM = FUNCTION.getVolatilityFunction(CALL_ATM).evaluate(DATA);
    double[] volatilityATMAdjoint = FUNCTION.getVolatilityAdjoint(CALL_ATM, DATA);
    assertEquals(volatilityATM, volatilityATMAdjoint[0], 1E-6);
    // Derivative forward.
    double deltaF = 0.000001;
    SABRFormulaData dataFP = new SABRFormulaData(FORWARD + deltaF, ALPHA, BETA, NU, RHO);
    SABRFormulaData dataFM = new SABRFormulaData(FORWARD - deltaF, ALPHA, BETA, NU, RHO);
    double volatilityFP = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(dataFP);
    double volatilityFM = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(dataFM);
    double derivativeF_FD = (volatilityFP - volatilityFM) / (2 * deltaF);
    assertEquals(derivativeF_FD, volatilityAdjoint[1], 1E-6);
    // Derivative strike.
    double deltaK = 0.000001;
    EuropeanVanillaOption optionKP = new EuropeanVanillaOption(STRIKE + deltaK, T, true);
    EuropeanVanillaOption optionKM = new EuropeanVanillaOption(STRIKE - deltaK, T, true);
    double volatilityKP = FUNCTION.getVolatilityFunction(optionKP).evaluate(DATA);
    double volatilityKM = FUNCTION.getVolatilityFunction(optionKM).evaluate(DATA);
    double derivativeK_FD = (volatilityKP - volatilityKM) / (2 * deltaK);
    assertEquals(derivativeK_FD, volatilityAdjoint[2], 1E-6);
    // Derivative alpha.
    double deltaA = 0.000001;
    SABRFormulaData dataAP = new SABRFormulaData(FORWARD, ALPHA + deltaA, BETA, NU, RHO);
    SABRFormulaData dataAM = new SABRFormulaData(FORWARD, ALPHA - deltaA, BETA, NU, RHO);
    double volatilityAP = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(dataAP);
    double volatilityAM = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(dataAM);
    double derivativeA_FD = (volatilityAP - volatilityAM) / (2 * deltaA);
    assertEquals(derivativeA_FD, volatilityAdjoint[3], 1E-6);
    // Derivative rho.
    double deltaR = 0.000001;
    SABRFormulaData dataRP = new SABRFormulaData(FORWARD, ALPHA, BETA, NU, RHO + deltaR);
    SABRFormulaData dataRM = new SABRFormulaData(FORWARD, ALPHA, BETA, NU, RHO - deltaR);
    double volatilityRP = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(dataRP);
    double volatilityRM = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(dataRM);
    double derivativeR_FD = (volatilityRP - volatilityRM) / (2 * deltaR);
    assertEquals(derivativeR_FD, volatilityAdjoint[4], 1E-6);
    // Derivative nu.
    double deltaN = 0.000001;
    SABRFormulaData dataNP = new SABRFormulaData(FORWARD, ALPHA, BETA, NU + deltaN, RHO);
    SABRFormulaData dataNM = new SABRFormulaData(FORWARD, ALPHA, BETA, NU - deltaN, RHO);
    double volatilityNP = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(dataNP);
    double volatilityNM = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(dataNM);
    double derivativeN_FD = (volatilityNP - volatilityNM) / (2 * deltaF);
    assertEquals(derivativeN_FD, volatilityAdjoint[5], 1E-6);
  }

  @Test
  /**
   * Tests the first order adjoint derivatives for the SABR Hagan volatility function. The derivatives with respect to the forward, strike, alpha, rho and nu are provided.
   */
  public void testVolatilityAdjointDebug() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    testVolatilityAdjoint(CALL_ATM, DATA, eps, tol);
    testVolatilityAdjoint(CALL_STRIKE, DATA, eps, tol);
  }

  /**
   *Test the alpha = 0 edge case
   */
  @Test
  public void testVolatilityAdjointAlpha0() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withAlpha(0.0);
    testVolatilityAdjoint(CALL_ATM, data, eps, tol);

    double volatility = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(data);
    double[] volatilityAdjoint = FUNCTION.getVolatilityAdjointDebug(CALL_STRIKE, data);

    assertEquals("Vol", volatility, volatilityAdjoint[0], tol);

    assertEquals("Forward Sensitivity", 0.0, volatilityAdjoint[1], tol);
    assertEquals("Strike Sensitivity", 0.0, volatilityAdjoint[2], tol);
    assertEquals("Alpha Sensitivity", 1e7, volatilityAdjoint[3], tol);
    assertEquals("Beta Sensitivity", 0.0, volatilityAdjoint[4], tol);
    assertEquals("Rho Sensitivity", 0.0, volatilityAdjoint[5], tol);
    assertEquals("Nu Sensitivity", 0.0, volatilityAdjoint[6], tol);
  }

  @Test
  public void testVolatilityAdjointSmallAlpha() {
    final double eps = 1e-9;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withAlpha(1e-5);
    testVolatilityAdjoint(CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(CALL_STRIKE, data, eps, tol);
    testVolatilityAdjoint(CALL_ATM.withStrike(DATA.getForward() * 1.1), data, eps, 100 * tol);
  }

  /**
   *Test the beta = 0 edge case
   */
  @Test
  public void testVolatilityAdjointBeta0() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withBeta(0.0);
    testVolatilityAdjoint(CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(CALL_STRIKE, data, eps, tol);
  }

  /**
   *Test the beta = 1 edge case
   */
  @Test
  public void testVolatilityAdjointBeta1() {
    final double eps = 1e-6;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withBeta(1.0);
    testVolatilityAdjoint(CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(CALL_STRIKE, data, eps, tol);
  }

  /**
   *Test the nu = 0 edge case
   */
  @Test
  //NOTWORKING
  public void testVolatilityAdjointNu0() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withNu(0.0000001);
    //    testVolatilityAdjoint(CALL_ATM, data, eps, tol);

    double[] volatilityAdjoint = FUNCTION.getVolatilityAdjointDebug(CALL_STRIKE, data);
    System.out.println(volatilityAdjoint[6]);
    testVolatilityAdjoint(CALL_STRIKE, data, eps, tol);
  }

  /**
   *Test the rho = -1 edge case
   */
  @Test
  public void testVolatilityAdjointRhoM1() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withRho(-1.0);
    testVolatilityAdjoint(CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(CALL_STRIKE, data, eps, tol);
  }

  /**
   *Test the rho = 1 edge case
   */
  @Test
  //NOTWORKING
  public void testVolatilityAdjointRho1() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withRho(1.0);
    testVolatilityAdjoint(CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(CALL_STRIKE, data, eps, tol);
  }

  private void testVolatilityAdjoint(final EuropeanVanillaOption optionData, final SABRFormulaData sabrData, final double eps, final double tol) {
    double volatility = FUNCTION.getVolatilityFunction(optionData).evaluate(sabrData);
    double[] volatilityAdjoint = FUNCTION.getVolatilityAdjointDebug(optionData, sabrData);

    // System.out.println("Debug alpha:" + volatilityAdjoint[3]);

    assertEquals("Vol", volatility, volatilityAdjoint[0], tol);

    assertEquals("Forward Sensitivity" + sabrData.toString(), fdSensitivity(optionData, sabrData, SABRParameter.Forward, eps), volatilityAdjoint[1], tol);
    assertEquals("Strike Sensitivity" + sabrData.toString(), fdSensitivity(optionData, sabrData, SABRParameter.Strike, eps), volatilityAdjoint[2], tol);
    assertEquals("Alpha Sensitivity" + sabrData.toString(), fdSensitivity(optionData, sabrData, SABRParameter.Alpha, eps), volatilityAdjoint[3], tol);
    assertEquals("Beta Sensitivity" + sabrData.toString(), fdSensitivity(optionData, sabrData, SABRParameter.Beta, eps), volatilityAdjoint[4], tol);
    assertEquals("Rho Sensitivity" + sabrData.toString(), fdSensitivity(optionData, sabrData, SABRParameter.Rho, eps), volatilityAdjoint[5], tol);
    assertEquals("Nu Sensitivity" + sabrData.toString(), fdSensitivity(optionData, sabrData, SABRParameter.Nu, eps), volatilityAdjoint[6], tol);
  }

  @Test
  /**
   * Tests the second order adjoint derivatives for the SABR Hagan volatility function. Only the derivatives with respect to the forward and the strike are provided.
   */
  public void testVolatilityAdjoint2() {
    // Price
    double volatility = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(DATA);
    double[] volatilityAdjoint = FUNCTION.getVolatilityAdjoint(CALL_STRIKE, DATA);
    double[] volD = new double[5];
    double[][] volD2 = new double[2][2];
    double vol = FUNCTION.getVolatilityAdjoint2(CALL_STRIKE, DATA, volD, volD2);
    assertEquals(volatility, vol, 1E-6);
    // Derivative
    for (int loopder = 0; loopder < 5; loopder++) {
      assertEquals("Derivative " + loopder, volatilityAdjoint[loopder + 1], volD[loopder], 1E-6);
    }
    // Derivative forward-forward
    double deltaF = 0.000001;
    SABRFormulaData dataFP = new SABRFormulaData(FORWARD + deltaF, ALPHA, BETA, NU, RHO);
    SABRFormulaData dataFM = new SABRFormulaData(FORWARD - deltaF, ALPHA, BETA, NU, RHO);
    double volatilityFP = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(dataFP);
    double volatilityFM = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(dataFM);
    double derivativeFF_FD = (volatilityFP + volatilityFM - 2 * volatility) / (deltaF * deltaF);
    assertEquals("SABR adjoint order 2: forward-forward", derivativeFF_FD, volD2[0][0], 1E-2);
    // Derivative strike-strike
    double deltaK = 0.000001;
    EuropeanVanillaOption optionKP = new EuropeanVanillaOption(STRIKE + deltaK, T, true);
    EuropeanVanillaOption optionKM = new EuropeanVanillaOption(STRIKE - deltaK, T, true);
    double volatilityKP = FUNCTION.getVolatilityFunction(optionKP).evaluate(DATA);
    double volatilityKM = FUNCTION.getVolatilityFunction(optionKM).evaluate(DATA);
    double derivativeKK_FD = (volatilityKP + volatilityKM - 2 * volatility) / (deltaK * deltaK);
    assertEquals("SABR adjoint order 2: strike-strike", derivativeKK_FD, volD2[1][1], 1E-2);
    // Derivative strike-forward
    double volatilityFPKP = FUNCTION.getVolatilityFunction(optionKP).evaluate(dataFP);
    double derivativeFK_FD = (volatilityFPKP + volatility - volatilityFP - volatilityKP) / (deltaF * deltaK);
    assertEquals("SABR adjoint order 2: forward-strike", derivativeFK_FD, volD2[0][1], 1E-2);
    assertEquals("SABR adjoint order 2: strike-forward", volD2[0][1], volD2[1][0], 1E-6);
  }

  @Test
  /**
   * Test the adjoint version with a strike = 0.
   */
  public void testVolatilityAdjointVolatilty0() {
    // Price
    double volatility = FUNCTION.getVolatilityFunction(CALL_STRIKE_0).evaluate(DATA);
    double[] volatilityAdjoint = FUNCTION.getVolatilityAdjoint(CALL_STRIKE_0, DATA);
    assertEquals(volatility, volatilityAdjoint[0], 1E-6);
  }

  @Test
  /**
   * Test the adjoint version with a correlation (rho) at 1.0 or very close.
   */
  public void volatilityAdjointCorrelation1() {
    double rho1 = 1.0;
    final SABRFormulaData data1 = new SABRFormulaData(FORWARD, ALPHA, BETA, NU, rho1);
    double volatility1 = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(data1);
    double[] volatilityAdjoint1 = FUNCTION.getVolatilityAdjoint(CALL_STRIKE, data1);
    assertEquals("SABR Hagan formula for rho=1", volatility1, volatilityAdjoint1[0], 1E-12);
    double deltaR = 1E-7;
    double rho1M = 1.0 - deltaR;
    final SABRFormulaData data1M = new SABRFormulaData(FORWARD, ALPHA, BETA, NU, rho1M);
    double volatility1M = FUNCTION.getVolatilityFunction(CALL_STRIKE).evaluate(data1M);
    double[] volatilityAdjoint1M = FUNCTION.getVolatilityAdjoint(CALL_STRIKE, data1M);
    assertEquals("SABR Hagan formula for rho=1-eps", volatility1M, volatilityAdjoint1M[0], 1E-12);
    assertEquals("SABR Hagan formula for rho=1-eps", volatilityAdjoint1[0], volatilityAdjoint1M[0], 1E-8);
    assertEquals(volatilityAdjoint1[3], volatilityAdjoint1M[3], 1E-6);
    //FIXME: Complete the derivatives computation in the degenerate case rho=1.
    //    assertEquals(volatilityAdjoint1[4], volatilityAdjoint1M[4], 1E-6);
    assertEquals(volatilityAdjoint1[5], volatilityAdjoint1M[5], 1E-6);
    //    double derivativeR_FD = (volatility1 - volatility1M) / deltaR;
    //    assertEquals(derivativeR_FD, volatilityAdjoint1[4], 1E-6);
  }

  //TODO write a fuzzer that hits SABR with random parameters 
  @Test
  public void testRandomParameters() {
    final double eps = 1e-5;
    final double tol = 1e-6;

    for (int count = 0; count < 100; count++) {
      double alpha = Math.exp(NORMAL.nextRandom() * 0.2 - 2);
      double beta = Math.random(); //TODO Uniform numbers in distribution
      double nu = Math.exp(NORMAL.nextRandom() * 0.3 - 1);
      double rho = 2 * Math.random() - 1;
      SABRFormulaData data = new SABRFormulaData(DATA.getForward(), alpha, beta, nu, rho);
      testVolatilityAdjoint(CALL_ATM, data, eps, tol);
      //    testVolatilityAdjoint(CALL_STRIKE, data, 1e-6);
    }
  }

  @Test
  public void testExtremeParameters2() {
    double alpha = 0.05;
    double beta = 0.5;
    double nu = 0;
    double rho = -0.25;
    double forward = DATA.getForward();
    EuropeanVanillaOption option = CALL_STRIKE.withStrike(forward * 1.01);

//    for (int i = 0; i < 200; i++) {
//      double e = -6 + 6.0 * i / 199;
//
//      SABRFormulaData data = new SABRFormulaData(forward, alpha, beta, nu, rho);
//      double volatility = FUNCTION.getVolatilityFunction(option).evaluate(data);
//      double[] volatilityAdjoint = FUNCTION.getVolatilityAdjointDebug(option, data);
//      System.out.println(nu + "\t" + volatility+"\t"+volatilityAdjoint[6]);
//      nu = Math.pow(10, e);
//    }

    SABRFormulaData data = new SABRFormulaData(forward, alpha, beta, 2.5e-2, rho);
//    double volatility = FUNCTION.getVolatilityFunction(option).evaluate(data);
    
    double[] volatilityAdjoint = FUNCTION.getVolatilityAdjointDebug(option, data);
    System.out.println(volatilityAdjoint[6]);
    
    //    testVolatilityAdjoint(option, data, 1e-6);
    //    testVolatilityAdjoint(CALL_ATM, data, 1e-5);
  }

  //Extreme 
  @Test
  public void testExtremeParameters() {
    double alpha = 0;
    double beta = 1.0;
    double nu = 19.0;
    double rho = -0.97;

    double fwd = 0.0416;
    double k = 0.005;
    double t = 17.5; //this is too long for SABR
    EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
    //  for (int i = 0; i < 200; i++) {
    //  rho = -1.0 + i * 2.0 / 200;
    SABRFormulaData data = new SABRFormulaData(fwd, alpha, beta, nu, rho);
    double vol = getFunction().getVolatilityFunction(option).evaluate(data);
    System.out.println(rho + "\t" + vol);
    // }
  }

  private enum SABRParameter {
    Forward,
    Strike,
    Alpha,
    Beta,
    Nu,
    Rho
  }

  private double fdSensitivity(final EuropeanVanillaOption optionData, final SABRFormulaData sabrData,
      final SABRParameter param, final double delta) {

    Function1D<SABRFormulaData, Double> funcC = null;
    Function1D<SABRFormulaData, Double> funcB = null;
    Function1D<SABRFormulaData, Double> funcA = null;
    SABRFormulaData dataC = null;
    SABRFormulaData dataB = sabrData;
    SABRFormulaData dataA = null;
    final Function1D<SABRFormulaData, Double> func = FUNCTION.getVolatilityFunction(optionData);

    FiniteDifferenceType fdType = null;

    switch (param) {
      case Strike:
        double strike = optionData.getStrike();
        if (strike >= delta) {
          fdType = FiniteDifferenceType.CENTRAL;
          funcA = FUNCTION.getVolatilityFunction(optionData.withStrike(strike - delta));
          funcC = FUNCTION.getVolatilityFunction(optionData.withStrike(strike + delta));
        } else {
          fdType = FiniteDifferenceType.FORWARD;
          funcA = func;
          funcB = FUNCTION.getVolatilityFunction(optionData.withStrike(strike + delta));
          funcC = FUNCTION.getVolatilityFunction(optionData.withStrike(strike + 2 * delta));
        }
        dataC = sabrData;
        dataB = sabrData;
        dataA = sabrData;
        break;
      case Forward:
        double fwd = sabrData.getForward();
        if (fwd > delta) {
          fdType = FiniteDifferenceType.CENTRAL;
          dataA = sabrData.withForward(fwd - delta);
          dataC = sabrData.withForward(fwd + delta);
        } else {
          fdType = FiniteDifferenceType.FORWARD;
          dataA = sabrData;
          dataB = sabrData.withForward(fwd + delta);
          dataC = sabrData.withForward(fwd + 2 * delta);
        }
        funcC = func;
        funcB = func;
        funcA = func;
        break;
      case Alpha:
        double a = sabrData.getAlpha();
        if (a >= delta) {
          fdType = FiniteDifferenceType.CENTRAL;
          dataA = sabrData.withAlpha(a - delta);
          dataC = sabrData.withAlpha(a + delta);
        } else {
          fdType = FiniteDifferenceType.FORWARD;
          dataA = sabrData;
          dataB = sabrData.withAlpha(a + delta);
          dataC = sabrData.withAlpha(a + 2 * delta);
        }
        funcC = func;
        funcB = func;
        funcA = func;
        break;
      case Beta:
        double b = sabrData.getBeta();
        if (b >= delta) {
          fdType = FiniteDifferenceType.CENTRAL;
          dataA = sabrData.withBeta(b - delta);
          dataC = sabrData.withBeta(b + delta);
        } else {
          fdType = FiniteDifferenceType.FORWARD;
          dataA = sabrData;
          dataB = sabrData.withBeta(b + delta);
          dataC = sabrData.withBeta(b + 2 * delta);
        }
        funcC = func;
        funcB = func;
        funcA = func;
        break;
      case Nu:
        double n = sabrData.getNu();
        if (n >= delta) {
          fdType = FiniteDifferenceType.CENTRAL;
          dataA = sabrData.withNu(n - delta);
          dataC = sabrData.withNu(n + delta);
        } else {
          fdType = FiniteDifferenceType.FORWARD;
          dataA = sabrData;
          dataB = sabrData.withNu(n + delta);
          dataC = sabrData.withNu(n + 2 * delta);
        }
        funcC = func;
        funcB = func;
        funcA = func;
        break;
      case Rho:
        double r = sabrData.getRho();
        if ((r + 1) < delta) {
          fdType = FiniteDifferenceType.FORWARD;
          dataA = sabrData;
          dataB = sabrData.withRho(r + delta);
          dataC = sabrData.withRho(r + 2 * delta);
        } else if ((1 - r) < delta) {
          fdType = FiniteDifferenceType.BACKWARD;
          dataA = sabrData.withRho(r - 2 * delta);
          dataB = sabrData.withRho(r - delta);
          dataC = sabrData;
        } else {
          fdType = FiniteDifferenceType.CENTRAL;
          dataC = sabrData.withRho(r + delta);
          dataA = sabrData.withRho(r - delta);
        }
        funcC = func;
        funcB = func;
        funcA = func;
        break;
    }

    switch (fdType) {
      case FORWARD:
        return (-1.5 * funcA.evaluate(dataA) + 2.0 * funcB.evaluate(dataB) - 0.5 * funcC.evaluate(dataC)) / delta;
      case BACKWARD:
        return (0.5 * funcA.evaluate(dataA) - 2.0 * funcB.evaluate(dataB) + 1.5 * funcC.evaluate(dataC)) / delta;
      case CENTRAL:
        return (funcC.evaluate(dataC) - funcA.evaluate(dataA)) / 2.0 / delta;
      default:
        throw new MathException("enum not found");
    }
  }
}
