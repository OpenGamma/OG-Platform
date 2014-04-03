/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Tests related to the construction of the Hull-White one factor model with piecewise constant volatility. The computation of several model related factors are also tested.
 */
@Test(groups = TestGroup.UNIT)
public class HullWhiteOneFactorPiecewiseConstantInterestRateModelTest {

  private static final double MEAN_REVERSION = 0.01;
  private static final double[] VOLATILITY = new double[] {0.01, 0.011, 0.012, 0.013, 0.014 };
  private static final double[] VOLATILITY_TIME = new double[] {0.5, 1.0, 2.0, 5.0 };
  private static final HullWhiteOneFactorPiecewiseConstantParameters MODEL_PARAMETERS = new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY, VOLATILITY_TIME);
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  private static final double[] DCF_FIXED = new double[] {0.50, 0.48 };
  private static final double[] ALPHA_FIXED = new double[] {0.02, 0.04 };
  private static final double[] DCF_IBOR = new double[] {-1.0, -0.01, 0.01, -0.01, 0.95 };
  private static final double[] ALPHA_IBOR = new double[] {0.00, 0.01, 0.02, 0.03, 0.04 };

  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_RATE_DELTA = 1.0E-8;
  private static final double TOLERANCE_RATE_DELTA2 = 1.0E-7;
  private static final double TOLERANCE_ALPHA = 1E-8;

  private static final IborIndex EURIBOR3M = IndexIborMaster.getInstance().getIndex("EURIBOR3M");

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals(MEAN_REVERSION, MODEL_PARAMETERS.getMeanReversion());
    for (int loopperiod = 0; loopperiod < VOLATILITY.length; loopperiod++) {
      assertEquals(VOLATILITY[loopperiod], MODEL_PARAMETERS.getVolatility()[loopperiod]);
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
    final double volReplaced = 0.02;
    MODEL_PARAMETERS.setLastVolatility(volReplaced);
    assertEquals(volReplaced, MODEL_PARAMETERS.getVolatility()[MODEL_PARAMETERS.getVolatility().length - 1]);
    MODEL_PARAMETERS.setLastVolatility(VOLATILITY[VOLATILITY.length - 1]);
    for (int loopperiod = 0; loopperiod < VOLATILITY.length; loopperiod++) {
      assertEquals(VOLATILITY[loopperiod], MODEL_PARAMETERS.getVolatility()[loopperiod]);
    }
  }

  @Test
  /**
   * Tests the equal and hash code methods.
   */
  public void equalHash() {
    final HullWhiteOneFactorPiecewiseConstantParameters newParameter = new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY, VOLATILITY_TIME);
    assertTrue("Hull-White model equals", MODEL_PARAMETERS.equals(newParameter));
    assertTrue("Hull-White model hash code", MODEL_PARAMETERS.hashCode() == newParameter.hashCode());
    final HullWhiteOneFactorPiecewiseConstantParameters modifiedParameter = new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION + 0.01, VOLATILITY, VOLATILITY_TIME);
    assertFalse("Hull-White model equals", MODEL_PARAMETERS.equals(modifiedParameter));
  }

  @Test
  /**
   * Test the future convexity adjustment factor v a hard-coded value.
   */
  public void futureConvexityFactor() {
    final Calendar calendar = new MondayToFridayCalendar("A");
    final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
    final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -EURIBOR3M.getSpotLag(), calendar);
    final double noitonal = 1000000.0; // 1m
    final double futuresAccrualFactor = 0.25;
    final double referencePrice = 0.99;
    final String name = "ERU2";
    final LocalDate REFERENCE_DATE = LocalDate.of(2010, 8, 18);
    final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE, LocalTime.MIDNIGHT), ZoneOffset.UTC);
    final InterestRateFutureSecurityDefinition eru2Definition = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, EURIBOR3M, noitonal, futuresAccrualFactor, name, calendar);
    final InterestRateFutureSecurity eru2 = eru2Definition.toDerivative(REFERENCE_DATE_ZONED, referencePrice);
    final double factor = MODEL.futuresConvexityFactor(MODEL_PARAMETERS, eru2.getTradingLastTime(), eru2.getFixingPeriodStartTime(), eru2.getFixingPeriodEndTime());
    final double expectedFactor = 1.000079130767980;
    assertEquals("Hull-White one factor: future convexity adjusment factor", expectedFactor, factor, TOLERANCE_RATE);
    // Derivative with respect to volatility parameters
    final int nbSigma = MODEL_PARAMETERS.getVolatility().length;
    final double[] sigmaBar = new double[nbSigma];
    final double factor2 = MODEL.futuresConvexityFactor(MODEL_PARAMETERS, eru2.getTradingLastTime(), eru2.getFixingPeriodStartTime(), eru2.getFixingPeriodEndTime(), sigmaBar);
    assertEquals("Hull-White one factor: future convexity adjusment factor", factor, factor2, TOLERANCE_RATE);
    final double[] sigmaBarExpected = new double[nbSigma];
    final double shift = 1E-6;
    for (int loops = 0; loops < nbSigma; loops++) {
      double[] volBumped = VOLATILITY.clone();
      volBumped[loops] += shift;
      HullWhiteOneFactorPiecewiseConstantParameters parametersBumped = new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION, volBumped, VOLATILITY_TIME);
      double factorPlus = MODEL.futuresConvexityFactor(parametersBumped, eru2.getTradingLastTime(), eru2.getFixingPeriodStartTime(), eru2.getFixingPeriodEndTime());
      volBumped[loops] -= 2 * shift;
      parametersBumped = new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION, volBumped, VOLATILITY_TIME);
      double factorMinus = MODEL.futuresConvexityFactor(parametersBumped, eru2.getTradingLastTime(), eru2.getFixingPeriodStartTime(), eru2.getFixingPeriodEndTime());
      sigmaBarExpected[loops] = (factorPlus - factorMinus) / (2 * shift);
      assertEquals("Hull-White one factor: future convexity adjusment factor", sigmaBarExpected[loops], sigmaBar[loops], TOLERANCE_RATE);
    }
  }

  @Test
  /**
   * Test the payment delay convexity adjustment factor.
   */
  public void paymentDelayConvexityFactor() {
    final double startExpiryTime = 1.00;
    final double endExpiryTime = 3.00;
    final double startFixingPeriod = 3.05;
    final double endFixingPeriod = 3.55;
    final double paymentTime = 3.45;
    final double hwMeanReversion = 0.011;
    // Constant volatility
    final double hwEta = 0.02;
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = new HullWhiteOneFactorPiecewiseConstantParameters(hwMeanReversion, new double[] {hwEta }, new double[0]);
    final double factor1 = (Math.exp(-hwMeanReversion * endFixingPeriod) - Math.exp(-hwMeanReversion * paymentTime)) *
        (Math.exp(-hwMeanReversion * endFixingPeriod) - Math.exp(-hwMeanReversion * startFixingPeriod));
    final double num = 2 * Math.pow(hwMeanReversion, 3);
    final double factor2 = hwEta * hwEta * (Math.exp(2 * hwMeanReversion * endExpiryTime) - Math.exp(2 * hwMeanReversion * startExpiryTime));
    final double factorExpected = Math.exp(factor1 * factor2 / num);
    final double factorComputed = MODEL.paymentDelayConvexityFactor(parameters, startExpiryTime, endExpiryTime, startFixingPeriod, endFixingPeriod, paymentTime);
    assertEquals("Hull-White one factor: payment delay adjustment factor", factorExpected, factorComputed, TOLERANCE_RATE);
    // Piecewise constant constant volatility
    final double[] hwEtaP = new double[] {0.02, 0.021, 0.022, 0.023 };
    final double[] hwTime = new double[] {0.5, 1.0, 2.0 };
    final HullWhiteOneFactorPiecewiseConstantParameters parametersP = new HullWhiteOneFactorPiecewiseConstantParameters(hwMeanReversion, hwEtaP, hwTime);
    double factorP2 = hwEtaP[2] * hwEtaP[2] * (Math.exp(2 * hwMeanReversion * hwTime[2]) - Math.exp(2 * hwMeanReversion * startExpiryTime));
    factorP2 += hwEtaP[3] * hwEtaP[3] * (Math.exp(2 * hwMeanReversion * endExpiryTime) - Math.exp(2 * hwMeanReversion * hwTime[2]));
    final double factorPExpected = Math.exp(factor1 * factorP2 / num);
    final double factorPComputed = MODEL.paymentDelayConvexityFactor(parametersP, startExpiryTime, endExpiryTime, startFixingPeriod, endFixingPeriod, paymentTime);
    assertEquals("Hull-White one factor: payment delay adjustment factor", factorPExpected, factorPComputed, TOLERANCE_RATE);
  }

  @Test
  /**
   * Test the bond volatility (called alpha) vs a hard-coded value.
   */
  public void alpha() {
    final double expiry1 = 0.25;
    final double expiry2 = 2.25;
    final double numeraire = 10.0;
    final double maturity = 9.0;
    double alphaExpected = -0.015191631;
    double alpha = MODEL.alpha(MODEL_PARAMETERS, expiry1, expiry2, numeraire, maturity); //All data
    assertEquals("Hull-White one factor: bond volatility (alpha) - all", alphaExpected, alpha, TOLERANCE_ALPHA);
    alphaExpected = -0.015859116;
    alpha = MODEL.alpha(MODEL_PARAMETERS, 0.0, expiry2, numeraire, maturity);//From today
    assertEquals("Hull-White one factor: bond volatility (alpha)- today", alphaExpected, alpha, TOLERANCE_ALPHA);
    alphaExpected = 0.111299267;
    alpha = MODEL.alpha(MODEL_PARAMETERS, 0.0, expiry2, expiry2, maturity);// From today with expiry numeraire
    assertEquals("Hull-White one factor: bond volatility (alpha) - today and expiry numeraire", alphaExpected, alpha, TOLERANCE_ALPHA);
    alpha = MODEL.alpha(MODEL_PARAMETERS, 0.0, 0.0, numeraire, maturity); // From 0 to 0
    assertEquals("Hull-White one factor: bond volatility (alpha) - today and expiry numeraire", 0.0d, alpha, TOLERANCE_ALPHA);
  }

  @Test
  /**
   * Test the adjoint algorithmic differentiation version of alpha.
   */
  public void alphaDSigma() {
    final double expiry1 = 0.25;
    final double expiry2 = 2.25;
    final double numeraire = 10.0;
    final double maturity = 9.0;
    final int nbVolatility = VOLATILITY.length;
    final double[] alphaDerivatives = new double[nbVolatility];
    final double alpha = MODEL.alpha(MODEL_PARAMETERS, expiry1, expiry2, numeraire, maturity, alphaDerivatives);
    final double alpha2 = MODEL.alpha(MODEL_PARAMETERS, expiry1, expiry2, numeraire, maturity);
    assertEquals("Alpha adjoint: value", alpha2, alpha, 1.0E-10);
    final double shiftVol = 1.0E-6;
    final double[] volatilityBumped = new double[nbVolatility];
    System.arraycopy(VOLATILITY, 0, volatilityBumped, 0, nbVolatility);
    final double[] alphaBumpedPlus = new double[nbVolatility];
    final double[] alphaBumpedMinus = new double[nbVolatility];
    HullWhiteOneFactorPiecewiseConstantParameters parametersBumped;
    for (int loopvol = 0; loopvol < nbVolatility; loopvol++) {
      volatilityBumped[loopvol] += shiftVol;
      parametersBumped = new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION, volatilityBumped, VOLATILITY_TIME);
      alphaBumpedPlus[loopvol] = MODEL.alpha(parametersBumped, expiry1, expiry2, numeraire, maturity);
      volatilityBumped[loopvol] -= 2 * shiftVol;
      parametersBumped = new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION, volatilityBumped, VOLATILITY_TIME);
      alphaBumpedMinus[loopvol] = MODEL.alpha(parametersBumped, expiry1, expiry2, numeraire, maturity);
      assertEquals("Alpha adjoint: derivative " + loopvol + " - Difference: " + ((alphaBumpedPlus[loopvol] - alphaBumpedMinus[loopvol]) / (2 * shiftVol) - alphaDerivatives[loopvol]),
          (alphaBumpedPlus[loopvol] - alphaBumpedMinus[loopvol]) / (2 * shiftVol), alphaDerivatives[loopvol], 1.0E-9);
      volatilityBumped[loopvol] = VOLATILITY[loopvol];
    }
  }

  @Test
  /**
   * Test the swaption exercise boundary.
   */
  public void kappa() {
    final double[] cashFlowAmount = new double[] {-1.0, 0.05, 0.05, 0.05, 0.05, 1.05 };
    final double notional = 100000000; // 100m
    final double[] cashFlowTime = new double[] {10.0, 11.0, 12.0, 13.0, 14.00, 15.00 };
    final double expiryTime = cashFlowTime[0] - 2.0 / 365.0;
    final int nbCF = cashFlowAmount.length;
    final double[] discountedCashFlow = new double[nbCF];
    final double[] alpha = new double[nbCF];
    final double rate = 0.04;
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      discountedCashFlow[loopcf] = cashFlowAmount[loopcf] * Math.exp(-rate * cashFlowTime[loopcf]) * notional;
      alpha[loopcf] = MODEL.alpha(MODEL_PARAMETERS, 0.0, expiryTime, expiryTime, cashFlowTime[loopcf]);
    }
    final double kappa = MODEL.kappa(discountedCashFlow, alpha);
    double swapValue = 0.0;
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      swapValue += discountedCashFlow[loopcf] * Math.exp(-Math.pow(alpha[loopcf], 2.0) / 2.0 - alpha[loopcf] * kappa);
    }
    assertEquals("Exercise boundary", 0.0, swapValue, 1.0E-1);
  }

  @Test
  public void swapRate() {
    final double shift = 1.0E-4;
    final double x = 0.1;
    double numerator = 0.0;
    for (int loopcf = 0; loopcf < DCF_IBOR.length; loopcf++) {
      numerator += DCF_IBOR[loopcf] * Math.exp(-ALPHA_IBOR[loopcf] * x - 0.5 * ALPHA_IBOR[loopcf] * ALPHA_IBOR[loopcf]);
    }
    double denominator = 0.0;
    for (int loopcf = 0; loopcf < DCF_FIXED.length; loopcf++) {
      denominator += DCF_FIXED[loopcf] * Math.exp(-ALPHA_FIXED[loopcf] * x - 0.5 * ALPHA_FIXED[loopcf] * ALPHA_FIXED[loopcf]);
    }
    final double swapRateExpected = -numerator / denominator;
    final double swapRateComputed = MODEL.swapRate(x, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);
    assertEquals("Hull-White model: swap rate", swapRateExpected, swapRateComputed, TOLERANCE_RATE);
    final double swapRatePlus = MODEL.swapRate(x + shift, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);
    final double swapRateMinus = MODEL.swapRate(x - shift, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);
    final double swapRateDx1Expected = (swapRatePlus - swapRateMinus) / (2 * shift);
    final double swapRateDx1Computed = MODEL.swapRateDx1(x, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);
    assertEquals("Hull-White model: swap rate", swapRateDx1Expected, swapRateDx1Computed, TOLERANCE_RATE_DELTA);
    final double swapRateDx2Expected = (swapRatePlus + swapRateMinus - 2 * swapRateComputed) / (shift * shift);
    final double swapRateDx2Computed = MODEL.swapRateDx2(x, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);
    assertEquals("Hull-White model: swap rate", swapRateDx2Expected, swapRateDx2Computed, TOLERANCE_RATE_DELTA2);
  }

  @Test
  public void swapRateDdcf() {
    final double shift = 1.0E-8;
    final double x = 0.0;
    final double[] ddcffComputed = MODEL.swapRateDdcff1(x, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);

    final double[] ddcffExpected = new double[DCF_FIXED.length];
    for (int loopcf = 0; loopcf < DCF_FIXED.length; loopcf++) {
      final double[] dsf_bumped = DCF_FIXED.clone();
      dsf_bumped[loopcf] += shift;
      final double swapRatePlus = MODEL.swapRate(x, dsf_bumped, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);
      dsf_bumped[loopcf] -= 2 * shift;
      final double swapRateMinus = MODEL.swapRate(x, dsf_bumped, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);
      ddcffExpected[loopcf] = (swapRatePlus - swapRateMinus) / (2 * shift);
    }
    ArrayAsserts.assertArrayEquals("Hull-White model: swap rate", ddcffExpected, ddcffComputed, TOLERANCE_RATE_DELTA);

    final double[] ddcfiExpected = new double[DCF_IBOR.length];
    for (int loopcf = 0; loopcf < DCF_IBOR.length; loopcf++) {
      final double[] dsf_bumped = DCF_IBOR.clone();
      dsf_bumped[loopcf] += shift;
      final double swapRatePlus = MODEL.swapRate(x, DCF_FIXED, ALPHA_FIXED, dsf_bumped, ALPHA_IBOR);
      dsf_bumped[loopcf] -= 2 * shift;
      final double swapRateMinus = MODEL.swapRate(x, DCF_FIXED, ALPHA_FIXED, dsf_bumped, ALPHA_IBOR);
      ddcfiExpected[loopcf] = (swapRatePlus - swapRateMinus) / (2 * shift);
    }
    final double[] ddcfiComputed = MODEL.swapRateDdcfi1(x, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);
    ArrayAsserts.assertArrayEquals("Hull-White model: swap rate", ddcfiExpected, ddcfiComputed, TOLERANCE_RATE_DELTA);
  }

  @Test
  public void swapRateDa() {
    final double shift = 1.0E-8;
    final double x = 0.0;
    final double[] dafComputed = MODEL.swapRateDaf1(x, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);

    final double[] dafExpected = new double[ALPHA_FIXED.length];
    for (int loopcf = 0; loopcf < ALPHA_FIXED.length; loopcf++) {
      final double[] afBumped = ALPHA_FIXED.clone();
      afBumped[loopcf] += shift;
      final double swapRatePlus = MODEL.swapRate(x, DCF_FIXED, afBumped, DCF_IBOR, ALPHA_IBOR);
      afBumped[loopcf] -= 2 * shift;
      final double swapRateMinus = MODEL.swapRate(x, DCF_FIXED, afBumped, DCF_IBOR, ALPHA_IBOR);
      dafExpected[loopcf] = (swapRatePlus - swapRateMinus) / (2 * shift);
    }
    ArrayAsserts.assertArrayEquals("Hull-White model: swap rate dAlphaFixed", dafExpected, dafComputed, TOLERANCE_RATE_DELTA);

    final double[] daiExpected = new double[DCF_IBOR.length];
    for (int loopcf = 0; loopcf < DCF_IBOR.length; loopcf++) {
      final double[] aiBumped = ALPHA_IBOR.clone();
      aiBumped[loopcf] += shift;
      final double swapRatePlus = MODEL.swapRate(x, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, aiBumped);
      aiBumped[loopcf] -= 2 * shift;
      final double swapRateMinus = MODEL.swapRate(x, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, aiBumped);
      daiExpected[loopcf] = (swapRatePlus - swapRateMinus) / (2 * shift);
    }
    final double[] daiComputed = MODEL.swapRateDai1(x, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);
    ArrayAsserts.assertArrayEquals("Hull-White model: swap rate dAlphaIbor", daiExpected, daiComputed, TOLERANCE_RATE_DELTA);
  }

  @Test
  public void swapRateDx2Ddcf() {
    final double shift = 1.0E-7;
    final double x = 0.0;
    final Pair<double[], double[]> dx2ddcfComputed = MODEL.swapRateDx2Ddcf1(x, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);
    final double[] dx2DdcffExpected = new double[DCF_FIXED.length];
    for (int loopcf = 0; loopcf < DCF_FIXED.length; loopcf++) {
      final double[] dsf_bumped = DCF_FIXED.clone();
      dsf_bumped[loopcf] += shift;
      final double swapRatePlus = MODEL.swapRateDx2(x, dsf_bumped, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);
      dsf_bumped[loopcf] -= 2 * shift;
      final double swapRateMinus = MODEL.swapRateDx2(x, dsf_bumped, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);
      dx2DdcffExpected[loopcf] = (swapRatePlus - swapRateMinus) / (2 * shift);
    }
    ArrayAsserts.assertArrayEquals("Hull-White model: swap rate", dx2DdcffExpected, dx2ddcfComputed.getFirst(), TOLERANCE_RATE_DELTA2);
    final double[] dx2DdcfiExpected = new double[DCF_IBOR.length];
    for (int loopcf = 0; loopcf < DCF_IBOR.length; loopcf++) {
      final double[] dsf_bumped = DCF_IBOR.clone();
      dsf_bumped[loopcf] += shift;
      final double swapRatePlus = MODEL.swapRateDx2(x, DCF_FIXED, ALPHA_FIXED, dsf_bumped, ALPHA_IBOR);
      dsf_bumped[loopcf] -= 2 * shift;
      final double swapRateMinus = MODEL.swapRateDx2(x, DCF_FIXED, ALPHA_FIXED, dsf_bumped, ALPHA_IBOR);
      dx2DdcfiExpected[loopcf] = (swapRatePlus - swapRateMinus) / (2 * shift);
    }
    ArrayAsserts.assertArrayEquals("Hull-White model: swap rate", dx2DdcfiExpected, dx2ddcfComputed.getSecond(), TOLERANCE_RATE_DELTA2);
  }

  @Test
  public void swapRateDx2Da() {
    final double shift = 1.0E-7;
    final double x = 0.0;
    final Pair<double[], double[]> dx2DaComputed = MODEL.swapRateDx2Da1(x, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, ALPHA_IBOR);

    final double[] dx2DafExpected = new double[DCF_FIXED.length];
    for (int loopcf = 0; loopcf < DCF_FIXED.length; loopcf++) {
      final double[] afBumped = ALPHA_FIXED.clone();
      afBumped[loopcf] += shift;
      final double swapRatePlus = MODEL.swapRateDx2(x, DCF_FIXED, afBumped, DCF_IBOR, ALPHA_IBOR);
      afBumped[loopcf] -= 2 * shift;
      final double swapRateMinus = MODEL.swapRateDx2(x, DCF_FIXED, afBumped, DCF_IBOR, ALPHA_IBOR);
      dx2DafExpected[loopcf] = (swapRatePlus - swapRateMinus) / (2 * shift);
    }
    ArrayAsserts.assertArrayEquals("Hull-White model: swap rate - dx2 dAlphaFixed", dx2DafExpected, dx2DaComputed.getFirst(), TOLERANCE_RATE_DELTA2);

    final double[] dx2DaiExpected = new double[DCF_IBOR.length];
    for (int loopcf = 0; loopcf < DCF_IBOR.length; loopcf++) {
      final double[] aiBumped = ALPHA_IBOR.clone();
      aiBumped[loopcf] += shift;
      final double swapRatePlus = MODEL.swapRateDx2(x, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, aiBumped);
      aiBumped[loopcf] -= 2 * shift;
      final double swapRateMinus = MODEL.swapRateDx2(x, DCF_FIXED, ALPHA_FIXED, DCF_IBOR, aiBumped);
      dx2DaiExpected[loopcf] = (swapRatePlus - swapRateMinus) / (2 * shift);
    }
    ArrayAsserts.assertArrayEquals("Hull-White model: swap rate - dx2 dAlphaIbor", dx2DaiExpected, dx2DaComputed.getSecond(), TOLERANCE_RATE_DELTA2);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performanceAlphaAdjoint() {
    final double expiry1 = 0.25;
    final double expiry2 = 2.25;
    final double numeraire = 10.0;
    final double maturity = 9.0;
    final int nbVolatility = VOLATILITY.length;
    final double[] alphaDerivatives = new double[nbVolatility];
    long startTime, endTime;
    final int nbTest = 100000;
    double alpha = 0.0;
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      alpha = MODEL.alpha(MODEL_PARAMETERS, expiry1, expiry2, numeraire, maturity);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " alpha Hull-White: " + (endTime - startTime) + " ms");
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      alpha = MODEL.alpha(MODEL_PARAMETERS, expiry1, expiry2, numeraire, maturity, alphaDerivatives);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " alpha Hull-White adjoint (value+" + nbVolatility + " derivatives): " + (endTime - startTime) + " ms");
    // Performance note: value: 31-Aug-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 75 ms for 1000000 swaptions.
    // Performance note: value+derivatives: 31-Aug-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 100 ms for 1000000 swaptions.
    System.out.println("Alpha: " + alpha);
  }

  @Test(enabled = false)
  /**
   * Test the payment delay convexity adjustment factor. Analysis of the size. 
   * In normal test, should have (enabled=false)
   */
  public void paymentDelayConvexityFactorAnalysis() {

    final double hwMeanReversion = 0.01;
    final double rate = 0.02;

    final double[] tenorTime = {0.25, 0.50 };
    final int nbTenors = tenorTime.length;
    final double[] lagPayTime = {1.0d / 365.0d, 2.0d / 365.0d, 7.0d / 365.0d };
    final int nbLags = lagPayTime.length;
    final double lagFixTime = 2.0d / 365.0d;
    final int nbPeriods = 120;
    final double startTimeFirst = 0.25;
    final double startTimeStep = 0.25;
    final double[] startTime = new double[nbPeriods];
    for (int loopp = 0; loopp < nbPeriods; loopp++) {
      startTime[loopp] = startTimeFirst + loopp * startTimeStep;
    }

    // Constant volatility
    final double hwEta = 0.02;
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = new HullWhiteOneFactorPiecewiseConstantParameters(hwMeanReversion, new double[] {hwEta }, new double[0]);

    final double[][][] factor = new double[nbTenors][nbLags][nbPeriods];
    final double[][][] adj = new double[nbTenors][nbLags][nbPeriods];
    for (int loopt = 0; loopt < nbTenors; loopt++) {
      for (int loopl = 0; loopl < nbLags; loopl++) {
        for (int loopp = 0; loopp < nbPeriods; loopp++) {
          factor[loopt][loopl][loopp] = MODEL.paymentDelayConvexityFactor(parameters, 0, startTime[loopp] - lagFixTime, startTime[loopp], startTime[loopp] + tenorTime[loopt],
              startTime[loopp] + tenorTime[loopt] - lagPayTime[loopl]);
          adj[loopt][loopl][loopp] = (1.0d / tenorTime[loopt] - rate) * (factor[loopt][loopl][loopp] - 1);
        }
      }
    }

    @SuppressWarnings("unused")
    int t = 0;
    t++;

  }

}
