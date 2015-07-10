/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SpreadDoublesCurveTest {
  private static final double[] X = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
  private static final double[] Y1 = new double[] {2, 4, 6, 8, 10, 12, 14, 16, 18};
  private static final double[] Y2 = new double[] {1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1};
  private static final InterpolatedDoublesCurve INTERPOLATED1 = InterpolatedDoublesCurve.from(X, Y1, new LinearInterpolator1D(), "a");
  private static final InterpolatedDoublesCurve INTERPOLATED2 = InterpolatedDoublesCurve.from(X, Y2, new LinearInterpolator1D(), "b");
  private static final FunctionalDoublesCurve FUNCTIONAL1 = FunctionalDoublesCurve.from(new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * 2;
    }

  });
  private static final ConstantDoublesCurve CONSTANT1 = ConstantDoublesCurve.from(0.02);
  private static final CurveSpreadFunction ADD = CurveSpreadFunctionFactory.of("+");
  private static final CurveSpreadFunction SUBTRACT = CurveSpreadFunctionFactory.of("-");
  private static final String NAME1 = "X";
  private static final String NAME2 = "Y";
  private static final String NAME3 = "Z";
  private static final DoublesCurve[] CURVES1 = new DoublesCurve[] {INTERPOLATED1, INTERPOLATED2};
  private static final SpreadDoublesCurve SPREAD1 = SpreadDoublesCurve.from(ADD, NAME1, CURVES1);
  private static final DoublesCurve[] CURVES2 = new DoublesCurve[] {SPREAD1, INTERPOLATED1};
  private static final SpreadDoublesCurve SPREAD2 = SpreadDoublesCurve.from(SUBTRACT, NAME2, CURVES2);
  private static final DoublesCurve[] CURVES3 = new DoublesCurve[] {INTERPOLATED1, INTERPOLATED1, INTERPOLATED1};
  private static final SpreadDoublesCurve SPREAD3 = SpreadDoublesCurve.from(ADD, NAME3, CURVES3);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves1() {
    new SpreadDoublesCurve(ADD, (DoublesCurve) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTooFewCurves1() {
    new SpreadDoublesCurve(ADD, new DoublesCurve[] {INTERPOLATED1});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSpreadFunction1() {
    new SpreadDoublesCurve(null, CURVES1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves2() {
    new SpreadDoublesCurve(ADD, NAME1, (DoublesCurve) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTooFewCurves2() {
    new SpreadDoublesCurve(ADD, NAME1, new DoublesCurve[] {INTERPOLATED1});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSpreadFunction2() {
    new SpreadDoublesCurve(null, NAME1, CURVES1);
  }

  @Test
  public void testHashCodeAndEquals() {
    SpreadDoublesCurve other = SpreadDoublesCurve.from(ADD, NAME1, CURVES1);
    assertEquals(other, SPREAD1);
    assertEquals(other.hashCode(), SPREAD1.hashCode());
    other = SpreadDoublesCurve.from(ADD, NAME1, CURVES2);
    assertFalse(other.equals(SPREAD1));
    other = SpreadDoublesCurve.from(SUBTRACT, NAME1, CURVES1);
    assertFalse(other.equals(SPREAD1));
    other = SpreadDoublesCurve.from(ADD, NAME2, CURVES1);
    assertFalse(other.equals(SPREAD1));
    other = SpreadDoublesCurve.from(ADD, CURVES1);
    assertFalse(other.equals(SPREAD1));
    other = SpreadDoublesCurve.from(ADD, CURVES1);
    assertFalse(other.equals(SPREAD1));
    other = new SpreadDoublesCurve(ADD, CURVES1);
    assertFalse(other.equals(SPREAD1));
    other = new SpreadDoublesCurve(ADD, CURVES1);
    assertFalse(other.equals(SPREAD1));
  }

  @Test
  public void testStaticConstructors() {
    assertEquals(new SpreadDoublesCurve(ADD, NAME1, CURVES1), SPREAD1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetXValues() {
    SPREAD1.getXData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetYValues() {
    SPREAD1.getYData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetSize() {
    SpreadDoublesCurve.from(ADD, new DoublesCurve[] {FUNCTIONAL1, CONSTANT1}).size();
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

  /**
   * Tests that size() can be called for spread curves consisting combinations of interpolated and constant doubles curves.
   */
  @Test
  public void testSize() {
    assertEquals(INTERPOLATED1.size() + INTERPOLATED2.size(), SPREAD1.size());
    assertEquals(INTERPOLATED1.size() + 2 * INTERPOLATED2.size(), SpreadDoublesCurve.from(ADD, new DoublesCurve[] {INTERPOLATED1, SPREAD1}).size());
    assertEquals(INTERPOLATED1.size() + INTERPOLATED2.size(), SpreadDoublesCurve.from(ADD, new DoublesCurve[] {CONSTANT1, SPREAD1}).size());
    assertEquals(INTERPOLATED1.size() + INTERPOLATED2.size(), SpreadDoublesCurve.from(ADD, new DoublesCurve[] {CONSTANT1, SPREAD1}).size());
  }

  @Test
  public void testGetLongName() {
    assertEquals(SPREAD1.getLongName(), NAME1 + "=(a+b)");
    assertEquals(SPREAD2.getLongName(), NAME2 + "=((a+b)-a)");
    assertEquals(SPREAD3.getLongName(), NAME3 + "=(a+a+a)");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
