/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.data;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.MonotoneConvexSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.PiecewisePolynomialInterpolator1D;
import com.opengamma.analytics.math.interpolation.ShapePreservingCubicSplineInterpolator1D;

/**
 * 
 */
public class Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundleTest {
  /**
   * 
   */
  @Test
  public void hashCodeEqualsTest() {
    final double[] x = new double[] {1., 2., 3., 4., 5. };
    final double[] y = new double[] {1., 2., 3., 4., 5. };
    final double[] x1 = new double[] {1., 2., 3., 3.5, 5. };

    final PiecewisePolynomialInterpolator1D interp1 = new MonotoneConvexSplineInterpolator1D();
    final PiecewisePolynomialInterpolator1D interp2 = new MonotoneConvexSplineInterpolator1D();
    final PiecewisePolynomialInterpolator1D interp3 = new ShapePreservingCubicSplineInterpolator1D();

    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle bundle1 = new Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle(interp1.getDataBundle(x, y), interp1.getInterpolator());
    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle bundle2 = new Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle(interp2.getDataBundle(x, y), interp2.getInterpolator());
    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle bundle3 = new Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle(interp3.getDataBundle(x, y), interp3.getInterpolator());
    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle bundle4 = new Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle(interp1.getDataBundle(x1, y), interp1.getInterpolator());

    assertTrue(bundle1.equals(bundle1));

    assertTrue(bundle1.equals(bundle2));
    assertTrue(bundle2.equals(bundle1));
    assertTrue(bundle1.hashCode() == bundle2.hashCode());

    assertTrue(!(bundle1.hashCode() == bundle3.hashCode()));
    assertTrue(!(bundle1.equals(bundle3)));
    assertTrue(!(bundle3.equals(bundle1)));

    assertTrue(!(bundle1.hashCode() == bundle4.hashCode()));
    assertTrue(!(bundle1.equals(bundle4)));
    assertTrue(!(bundle4.equals(bundle1)));

    assertTrue(!(bundle1.equals(null)));
    assertTrue(!(bundle1.equals(interp1)));
  }
}
