/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import static com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceLogMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.Strike;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VarianceSwapStaticReplicationTest {

  // Setup ------------------------------------------

  // The derivative
  private static final double varStrike = 0.05;
  private static final double varNotional = 3150;
  private static final double now = 0;
  private static final double aYearAgo = -1;
  private static final double expiry6M = 0.5;
  private static final double expiry1 = 1;
  private static final double expiry2 = 2;
  private static final double expiry5 = 5;
  private static final double expiry10 = 10;
  private static final int nObsExpected = 750;
  private static final int nObsDisrupted = 0;
  private static final double annualization = 252;

  private static final double[] observations = {};
  private static final double[] obsWeights = {};

  private static final VarianceSwap swap0 = new VarianceSwap(aYearAgo, now, now, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  private static final VarianceSwap swap6M = new VarianceSwap(now, expiry6M, expiry6M, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  private static final VarianceSwap swap1 = new VarianceSwap(now, expiry1, expiry1, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  private static final VarianceSwap swap2 = new VarianceSwap(now, expiry2, expiry2, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  private static final VarianceSwap swap5 = new VarianceSwap(now, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  private static final VarianceSwap swap10 = new VarianceSwap(now, expiry10, expiry10, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);

  private static final VarianceSwap swap5x10 = new VarianceSwap(expiry5, expiry10, expiry10, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  // Market data
  private static final double SPOT = 80;
  private static final double DRIFT = 0.05;
  private static final ForwardCurve FORWARD_CURVE = new ForwardCurve(SPOT, DRIFT);
  private static final double TEST_VOL = 0.25;
  private static final YieldAndDiscountCurve DISCOUNT = new YieldCurve("Discount", ConstantDoublesCurve.from(0.05));

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0, 10.0 };

  private static final double[] CALLDELTAS = new double[] {0.9, 0.75, 0.5, 0.25, 0.1, 0.9, 0.75, 0.5, 0.25, 0.1, 0.9, 0.75, 0.5, 0.25, 0.1, 0.9, 0.75, 0.5, 0.25, 0.1 };

  private static final double[] STRIKES = new double[] {20, 40, 80, 100, 120, 20, 40, 80, 100, 120, 20, 40, 80, 100, 120, 20, 40, 80, 100, 120 };

  private static final double[] VOLS = new double[] {0.28, 0.28, 0.28, 0.28, 0.28, 0.25, 0.25, 0.25, 0.25, 0.25, 0.27, 0.26, 0.24, 0.23, 0.25, 0.27, 0.26, 0.25, 0.26, 0.27 };

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR_1D_STRIKE = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR_1D_EXPIRY = getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final Interpolator2D INTERPOLATOR_2D = new GridInterpolator2D(INTERPOLATOR_1D_EXPIRY, INTERPOLATOR_1D_STRIKE);
  private static final BlackVolatilitySurfaceStrike VOL_STRIKE_SURFACE = new BlackVolatilitySurfaceStrike(new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, INTERPOLATOR_2D));
  private static final BlackVolatilitySurfaceDelta VOL_CALLDELTA_SURFACE = new BlackVolatilitySurfaceDelta(new InterpolatedDoublesSurface(EXPIRIES, CALLDELTAS, VOLS, INTERPOLATOR_2D), FORWARD_CURVE);

  private static final StaticReplicationDataBundle MARKET_W_STRIKESURF = new StaticReplicationDataBundle(VOL_STRIKE_SURFACE, DISCOUNT, FORWARD_CURVE);
  private static final StaticReplicationDataBundle MARKET_W_CALLDELTASURF = new StaticReplicationDataBundle(VOL_CALLDELTA_SURFACE, DISCOUNT, FORWARD_CURVE);

  //Since we use very conservative estimates of the tolerance, the actual error is 100x less than the tolerance set. In really, you'll never need a  1 part in 1,000,000,000
  //accuracy that we test for here.
  private static final double INTEGRAL_TOL = 1e-9;
  private static final double TEST_TOL = 1e-9;

  private static final VarianceSwapStaticReplication PRICER = new VarianceSwapStaticReplication(INTEGRAL_TOL);

  // impliedVariance Tests ------------------------------------------
  /**
   * Test ConstantDoublesSurface delta surface at 1 and 10 years
   */
  @Test
  public void testConstantDoublesDeltaSurface() {
    final BlackVolatilitySurfaceDelta constVolSurf = new BlackVolatilitySurfaceDelta(ConstantDoublesSurface.from(TEST_VOL), FORWARD_CURVE);
    final double testVar = PRICER.expectedVariance(swap1, new StaticReplicationDataBundle(constVolSurf, DISCOUNT, FORWARD_CURVE));
    final double testVar2 = PRICER.expectedVariance(swap10, new StaticReplicationDataBundle(constVolSurf, DISCOUNT, FORWARD_CURVE));
    final double targetVar = TEST_VOL * TEST_VOL;

    assertEquals(targetVar, testVar, TEST_TOL);
    assertEquals(targetVar, testVar2, TEST_TOL);
  }

  /**
   * Test ConstantDoublesSurface strike surface at 1 and 10 years
   */
  @Test
  public void testConstantDoublesStrikeSurface() {
    final BlackVolatilitySurfaceStrike constVolSurf = new BlackVolatilitySurfaceStrike(ConstantDoublesSurface.from(TEST_VOL));
    final double testVar = PRICER.expectedVariance(swap1, new StaticReplicationDataBundle(constVolSurf, DISCOUNT, FORWARD_CURVE));
    final double testVar2 = PRICER.expectedVariance(swap10, new StaticReplicationDataBundle(constVolSurf, DISCOUNT, FORWARD_CURVE));
    final double targetVar = TEST_VOL * TEST_VOL;
    assertEquals(targetVar, testVar, TEST_TOL);
    assertEquals(targetVar, testVar2, TEST_TOL);
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
    PRICER.expectedVariance(swap1, new StaticReplicationDataBundle(volSurface, DISCOUNT, FORWARD_CURVE));
  }

  /**
   * Test of flat VolatilitySurface Strike vs Delta with tail extrapolation
   */
  @Test
  public void testFlatSurfaceOnStrikeAndDelta() {

    final double testDeltaVar = PRICER.expectedVariance(swap6M, MARKET_W_CALLDELTASURF);
    final double testStrikeVar = PRICER.expectedVariance(swap6M, MARKET_W_STRIKESURF);
    final double targetVar = 0.28 * 0.28;
    assertEquals(targetVar, testDeltaVar, 1e-9);
    assertEquals(targetVar, testStrikeVar, 1e-9);
  }

  /**
   * Test that an expired swap returns 0 variance
   */
  @Test
  public void testExpiredSwap() {
    final double noMoreVariance = PRICER.expectedVariance(swap0, MARKET_W_STRIKESURF);
    assertEquals(0.0, noMoreVariance, 1e-9);
  }

  /**
   * Test that for the same volatility surface, the result is the same for integrals over strike, delta, moneyness and log-moneyness. The volatility surface is defined as a delta surface,
   *  and converted to a the other surfaces - so the surfaces are numerically different, but conceptually the same thing
   */
  @Test
  public void testVolSurface() {

    final Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... x) {
        final double delta = x[1];
        return 0.2 + 0.3 * (delta - 0.4) * (delta - 0.4);
      }
    };

    final BlackVolatilitySurfaceDelta surfaceDelta = new BlackVolatilitySurfaceDelta(FunctionalDoublesSurface.from(surf), FORWARD_CURVE);
    final BlackVolatilitySurfaceLogMoneyness surfaceLogMoneyness = BlackVolatilitySurfaceConverter.toLogMoneynessSurface(surfaceDelta);
    final BlackVolatilitySurfaceMoneyness surfaceMoneyness = BlackVolatilitySurfaceConverter.toMoneynessSurface(surfaceLogMoneyness);
    final BlackVolatilitySurfaceStrike surfaceStrike = BlackVolatilitySurfaceConverter.toStrikeSurface(surfaceLogMoneyness);

    final StaticReplicationDataBundle marketStrike = new StaticReplicationDataBundle(surfaceStrike, DISCOUNT, FORWARD_CURVE);
    final StaticReplicationDataBundle marketLogMoneyness = new StaticReplicationDataBundle(surfaceLogMoneyness, DISCOUNT, FORWARD_CURVE);
    final StaticReplicationDataBundle marketMoneyness = new StaticReplicationDataBundle(surfaceMoneyness, DISCOUNT, FORWARD_CURVE);
    final StaticReplicationDataBundle marketDelta = new StaticReplicationDataBundle(surfaceDelta, DISCOUNT, FORWARD_CURVE);

    final double totalVarStrike = PRICER.expectedVariance(swap1, marketStrike);
    final double totalVarLogMoneyness = PRICER.expectedVariance(swap1, marketLogMoneyness);
    final double totalVarMoneyness = PRICER.expectedVariance(swap1, marketMoneyness);
    final double totalVarDelta = PRICER.expectedVariance(swap1, marketDelta);
    assertEquals(totalVarStrike, totalVarDelta, 1e-7); //TODO why is integral over delta not as good?
    assertEquals(totalVarStrike, totalVarLogMoneyness, TEST_TOL);
    assertEquals(totalVarStrike, totalVarMoneyness, TEST_TOL);
  }

  /**
   * For a symmetric mixed logNormal model (i.e. the forward is the same for all states of the world), then the expected variance is trivial to calculate
   */
  @Test
  public void testMixedLogNormalVolSurface() {

    final double sigma1 = 0.2;
    final double sigma2 = 1.0;
    final double w = 0.9;

    final Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        @SuppressWarnings("synthetic-access")
        final double fwd = FORWARD_CURVE.getForward(t);
        final boolean isCall = k > fwd;
        final double price = w * BlackFormulaRepository.price(fwd, k, t, sigma1, isCall) + (1 - w) * BlackFormulaRepository.price(fwd, k, t, sigma2, isCall);
        return BlackFormulaRepository.impliedVolatility(price, fwd, k, t, isCall);
      }
    };

    final BlackVolatilitySurface<Strike> surfaceStrike = new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surf));
    final StaticReplicationDataBundle marketStrike = new StaticReplicationDataBundle(surfaceStrike, DISCOUNT, FORWARD_CURVE);

    final double compVar = PRICER.expectedVariance(swap1, marketStrike);
    final double compVarLimits = PRICER.expectedVariance(swap1, marketStrike);
    final double expected = w * sigma1 * sigma1 + (1 - w) * sigma2 * sigma2;
    assertEquals(expected, compVar, TEST_TOL);
    assertEquals(expected, compVarLimits, 2e-4); //The substitution of shifted log-normal below the cutoff introduced some error

    //test a forward start
    final double compVar2 = PRICER.expectedVariance(swap5x10, marketStrike);
    assertEquals(expected, compVar2, 10 * TEST_TOL);
  }

  /**
   * A general mixed logNormal model. In this case the different forwards for the different branches, themselves generate volatility which must be accounted for
   */
  @Test
  public void testMixedLogNormalVolSurface2() {

    final double t = swap2.getTimeToObsEnd();
    final double fwd = FORWARD_CURVE.getForward(t);

    final int n = 3;
    final double[] sigma = new double[] {0.2, 0.5, 2.0 };
    final double[] w = new double[] {0.8, 0.15, 0.05 };
    final double[] f = new double[n];
    f[0] = 1.05 * fwd;
    f[1] = 0.9 * fwd;
    double sum = 0;
    for (int i = 0; i < n - 1; i++) {
      sum += w[i] * f[i];
    }
    f[n - 1] = (fwd - sum) / w[n - 1];
    Validate.isTrue(f[n - 1] > 0);

    final Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... x) {
        final double expiry = x[0];
        final double k = x[1];
        final boolean isCall = k > fwd;
        double price = 0;
        for (int i = 0; i < n; i++) {
          price += w[i] * BlackFormulaRepository.price(f[i], k, expiry, sigma[i], isCall);
        }
        return BlackFormulaRepository.impliedVolatility(price, fwd, k, expiry, isCall);
      }
    };

    final BlackVolatilitySurface<Strike> surfaceStrike = new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surf));

    //PDEUtilityTools.printSurface(null, surfaceStrike.getSurface(), 0.1, 4, 5, 300);

    final StaticReplicationDataBundle marketStrike = new StaticReplicationDataBundle(surfaceStrike, DISCOUNT, FORWARD_CURVE);

    final double compVar = PRICER.expectedVariance(swap2, marketStrike);

    double expected = 0;
    for (int i = 0; i < n; i++) {
      expected += w[i] * (sigma[i] * sigma[i] + 2 / t * (f[i] / fwd + Math.log(fwd / f[i]) - 1));
    }

    assertEquals(expected, compVar, 5e-7);
  }

  // impliedVolatility Tests ------------------------------------------

  @Test
  public void testExpectedVolatility() {
    final double sigmaSquared = PRICER.expectedVariance(swap5, MARKET_W_STRIKESURF);
    final double sigma = PRICER.expectedVolatility(swap5, MARKET_W_STRIKESURF);

    assertEquals(sigmaSquared, sigma * sigma, 1e-9);

  }

}
