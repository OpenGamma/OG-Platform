/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.FlatExtrapolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.math.interpolation.LinearExtrapolator1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.math.interpolation.StepInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DCubicSplineDataBundle;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class MathInterpolationTest extends AnalyticsTestBase {
  private static final StepInterpolator1D STEP = new StepInterpolator1D();
  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();
  private static final NaturalCubicSplineInterpolator1D CUBIC_SPLINE = new NaturalCubicSplineInterpolator1D();
  private static final FlatExtrapolator1D<Interpolator1DCubicSplineDataBundle> FLAT_EXTRAPOLATOR = new FlatExtrapolator1D<Interpolator1DCubicSplineDataBundle>();
  private static final LinearExtrapolator1D<Interpolator1DCubicSplineDataBundle> LINEAR_EXTRAPOLATOR = new LinearExtrapolator1D<Interpolator1DCubicSplineDataBundle>(CUBIC_SPLINE);
  private static final CombinedInterpolatorExtrapolator<Interpolator1DCubicSplineDataBundle> COMBINED = new CombinedInterpolatorExtrapolator<Interpolator1DCubicSplineDataBundle>(CUBIC_SPLINE,
      FLAT_EXTRAPOLATOR, LINEAR_EXTRAPOLATOR);
  private static final GridInterpolator2D GRID_2D = new GridInterpolator2D(LINEAR, STEP);

  @SuppressWarnings("unchecked")
  @Test
  public void testInterpolator1D() {
    Interpolator1D<? extends Interpolator1DDataBundle> cycled = cycleObject(Interpolator1D.class, LINEAR);
    assertEquals(cycled, LINEAR);
    cycled = cycleObject(Interpolator1D.class, CUBIC_SPLINE);
    assertEquals(cycled, CUBIC_SPLINE);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCombinedInterpolator() {
    final CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> cycled = cycleObject(CombinedInterpolatorExtrapolator.class, COMBINED);
    assertEquals(cycled, COMBINED);
  }

  @Test
  public void testGridInterpolator2D() {
    final Interpolator2D cycled = cycleObject(Interpolator2D.class, GRID_2D);
    assertEquals(cycled, GRID_2D);
  }
}
