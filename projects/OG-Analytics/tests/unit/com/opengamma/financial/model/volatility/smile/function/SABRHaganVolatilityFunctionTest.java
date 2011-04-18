/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * Tests related to the Hagan et al. approximation of the SABR implied volatility.
 */
public class SABRHaganVolatilityFunctionTest extends SABRVolatilityFunctionTestCase {

  private static final SABRHaganVolatilityFunction FUNCTION = new SABRHaganVolatilityFunction();

  private static final double ALPHA = 0.05;
  private static final double BETA = 0.50;
  private static final double RHO = -0.25;
  private static final double NU = 0.4;
  private static final double FORWARD = 0.05;
  private static final SABRFormulaData DATA = new SABRFormulaData(FORWARD, ALPHA, BETA, NU, RHO);
  private static final double T = 4.5;
  private static final double STRIKE = 0.0450;
//  private static final double STRIKE_0 = 0.00;
  private static final EuropeanVanillaOption CALL_ATM = new EuropeanVanillaOption(FORWARD, T, true);
  private static final EuropeanVanillaOption CALL_STRIKE = new EuropeanVanillaOption(STRIKE, T, true);
//  private static final EuropeanVanillaOption CALL_STRIKE_0 = new EuropeanVanillaOption(STRIKE_0, T, true);

  @Override
  protected VolatilityFunctionProvider<SABRFormulaData> getFunction() {
    return FUNCTION;
  }

  @Test
  public void testATMSmoothness() {
    // Test if the Hagan volatility function implementation around ATM is numerically stable enough (the finite difference slope should be small enough).
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
      assertEquals(true, Math.abs(sabrVolatilty[looppts + nbPoints + 1] - sabrVolatilty[looppts + nbPoints]) / (strike[looppts + nbPoints + 1] - strike[looppts + nbPoints]) < 20.0);
    }

  }

  @Test
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
  public void testVolatilityAdjoint0() {
    // Price
    double volatility = FUNCTION.getVolatilityFunction(CALL_STRIKE_0).evaluate(DATA);
    double[] volatilityAdjoint = FUNCTION.getVolatilityAdjoint(CALL_STRIKE_0, DATA);
    assertEquals(volatility, volatilityAdjoint[0], 1E-6);
  }
}
