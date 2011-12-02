/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.math.function.Function1D;

/**
 * Tests of the SABR valuation of options with extrapolation on the right (for high strikes). The SABR pricing is through Black formula with implied volatility.
 */
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
  BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();

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
  public void priceDerivativeSABR() {
    double strikeIn = 0.08;
    double strikeAt = CUT_OFF_STRIKE;
    double strikeOut = 0.12;
    EuropeanVanillaOption optionIn = new EuropeanVanillaOption(strikeIn, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionAt = new EuropeanVanillaOption(strikeAt, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionOut = new EuropeanVanillaOption(strikeOut, TIME_TO_EXPIRY, true);
    double shift = 0.000001;
    SABRFormulaData sabrDataAP = new SABRFormulaData(ALPHA + shift, BETA, RHO, NU);
    SABRFormulaData sabrDataRP = new SABRFormulaData(ALPHA, BETA, RHO + shift, NU);
    SABRFormulaData sabrDataNP = new SABRFormulaData(ALPHA, BETA, RHO, NU + shift);
    SABRExtrapolationRightFunction sabrExtrapolationAP = new SABRExtrapolationRightFunction(FORWARD, sabrDataAP, CUT_OFF_STRIKE, TIME_TO_EXPIRY, MU);
    SABRExtrapolationRightFunction sabrExtrapolationRP = new SABRExtrapolationRightFunction(FORWARD, sabrDataRP, CUT_OFF_STRIKE, TIME_TO_EXPIRY, MU);
    SABRExtrapolationRightFunction sabrExtrapolationNP = new SABRExtrapolationRightFunction(FORWARD, sabrDataNP, CUT_OFF_STRIKE, TIME_TO_EXPIRY, MU);
    // Below cut-off strike
    double priceInExpected = SABR_EXTRAPOLATION.price(optionIn);
    double[] priceInPP = new double[3];
    priceInPP[0] = sabrExtrapolationAP.price(optionIn);
    priceInPP[1] = sabrExtrapolationRP.price(optionIn);
    priceInPP[2] = sabrExtrapolationNP.price(optionIn);
    double[] priceInDsabr = new double[3];
    double priceIn = SABR_EXTRAPOLATION.priceAdjointSABR(optionIn, priceInDsabr);
    assertEquals("SABR extrapolation below cut-off: price in adjoint", priceInExpected, priceIn, 1E-5);
    double[] priceInDsabrExpected = new double[3];
    for (int loopparam = 0; loopparam < 3; loopparam++) {
      priceInDsabrExpected[loopparam] = (priceInPP[loopparam] - priceIn) / shift;
      assertEquals("SABR extrapolation below cut-off: derivative with respect to SABR parameter " + loopparam, priceInDsabrExpected[loopparam], priceInDsabr[loopparam], 1E-5);
    }
    // At cut-off strike
    double priceAtExpected = SABR_EXTRAPOLATION.price(optionAt);
    double[] priceAtPP = new double[3];
    priceAtPP[0] = sabrExtrapolationAP.price(optionAt);
    priceAtPP[1] = sabrExtrapolationRP.price(optionAt);
    priceAtPP[2] = sabrExtrapolationNP.price(optionAt);
    double[] priceAtDsabr = new double[3];
    double priceAt = SABR_EXTRAPOLATION.priceAdjointSABR(optionAt, priceAtDsabr);
    assertEquals("SABR extrapolation at cut-off: price in adjoint", priceAtExpected, priceAt, 1E-5);
    double[] priceAtDsabrExpected = new double[3];
    for (int loopparam = 0; loopparam < 3; loopparam++) {
      priceAtDsabrExpected[loopparam] = (priceAtPP[loopparam] - priceAt) / shift;
      assertEquals("SABR extrapolation at cut-off: derivative with respect to SABR parameter " + loopparam, priceAtDsabrExpected[loopparam], priceAtDsabr[loopparam], 1E-5);
    }
    // Above cut-off strike
    double[] abc = SABR_EXTRAPOLATION.getParameter();
    double[][] abcDP = SABR_EXTRAPOLATION.getParameterDerivativeSABR();
    double[][] abcPP = new double[3][3];
    abcPP[0] = sabrExtrapolationAP.getParameter();
    abcPP[1] = sabrExtrapolationRP.getParameter();
    abcPP[2] = sabrExtrapolationNP.getParameter();
    double[][] abcDPExpected = new double[3][3];
    for (int loopparam = 0; loopparam < 3; loopparam++) {
      for (int loopabc = 0; loopabc < 3; loopabc++) {
        abcDPExpected[loopparam][loopabc] = (abcPP[loopparam][loopabc] - abc[loopabc]) / shift;
        assertEquals("SABR extrapolation: parameters derivative " + loopparam + " " + loopabc, 1.0, abcDPExpected[loopparam][loopabc] / abcDP[loopparam][loopabc], 5.0E-2);
      }
    }
    double priceOutExpected = SABR_EXTRAPOLATION.price(optionOut);
    double[] priceOutPP = new double[3];
    priceOutPP[0] = sabrExtrapolationAP.price(optionOut);
    priceOutPP[1] = sabrExtrapolationRP.price(optionOut);
    priceOutPP[2] = sabrExtrapolationNP.price(optionOut);
    double[] priceOutDsabr = new double[3];
    double priceOut = SABR_EXTRAPOLATION.priceAdjointSABR(optionOut, priceOutDsabr);
    assertEquals("SABR extrapolation above cut-off: price in adjoint", priceOutExpected, priceOut, 1E-5);
    double[] priceOutDsabrExpected = new double[3];
    for (int loopparam = 0; loopparam < 3; loopparam++) {
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

  @Test(enabled = false)
  /**
   * Tests to graph the smile for different tail parameters.
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
