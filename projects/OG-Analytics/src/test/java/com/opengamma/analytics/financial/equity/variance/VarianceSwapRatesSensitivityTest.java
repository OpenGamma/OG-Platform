/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import static com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.equity.EquityDerivativeSensitivityCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.NodalDoublesSurface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VarianceSwapRatesSensitivityTest {

  private static final EquityDerivativeSensitivityCalculator DELTA_CAL = new EquityDerivativeSensitivityCalculator(VarianceSwapPresentValueCalculator.getInstance());
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

  private static double TOLERATED = 1.0E-8;

  /**
   * Forward sensitivity comes only from volatility skew. Let's check
   * Note the vol surface is flat at 0.5, 1 and 10 years, but skewed at 5 years.
   */
  @Test
  public void testForwardSensitivity() {

    final double relShift = 0.01;

    final double deltaSkew = DELTA_CAL.calcForwardSensitivity(swap5y, MARKET, relShift);
    final double deltaFlatLong = DELTA_CAL.calcForwardSensitivity(swap10y, MARKET, relShift);
    final double deltaFlatShort = DELTA_CAL.calcForwardSensitivity(swap1y, MARKET, relShift);

    assertTrue(Math.abs(deltaSkew) > Math.abs(deltaFlatShort));
    assertTrue(Math.abs(deltaSkew) > Math.abs(deltaFlatLong));
    assertEquals(deltaFlatLong, deltaFlatShort, TOLERATED);
    assertEquals(0.0, deltaFlatShort, TOLERATED);
  }

  /**
   * If the smile/skew translates with the forward, we always expect zero forward sensitivity.
   */
  @Test
  public void testForwardSensitivityForDeltaStrikeParameterisation() {

    final InterpolatedDoublesSurface DELTA_SURFACE = new InterpolatedDoublesSurface(EXPIRIES, CALLDELTAs, VOLS, new GridInterpolator2D(INTERPOLATOR_1D_LINEAR, INTERPOLATOR_1D_DBLQUAD));
    final BlackVolatilitySurfaceDelta DELTA_VOL_SURFACE = new BlackVolatilitySurfaceDelta(DELTA_SURFACE, FORWARD_CURVE);
    final StaticReplicationDataBundle DELTA_MARKET = new StaticReplicationDataBundle(DELTA_VOL_SURFACE, FUNDING, FORWARD_CURVE);

    final double relShift = 0.1;

    final double deltaSkew = DELTA_CAL.calcForwardSensitivity(swap5y, DELTA_MARKET, relShift);
    final double deltaFlatLong = DELTA_CAL.calcForwardSensitivity(swap10y, DELTA_MARKET, relShift);
    final double deltaFlatShort = DELTA_CAL.calcForwardSensitivity(swap1y, DELTA_MARKET, relShift);

    assertEquals(0.0, deltaSkew, TOLERATED);
    assertEquals(0.0, deltaFlatLong, TOLERATED);
    assertEquals(0.0, deltaFlatShort, TOLERATED);
  }

  @Test
  public void testTotalRateSensitivity() {

    final double relShift = 0.01;

    final double delta = DELTA_CAL.calcForwardSensitivity(swap5y, MARKET, relShift);
    final double pv = pricer_without_cutoff.presentValue(swap5y, MARKET);
    final double settlement = swap5y.getTimeToSettlement();

    final double totalRateSens = DELTA_CAL.calcDiscountRateSensitivity(swap5y, MARKET, relShift);
    final double fwd = FORWARD_CURVE.getForward(settlement);

    assertEquals(totalRateSens, settlement * (delta * fwd - pv), TOLERATED);

  }

  @Test
  public void testDiscountRateSensitivityWithNoSkew() {

    final double rateSens = DELTA_CAL.calcDiscountRateSensitivity(swap10y, MARKET);
    final double pv = pricer_without_cutoff.presentValue(swap10y, MARKET);
    final double settlement = swap10y.getTimeToSettlement();

    assertEquals(-settlement * pv, rateSens, TOLERATED);
  }

  @Test
  public void testPV01() {

    final double rateSens = DELTA_CAL.calcDiscountRateSensitivity(swapStartsNow, MARKET);
    final double pv01 = DELTA_CAL.calcPV01(swapStartsNow, MARKET);

    assertEquals(pv01 * 10000, rateSens, TOLERATED);
  }

  @Test
  public void testBucketedDeltaVsPV01() {

    final double rateSens = DELTA_CAL.calcDiscountRateSensitivity(swapStartsNow, MARKET);
    final DoubleMatrix1D deltaBuckets = DELTA_CAL.calcDeltaBucketed(swapStartsNow, MARKET);
    final int nDeltas = deltaBuckets.getNumberOfElements();
    final int nYieldNodes = ((YieldCurve) MARKET.getDiscountCurve()).getCurve().size();
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

    final Double vegaParallel = DELTA_CAL.calcBlackVegaParallel(swap, MARKET);
    final double theoreticalVega = 2 * sigma * zcb * swap.getVarNotional();

    assertEquals(theoreticalVega, vegaParallel, 0.01);
  }

  @Test
  public void testBlackVegaForEntireSurface() {

    // Compute the surface
    final NodalDoublesSurface vegaSurface = DELTA_CAL.calcBlackVegaForEntireSurface(swapStartsNow, MARKET);
    // Sum up each constituent
    final double[] vegaBuckets = vegaSurface.getZDataAsPrimitive();
    double sumVegaBuckets = 0.0;
    for (int i = 0; i < vegaSurface.size(); i++) {
      sumVegaBuckets += vegaBuckets[i];
    }

    // Compute parallel vega, ie to a true parallel shift
    final Double parallelVega = DELTA_CAL.calcBlackVegaParallel(swapStartsNow, MARKET);

    assertEquals(parallelVega, sumVegaBuckets, 0.01);
  }

  /**
   * Test BlackVolatilityDeltaSurface
   * sum of vega buckets = 4583.92106434809
  parallelVega = 4583.95175875458
   */
  @Test
  public void testBlackVegaForDeltaSurface() {

    final InterpolatedDoublesSurface DELTA_SURFACE = new InterpolatedDoublesSurface(EXPIRIES, CALLDELTAs, VOLS, new GridInterpolator2D(INTERPOLATOR_1D_LINEAR, INTERPOLATOR_1D_DBLQUAD));
    final BlackVolatilitySurfaceDelta DELTA_VOL_SURFACE = new BlackVolatilitySurfaceDelta(DELTA_SURFACE, FORWARD_CURVE);
    final StaticReplicationDataBundle DELTA_MARKET = new StaticReplicationDataBundle(DELTA_VOL_SURFACE, FUNDING, FORWARD_CURVE);

    // Compute the surface
    final NodalDoublesSurface vegaSurface = DELTA_CAL.calcBlackVegaForEntireSurface(swapStartsNow, DELTA_MARKET);
    // Sum up each constituent
    final double[] vegaBuckets = vegaSurface.getZDataAsPrimitive();
    double sumVegaBuckets = 0.0;
    for (int i = 0; i < vegaSurface.size(); i++) {
      sumVegaBuckets += vegaBuckets[i];
    }

    // Compute parallel vega, ie to a true parallel shift
    final Double parallelVega = DELTA_CAL.calcBlackVegaParallel(swapStartsNow, DELTA_MARKET);

    assertEquals(parallelVega, sumVegaBuckets, 0.033);
  }

  // Setup ------------------------------------------

  // The pricing method
  //  final VarianceSwapStaticReplication pricer_default_w_cutoff = new VarianceSwapStaticReplication(StrikeParameterization.STRIKE);
  final VarianceSwapStaticReplication pricer_without_cutoff = new VarianceSwapStaticReplication();

  // Market data
  private static final double SPOT = 80;
  private static final double DRIFT = 0.03;
  private static final ForwardCurve FORWARD_CURVE = new ForwardCurve(SPOT, DRIFT);
  // private static final double FORWARD = 100;

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 0.5, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0 };
  private static final double[] STRIKES = new double[] {40, 80, 100, 120, 40, 80, 100, 120, 40, 80, 100, 120, 40, 80, 100, 120 };
  private static final double[] CALLDELTAs = new double[] {0.9, 0.75, 0.5, 0.25, 0.9, 0.75, 0.5, 0.25, 0.9, 0.75, 0.5, 0.25, 0.9, 0.75, 0.5, 0.25 };

  private static final double[] VOLS = new double[] {0.28, 0.28, 0.28, 0.28, 0.25, 0.25, 0.25, 0.25, 0.26, 0.24, 0.23, 0.25, 0.20, 0.20, 0.20, 0.20 };

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR_1D_DBLQUAD = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  final static CombinedInterpolatorExtrapolator INTERPOLATOR_1D_LINEAR = getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, new GridInterpolator2D(INTERPOLATOR_1D_LINEAR, INTERPOLATOR_1D_DBLQUAD));
  private static final BlackVolatilitySurfaceStrike VOL_SURFACE = new BlackVolatilitySurfaceStrike(SURFACE);

  private static double[] maturities = {0.5, 1.0, 5.0, 10.0, 20.0 };
  private static double[] rates = {0.02, 0.03, 0.05, 0.05, 0.04 };
  private static final YieldCurve FUNDING = YieldCurve.from(new InterpolatedDoublesCurve(maturities, rates, INTERPOLATOR_1D_DBLQUAD, true));

  private static final StaticReplicationDataBundle MARKET = new StaticReplicationDataBundle(VOL_SURFACE, FUNDING, FORWARD_CURVE);

  // The derivative
  private static final double varStrike = 0.05;
  private static final double varNotional = 10000; // A notional of 10000 means PV is in bp
  private static final double now = 0;
  private static final double expiry1 = 1;
  private static final double expiry2 = 2;
  private static final double expiry5 = 5;
  private static final double expiry10 = 10;
  private static final int nObsExpected = 750;
  private static final int noObsDisrupted = 0;
  private static final double annualization = 252;

  private static final ZonedDateTime today = ZonedDateTime.now();
  private static final ZonedDateTime tomorrow = today.plusDays(1);
  private static final double tPlusOne = TimeCalculator.getTimeBetween(today, tomorrow);

  private static final double[] noObservations = {};
  private static final double[] noObsWeights = {};
  private static final double[] singleObsSoNoReturn = {80 };

  private static final VarianceSwap swapStartsNow = new VarianceSwap(now, expiry2, expiry2, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, singleObsSoNoReturn, noObsWeights);

  //private static final VarianceSwap swapStartsTomorrow = new VarianceSwap(tPlusOne, expiry2, expiry2, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, noObservations, noObsWeights);

  private static final VarianceSwap swap10y = new VarianceSwap(tPlusOne, expiry10, expiry10, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, noObservations, noObsWeights);
  private static final VarianceSwap swap5y = new VarianceSwap(tPlusOne, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, noObservations, noObsWeights);
  private static final VarianceSwap swap1y = new VarianceSwap(tPlusOne, expiry1, expiry1, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, noObsDisrupted, noObservations, noObsWeights);

}
