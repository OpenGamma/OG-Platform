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

import org.testng.annotations.Test;

/**
 * 
 */
public class VarSwapStaticReplicationTest {

  // The derivative
  final double varStrike = 0.05;
  final double varNotional = 3150;
  final double expiry1 = 1;
  final double expiry2 = 2;
  final double expiry5 = 5;

  final VarianceSwap swap1 = new VarianceSwap(0, expiry1, expiry1, 750, 750, 0, Currency.EUR, varStrike, varNotional);
  final VarianceSwap swap2 = new VarianceSwap(0, expiry2, expiry2, 750, 750, 0, Currency.EUR, varStrike, varNotional);
  final VarianceSwap swap5 = new VarianceSwap(0, expiry5, expiry5, 750, 750, 0, Currency.EUR, varStrike, varNotional);

  // The pricing method
  final VarSwapStaticReplication pricer = new VarSwapStaticReplication();

  // Market data
  double spot = 80;
  final YieldCurveBundle curves = TestsDataSets.createCurves1();
  final YieldAndDiscountCurve curveDiscount = curves.getCurve("Funding");

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0 };
  private static final double[] STRIKES = new double[] {80, 100, 120, 80, 100, 120, 80, 100, 120, 80, 100, 120 };
  private static final double[] VOLS = new double[] {0.2563287801311072, 0.2563287801311072, 0.2563287801311072,
                                                     0.25, 0.25, 0.25,
                                                     0.24968268826949546, 0.23968268826949546, 0.23968268826949546,
                                                     0.1594070379054433, 0.1594070379054433, 0.1594070379054433 };

  private static final CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR_1D = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  @SuppressWarnings("unchecked")
  private static final Interpolator2D INTERPOLATOR_2D = new GridInterpolator2D(new LinearInterpolator1D(), (Interpolator1D<Interpolator1DDataBundle>) INTERPOLATOR_1D);
  private static final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, INTERPOLATOR_2D);

  // Tests ------------------------------------------
  /**
   * Test of VolatilitySurface type
   */
  @Test
  public void testConstantDoublesSurface() {

    double vol = 0.25;
    double targetVar = swap1.getTimeToObsEnd() * vol * vol;

    final ConstantDoublesSurface constSurf = ConstantDoublesSurface.from(0.25);
    final VolatilitySurface vols = new VolatilitySurface(constSurf);
    final VarianceSwapDataBundle market = new VarianceSwapDataBundle(vols, curveDiscount, spot);

    double testVar = pricer.impliedVariance(swap1, market);
    System.out.println(testVar);

    assertEquals(testVar, targetVar, 1e-9);
  }

  /**
   * Test of VolatilitySurface type
   */
  @Test
  public void testInterpolatedDoublesSurfaceFlat() {

    double vol = 0.25;
    double targetVar = expiry1 * vol * vol;

    final VarianceSwapDataBundle market = new VarianceSwapDataBundle(new VolatilitySurface(SURFACE), curveDiscount, spot);

    double testVar = pricer.impliedVariance(swap1, market);
    System.out.println(testVar);

    assertEquals(testVar, targetVar, 1e-9);
  }

  /**
   * Confirm all is well when null values are passed for the cutoff, 
   * at least when the surface can handle low strikes itself
   */
  @Test
  public void testInterpolatedDoublesSurfaceWithoutCutoff() {

    VarSwapStaticReplication pricerNoCutoff = new VarSwapStaticReplication(1e-16, 10, new RungeKuttaIntegrator1D(), null, null);

    double vol = 0.25;
    double targetVar = expiry1 * vol * vol;

    final VarianceSwapDataBundle market = new VarianceSwapDataBundle(new VolatilitySurface(SURFACE), curveDiscount, spot);

    double testVar = pricerNoCutoff.impliedVariance(swap1, market);
    System.out.println(testVar);

    assertEquals(testVar, targetVar, 1e-9);
  }

  /** 
   * Confirm all is well when null values are passed for the cutoff, 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInterpolatedDoublesSurfaceWithPoorCutoffDescription() {

    VarSwapStaticReplication pricerBadCutoff = new VarSwapStaticReplication(1e-16, 10, new RungeKuttaIntegrator1D(), null, 0.05);

    double vol = 0.25;
    double targetVar = expiry1 * vol * vol;

    final VarianceSwapDataBundle market = new VarianceSwapDataBundle(new VolatilitySurface(SURFACE), curveDiscount, spot);

    double testVar = pricerBadCutoff.impliedVariance(swap1, market);
    System.out.println(testVar);

    assertEquals(testVar, targetVar, 1e-9);
  }

  /**
   * Although all we get from impliedVariance(swap,market) is the variance, it is clear that the fit is the Lognormal with zero shift,
   * as the variance produced is not affected.
   */
  @Test
  public void testShiftedLognormalFitOnFlatSurface() {

    VarSwapStaticReplication pricerHighCutoff = new VarSwapStaticReplication(1e-16, 10, new RungeKuttaIntegrator1D(), 0.95, 0.05);

    double vol = 0.25;
    double targetVar = expiry1 * vol * vol;

    final VarianceSwapDataBundle market = new VarianceSwapDataBundle(new VolatilitySurface(SURFACE), curveDiscount, spot);

    double testVar = pricerHighCutoff.impliedVariance(swap1, market);
    System.out.println(testVar);

    assertEquals(testVar, targetVar, 1e-9);
  }

  /**
   * Confirm that we can split an integral over [a,c] into [a,b]+[b,c]
   */
  @Test
  public void testIntegrationBounds() {

    VarSwapStaticReplication pricerPuts = new VarSwapStaticReplication(1e-16, 1.0, new RungeKuttaIntegrator1D(), 0.9, 0.01);
    VarSwapStaticReplication pricerCalls = new VarSwapStaticReplication(1.0, 10, new RungeKuttaIntegrator1D(), null, null);

    double vol = 0.25;
    double targetVar = expiry1 * vol * vol;

    final VarianceSwapDataBundle market = new VarianceSwapDataBundle(new VolatilitySurface(SURFACE), curveDiscount, spot);

    double varFromPuts = pricerPuts.impliedVariance(swap1, market);
    double varFromCalls = pricerCalls.impliedVariance(swap1, market);
    double testVar = varFromPuts + varFromCalls;
    System.out.println(varFromPuts);
    System.out.println(varFromCalls);
    System.out.println(testVar);

    assertEquals(testVar, targetVar, 1e-9);
  }

  /**
   * Test of VolatilitySurface with Smile and Skew
   */
  @Test
  public void testInterpolatedDoublesSurfaceV() {

    double vol = 0.25;
    double targetVar = swap5.getTimeToObsEnd() * vol * vol;

    final VarianceSwapDataBundle market = new VarianceSwapDataBundle(new VolatilitySurface(SURFACE), curveDiscount, spot);
    VarSwapStaticReplication pricerNoCutoff = new VarSwapStaticReplication(1e-16, 10, new RungeKuttaIntegrator1D(), 0.25, 0.01);

    double testVar = pricerNoCutoff.impliedVariance(swap5, market);
    System.out.println(testVar);

    //assertEquals(testVar, targetVar, 1e-9);
  }
}
