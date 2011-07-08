/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap;

import static com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.equity.varswap.derivative.VarianceSwap;
import com.opengamma.financial.equity.varswap.pricing.VarSwapStaticReplication;
import com.opengamma.financial.equity.varswap.pricing.VarianceSwapDataBundle;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;

/**
 *    
 */
public class VarSwapStaticReplicationTest {

  // The derivative
  final double varStrike = 0.05;
  final double varNotional = 3150;
  final double now = 0;
  final double expiry1 = 1;
  final double expiry2 = 2;
  final double expiry5 = 5;
  final double expiry10 = 10;
  final int nObsExpected = 750;
  final double annualization = 252;

  final VarianceSwap swap0 = new VarianceSwap(now, now, now, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, null, null);
  final VarianceSwap swap1 = new VarianceSwap(now, expiry1, expiry1, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, null, null);
  final VarianceSwap swap2 = new VarianceSwap(now, expiry2, expiry2, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, null, null);
  final VarianceSwap swap5 = new VarianceSwap(now, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, null, null);
  final VarianceSwap swap10 = new VarianceSwap(now, expiry10, expiry10, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, null, null);
  final VarianceSwap swapExpired = new VarianceSwap(now, now - 1, now - 1, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, null, null);

  // The pricing method
  final VarSwapStaticReplication pricer_default_w_cutoff = new VarSwapStaticReplication();
  final VarSwapStaticReplication pricer_null_cutoff = new VarSwapStaticReplication(1.0E-4, 5.0, new RungeKuttaIntegrator1D(), null, null);

  // Market data
  private static final double SPOT = 80;
  private static final double FORWARD = 100;
  private static final double TEST_VOL = 0.25;
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  private static final YieldAndDiscountCurve DISCOUNT = CURVES.getCurve("Funding");

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 0.5,
                                                          1.0, 1.0, 1.0, 1.0,
                                                          5.0, 5.0, 5.0, 5.0,
                                                          10.0, 10.0, 10.0, 10.0};
  private static final double[] STRIKES = new double[] {40, 80, 100, 120,
                                                        40, 80, 100, 120,
                                                        40, 80, 100, 120,
                                                        40, 80, 100, 120};
  private static final double[] VOLS = new double[] {0.28, 0.28, 0.28, 0.28,
                                                     0.25, 0.25, 0.25, 0.25,
                                                     0.26, 0.24, 0.23, 0.25,
                                                     0.20, 0.20, 0.20, 0.20};

  private static final CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR_1D_STRIKE = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  final static CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR_1D_EXPIRY = getInterpolator(Interpolator1DFactory.LINEAR,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  @SuppressWarnings("unchecked")
  // This removes warning from unchecked cast of interpolators below
  private static final Interpolator2D<Interpolator1DDataBundle> INTERPOLATOR_2D = new GridInterpolator2D<Interpolator1DDataBundle, Interpolator1DDataBundle>(
      (Interpolator1D<Interpolator1DDataBundle>) INTERPOLATOR_1D_EXPIRY,
      (Interpolator1D<Interpolator1DDataBundle>) INTERPOLATOR_1D_STRIKE);
  private static final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, INTERPOLATOR_2D);
  private static final VolatilitySurface VOL_SURFACE = new VolatilitySurface(SURFACE);
  private static final VarianceSwapDataBundle MARKET = new VarianceSwapDataBundle(VOL_SURFACE, DISCOUNT, SPOT, FORWARD);

  // Tests ------------------------------------------
  /**
   * Test of VolatilitySurface type
   */
  @Test
  public void testConstantDoublesSurface() {

    final ConstantDoublesSurface constSurf = ConstantDoublesSurface.from(TEST_VOL);
    @SuppressWarnings("unused")
    final VolatilitySurface vols = new VolatilitySurface(constSurf);

    final double testVar = pricer_null_cutoff.impliedVariance(swap1, MARKET);

    final double targetVar = swap1.getTimeToObsEnd() * TEST_VOL * TEST_VOL;
    assertEquals(testVar, targetVar, 1e-9);
  }

  /**
   * Test of VolatilitySurface type
   */
  @Test
  public void testInterpolatedDoublesSurfaceFlat() {

    final double testVar = pricer_default_w_cutoff.impliedVariance(swap1, MARKET);
    final double targetVar = swap1.getTimeToObsEnd() * TEST_VOL * TEST_VOL;
    assertEquals(testVar, targetVar, 1e-9);
  }

  /**
   * Confirm all is well when null values are passed for the cutoff, 
   * at least when the surface can handle low strikes itself
   */
  @Test
  public void testInterpolatedDoublesSurfaceWithoutCutoff() {

    final VarSwapStaticReplication pricerNoCutoff = new VarSwapStaticReplication(1e-16, 10, new RungeKuttaIntegrator1D(), null, null);

    final double testVar = pricerNoCutoff.impliedVariance(swap1, MARKET);
    final double targetVar = expiry1 * TEST_VOL * TEST_VOL;
    assertEquals(testVar, targetVar, 1e-9);
  }

  /** 
   * Confirm error trapped when bad values are passed for the cutoff 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInterpolatedDoublesSurfaceWithPoorCutoffDescription() {

    final VarSwapStaticReplication pricerBadCutoff = new VarSwapStaticReplication(1e-16, 10, new RungeKuttaIntegrator1D(), null, 0.05);
    pricerBadCutoff.impliedVariance(swap1, MARKET);
  }

  /**
   * Although all we get from impliedVariance(swap,market) is the variance, it is clear that the fit is the Lognormal with zero shift,
   * as the variance produced is not affected.
   */
  @Test
  public void testShiftedLognormalFitOnFlatSurface() {

    final VarSwapStaticReplication pricerHighCutoff = new VarSwapStaticReplication(1e-16, 10, new RungeKuttaIntegrator1D(), 0.95, 0.05);

    final double testVar = pricerHighCutoff.impliedVariance(swap1, MARKET);
    final double targetVar = expiry1 * TEST_VOL * TEST_VOL;
    assertEquals(testVar, targetVar, 1e-9);
  }

  /**
   * Confirm that we can split an integral over [a,c] into [a,b]+[b,c]
   */
  @Test
  public void testIntegrationBounds() {

    final VarSwapStaticReplication pricerPuts = new VarSwapStaticReplication(1e-16, 1.0, new RungeKuttaIntegrator1D(), 0.9, 0.01);
    final VarSwapStaticReplication pricerCalls = new VarSwapStaticReplication(1.0, 10, new RungeKuttaIntegrator1D(), null, null);

    final double varFromPuts = pricerPuts.impliedVariance(swap1, MARKET);
    final double varFromCalls = pricerCalls.impliedVariance(swap1, MARKET);
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

    final VarSwapStaticReplication pricer0 = new VarSwapStaticReplication(1e-4, 0.1, new RungeKuttaIntegrator1D(), 0.25, 0.05);
    final VarSwapStaticReplication pricer1 = new VarSwapStaticReplication(0.1, 0.2, new RungeKuttaIntegrator1D(), 0.25, 0.05);
    final VarSwapStaticReplication pricer2 = new VarSwapStaticReplication(0.2, 0.3, new RungeKuttaIntegrator1D(), 0.25, 0.05);
    final VarSwapStaticReplication pricer3 = new VarSwapStaticReplication(0.3, 0.4, new RungeKuttaIntegrator1D(), 0.25, 0.05);
    final VarSwapStaticReplication pricer4 = new VarSwapStaticReplication(0.4, 0.5, new RungeKuttaIntegrator1D(), 0.25, 0.05);
    final VarSwapStaticReplication pricer5 = new VarSwapStaticReplication(0.5, 1.0, new RungeKuttaIntegrator1D(), 0.25, 0.05);
    final VarSwapStaticReplication pricer10 = new VarSwapStaticReplication(1.0, 1.5, new RungeKuttaIntegrator1D(), 0.25, 0.05);
    final VarSwapStaticReplication pricer15 = new VarSwapStaticReplication(1.5, 5., new RungeKuttaIntegrator1D(), 0.25, 0.05);

    double variance_total = 0.0;
    double variance_slice;
    variance_slice = pricer0.impliedVariance(swap5, MARKET);
    variance_total += variance_slice;
    variance_slice = pricer1.impliedVariance(swap5, MARKET);
    variance_total += variance_slice;
    variance_slice = pricer2.impliedVariance(swap5, MARKET);
    variance_total += variance_slice;
    variance_slice = pricer3.impliedVariance(swap5, MARKET);
    variance_total += variance_slice;
    variance_slice = pricer4.impliedVariance(swap5, MARKET);
    variance_total += variance_slice;
    variance_slice = pricer5.impliedVariance(swap5, MARKET);
    variance_total += variance_slice;
    variance_slice = pricer10.impliedVariance(swap5, MARKET);
    variance_total += variance_slice;
    variance_slice = pricer15.impliedVariance(swap5, MARKET);
    variance_total += variance_slice;

    final double variance_onego = pricer_default_w_cutoff.impliedVariance(swap5, MARKET);

    assertEquals(variance_total, variance_onego, 1e-9);
  }

  @Test
  public void testVInterpolatedDoublesSurface() {

    final VarSwapStaticReplication pricerCutoff = new VarSwapStaticReplication(1e-4, 5, new RungeKuttaIntegrator1D(), 0.3894, 0.05);

    final double varSmiley = pricerCutoff.impliedVariance(swap5, MARKET);

    assertEquals(varSmiley, 0.3073988343625848, 1e-9);

    @SuppressWarnings("unused")
    final double refVariance = expiry5 * TEST_VOL * TEST_VOL;

  }

  /**
   * Instead of matching slope and level at the cutoff, try choosing a level at zero strike, then filling in with shifted lognormal distribution
   */
  @SuppressWarnings("hiding")
  @Test
  public void testAlternativeUseOfShiftedLognormal() {

    final double lowerBound = 1.0e-4;
    final double upperBound = 5.0;

    final double[] EXPIRIES = new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                                            5.0, 5.0, 5.0, 5.0, 5.0, 5.0};
    final double[] STRIKES = new double[] {0, 25, 75, 100, 125, 150,
                                           0, 25, 75, 100, 125, 150};
    final double[] VOLS = new double[] {.4, 0.3, 0.25, 0.25, 0.3, 0.3,
                                        .4, 0.3, 0.25, 0.25, 0.3, 0.3};

    final CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR_1D = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    @SuppressWarnings("unchecked")
    final Interpolator2D<Interpolator1DDataBundle> INTERPOLATOR_2D = new GridInterpolator2D<Interpolator1DDataBundle, Interpolator1DDataBundle>(new LinearInterpolator1D(),
        (Interpolator1D<Interpolator1DDataBundle>) INTERPOLATOR_1D);
    final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, INTERPOLATOR_2D);

    final VarianceSwapDataBundle market = new VarianceSwapDataBundle(new VolatilitySurface(SURFACE), DISCOUNT, SPOT, FORWARD);
    final VarSwapStaticReplication pricerCutoff = new VarSwapStaticReplication(lowerBound, upperBound, new RungeKuttaIntegrator1D(), 0.5, -.25); // Hit 75 and 25. Check shape in between. Use this value to extrapolate left

    final double variance = pricerCutoff.impliedVariance(swap1, market);
    assertEquals(variance, 0.07044057342667964, 1e-9);

  }

  @Test
  public void testExpiredSwap() {

    final VarSwapStaticReplication pricerCutoff = new VarSwapStaticReplication(1e-4, 5, new RungeKuttaIntegrator1D(), 0.3894, 0.05);

    final double noMoreVariance = pricerCutoff.impliedVariance(swap0, MARKET);
    assertEquals(0.0, noMoreVariance, 1e-9);

    final double varInExpiredSwap = pricerCutoff.impliedVariance(swap0, MARKET);
    assertEquals(0.0, varInExpiredSwap, 1e-9);
  }
}
