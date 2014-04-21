/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Tests of the SABR valuation of options with extrapolation on the right (for high strikes). The SABR pricing is through Black formula with implied volatility.
 */
@Test(groups = TestGroup.UNIT)
public class SABRExtrapolationRightFunctionTest {
  // Data
  private static final double NU = 0.50;
  private static final double RHO = -0.25;
  private static final double BETA = 0.50;
  private static final double ALPHA = 0.05;
  private static final double FORWARD = 0.05;
  private static final SABRFormulaData SABR_DATA = new SABRFormulaData(ALPHA, BETA, RHO, NU);
  private static final double CUT_OFF_STRIKE = 0.10; // Set low for the test
  private static final double MU = 4.0;
  private static final double TIME_TO_EXPIRY = 2.0;
  private static final SABRExtrapolationRightFunction SABR_EXTRAPOLATION = new SABRExtrapolationRightFunction(FORWARD, SABR_DATA, CUT_OFF_STRIKE, TIME_TO_EXPIRY, MU);
  // Function
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();

  private static final double TOLERANCE_PRICE = 1.0E-10;

  @Test
  /**
   * Tests the price for options in SABR model with extrapolation.
   */
  public void price() {
    double strikeIn = 0.08;
    double strikeAt = CUT_OFF_STRIKE;
    double strikeOut = 0.12;
    EuropeanVanillaOption optionIn = new EuropeanVanillaOption(strikeIn, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionAt = new EuropeanVanillaOption(strikeAt, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionOut = new EuropeanVanillaOption(strikeOut, TIME_TO_EXPIRY, true);
    Function1D<SABRFormulaData, Double> funcSabrIn = SABR_FUNCTION.getVolatilityFunction(optionIn, FORWARD);
    double volatilityIn = funcSabrIn.evaluate(SABR_DATA);
    BlackFunctionData dataBlackIn = new BlackFunctionData(FORWARD, 1.0, volatilityIn);
    Function1D<BlackFunctionData, Double> funcBlackIn = BLACK_FUNCTION.getPriceFunction(optionIn);
    double priceExpectedIn = funcBlackIn.evaluate(dataBlackIn);
    double priceIn = SABR_EXTRAPOLATION.price(optionIn);
    assertEquals("SABR extrapolation, below cut-off", priceExpectedIn, priceIn, 1E-10);
    Function1D<SABRFormulaData, Double> funcSabrAt = SABR_FUNCTION.getVolatilityFunction(optionAt, FORWARD);
    double volatilityAt = funcSabrAt.evaluate(SABR_DATA);
    BlackFunctionData dataBlackAt = new BlackFunctionData(FORWARD, 1.0, volatilityAt);
    Function1D<BlackFunctionData, Double> funcBlackAt = BLACK_FUNCTION.getPriceFunction(optionAt);
    double priceExpectedAt = funcBlackAt.evaluate(dataBlackAt);
    double priceAt = SABR_EXTRAPOLATION.price(optionAt);
    assertEquals("SABR extrapolation, at cut-off", priceExpectedAt, priceAt, 1E-10);
    double priceOut = SABR_EXTRAPOLATION.price(optionOut);
    double priceExpectedOut = 5.427104E-5; // From previous run
    assertEquals("SABR extrapolation, above cut-off", priceExpectedOut, priceOut, 1E-10);
  }

  @Test
  /**
   * Tests the price for options in SABR model with extrapolation.
   */
  public void priceCloseToExpiry() {
    double[] timeToExpiry = {1.0 / 365, 0.0}; // One day and on expiry day.
    double strikeIn = 0.08;
    double strikeAt = CUT_OFF_STRIKE;
    double strikeOut = 0.12;
    for (int loopexp = 0; loopexp < timeToExpiry.length; loopexp++) {
      SABRExtrapolationRightFunction sabrExtra = new SABRExtrapolationRightFunction(FORWARD, SABR_DATA, CUT_OFF_STRIKE, timeToExpiry[loopexp], MU);
      EuropeanVanillaOption optionIn = new EuropeanVanillaOption(strikeIn, timeToExpiry[loopexp], true);
      EuropeanVanillaOption optionAt = new EuropeanVanillaOption(strikeAt, timeToExpiry[loopexp], true);
      EuropeanVanillaOption optionOut = new EuropeanVanillaOption(strikeOut, timeToExpiry[loopexp], true);
      Function1D<SABRFormulaData, Double> funcSabrIn = SABR_FUNCTION.getVolatilityFunction(optionIn, FORWARD);
      double volatilityIn = funcSabrIn.evaluate(SABR_DATA);
      BlackFunctionData dataBlackIn = new BlackFunctionData(FORWARD, 1.0, volatilityIn);
      Function1D<BlackFunctionData, Double> funcBlackIn = BLACK_FUNCTION.getPriceFunction(optionIn);
      double priceExpectedIn = funcBlackIn.evaluate(dataBlackIn);
      double priceIn = sabrExtra.price(optionIn);
      assertEquals("SABR extrapolation, below cut-off", priceExpectedIn, priceIn, TOLERANCE_PRICE);
      Function1D<SABRFormulaData, Double> funcSabrAt = SABR_FUNCTION.getVolatilityFunction(optionAt, FORWARD);
      double volatilityAt = funcSabrAt.evaluate(SABR_DATA);
      BlackFunctionData dataBlackAt = new BlackFunctionData(FORWARD, 1.0, volatilityAt);
      Function1D<BlackFunctionData, Double> funcBlackAt = BLACK_FUNCTION.getPriceFunction(optionAt);
      double priceExpectedAt = funcBlackAt.evaluate(dataBlackAt);
      double priceAt = sabrExtra.price(optionAt);
      assertEquals("SABR extrapolation, at cut-off", priceExpectedAt, priceAt, TOLERANCE_PRICE);
      double priceOut = sabrExtra.price(optionOut);
      double priceExpectedOut = 0.0; // From previous run
      assertEquals("SABR extrapolation, above cut-off", priceExpectedOut, priceOut, TOLERANCE_PRICE);
    }
  }

  @Test
  /**
   * Tests the price derivative with respect to forward for options in SABR model with extrapolation.
   */
  public void priceDerivativeForward() {
    double strikeIn = 0.08;
    double strikeAt = CUT_OFF_STRIKE;
    double strikeOut = 0.12;
    EuropeanVanillaOption optionIn = new EuropeanVanillaOption(strikeIn, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionAt = new EuropeanVanillaOption(strikeAt, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionOut = new EuropeanVanillaOption(strikeOut, TIME_TO_EXPIRY, true);
    double shiftF = 0.000001;
    SABRFormulaData sabrDataFP = new SABRFormulaData(ALPHA, BETA, RHO, NU);
    SABRExtrapolationRightFunction sabrExtrapolationFP = new SABRExtrapolationRightFunction(FORWARD + shiftF, sabrDataFP, CUT_OFF_STRIKE, TIME_TO_EXPIRY, MU);
    // Below cut-off strike
    double priceIn = SABR_EXTRAPOLATION.price(optionIn);
    double priceInFP = sabrExtrapolationFP.price(optionIn);
    double priceInDF = SABR_EXTRAPOLATION.priceDerivativeForward(optionIn);
    double priceInDFExpected = (priceInFP - priceIn) / shiftF;
    assertEquals("SABR extrapolation: derivative with respect to forward, below cut-off", priceInDFExpected, priceInDF, 1E-5);
    // At cut-off strike
    double priceAt = SABR_EXTRAPOLATION.price(optionAt);
    double priceAtFP = sabrExtrapolationFP.price(optionAt);
    double priceAtDF = SABR_EXTRAPOLATION.priceDerivativeForward(optionAt);
    double priceAtDFExpected = (priceAtFP - priceAt) / shiftF;
    assertEquals("SABR extrapolation: derivative with respect to forward, at cut-off", priceAtDFExpected, priceAtDF, 1E-6);
    // Above cut-off strike
    double[] abc = SABR_EXTRAPOLATION.getParameter();
    double[] abcDF = SABR_EXTRAPOLATION.getParameterDerivativeForward();
    double[] abcFP = sabrExtrapolationFP.getParameter();
    double[] abcDFExpected = new double[3];
    for (int loopparam = 0; loopparam < 3; loopparam++) {
      abcDFExpected[loopparam] = (abcFP[loopparam] - abc[loopparam]) / shiftF;
      assertEquals("SABR extrapolation: parameters derivative " + loopparam, 1.0, abcDFExpected[loopparam] / abcDF[loopparam], 5E-2);
    }
    double priceOut = SABR_EXTRAPOLATION.price(optionOut);
    double priceOutFP = sabrExtrapolationFP.price(optionOut);
    double priceOutDF = SABR_EXTRAPOLATION.priceDerivativeForward(optionOut);
    double priceOutDFExpected = (priceOutFP - priceOut) / shiftF;
    assertEquals("SABR extrapolation: derivative with respect to forward, above cut-off", priceOutDFExpected, priceOutDF, 1E-5);
  }

  @Test
  /**
   * Tests the price derivative with respect to forward for options in SABR model with extrapolation.
   */
  public void priceDerivativeStrike() {
    double strikeIn = 0.08;
    double strikeAt = CUT_OFF_STRIKE;
    double strikeOut = 0.12;
    double shiftK = 0.000001;
    EuropeanVanillaOption optionIn = new EuropeanVanillaOption(strikeIn, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionAt = new EuropeanVanillaOption(strikeAt, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionOut = new EuropeanVanillaOption(strikeOut, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionInKP = new EuropeanVanillaOption(strikeIn + shiftK, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionAtKP = new EuropeanVanillaOption(strikeAt + shiftK, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionOutKP = new EuropeanVanillaOption(strikeOut + shiftK, TIME_TO_EXPIRY, true);
    // Below cut-off strike
    double priceIn = SABR_EXTRAPOLATION.price(optionIn);
    double priceInKP = SABR_EXTRAPOLATION.price(optionInKP);
    double priceInDK = SABR_EXTRAPOLATION.priceDerivativeStrike(optionIn);
    double priceInDFExpected = (priceInKP - priceIn) / shiftK;
    assertEquals("SABR extrapolation: derivative with respect to strike, below cut-off", priceInDFExpected, priceInDK, 1E-5);
    // At cut-off strike
    double priceAt = SABR_EXTRAPOLATION.price(optionAt);
    double priceAtKP = SABR_EXTRAPOLATION.price(optionAtKP);
    double priceAtDK = SABR_EXTRAPOLATION.priceDerivativeStrike(optionAt);
    double priceAtDFExpected = (priceAtKP - priceAt) / shiftK;
    assertEquals("SABR extrapolation: derivative with respect to strike, at cut-off", priceAtDFExpected, priceAtDK, 1E-5);
    // At cut-off strike
    double priceOut = SABR_EXTRAPOLATION.price(optionOut);
    double priceOutKP = SABR_EXTRAPOLATION.price(optionOutKP);
    double priceOutDK = SABR_EXTRAPOLATION.priceDerivativeStrike(optionOut);
    double priceOutDFExpected = (priceOutKP - priceOut) / shiftK;
    assertEquals("SABR extrapolation: derivative with respect to strike, above cut-off", priceOutDFExpected, priceOutDK, 1E-5);
  }

  @Test
  /**
   * Tests the price derivative with respect to forward for options in SABR model with extrapolation.
   */
  public void priceDerivativeSABR() {
    double strikeIn = 0.08;
    double strikeAt = CUT_OFF_STRIKE;
    double strikeOut = 0.12;
    EuropeanVanillaOption optionIn = new EuropeanVanillaOption(strikeIn, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionAt = new EuropeanVanillaOption(strikeAt, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionOut = new EuropeanVanillaOption(strikeOut, TIME_TO_EXPIRY, true);
    double shift = 0.000001;
    SABRFormulaData sabrDataAP = new SABRFormulaData(ALPHA + shift, BETA, RHO, NU);
    SABRFormulaData sabrDataBP = new SABRFormulaData(ALPHA, BETA + shift, RHO, NU);
    SABRFormulaData sabrDataRP = new SABRFormulaData(ALPHA, BETA, RHO + shift, NU);
    SABRFormulaData sabrDataNP = new SABRFormulaData(ALPHA, BETA, RHO, NU + shift);
    SABRExtrapolationRightFunction sabrExtrapolationAP = new SABRExtrapolationRightFunction(FORWARD, sabrDataAP, CUT_OFF_STRIKE, TIME_TO_EXPIRY, MU);
    SABRExtrapolationRightFunction sabrExtrapolationBP = new SABRExtrapolationRightFunction(FORWARD, sabrDataBP, CUT_OFF_STRIKE, TIME_TO_EXPIRY, MU);
    SABRExtrapolationRightFunction sabrExtrapolationRP = new SABRExtrapolationRightFunction(FORWARD, sabrDataRP, CUT_OFF_STRIKE, TIME_TO_EXPIRY, MU);
    SABRExtrapolationRightFunction sabrExtrapolationNP = new SABRExtrapolationRightFunction(FORWARD, sabrDataNP, CUT_OFF_STRIKE, TIME_TO_EXPIRY, MU);
    // Below cut-off strike
    double priceInExpected = SABR_EXTRAPOLATION.price(optionIn);
    double[] priceInPP = new double[4];
    priceInPP[0] = sabrExtrapolationAP.price(optionIn);
    priceInPP[1] = sabrExtrapolationBP.price(optionIn);
    priceInPP[2] = sabrExtrapolationRP.price(optionIn);
    priceInPP[3] = sabrExtrapolationNP.price(optionIn);
    double[] priceInDsabr = new double[4];
    double priceIn = SABR_EXTRAPOLATION.priceAdjointSABR(optionIn, priceInDsabr);
    assertEquals("SABR extrapolation below cut-off: price in adjoint", priceInExpected, priceIn, 1E-5);
    double[] priceInDsabrExpected = new double[4];
    for (int loopparam = 0; loopparam < 3; loopparam++) {
      priceInDsabrExpected[loopparam] = (priceInPP[loopparam] - priceIn) / shift;
      assertEquals("SABR extrapolation below cut-off: derivative with respect to SABR parameter " + loopparam, priceInDsabrExpected[loopparam], priceInDsabr[loopparam], 1E-5);
    }
    // At cut-off strike
    double priceAtExpected = SABR_EXTRAPOLATION.price(optionAt);
    double[] priceAtPP = new double[4];
    priceAtPP[0] = sabrExtrapolationAP.price(optionAt);
    priceAtPP[1] = sabrExtrapolationBP.price(optionAt);
    priceAtPP[2] = sabrExtrapolationRP.price(optionAt);
    priceAtPP[3] = sabrExtrapolationNP.price(optionAt);
    double[] priceAtDsabr = new double[4];
    double priceAt = SABR_EXTRAPOLATION.priceAdjointSABR(optionAt, priceAtDsabr);
    assertEquals("SABR extrapolation at cut-off: price in adjoint", priceAtExpected, priceAt, 1E-5);
    double[] priceAtDsabrExpected = new double[4];
    for (int loopparam = 0; loopparam < 3; loopparam++) {
      priceAtDsabrExpected[loopparam] = (priceAtPP[loopparam] - priceAt) / shift;
      assertEquals("SABR extrapolation at cut-off: derivative with respect to SABR parameter " + loopparam, priceAtDsabrExpected[loopparam], priceAtDsabr[loopparam], 1E-5);
    }
    // Above cut-off strike
    double[] abc = SABR_EXTRAPOLATION.getParameter();
    double[][] abcDP = SABR_EXTRAPOLATION.getParameterDerivativeSABR();
    double[][] abcPP = new double[4][3];
    abcPP[0] = sabrExtrapolationAP.getParameter();
    abcPP[1] = sabrExtrapolationBP.getParameter();
    abcPP[2] = sabrExtrapolationRP.getParameter();
    abcPP[3] = sabrExtrapolationNP.getParameter();
    double[][] abcDPExpected = new double[4][3];
    for (int loopparam = 0; loopparam < 4; loopparam++) {
      for (int loopabc = 0; loopabc < 3; loopabc++) {
        abcDPExpected[loopparam][loopabc] = (abcPP[loopparam][loopabc] - abc[loopabc]) / shift;
        assertEquals("SABR extrapolation: parameters derivative " + loopparam + " / " + loopabc, 1.0, abcDPExpected[loopparam][loopabc] / abcDP[loopparam][loopabc], 5.0E-2);
      }
    }
    double priceOutExpected = SABR_EXTRAPOLATION.price(optionOut);
    double[] priceOutPP = new double[4];
    priceOutPP[0] = sabrExtrapolationAP.price(optionOut);
    priceOutPP[1] = sabrExtrapolationBP.price(optionOut);
    priceOutPP[2] = sabrExtrapolationRP.price(optionOut);
    priceOutPP[3] = sabrExtrapolationNP.price(optionOut);
    double[] priceOutDsabr = new double[4];
    double priceOut = SABR_EXTRAPOLATION.priceAdjointSABR(optionOut, priceOutDsabr);
    assertEquals("SABR extrapolation above cut-off: price in adjoint", priceOutExpected, priceOut, 1E-5);
    double[] priceOutDsabrExpected = new double[4];
    for (int loopparam = 0; loopparam < 4; loopparam++) {
      priceOutDsabrExpected[loopparam] = (priceOutPP[loopparam] - priceOut) / shift;
      assertEquals("SABR extrapolation above cut-off: derivative with respect to SABR parameter " + loopparam, 1.0, priceOutDsabrExpected[loopparam] / priceOutDsabr[loopparam], 4.0E-4);
    }
  }

  @Test
  /**
   * Tests the price derivative with respect to forward for options in SABR model with extrapolation. Other data.
   */
  public void priceDerivativeSABR2() {
    double alpha = 0.06;
    double beta = 0.5;
    double rho = 0.0;
    double nu = 0.3;
    double cutOff = 0.10;
    double mu = 2.5;
    double strike = 0.15;
    double t = 2.366105247;
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, t, true);
    SABRFormulaData sabrData = new SABRFormulaData(alpha, beta, rho, nu);
    double forward = 0.0404500579038675;
    SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrData, cutOff, t, mu);
    double shift = 0.000001;
    SABRFormulaData sabrDataAP = new SABRFormulaData(alpha + shift, beta, rho, nu);
    SABRFormulaData sabrDataBP = new SABRFormulaData(alpha, beta + shift, rho, nu);
    SABRFormulaData sabrDataRP = new SABRFormulaData(alpha, beta, rho + shift, nu);
    SABRFormulaData sabrDataNP = new SABRFormulaData(alpha, beta, rho, nu + shift);
    SABRExtrapolationRightFunction sabrExtrapolationAP = new SABRExtrapolationRightFunction(forward, sabrDataAP, cutOff, t, mu);
    SABRExtrapolationRightFunction sabrExtrapolationBP = new SABRExtrapolationRightFunction(forward, sabrDataBP, cutOff, t, mu);
    SABRExtrapolationRightFunction sabrExtrapolationRP = new SABRExtrapolationRightFunction(forward, sabrDataRP, cutOff, t, mu);
    SABRExtrapolationRightFunction sabrExtrapolationNP = new SABRExtrapolationRightFunction(forward, sabrDataNP, cutOff, t, mu);
    // Above cut-off strike
    double[] abc = sabrExtrapolation.getParameter();
    double[][] abcDP = sabrExtrapolation.getParameterDerivativeSABR();
    double[][] abcPP = new double[4][3];
    abcPP[0] = sabrExtrapolationAP.getParameter();
    abcPP[1] = sabrExtrapolationBP.getParameter();
    abcPP[2] = sabrExtrapolationRP.getParameter();
    abcPP[3] = sabrExtrapolationNP.getParameter();
    double[][] abcDPExpected = new double[4][3];
    for (int loopparam = 0; loopparam < 4; loopparam++) {
      for (int loopabc = 0; loopabc < 3; loopabc++) {
        abcDPExpected[loopparam][loopabc] = (abcPP[loopparam][loopabc] - abc[loopabc]) / shift;
        assertEquals("SABR extrapolation: parameters derivative " + loopparam + " / " + loopabc, 1.0, abcDPExpected[loopparam][loopabc] / abcDP[loopparam][loopabc], 5.0E-2);
      }
    }
    double priceOutExpected = sabrExtrapolation.price(option);
    double[] priceOutPP = new double[4];
    priceOutPP[0] = sabrExtrapolationAP.price(option);
    priceOutPP[1] = sabrExtrapolationBP.price(option);
    priceOutPP[2] = sabrExtrapolationRP.price(option);
    priceOutPP[3] = sabrExtrapolationNP.price(option);
    double[] priceOutDsabr = new double[4];
    double priceOut = sabrExtrapolation.priceAdjointSABR(option, priceOutDsabr);
    assertEquals("SABR extrapolation above cut-off: price in adjoint", priceOutExpected, priceOut, 1E-5);
    double[] priceOutDsabrExpected = new double[4];
    for (int loopparam = 0; loopparam < 4; loopparam++) {
      priceOutDsabrExpected[loopparam] = (priceOutPP[loopparam] - priceOut) / shift;
      assertEquals("SABR extrapolation above cut-off: derivative with respect to SABR parameter " + loopparam, 1.0, priceOutDsabrExpected[loopparam] / priceOutDsabr[loopparam], 4.0E-4);
    }
  }

  @Test
  /**
   * Tests the price put/call parity for options in SABR model with extrapolation.
   */
  public void pricePutCallParity() {
    double strikeIn = 0.08;
    double strikeAt = CUT_OFF_STRIKE;
    double strikeOut = 0.12;
    EuropeanVanillaOption callIn = new EuropeanVanillaOption(strikeIn, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption putIn = new EuropeanVanillaOption(strikeIn, TIME_TO_EXPIRY, false);
    EuropeanVanillaOption callAt = new EuropeanVanillaOption(strikeAt, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption putAt = new EuropeanVanillaOption(strikeAt, TIME_TO_EXPIRY, false);
    EuropeanVanillaOption callOut = new EuropeanVanillaOption(strikeOut, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption putOut = new EuropeanVanillaOption(strikeOut, TIME_TO_EXPIRY, false);
    double priceCallIn = SABR_EXTRAPOLATION.price(callIn);
    double pricePutIn = SABR_EXTRAPOLATION.price(putIn);
    assertEquals("SABR extrapolation, below cut-off: put/call parity", FORWARD - strikeIn, priceCallIn - pricePutIn, 1E-10);
    double priceCallAt = SABR_EXTRAPOLATION.price(callAt);
    double pricePutAt = SABR_EXTRAPOLATION.price(putAt);
    assertEquals("SABR extrapolation, at cut-off: put/call parity", FORWARD - strikeAt, priceCallAt - pricePutAt, 1E-10);
    double priceCallOut = SABR_EXTRAPOLATION.price(callOut);
    double pricePutOut = SABR_EXTRAPOLATION.price(putOut);
    assertEquals("SABR extrapolation, above cut-off: put/call parity", FORWARD - strikeOut, priceCallOut - pricePutOut, 1E-10);
  }

  @Test
  /**
   * Tests that the smile and its derivatives are smooth enough in SABR model with extrapolation.
   */
  public void smileSmooth() {
    int nbPts = 100;
    double rangeStrike = 0.02;
    double[] price = new double[nbPts + 1];
    double[] strike = new double[nbPts + 1];
    for (int looppts = 0; looppts <= nbPts; looppts++) {
      strike[looppts] = CUT_OFF_STRIKE - rangeStrike + looppts * 2.0 * rangeStrike / nbPts;
      EuropeanVanillaOption option = new EuropeanVanillaOption(strike[looppts], TIME_TO_EXPIRY, true);
      price[looppts] = SABR_EXTRAPOLATION.price(option);
    }
    double[] priceD = new double[nbPts];
    double[] priceD2 = new double[nbPts];
    for (int looppts = 1; looppts < nbPts; looppts++) {
      priceD[looppts] = (price[looppts + 1] - price[looppts - 1]) / (strike[looppts + 1] - strike[looppts - 1]);
      priceD2[looppts] = (price[looppts + 1] + price[looppts - 1] - 2 * price[looppts]) / ((strike[looppts + 1] - strike[looppts]) * (strike[looppts + 1] - strike[looppts]));
    }
    for (int looppts = 2; looppts < nbPts; looppts++) {
      assertEquals("SABR extrapolation, smooth first derivative", priceD[looppts - 1], priceD[looppts], 1.5E-3);
      assertEquals("SABR extrapolation, smooth second derivative", priceD2[looppts - 1], priceD2[looppts], 1.5E-1);
    }
  }

  @Test
  /**
   * Tests that the smile and its derivatives are smooth enough in SABR model with extrapolation for different time to maturity (in particular close to maturity).
   */
  public void smileSmoothMaturity() {
    int nbPts = 100;
    double[] timeToExpiry = new double[] {2.0, 1.0, 0.50, 0.25, 1.0d / 12.0d, 1.0d / 52.0d, 1.0d / 365d};
    int nbTTM = timeToExpiry.length;
    double rangeStrike = 0.02;
    double[] strike = new double[nbPts + 1];
    for (int looppts = 0; looppts <= nbPts; looppts++) {
      strike[looppts] = CUT_OFF_STRIKE - rangeStrike + looppts * 2.0 * rangeStrike / nbPts;
    }
    SABRExtrapolationRightFunction[] sabrExtrapolation = new SABRExtrapolationRightFunction[nbTTM];
    for (int loopmat = 0; loopmat < nbTTM; loopmat++) {
      sabrExtrapolation[loopmat] = new SABRExtrapolationRightFunction(FORWARD, SABR_DATA, CUT_OFF_STRIKE, timeToExpiry[loopmat], MU);
    }
    double[][] price = new double[nbTTM][nbPts + 1];
    for (int loopmat = 0; loopmat < nbTTM; loopmat++) {
      for (int looppts = 0; looppts <= nbPts; looppts++) {
        EuropeanVanillaOption option = new EuropeanVanillaOption(strike[looppts], timeToExpiry[loopmat], true);
        price[loopmat][looppts] = sabrExtrapolation[loopmat].price(option);
      }
    }
    double[][] priceD = new double[nbTTM][nbPts - 1];
    double[][] priceD2 = new double[nbTTM][nbPts - 1];
    for (int loopmat = 0; loopmat < nbTTM; loopmat++) {
      for (int looppts = 1; looppts < nbPts; looppts++) {
        priceD[loopmat][looppts - 1] = (price[loopmat][looppts + 1] - price[loopmat][looppts - 1]) / (strike[looppts + 1] - strike[looppts - 1]);
        priceD2[loopmat][looppts - 1] = (price[loopmat][looppts + 1] + price[loopmat][looppts - 1] - 2 * price[loopmat][looppts])
            / ((strike[looppts + 1] - strike[looppts]) * (strike[looppts + 1] - strike[looppts]));
      }
    }
    double epsDensity = 1.0E-20; // Conditions are not checked when the density is very small.
    for (int loopmat = 0; loopmat < nbTTM; loopmat++) {
      for (int looppts = 1; looppts < nbPts - 1; looppts++) {
        assertTrue("SABR extrapolation, smooth first derivative - mat " + loopmat + " / pt " + looppts + " [" + priceD[loopmat][looppts] + "/" + priceD[loopmat][looppts - 1] + "]",
            ((priceD[loopmat][looppts] / priceD[loopmat][looppts - 1] < 1) && (priceD[loopmat][looppts] / priceD[loopmat][looppts - 1] > 0.50)) || Math.abs(priceD2[loopmat][looppts]) < epsDensity);
        assertTrue("SABR extrapolation, positive second derivative - mat " + loopmat + " / pt " + looppts + " [" + priceD2[loopmat][looppts] + "]",
            priceD2[loopmat][looppts] > 0 || Math.abs(priceD2[loopmat][looppts]) < epsDensity);
        assertTrue("SABR extrapolation, smooth second derivative - mat " + loopmat + " / pt " + looppts + " [" + priceD2[loopmat][looppts] + "/" + priceD2[loopmat][looppts - 1] + "]",
            (priceD2[loopmat][looppts] / priceD2[loopmat][looppts - 1] < 1 && priceD2[loopmat][looppts] / priceD2[loopmat][looppts - 1] > 0.50) || Math.abs(priceD2[loopmat][looppts]) < epsDensity);
      }
    }
  }

  @Test(enabled = false)
  /**
   * To graph the smile for different tail parameters.
   */
  public void smileMultiMu() {
    double[] mu = new double[] {5.0, 40.0, 90.0, 150.0};
    int nbMu = mu.length;
    int nbPts = 100;
    double rangeStrike = 0.02;
    double[] strike = new double[nbPts + 1];
    double[][] price = new double[nbMu][nbPts + 1];
    double[][] impliedVolatility = new double[nbMu][nbPts + 1];
    BlackImpliedVolatilityFormula implied = new BlackImpliedVolatilityFormula();
    BlackFunctionData blackData = new BlackFunctionData(FORWARD, 1.0, 0.0);
    for (int loopmu = 0; loopmu < nbMu; loopmu++) {
      SABRExtrapolationRightFunction sabrExtra = new SABRExtrapolationRightFunction(FORWARD, SABR_DATA, CUT_OFF_STRIKE, TIME_TO_EXPIRY, mu[loopmu]);
      for (int looppts = 0; looppts <= nbPts; looppts++) {
        strike[looppts] = CUT_OFF_STRIKE - rangeStrike + looppts * 4.0 * rangeStrike / nbPts;
        EuropeanVanillaOption option = new EuropeanVanillaOption(strike[looppts], TIME_TO_EXPIRY, true);
        price[loopmu][looppts] = sabrExtra.price(option);
        impliedVolatility[loopmu][looppts] = implied.getImpliedVolatility(blackData, option, price[loopmu][looppts]);
      }
    }
  }
}
