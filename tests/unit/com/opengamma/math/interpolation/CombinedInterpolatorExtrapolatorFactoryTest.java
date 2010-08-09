/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class CombinedInterpolatorExtrapolatorFactoryTest {

  @Test(expected = IllegalArgumentException.class)
  public void testBadInterpolatorName1() {
    getInterpolator("Wrong name");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadInterpolatorName2() {
    getInterpolator("Wrong name", Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadInterpolatorName3() {
    getInterpolator("Wrong name", Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadExtrapolatorName1() {
    getInterpolator(Interpolator1DFactory.LINEAR, "Wrong name");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadExtrapolatorName2() {
    getInterpolator(Interpolator1DFactory.LINEAR, "Wrong name", Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadExtrapolatorName3() {
    getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, "Wrong name");
  }

  @Test
  public void testNoExtrapolator() {
    CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> combined = getInterpolator(Interpolator1DFactory.LINEAR);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertNull(combined.getLeftExtrapolator());
    assertNull(combined.getRightExtrapolator());
    combined = getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE);
    assertEquals(combined.getInterpolator().getClass(), NaturalCubicSplineInterpolator1D.class);
  }

  @Test
  public void testOneExtrapolator() {
    CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> combined = getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), FlatExtrapolator1D.class);
    combined = getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    assertEquals(combined.getInterpolator().getClass(), NaturalCubicSplineInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), FlatExtrapolator1D.class);
  }

  @Test
  public void testTwoExtrapolators() {
    CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> combined = getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), LinearExtrapolator1D.class);
    combined = getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    assertEquals(combined.getInterpolator().getClass(), NaturalCubicSplineInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), LinearExtrapolator1D.class);
  }
}
