/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance;

import static com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.financial.equity.variance.pricing.RealizedVariance;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication.StrikeParameterization;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.BlackVolatilityFixedStrikeSurface;
import com.opengamma.math.FunctionUtils;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.TimeCalculator;

/**
 * 
 */
public class VarianceSwapPresentValueTest {

  // Setup ------------------------------------------

  // The pricing method
  final VarianceSwapStaticReplication pricer_default_w_cutoff = new VarianceSwapStaticReplication(StrikeParameterization.STRIKE);
  final VarianceSwapStaticReplication pricer_without_cutoff = new VarianceSwapStaticReplication();

  // Market data
  private static final double SPOT = 80;
  private static final double FORWARD = 100;
  @SuppressWarnings("unused")
  private static final double TEST_VOL = 0.25;
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final YieldAndDiscountCurve DISCOUNT = CURVES.getCurve("Funding");

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 0.5, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0 };
  private static final double[] STRIKES = new double[] {40, 80, 100, 120, 40, 80, 100, 120, 40, 80, 100, 120, 40, 80, 100, 120 };
  private static final double[] VOLS = new double[] {0.28, 0.28, 0.28, 0.28, 0.25, 0.25, 0.25, 0.25, 0.26, 0.24, 0.23, 0.25, 0.20, 0.20, 0.20, 0.20 };

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR_1D_STRIKE = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  final static CombinedInterpolatorExtrapolator INTERPOLATOR_1D_EXPIRY = getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, new GridInterpolator2D(INTERPOLATOR_1D_EXPIRY, INTERPOLATOR_1D_STRIKE));
  private static final BlackVolatilityFixedStrikeSurface VOL_SURFACE = new BlackVolatilityFixedStrikeSurface(SURFACE);
  private static final VarianceSwapDataBundle MARKET = new VarianceSwapDataBundle(VOL_SURFACE, DISCOUNT, SPOT, FORWARD);

  // The derivative
  final double varStrike = 0.05;
  final double varNotional = 10000; // A notional of 10000 means PV is in bp
  final double now = 0;
  final double expiry1 = 1;
  final double expiry2 = 2;
  final double expiry5 = 5;
  final double expiry10 = 10;
  final int nObsExpected = 750;
  final int noObsDisrupted = 0;
  final static double annualization = 252;

  final double[] noObservations = {};
  final double[] noObsWeights = {};

  double[] singleObsSoNoReturn = {80 };
  final VarianceSwap swapStartsNow = new VarianceSwap(now, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, singleObsSoNoReturn, noObsWeights);

  final ZonedDateTime today = ZonedDateTime.now();
  final ZonedDateTime tomorrow = today.plusDays(1);
  final double tPlusOne = TimeCalculator.getTimeBetween(today, tomorrow);
  final VarianceSwap swapStartsTomorrow = new VarianceSwap(tPlusOne, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, noObservations, noObsWeights);

  // Tests ------------------------------------------

  private static double TOLERATED = 1.0E-9;

  @Test
  /**
   * Compare presentValue with impliedVariance, ensuring that spot starting varianceSwaps equal that coming only from implied part <p>
   * Ensure we handle the one underlying observation correctly. i.e. no *returns* yet
   * 
   */
  public void onFirstObsDateWithOneObs() {

    final double pv = pricer_default_w_cutoff.presentValue(swapStartsNow, MARKET);
    final double variance = pricer_default_w_cutoff.impliedVariance(swapStartsNow, MARKET);
    final double pvOfHedge = swapStartsNow.getVarNotional() * (variance - swapStartsNow.getVarStrike()) * MARKET.getDiscountCurve().getDiscountFactor(expiry5);
    assertEquals(pv, pvOfHedge, TOLERATED);
  }

  @Test
  /**
  * A few days before observations begin is the same as the day when they do => No convexity modelled for a couple days forward. 
  * Compare to swapForwardStarting below
  **/
  public void swapStartsTomorrow() {

    final double pvStartsObsTomorrow = pricer_default_w_cutoff.presentValue(swapStartsTomorrow, MARKET);
    final double pvStartsObsToday = pricer_default_w_cutoff.presentValue(swapStartsNow, MARKET);

    assertEquals(pvStartsObsTomorrow, pvStartsObsToday, TOLERATED);
  }

  @Test
  /**
   * Variance is additive, hence a forward starting VarianceSwap may be decomposed into the difference of two spot starting ones.
   */
  public void swapForwardStarting() {

    // First, create a swap which starts in 1 year and observes for a further four
    final VarianceSwap swapForwardStarting1to5 = new VarianceSwap(expiry1, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, singleObsSoNoReturn,
        noObsWeights);

    final double pvFowardStart = pricer_default_w_cutoff.presentValue(swapForwardStarting1to5, MARKET);

    // Second, create two spot starting swaps. One that expires at the end of observations, one expiring at the beginnning
    final VarianceSwap swapSpotStarting1 = new VarianceSwap(now, expiry1, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, singleObsSoNoReturn, noObsWeights);
    final VarianceSwap swapSpotStarting5 = new VarianceSwap(now, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, singleObsSoNoReturn, noObsWeights);

    final double pvSpot1 = pricer_default_w_cutoff.presentValue(swapSpotStarting1, MARKET);
    final double pvSpot5 = pricer_default_w_cutoff.presentValue(swapSpotStarting5, MARKET);

    final double pvDiffOfTwoSpotStarts = (5.0 * pvSpot5 - 1.0 * pvSpot1) / 4.0;

    assertEquals(pvFowardStart, pvDiffOfTwoSpotStarts, TOLERATED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void onFirstObsWithoutObs() {

    final VarianceSwap swapOnFirstObsWithoutObs = new VarianceSwap(now, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, noObservations,
        noObsWeights);
    @SuppressWarnings("unused")
    final double pv = pricer_default_w_cutoff.presentValue(swapOnFirstObsWithoutObs, MARKET);
  }

  final static double volAnnual = 0.28;
  final static double volDaily = volAnnual / Math.sqrt(annualization);
  final static double stdDevDaily = Math.sqrt(0.5) * volDaily;
  //final static ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, stdDevDaily);
  final static ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, stdDevDaily, new MersenneTwister64(99));

  final static int nObs = 252 * 5;
  final static double[] obsWeight = {1.0 };
  static double avgReturn = 0;
  static double avgSquareReturn = 0;
  static double[] obs = new double[nObs];

  static {
    for (int i = 0; i < nObs; i++) {
      obs[i] = Math.exp(NORMAL.nextRandom());
      if (i > 0) {
        avgReturn += Math.log(obs[i] / obs[i - 1]);
        avgSquareReturn += Math.pow(Math.log(obs[i] / obs[i - 1]), 2);
      }
    }
    avgReturn /= (nObs - 1);
    avgSquareReturn /= (nObs - 1);
  }

  @Test
  /**
   * Simply test the machinery: the average of squared log returns of a lognormal distribution 
   * will return the standard deviation used to generate the random observations
   */
  public void testAvgSquareReturn() {
    final double sampleDailyVariance = avgSquareReturn - FunctionUtils.square(avgReturn);
    final double sampleAnnualVariance = sampleDailyVariance * annualization;
    final double annualVolatilityEstimate = Math.sqrt(sampleAnnualVariance);
    assertEquals(volAnnual, annualVolatilityEstimate, 0.01);
    assertEquals(0.27710025417636447, annualVolatilityEstimate, TOLERATED);
  }

  @Test
  /**
   * After lastObs but before settlement date, presentValue == RealizedVar
   */
  public void swapObservationsCompleted() {

    final VarianceSwap swapPaysTomorrow = new VarianceSwap(-1., -tPlusOne, tPlusOne, varStrike, varNotional, Currency.EUR, annualization, nObs - 1, 0, obs, obsWeight);

    final double pv = pricer_default_w_cutoff.presentValue(swapPaysTomorrow, MARKET);
    final double variance = new RealizedVariance().evaluate(swapPaysTomorrow);
    final double pvOfHedge = swapStartsNow.getVarNotional() * (variance - swapStartsNow.getVarStrike()) * MARKET.getDiscountCurve().getDiscountFactor(tPlusOne);
    assertEquals(pvOfHedge, pv, 0.01);

  }

  @Test
  /**
   * After settlement, presentValue == 0.0
   */
  public void swapAfterSettlement() {
    final VarianceSwap swapEnded = new VarianceSwap(-1.0, -1.0 / 365, -1.0 / 365, varStrike, varNotional, Currency.EUR, annualization, nObs, 0, obs, obsWeight);
    final double pv = pricer_default_w_cutoff.presentValue(swapEnded, MARKET);
    assertEquals(0.0, pv, TOLERATED);
  }

  @Test
  /**
   * As valuation date approaches expiry, computation of ImpliedVariance remains robust. 
   * In particular, failures don't occur while fitting the left tail of the terminal distribution with a shiftedLognormal.. 
   * FIXME CASE: shiftedLognormal fitting fails if the prices at cutoff and spread points are zero. => Reparameterise strike based on delta so that strikes come in as time to expiry approaches
   */
  public void successFittingLeftTailAsExpiryApproaches() {

    final VarianceSwap swapEndsTomorrow = new VarianceSwap(-4.996, tPlusOne, tPlusOne, varStrike, varNotional, Currency.EUR, annualization, nObs, 0, obs, obsWeight);
    final double pvExtrapFlat = pricer_without_cutoff.presentValue(swapEndsTomorrow, MARKET);
    final double pvFitShiftedLn = pricer_default_w_cutoff.presentValue(swapEndsTomorrow, MARKET);
    assertEquals(pvExtrapFlat, pvFitShiftedLn, 0.01);
  }
}
