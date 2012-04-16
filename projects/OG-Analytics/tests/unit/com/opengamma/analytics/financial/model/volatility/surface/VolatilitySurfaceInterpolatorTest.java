/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;

/**
 * 
 */
public class VolatilitySurfaceInterpolatorTest {
  private static final GeneralSmileInterpolator SMILE_INTERPOLATOR = new SmileInterpolatorSpline();
  private static final Interpolator1D TIME_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final boolean USE_LOG_TIME = false;
  private static final boolean USE_INTEGRATED_VARIANCE = true;
  private static final boolean USE_LOG_VALUE = true;
  private static final VolatilitySurfaceInterpolator INTERPOLATOR = new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, TIME_INTERPOLATOR, USE_LOG_TIME,
      USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSmileInterpolator() {
    new VolatilitySurfaceInterpolator(null, TIME_INTERPOLATOR, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTimeInterpolator() {
    new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, null, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData1() {
    INTERPOLATOR.getIndependentSmileFits(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData2() {
    INTERPOLATOR.getBumpedVolatilitySurface(null, 0, 0, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData3() {
    INTERPOLATOR.getVolatilitySurface(null);
  }

  @Test
  public void testGetters() {
    assertEquals(INTERPOLATOR.useIntegratedVariance(), USE_INTEGRATED_VARIANCE);
    assertEquals(INTERPOLATOR.useLogTime(), USE_LOG_TIME);
    assertEquals(INTERPOLATOR.useLogValue(), USE_LOG_VALUE);
    assertEquals(INTERPOLATOR.getSmileInterpolator(), SMILE_INTERPOLATOR);
    assertEquals(INTERPOLATOR.getTimeInterpolator(), TIME_INTERPOLATOR);
  }

  @Test
  public void testHashCodeEquals() {
    VolatilitySurfaceInterpolator other = new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, TIME_INTERPOLATOR, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
    assertEquals(INTERPOLATOR, other);
    assertEquals(INTERPOLATOR.hashCode(), other.hashCode());
    other = new VolatilitySurfaceInterpolator(new SmileInterpolatorSABR(), TIME_INTERPOLATOR, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
    assertFalse(other.equals(INTERPOLATOR));
    other = new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, Interpolator1DFactory.LINEAR_INSTANCE, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
    assertFalse(other.equals(INTERPOLATOR));
    other = new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, Interpolator1DFactory.LINEAR_INSTANCE, !USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
    assertFalse(other.equals(INTERPOLATOR));
    other = new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, Interpolator1DFactory.LINEAR_INSTANCE, USE_LOG_TIME, !USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
    assertFalse(other.equals(INTERPOLATOR));
    other = new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, Interpolator1DFactory.LINEAR_INSTANCE, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, !USE_LOG_VALUE);
    assertFalse(other.equals(INTERPOLATOR));
  }
}
