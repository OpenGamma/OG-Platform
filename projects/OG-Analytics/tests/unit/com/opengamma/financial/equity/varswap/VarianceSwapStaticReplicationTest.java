/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap;

import static com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;

import com.opengamma.financial.equity.varswap.derivative.VarianceSwap;
import com.opengamma.financial.equity.varswap.pricing.VarSwapStaticReplication;
import com.opengamma.financial.equity.varswap.pricing.VarSwapStaticReplication.StrikeParameterisation;
import com.opengamma.financial.equity.varswap.pricing.VarianceSwapDataBundle;
import com.opengamma.financial.interestrate.TestsDataSets;
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
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;

import org.testng.annotations.Test;

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
  final VarSwapStaticReplication pricer_strike_default_w_cutoff = new VarSwapStaticReplication(StrikeParameterisation.STRIKE);
  final VarSwapStaticReplication pricer_delta_default_w_cutoff = new VarSwapStaticReplication(StrikeParameterisation.DELTA);
  final VarSwapStaticReplication pricer_delta_null_cutoff = new VarSwapStaticReplication(EPS, 1 - EPS, new RungeKuttaIntegrator1D(), null, null, null);
  final VarSwapStaticReplication pricer_strike_null_cutoff = new VarSwapStaticReplication(EPS, 10, new RungeKuttaIntegrator1D(), null, null, null);

  // Market data
  private static final double SPOT = 80;
  private static final double FORWARD = 100;
  private static final double TEST_VOL = 0.25;
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  private static final YieldAndDiscountCurve DISCOUNT = CURVES.getCurve("Funding");

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 0.5,
                                                          1.0, 1.0, 1.0, 1.0,
                                                          5.0, 5.0, 5.0, 5.0,
                                                          10.0, 10.0, 10.0, 10.0 };

  private static final double[] PUTDELTAS = new double[] {0.1, 0.25, 0.5, 0.75,
                                                        0.1, 0.25, 0.5, 0.75,
                                                        0.1, 0.25, 0.5, 0.75,
                                                        0.1, 0.25, 0.5, 0.75 };

  private static final double[] CALLDELTAS = new double[] {0.75, 0.5, 0.25, 0.1,
                                                          0.75, 0.5, 0.25, 0.1,
                                                          0.75, 0.5, 0.25, 0.1,
                                                          0.75, 0.5, 0.25, 0.1 };

  private static final double[] STRIKES = new double[] {40, 80, 100, 120,
                                                        40, 80, 100, 120,
                                                        40, 80, 100, 120,
                                                        40, 80, 100, 120 };

  private static final double[] VOLS = new double[] {0.28, 0.28, 0.28, 0.28,
                                                     0.25, 0.25, 0.25, 0.25,
                                                     0.26, 0.24, 0.23, 0.25,
                                                     0.20, 0.20, 0.20, 0.20 };

  private static final CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR_1D_STRIKE = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  final static CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR_1D_EXPIRY = getInterpolator(Interpolator1DFactory.LINEAR,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  @SuppressWarnings({"unchecked", "rawtypes" })
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
    final double testVar = pricer_delta_null_cutoff.impliedVariance(swap1, new VarianceSwapDataBundle(constVolSurf, DISCOUNT, SPOT, FORWARD));
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
  @SuppressWarnings({"unchecked", "rawtypes" })
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSurfaceWithoutStrikeExtrapolation() {
    final CombinedInterpolatorExtrapolator<Interpolator1DDataBundle> interpOnlyStrike = getInterpolator(Interpolator1DFactory.LINEAR);
    final Interpolator2D interp2D = new GridInterpolator2D(INTERPOLATOR_1D_EXPIRY, interpOnlyStrike);
    @SuppressWarnings("unused")
    final InterpolatedDoublesSurface surface = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, interp2D);
    final BlackVolatilityFixedStrikeSurface volSurface = new BlackVolatilityFixedStrikeSurface(surface);
    pricer_delta_null_cutoff.impliedVariance(swap1, new VarianceSwapDataBundle(volSurface, DISCOUNT, SPOT, FORWARD));
  }

  /**
   * Test of VolatilitySurface type
   */
  @Test
  public void testFlatSurfaceOnStrikeAndDelta() {

    final double testStrikeVar = pricer_strike_default_w_cutoff.impliedVariance(swap1, MARKET_W_STRIKESURF);
    final double targetVar = swap1.getTimeToObsEnd() * TEST_VOL * TEST_VOL;
    assertEquals(testStrikeVar, targetVar, 1e-9);

    final double testDeltaVar = pricer_delta_default_w_cutoff.impliedVariance(swap1, MARKET_W_PUTDELTASURF);

    assertEquals(testStrikeVar, testDeltaVar, 1e-9);

    final double testCallDeltaVar = pricer_delta_default_w_cutoff.impliedVariance(swap1, MARKET_W_CALLDELTASURF);

    assertEquals(testDeltaVar, testCallDeltaVar, 1e-9);
  }

  /**
   * Confirm all is well when null values are passed for the cutoff, 
   * at least when the surface can handle low strikes itself
   */
  @Test
  public void testInterpolatedDoublesSurfaceWithoutCutoff() {

    final VarSwapStaticReplication pricerNoCutoff = new VarSwapStaticReplication(1e-16, 10, new RungeKuttaIntegrator1D(), null, null, null);

    final double testVar = pricerNoCutoff.impliedVariance(swap1, MARKET_W_STRIKESURF);
    final double targetVar = expiry1 * TEST_VOL * TEST_VOL;
    assertEquals(testVar, targetVar, 1e-9);
  }

  /** 
   * Confirm error trapped when bad values are passed for the cutoff 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInterpolatedDoublesSurfaceWithPoorCutoffDescription() {

    final VarSwapStaticReplication pricerBadCutoff = new VarSwapStaticReplication(1e-16, 10, new RungeKuttaIntegrator1D(), StrikeParameterisation.DELTA, null, 0.05);
    pricerBadCutoff.impliedVariance(swap1, MARKET_W_STRIKESURF);
  }

  /**
   * Although all we get from impliedVariance(swap,market) is the variance, it is clear that the fit is the Lognormal with zero shift,
   * as the variance produced is not affected.
   */
  @Test
  public void testShiftedLognormalFitOnFlatSurface() {

    final VarSwapStaticReplication pricerHighCutoff = new VarSwapStaticReplication(1e-16, 10, new RungeKuttaIntegrator1D(), StrikeParameterisation.STRIKE, 0.95, 0.05);

    final double testVar = pricerHighCutoff.impliedVariance(swap1, MARKET_W_STRIKESURF);
    final double targetVar = expiry1 * TEST_VOL * TEST_VOL;
    assertEquals(testVar, targetVar, 1e-9);
  }

  /**
   * Confirm that we can split an integral over [a,c] into [a,b]+[b,c]
   */
  @Test
  public void testIntegrationBounds() {

    final VarSwapStaticReplication pricerPuts = new VarSwapStaticReplication(1e-16, 1.0, new RungeKuttaIntegrator1D(), StrikeParameterisation.STRIKE, 0.9, 0.01);
    final VarSwapStaticReplication pricerCalls = new VarSwapStaticReplication(1.0, 10, new RungeKuttaIntegrator1D(), null, null, null);

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
    final VarSwapStaticReplication pricer0 = new VarSwapStaticReplication(1e-4, 0.1, integrator, StrikeParameterisation.STRIKE, 0.25, 0.05);
    final VarSwapStaticReplication pricer1 = new VarSwapStaticReplication(0.1, 0.2, integrator, StrikeParameterisation.STRIKE, 0.25, 0.05);
    final VarSwapStaticReplication pricer2 = new VarSwapStaticReplication(0.2, 0.3, integrator, StrikeParameterisation.STRIKE, 0.25, 0.05);
    final VarSwapStaticReplication pricer3 = new VarSwapStaticReplication(0.3, 0.4, integrator, StrikeParameterisation.STRIKE, 0.25, 0.05);
    final VarSwapStaticReplication pricer4 = new VarSwapStaticReplication(0.4, 0.5, integrator, StrikeParameterisation.STRIKE, 0.25, 0.05);
    final VarSwapStaticReplication pricer5 = new VarSwapStaticReplication(0.5, 1.0, integrator, StrikeParameterisation.STRIKE, 0.25, 0.05);
    final VarSwapStaticReplication pricer10 = new VarSwapStaticReplication(1.0, 1.5, integrator, StrikeParameterisation.STRIKE, 0.25, 0.05);
    final VarSwapStaticReplication pricer15 = new VarSwapStaticReplication(1.5, 5., integrator, StrikeParameterisation.STRIKE, 0.25, 0.05);

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

    final VarSwapStaticReplication pricer_onego = new VarSwapStaticReplication(1e-4, 5.0, integrator, StrikeParameterisation.STRIKE, 0.25, 0.05);
    final double variance_onego = pricer_onego.impliedVariance(swap5, MARKET_W_STRIKESURF);
    System.err.println("variance = " + variance_onego);
    assertEquals(variance_total, variance_onego, 1e-9);
  }

  @Test
  public void testVInterpolatedDoublesSurfaceWITHShiftedLN() {
    final VarSwapStaticReplication pricerCutoff = new VarSwapStaticReplication(1e-12, 50, new RungeKuttaIntegrator1D(), StrikeParameterisation.STRIKE, 0.25, 0.05);
    final double varSmiley = pricerCutoff.impliedVariance(swap5, MARKET_W_STRIKESURF);
    assertEquals(0.061180009731676255, varSmiley, 1e-9);
  }

  @Test
  public void testVInterpolatedDoublesSurfaceWITHOUTShiftedLN() {

    final VarSwapStaticReplication pricerNoCutoff = new VarSwapStaticReplication(1e-12, 15, new RungeKuttaIntegrator1D(), null, null, null);
    final double varNoCut = pricerNoCutoff.impliedVariance(swap5, MARKET_W_STRIKESURF);
    assertEquals(0.06115666079281433, varNoCut, 1e-9);

    final VarSwapStaticReplication pricerDefault = new VarSwapStaticReplication();
    final double varDef = pricerDefault.impliedVariance(swap5, MARKET_W_STRIKESURF);

    assertEquals(varDef, varNoCut, 1e-9);

  }

  @Test
  public void testSmileyDeltaWithCutoff() {
    final double varSmileyFromPutSurface = pricer_delta_default_w_cutoff.impliedVariance(swap5, MARKET_W_PUTDELTASURF);
    assertEquals(0.06368727378513109, varSmileyFromPutSurface, 1e-9);

    final double varSmileyFromCallSurface = pricer_delta_default_w_cutoff.impliedVariance(swap5, MARKET_W_CALLDELTASURF);
    assertEquals(varSmileyFromPutSurface, varSmileyFromCallSurface, 1e-9);
  }

  @Test
  public void testSmileySurfaceOnDeltaAndStrike() {

    final double lowerBound = EPS;
    final double upperBound = 1.0 - EPS;

    final double[] EXPIRIES = new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
    final double[] DELTAS = new double[] {0.01, .1, .25, .5, .75, .99 };
    final boolean strikesAreCallDeltas = false;
    final double[] STRIKES = {52.05317664154072,
                              73.37631967171933,
                              87.16455321461393,
                              103.17434074991027,
                              128.06233160404426,
                              210.73392166917154 };
    final double[] VOLS = new double[] {.3, 0.27, 0.25, 0.25, 0.3, 0.3 };
    @SuppressWarnings({"unchecked" })
    final InterpolatedDoublesSurface surfDelta = new InterpolatedDoublesSurface(EXPIRIES, DELTAS, VOLS, INTERPOLATOR_2D);
    final VarianceSwapDataBundle marketDelta = new VarianceSwapDataBundle(new BlackVolatilityDeltaSurface(surfDelta, strikesAreCallDeltas), DISCOUNT, SPOT, FORWARD);
    VarSwapStaticReplication pricerDelta = new VarSwapStaticReplication(0.1, upperBound, new RungeKuttaIntegrator1D(), StrikeParameterisation.DELTA, 0.1, 0.15);
    final double varianceDelta = pricerDelta.impliedVariance(swap1, marketDelta);
    @SuppressWarnings({"unchecked" })
    final InterpolatedDoublesSurface surfStrike = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, INTERPOLATOR_2D);
    final VarianceSwapDataBundle marketStrike = new VarianceSwapDataBundle(new BlackVolatilityFixedStrikeSurface(surfStrike), DISCOUNT, SPOT, FORWARD);
    VarSwapStaticReplication pricerStrike = new VarSwapStaticReplication(0.7337631967171933, 8.63095675657578, new RungeKuttaIntegrator1D(), StrikeParameterisation.STRIKE, 0.7337631967171933,
        (0.8716455321461393 - 0.7337631967171933));
    final double varianceStrike = pricerStrike.impliedVariance(swap1, marketStrike);
    System.err.println(varianceStrike + " = varianceStrike");
    assertEquals(varianceStrike, varianceDelta, 1e-9);
  }

  /**
   * Instead of matching slope and level at the cutoff, try choosing a level at zero strike, then filling in with shifted lognormal distribution
   */
  @Test
  public void testAlternativeUseOfShiftedLognormal() {

    final double lowerBound = EPS;
    final double upperBound = 1.0 - EPS;

    final double[] EXPIRIES = new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
    final double[] DELTAS = new double[] {0, .1, .25, .5, .75, 1.00 };
    final boolean strikesAreCallDeltas = false;
    final double[] VOLS = new double[] {.4, 0.27, 0.25, 0.25, 0.3, 0.3 };

    @SuppressWarnings({"unchecked" })
    final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, DELTAS, VOLS, INTERPOLATOR_2D);

    final VarianceSwapDataBundle market = new VarianceSwapDataBundle(new BlackVolatilityDeltaSurface(SURFACE, strikesAreCallDeltas), DISCOUNT, SPOT, FORWARD);
    VarSwapStaticReplication pricerCutoff = new VarSwapStaticReplication(lowerBound, upperBound, new RungeKuttaIntegrator1D(), StrikeParameterisation.DELTA, 0.1, 0.15); // Hit 10 and 25 Put delta (90 and 75 call)

    final double variance = pricerCutoff.impliedVariance(swap1, market);
    assertEquals(variance, 0.07048720344258558, 1e-9);
  }

  @Test
  public void testAlternativeUseOfShiftedLognormal_Calls() {

    final double lowerBound = EPS;
    final double upperBound = 1.0 - EPS;

    final double[] EXPIRIES = new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                                            5.0, 5.0, 5.0, 5.0, 5.0, 5.0 };
    final double[] DELTAS = new double[] {1.0, .75, .5, .25, .1, 0,
                                          1.0, .75, .5, .25, .1, 0 };
    final boolean strikesAreCallDeltas = true;
    final double[] VOLS = new double[] {.4, 0.3, 0.25, 0.25, 0.3, 0.3,
                                        .4, 0.3, 0.25, 0.25, 0.3, 0.3 };

    final CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR_1D = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

    @SuppressWarnings({"unchecked", "rawtypes" })
    //final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, DELTAS, VOLS, new GridInterpolator2D(new LinearInterpolator1D(), INTERPOLATOR_1D));
    final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, DELTAS, VOLS, INTERPOLATOR_2D);

    final VarianceSwapDataBundle market = new VarianceSwapDataBundle(new BlackVolatilityDeltaSurface(SURFACE, strikesAreCallDeltas), DISCOUNT, SPOT, FORWARD);
    VarSwapStaticReplication pricerCutoff = new VarSwapStaticReplication(lowerBound, upperBound, new RungeKuttaIntegrator1D(), StrikeParameterisation.DELTA, 0.75, .15); // Hit 10 and 25. Check shape in between. Use this value to extrapolate left

    final double variance = pricerCutoff.impliedVariance(swap1, market);
    assertEquals(variance, 0.07048720344258558, 1e-9);
  }

  @Test
  public void testAlternativeUseOfShiftedLognormal_Strike() {

    final double lowerBound = EPS;
    final double upperBound = 10;

    final double[] EXPIRIES = new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                                            5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0 };

    final double[] STRIKES = new double[] {0, 25, 71.21508872954146, 71.38726266683607, 75, 100, 125, 150,
                                           0, 25, 71.21508872954146, 71.38726266683607, 75, 100, 125, 150 };

    final double[] VOLS = new double[] {.4, 0.3, 0.3, 0.2992711544444444, 0.25, 0.25, 0.3, 0.3,
                                        .4, 0.3, 0.3, 0.2992711544444444, 0.25, 0.25, 0.3, 0.3 };

    final CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR_1D = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

    @SuppressWarnings({"unchecked", "rawtypes" })
    final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, new GridInterpolator2D(new LinearInterpolator1D(), INTERPOLATOR_1D));

    final VarianceSwapDataBundle market = new VarianceSwapDataBundle(new BlackVolatilityFixedStrikeSurface(SURFACE), DISCOUNT, SPOT, FORWARD);
    VarSwapStaticReplication pricerCutoff = new VarSwapStaticReplication(lowerBound, upperBound, new RungeKuttaIntegrator1D(), StrikeParameterisation.STRIKE,
        .7121508872954146, (0.7138726266683607 - 0.7121508872954146)); // Hit 75 and 25. Check shape in between. Use this value to extrapolate left

    final double variance = pricerCutoff.impliedVariance(swap1, market);
    assertEquals(0.07048720344258558, variance, 1e-9);

  }

  @Test
  public void testExpiredSwap() {

    final VarSwapStaticReplication pricerCutoff = new VarSwapStaticReplication(1e-4, 5, new RungeKuttaIntegrator1D(), StrikeParameterisation.STRIKE, 0.3894, 0.05);

    final double noMoreVariance = pricerCutoff.impliedVariance(swap0, MARKET_W_STRIKESURF);
    assertEquals(0.0, noMoreVariance, 1e-9);

    final double varInExpiredSwap = pricerCutoff.impliedVariance(swap0, MARKET_W_STRIKESURF);
    assertEquals(0.0, varInExpiredSwap, 1e-9);
  }

  // impliedVolatility Tests ------------------------------------------

  @Test
  public void testImpliedVolatility() {

    VarSwapStaticReplication pricerCutoff = new VarSwapStaticReplication(1e-4, 5, new RungeKuttaIntegrator1D(), StrikeParameterisation.STRIKE, 0.3894, 0.05);

    double sigmaSquared = pricerCutoff.impliedVariance(swap5, MARKET_W_STRIKESURF);
    double sigma = pricerCutoff.impliedVolatility(swap5, MARKET_W_STRIKESURF);

    assertEquals(sigmaSquared, sigma * sigma, 1e-9);

  }
}
