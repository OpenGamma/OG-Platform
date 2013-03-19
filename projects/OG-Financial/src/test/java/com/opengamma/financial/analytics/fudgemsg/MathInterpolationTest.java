/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.interpolation.LinearExtrapolator1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.StepInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class MathInterpolationTest extends AnalyticsTestBase {

  private static final StepInterpolator1D STEP = new StepInterpolator1D();
  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();
  private static final NaturalCubicSplineInterpolator1D CUBIC_SPLINE = new NaturalCubicSplineInterpolator1D();
  private static final FlatExtrapolator1D FLAT_EXTRAPOLATOR = new FlatExtrapolator1D();
  private static final LinearExtrapolator1D LINEAR_EXTRAPOLATOR = new LinearExtrapolator1D(CUBIC_SPLINE);
  private static final CombinedInterpolatorExtrapolator COMBINED = new CombinedInterpolatorExtrapolator(CUBIC_SPLINE,
      FLAT_EXTRAPOLATOR, LINEAR_EXTRAPOLATOR);
  private static final GridInterpolator2D GRID_2D = new GridInterpolator2D(LINEAR, STEP);

  @Test
  public void testInterpolator1D() {
    Interpolator1D cycled = cycleObject(Interpolator1D.class, LINEAR);
    assertEquals(cycled, LINEAR);
    cycled = cycleObject(Interpolator1D.class, CUBIC_SPLINE);
    assertEquals(cycled, CUBIC_SPLINE);
  }

  @Test
  public void testCombinedInterpolator() {
    final CombinedInterpolatorExtrapolator cycled = cycleObject(CombinedInterpolatorExtrapolator.class, COMBINED);
    assertEquals(cycled, COMBINED);
  }

  @Test
  public void testGridInterpolator2D() {
    final Interpolator2D cycled = cycleObject(Interpolator2D.class, GRID_2D);
    assertEquals(cycled, GRID_2D);
  }
}
