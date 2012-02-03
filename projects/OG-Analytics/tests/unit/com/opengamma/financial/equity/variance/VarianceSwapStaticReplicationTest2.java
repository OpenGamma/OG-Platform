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
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication2;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplicationDelta;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplicationStrike;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedStrikeFromDeltaFunction;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceConverter;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.financial.model.volatility.surface.Delta;
import com.opengamma.financial.model.volatility.surface.Strike;
import com.opengamma.math.function.Function;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class VarianceSwapStaticReplicationTest2 {

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
  final VarianceSwapStaticReplicationStrike pricer_strike_default_w_cutoff = new VarianceSwapStaticReplicationStrike();
  final VarianceSwapStaticReplicationDelta pricer_putdelta_default_w_cutoff = new VarianceSwapStaticReplicationDelta();
  final VarianceSwapStaticReplicationDelta pricer_putdelta_null_cutoff = new VarianceSwapStaticReplicationDelta(EPS, 1 - EPS, new RungeKuttaIntegrator1D(), null, null);
  final VarianceSwapStaticReplicationStrike pricer_strike_null_cutoff = new VarianceSwapStaticReplicationStrike(EPS, 10, new RungeKuttaIntegrator1D(), null, null);

  // Market data
  private static final double SPOT = 80;
  private static final double DRIFT = 0.05;
  //  private static final double FORWARD = 100;
  private static final ForwardCurve FORWARD_CURVE = new ForwardCurve(SPOT, DRIFT);
  private static final double TEST_VOL = 0.25;
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final YieldAndDiscountCurve DISCOUNT = CURVES.getCurve("Funding");

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0, 10.0 };

  private static final double[] PUTDELTAS = new double[] {0.1, 0.25, 0.5, 0.75, 0.9, 0.1, 0.25, 0.5, 0.75, 0.9, 0.1, 0.25, 0.5, 0.75, 0.9, 0.1, 0.25, 0.5, 0.75, 0.9 };

  private static final double[] CALLDELTAS = new double[] {0.9, 0.75, 0.5, 0.25, 0.1, 0.9, 0.75, 0.5, 0.25, 0.1, 0.9, 0.75, 0.5, 0.25, 0.1, 0.9, 0.75, 0.5, 0.25, 0.1 };

  private static final double[] STRIKES = new double[] {20, 40, 80, 100, 120, 20, 40, 80, 100, 120, 20, 40, 80, 100, 120, 20, 40, 80, 100, 120 };

  private static final double[] VOLS = new double[] {0.28, 0.28, 0.28, 0.28, 0.28, 0.25, 0.25, 0.25, 0.25, 0.25, 0.27, 0.26, 0.24, 0.23, 0.25, 0.27, 0.26, 0.25, 0.26, 0.27 };

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR_1D_STRIKE = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);

  final static CombinedInterpolatorExtrapolator INTERPOLATOR_1D_EXPIRY = getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final Interpolator2D INTERPOLATOR_2D = new GridInterpolator2D(INTERPOLATOR_1D_EXPIRY, INTERPOLATOR_1D_STRIKE);
  private static final BlackVolatilitySurfaceStrike VOL_STRIKE_SURFACE = new BlackVolatilitySurfaceStrike(new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, INTERPOLATOR_2D));
  //  private static final BlackVolatilityDeltaSurface VOL_PUTDELTA_SURFACE = new BlackVolatilityDeltaSurface(new InterpolatedDoublesSurface(EXPIRIES, PUTDELTAS, VOLS, INTERPOLATOR_2D), false);
  private static final BlackVolatilitySurfaceDelta VOL_CALLDELTA_SURFACE = new BlackVolatilitySurfaceDelta(new InterpolatedDoublesSurface(EXPIRIES, CALLDELTAS, VOLS, INTERPOLATOR_2D), FORWARD_CURVE);

  private static final VarianceSwapDataBundle2<Strike> MARKET_W_STRIKESURF = new VarianceSwapDataBundle2<Strike>(VOL_STRIKE_SURFACE, DISCOUNT, FORWARD_CURVE);
  // private static final VarianceSwapDataBundle MARKET_W_PUTDELTASURF = new VarianceSwapDataBundle(VOL_PUTDELTA_SURFACE, DISCOUNT, SPOT, FORWARD);
  private static final VarianceSwapDataBundle2<Delta> MARKET_W_CALLDELTASURF = new VarianceSwapDataBundle2<Delta>(VOL_CALLDELTA_SURFACE, DISCOUNT, FORWARD_CURVE);

  // impliedVariance Tests ------------------------------------------
  /**
   * Test of VolatilitySurface type, ConstantDoublesSurface
   */
  @Test
  public void testConstantDoublesDeltaSurface() {
    final BlackVolatilitySurfaceDelta constVolSurf = new BlackVolatilitySurfaceDelta(ConstantDoublesSurface.from(TEST_VOL), FORWARD_CURVE);
    final double testVar = pricer_putdelta_null_cutoff.impliedVariance(swap1, new VarianceSwapDataBundle2<Delta>(constVolSurf, DISCOUNT, FORWARD_CURVE));
    final double targetVar = swap1.getTimeToObsEnd() * TEST_VOL * TEST_VOL;

    assertEquals(targetVar, testVar, 1e-9);
  }

  @Test
  public void testConstantDoublesStrikeSurface() {
    final BlackVolatilitySurfaceStrike constVolSurf = new BlackVolatilitySurfaceStrike(ConstantDoublesSurface.from(TEST_VOL));
    final double testVar = pricer_strike_null_cutoff.impliedVariance(swap1, new VarianceSwapDataBundle2<Strike>(constVolSurf, DISCOUNT, FORWARD_CURVE));
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
    final BlackVolatilitySurfaceStrike volSurface = new BlackVolatilitySurfaceStrike(surface);
    pricer_strike_null_cutoff.impliedVariance(swap1, new VarianceSwapDataBundle2<Strike>(volSurface, DISCOUNT, FORWARD_CURVE));
  }

  /**
   * Test of VolatilitySurface strike parameterisations: Strike vs Delta
   */
  @Test
  public void testFlatSurfaceOnStrikeAndDelta() {

    final double testStrikeVar = pricer_strike_default_w_cutoff.impliedVariance(swap1, MARKET_W_STRIKESURF);
    final double targetVar = swap1.getTimeToObsEnd() * TEST_VOL * TEST_VOL;
    assertEquals(testStrikeVar, targetVar, 1e-9);

    //    final double testDeltaVar = pricer_putdelta_default_w_cutoff.impliedVariance(swap1, MARKET_W_PUTDELTASURF);
    //    assertEquals(testStrikeVar, testDeltaVar, 1e-9);

    final VarianceSwapStaticReplication2<Delta> pricer_calldelta_default_w_cutoff = new VarianceSwapStaticReplicationDelta();
    final double testCallDeltaVar = pricer_calldelta_default_w_cutoff.impliedVariance(swap1, MARKET_W_CALLDELTASURF);
    assertEquals(targetVar, testCallDeltaVar, 1e-9);
  }

  /**
   * Tests of Strike Parameterisation with Smile.
   * Prices are very sensitive to Vol interpolation.. We ensure variance matches over flat portion of strikes,
   * and then mark to fixed levels over entire strike space
   */
  @Test
  //TODO remove test - should test proper smooth smiles
  public void testDeltaVsStrikeWithSmile() {

    // 'Equivalent' Vol Surfaces
    final double[] expiries = new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
    final double[] deltas = new double[] {0.01, .1, .24, .25, .5, .75, 0.76, 0.9, .99 };
    final double[] vols = new double[] {.3, 0.27, 0.25, 0.25, 0.25, 0.25, 0.25, 0.27, 0.3 };
    final double fwd = FORWARD_CURVE.getForward(1.0);
    int n = vols.length;
    final double[] strikes = new double[n];
    for (int i = 0; i < n; i++) {
      strikes[n - 1 - i] = BlackImpliedStrikeFromDeltaFunction.impliedStrike(deltas[i], true, fwd, 1.0, vols[i]);
    }

    final CombinedInterpolatorExtrapolator INTERP1D_STEP = getInterpolator(Interpolator1DFactory.STEP, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    final Interpolator2D INTERP2D = new GridInterpolator2D(INTERPOLATOR_1D_EXPIRY, INTERP1D_STEP);

    final BlackVolatilitySurfaceDelta volDelta = new BlackVolatilitySurfaceDelta(new InterpolatedDoublesSurface(expiries, deltas, vols, INTERP2D), FORWARD_CURVE);
    final VarianceSwapDataBundle2<Delta> marketDelta = new VarianceSwapDataBundle2<Delta>(volDelta, DISCOUNT, FORWARD_CURVE);

    final BlackVolatilitySurfaceStrike volStrike = new BlackVolatilitySurfaceStrike(new InterpolatedDoublesSurface(expiries, strikes, vols, INTERP2D));
    final VarianceSwapDataBundle2<Strike> marketStrike = new VarianceSwapDataBundle2<Strike>(volStrike, DISCOUNT, FORWARD_CURVE);

    // TEST FLAT SECTION OF VOL SURFACE
    //NB The test is between the 0.25 and 0.75 delta - The smile is flat between 0.24 and 0.76, but dSimga/dDelta terms will be picked up here
    final VarianceSwapStaticReplicationDelta pricer_delta_flat = new VarianceSwapStaticReplicationDelta(deltas[3], deltas[5], new RungeKuttaIntegrator1D(), null, null);
    final VarianceSwapStaticReplicationStrike pricer_strike_flat = new VarianceSwapStaticReplicationStrike(strikes[3] / fwd, strikes[5] / fwd, new RungeKuttaIntegrator1D(), null, null);

    final double varFlatDeltaNoCutoff = pricer_delta_flat.impliedVariance(swap1, marketDelta);
    final double varFlatStrikeNoCutoff = pricer_strike_flat.impliedVariance(swap1, marketStrike);
    assertEquals(varFlatDeltaNoCutoff, varFlatStrikeNoCutoff, 1e-9);

    // TEST ENTIRE SURFACE
    // 'Equivalent' Pricers. (No messing with shifted lognormal distributions)
    final double small = 1e-20;
    final double big = 50;
    final VarianceSwapStaticReplicationDelta pricer_delta_nocutoff = new VarianceSwapStaticReplicationDelta(small, 1 - small, new RungeKuttaIntegrator1D(), null, null);
    final VarianceSwapStaticReplicationStrike pricer_strike_nocutoff = new VarianceSwapStaticReplicationStrike(small, big, new RungeKuttaIntegrator1D(), null, null);

    final double totalVarDelta = pricer_delta_nocutoff.impliedVariance(swap1, marketDelta);
    final double totalVarStrike = pricer_strike_nocutoff.impliedVariance(swap1, marketStrike);

    //the delta and strike surfaces are both use piecewise constant interpolation - this means there are dirac delta functions coming from dSigma/dDelta
    //but these will not be picked up by the integrator - hence the low accuracy
    assertEquals(totalVarDelta, totalVarStrike, 1e-2);

    //legacy test - really, really shouldn't have 'magic' number tests
    assertEquals(0.06955150157201935, totalVarStrike, 1e-9);
  }

  /**
   * Confirm all is well when null values are passed for the cutoff,
   * at least when the surface can handle low strikes itself
   */
  @Test
  public void testInterpolatedDoublesSurfaceWithoutCutoff() {

    final VarianceSwapStaticReplicationStrike pricerNoCutoff = new VarianceSwapStaticReplicationStrike(1e-16, 10, new RungeKuttaIntegrator1D(), null, null);

    final double testVar = pricerNoCutoff.impliedVariance(swap1, MARKET_W_STRIKESURF);
    final double targetVar = expiry1 * TEST_VOL * TEST_VOL;
    assertEquals(testVar, targetVar, 1e-9);
  }

  /**
   * Although all we get from impliedVariance(swap,market) is the variance, it is clear that the fit is the Lognormal with zero shift,
   * as the variance produced is not affected.
   */
  @Test
  public void testShiftedLognormalFitOnFlatSurface() {
    final VarianceSwapStaticReplicationStrike pricerHighCutoff = new VarianceSwapStaticReplicationStrike(1e-16, 10, new RungeKuttaIntegrator1D(), new Strike(0.95), new Strike(0.05));
    final double testVar = pricerHighCutoff.impliedVariance(swap1, MARKET_W_STRIKESURF);
    final double targetVar = expiry1 * TEST_VOL * TEST_VOL;
    assertEquals(testVar, targetVar, 1e-9);
  }

  /**
   * Confirm that we can split an integral over [a,c] into [a,b]+[b,c]
   */
  @Test
  public void testIntegrationBounds() {

    final VarianceSwapStaticReplicationStrike pricerPuts = new VarianceSwapStaticReplicationStrike(1e-16, 1.0, new RungeKuttaIntegrator1D(), new Strike(0.9), new Strike(0.01));
    final VarianceSwapStaticReplicationStrike pricerCalls = new VarianceSwapStaticReplicationStrike(1.0, 10, new RungeKuttaIntegrator1D(), null, null);

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
    final double[] partition = new double[] {1e-4, 0.1, 0.2, 0.3, 0.4, 0.5, 1.0, 1.5, 5.0 };
    final int n = partition.length - 1;
    Strike lowerStrikeCutoff = new Strike(0.25);
    Strike strikeSpread = new Strike(0.05);
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
      VarianceSwapStaticReplicationStrike pricer = new VarianceSwapStaticReplicationStrike(partition[i], partition[i + 1], integrator, lowerStrikeCutoff, strikeSpread);
      sum += pricer.impliedVariance(swap5, MARKET_W_STRIKESURF);
    }

    final VarianceSwapStaticReplicationStrike pricer_onego = new VarianceSwapStaticReplicationStrike(partition[0], partition[n], integrator, lowerStrikeCutoff, strikeSpread);
    final double variance_onego = pricer_onego.impliedVariance(swap5, MARKET_W_STRIKESURF);
    assertEquals(variance_onego, sum, 1e-9);
  }

  /**
   * Test that DELTA parameterisations are robust to lowerBounds
   */
  @Test
  public void testDeltaNearZero() {

    // NoCutoff
    final double small = 1e-12;
    final VarianceSwapStaticReplicationDelta pricer_small = new VarianceSwapStaticReplicationDelta(small, 1 - small, new RungeKuttaIntegrator1D(), null, null);
    final double varSmall = pricer_small.impliedVariance(swap5, MARKET_W_CALLDELTASURF);
    assertEquals(0.06547657669594362, varSmall, 1e-9); //TODO legacy test

    final double smaller = 1e-16;
    final VarianceSwapStaticReplicationDelta pricer_smaller = new VarianceSwapStaticReplicationDelta(smaller, 1 - smaller, new RungeKuttaIntegrator1D(), null, null);
    final double varSmaller = pricer_smaller.impliedVariance(swap5, MARKET_W_CALLDELTASURF);
    assertEquals(varSmall, varSmaller, 1e-9);

    // Cutoff
    Delta cutoffDelta = new Delta(0.99); //a (call) delta near 1.0 means a strike near zero - so the cutoff is near 1.0
    Delta deltaSpread = new Delta(0.001);
    final VarianceSwapStaticReplicationDelta pricer_small_shift = new VarianceSwapStaticReplicationDelta(small, 1 - small, new RungeKuttaIntegrator1D(), cutoffDelta, deltaSpread);
    final double varSmallShift = pricer_small_shift.impliedVariance(swap5, MARKET_W_CALLDELTASURF);
    assertEquals(0.06548903771494659, varSmallShift, 1e-9);

    final VarianceSwapStaticReplicationDelta pricer_smaller_shift = new VarianceSwapStaticReplicationDelta(smaller, 1 - smaller, new RungeKuttaIntegrator1D(), cutoffDelta, deltaSpread);
    final double varSmallerShift = pricer_smaller_shift.impliedVariance(swap5, MARKET_W_CALLDELTASURF);
    assertEquals(varSmallShift, varSmallerShift, 1e-9);

    final double smallest = 1e-20;
    final VarianceSwapStaticReplicationDelta pricer_smallest_shift = new VarianceSwapStaticReplicationDelta(smallest, 1 - smallest, new RungeKuttaIntegrator1D(), cutoffDelta, deltaSpread);
    final double varSmallestShift = pricer_smallest_shift.impliedVariance(swap5, MARKET_W_CALLDELTASURF);
    assertEquals(varSmallShift, varSmallestShift, 1e-9);
  }

  /**
   * Test that an expired swap returns 0 variance
   */
  @Test
  public void testExpiredSwap() {

    final VarianceSwapStaticReplicationStrike pricerCutoff = new VarianceSwapStaticReplicationStrike(1e-4, 5, new RungeKuttaIntegrator1D(), new Strike(0.3894), new Strike(0.05));

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
    final VarianceSwapStaticReplicationStrike pricerCutoff = new VarianceSwapStaticReplicationStrike(1e-12, 50, new RungeKuttaIntegrator1D(), new Strike(0.25), new Strike(0.05));
    final double varSmiley = pricerCutoff.impliedVariance(swap5, MARKET_W_STRIKESURF);
    assertEquals(0.15136295669874641, varSmiley, 1e-9); //legacy magic number - note it differs from the original test because we use a forward curve rather than a fixed forward
    // assertEquals(0.1481446155127914, varSmiley, 1e-9);
  }

  /**
   * Test RIGHT TAIL behaviour. Note that price is sensitive to cutoff in strike space if implied vol is increasing
   * While this contains 'magic' numbers (different from the original test because we use a forward curve rather than a fixed forward), you can see great sensitivity to the right cutoff
   */
  @Test
  public void testVInterpolatedDoublesSurfaceWITHOUTShiftedLN() {

    final VarianceSwapStaticReplicationStrike pricerNoCutoff = new VarianceSwapStaticReplicationStrike(1e-20, 100, new RungeKuttaIntegrator1D(), null, null);
    final double varNoCut = pricerNoCutoff.impliedVariance(swap5, MARKET_W_STRIKESURF);
    assertEquals(0.1553629566987541, varNoCut, 1e-9);
    //assertEquals(0.1521212665693416, varNoCut, 1e-9);

    final VarianceSwapStaticReplicationStrike pricerDefault = new VarianceSwapStaticReplicationStrike(1e-20, 20, new RungeKuttaIntegrator1D(), null, null);
    final double varDef = pricerDefault.impliedVariance(swap5, MARKET_W_STRIKESURF);
    assertEquals(0.13936316584699407, varDef, 1e-9);
    //assertEquals(0.1361216093313216, varDef, 1e-9);

  }

  /**
   * Instead of matching slope and level at the cutoff, try choosing a level at zero strike, then filling in with shifted lognormal distribution
   */
  //  @Test
  //  public void testAlternativeUseOfShiftedLognormal() {
  //
  //    final double lowerBound = EPS;
  //    final double upperBound = 1.0 - EPS;
  //
  //    final double[] expiries = new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
  //    final double[] putDeltas = new double[] {0, .1, .25, .5, .75, 1.00 };
  //    boolean strikesAreCallDeltas = false;
  //    final double[] vols = new double[] {.4, 0.27, 0.25, 0.25, 0.3, 0.3 };
  //
  //    InterpolatedDoublesSurface surface = new InterpolatedDoublesSurface(expiries, putDeltas, vols, INTERPOLATOR_2D);
  //    VarianceSwapDataBundle market = new VarianceSwapDataBundle(new BlackVolatilityDeltaSurface(surface, strikesAreCallDeltas), DISCOUNT, SPOT, FORWARD);
  //    VarianceSwapStaticReplication pricerCutoff = new VarianceSwapStaticReplication(lowerBound, upperBound, new RungeKuttaIntegrator1D(), StrikeParameterization.PUTDELTA, 0.1, 0.15); // Hit 10 and 25 Put delta (90 and 75 call)
  //
  //    final double variancePutDeltas = pricerCutoff.impliedVariance(swap1, market);
  //    assertEquals(0.06728073703202365, variancePutDeltas, 1e-9);
  //
  //    // Try with Call Deltas too
  //    final double[] callDeltas = new double[] {1.0, 0.90, 0.75, 0.50, 0.25, 0.00 };
  //    strikesAreCallDeltas = true;
  //
  //    surface = new InterpolatedDoublesSurface(expiries, callDeltas, vols, INTERPOLATOR_2D);
  //    market = new VarianceSwapDataBundle(new BlackVolatilityDeltaSurface(surface, strikesAreCallDeltas), DISCOUNT, SPOT, FORWARD);
  //    pricerCutoff = new VarianceSwapStaticReplication(lowerBound, upperBound, new RungeKuttaIntegrator1D(), StrikeParameterization.CALLDELTA, 0.9, -.15); // Hit 90 and 75 call delta (10 and 25 put).
  //
  //    final double varianceCallDeltas = pricerCutoff.impliedVariance(swap1, market);
  //    assertEquals(variancePutDeltas, varianceCallDeltas, 1e-9);
  //
  //  }

  @Test
  public void testVolSurface() {

    Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... x) {
        double delta = x[1];
        return 0.2 + 0.3 * (delta - 0.4) * (delta - 0.4);
      }
    };

    BlackVolatilitySurfaceDelta surfaceDelta = new BlackVolatilitySurfaceDelta(FunctionalDoublesSurface.from(surf), FORWARD_CURVE);
    BlackVolatilitySurfaceStrike surfaceStrike = BlackVolatilitySurfaceConverter.toStrikeSurface(surfaceDelta);

    final VarianceSwapDataBundle2<Strike> marketStrike = new VarianceSwapDataBundle2<Strike>(surfaceStrike, DISCOUNT, FORWARD_CURVE);
    final VarianceSwapDataBundle2<Delta> marketDelta = new VarianceSwapDataBundle2<Delta>(surfaceDelta, DISCOUNT, FORWARD_CURVE);

    final double t = swap1.getTimeToSettlement();
    final double fwd = FORWARD_CURVE.getForward(t);
    final double small = 1e-6;
    final double big = 50;
    final double kMin = small * fwd;
    final double kMax = big * fwd;
    final double volKMin = surfaceStrike.getVolatility(t, kMin);
    final double volKMax = surfaceStrike.getVolatility(t, kMax);
    final double deltaMax = BlackFormulaRepository.delta(fwd, kMin, t, volKMin, true);
    final double deltaMin = BlackFormulaRepository.delta(fwd, kMax, t, volKMax, true);

    //    //sanity check
    //    final double volDeltaMin = surfaceDelta.getVolatilityForDelta(t, deltaMin);
    //    final double volDeltaMax = surfaceDelta.getVolatilityForDelta(t, deltaMax);

    final VarianceSwapStaticReplicationStrike pricer_strike_nocutoff = new VarianceSwapStaticReplicationStrike(small, big, new RungeKuttaIntegrator1D(), null, null);
    final VarianceSwapStaticReplicationDelta pricer_delta_nocutoff = new VarianceSwapStaticReplicationDelta(deltaMin, deltaMax, new RungeKuttaIntegrator1D(), null, null);

    final double totalVarStrike = pricer_strike_nocutoff.impliedVariance(swap1, marketStrike);
    final double totalVarDelta = pricer_delta_nocutoff.impliedVariance(swap1, marketDelta);
    assertEquals(totalVarStrike, totalVarDelta, 1e-7);

  }

  // impliedVolatility Tests ------------------------------------------

  @Test
  public void testImpliedVolatility() {

    final VarianceSwapStaticReplicationStrike pricerCutoff = new VarianceSwapStaticReplicationStrike(1e-4, 5, new RungeKuttaIntegrator1D(), new Strike(0.3894), new Strike(0.05));

    final double sigmaSquared = pricerCutoff.impliedVariance(swap5, MARKET_W_STRIKESURF);
    final double sigma = pricerCutoff.impliedVolatility(swap5, MARKET_W_STRIKESURF);

    assertEquals(sigmaSquared, sigma * sigma, 1e-9);

  }

}
