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

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0 };
  private static final double[] STRIKES = new double[] {80, 100, 120, 80, 100, 120, 80, 100, 120, 80, 100, 120 };
  private static final double[] VOLS = new double[] {0.2563287801311072, 0.2563287801311072, 0.2563287801311072,
                                                     0.25, 0.25, 0.25,
                                                     0.20468268826949546, 0.20468268826949546, 0.20468268826949546,
                                                     0.1594070379054433, 0.1594070379054433, 0.1594070379054433 };

  private static final CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR_1D = getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
                                                                                        Interpolator1DFactory.FLAT_EXTRAPOLATOR,
                                                                                        Interpolator1DFactory.LINEAR_EXTRAPOLATOR);

  private static final Interpolator2D INTERPOLATOR_2D = new GridInterpolator2D(new LinearInterpolator1D(), (Interpolator1D<Interpolator1DDataBundle>) INTERPOLATOR_1D);
  private static final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, INTERPOLATOR_2D);

  final double varStrike = 0.05;
  final double varNotional = 3150;
  final double expiry = 1;

  final VarianceSwap swap = new VarianceSwap(0, expiry, expiry, 750, 750, 0, Currency.EUR, varStrike, varNotional);

  double spot = 80;
  final YieldCurveBundle curves = TestsDataSets.createCurves1();
  final YieldAndDiscountCurve curveDiscount = curves.getCurve("Funding");

  @Test
  public void testConstantDoublesSurface() {

    double vol = 0.25;
    double targetVar = expiry * vol * vol;

    final ConstantDoublesSurface constSurf = ConstantDoublesSurface.from(0.25);
    final VolatilitySurface vols = new VolatilitySurface(constSurf);
    final VarianceSwapDataBundle market = new VarianceSwapDataBundle(vols, curveDiscount, spot);

    double testVar = VarSwapStaticReplication.impliedVariance(swap, market);
    System.out.println(testVar);

    assertEquals(testVar, targetVar, 1e-9);
  }

  @Test
  public void testInterpolatedDoublesSurface() {

    double vol = 0.25;
    double targetVar = expiry * vol * vol;

    final VarianceSwapDataBundle market = new VarianceSwapDataBundle(new VolatilitySurface(SURFACE), curveDiscount, spot);

    double testVar = VarSwapStaticReplication.impliedVariance(swap, market);
    System.out.println(testVar);

    assertEquals(testVar, targetVar, 1e-9);
  }
}
