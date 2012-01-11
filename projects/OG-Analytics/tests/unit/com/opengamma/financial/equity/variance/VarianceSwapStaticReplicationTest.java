/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance;

import static com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication.StrikeParameterization;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.BlackVolatilityDeltaSurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilityFixedStrikeSurface;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;

/**
 *    
 */
public class VarianceSwapStaticReplicationTest {

  // Setup ------------------------------------------

  // The derivative
  final double varStrike = 0.05;
  final double varNotional = 3150;
  final double now = 0;
  final double aYearAgo = -1;
  final double expiry1 = 1;
  final double expiry2 = 2;
  final double expiry5 = 5;
  final double expiry10 = 10;
  final int nObsExpected = 750;
  final int nObsDisrupted = 0;
  final double annualization = 252;

  final double[] observations = {};
  final double[] obsWeights = {};

  final VarianceSwap swap0 = new VarianceSwap(aYearAgo, now, now, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  final VarianceSwap swap1 = new VarianceSwap(now, expiry1, expiry1, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  final VarianceSwap swap2 = new VarianceSwap(now, expiry2, expiry2, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  final VarianceSwap swap5 = new VarianceSwap(now, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  final VarianceSwap swap10 = new VarianceSwap(now, expiry10, expiry10, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  final VarianceSwap swapExpired = new VarianceSwap(now, now - 1, now - 1, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);

  // The pricing method
  final double EPS = 1.0e-12;
  final VarianceSwapStaticReplication pricer_strike_default_w_cutoff = new VarianceSwapStaticReplication(StrikeParameterization.STRIKE);
  final VarianceSwapStaticReplication pricer_putdelta_default_w_cutoff = new VarianceSwapStaticReplication(StrikeParameterization.PUTDELTA);
  final VarianceSwapStaticReplication pricer_putdelta_null_cutoff = new VarianceSwapStaticReplication(EPS, 1 - EPS, new RungeKuttaIntegrator1D(), null, null, null);
  final VarianceSwapStaticReplication pricer_strike_null_cutoff = new VarianceSwapStaticReplication(EPS, 10, new RungeKuttaIntegrator1D(), null, null, null);

  // Market data
  private static final double SPOT = 80;
  private static final double FORWARD = 100;
  private static final double TEST_VOL = 0.25;
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final YieldAndDiscountCurve DISCOUNT = CURVES.getCurve("Funding");

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0, 10.0};

  private static final double[] PUTDELTAS = new double[] {0.1, 0.25, 0.5, 0.75, 0.9, 0.1, 0.25, 0.5, 0.75, 0.9, 0.1, 0.25, 0.5, 0.75, 0.9, 0.1, 0.25, 0.5, 0.75, 0.9};

  private static final double[] CALLDELTAS = new double[] {0.9, 0.75, 0.5, 0.25, 0.1, 0.9, 0.75, 0.5, 0.25, 0.1, 0.9, 0.75, 0.5, 0.25, 0.1, 0.9, 0.75, 0.5, 0.25, 0.1};

  private static final double[] STRIKES = new double[] {20, 40, 80, 100, 120, 20, 40, 80, 100, 120, 20, 40, 80, 100, 120, 20, 40, 80, 100, 120};

  private static final double[] VOLS = new double[] {0.28, 0.28, 0.28, 0.28, 0.28, 0.25, 0.25, 0.25, 0.25, 0.25, 0.27, 0.26, 0.24, 0.23, 0.25, 0.27, 0.26, 0.25, 0.26, 0.27};

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR_1D_STRIKE = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);

  final static CombinedInterpolatorExtrapolator INTERPOLATOR_1D_EXPIRY = getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final Interpolator2D INTERPOLATOR_2D = new GridInterpolator2D(INTERPOLATOR_1D_EXPIRY, INTERPOLATOR_1D_STRIKE);
  private static final BlackVolatilityFixedStrikeSurface VOL_STRIKE_SURFACE = new BlackVolatilityFixedStrikeSurface(new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, INTERPOLATOR_2D));
  private static final BlackVolatilityDeltaSurface VOL_PUTDELTA_SURFACE = new BlackVolatilityDeltaSurface(new InterpolatedDoublesSurface(EXPIRIES, PUTDELTAS, VOLS, INTERPOLATOR_2D), false);
  private static final BlackVolatilityDeltaSurface VOL_CALLDELTA_SURFACE = new BlackVolatilityDeltaSurface(new InterpolatedDoublesSurface(EXPIRIES, CALLDELTAS, VOLS, INTERPOLATOR_2D), true);
  private static final VarianceSwapDataBundle MARKET_W_STRIKESURF = new VarianceSwapDataBundle(VOL_STRIKE_SURFACE, DISCOUNT, SPOT, FORWARD);
  private static final VarianceSwapDataBundle MARKET_W_PUTDELTASURF = new VarianceSwapDataBundle(VOL_PUTDELTA_SURFACE, DISCOUNT, SPOT, FORWARD);
  private static final VarianceSwapDataBundle MARKET_W_CALLDELTASURF = new VarianceSwapDataBundle(VOL_CALLDELTA_SURFACE, DISCOUNT, SPOT, FORWARD);

  // impliedVariance Tests ------------------------------------------
  /**
   * Test of VolatilitySurface type, ConstantDoublesSurface
   */
  @Test
  public void testConstantDoublesDeltaSurface() {

    final ConstantDoublesSurface constSurf = ConstantDoublesSurface.from(TEST_VOL);
    final BlackVolatilityDeltaSurface constVolSurf = new BlackVolatilityDeltaSurface(constSurf);
    final double testVar = pricer_putdelta_null_cutoff.impliedVariance(swap1, new VarianceSwapDataBundle(constVolSurf, DISCOUNT, SPOT, FORWARD));
    final double targetVar = swap1.getTimeToObsEnd() * TEST_VOL * TEST_VOL;

    assertEquals(targetVar, testVar, 1e-9);
  }

  @Test
  public void testConstantDoublesStrikeSurface() {

    final ConstantDoublesSurface constSurf = ConstantDoublesSurface.from(TEST_VOL);
    final BlackVolatilityFixedStrikeSurface constVolSurf = new BlackVolatilityFixedStrikeSurface(constSurf);
    final double testVar = pricer_strike_null_cutoff.impliedVariance(swap1, new VarianceSwapDataBundle(constVolSurf, DISCOUNT, SPOT, FORWARD));
    final double targetVar = swap1.getTimeToObsEnd() * TEST_VOL * TEST_VOL;
    assertEquals(testVar, targetVar, 1e-9);
  }

  /**
   * Test of VolatilitySurface that doesn't permit extrapolation in strike dimension
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSurfaceWithoutStrikeExtrapolation() {
    final CombinedInterpolatorExtrapolator interpOnlyStrike = getInterpolator(Interpolator1DFactory.LINEAR);
    final Interpolator2D interp2D = new GridInterpolator2D(INTERPOLATOR_1D_EXPIRY, interpOnlyStrike);
    final InterpolatedDoublesSurface surface = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, interp2D);
    final BlackVolatilityFixedStrikeSurface volSurface = new BlackVolatilityFixedStrikeSurface(surface);
    pricer_putdelta_null_cutoff.impliedVariance(swap1, new VarianceSwapDataBundle(volSurface, DISCOUNT, SPOT, FORWARD));
  }

  /**
   * Test of VolatilitySurface strike parameterisations: Strike vs Delta
   * As the relation is not linear, and the variance is highly sensitive to interpolation,
   * the only sensible test is on a flat surface 
   */
  @Test
  public void testFlatSurfaceOnStrikeAndDelta() {

    final double testStrikeVar = pricer_strike_default_w_cutoff.impliedVariance(swap1, MARKET_W_STRIKESURF);
    final double targetVar = swap1.getTimeToObsEnd() * TEST_VOL * TEST_VOL;
    assertEquals(testStrikeVar, targetVar, 1e-9);

    final double testDeltaVar = pricer_putdelta_default_w_cutoff.impliedVariance(swap1, MARKET_W_PUTDELTASURF);

    assertEquals(testStrikeVar, testDeltaVar, 1e-9);

    final VarianceSwapStaticReplication pricer_calldelta_default_w_cutoff = new VarianceSwapStaticReplication(StrikeParameterization.CALLDELTA);
    final double testCallDeltaVar = pricer_calldelta_default_w_cutoff.impliedVariance(swap1, MARKET_W_CALLDELTASURF);

    assertEquals(testDeltaVar, testCallDeltaVar, 1e-9);
  }

  /**
   * Tests of Strike Parameterisation with Smile.
   * Prices are very sensitive to Vol interpolation.. We ensure variance matches over flat portion of strikes,
   * and then mark to fixed levels over entire strike space
   */
  @Test
  public void testDeltaVsStrikeWithSmile() {

    // 'Equivalent' Vol Surfaces
    final double[] expiries = new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
    final double[] deltas = new double[] {0.01, .1, .25, .5, .75, 0.9, .99};
    final double[] vols = new double[] {.3, 0.27, 0.25, 0.25, 0.25, 0.27, 0.3};
    // !!! Note. If you change deltas or vols, you will have to change the corresponding strikes, computed via BlackFormula
    final double[] strikes = new double[] {52.0531766415, 73.3763196717, 87.1645532146, 103.1743407499, 122.1246962968, 146.5899317452, 210.203171890956};

    final CombinedInterpolatorExtrapolator INTERP1D_STEP = getInterpolator(Interpolator1DFactory.STEP, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    final Interpolator2D INTERP2D = new GridInterpolator2D(INTERPOLATOR_1D_EXPIRY, INTERP1D_STEP);

    final BlackVolatilityDeltaSurface volDelta = new BlackVolatilityDeltaSurface(new InterpolatedDoublesSurface(expiries, deltas, vols, INTERP2D), false);
    final VarianceSwapDataBundle marketDelta = new VarianceSwapDataBundle(volDelta, DISCOUNT, SPOT, FORWARD);

    final BlackVolatilityFixedStrikeSurface volStrike = new BlackVolatilityFixedStrikeSurface(new InterpolatedDoublesSurface(expiries, strikes, vols, INTERP2D));
    final VarianceSwapDataBundle marketStrike = new VarianceSwapDataBundle(volStrike, DISCOUNT, SPOT, FORWARD);

    // TEST FLAT SECTION OF VOL SURFACE
    final VarianceSwapStaticReplication pricer_delta_flat = new VarianceSwapStaticReplication(deltas[2], deltas[4], new RungeKuttaIntegrator1D(), null, null, null);
    final VarianceSwapStaticReplication pricer_strike_flat = new VarianceSwapStaticReplication(strikes[2] / FORWARD, strikes[4] / FORWARD, new RungeKuttaIntegrator1D(), null, null, null);

    final double varFlatDeltaNoCutoff = pricer_delta_flat.impliedVariance(swap1, marketDelta);
    final double varFlatStrikeNoCutoff = pricer_strike_flat.impliedVariance(swap1, marketStrike);
    assertEquals(varFlatDeltaNoCutoff, varFlatStrikeNoCutoff, 1e-9);

    // TEST ENTIRE SURFACE
    // 'Equivalent' Pricers. (No messing with shifted lognormal distributions)
    final double small = 1e-20;
    final double big = 50;
    final VarianceSwapStaticReplication pricer_delta_nocutoff = new VarianceSwapStaticReplication(small, 1 - small, new RungeKuttaIntegrator1D(), null, null, null);
    final VarianceSwapStaticReplication pricer_strike_nocutoff = new VarianceSwapStaticReplication(small, big, new RungeKuttaIntegrator1D(), null, null, null);

    final double totalVarDelta = pricer_delta_nocutoff.impliedVariance(swap1, marketDelta);
    assertEquals(0.06681872990093872, totalVarDelta, 1e-9);

    final double totalVarStrike = pricer_strike_nocutoff.impliedVariance(swap1, marketStrike);
    assertEquals(0.06966798235713362, totalVarStrike, 1e-9);
  }

  /**
   * Confirm all is well when null values are passed for the cutoff, 
   * at least when the surface can handle low strikes itself
   */
  @Test
  public void testInterpolatedDoublesSurfaceWithoutCutoff() {

    final VarianceSwapStaticReplication pricerNoCutoff = new VarianceSwapStaticReplication(1e-16, 10, new RungeKuttaIntegrator1D(), null, null, null);

    final double testVar = pricerNoCutoff.impliedVariance(swap1, MARKET_W_STRIKESURF);
    final double targetVar = expiry1 * TEST_VOL * TEST_VOL;
    assertEquals(testVar, targetVar, 1e-9);
  }

  /** 
   * Confirm error trapped when bad values are passed for the cutoff 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInterpolatedDoublesSurfaceWithPoorCutoffDescription() {

    final VarianceSwapStaticReplication pricerBadCutoff = new VarianceSwapStaticReplication(1e-16, 10, new RungeKuttaIntegrator1D(), StrikeParameterization.PUTDELTA, null, 0.05);
    pricerBadCutoff.impliedVariance(swap1, MARKET_W_STRIKESURF);
  }

  /**
   * Although all we get from impliedVariance(swap,market) is the variance, it is clear that the fit is the Lognormal with zero shift,
   * as the variance produced is not affected.
   */
  @Test
  public void testShiftedLognormalFitOnFlatSurface() {

    final VarianceSwapStaticReplication pricerHighCutoff = new VarianceSwapStaticReplication(1e-16, 10, new RungeKuttaIntegrator1D(), StrikeParameterization.STRIKE, 0.95, 0.05);

    final double testVar = pricerHighCutoff.impliedVariance(swap1, MARKET_W_STRIKESURF);
    final double targetVar = expiry1 * TEST_VOL * TEST_VOL;
    assertEquals(testVar, targetVar, 1e-9);
  }

  /**
   * Confirm that we can split an integral over [a,c] into [a,b]+[b,c]
   */
  @Test
  public void testIntegrationBounds() {

    final VarianceSwapStaticReplication pricerPuts = new VarianceSwapStaticReplication(1e-16, 1.0, new RungeKuttaIntegrator1D(), StrikeParameterization.STRIKE, 0.9, 0.01);
    final VarianceSwapStaticReplication pricerCalls = new VarianceSwapStaticReplication(1.0, 10, new RungeKuttaIntegrator1D(), null, null, null);

    final double varFromPuts = pricerPuts.impliedVariance(swap1, MARKET_W_STRIKESURF);
    final double varFromCalls = pricerCalls.impliedVariance(swap1, MARKET_W_STRIKESURF);
    final double testVar = varFromPuts + varFromCalls;

    final double targetVar = expiry1 * TEST_VOL * TEST_VOL;
    assertEquals(testVar, targetVar, 1e-9);
  }

  /**
   * Examine the contribution to the price of the low struck options.
   * Ensuring computing the variance contributions is the same when integral is cut into pieces
   */
  @Test
  public void testContributionByStrike() {

    final Integrator1D<Double, Double> integrator = new RungeKuttaIntegrator1D();
    final VarianceSwapStaticReplication pricer0 = new VarianceSwapStaticReplication(1e-4, 0.1, integrator, StrikeParameterization.STRIKE, 0.25, 0.05);
    final VarianceSwapStaticReplication pricer1 = new VarianceSwapStaticReplication(0.1, 0.2, integrator, StrikeParameterization.STRIKE, 0.25, 0.05);
    final VarianceSwapStaticReplication pricer2 = new VarianceSwapStaticReplication(0.2, 0.3, integrator, StrikeParameterization.STRIKE, 0.25, 0.05);
    final VarianceSwapStaticReplication pricer3 = new VarianceSwapStaticReplication(0.3, 0.4, integrator, StrikeParameterization.STRIKE, 0.25, 0.05);
    final VarianceSwapStaticReplication pricer4 = new VarianceSwapStaticReplication(0.4, 0.5, integrator, StrikeParameterization.STRIKE, 0.25, 0.05);
    final VarianceSwapStaticReplication pricer5 = new VarianceSwapStaticReplication(0.5, 1.0, integrator, StrikeParameterization.STRIKE, 0.25, 0.05);
    final VarianceSwapStaticReplication pricer10 = new VarianceSwapStaticReplication(1.0, 1.5, integrator, StrikeParameterization.STRIKE, 0.25, 0.05);
    final VarianceSwapStaticReplication pricer15 = new VarianceSwapStaticReplication(1.5, 5., integrator, StrikeParameterization.STRIKE, 0.25, 0.05);

    double variance_total = 0.0;
    double variance_slice;
    variance_slice = pricer0.impliedVariance(swap5, MARKET_W_STRIKESURF);
    variance_total += variance_slice;
    variance_slice = pricer1.impliedVariance(swap5, MARKET_W_STRIKESURF);
    variance_total += variance_slice;
    variance_slice = pricer2.impliedVariance(swap5, MARKET_W_STRIKESURF);
    variance_total += variance_slice;
    variance_slice = pricer3.impliedVariance(swap5, MARKET_W_STRIKESURF);
    variance_total += variance_slice;
    variance_slice = pricer4.impliedVariance(swap5, MARKET_W_STRIKESURF);
    variance_total += variance_slice;
    variance_slice = pricer5.impliedVariance(swap5, MARKET_W_STRIKESURF);
    variance_total += variance_slice;
    variance_slice = pricer10.impliedVariance(swap5, MARKET_W_STRIKESURF);
    variance_total += variance_slice;
    variance_slice = pricer15.impliedVariance(swap5, MARKET_W_STRIKESURF);
    variance_total += variance_slice;

    final VarianceSwapStaticReplication pricer_onego = new VarianceSwapStaticReplication(1e-4, 5.0, integrator, StrikeParameterization.STRIKE, 0.25, 0.05);
    final double variance_onego = pricer_onego.impliedVariance(swap5, MARKET_W_STRIKESURF);
    assertEquals(variance_total, variance_onego, 1e-9);
  }

  @Test
  public void testCallDeltaConstruction() {
    @SuppressWarnings({"unused", "hiding"})
    final VarianceSwapStaticReplication pricer_putdelta_default_w_cutoff = new VarianceSwapStaticReplication(StrikeParameterization.CALLDELTA);
  }

  /**
   * Tests that equivalent vol surfaces parameterised as either call or put deltas match
   * For convenience, and that only, the setup here requires a symmetrical vol surface 
   */
  @Test
  public void testSmileyDeltaCallAndPutWithCutoff() {
    final double varSmileyFromPutSurface = pricer_putdelta_default_w_cutoff.impliedVariance(swap5, MARKET_W_PUTDELTASURF);
    assertEquals(0.06652123567708423, varSmileyFromPutSurface, 1e-9);

    final VarianceSwapStaticReplication pricer_calldelta_default = new VarianceSwapStaticReplication(StrikeParameterization.CALLDELTA);
    final double varSmileyFromCallSurface = pricer_calldelta_default.impliedVariance(swap5, MARKET_W_CALLDELTASURF);
    assertEquals(varSmileyFromPutSurface, varSmileyFromCallSurface, 1e-9);
  }

  /**
   * Tests that equivalent vol surfaces parameterised as either call or put deltas match
   * For convenience, and that only, the setup here requires a symmetrical vol surface 
   */
  @Test
  public void testSmileyDeltaCallAndPutWithNoCutoff() {

    final VarianceSwapStaticReplication pricer__noshift = new VarianceSwapStaticReplication(EPS, 1 - EPS, new RungeKuttaIntegrator1D(), null, null, null);

    final double varSmileyFromPutSurface = pricer__noshift.impliedVariance(swap5, MARKET_W_PUTDELTASURF);
    assertEquals(0.0657060830502365, varSmileyFromPutSurface, 1e-9);

    final double varSmileyFromCallSurface = pricer__noshift.impliedVariance(swap5, MARKET_W_CALLDELTASURF);
    assertEquals(varSmileyFromPutSurface, varSmileyFromCallSurface, 1e-9);
  }

  /**
   * Test that DELTA parameterisations are robust to lowerBounds
   */
  @Test
  public void testDeltaNearZero() {

    // NoCutoff
    final double small = 1e-12;
    final VarianceSwapStaticReplication pricer_small = new VarianceSwapStaticReplication(small, 1 - small, new RungeKuttaIntegrator1D(), null, null, null);
    final double varSmall = pricer_small.impliedVariance(swap5, MARKET_W_PUTDELTASURF);
    assertEquals(0.0657060830502365, varSmall, 1e-9);

    final double smaller = 1e-16;
    final VarianceSwapStaticReplication pricer_smaller = new VarianceSwapStaticReplication(smaller, 1 - smaller, new RungeKuttaIntegrator1D(), null, null, null);
    final double varSmaller = pricer_smaller.impliedVariance(swap5, MARKET_W_PUTDELTASURF);
    assertEquals(varSmall, varSmaller, 1e-9);

    // Cutoff
    final VarianceSwapStaticReplication pricer_small_shift = new VarianceSwapStaticReplication(small, 1 - small, new RungeKuttaIntegrator1D(), StrikeParameterization.PUTDELTA, 0.01, 0.001);
    final double varSmallShift = pricer_small_shift.impliedVariance(swap5, MARKET_W_PUTDELTASURF);
    assertEquals(0.06571851926626246, varSmallShift, 1e-9);

    final VarianceSwapStaticReplication pricer_smaller_shift = new VarianceSwapStaticReplication(smaller, 1 - smaller, new RungeKuttaIntegrator1D(), StrikeParameterization.PUTDELTA, 0.01, 0.001);
    final double varSmallerShift = pricer_smaller_shift.impliedVariance(swap5, MARKET_W_PUTDELTASURF);
    assertEquals(varSmallShift, varSmallerShift, 1e-9);

    final double smallest = 1e-20;
    final VarianceSwapStaticReplication pricer_smallest_shift = new VarianceSwapStaticReplication(smallest, 1 - smallest, new RungeKuttaIntegrator1D(), StrikeParameterization.PUTDELTA, 0.01, 0.001);
    final double varSmallestShift = pricer_smallest_shift.impliedVariance(swap5, MARKET_W_PUTDELTASURF);
    assertEquals(varSmallShift, varSmallestShift, 1e-9);

  }

  /**
   * Test that an expired swap returns 0 variance
   */
  @Test
  public void testExpiredSwap() {

    final VarianceSwapStaticReplication pricerCutoff = new VarianceSwapStaticReplication(1e-4, 5, new RungeKuttaIntegrator1D(), StrikeParameterization.STRIKE, 0.3894, 0.05);

    final double noMoreVariance = pricerCutoff.impliedVariance(swap0, MARKET_W_STRIKESURF);
    assertEquals(0.0, noMoreVariance, 1e-9);

    final double varInExpiredSwap = pricerCutoff.impliedVariance(swap0, MARKET_W_STRIKESURF);
    assertEquals(0.0, varInExpiredSwap, 1e-9);
  }

  /**
   * Test RIGHT TAIL behaviour. Note that price is sensitive to cutoff in strike space if implied vol is increasing
   */
  @Test
  public void testVInterpolatedDoublesSurfaceWITHShiftedLN() {
    final VarianceSwapStaticReplication pricerCutoff = new VarianceSwapStaticReplication(1e-12, 50, new RungeKuttaIntegrator1D(), StrikeParameterization.STRIKE, 0.25, 0.05);
    final double varSmiley = pricerCutoff.impliedVariance(swap5, MARKET_W_STRIKESURF);
    assertEquals(0.1481446155127914, varSmiley, 1e-9);
  }

  /**
   * Test RIGHT TAIL behaviour. Note that price is sensitive to cutoff in strike space if implied vol is increasing
   */
  @Test
  public void testVInterpolatedDoublesSurfaceWITHOUTShiftedLN() {

    final VarianceSwapStaticReplication pricerNoCutoff = new VarianceSwapStaticReplication(1e-20, 100, new RungeKuttaIntegrator1D(), null, null, null);
    final double varNoCut = pricerNoCutoff.impliedVariance(swap5, MARKET_W_STRIKESURF);
    assertEquals(0.1521212665693416, varNoCut, 1e-9);

    final VarianceSwapStaticReplication pricerDefault = new VarianceSwapStaticReplication();
    final double varDef = pricerDefault.impliedVariance(swap5, MARKET_W_STRIKESURF);
    assertEquals(0.1361216093313216, varDef, 1e-9);

  }

  /**
   * Instead of matching slope and level at the cutoff, try choosing a level at zero strike, then filling in with shifted lognormal distribution
   */
  @Test
  public void testAlternativeUseOfShiftedLognormal() {

    final double lowerBound = EPS;
    final double upperBound = 1.0 - EPS;

    final double[] expiries = new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
    final double[] putDeltas = new double[] {0, .1, .25, .5, .75, 1.00};
    boolean strikesAreCallDeltas = false;
    final double[] vols = new double[] {.4, 0.27, 0.25, 0.25, 0.3, 0.3};

    InterpolatedDoublesSurface surface = new InterpolatedDoublesSurface(expiries, putDeltas, vols, INTERPOLATOR_2D);
    VarianceSwapDataBundle market = new VarianceSwapDataBundle(new BlackVolatilityDeltaSurface(surface, strikesAreCallDeltas), DISCOUNT, SPOT, FORWARD);
    VarianceSwapStaticReplication pricerCutoff = new VarianceSwapStaticReplication(lowerBound, upperBound, new RungeKuttaIntegrator1D(), StrikeParameterization.PUTDELTA, 0.1, 0.15); // Hit 10 and 25 Put delta (90 and 75 call)

    final double variancePutDeltas = pricerCutoff.impliedVariance(swap1, market);
    assertEquals(0.06728073703202365, variancePutDeltas, 1e-9);

    // Try with Call Deltas too
    final double[] callDeltas = new double[] {1.0, 0.90, 0.75, 0.50, 0.25, 0.00};
    strikesAreCallDeltas = true;

    surface = new InterpolatedDoublesSurface(expiries, callDeltas, vols, INTERPOLATOR_2D);
    market = new VarianceSwapDataBundle(new BlackVolatilityDeltaSurface(surface, strikesAreCallDeltas), DISCOUNT, SPOT, FORWARD);
    pricerCutoff = new VarianceSwapStaticReplication(lowerBound, upperBound, new RungeKuttaIntegrator1D(), StrikeParameterization.CALLDELTA, 0.9, -.15); // Hit 90 and 75 call delta (10 and 25 put).

    final double varianceCallDeltas = pricerCutoff.impliedVariance(swap1, market);
    assertEquals(variancePutDeltas, varianceCallDeltas, 1e-9);

  }

  // impliedVolatility Tests ------------------------------------------

  @Test
  public void testImpliedVolatility() {

    final VarianceSwapStaticReplication pricerCutoff = new VarianceSwapStaticReplication(1e-4, 5, new RungeKuttaIntegrator1D(), StrikeParameterization.STRIKE, 0.3894, 0.05);

    final double sigmaSquared = pricerCutoff.impliedVariance(swap5, MARKET_W_STRIKESURF);
    final double sigma = pricerCutoff.impliedVolatility(swap5, MARKET_W_STRIKESURF);

    assertEquals(sigmaSquared, sigma * sigma, 1e-9);

  }

  // Failing Tests ------------------------------------------

}
