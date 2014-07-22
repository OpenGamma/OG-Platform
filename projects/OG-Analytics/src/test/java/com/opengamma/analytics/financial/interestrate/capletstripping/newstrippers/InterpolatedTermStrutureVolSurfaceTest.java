/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping.newstrippers;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStrippingSetup;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.TransformedInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;

/**
 * 
 */
public class InterpolatedTermStrutureVolSurfaceTest extends CapletStrippingSetup {

  private static final VolatilitySurfaceProvider VOL_SURF_PROV;
  private static final ParameterLimitsTransform TRANSFORM;
  private static final Interpolator1D INTERPOLATOR;
  private static final double[] KNOTS = new double[] {1, 2, 3, 4, 5, 7, 10 };

  static {

    final Interpolator1D baseInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    TRANSFORM = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);
    INTERPOLATOR = new TransformedInterpolator1D(baseInterpolator, TRANSFORM);
    VOL_SURF_PROV = new InterpolatedVolatilityTermStructureProvider(KNOTS, INTERPOLATOR);
  }

  //******************************************************************************************************
  //These are global (i.e across all strikes) fits using a model with no strike variation 
  //******************************************************************************************************
  @Test
  public void priceFitTest() {
    final CapletStripper stripper = new CapletStripper(getAllCaps(), getYieldCurves(), VOL_SURF_PROV);
    final int n = VOL_SURF_PROV.getNumModelParameters();
    final LeastSquareResults res = stripper.leastSqrSolveForCapPrices(getAllCapPrices(), new DoubleMatrix1D(n, 0.4));

    final int nKnots = KNOTS.length;
    final VolatilitySurface vs = VOL_SURF_PROV.getVolSurface(res.getFitParameters());
    final double[] expected = new double[] {1.5005943508486885, 0.881917510522987, 0.6782635291456757, 0.5404131363370122, 0.37753021973438206, 0.39337662998268913, 0.1578210650542591 };
    for (int i = 0; i < nKnots; i++) {
      final double vol = vs.getVolatility(KNOTS[i], 1.0); //strike is irrelevant 
      assertEquals(expected[i], vol, 1e-15);
      //  System.out.println(KNOTS[i] + "\t" + vol);
    }
  }

  @Test
  public void priceVegaFitTest() {
    final CapletStripper stripper = new CapletStripper(getAllCaps(), getYieldCurves(), VOL_SURF_PROV);
    final MultiCapFloorPricer2 pricer = new MultiCapFloorPricer2(getAllCaps(), getYieldCurves());
    final double[] capVols = getAllCapVols();
    final double[] vega = pricer.vega(capVols);
    final double[] prices = pricer.price(capVols);
    final int n = VOL_SURF_PROV.getNumModelParameters();
    final LeastSquareResults res = stripper.leastSqrSolveForCapPrices(prices, vega, new DoubleMatrix1D(n, 0.4));

    final int nKnots = KNOTS.length;
    final VolatilitySurface vs = VOL_SURF_PROV.getVolSurface(res.getFitParameters());
    final double[] expected = new double[] {0.7908281826425202, 0.8202635873345728, 0.6146224953598394, 0.5301436957465266, 0.450632487793525, 0.3193622739501331, 0.33295740508698773 };
    for (int i = 0; i < nKnots; i++) {
      final double vol = vs.getVolatility(KNOTS[i], 1.0); //strike is irrelevant 
      assertEquals(expected[i], vol, 1e-15);
      //System.out.println(KNOTS[i] + "\t" + vol);
    }
  }

  @Test
  public void volFitTest() {
    final double[] vols = getAllCapVols();
    final CapletStripper stripper = new CapletStripper(getAllCaps(), getYieldCurves(), VOL_SURF_PROV);
    final int n = VOL_SURF_PROV.getNumModelParameters();
    final LeastSquareResults res = stripper.leastSqrSolveForCapVols(vols, new DoubleMatrix1D(n, 0.4));

    final int nKnots = KNOTS.length;
    final VolatilitySurface vs = VOL_SURF_PROV.getVolSurface(res.getFitParameters());
    final double[] expected = new double[] {0.8981019131120316, 0.7880630646351185, 0.641920514517539, 0.5021924650455317, 0.5030338263866614, 0.2578060893211363, 0.5074588007227301 };
    for (int i = 0; i < nKnots; i++) {
      final double vol = vs.getVolatility(KNOTS[i], 1.0); //strike is irrelevant 
      assertEquals(expected[i], vol, 1e-15);
      //  System.out.println(KNOTS[i] + "\t" + vol);
    }
  }

  //******************************************************************************************************
  //Fit the ATM caps only 
  //******************************************************************************************************
  /**
   * here we fit just the ATM caps (implicitly) assuming there is no smile 
   */
  @Test
  public void priceFitATMTest() {
    final double[] expected = new double[] {0.7362162321445822, 0.8341909447608759, 0.9163146810114016, 0.4976592928612058, 0.7160001917232186, 0.0906099950686755, 2.629298767276271 };
    priceFitATMTest(KNOTS, VOL_SURF_PROV, expected);
  }

  /**
   * Get a more sensible looking term structure by slightly changing the knot positions 
   */
  @Test
  public void priceFitATMTest2() {
    final double[] knots = new double[] {0., 1, 2, 3, 4, 5, 7 };
    final InterpolatedVolatilityTermStructureProvider surfaceProvider = new InterpolatedVolatilityTermStructureProvider(knots, INTERPOLATOR);
    final double[] expected = new double[] {0.8011335645948967, 0.6725080484498808, 0.9391840682157704, 0.7797794758267345, 0.6465156748887224, 0.460706261635471, 0.3667784593021372 };
    priceFitATMTest(knots, surfaceProvider, expected);
  }

  private void priceFitATMTest(final double[] knots, final VolatilitySurfaceProvider volSurfProvider, final double[] expected) {
    final CapletStripper stripper = new CapletStripper(getATMCaps(), getYieldCurves(), volSurfProvider);
    final int n = volSurfProvider.getNumModelParameters();
    final LeastSquareResults res = stripper.leastSqrSolveForCapPrices(getATMCapPrices(), new DoubleMatrix1D(n, 0.4));

    //should get close to an exact fit even if actual vol term structure is bonkers 
    assertTrue(res.getChiSq() < 1e-14);

    final int nKnots = knots.length;
    final VolatilitySurface vs = volSurfProvider.getVolSurface(res.getFitParameters());

    for (int i = 0; i < nKnots; i++) {
      final double vol = vs.getVolatility(knots[i], 1.0); //strike is irrelevant 
      assertEquals(expected[i], vol, 1e-15);
      // System.out.println(knots[i] + "\t" + vol);
    }
  }

  /**
   * fit ATM using a root-finder rather than least squares 
   */
  @Test
  void rootFindATMTest() {
    final double[] knots = new double[] {0., 1, 2, 3, 4, 5, 7 };
    final InterpolatedVolatilityTermStructureProvider surfaceProvider = new InterpolatedVolatilityTermStructureProvider(knots, INTERPOLATOR);
    final double[] expected = new double[] {0.8011335645948967, 0.6725080484498808, 0.9391840682157704, 0.7797794758267345, 0.6465156748887224, 0.460706261635471, 0.3667784593021372 };

    final CapletStripper stripper = new CapletStripper(getATMCaps(), getYieldCurves(), surfaceProvider);
    final int n = surfaceProvider.getNumModelParameters();
    final DoubleMatrix1D res = stripper.rootFindForCapPrices(getATMCapPrices(), new DoubleMatrix1D(n, 0.4));

    final int nKnots = knots.length;
    final VolatilitySurface vs = surfaceProvider.getVolSurface(res);

    for (int i = 0; i < nKnots; i++) {
      final double vol = vs.getVolatility(knots[i], 1.0); //strike is irrelevant 
      //  assertEquals(expected[i], vol, 1e-15);
      System.out.println(knots[i] + "\t" + vol);
    }

  }

}
