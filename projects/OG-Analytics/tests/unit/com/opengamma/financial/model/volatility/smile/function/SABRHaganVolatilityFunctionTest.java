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
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.math.MathException;
import com.opengamma.math.differentiation.FiniteDifferenceType;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.math.rootfinding.BracketRoot;
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
  private static final SABRFormulaData DATA = new SABRFormulaData(ALPHA, BETA, RHO, NU);
  private static final double T = 4.5;
  private static final double STRIKE_ITM = 0.0450;
  private static final double STRIKE_OTM = 0.0550;

  private static final EuropeanVanillaOption CALL_ATM = new EuropeanVanillaOption(FORWARD, T, true);
  private static final EuropeanVanillaOption CALL_ITM = new EuropeanVanillaOption(STRIKE_ITM, T, true);
  private static final EuropeanVanillaOption CALL_OTM = new EuropeanVanillaOption(STRIKE_OTM, T, true);

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
      SABRFormulaData SabrData = new SABRFormulaData(alpha, beta, rho, nu);
      sabrVolatilty[looppts + nbPoints] = FUNCTION.getVolatilityFunction(option, forward).evaluate(SabrData);
    }
    for (int looppts = -nbPoints; looppts < nbPoints; looppts++) {
      assertTrue(Math.abs(sabrVolatilty[looppts + nbPoints + 1] - sabrVolatilty[looppts + nbPoints]) / (strike[looppts + nbPoints + 1] - strike[looppts + nbPoints]) < 20.0);
    }

  }

  @Test
  /**
   * Tests the first order adjoint derivatives for the SABR Hagan volatility function.
   * The derivatives with respect to the forward, strike, alpha, beta  rho and nu are provided.
   */
  public void testVolatilityAdjointDebug() {
    final double eps = 1e-6;
    final double tol = 1e-5;
    testVolatilityAdjoint(FORWARD, CALL_ATM, DATA, eps, tol);
    testVolatilityAdjoint(FORWARD, CALL_ITM, DATA, eps, tol);
    testVolatilityAdjoint(FORWARD, CALL_OTM, DATA, eps, tol);
  }

  /**
   * Test small strike edge case. Vol -> infinity as strike -> 0, so the strike is floored - tested against finite difference below this
   * floor will give spurious results
   */
  @Test
  public void testVolatilityAdjointSmallStrike() {
    final double eps = 1e-10;
    final double tol = 1e-6;
    final double strike = 2e-6 * FORWARD;
    testVolatilityAdjoint(FORWARD, CALL_ATM.withStrike(strike), DATA, eps, tol);
  }

  /**
   *Test the alpha = 0 edge case. Implied vol is zero for alpha = 0, and except in the ATM case, the alpha sensitivity is infinite. We
   *choose to (arbitrarily) return 1e7 in this case.
   */
  @Test
  public void testVolatilityAdjointAlpha0() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withAlpha(0.0);
    testVolatilityAdjoint(FORWARD, CALL_ATM, data, eps, tol);

    double volatility = FUNCTION.getVolatilityFunction(CALL_ITM, FORWARD).evaluate(data);
    double[] volatilityAdjoint = FUNCTION.getVolatilityAdjoint(CALL_ITM, FORWARD, data);

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
    final double eps = 1e-7;
    final double tol = 1e-3;
    SABRFormulaData data = DATA.withAlpha(1e-5);
    testVolatilityAdjoint(FORWARD, CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(FORWARD, CALL_ITM, data, eps, tol);
    testVolatilityAdjoint(FORWARD, CALL_OTM, data, eps, tol);
  }

  /**
   *Test the beta = 0 edge case
   */
  @Test
  public void testVolatilityAdjointBeta0() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withBeta(0.0);
    testVolatilityAdjoint(FORWARD, CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(FORWARD, CALL_ITM, data, eps, tol);
    testVolatilityAdjoint(FORWARD, CALL_OTM, data, eps, tol);
  }

  /**
   *Test the beta = 1 edge case
   */
  @Test
  public void testVolatilityAdjointBeta1() {
    final double eps = 1e-6;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withBeta(1.0);
    testVolatilityAdjoint(FORWARD, CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(FORWARD, CALL_ITM, data, eps, tol);
    testVolatilityAdjoint(FORWARD, CALL_OTM, data, eps, tol);
  }

  /**
   *Test the nu = 0 edge case
   */
  @Test
  public void testVolatilityAdjointNu0() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withNu(0.0);
    testVolatilityAdjoint(FORWARD, CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(FORWARD, CALL_ITM, data, eps, 2e-4);
    testVolatilityAdjoint(FORWARD, CALL_OTM, data, eps, 5e-5);
  }

  /**
   *Test the rho = -1 edge case
   */
  @Test
  public void testVolatilityAdjointRhoM1() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withRho(-1.0);
    testVolatilityAdjoint(FORWARD, CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(FORWARD, CALL_ITM, data, eps, tol);
    testVolatilityAdjoint(FORWARD, CALL_OTM, data, eps, tol);
  }

  /**
   *Test the rho = 1 edge case
   */
  @Test
  public void testVolatilityAdjointRho1() {
    final double eps = 1e-4;
    final double tol = 1e-5;
    SABRFormulaData data = DATA.withRho(1.0);
    testVolatilityAdjoint(FORWARD, CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(FORWARD, CALL_ITM, data, eps, tol);
    testVolatilityAdjoint(FORWARD, CALL_OTM, data, eps, tol);
  }

  @Test
  public void testVolatilityAdjointLargeRhoZLessThan1() {
    final double eps = 1e-4;
    final double tol = 1e-5;
    SABRFormulaData data = DATA.withRho(1.0 - 1e-9);
    testVolatilityAdjoint(FORWARD, CALL_ITM, data, eps, tol);
  }

  @Test
  public void testVolatilityAdjointLargeRhoZGreaterThan1() {
    final double eps = 1e-11;
    final double tol = 1e-4;
    SABRFormulaData data = DATA.withRho(1.0 - 1e-9).withAlpha(0.15 * ALPHA);
    testVolatilityAdjoint(FORWARD, CALL_ITM, data, eps, tol);
  }

  @Test
  public void testVolatilityModelAdjoint() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    SABRFormulaData data = DATA;
    testVolatilityModelAdjoint(FORWARD, CALL_ATM, data, eps, tol);
    testVolatilityModelAdjoint(FORWARD, CALL_ITM, data, eps, tol);
    testVolatilityModelAdjoint(FORWARD, CALL_OTM, data, eps, tol);
  }

  @Test
  public void testVolatilityModelAdjointRhoM1() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withRho(-1.0);
    testVolatilityModelAdjoint(FORWARD, CALL_ATM, data, eps, tol); //z=0 case
    double z = -0.975;
    double strike = strikeForZ(z, FORWARD, ALPHA, BETA, NU);
    testVolatilityModelAdjoint(FORWARD, CALL_ATM.withStrike(strike), data, eps, 5e-4);
    z = 2.0;
    strike = strikeForZ(z, FORWARD, ALPHA, BETA, NU);
    testVolatilityModelAdjoint(FORWARD, CALL_ATM.withStrike(strike), data, eps, tol);
    z = -2.0;
    strike = strikeForZ(z, FORWARD, ALPHA, BETA, NU);
    //The true rho sensitivity at rho=-1 is infinity
    testVolatilityModelAdjoint(FORWARD, CALL_ATM.withStrike(strike), DATA.withRho(-1 + 1e-3), eps, 1e-4);
    testVolatilityModelAdjoint(FORWARD, CALL_ATM.withStrike(strike), data, 1e-6, 1.5);
  }

  @Test
  public void testVolatilityModelAdjointRhoP1() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withRho(1.0);
    testVolatilityModelAdjoint(FORWARD, CALL_ATM, data, eps, tol); //z=0 case
    double z = 0.975;
    double strike = strikeForZ(z, FORWARD, ALPHA, BETA, NU);
    testVolatilityModelAdjoint(FORWARD, CALL_ATM.withStrike(strike), data, eps, 1e-2);
    z = -2.0;
    strike = strikeForZ(z, FORWARD, ALPHA, BETA, NU);
    testVolatilityModelAdjoint(FORWARD, CALL_ATM.withStrike(strike), data, eps, 50 * tol);
    z = 2.0;
    strike = strikeForZ(z, FORWARD, ALPHA, BETA, NU);
    //The true rho sensitivity at rho= 1 is -infinity
    testVolatilityModelAdjoint(FORWARD, CALL_ATM.withStrike(strike), DATA.withRho(1 - 1e-3), eps, 5e-5);
    testVolatilityModelAdjoint(FORWARD, CALL_ATM.withStrike(strike), data, 1e-6, 1.0);
  }

  @Test
  public void testVolatilityModelAdjointBeta0() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withBeta(0);
    testVolatilityModelAdjoint(FORWARD, CALL_ATM, data, eps, tol);
    testVolatilityModelAdjoint(FORWARD, CALL_ITM, data, eps, tol);
    testVolatilityModelAdjoint(FORWARD, CALL_OTM, data, eps, tol);
  }

  @Test
  public void testVolatilityModelAdjointBeta1() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withBeta(1);
    testVolatilityModelAdjoint(FORWARD, CALL_ATM, data, eps, tol);
    testVolatilityModelAdjoint(FORWARD, CALL_ITM, data, eps, tol);
    testVolatilityModelAdjoint(FORWARD, CALL_OTM, data, eps, tol);
  }

  @Test
  public void testVolatilityModelAdjointNu0() {
    final double eps = 1e-6;
    final double tol = 1e-6;
    SABRFormulaData data = DATA.withNu(0);
    testVolatilityModelAdjoint(FORWARD, CALL_ATM, data, eps, tol);
    testVolatilityModelAdjoint(FORWARD, CALL_ITM, data, eps, tol);
    testVolatilityModelAdjoint(FORWARD, CALL_OTM, data, eps, tol);
  }

  @Test
  public void testVolatilityModelAdjoinAlpha0() {

    final double eps = 1e-10;
    final double tol = 1e-2;

    double z = getZ(FORWARD, CALL_ITM.getStrike(), ALPHA, BETA, NU);
    double alpha = z / 2e8;
    SABRFormulaData data = DATA.withAlpha(alpha);
    testVolatilityModelAdjoint(FORWARD, CALL_ITM, data, eps, tol);

    z = getZ(FORWARD, CALL_OTM.getStrike(), ALPHA, BETA, NU);
    alpha = -z / 2e6;
    data = DATA.withAlpha(alpha);
    testVolatilityModelAdjoint(FORWARD, CALL_ITM, data, eps, tol);

  }

  /**
   *Test the beta = 0.0, rho = 1 edge case
   */
  @Test
  public void testVolatilityModelAdjointBeta0Rho1() {
    final double eps = 1e-4;
    final double tol = 1e-5;
    SABRFormulaData data = DATA.withRho(1.0).withBeta(0.0).withNu(20.0);
    testVolatilityModelAdjoint(FORWARD, CALL_ATM, data, eps, tol);
    // testVolatilityModelAdjoint(FORWARD, CALL_ITM, data, eps, tol);
    testVolatilityModelAdjoint(FORWARD, CALL_OTM, data, eps, tol);
  }

  private double getZ(final double forward, final double strike, final double alpha, final double beta, final double nu) {
    return nu / alpha * Math.pow(forward * strike, (1 - beta) / 2) * Math.log(forward / strike);
  }

  private double strikeForZ(final double z, final double forward, final double alpha, final double beta, final double nu) {
    if (z == 0) {
      return forward;
    }
    if (beta == 1) {
      return forward * Math.exp(-alpha * z / nu);
    }

    final BracketRoot bracketer = new BracketRoot();
    final BisectionSingleRootFinder rootFinder = new BisectionSingleRootFinder(1e-5);

    Function1D<Double, Double> func = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double strike) {
        return getZ(forward, strike, alpha, beta, nu) - z;
      }
    };

    final double k = forward * Math.exp(-alpha * z / nu * Math.pow(forward, beta - 1));
    double l, h;
    if (z > 0) {
      h = k;
      l = h / 2;
    } else {
      l = k;
      h = 2 * l;
    }

    double[] brackets = bracketer.getBracketedPoints(func, l, h, forward / 20, 20 * forward);
    return rootFinder.getRoot(func, brackets[0], brackets[1]);
  }

  private void testVolatilityAdjoint(final double forward, final EuropeanVanillaOption optionData, final SABRFormulaData sabrData, final double eps, final double tol) {
    double volatility = FUNCTION.getVolatilityFunction(optionData, forward).evaluate(sabrData);
    double[] volatilityAdjoint = FUNCTION.getVolatilityAdjoint(optionData, forward, sabrData);

    assertEquals("Vol", volatility, volatilityAdjoint[0], tol);

    assertEqualsRelTol("Forward Sensitivity" + sabrData.toString(), fdSensitivity(optionData, forward, sabrData, SABRParameter.Forward, eps), volatilityAdjoint[1], tol);
    assertEqualsRelTol("Strike Sensitivity" + sabrData.toString(), fdSensitivity(optionData, forward, sabrData, SABRParameter.Strike, eps), volatilityAdjoint[2], tol);
    assertEqualsRelTol("Alpha Sensitivity" + sabrData.toString(), fdSensitivity(optionData, forward, sabrData, SABRParameter.Alpha, eps), volatilityAdjoint[3], tol);
    assertEqualsRelTol("Beta Sensitivity" + sabrData.toString(), fdSensitivity(optionData, forward, sabrData, SABRParameter.Beta, eps), volatilityAdjoint[4], tol);
    assertEqualsRelTol("Rho Sensitivity" + sabrData.toString(), fdSensitivity(optionData, forward, sabrData, SABRParameter.Rho, eps), volatilityAdjoint[5], tol);
    assertEqualsRelTol("Nu Sensitivity" + sabrData.toString(), fdSensitivity(optionData, forward, sabrData, SABRParameter.Nu, eps), volatilityAdjoint[6], tol);
  }

  private void testVolatilityModelAdjoint(final double forward, final EuropeanVanillaOption optionData, final SABRFormulaData sabrData, final double eps, final double tol) {
    //    double volatility = FUNCTION.getVolatilityFunction(optionData, forward).evaluate(sabrData);
    double[] volatilityAdjoint = FUNCTION.getVolatilityModelAdjoint(optionData, forward, sabrData);

    assertEqualsRelTol("Alpha Sensitivity" + sabrData.toString(), fdSensitivity(optionData, forward, sabrData, SABRParameter.Alpha, eps), volatilityAdjoint[0], tol);
    assertEqualsRelTol("Beta Sensitivity" + sabrData.toString(), fdSensitivity(optionData, forward, sabrData, SABRParameter.Beta, eps), volatilityAdjoint[1], tol);
    assertEqualsRelTol("Rho Sensitivity" + sabrData.toString(), fdSensitivity(optionData, forward, sabrData, SABRParameter.Rho, eps), volatilityAdjoint[2], tol);
    assertEqualsRelTol("Nu Sensitivity" + sabrData.toString(), fdSensitivity(optionData, forward, sabrData, SABRParameter.Nu, eps), volatilityAdjoint[3], tol);
  }

  private void assertEqualsRelTol(final String msg, final double exp, final double act, final double tol) {
    final double delta = (Math.abs(exp) + Math.abs(act)) * tol / 2.0;
    assertEquals(msg, exp, act, delta);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the second order adjoint derivatives for the SABR Hagan volatility function. Only the derivatives with respect to the forward and the strike are provided.
   */
  public void testVolatilityAdjoint2() {
    // Price
    double volatility = FUNCTION.getVolatilityFunction(CALL_ITM, FORWARD).evaluate(DATA);
    double[] volatilityAdjoint = FUNCTION.getVolatilityAdjointOld(CALL_ITM, FORWARD, DATA);
    double[] volD = new double[5];
    double[][] volD2 = new double[2][2];
    double vol = FUNCTION.getVolatilityAdjoint2(CALL_ITM, FORWARD, DATA, volD, volD2);
    assertEquals(volatility, vol, 1E-6);
    // Derivative
    for (int loopder = 0; loopder < 5; loopder++) {
      assertEquals("Derivative " + loopder, volatilityAdjoint[loopder + 1], volD[loopder], 1E-6);
    }
    // Derivative forward-forward
    double deltaF = 0.000001;
    double volatilityFP = FUNCTION.getVolatilityFunction(CALL_ITM, FORWARD + deltaF).evaluate(DATA);
    double volatilityFM = FUNCTION.getVolatilityFunction(CALL_ITM, FORWARD - deltaF).evaluate(DATA);
    double derivativeFF_FD = (volatilityFP + volatilityFM - 2 * volatility) / (deltaF * deltaF);
    assertEquals("SABR adjoint order 2: forward-forward", derivativeFF_FD, volD2[0][0], 1E-2);
    // Derivative strike-strike
    double deltaK = 0.000001;
    EuropeanVanillaOption optionKP = new EuropeanVanillaOption(STRIKE_ITM + deltaK, T, true);
    EuropeanVanillaOption optionKM = new EuropeanVanillaOption(STRIKE_ITM - deltaK, T, true);
    double volatilityKP = FUNCTION.getVolatilityFunction(optionKP, FORWARD).evaluate(DATA);
    double volatilityKM = FUNCTION.getVolatilityFunction(optionKM, FORWARD).evaluate(DATA);
    double derivativeKK_FD = (volatilityKP + volatilityKM - 2 * volatility) / (deltaK * deltaK);
    assertEquals("SABR adjoint order 2: strike-strike", derivativeKK_FD, volD2[1][1], 1E-2);
    // Derivative strike-forward
    double volatilityFPKP = FUNCTION.getVolatilityFunction(optionKP, FORWARD + deltaF).evaluate(DATA);
    double derivativeFK_FD = (volatilityFPKP + volatility - volatilityFP - volatilityKP) / (deltaF * deltaK);
    assertEquals("SABR adjoint order 2: forward-strike", derivativeFK_FD, volD2[0][1], 1E-2);
    assertEquals("SABR adjoint order 2: strike-forward", volD2[0][1], volD2[1][0], 1E-6);
  }

  //TODO write a fuzzer that hits SABR with random parameters
  @Test
  public void testRandomParameters() {
    final double eps = 1e-5;
    final double tol = 1e-3;

    for (int count = 0; count < 100; count++) {
      double alpha = Math.exp(NORMAL.nextRandom() * 0.2 - 2);
      double beta = Math.random(); //TODO Uniform numbers in distribution
      double nu = Math.exp(NORMAL.nextRandom() * 0.3 - 1);
      double rho = 2 * Math.random() - 1;
      SABRFormulaData data = new SABRFormulaData(alpha, beta, rho, nu);
      testVolatilityAdjoint(FORWARD, CALL_ATM, data, eps, tol);
      testVolatilityAdjoint(FORWARD, CALL_ITM, data, eps, tol);
      testVolatilityAdjoint(FORWARD, CALL_OTM, data, eps, tol);
    }
  }

  @Test(enabled = false)
  public void testExtremeParameters2() {
    @SuppressWarnings("unused")
    double alpha = 0.2 * ALPHA;
    //    double beta = 0.5;
    //    double nu = 0.2;
    @SuppressWarnings("unused")
    double rho = 1 - 1e-9;

    //    double strike = 1e-8;
    //    EuropeanVanillaOption option = CALL_ITM.withStrike(strike);

    //  EuropeanVanillaOption option = CALL_STRIKE.withStrike(forward * 1.01);

    for (int i = 0; i < 200; i++) {
      //      double e = -5 - 15.*i/199;
      //      rho = 1.0 - Math.pow(10,e);
      double forward = 0.045 + 0.01 * i / 199;

      double volatility = FUNCTION.getVolatilityFunction(CALL_ITM, forward).evaluate(DATA);
      double[] volatilityAdjoint = FUNCTION.getVolatilityAdjoint(CALL_ITM, FORWARD, DATA);
      System.out.println(forward + "\t" + volatility + "\t" + volatilityAdjoint[1]);

    }
    //
    //    SABRFormulaData data = new SABRFormulaData(forward, alpha, beta, nu, rho);
    //    double volatility = FUNCTION.getVolatilityFunction(option).evaluate(data);
    //
    //    double[] volatilityAdjoint = FUNCTION.getVolatilityAdjointDebug(option, data);
    //    System.out.println(volatility + "\t" + volatilityAdjoint[2]);

    //    testVolatilityAdjoint(option, data, 1e-6);
    //    testVolatilityAdjoint(CALL_ATM, data, 1e-5);
  }

  /**
   *Calculate the true SABR delta and gamma and compare with that found by finite difference
   */
  public void testGreeks() {
    double eps = 1e-3;
    double f = 1.2;
    double k = 1.4;
    double t = 5.0;
    double alpha = 0.3;
    double beta = 0.6;
    double rho = -0.4;
    double nu = 0.4;
    SABRFormulaData sabrData = new SABRFormulaData(alpha, beta, rho, nu);

    SABRHaganVolatilityFunction sabr = new SABRHaganVolatilityFunction();
    double[] vol = sabr.getVolatilityAdjoint(new EuropeanVanillaOption(k, t, true), f, sabrData);
    double bsDelta = BlackFormulaRepository.delta(f, k, t, vol[0], true);
    double bsVega = BlackFormulaRepository.vega(f, k, t, vol[0]);
    double volForwardSense = vol[1];
    double delta = bsDelta + bsVega * volForwardSense;

    double volUp = sabr.getVolatility(f + eps, k, t, alpha, beta, rho, nu);
    double volDown = sabr.getVolatility(f - eps, k, t, alpha, beta, rho, nu);
    double priceUp = BlackFormulaRepository.price(f + eps, k, t, volUp, true);
    double price = BlackFormulaRepository.price(f, k, t, vol[0], true);
    double priceDown = BlackFormulaRepository.price(f - eps, k, t, volDown, true);
    double fdDelta = (priceUp - priceDown) / 2 / eps;
    assertEquals(fdDelta, delta, 1e-6);

    double bsVanna = BlackFormulaRepository.vanna(f, k, t, vol[0]);
    double bsGamma = BlackFormulaRepository.gamma(f, k, t, vol[0]);

    double[] volD1 = new double[5];
    double[][] volD2 = new double[2][2];
    sabr.getVolatilityAdjoint2(new EuropeanVanillaOption(k, t, true), f, sabrData, volD1, volD2);
    double d2Sigmad2Fwd = volD2[0][0];
    double gamma = bsGamma + 2 * bsVanna * vol[1] + bsVega * d2Sigmad2Fwd;
    double fdGamma = (priceUp + priceDown - 2 * price) / eps / eps;

    double d2Sigmad2FwdFD = (volUp + volDown - 2 * vol[0]) / eps / eps;
    assertEquals(d2Sigmad2FwdFD, d2Sigmad2Fwd, 1e-4);

    assertEquals(fdGamma, gamma, 1e-2);
  }

  private enum SABRParameter {
    Forward,
    Strike,
    Alpha,
    Beta,
    Nu,
    Rho
  }

  private double fdSensitivity(final EuropeanVanillaOption optionData, final double forward, final SABRFormulaData sabrData,
      final SABRParameter param, final double delta) {

    Function1D<SABRFormulaData, Double> funcC = null;
    Function1D<SABRFormulaData, Double> funcB = null;
    Function1D<SABRFormulaData, Double> funcA = null;
    SABRFormulaData dataC = null;
    SABRFormulaData dataB = sabrData;
    SABRFormulaData dataA = null;
    final Function1D<SABRFormulaData, Double> func = FUNCTION.getVolatilityFunction(optionData, forward);

    FiniteDifferenceType fdType = null;

    switch (param) {
      case Strike:
        double strike = optionData.getStrike();
        if (strike >= delta) {
          fdType = FiniteDifferenceType.CENTRAL;
          funcA = FUNCTION.getVolatilityFunction(optionData.withStrike(strike - delta), forward);
          funcC = FUNCTION.getVolatilityFunction(optionData.withStrike(strike + delta), forward);
        } else {
          fdType = FiniteDifferenceType.FORWARD;
          funcA = func;
          funcB = FUNCTION.getVolatilityFunction(optionData.withStrike(strike + delta), forward);
          funcC = FUNCTION.getVolatilityFunction(optionData.withStrike(strike + 2 * delta), forward);
        }
        dataC = sabrData;
        dataB = sabrData;
        dataA = sabrData;
        break;
      case Forward:
        if (forward > delta) {
          fdType = FiniteDifferenceType.CENTRAL;
          funcA = FUNCTION.getVolatilityFunction(optionData, forward - delta);
          funcC = FUNCTION.getVolatilityFunction(optionData, forward + delta);
        } else {
          fdType = FiniteDifferenceType.FORWARD;
          funcA = func;
          funcB = FUNCTION.getVolatilityFunction(optionData, forward + delta);
          funcC = FUNCTION.getVolatilityFunction(optionData, forward + 2 * delta);
        }
        dataC = sabrData;
        dataB = sabrData;
        dataA = sabrData;
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
