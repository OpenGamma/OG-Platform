/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import static com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory.getSensitivityCalculator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactoryTest {
  @Test(expected = IllegalArgumentException.class)
  public void testBadInterpolatorName1() {
    getSensitivityCalculator("Wrong name");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadInterpolatorName2() {
    getSensitivityCalculator("Wrong name", Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadInterpolatorName3() {
    getSensitivityCalculator("Wrong name", Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadExtrapolatorName1() {
    getSensitivityCalculator(Interpolator1DFactory.LINEAR, "Wrong name");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadExtrapolatorName2() {
    getSensitivityCalculator(Interpolator1DFactory.LINEAR, "Wrong name", Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadExtrapolatorName3() {
    getSensitivityCalculator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, "Wrong name");
  }

  @Test
  public void testNullExtrapolatorName() {
    final CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> combined = getSensitivityCalculator(Interpolator1DFactory.LINEAR, null);
    assertEquals(combined.getSensitivityCalculator().getClass(), LinearInterpolator1DNodeSensitivityCalculator.class);
    assertNull(combined.getLeftSensitivityCalculator());
    assertNull(combined.getRightSensitivityCalculator());
  }

  @Test
  public void testEmptyExtrapolatorName() {
    final CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> combined = getSensitivityCalculator(Interpolator1DFactory.LINEAR, "");
    assertEquals(combined.getSensitivityCalculator().getClass(), LinearInterpolator1DNodeSensitivityCalculator.class);
    assertNull(combined.getLeftSensitivityCalculator());
    assertNull(combined.getRightSensitivityCalculator());
  }

  @Test
  public void testNullLeftExtrapolatorName() {
    final CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> combined = getSensitivityCalculator(Interpolator1DFactory.LINEAR, null,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    assertEquals(combined.getSensitivityCalculator().getClass(), LinearInterpolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getLeftSensitivityCalculator().getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getRightSensitivityCalculator().getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
  }

  @Test
  public void testEmptyLeftExtrapolatorName() {
    final CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> combined = getSensitivityCalculator(Interpolator1DFactory.LINEAR, "",
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    assertEquals(combined.getSensitivityCalculator().getClass(), LinearInterpolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getLeftSensitivityCalculator().getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getRightSensitivityCalculator().getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
  }

  @Test
  public void testNullRightExtrapolatorName() {
    final CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> combined = getSensitivityCalculator(Interpolator1DFactory.LINEAR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR, null);
    assertEquals(combined.getSensitivityCalculator().getClass(), LinearInterpolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getLeftSensitivityCalculator().getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getRightSensitivityCalculator().getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
  }

  @Test
  public void testEmptyRightExtrapolatorName() {
    final CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> combined = getSensitivityCalculator(Interpolator1DFactory.LINEAR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR, "");
    assertEquals(combined.getSensitivityCalculator().getClass(), LinearInterpolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getLeftSensitivityCalculator().getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getRightSensitivityCalculator().getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
  }

  @Test
  public void testNullLeftAndRightExtrapolatorName() {
    final CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> combined = getSensitivityCalculator(Interpolator1DFactory.LINEAR, null, null);
    assertEquals(combined.getSensitivityCalculator().getClass(), LinearInterpolator1DNodeSensitivityCalculator.class);
    assertNull(combined.getLeftSensitivityCalculator());
    assertNull(combined.getRightSensitivityCalculator());
  }

  @Test
  public void testEmptyLeftAndRightExtrapolatorName() {
    final CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> combined = getSensitivityCalculator(Interpolator1DFactory.LINEAR, "", "");
    assertEquals(combined.getSensitivityCalculator().getClass(), LinearInterpolator1DNodeSensitivityCalculator.class);
    assertNull(combined.getLeftSensitivityCalculator());
    assertNull(combined.getRightSensitivityCalculator());
  }

  @Test
  public void testNoExtrapolator() {
    CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> combined = getSensitivityCalculator(Interpolator1DFactory.LINEAR);
    assertEquals(combined.getSensitivityCalculator().getClass(), LinearInterpolator1DNodeSensitivityCalculator.class);
    assertNull(combined.getLeftSensitivityCalculator());
    assertNull(combined.getRightSensitivityCalculator());
    combined = getSensitivityCalculator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE);
    assertEquals(combined.getSensitivityCalculator().getClass(), NaturalCubicSplineInterpolator1DNodeSensitivityCalculator.class);
  }

  @Test
  public void testOneExtrapolator() {
    CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> combined = getSensitivityCalculator(Interpolator1DFactory.LINEAR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    assertEquals(combined.getSensitivityCalculator().getClass(), LinearInterpolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getLeftSensitivityCalculator().getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getRightSensitivityCalculator().getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
    combined = getSensitivityCalculator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    assertEquals(combined.getSensitivityCalculator().getClass(), NaturalCubicSplineInterpolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getLeftSensitivityCalculator().getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getRightSensitivityCalculator().getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
  }

  @Test
  public void testTwoExtrapolators() {
    CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<? extends Interpolator1DDataBundle> combined = getSensitivityCalculator(Interpolator1DFactory.LINEAR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    assertEquals(combined.getSensitivityCalculator().getClass(), LinearInterpolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getLeftSensitivityCalculator().getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getRightSensitivityCalculator().getClass(), LinearExtrapolator1DNodeSensitivityCalculator.class);
    combined = getSensitivityCalculator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    assertEquals(combined.getSensitivityCalculator().getClass(), NaturalCubicSplineInterpolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getLeftSensitivityCalculator().getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
    assertEquals(combined.getRightSensitivityCalculator().getClass(), LinearExtrapolator1DNodeSensitivityCalculator.class);
  }
}
