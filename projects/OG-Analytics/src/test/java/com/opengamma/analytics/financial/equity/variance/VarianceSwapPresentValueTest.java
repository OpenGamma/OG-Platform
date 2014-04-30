/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import static com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.RealizedVariance;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VarianceSwapPresentValueTest {

  // Setup ------------------------------------------

  // Market data
  private static final double SPOT = 80;
  private static final double DRIFT = 0.05;
  private static final ForwardCurve FORWARD_CURVE = new ForwardCurve(SPOT, DRIFT);
  // private static final double FORWARD = 100;

  // The pricing method
  private static final VarianceSwapStaticReplication PRICER = new VarianceSwapStaticReplication();

  private static final YieldAndDiscountCurve DISCOUNT = new YieldCurve("Discount", ConstantDoublesCurve.from(0.05));

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 0.5, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0 };
  private static final double[] STRIKES = new double[] {40, 80, 100, 120, 40, 80, 100, 120, 40, 80, 100, 120, 40, 80, 100, 120 };
  private static final double[] VOLS = new double[] {0.28, 0.28, 0.28, 0.28, 0.25, 0.25, 0.25, 0.25, 0.26, 0.24, 0.23, 0.25, 0.20, 0.20, 0.20, 0.20 };

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR_1D_STRIKE = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  final static CombinedInterpolatorExtrapolator INTERPOLATOR_1D_EXPIRY = getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, new GridInterpolator2D(INTERPOLATOR_1D_EXPIRY, INTERPOLATOR_1D_STRIKE));
  private static final BlackVolatilitySurfaceStrike VOL_SURFACE = new BlackVolatilitySurfaceStrike(SURFACE);
  private static final StaticReplicationDataBundle MARKET = new StaticReplicationDataBundle(VOL_SURFACE, DISCOUNT, FORWARD_CURVE);

  // The derivative
  private static final double varStrike = 0.05;
  private static final double varNotional = 10000; // A notional of 10000 means PV is in bp
  private static final double now = 0;
  private static final double expiry1 = 1;
  //private static final double expiry2 = 2;
  private static final double expiry5 = 5;
  //private static final double expiry10 = 10;
  private static final int nObsExpected = 750;
  private static final int noObsDisrupted = 0;
  private static final double annualization = 252;

  private static final double[] noObservations = {};
  private static final double[] noObsWeights = {};

  private static final double[] singleObsSoNoReturn = {80 };
  private static final VarianceSwap swapStartsNow = new VarianceSwap(now, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, singleObsSoNoReturn, noObsWeights);

  private static final ZonedDateTime today = ZonedDateTime.now();
  private static final ZonedDateTime tomorrow = today.plusDays(1);
  private static final double tPlusOne = TimeCalculator.getTimeBetween(today, tomorrow);

  // Tests ------------------------------------------

  private static double TOLERATED = 1.0E-9;

  @Test
  /**
   * Compare presentValue with impliedVariance, ensuring that spot starting varianceSwaps equal that coming only from implied part <p>
   * Ensure we handle the one underlying observation correctly. i.e. no *returns* yet
   *
   */
  public void onFirstObsDateWithOneObs() {

    final double pv = PRICER.presentValue(swapStartsNow, MARKET);
    final double variance = PRICER.expectedVariance(swapStartsNow, MARKET);
    final double pvOfHedge = swapStartsNow.getVarNotional() * (variance - swapStartsNow.getVarStrike()) * MARKET.getDiscountCurve().getDiscountFactor(expiry5);
    assertEquals(pv, pvOfHedge, TOLERATED);
  }

  @Test
  /**
   * Variance is additive, hence a forward starting VarianceSwap may be decomposed into the difference of two spot starting ones.
   */
  public void swapForwardStarting() {

    // First, create a swap which starts in 1 year and observes for a further four
    final VarianceSwap swapForwardStarting1to5 = new VarianceSwap(expiry1, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, singleObsSoNoReturn,
        noObsWeights);

    final double pvFowardStart = PRICER.presentValue(swapForwardStarting1to5, MARKET);

    // Second, create two spot starting swaps. One that expires at the end of observations, one expiring at the beginnning
    final VarianceSwap swapSpotStarting1 = new VarianceSwap(now, expiry1, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, singleObsSoNoReturn, noObsWeights);
    final VarianceSwap swapSpotStarting5 = new VarianceSwap(now, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, singleObsSoNoReturn, noObsWeights);

    final double pvSpot1 = PRICER.presentValue(swapSpotStarting1, MARKET);
    final double pvSpot5 = PRICER.presentValue(swapSpotStarting5, MARKET);

    final double pvDiffOfTwoSpotStarts = (5.0 * pvSpot5 - 1.0 * pvSpot1) / 4.0;

    assertEquals(pvFowardStart, pvDiffOfTwoSpotStarts, TOLERATED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void onFirstObsWithoutObs() {

    final VarianceSwap swapOnFirstObsWithoutObs = new VarianceSwap(now, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, noObservations,
        noObsWeights);
    @SuppressWarnings("unused")
    final double pv = PRICER.presentValue(swapOnFirstObsWithoutObs, MARKET);
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

    final double pv = PRICER.presentValue(swapPaysTomorrow, MARKET);
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
    final double pv = PRICER.presentValue(swapEnded, MARKET);
    assertEquals(0.0, pv, TOLERATED);
  }

}
