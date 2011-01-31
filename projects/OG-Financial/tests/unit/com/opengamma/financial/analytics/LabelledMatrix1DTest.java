/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.junit.Test;

/**
 * 
 */
public class LabelledMatrix1DTest {
  private static final Integer[] KEYS = new Integer[] {1, 2, 3, 4};
  private static final String[] LABELS = new String[] {"1", "2", "3", "4"};
  private static final double[] VALUES = new double[] {5, 6, 7, 8};
  private static final Double[] TIMES1 = new Double[] {1. / 365, 2. / 365, 7. / 365, 1. / 12, 2. / 12, 0.5, 1., 1.5, 2., 3., 4., 5., 6., 7., 8., 9., 10.};
  private static final Object[] LABELS1 = new Object[] {"1D", "2D", "1W", "1M", "2M", "6M", "1Y", "18M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y"};
  private static final double[] VALUES1 = new double[] {0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0};
  private static final DoubleLabelledMatrix1D M1 = new DoubleLabelledMatrix1D(TIMES1, LABELS1, VALUES1);
  private static final double HIGH_TOLERANCE = 0.25 / 365;

  @Test(expected = IllegalArgumentException.class)
  public void testNullKeys1() {
    new MyLabelledMatrix1D(null, VALUES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullKeys2() {
    new MyLabelledMatrix1D(null, LABELS, VALUES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLabels() {
    new MyLabelledMatrix1D(KEYS, null, VALUES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValues1() {
    new MyLabelledMatrix1D(KEYS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValues2() {
    new MyLabelledMatrix1D(KEYS, LABELS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLength1() {
    new MyLabelledMatrix1D(KEYS, new double[] {1, 2, 3});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLength2() {
    new MyLabelledMatrix1D(KEYS, LABELS, new double[] {1, 2, 3});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLength3() {
    new MyLabelledMatrix1D(KEYS, new String[] {"1", "2", "3"}, VALUES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddWithNullKey() {
    M1.addIgnoringLabel(null, "N", 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddWithNullLabel() {
    M1.addIgnoringLabel(2., null, 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddSingleValueWrongLabel1() {
    M1.add(1., "12M", 0.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddSingleValueWrongLabel2() {
    M1.add(1. + HIGH_TOLERANCE / 2, "12M", 0.1, HIGH_TOLERANCE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddNullMatrix1() {
    M1.addIgnoringLabel(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddNullMatrix2() {
    M1.add(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddWithWrongLabel() {
    final int n = TIMES1.length;
    final Double[] times = Arrays.copyOf(TIMES1, n);
    final Object[] labels = Arrays.copyOf(LABELS1, n);
    labels[3] = "7D";
    final double[] values = Arrays.copyOf(VALUES1, n);
    final LabelledMatrix1D<Double, Double> m = new DoubleLabelledMatrix1D(times, labels, values);
    M1.add(m);
  }

  @Test
  public void test() {
    MyLabelledMatrix1D m = new MyLabelledMatrix1D(KEYS, VALUES);
    MyLabelledMatrix1D other = new MyLabelledMatrix1D(KEYS, VALUES);
    assertEquals(m, other);
    assertEquals(m.hashCode(), other.hashCode());
    assertArrayEquals(m.getKeys(), KEYS);
    assertArrayEquals(m.getLabels(), KEYS);
    assertArrayEquals(m.getValues(), VALUES, 0);
    other = new MyLabelledMatrix1D(KEYS, KEYS, VALUES);
    assertEquals(m, other);
    assertEquals(m.hashCode(), other.hashCode());
    m = new MyLabelledMatrix1D(KEYS, LABELS, VALUES);
    other = new MyLabelledMatrix1D(KEYS, LABELS, VALUES);
    assertEquals(m, other);
    assertEquals(m.hashCode(), other.hashCode());
    assertArrayEquals(m.getKeys(), KEYS);
    assertArrayEquals(m.getLabels(), LABELS);
    assertArrayEquals(m.getValues(), VALUES, 0);
    other = new MyLabelledMatrix1D(new Integer[] {4, 5, 6, 7}, LABELS, VALUES);
    assertFalse(m.equals(other));
    other = new MyLabelledMatrix1D(KEYS, KEYS, VALUES);
    assertFalse(m.equals(other));
    other = new MyLabelledMatrix1D(KEYS, LABELS, new double[] {9, 10, 11, 12});
    assertFalse(m.equals(other));
  }

  @Test
  public void testSort() {
    final Double[] keys = new Double[] {1., 3., 8., 9., 2., 0., 7., 5., 6., 4.};
    final Object[] labels = new Object[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    final double[] values = new double[] {11, 13, 18, 19, 12, 10, 17, 15, 16, 14};
    DoubleLabelledMatrix1D m = new DoubleLabelledMatrix1D(keys, values);
    final Double[] expectedKeys = new Double[] {0., 1., 2., 3., 4., 5., 6., 7., 8., 9.};
    final Object[] expectedLabels = new Object[] {"F", "A", "E", "B", "J", "H", "I", "G", "C", "D"};
    final double[] expectedValues = new double[] {10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
    assertArrayEquals(m.getKeys(), expectedKeys);
    assertArrayEquals(m.getLabels(), expectedKeys);
    assertArrayEquals(m.getValues(), expectedValues, 0);
    m = new DoubleLabelledMatrix1D(keys, labels, values);
    assertArrayEquals(m.getKeys(), expectedKeys);
    assertArrayEquals(m.getLabels(), expectedLabels);
    assertArrayEquals(m.getValues(), expectedValues, 0);
  }

  private static class MyLabelledMatrix1D extends LabelledMatrix1D<Integer, Integer> {
    private static final int TOLERANCE = 0;

    public MyLabelledMatrix1D(final Integer[] keys, final double[] values) {
      super(keys, values, TOLERANCE);
    }

    public MyLabelledMatrix1D(final Integer[] keys, final Object[] labels, final double[] values) {
      super(keys, labels, values, TOLERANCE);
    }

    @Override
    protected int compare(final Integer o1, final Integer o2, final Integer tolerance) {
      return o1.compareTo(o2);
    }

    @Override
    protected LabelledMatrix1D<Integer, Integer> getMatrix(final Integer[] keys, final Object[] labels, final double[] values) {
      return new MyLabelledMatrix1D(keys, labels, values);
    }

    @Override
    protected LabelledMatrix1D<Integer, Integer> getMatrix(final Integer[] keys, final double[] values) {
      return new MyLabelledMatrix1D(keys, values);
    }
  }
}
