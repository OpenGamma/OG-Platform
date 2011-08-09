/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance;

import static com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import com.opengamma.financial.equity.variance.VarianceSwapRatesSensitivityCalculator;
import com.opengamma.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapDataBundle;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication.StrikeParameterization;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.volatility.surface.BlackVolatilityDeltaSurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilityFixedStrikeSurface;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.math.surface.NodalDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.TimeCalculator;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

/**
 * 
 */
public class VarianceSwapRatesSensitivityTest {

  private static VarianceSwapRatesSensitivityCalculator deltaCalculator = VarianceSwapRatesSensitivityCalculator.getInstance();
  // Tests ------------------------------------------

  /*
   * List of tests:
   * EQUITY FWD
   *  - Test zero if vols are flat
   * Rates 
   *  - For two scalar risks, compare to presentValue calc
   *  - calcDiscountRateSensitivity = 10000 * calcPV01
   *  - For bucketedDelta, compare Sum to calcDiscountRateSensitivity 
   * Vega
   * - Parallel Vega should be equal to 2 * sigma * Z(0,T)
   * - Test sum of individuals = One big shift by manually creating another vol surface
   * - Test that we don't get sensitivity on unexpected expiries
   */

  private static double TOLERATED = 1.0E-9;

  /**
   * Forward sensitivity comes only from volatility skew. Let's check
   */
  @Test
  public void testForwardSensitivity() {

    double fwd = MARKET.getForwardUnderlying();
    double relShift = 0.01;

    double deltaSkew = deltaCalculator.calcForwardSensitivity(swap5y, MARKET, relShift * fwd);
    double deltaFlatLong = deltaCalculator.calcForwardSensitivity(swap10y, MARKET, relShift * fwd);
    double deltaFlatShort = deltaCalculator.calcForwardSensitivity(swap1y, MARKET, relShift * fwd);

    assertTrue(Math.abs(deltaSkew) > Math.abs(deltaFlatShort));
    assertTrue(Math.abs(deltaSkew) > Math.abs(deltaFlatLong));
    assertEquals(deltaFlatLong, deltaFlatShort, TOLERATED);
    assertEquals(0.0, deltaFlatShort, TOLERATED);
  }

  /**
   * If the smile/skew translates with the forward, we always expect zero forward sensitivity.
   */
  @Test
  @SuppressWarnings({"rawtypes", "unchecked" })
  public void testForwardSensitivityForDeltaStrikeParameterisation() {

    final InterpolatedDoublesSurface DELTA_SURFACE = new InterpolatedDoublesSurface(EXPIRIES, CALLDELTAs, VOLS, new GridInterpolator2D(INTERPOLATOR_1D_LINEAR, INTERPOLATOR_1D_DBLQUAD));
    final BlackVolatilityDeltaSurface DELTA_VOL_SURFACE = new BlackVolatilityDeltaSurface(DELTA_SURFACE, true);
    final VarianceSwapDataBundle DELTA_MARKET = new VarianceSwapDataBundle(DELTA_VOL_SURFACE, FUNDING, SPOT, FORWARD);

    double fwd = DELTA_MARKET.getForwardUnderlying();
    double relShift = 0.01;

    double deltaSkew = deltaCalculator.calcForwardSensitivity(swap5y, DELTA_MARKET, relShift * fwd);
    double deltaFlatLong = deltaCalculator.calcForwardSensitivity(swap10y, DELTA_MARKET, relShift * fwd);
    double deltaFlatShort = deltaCalculator.calcForwardSensitivity(swap1y, DELTA_MARKET, relShift * fwd);

    assertEquals(0.0, deltaSkew, TOLERATED);
    assertEquals(0.0, deltaFlatLong, TOLERATED);
    assertEquals(0.0, deltaFlatShort, TOLERATED);
  }

  @Test
  public void testTotalRateSensitivity() {

    double fwd = MARKET.getForwardUnderlying();
    double relShift = 0.01;

    double delta = deltaCalculator.calcForwardSensitivity(swap5y, MARKET, relShift * fwd);
    double pv = pricer_default_w_cutoff.presentValue(swap5y, MARKET);
    double settlement = swap5y.getTimeToSettlement();

    double totalRateSens = deltaCalculator.calcDiscountRateSensitivity(swap5y, MARKET, relShift * fwd);

    assertEquals(totalRateSens, settlement * (delta * fwd - pv), TOLERATED);

  }

  @Test
  public void testDiscountRateSensitivityWithNoSkew() {

    double rateSens = deltaCalculator.calcDiscountRateSensitivity(swap10y, MARKET);
    double pv = pricer_default_w_cutoff.presentValue(swap10y, MARKET);
    double settlement = swap10y.getTimeToSettlement();

    assertEquals(-settlement * pv, rateSens, TOLERATED);
  }

  @Test
  public void testPV01() {

    double rateSens = deltaCalculator.calcDiscountRateSensitivity(swapStartsNow, MARKET);
    double pv01 = deltaCalculator.calcPV01(swapStartsNow, MARKET);

    assertEquals(pv01 * 10000, rateSens, TOLERATED);
  }

  @Test
  public void testBucketedDeltaVsPV01() {

    double rateSens = deltaCalculator.calcDiscountRateSensitivity(swapStartsNow, MARKET);
    DoubleMatrix1D deltaBuckets = deltaCalculator.calcDeltaBucketed(swapStartsNow, MARKET);
    int nDeltas = deltaBuckets.getNumberOfElements();
    int nYieldNodes = MARKET.getDiscountCurve().getCurve().size();
    assertEquals(nDeltas, nYieldNodes, TOLERATED);

    double bucketSum = 0.0;
    for (int i = 0; i < nDeltas; i++) {
      bucketSum += deltaBuckets.getEntry(i);
    }
    assertEquals(rateSens, bucketSum, TOLERATED);
  }

  @Test
  public void testBlackVegaParallel() {

    final double expiry = 0.5;
    final double sigma = SURFACE.getZValue(expiry, 100.0);
    final VarianceSwap swap = new VarianceSwap(tPlusOne, expiry, expiry, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, noObservations, noObsWeights);
    final double zcb = MARKET.getDiscountCurve().getDiscountFactor(swap.getTimeToSettlement());

    final Double vegaParallel = deltaCalculator.calcBlackVegaParallel(swap, MARKET);
    final double theoreticalVega = 2 * sigma * zcb * swap.getVarNotional();

    assertEquals(theoreticalVega, vegaParallel, 0.01);
  }

  @Test
  public void testBlackVegaForEntireSurface() {

    // Compute the surface 
    NodalDoublesSurface vegaSurface = deltaCalculator.calcBlackVegaForEntireSurface(swapStartsNow, MARKET);
    // Sum up each constituent
    double[] vegaBuckets = vegaSurface.getZDataAsPrimitive();
    double sumVegaBuckets = 0.0;
    for (int i = 0; i < vegaSurface.size(); i++) {
      sumVegaBuckets += vegaBuckets[i];
    }

    // Compute parallel vega, ie to a true parallel shift
    final Double parallelVega = deltaCalculator.calcBlackVegaParallel(swapStartsNow, MARKET);

    assertEquals(parallelVega, sumVegaBuckets, 0.01);
  }

  /**
   * Test BlackVolatilityDeltaSurface
   * sum of vega buckets = 4583.92106434809
  parallelVega = 4583.95175875458
   */
  @Test
  public void testBlackVegaForDeltaSurface() {

    @SuppressWarnings({"rawtypes", "unchecked" })
    final InterpolatedDoublesSurface DELTA_SURFACE = new InterpolatedDoublesSurface(EXPIRIES, CALLDELTAs, VOLS, new GridInterpolator2D(INTERPOLATOR_1D_LINEAR, INTERPOLATOR_1D_DBLQUAD));
    final BlackVolatilityDeltaSurface DELTA_VOL_SURFACE = new BlackVolatilityDeltaSurface(DELTA_SURFACE, true);
    final VarianceSwapDataBundle DELTA_MARKET = new VarianceSwapDataBundle(DELTA_VOL_SURFACE, FUNDING, SPOT, FORWARD);

    // Compute the surface 
    NodalDoublesSurface vegaSurface = deltaCalculator.calcBlackVegaForEntireSurface(swapStartsNow, DELTA_MARKET);
    // Sum up each constituent
    double[] vegaBuckets = vegaSurface.getZDataAsPrimitive();
    double sumVegaBuckets = 0.0;
    for (int i = 0; i < vegaSurface.size(); i++) {
      sumVegaBuckets += vegaBuckets[i];
    }

    // Compute parallel vega, ie to a true parallel shift
    final Double parallelVega = deltaCalculator.calcBlackVegaParallel(swapStartsNow, DELTA_MARKET);

    assertEquals(parallelVega, sumVegaBuckets, 0.033);
  }

  // Setup ------------------------------------------

  // The pricing method
  final VarianceSwapStaticReplication pricer_default_w_cutoff = new VarianceSwapStaticReplication(StrikeParameterization.STRIKE);
  final VarianceSwapStaticReplication pricer_without_cutoff = new VarianceSwapStaticReplication();

  // Market data
  private static final double SPOT = 80;
  private static final double FORWARD = 100;

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 0.5,
                                                          1.0, 1.0, 1.0, 1.0,
                                                          5.0, 5.0, 5.0, 5.0,
                                                          10.0, 10.0, 10.0, 10.0 };
  private static final double[] STRIKES = new double[] {40, 80, 100, 120,
                                                        40, 80, 100, 120,
                                                        40, 80, 100, 120,
                                                        40, 80, 100, 120 };
  private static final double[] CALLDELTAs = new double[] {0.9, 0.75, 0.5, 0.25,
                                                            0.9, 0.75, 0.5, 0.25,
                                                            0.9, 0.75, 0.5, 0.25,
                                                            0.9, 0.75, 0.5, 0.25 };

  private static final double[] VOLS = new double[] {0.28, 0.28, 0.28, 0.28,
                                                     0.25, 0.25, 0.25, 0.25,
                                                     0.26, 0.24, 0.23, 0.25,
                                                     0.20, 0.20, 0.20, 0.20 };

  private static final CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR_1D_DBLQUAD = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  final static CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR_1D_LINEAR = getInterpolator(Interpolator1DFactory.LINEAR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  @SuppressWarnings({"unchecked", "rawtypes" })
  private static final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, new GridInterpolator2D(INTERPOLATOR_1D_LINEAR, INTERPOLATOR_1D_DBLQUAD));
  private static final BlackVolatilityFixedStrikeSurface VOL_SURFACE = new BlackVolatilityFixedStrikeSurface(SURFACE);

  private static double[] maturities = {0.5, 1.0, 5.0, 10.0, 20.0 };
  private static double[] rates = {0.02, 0.03, 0.05, 0.05, 0.04 };
  private static final YieldCurve FUNDING = new YieldCurve(new InterpolatedDoublesCurve(maturities, rates, INTERPOLATOR_1D_DBLQUAD, true));

  private static final VarianceSwapDataBundle MARKET = new VarianceSwapDataBundle(VOL_SURFACE, FUNDING, SPOT, FORWARD);

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

  final ZonedDateTime today = ZonedDateTime.now();
  final ZonedDateTime tomorrow = today.plusDays(1);
  final double tPlusOne = TimeCalculator.getTimeBetween(today, tomorrow);

  final double[] noObservations = {};
  final double[] noObsWeights = {};
  final double[] singleObsSoNoReturn = {80 };

  final VarianceSwap swapStartsNow = new VarianceSwap(now, expiry2, expiry2, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, singleObsSoNoReturn, noObsWeights);

  final VarianceSwap swapStartsTomorrow = new VarianceSwap(tPlusOne, expiry2, expiry2, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, noObservations, noObsWeights);

  final VarianceSwap swap10y = new VarianceSwap(tPlusOne, expiry10, expiry10, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, noObservations, noObsWeights);
  final VarianceSwap swap5y = new VarianceSwap(tPlusOne, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, noObservations, noObsWeights);
  final VarianceSwap swap1y = new VarianceSwap(tPlusOne, expiry1, expiry1, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, noObservations, noObsWeights);

}
