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
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.pricing.fourier.IntegratedCIRTimeChangeCharacteristicExponent;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceConverter;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.financial.model.volatility.surface.Strike;
import com.opengamma.math.ComplexMathUtils;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.math.number.ComplexNumber;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

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
  final double expiry6M = 0.5;
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
  final VarianceSwap swap6M = new VarianceSwap(now, expiry6M, expiry6M, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  final VarianceSwap swap1 = new VarianceSwap(now, expiry1, expiry1, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  final VarianceSwap swap2 = new VarianceSwap(now, expiry2, expiry2, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  final VarianceSwap swap5 = new VarianceSwap(now, expiry5, expiry5, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  final VarianceSwap swap10 = new VarianceSwap(now, expiry10, expiry10, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
  final VarianceSwap swapExpired = new VarianceSwap(now, now - 1, now - 1, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);

  final VarianceSwap swap5x10 = new VarianceSwap(expiry5, expiry10, expiry10, varStrike, varNotional, Currency.EUR, annualization, nObsExpected, nObsDisrupted, observations, obsWeights);
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

  private static final VarianceSwapDataBundle MARKET_W_STRIKESURF = new VarianceSwapDataBundle(VOL_STRIKE_SURFACE, DISCOUNT, FORWARD_CURVE);
  // private static final VarianceSwapDataBundle MARKET_W_PUTDELTASURF = new VarianceSwapDataBundle(VOL_PUTDELTA_SURFACE, DISCOUNT, SPOT, FORWARD);
  private static final VarianceSwapDataBundle MARKET_W_CALLDELTASURF = new VarianceSwapDataBundle(VOL_CALLDELTA_SURFACE, DISCOUNT, FORWARD_CURVE);

  //Since we use very conservative estimates of the tolerance, the actual error is 100x less than the tolerance set. In really, you'll never need a  1 part in 1,000,000,000
  //accuracy that we test for here.
  private static final double INTEGRAL_TOL = 1e-9;
  private static final double TEST_TOL = 1e-9;

  private static final DoublesPair DELTA_CUTOFF = new DoublesPair(0.96, 0.95);
  private static final DoublesPair STRIKE_CUTOFF = new DoublesPair(0.15 * SPOT, 0.16 * SPOT);

  private static final VarianceSwapStaticReplication PRICER = new VarianceSwapStaticReplication(INTEGRAL_TOL);

  // impliedVariance Tests ------------------------------------------
  /**
   * Test ConstantDoublesSurface delta surface at 1 and 10 years
   */
  @Test
  public void testConstantDoublesDeltaSurface() {
    final BlackVolatilitySurfaceDelta constVolSurf = new BlackVolatilitySurfaceDelta(ConstantDoublesSurface.from(TEST_VOL), FORWARD_CURVE);
    final double testVar = PRICER.impliedVariance(swap1, new VarianceSwapDataBundle(constVolSurf, DISCOUNT, FORWARD_CURVE));
    final double testVar2 = PRICER.impliedVariance(swap10, new VarianceSwapDataBundle(constVolSurf, DISCOUNT, FORWARD_CURVE));
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
    final double testVar = PRICER.impliedVariance(swap1, new VarianceSwapDataBundle(constVolSurf, DISCOUNT, FORWARD_CURVE));
    final double testVar2 = PRICER.impliedVariance(swap10, new VarianceSwapDataBundle(constVolSurf, DISCOUNT, FORWARD_CURVE));
    final double targetVar = TEST_VOL * TEST_VOL;
    assertEquals(testVar, targetVar, TEST_TOL);
    assertEquals(testVar2, targetVar, TEST_TOL);
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
    PRICER.impliedVariance(swap1, new VarianceSwapDataBundle(volSurface, DISCOUNT, FORWARD_CURVE));
  }

  /**
   * Test of flat VolatilitySurface Strike vs Delta with tail extrapolation
   */
  @Test
  public void testFlatSurfaceOnStrikeAndDelta() {

    final double testDeltaVar = PRICER.impliedVariance(swap6M, MARKET_W_CALLDELTASURF, DELTA_CUTOFF);
    final double testStrikeVar = PRICER.impliedVariance(swap6M, MARKET_W_STRIKESURF, STRIKE_CUTOFF);
    final double targetVar = 0.28 * 0.28;
    assertEquals(targetVar, testDeltaVar, 1e-9);
    assertEquals(targetVar, testStrikeVar, 1e-9);
  }

  /**
   * Test that an expired swap returns 0 variance
   */
  @Test
  public void testExpiredSwap() {
    final double noMoreVariance = PRICER.impliedVariance(swap0, MARKET_W_STRIKESURF);
    assertEquals(0.0, noMoreVariance, 1e-9);
  }

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

    final VarianceSwapDataBundle marketStrike = new VarianceSwapDataBundle(surfaceStrike, DISCOUNT, FORWARD_CURVE);
    final VarianceSwapDataBundle marketDelta = new VarianceSwapDataBundle(surfaceDelta, DISCOUNT, FORWARD_CURVE);

    final double totalVarStrike = PRICER.impliedVariance(swap1, marketStrike);
    final double totalVarDelta = PRICER.impliedVariance(swap1, marketDelta);
    assertEquals(totalVarStrike, totalVarDelta, 1e-7);

  }

  /**
   * For a symmetric mixed logNormal model (i.e. the forward is the same for all states of the world), then the expected variance is trivial to calculate
   */
  @Test
  public void testMixedLogNormalVolSurface() {

    final double sigma1 = 0.2;
    final double sigma2 = 1.0;
    final double w = 0.9;

    Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... x) {
        final double t = x[0];
        final double k = x[1];
        final double fwd = FORWARD_CURVE.getForward(t);
        final boolean isCall = k > fwd;
        final double price = w * BlackFormulaRepository.price(fwd, k, t, sigma1, isCall) + (1 - w) * BlackFormulaRepository.price(fwd, k, t, sigma2, isCall);
        return BlackFormulaRepository.impliedVolatility(price, fwd, k, t, isCall);
      }
    };

    BlackVolatilitySurface<Strike> surfaceStrike = new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surf));
    final VarianceSwapDataBundle marketStrike = new VarianceSwapDataBundle(surfaceStrike, DISCOUNT, FORWARD_CURVE);

    final double compVar = PRICER.impliedVariance(swap1, marketStrike);
    final double compVarLimits = PRICER.impliedVariance(swap1, marketStrike, STRIKE_CUTOFF);
    double expected = w * sigma1 * sigma1 + (1 - w) * sigma2 * sigma2;
    assertEquals(expected, compVar, 1e-7);
    assertEquals(expected, compVarLimits, 2e-3); //TODO The shifted log normal does not perform that well here

    //test a forward start
    final double compVar2 = PRICER.impliedVariance(swap5x10, marketStrike);
    assertEquals(expected, compVar2, 1e-7);
  }

  @Test
  public void testHestonVolSurface() {

    double kappa = 0.0;
    double var0 = 0.3;
    double theta = 0.3;
    double lambda = 0.5;
    double eps = 1e-5;

    IntegratedCIRTimeChangeCharacteristicExponent cf = new IntegratedCIRTimeChangeCharacteristicExponent(kappa, theta / var0, lambda / Math.sqrt(var0));
    Function1D<ComplexNumber, ComplexNumber> func = cf.getFunction(1.0);
    ComplexNumber v1 = func.evaluate(new ComplexNumber(eps));
    ComplexNumber v2 = func.evaluate(new ComplexNumber(-eps));

    ComplexNumber res = ComplexMathUtils.subtract(v1, v2);
    System.out.println(res.toString());

    double div = var0 * res.getImaginary() / 2 / eps;
    System.out.println(div);
  }

  // impliedVolatility Tests ------------------------------------------

  @Test
  public void testImpliedVolatility() {
    final double sigmaSquared = PRICER.impliedVariance(swap5, MARKET_W_STRIKESURF);
    final double sigma = PRICER.impliedVolatility(swap5, MARKET_W_STRIKESURF);

    assertEquals(sigmaSquared, sigma * sigma, 1e-9);

  }

}
