/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CombinedInterpolatorExtrapolatorFactoryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadInterpolatorName1() {
    getInterpolator("Wrong name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadInterpolatorName2() {
    getInterpolator("Wrong name", Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadInterpolatorName3() {
    getInterpolator("Wrong name", Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadExtrapolatorName1() {
    getInterpolator(Interpolator1DFactory.LINEAR, "Wrong name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadExtrapolatorName2() {
    getInterpolator(Interpolator1DFactory.LINEAR, "Wrong name", Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadExtrapolatorName3() {
    getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, "Wrong name");
  }

  @Test
  public void testNullExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(Interpolator1DFactory.LINEAR, null);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertNull(combined.getLeftExtrapolator());
    assertNull(combined.getRightExtrapolator());
  }

  @Test
  public void testEmptyExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(Interpolator1DFactory.LINEAR, "");
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertNull(combined.getLeftExtrapolator());
    assertNull(combined.getRightExtrapolator());
  }

  @Test
  public void testNullLeftExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(Interpolator1DFactory.LINEAR, null, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), FlatExtrapolator1D.class);
  }

  @Test
  public void testEmptyLeftExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(Interpolator1DFactory.LINEAR, "", Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), FlatExtrapolator1D.class);
  }

  @Test
  public void testNullRightExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, null);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), FlatExtrapolator1D.class);
  }

  @Test
  public void testEmptyRightExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, "");
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), FlatExtrapolator1D.class);
  }

  @Test
  public void testNullLeftAndRightExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(Interpolator1DFactory.LINEAR, null, null);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertNull(combined.getLeftExtrapolator());
    assertNull(combined.getRightExtrapolator());
  }

  @Test
  public void testEmptyLeftAndRightExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(Interpolator1DFactory.LINEAR, "", "");
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertNull(combined.getLeftExtrapolator());
    assertNull(combined.getRightExtrapolator());
  }

  @Test
  public void testNoExtrapolator() {
    CombinedInterpolatorExtrapolator combined = getInterpolator(Interpolator1DFactory.LINEAR);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertNull(combined.getLeftExtrapolator());
    assertNull(combined.getRightExtrapolator());
    combined = getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE);
    assertEquals(combined.getInterpolator().getClass(), NaturalCubicSplineInterpolator1D.class);
  }

  @Test
  public void testOneExtrapolator() {
    CombinedInterpolatorExtrapolator combined = getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), FlatExtrapolator1D.class);
    combined = getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    assertEquals(combined.getInterpolator().getClass(), NaturalCubicSplineInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), FlatExtrapolator1D.class);
  }

  @Test
  public void testTwoExtrapolators1() {
    CombinedInterpolatorExtrapolator combined = getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), LinearExtrapolator1D.class);
    combined = getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    assertEquals(combined.getInterpolator().getClass(), NaturalCubicSplineInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), LinearExtrapolator1D.class);
  }

  @Test
  public void testTwoExtrapolators2() {
    Interpolator1D interp = new ProductPiecewisePolynomialInterpolator1D(new NaturalSplineInterpolator());
    CombinedInterpolatorExtrapolator combined = new CombinedInterpolatorExtrapolator(interp,
        new FlatExtrapolator1D(), new ReciprocalExtrapolator1D(interp));
    assertEquals(ProductPiecewisePolynomialInterpolator1D.class, combined.getInterpolator().getClass());
    assertEquals(FlatExtrapolator1D.class, combined.getLeftExtrapolator().getClass());
    assertEquals(ReciprocalExtrapolator1D.class, combined.getRightExtrapolator().getClass());
    combined = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        Interpolator1DFactory.PRODUCT_NATURAL_CUBIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.RECIPROCAL_EXTRAPOLATOR);
    assertEquals(ProductPiecewisePolynomialInterpolator1D.class, combined.getInterpolator().getClass());
    assertEquals(FlatExtrapolator1D.class, combined.getLeftExtrapolator().getClass());
    assertEquals(ReciprocalExtrapolator1D.class, combined.getRightExtrapolator().getClass());
  }
}
