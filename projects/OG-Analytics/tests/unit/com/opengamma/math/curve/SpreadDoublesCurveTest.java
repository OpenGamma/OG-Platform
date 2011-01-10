/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * 
 */
@SuppressWarnings("unchecked")
public class SpreadDoublesCurveTest {
  private static final double[] X = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
  private static final double[] Y1 = new double[] {2, 4, 6, 8, 10, 12, 14, 16, 18};
  private static final double[] Y2 = new double[] {1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1};
  private static final InterpolatedDoublesCurve INTERPOLATED1 = InterpolatedDoublesCurve.from(X, Y1, new LinearInterpolator1D(), "a");
  private static final InterpolatedDoublesCurve INTERPOLATED2 = InterpolatedDoublesCurve.from(X, Y2, new LinearInterpolator1D(), "b");
  private static final CurveSpreadFunction ADD = new AddCurveSpreadFunction();
  private static final CurveSpreadFunction SUBTRACT = new SubtractCurveSpreadFunction();
  private static final String NAME1 = "X";
  private static final String NAME2 = "Y";
  private static final String NAME3 = "Z";
  private static final Curve<Double, Double>[] CURVES1 = new Curve[] {INTERPOLATED1, INTERPOLATED2};
  private static final SpreadDoublesCurve SPREAD1 = SpreadDoublesCurve.from(CURVES1, ADD, NAME1);
  private static final Curve<Double, Double>[] CURVES2 = new Curve[] {SPREAD1, INTERPOLATED1};
  private static final SpreadDoublesCurve SPREAD2 = SpreadDoublesCurve.from(CURVES2, SUBTRACT, NAME2);
  private static final Curve<Double, Double>[] CURVES3 = new Curve[] {INTERPOLATED1, INTERPOLATED1, INTERPOLATED1};
  private static final SpreadDoublesCurve SPREAD3 = SpreadDoublesCurve.from(CURVES3, ADD, NAME3);

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurves1() {
    new SpreadDoublesCurve(null, ADD);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTooFewCurves1() {
    new SpreadDoublesCurve(new Curve[] {INTERPOLATED1}, ADD);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSpreadFunction1() {
    new SpreadDoublesCurve(CURVES1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurves2() {
    new SpreadDoublesCurve(null, ADD, NAME1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTooFewCurves2() {
    new SpreadDoublesCurve(new Curve[] {INTERPOLATED1}, ADD, NAME1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSpreadFunction2() {
    new SpreadDoublesCurve(CURVES1, null, NAME1);
  }

  @Test
  public void testHashCodeAndEquals() {
    SpreadDoublesCurve other = SpreadDoublesCurve.from(CURVES1, ADD, NAME1);
    assertEquals(other, SPREAD1);
    assertEquals(other.hashCode(), SPREAD1.hashCode());
    other = SpreadDoublesCurve.from(CURVES2, ADD, NAME1);
    assertFalse(other.equals(SPREAD1));
    other = SpreadDoublesCurve.from(CURVES1, SUBTRACT, NAME1);
    assertFalse(other.equals(SPREAD1));
    other = SpreadDoublesCurve.from(CURVES1, ADD, NAME2);
    assertFalse(other.equals(SPREAD1));
    other = SpreadDoublesCurve.from(CURVES1, ADD);
    assertFalse(other.equals(SPREAD1));
    other = SpreadDoublesCurve.from(CURVES1, ADD);
    assertFalse(other.equals(SPREAD1));
    other = new SpreadDoublesCurve(CURVES1, ADD);
    assertFalse(other.equals(SPREAD1));
    other = new SpreadDoublesCurve(CURVES1, ADD);
    assertFalse(other.equals(SPREAD1));
  }

  @Test
  public void testStaticConstructors() {
    assertEquals(new SpreadDoublesCurve(CURVES1, ADD, NAME1), SPREAD1);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetXValues() {
    SPREAD1.getXData();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetYValues() {
    SPREAD1.getYData();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetSize() {
    SPREAD1.size();
  }

  @Test
  public void testGetters() {
    assertEquals(SPREAD1.getName(), NAME1);
    assertArrayEquals(SPREAD1.getUnderlyingCurves(), CURVES1);
    assertEquals(SPREAD2.getName(), NAME2);
    assertArrayEquals(SPREAD2.getUnderlyingCurves(), CURVES2);
    assertEquals(SPREAD3.getName(), NAME3);
    assertArrayEquals(SPREAD3.getUnderlyingCurves(), CURVES3);
  }

  @Test
  public void testGetUnderlyingNames() {
    Set<String> expected = Sets.newHashSet("a", "b");
    Set<String> actual = SPREAD1.getUnderlyingNames();
    assertEquals(expected.size(), actual.size());
    for (final String s : expected) {
      assertTrue(actual.contains(s));
    }
    expected = Sets.newHashSet("a", "b");
    actual = SPREAD2.getUnderlyingNames();
    assertEquals(expected.size(), actual.size());
    for (final String s : expected) {
      assertTrue(actual.contains(s));
    }
    expected = Sets.newHashSet("a", "b");
    actual = SPREAD2.getUnderlyingNames();
    assertEquals(expected.size(), actual.size());
    for (final String s : expected) {
      assertTrue(actual.contains(s));
    }
  }

  @Test
  public void testGetLongName() {
    assertEquals(SPREAD1.getLongName(), NAME1 + "=(a+b)");
    assertEquals(SPREAD2.getLongName(), NAME2 + "=((a+b)-a)");
    assertEquals(SPREAD3.getLongName(), NAME3 + "=(a+a+a)");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullXValue() {
    SPREAD1.getYValue(null);
  }

  @Test
  public void testGetYValue() {
    final double x = 1.5;
    final double eps = 1e-12;
    assertEquals(SPREAD1.getYValue(x), INTERPOLATED1.getYValue(x) + INTERPOLATED2.getYValue(x), eps);
    assertEquals(SPREAD2.getYValue(x), INTERPOLATED2.getYValue(x), eps);
    assertEquals(SPREAD3.getYValue(x), 3 * INTERPOLATED1.getYValue(x), eps);
  }
}
