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

public class SABRExtrapolationRightFunctionTest {
  // Data
  private static final double NU = 0.50;
  private static final double RHO = -0.25;
  private static final double BETA = 0.50;
  private static final double ALPHA = 0.05;
  private static final double FORWARD = 0.05;
  private static final SABRFormulaData SABR_DATA = new SABRFormulaData(FORWARD, ALPHA, BETA, NU, RHO);
  private static final double CUT_OFF_STRIKE = 0.10; // Set low for the test
  private static final double MU = 4.0;
  private static final double TIME_TO_EXPIRY = 2.0;
  private static final SABRExtrapolationRightFunction SABR_EXTRAPOLATION = new SABRExtrapolationRightFunction(SABR_DATA, CUT_OFF_STRIKE, TIME_TO_EXPIRY, MU);

  // Function
  BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();

  @Test
  public void price() {
    double strikeIn = 0.08;
    double strikeAt = CUT_OFF_STRIKE;
    double strikeOut = 0.12;
    EuropeanVanillaOption optionIn = new EuropeanVanillaOption(strikeIn, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionAt = new EuropeanVanillaOption(strikeAt, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionOut = new EuropeanVanillaOption(strikeOut, TIME_TO_EXPIRY, true);
    Function1D<SABRFormulaData, Double> funcSabrIn = SABR_FUNCTION.getVolatilityFunction(optionIn);
    double volatilityIn = funcSabrIn.evaluate(SABR_DATA);
    BlackFunctionData dataBlackIn = new BlackFunctionData(SABR_DATA.getForward(), 1.0, volatilityIn);
    Function1D<BlackFunctionData, Double> funcBlackIn = BLACK_FUNCTION.getPriceFunction(optionIn);
    double priceExpectedIn = funcBlackIn.evaluate(dataBlackIn);
    double priceIn = SABR_EXTRAPOLATION.price(optionIn);
    assertEquals("SABR extrapolation, below cut-off", priceExpectedIn, priceIn, 1E-10);
    Function1D<SABRFormulaData, Double> funcSabrAt = SABR_FUNCTION.getVolatilityFunction(optionAt);
    double volatilityAt = funcSabrAt.evaluate(SABR_DATA);
    BlackFunctionData dataBlackAt = new BlackFunctionData(SABR_DATA.getForward(), 1.0, volatilityAt);
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
  public void priceDerivative() {

    double strikeIn = 0.08;
    double strikeAt = CUT_OFF_STRIKE;
    double strikeOut = 0.12;
    EuropeanVanillaOption optionIn = new EuropeanVanillaOption(strikeIn, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionAt = new EuropeanVanillaOption(strikeAt, TIME_TO_EXPIRY, true);
    EuropeanVanillaOption optionOut = new EuropeanVanillaOption(strikeOut, TIME_TO_EXPIRY, true);

    double shiftF = 0.000001;
    SABRFormulaData sabrDataFP = new SABRFormulaData(FORWARD + shiftF, ALPHA, BETA, NU, RHO);
    SABRExtrapolationRightFunction sabrExtrapolationFP = new SABRExtrapolationRightFunction(sabrDataFP, CUT_OFF_STRIKE, TIME_TO_EXPIRY, MU);

    // Below cut-off strike
    double priceIn = SABR_EXTRAPOLATION.price(optionIn);
    double priceInFP = sabrExtrapolationFP.price(optionIn);
    double priceInDF = SABR_EXTRAPOLATION.priceDF(optionIn);
    double priceInDFExpected = (priceInFP - priceIn) / shiftF;
    assertEquals("SABR extrapolation: derivative with respect to forward, below cut-off", priceInDFExpected, priceInDF, 1E-5);
    // At cut-off strike
    double priceAt = SABR_EXTRAPOLATION.price(optionAt);
    double priceAtFP = sabrExtrapolationFP.price(optionAt);
    double priceAtDF = SABR_EXTRAPOLATION.priceDF(optionAt);
    double priceAtDFExpected = (priceAtFP - priceAt) / shiftF;
    assertEquals("SABR extrapolation: derivative with respect to forward, at cut-off", priceAtDFExpected, priceAtDF, 1E-6);
    // Above cut-off strike
    double[] abc = SABR_EXTRAPOLATION.getParameter();
    double[] abcDF = SABR_EXTRAPOLATION.getParameterDF();
    double[] abcFP = sabrExtrapolationFP.getParameter();
    double[] abcDFExpected = new double[3];
    for (int loopparam = 0; loopparam < 3; loopparam++) {
      abcDFExpected[loopparam] = (abcFP[loopparam] - abc[loopparam]) / shiftF;
      assertEquals("SABR extrapolation: parameters derivative " + loopparam, 1.0, abcDFExpected[loopparam] / abcDF[loopparam], 5E-2);
    }
    double priceOut = SABR_EXTRAPOLATION.price(optionOut);
    double priceOutFP = sabrExtrapolationFP.price(optionOut);
    double priceOutDF = SABR_EXTRAPOLATION.priceDF(optionOut);
    double priceOutDFExpected = (priceOutFP - priceOut) / shiftF;
    assertEquals("SABR extrapolation: derivative with respect to forward, above cut-off", priceOutDFExpected, priceOutDF, 1E-5);
  }

  @Test
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
      SABRExtrapolationRightFunction sabrExtra = new SABRExtrapolationRightFunction(SABR_DATA, CUT_OFF_STRIKE, TIME_TO_EXPIRY, mu[loopmu]);
      for (int looppts = 0; looppts <= nbPts; looppts++) {
        strike[looppts] = CUT_OFF_STRIKE - rangeStrike + looppts * 4.0 * rangeStrike / nbPts;
        EuropeanVanillaOption option = new EuropeanVanillaOption(strike[looppts], TIME_TO_EXPIRY, true);
        price[loopmu][looppts] = sabrExtra.price(option);
        impliedVolatility[loopmu][looppts] = implied.getImpliedVolatility(blackData, option, price[loopmu][looppts]);
      }
    }
  }
}
