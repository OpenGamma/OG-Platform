/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import static com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.equity.variance.pricing.DisplacedDiffusionModel;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.rootfinding.VectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DisplacedDiffusionModelTest {

  // -------------------------------- SETUP ------------------------------------------

  // The derivative
  //  private static final double varStrike = 0.05;
  //  private static final double varNotional = 3150;
  private static final double expiry1 = 1;
  private static final double expiry5 = 5;
  //  private static final int nObsExpected = 750;
  //  private static final int nObsDisrupted = 0;
  //  private static final double[] observations = {};
  //  private static final double[] obsWeights = {};

  /*
  final VarianceSwap swap1 = new VarianceSwap(0, expiry1, expiry1, varStrike, varNotional, Currency.EUR, 250, nObsExpected, nObsDisrupted, observations, obsWeights);
  final VarianceSwap swap5 = new VarianceSwap(0, expiry5, expiry5, varStrike, varNotional, Currency.EUR, 250, nObsExpected, nObsDisrupted, observations, obsWeights);

  // The pricing method
  final VarSwapStaticReplication pricer_default_w_cutoff = new VarSwapStaticReplication(null);
  final VarSwapStaticReplication pricer_null_cutoff = new VarSwapStaticReplication(1.0E-4, 5.0, new RungeKuttaIntegrator1D(), null, null, null);
   */
  // Market data
  private static final double SPOT = 80;
  private static final double DRIFT = -0.01;
  private static final ForwardCurve FORWARD_CURVE = new ForwardCurve(SPOT, DRIFT);
  //double forward = 100;
  //private static final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
  //private static final YieldAndDiscountCurve curveDiscount = curves.getCurve("Funding");

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 0.5,
    1.0, 1.0, 1.0, 1.0,
    5.0, 5.0, 5.0, 5.0 };
  private static final double[] DELTAS = new double[] {0.75, 0.5, 0.25, 0.1,
    0.75, 0.5, 0.25, 0.1,
    0.75, 0.5, 0.25, 0.1 };

  private static final double[] STRIKES = new double[] {10, 50, 105, 150,
    10, 50, 105, 150,
    10, 50, 105, 150 };

  private static final double[] VOLS = new double[] {0.28, 0.28, 0.28, 0.28,
    0.25, 0.25, 0.25, 0.25,
    0.26, 0.24, 0.23, 0.25 };

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR_1D = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator2D INTERPOLATOR_2D = new GridInterpolator2D(new LinearInterpolator1D(), INTERPOLATOR_1D);
  private static final InterpolatedDoublesSurface DELTA_SURFACE = new InterpolatedDoublesSurface(EXPIRIES, DELTAS, VOLS, INTERPOLATOR_2D);
  private static final BlackVolatilitySurface<?> DELTA_VOLSURFACE = new BlackVolatilitySurfaceDelta(DELTA_SURFACE, FORWARD_CURVE);

  private static final InterpolatedDoublesSurface STRIKE_SURFACE = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, INTERPOLATOR_2D);
  private static final BlackVolatilitySurface<?> STRIKE_VOLSURFACE = new BlackVolatilitySurfaceStrike(STRIKE_SURFACE);

  // -------------------------------- TESTS  ------------------------------------------
  @Test
  public void testFlatSurfaceReproducesVolAndZeroShift() {

    final double delta1 = 0.75;
    final double forward = FORWARD_CURVE.getForward(expiry1);
    final double vol1 = DELTA_VOLSURFACE.getVolatility(expiry1, delta1);
    final double strike1 = BlackFormulaRepository.impliedStrike(delta1, true, forward, expiry1, vol1);
    final double delta2 = 0.5;
    final double vol2 = DELTA_VOLSURFACE.getVolatility(expiry1, delta2);
    final double strike2 = BlackFormulaRepository.impliedStrike(delta2, true, forward, expiry1, vol2);
    final DisplacedDiffusionModel logBlack = new DisplacedDiffusionModel(forward, expiry1, strike1, vol1, strike2, vol2);

    assertEquals(logBlack.getVol(), 0.25, 1e-9);
    assertEquals(logBlack.getShift(), 0.0, 1e-9);
  }

  @Test
  public void testConstantDoublesSurface() {
    final BlackVolatilitySurface<?> constVolSurf = new BlackVolatilitySurfaceDelta(ConstantDoublesSurface.from(0.25), FORWARD_CURVE);
    final double forward = FORWARD_CURVE.getForward(expiry1);
    final double delta1 = 0.75;
    final double vol1 = constVolSurf.getVolatility(expiry1, delta1);
    final double strike1 = BlackFormulaRepository.impliedStrike(delta1, true, forward, expiry1, vol1);
    final double delta2 = 0.5;
    final double vol2 = constVolSurf.getVolatility(expiry1, delta2);
    final double strike2 = BlackFormulaRepository.impliedStrike(delta2, true, forward, expiry1, vol2);
    final DisplacedDiffusionModel logBlack = new DisplacedDiffusionModel(forward, expiry1, strike1, vol1, strike2, vol2);

    assertEquals(logBlack.getVol(), 0.25, 1e-9);
    assertEquals(logBlack.getShift(), 0.0, 1e-9);
  }

  @Test
  public void testOnSmileySurface() {
    final double forward = FORWARD_CURVE.getForward(expiry5);
    final VectorRootFinder testSolver = new BroydenVectorRootFinder(1.0e-6, 1.0e-6, 50000);
    final double vol1 = STRIKE_VOLSURFACE.getVolatility(expiry5, 40);
    final double vol2 = STRIKE_VOLSURFACE.getVolatility(expiry5, 41);
    final DisplacedDiffusionModel logBlack = new DisplacedDiffusionModel(forward, expiry5, 40, vol1, 41, vol2, 0.26, 0.01, testSolver);

    //TODO really - more magic numbers
    assertEquals(0.21148605807417542, logBlack.getVol(), 1e-8);
    assertEquals(8.183093915461424, logBlack.getShift(), 1e-8);

    final DisplacedDiffusionModel logBlackDef = new DisplacedDiffusionModel(forward, expiry5, 40, vol1, 41, vol2);
    assertEquals(logBlackDef.getVol(), logBlack.getVol(), 1e-8);
    assertEquals(logBlackDef.getShift(), logBlack.getShift(), 1e-8);

  }

  @Test
  public void testDisplacedDiffusionSmile() {
    final double displacement = -10.0;
    final double sigma = 0.2;
    final Function<Double, Double> surf = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        @SuppressWarnings("synthetic-access")
        final double fwd = FORWARD_CURVE.getForward(t);
        final double price = BlackFormulaRepository.price(fwd + displacement, k + displacement, t, sigma, true);
        return BlackFormulaRepository.impliedVolatility(price, fwd, k, t, true);
      }
    };

    final BlackVolatilitySurface<?> volSurface = new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surf));
    final double forward = FORWARD_CURVE.getForward(expiry5);
    final double vol1 = volSurface.getVolatility(expiry5, 40);
    final double vol2 = volSurface.getVolatility(expiry5, 41);
    final DisplacedDiffusionModel logBlackDef = new DisplacedDiffusionModel(forward, expiry5, 40, vol1, 41, vol2);
    assertEquals(sigma, logBlackDef.getVol(), 1e-8);
    assertEquals(displacement, logBlackDef.getShift(), 1e-6);
  }

  //  @Test
  //  public void testPricingAndImpVol() {
  //    double vol1 = STRIKE_VOLSURFACE.getVolatility(expiry5, 40);
  //    double vol2 = STRIKE_VOLSURFACE.getVolatility(expiry5, 41);
  //    final ShiftedLognormalVolModel shiftedBlack = new ShiftedLognormalVolModel(forward, expiry5, 40, vol1, 41, vol2);
  //
  //    assertEquals(0.2123433522914358, shiftedBlack.getVol(), 1e-8);
  //    assertEquals(8.977510042076403, shiftedBlack.getShift(), 1e-8);
  //
  //    double[] targets = {0.8579743918497895, 0.2403423824470046, 0.23181972642559168, 0.22833151007347832, 0.22634625564003685, 0.2250331292507572, 0.2240855895344658,
  //        0.22336179752396107, 0.22278625617056824, 0.22231472625418022, 0.2219193953132662 };
  //
  //    final double highStrike = 5.0 * forward;
  //    final double lowStrike = 1e-4 * forward;
  //    final int nSamples = 10;
  //    final double[] strikes = new double[nSamples + 1];
  //    final double[] prices = new double[nSamples + 1];
  //    final double[] vols = new double[nSamples + 1];
  //    for (int i = 0; i <= nSamples; i++) {
  //      strikes[i] = lowStrike + (double) i / nSamples * (highStrike - lowStrike);
  //      prices[i] = shiftedBlack.priceFromFixedStrike(strikes[i]);
  //      vols[i] = new BlackFormula(forward, strikes[i], expiry5, null, prices[i], strikes[i] > forward).computeImpliedVolatility();
  //      assertEquals(targets[i], vols[i], 1e-8);
  //    }
  //  }
  //
  //  @Test
  //  public void testImpactOfShiftOnImpliedVols() {
  //
  //    final double highStrike = 5.0 * forward;
  //    final double lowStrike = 1e-4 * forward;
  //    final int nSamples = 10;
  //    final double[] strikes = new double[nSamples + 1];
  //    final double[] prices = new double[nSamples + 1];
  //    final double[] vols = new double[nSamples + 1];
  //
  //    double lowVol = 0.1;
  //    double highVol = 0.1;
  //    double lowShift = 0;
  //    double highShift = 0.5 * forward;
  //
  //    final double[] lnVols = new double[nSamples + 1];
  //    final double[] shifts = new double[nSamples + 1];
  //
  //    for (int j = 0; j <= nSamples; j++) {
  //      lnVols[j] = lowVol + (double) j / nSamples * (highVol - lowVol);
  //      shifts[j] = lowShift + (double) j / nSamples * (highShift - lowShift);
  //      ShiftedLognormalVolModel shiftedBlack = new ShiftedLognormalVolModel(forward, expiry5, lnVols[j], shifts[j]);
  //
  //      for (int i = 0; i <= nSamples; i++) {
  //        strikes[i] = lowStrike + (double) i / nSamples * (highStrike - lowStrike);
  //        prices[i] = shiftedBlack.priceFromFixedStrike(strikes[i]);
  //        vols[i] = new BlackFormula(forward, strikes[i], expiry5, null, prices[i], strikes[i] > forward).computeImpliedVolatility();
  //
  //        // System.err.println(lnVols[j] + "," + shifts[j] + "," + strikes[i] + "," + vols[i]);
  //      }
  //    }
  //  }
}

