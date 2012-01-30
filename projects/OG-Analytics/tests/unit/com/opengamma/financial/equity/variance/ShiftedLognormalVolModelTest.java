/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance;

import static com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.equity.variance.pricing.ShiftedLognormalVolModel;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.BlackFormula;
import com.opengamma.financial.model.volatility.surface.BlackVolatilityDeltaSurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilityFixedStrikeSurface;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.rootfinding.VectorRootFinder;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.InterpolatedDoublesSurface;

/**
 * 
 */
public class ShiftedLognormalVolModelTest {

  // -------------------------------- SETUP ------------------------------------------

  // The derivative
  final double varStrike = 0.05;
  final double varNotional = 3150;
  final double expiry1 = 1;
  final double expiry5 = 5;
  final int nObsExpected = 750;
  final int nObsDisrupted = 0;
  final double[] observations = {};
  final double[] obsWeights = {};

  /*
  final VarianceSwap swap1 = new VarianceSwap(0, expiry1, expiry1, varStrike, varNotional, Currency.EUR, 250, nObsExpected, nObsDisrupted, observations, obsWeights);
  final VarianceSwap swap5 = new VarianceSwap(0, expiry5, expiry5, varStrike, varNotional, Currency.EUR, 250, nObsExpected, nObsDisrupted, observations, obsWeights);

  // The pricing method
  final VarSwapStaticReplication pricer_default_w_cutoff = new VarSwapStaticReplication(null);
  final VarSwapStaticReplication pricer_null_cutoff = new VarSwapStaticReplication(1.0E-4, 5.0, new RungeKuttaIntegrator1D(), null, null, null);
  */
  // Market data
  double spot = 80;
  double forward = 100;
  final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
  final YieldAndDiscountCurve curveDiscount = curves.getCurve("Funding");

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
  private static final BlackVolatilityDeltaSurface DELTA_VOLSURFACE = new BlackVolatilityDeltaSurface(DELTA_SURFACE);

  private static final InterpolatedDoublesSurface STRIKE_SURFACE = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, INTERPOLATOR_2D);
  private static final BlackVolatilityFixedStrikeSurface STRIKE_VOLSURFACE = new BlackVolatilityFixedStrikeSurface(STRIKE_SURFACE);

  // -------------------------------- TESTS  ------------------------------------------
  @Test
  public void testFlatSurfaceReproducesVolAndZeroShift() {

    double delta1 = 0.75;
    double vol1 = DELTA_VOLSURFACE.getVolatility(expiry1, delta1);
    double strike1 = DELTA_VOLSURFACE.getStrike(expiry1, delta1, forward);
    double delta2 = 0.5;
    double vol2 = DELTA_VOLSURFACE.getVolatility(expiry1, delta2);
    double strike2 = DELTA_VOLSURFACE.getStrike(expiry1, delta2, forward);
    final ShiftedLognormalVolModel logBlack = new ShiftedLognormalVolModel(forward, expiry1, strike1, vol1, strike2, vol2);

    assertEquals(logBlack.getVol(), 0.25, 1e-9);
    assertEquals(logBlack.getShift(), 0.0, 1e-9);
  }

  @Test
  public void testConstantDoublesSurface() {
    BlackVolatilityDeltaSurface constVolSurf = new BlackVolatilityDeltaSurface(ConstantDoublesSurface.from(0.25));
    double delta1 = 0.75;
    double vol1 = constVolSurf.getVolatility(delta1, expiry1);
    double strike1 = constVolSurf.getStrike(expiry1, delta1, forward);
    double delta2 = 0.5;
    double vol2 = constVolSurf.getVolatility(delta2, expiry1);
    double strike2 = constVolSurf.getStrike(expiry1, delta2, forward);

    final ShiftedLognormalVolModel logBlack = new ShiftedLognormalVolModel(forward, expiry1, strike1, vol1, strike2, vol2);

    assertEquals(logBlack.getVol(), 0.25, 1e-9);
    assertEquals(logBlack.getShift(), 0.0, 1e-9);
  }

  @Test
  public void testOnSmileySurface() {
    final VectorRootFinder testSolver = new BroydenVectorRootFinder(1.0e-6, 1.0e-6, 50000);
    double vol1 = STRIKE_VOLSURFACE.getVolatility(expiry5, 40);
    double vol2 = STRIKE_VOLSURFACE.getVolatility(expiry5, 41);
    final ShiftedLognormalVolModel logBlack = new ShiftedLognormalVolModel(forward, expiry5, 40, vol1, 41, vol2, 0.26, 0.01, testSolver);

    assertEquals(0.2123433522914358, logBlack.getVol(), 1e-8);
    assertEquals(8.977510042076403, logBlack.getShift(), 1e-8);

    final ShiftedLognormalVolModel logBlackDef = new ShiftedLognormalVolModel(forward, expiry5, 40, vol1, 41, vol2);
    assertEquals(logBlackDef.getVol(), logBlack.getVol(), 1e-8);
    assertEquals(logBlackDef.getShift(), logBlack.getShift(), 1e-8);

  }

  @Test
  public void testPricingAndImpVol() {
    double vol1 = STRIKE_VOLSURFACE.getVolatility(expiry5, 40);
    double vol2 = STRIKE_VOLSURFACE.getVolatility(expiry5, 41);
    final ShiftedLognormalVolModel shiftedBlack = new ShiftedLognormalVolModel(forward, expiry5, 40, vol1, 41, vol2);

    assertEquals(0.2123433522914358, shiftedBlack.getVol(), 1e-8);
    assertEquals(8.977510042076403, shiftedBlack.getShift(), 1e-8);

    double[] targets = {0.8579743918497895, 0.2403423824470046, 0.23181972642559168, 0.22833151007347832, 0.22634625564003685, 0.2250331292507572, 0.2240855895344658,
        0.22336179752396107, 0.22278625617056824, 0.22231472625418022, 0.2219193953132662 };

    final double highStrike = 5.0 * forward;
    final double lowStrike = 1e-4 * forward;
    final int nSamples = 10;
    final double[] strikes = new double[nSamples + 1];
    final double[] prices = new double[nSamples + 1];
    final double[] vols = new double[nSamples + 1];
    for (int i = 0; i <= nSamples; i++) {
      strikes[i] = lowStrike + (double) i / nSamples * (highStrike - lowStrike);
      prices[i] = shiftedBlack.priceFromFixedStrike(strikes[i]);
      vols[i] = new BlackFormula(forward, strikes[i], expiry5, null, prices[i], strikes[i] > forward).computeImpliedVolatility();
      assertEquals(targets[i], vols[i], 1e-8);
    }
  }

  @Test
  public void testImpactOfShiftOnImpliedVols() {

    final double highStrike = 5.0 * forward;
    final double lowStrike = 1e-4 * forward;
    final int nSamples = 10;
    final double[] strikes = new double[nSamples + 1];
    final double[] prices = new double[nSamples + 1];
    final double[] vols = new double[nSamples + 1];

    double lowVol = 0.1;
    double highVol = 0.1;
    double lowShift = 0;
    double highShift = 0.5 * forward;

    final double[] lnVols = new double[nSamples + 1];
    final double[] shifts = new double[nSamples + 1];

    for (int j = 0; j <= nSamples; j++) {
      lnVols[j] = lowVol + (double) j / nSamples * (highVol - lowVol);
      shifts[j] = lowShift + (double) j / nSamples * (highShift - lowShift);
      ShiftedLognormalVolModel shiftedBlack = new ShiftedLognormalVolModel(forward, expiry5, lnVols[j], shifts[j]);

      for (int i = 0; i <= nSamples; i++) {
        strikes[i] = lowStrike + (double) i / nSamples * (highStrike - lowStrike);
        prices[i] = shiftedBlack.priceFromFixedStrike(strikes[i]);
        vols[i] = new BlackFormula(forward, strikes[i], expiry5, null, prices[i], strikes[i] > forward).computeImpliedVolatility();

        // System.err.println(lnVols[j] + "," + shifts[j] + "," + strikes[i] + "," + vols[i]);
      }
    }
  }
}
