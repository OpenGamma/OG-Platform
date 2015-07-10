/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Tests related to the Hagan et al. approximation of the SABR implied volatility.
 */
@Test(groups = TestGroup.UNIT)
public class SABRHaganVolatilityFunctionTest extends SABRVolatilityFunctionTestCase {

  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1, new MersenneTwister64(MersenneTwister.DEFAULT_SEED));

  private static final SABRHaganVolatilityFunction FUNCTION = new SABRHaganVolatilityFunction();

  private static final double ALPHA = 0.05;
  private static final double BETA = 0.50;
  private static final double RHO = -0.25;
  private static final double NU = 0.4;
  private static final double F = 0.05;
  private static final SABRFormulaData DATA = new SABRFormulaData(ALPHA, BETA, RHO, NU);
  private static final double T = 4.5;
  private static final double STRIKE_ITM = 0.0450;
  private static final double STRIKE_OTM = 0.0550;

  private static final EuropeanVanillaOption CALL_ATM = new EuropeanVanillaOption(F, T, true);
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
    final double timeToExpiry = 1;
    final boolean isCall = true;
    EuropeanVanillaOption option;
    final double alpha = 0.05;
    final double beta = 0.5;
    final double nu = 0.50;
    final double rho = -0.25;
    final int nbPoints = 100;
    final double forward = 0.05;
    final double[] sabrVolatilty = new double[2 * nbPoints + 1];
    final double range = 5E-9;
    final double strike[] = new double[2 * nbPoints + 1];
    for (int looppts = -nbPoints; looppts <= nbPoints; looppts++) {
      strike[looppts + nbPoints] = forward + ((double) looppts) / nbPoints * range;
      option = new EuropeanVanillaOption(strike[looppts + nbPoints], timeToExpiry, isCall);
      final SABRFormulaData SabrData = new SABRFormulaData(alpha, beta, rho, nu);
      sabrVolatilty[looppts + nbPoints] = FUNCTION.getVolatilityFunction(option, forward).evaluate(SabrData);
    }
    for (int looppts = -nbPoints; looppts < nbPoints; looppts++) {
      assertTrue(Math.abs(sabrVolatilty[looppts + nbPoints + 1] - sabrVolatilty[looppts + nbPoints]) / (strike[looppts + nbPoints + 1] - strike[looppts + nbPoints]) < 20.0);
    }
  }

  @Test(enabled = false)
  /**
   * Produce the smile for a given set of strikes.
   */
  public void smile() {
    final double alpha = 0.04079820992199477;
    final double beta = 0.5;
    final double rho = 0.12483799350466732;
    final double nu = 1.1156276403408933;
    final double timeToExpiry = 5.0;
    final double forward = 0.03189998273775524;
    final int nbpoints = 20;
    final double startStrike = 0.0001;
    final double endStrike = 0.2500;
    final SABRFormulaData SabrData = new SABRFormulaData(alpha, beta, rho, nu);
    final double[] strikes = new double[nbpoints + 1];
    final double[] sabrVolatilty = new double[nbpoints + 1];
    EuropeanVanillaOption option;
    for (int loopstrike = 0; loopstrike <= nbpoints; loopstrike++) {
      strikes[loopstrike] = startStrike + loopstrike * (endStrike - startStrike) / nbpoints;
      option = new EuropeanVanillaOption(strikes[loopstrike], timeToExpiry, true);
      sabrVolatilty[loopstrike] = FUNCTION.getVolatilityFunction(option, forward).evaluate(SabrData);
    }
  }

  @Test
  /**
   * Tests the first order adjoint derivatives for the SABR Hagan volatility function.
   * The derivatives with respect to the forward, strike, alpha, beta, rho and nu are provided.
   */
  public void testVolatilityAdjointDebug() {
    final double eps = 1e-6;
    final double tol = 1e-5;
    testVolatilityAdjoint(F, CALL_ATM, DATA, eps, tol);
    testVolatilityAdjoint(F, CALL_ITM, DATA, eps, tol);
    testVolatilityAdjoint(F, CALL_OTM, DATA, eps, tol);
  }

  /**
   * Test small strike edge case. Vol -> infinity as strike -> 0, so the strike is floored - tested against finite difference below this
   * floor will give spurious results
   */
  @Test
  public void testVolatilityAdjointSmallStrike() {
    final double eps = 1e-10;
    final double tol = 1e-6;
    final double strike = 2e-6 * F;
    testVolatilityAdjoint(F, CALL_ATM.withStrike(strike), DATA, eps, tol);
  }

  /**
   *Test the alpha = 0 edge case. Implied vol is zero for alpha = 0, and except in the ATM case, the alpha sensitivity is infinite. We
   *choose to (arbitrarily) return 1e7 in this case.
   */
  @Test
  public void testVolatilityAdjointAlpha0() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    final SABRFormulaData data = DATA.withAlpha(0.0);
    testVolatilityAdjoint(F, CALL_ATM, data, eps, tol);

    final double volatility = FUNCTION.getVolatilityFunction(CALL_ITM, F).evaluate(data);
    final double[] volatilityAdjoint = FUNCTION.getVolatilityAdjoint(CALL_ITM, F, data);

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
    final SABRFormulaData data = DATA.withAlpha(1e-5);
    testVolatilityAdjoint(F, CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(F, CALL_ITM, data, eps, tol);
    testVolatilityAdjoint(F, CALL_OTM, data, eps, tol);
  }

  /**
   *Test the beta = 0 edge case
   */
  @Test
  public void testVolatilityAdjointBeta0() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    final SABRFormulaData data = DATA.withBeta(0.0);
    testVolatilityAdjoint(F, CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(F, CALL_ITM, data, eps, tol);
    testVolatilityAdjoint(F, CALL_OTM, data, eps, tol);
  }

  /**
   *Test the beta = 1 edge case
   */
  @Test
  public void testVolatilityAdjointBeta1() {
    final double eps = 1e-6;
    final double tol = 1e-6;
    final SABRFormulaData data = DATA.withBeta(1.0);
    testVolatilityAdjoint(F, CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(F, CALL_ITM, data, eps, tol);
    testVolatilityAdjoint(F, CALL_OTM, data, eps, tol);
  }

  /**
   *Test the nu = 0 edge case
   */
  @Test
  public void testVolatilityAdjointNu0() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    final SABRFormulaData data = DATA.withNu(0.0);
    testVolatilityAdjoint(F, CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(F, CALL_ITM, data, eps, 2e-4);
    testVolatilityAdjoint(F, CALL_OTM, data, eps, 5e-5);
  }

  /**
   *Test the rho = -1 edge case
   */
  @Test
  public void testVolatilityAdjointRhoM1() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    final SABRFormulaData data = DATA.withRho(-1.0);
    testVolatilityAdjoint(F, CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(F, CALL_ITM, data, eps, tol);
    testVolatilityAdjoint(F, CALL_OTM, data, eps, tol);
  }

  /**
   *Test the rho = 1 edge case
   */
  @Test
  public void testVolatilityAdjointRho1() {
    final double eps = 1e-4;
    final double tol = 1e-5;
    final SABRFormulaData data = DATA.withRho(1.0);
    testVolatilityAdjoint(F, CALL_ATM, data, eps, tol);
    testVolatilityAdjoint(F, CALL_ITM, data, eps, tol);
    testVolatilityAdjoint(F, CALL_OTM, data, eps, tol);
  }

  @Test
  public void testVolatilityAdjointLargeRhoZLessThan1() {
    final double eps = 1e-4;
    final double tol = 1e-5;
    final SABRFormulaData data = DATA.withRho(1.0 - 1e-9);
    testVolatilityAdjoint(F, CALL_ITM, data, eps, tol);
  }

  @Test
  public void testVolatilityAdjointLargeRhoZGreaterThan1() {
    final double eps = 1e-11;
    final double tol = 1e-4;
    final SABRFormulaData data = DATA.withRho(1.0 - 1e-9).withAlpha(0.15 * ALPHA);
    testVolatilityAdjoint(F, CALL_ITM, data, eps, tol);
  }

  @Test
  public void testVolatilityModelAdjoint() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    final SABRFormulaData data = DATA;
    testVolatilityModelAdjoint(F, CALL_ATM, data, eps, tol);
    testVolatilityModelAdjoint(F, CALL_ITM, data, eps, tol);
    testVolatilityModelAdjoint(F, CALL_OTM, data, eps, tol);
  }

  @Test
  public void testVolatilityModelAdjointRhoM1() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    final SABRFormulaData data = DATA.withRho(-1.0);
    testVolatilityModelAdjoint(F, CALL_ATM, data, eps, tol); //z=0 case
    double z = -0.975;
    double strike = strikeForZ(z, F, ALPHA, BETA, NU);
    testVolatilityModelAdjoint(F, CALL_ATM.withStrike(strike), data, eps, 5e-4);
    z = 2.0;
    strike = strikeForZ(z, F, ALPHA, BETA, NU);
    testVolatilityModelAdjoint(F, CALL_ATM.withStrike(strike), data, eps, tol);
    z = -2.0;
    strike = strikeForZ(z, F, ALPHA, BETA, NU);
    //The true rho sensitivity at rho=-1 is infinity
    testVolatilityModelAdjoint(F, CALL_ATM.withStrike(strike), DATA.withRho(-1 + 1e-3), eps, 1e-4);
    testVolatilityModelAdjoint(F, CALL_ATM.withStrike(strike), data, 1e-6, 1.5);
  }

  @Test
  public void testVolatilityModelAdjointRhoP1() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    final SABRFormulaData data = DATA.withRho(1.0);
    testVolatilityModelAdjoint(F, CALL_ATM, data, eps, tol); //z=0 case
    double z = 0.975;
    double strike = strikeForZ(z, F, ALPHA, BETA, NU);

    testVolatilityModelAdjoint(F, CALL_ATM.withStrike(strike), data, eps, 5e-2);
    z = -2.0;
    strike = strikeForZ(z, F, ALPHA, BETA, NU);
    testVolatilityModelAdjoint(F, CALL_ATM.withStrike(strike), data, eps, 50 * tol);
    z = 2.0;
    strike = strikeForZ(z, F, ALPHA, BETA, NU);
    //The true rho sensitivity at rho= 1 is -infinity
    testVolatilityModelAdjoint(F, CALL_ATM.withStrike(strike), DATA.withRho(1 - 1e-3), eps, 5e-5);
    testVolatilityModelAdjoint(F, CALL_ATM.withStrike(strike), data, 1e-6, 1.0);
  }

  @Test
  public void testVolatilityModelAdjointBeta0() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    final SABRFormulaData data = DATA.withBeta(0);
    testVolatilityModelAdjoint(F, CALL_ATM, data, eps, tol);
    testVolatilityModelAdjoint(F, CALL_ITM, data, eps, tol);
    testVolatilityModelAdjoint(F, CALL_OTM, data, eps, tol);
  }

  @Test
  public void testVolatilityModelAdjointBeta1() {
    final double eps = 1e-5;
    final double tol = 1e-6;
    final SABRFormulaData data = DATA.withBeta(1);
    testVolatilityModelAdjoint(F, CALL_ATM, data, eps, tol);
    testVolatilityModelAdjoint(F, CALL_ITM, data, eps, tol);
    testVolatilityModelAdjoint(F, CALL_OTM, data, eps, tol);
  }

  @Test
  public void testVolatilityModelAdjointNu0() {
    final double eps = 1e-6;
    final double tol = 1e-6;
    final SABRFormulaData data = DATA.withNu(0);
    testVolatilityModelAdjoint(F, CALL_ATM, data, eps, tol);
    testVolatilityModelAdjoint(F, CALL_ITM, data, eps, tol);
    testVolatilityModelAdjoint(F, CALL_OTM, data, eps, tol);
  }

  @Test
  public void testVolatilityModelAdjoinAlpha0() {

    final double eps = 1e-10;
    final double tol = 1e-2;

    double z = getZ(F, CALL_ITM.getStrike(), ALPHA, BETA, NU);
    double alpha = z / 2e8;
    SABRFormulaData data = DATA.withAlpha(alpha);
    testVolatilityModelAdjoint(F, CALL_ITM, data, eps, tol);

    z = getZ(F, CALL_OTM.getStrike(), ALPHA, BETA, NU);
    alpha = -z / 2e6;
    data = DATA.withAlpha(alpha);
    testVolatilityModelAdjoint(F, CALL_ITM, data, eps, tol);

  }

  /**
   *Test the beta = 0.0, rho = 1 edge case
   */
  @Test
  public void testVolatilityModelAdjointBeta0Rho1() {
    final double eps = 1e-4;
    final double tol = 1e-5;
    final SABRFormulaData data = DATA.withRho(1.0).withBeta(0.0).withNu(20.0);
    testVolatilityModelAdjoint(F, CALL_ATM, data, eps, tol);
    // testVolatilityModelAdjoint(FORWARD, CALL_ITM, data, eps, tol);
    testVolatilityModelAdjoint(F, CALL_OTM, data, eps, tol);
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

    final Function1D<Double, Double> func = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double strike) {
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

    final double[] brackets = bracketer.getBracketedPoints(func, l, h, forward / 20, 20 * forward);
    return rootFinder.getRoot(func, brackets[0], brackets[1]);
  }

  private void testVolatilityAdjoint(final double forward, final EuropeanVanillaOption optionData, final SABRFormulaData sabrData, final double eps, final double tol) {
    final double volatility = FUNCTION.getVolatilityFunction(optionData, forward).evaluate(sabrData);
    final double[] volatilityAdjoint = FUNCTION.getVolatilityAdjoint(optionData, forward, sabrData);

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
    final double[] volatilityAdjoint = FUNCTION.getVolatilityModelAdjoint(optionData, forward, sabrData);

    assertEqualsRelTol("Alpha Sensitivity" + sabrData.toString(), fdSensitivity(optionData, forward, sabrData, SABRParameter.Alpha, eps), volatilityAdjoint[0], tol);
    assertEqualsRelTol("Beta Sensitivity" + sabrData.toString(), fdSensitivity(optionData, forward, sabrData, SABRParameter.Beta, eps), volatilityAdjoint[1], tol);
    assertEqualsRelTol("Rho Sensitivity" + sabrData.toString(), fdSensitivity(optionData, forward, sabrData, SABRParameter.Rho, eps), volatilityAdjoint[2], tol);
    assertEqualsRelTol("Nu Sensitivity" + sabrData.toString(), fdSensitivity(optionData, forward, sabrData, SABRParameter.Nu, eps), volatilityAdjoint[3], tol);
  }

  private void assertEqualsRelTol(final String msg, final double exp, final double act, final double tol) {
    final double delta = (Math.abs(exp) + Math.abs(act)) * tol / 2.0;
    assertEquals(msg, exp, act, delta);
  }

  @Test
  /**
   * Tests the second order adjoint derivatives for the SABR Hagan volatility function. Only the derivatives with respect to the forward and the strike are provided.
   */
  public void volatilityAdjoint2() {
    volatilityAdjoint2ForInstrument(CALL_ITM, 1.0E-6, 1.0E-2);
    volatilityAdjoint2ForInstrument(CALL_ATM, 1.0E-6, 1.0E+2); // ATM the second order derivative is poor.
    volatilityAdjoint2ForInstrument(CALL_OTM, 1.0E-6, 1.0E-2);
  }

  private void volatilityAdjoint2ForInstrument(final EuropeanVanillaOption option, final double tolerance1, final double tolerance2) {
    // Price
    final double volatility = FUNCTION.getVolatilityFunction(option, F).evaluate(DATA);
    final double[] volatilityAdjoint = FUNCTION.getVolatilityAdjoint(option, F, DATA);
    final double[] volD = new double[6];
    final double[][] volD2 = new double[2][2];
    final double vol = FUNCTION.getVolatilityAdjoint2(option, F, DATA, volD, volD2);
    assertEquals("SABR Hagan: adjoint 2", volatility, vol, tolerance1);
    // Derivative
    for (int loopder = 0; loopder < 6; loopder++) {
      assertEquals("Derivative " + loopder, volatilityAdjoint[loopder + 1], volD[loopder], tolerance1);
    }
    // Derivative forward-forward
    final double deltaF = 0.000001;
    final double volatilityFP = FUNCTION.getVolatilityFunction(option, F + deltaF).evaluate(DATA);
    final double volatilityFM = FUNCTION.getVolatilityFunction(option, F - deltaF).evaluate(DATA);
    final double derivativeFF_FD = (volatilityFP + volatilityFM - 2 * volatility) / (deltaF * deltaF);
    assertEquals("SABR adjoint order 2: forward-forward", derivativeFF_FD, volD2[0][0], tolerance2);
    // Derivative strike-strike
    final double deltaK = 0.000001;
    final EuropeanVanillaOption optionKP = new EuropeanVanillaOption(option.getStrike() + deltaK, T, true);
    final EuropeanVanillaOption optionKM = new EuropeanVanillaOption(option.getStrike() - deltaK, T, true);
    final double volatilityKP = FUNCTION.getVolatilityFunction(optionKP, F).evaluate(DATA);
    final double volatilityKM = FUNCTION.getVolatilityFunction(optionKM, F).evaluate(DATA);
    final double derivativeKK_FD = (volatilityKP + volatilityKM - 2 * volatility) / (deltaK * deltaK);
    assertEquals("SABR adjoint order 2: strike-strike", derivativeKK_FD, volD2[1][1], tolerance2);
    // Derivative strike-forward
    final double volatilityFPKP = FUNCTION.getVolatilityFunction(optionKP, F + deltaF).evaluate(DATA);
    final double derivativeFK_FD = (volatilityFPKP + volatility - volatilityFP - volatilityKP) / (deltaF * deltaK);
    assertEquals("SABR adjoint order 2: forward-strike", derivativeFK_FD, volD2[0][1], tolerance2);
    assertEquals("SABR adjoint order 2: strike-forward", volD2[0][1], volD2[1][0], 1E-6);
  }

  //TODO write a fuzzer that hits SABR with random parameters
  @Test(invocationCount = 5, successPercentage = 19, singleThreaded = true, groups = TestGroup.INTEGRATION)
  public void testRandomParameters() {
    final double eps = 1e-5;
    final double tol = 1e-3;

    for (int count = 0; count < 100; count++) {
      final double alpha = Math.exp(NORMAL.nextRandom() * 0.2 - 2);
      final double beta = Math.random(); //TODO Uniform numbers in distribution
      final double nu = Math.exp(NORMAL.nextRandom() * 0.3 - 1);
      final double rho = 2 * Math.random() - 1;
      final SABRFormulaData data = new SABRFormulaData(alpha, beta, rho, nu);
      testVolatilityAdjoint(F, CALL_ATM, data, eps, tol);
      testVolatilityAdjoint(F, CALL_ITM, data, eps, tol);
      testVolatilityAdjoint(F, CALL_OTM, data, eps, tol);
    }
  }


  /**
   * Calculate the true SABR delta and gamma and compare with that found by finite difference
   */
  @Test(enabled = false)
  public void testGreeks() {
    final double eps = 1e-3;
    final double f = 1.2;
    final double k = 1.4;
    final double t = 5.0;
    final double alpha = 0.3;
    final double beta = 0.6;
    final double rho = -0.4;
    final double nu = 0.4;
    final SABRFormulaData sabrData = new SABRFormulaData(alpha, beta, rho, nu);

    final SABRHaganVolatilityFunction sabr = new SABRHaganVolatilityFunction();
    final double[] vol = sabr.getVolatilityAdjoint(new EuropeanVanillaOption(k, t, true), f, sabrData);
    final double bsDelta = BlackFormulaRepository.delta(f, k, t, vol[0], true);
    final double bsVega = BlackFormulaRepository.vega(f, k, t, vol[0]);
    final double volForwardSense = vol[1];
    final double delta = bsDelta + bsVega * volForwardSense;

    final double volUp = sabr.getVolatility(f + eps, k, t, alpha, beta, rho, nu);
    final double volDown = sabr.getVolatility(f - eps, k, t, alpha, beta, rho, nu);
    final double priceUp = BlackFormulaRepository.price(f + eps, k, t, volUp, true);
    final double price = BlackFormulaRepository.price(f, k, t, vol[0], true);
    final double priceDown = BlackFormulaRepository.price(f - eps, k, t, volDown, true);
    final double fdDelta = (priceUp - priceDown) / 2 / eps;
    assertEquals(fdDelta, delta, 1e-6);

    final double bsVanna = BlackFormulaRepository.vanna(f, k, t, vol[0]);
    final double bsGamma = BlackFormulaRepository.gamma(f, k, t, vol[0]);

    final double[] volD1 = new double[5];
    final double[][] volD2 = new double[2][2];
    sabr.getVolatilityAdjoint2(new EuropeanVanillaOption(k, t, true), f, sabrData, volD1, volD2);
    final double d2Sigmad2Fwd = volD2[0][0];
    final double gamma = bsGamma + 2 * bsVanna * vol[1] + bsVega * d2Sigmad2Fwd;
    final double fdGamma = (priceUp + priceDown - 2 * price) / eps / eps;

    final double d2Sigmad2FwdFD = (volUp + volDown - 2 * vol[0]) / eps / eps;
    assertEquals(d2Sigmad2FwdFD, d2Sigmad2Fwd, 1e-4);

    assertEquals(fdGamma, gamma, 1e-2);
  }

  /**
   * Check that $\rho \simeq 1$ case is smoothly connected with a general case, i.e., 
   * comparing the approximated computation and full computation around the cutoff, which is currently $\rho = 1.0 - 1.0e-5$
   * Note that the resulting numbers contain a large error if $\rho \simeq 1$ and $z \simeq 1$ are true at the same time
   */
  @Test
  public void largeRhoSmoothnessTest() {
    double rhoEps = 1.e-5;
    // rhoIn is larger than the cutoff, 
    // thus vol and sensitivities are computed by approximation formulas which are regular in the limit rho -> 1. 
    double rhoIn = 1.0 - 0.5 * rhoEps;
    // rhoOut is smaller than the cutoff, thus vol and sensitivities are computed by full formula. 
    double rhoOut = 1.0 - 1.5 * rhoEps;
    SABRFormulaData dataIn = new SABRFormulaData(ALPHA, BETA, rhoIn, NU);
    SABRFormulaData dataOut = new SABRFormulaData(ALPHA, BETA, rhoOut, NU);

    /*
     * z<1 case, i.e., finite values in the rho->1 limit
     */
    double volatilityOut = FUNCTION.getVolatility(CALL_OTM, F, dataOut);
    double[] adjointOut = FUNCTION.getVolatilityAdjoint(CALL_OTM, F, dataOut);
    double[] adjointModelOut = FUNCTION.getVolatilityModelAdjoint(CALL_OTM, F, dataOut);
    double[] volatilityDOut = new double[6];
    double[][] volatilityD2Out = new double[2][2];
    double volatility2Out = FUNCTION.getVolatilityAdjoint2(CALL_OTM, F, dataOut, volatilityDOut, volatilityD2Out);

    double volatilityIn = FUNCTION.getVolatility(CALL_OTM, F, dataIn);
    double[] adjointIn = FUNCTION.getVolatilityAdjoint(CALL_OTM, F, dataIn);
    double[] adjointModelIn = FUNCTION.getVolatilityModelAdjoint(CALL_OTM, F, dataIn);
    double[] volatilityDIn = new double[6];
    double[][] volatilityD2In = new double[2][2];
    double volatility2In = FUNCTION.getVolatilityAdjoint2(CALL_OTM, F, dataIn, volatilityDIn, volatilityD2In);

    assertEquals(volatilityOut, volatilityIn, rhoEps);
    assertEquals(volatility2Out, volatility2In, rhoEps);
    for (int i = 0; i < adjointOut.length; ++i) {
      double ref = adjointOut[i];
      assertEquals(adjointOut[i], adjointIn[i], Math.max(Math.abs(ref), 1.0) * 1.e-3);
    }
    for (int i = 0; i < adjointModelOut.length; ++i) {
      double ref = adjointModelOut[i];
      assertEquals(adjointModelOut[i], adjointModelIn[i], Math.max(Math.abs(ref), 1.0) * 1.e-3);
    }
    for (int i = 0; i < volatilityDOut.length; ++i) {
      double ref = volatilityDOut[i];
      assertEquals(volatilityDOut[i], volatilityDIn[i], Math.max(Math.abs(ref), 1.0) * 1.e-3);
    }

    /*
     * z>1 case, runs into infinity or 0. 
     * Convergence speed is much faster (and typically smoother).
     */
    rhoIn = 1.0 - 0.999 * rhoEps;
    rhoOut = 1.0 - 1.001 * rhoEps;
    dataIn = new SABRFormulaData(ALPHA, BETA, rhoIn, NU);
    dataOut = new SABRFormulaData(ALPHA, BETA, rhoOut, NU);

    volatilityOut = FUNCTION.getVolatility(CALL_ITM, 3.0 * F, dataOut);
    adjointOut = FUNCTION.getVolatilityAdjoint(CALL_ITM, 3.0 * F, dataOut);
    adjointModelOut = FUNCTION.getVolatilityModelAdjoint(CALL_ITM, 3.0 * F, dataOut);
    volatilityDOut = new double[6];
    volatilityD2Out = new double[2][2];
    volatility2Out = FUNCTION.getVolatilityAdjoint2(CALL_ITM, 3.0 * F, dataOut, volatilityDOut, volatilityD2Out);

    volatilityIn = FUNCTION.getVolatility(CALL_ITM, 3.0 * F, dataIn);
    adjointIn = FUNCTION.getVolatilityAdjoint(CALL_ITM, 3.0 * F, dataIn);
    adjointModelIn = FUNCTION.getVolatilityModelAdjoint(CALL_ITM, 3.0 * F, dataIn);
    volatilityDIn = new double[6];
    volatilityD2In = new double[2][2];
    volatility2In = FUNCTION.getVolatilityAdjoint2(CALL_ITM, 3.0 * F, dataIn, volatilityDIn, volatilityD2In);

    assertEquals(volatilityOut, volatilityIn, rhoEps);
    assertEquals(volatility2Out, volatility2In, rhoEps);
    for (int i = 0; i < adjointOut.length; ++i) {
      double ref = adjointOut[i];
      assertEquals(ref, adjointIn[i], Math.max(Math.abs(ref), 1.0) * 1.e-2);
    }
    for (int i = 0; i < adjointModelOut.length; ++i) {
      double ref = adjointModelOut[i];
      assertEquals(ref, adjointModelIn[i], Math.max(Math.abs(ref), 1.0) * 1.e-2);
    }
    for (int i = 0; i < volatilityDOut.length; ++i) {
      double ref = volatilityDOut[i];
      assertEquals(ref, volatilityDIn[i], Math.max(Math.abs(ref), 1.0) * 1.e-2);
    }
  }

  private enum SABRParameter {
    Forward, Strike, Alpha, Beta, Nu, Rho
  }

  private double fdSensitivity(final EuropeanVanillaOption optionData, final double forward, final SABRFormulaData sabrData, final SABRParameter param, final double delta) {

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
        final double strike = optionData.getStrike();
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
        final double a = sabrData.getAlpha();
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
        final double b = sabrData.getBeta();
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
        final double n = sabrData.getNu();
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
        final double r = sabrData.getRho();
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
      default:
        throw new MathException("enum not found");
    }

    if (fdType != null) {
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
    throw new MathException("enum not found");
  }
}
