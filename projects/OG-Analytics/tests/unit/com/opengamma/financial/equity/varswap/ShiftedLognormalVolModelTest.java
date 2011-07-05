/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap;

import static com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;

import com.opengamma.financial.equity.varswap.derivative.VarianceSwap;
import com.opengamma.financial.equity.varswap.pricing.ShiftedLognormalVolModel;
import com.opengamma.financial.equity.varswap.pricing.VarSwapStaticReplication;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.BlackFormula;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.rootfinding.VectorRootFinder;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;

import org.testng.annotations.Test;

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

  final VarianceSwap swap1 = new VarianceSwap(0, expiry1, expiry1, varStrike, varNotional, Currency.EUR, 250, nObsExpected, null, null);
  final VarianceSwap swap5 = new VarianceSwap(0, expiry5, expiry5, varStrike, varNotional, Currency.EUR, 250, nObsExpected, null, null);

  // The pricing method
  final VarSwapStaticReplication pricer_default_w_cutoff = new VarSwapStaticReplication();
  final VarSwapStaticReplication pricer_null_cutoff = new VarSwapStaticReplication(1.0E-4, 5.0, new RungeKuttaIntegrator1D(), null, null);

  // Market data
  double spot = 80;
  double forward = 100;
  final YieldCurveBundle curves = TestsDataSets.createCurves1();
  final YieldAndDiscountCurve curveDiscount = curves.getCurve("Funding");

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 0.5,
                                                          1.0, 1.0, 1.0, 1.0,
                                                          5.0, 5.0, 5.0, 5.0 };
  private static final double[] STRIKES = new double[] {40, 80, 100, 120,
                                                        40, 80, 100, 120,
                                                        40, 80, 100, 120 };
  private static final double[] VOLS = new double[] {0.28, 0.28, 0.28, 0.28,
                                                     0.25, 0.25, 0.25, 0.25,
                                                     0.26, 0.24, 0.23, 0.25 };

  private static final CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR_1D = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  @SuppressWarnings("unchecked")
  private static final Interpolator2D INTERPOLATOR_2D = new GridInterpolator2D(new LinearInterpolator1D(), INTERPOLATOR_1D);
  private static final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, INTERPOLATOR_2D);

  private static final VolatilitySurface VOLSURFACE = new VolatilitySurface(SURFACE);

  // -------------------------------- TESTS  ------------------------------------------
  @Test
  public void testFlatSurfaceReproducesVolAndZeroShift() {
    ShiftedLognormalVolModel logBlack = new ShiftedLognormalVolModel(forward, expiry1, VOLSURFACE, 0.4, 0.8); //, 0.26, 0.01, testSolver);

    assertEquals(logBlack.getVol(), 0.25, 1e-9);
    assertEquals(logBlack.getShift(), 0.0, 1e-9);
  }

  @Test
  public void testConstantDoublesSurface() {
    ShiftedLognormalVolModel logBlack = new ShiftedLognormalVolModel(forward, expiry5, new VolatilitySurface(ConstantDoublesSurface.from(0.25)), 0.4, 0.8);

    assertEquals(logBlack.getVol(), 0.25, 1e-9);
    assertEquals(logBlack.getShift(), 0.0, 1e-9);
  }

  @Test
  public void testOnSmileySurface() {
    final VectorRootFinder testSolver = new BroydenVectorRootFinder(1.0e-6, 1.0e-6, 50000);
    ShiftedLognormalVolModel logBlack = new ShiftedLognormalVolModel(forward, expiry5, VOLSURFACE, 0.4, 0.41, 0.26, 0.01, testSolver);

    assertEquals(logBlack.getVol(), 0.22306620890629011, 1e-8);
    assertEquals(logBlack.getShift(), 0.09950883623451598, 1e-8);
  }

  @Test
  public void testPricingAndImpVol() {
    ShiftedLognormalVolModel shiftedBlack = new ShiftedLognormalVolModel(forward, expiry5, VOLSURFACE, 0.4, 0.41);

    assertEquals(shiftedBlack.getVol(), 0.22306620890629011, 1e-8);
    assertEquals(shiftedBlack.getShift(), 0.09950883623451598, 1e-8);

    double highStrike = 5.0;
    double lowStrike = 1e-4;
    int nSamples = 500;
    double[] strikes = new double[nSamples + 1];
    double[] prices = new double[nSamples + 1];
    double[] vols = new double[nSamples + 1];
    for (int i = 0; i <= nSamples; i++) {
      strikes[i] = lowStrike + (double) i / nSamples * (highStrike - lowStrike);
      prices[i] = shiftedBlack.priceFromRelativeStrike(strikes[i]);
      vols[i] = new BlackFormula(forward, strikes[i] * forward, expiry5, null, prices[i], strikes[i] > 1).computeImpliedVolatility();
    }
  }
}
