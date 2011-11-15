/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

/**
 * 
 */
public class LabelledMatrix1DTest {
  private static final Integer[] KEYS = new Integer[] {1, 2, 3, 4};
  private static final String[] LABELS1 = new String[] {"1", "2", "3", "4" };
  private static final String[] LABELS2 = new String[] {"A", "B", "C", "D" };
  private static final double[] VALUES = new double[] {5, 6, 7, 8};
  private static final Double[] TIMES3 = new Double[] {1. / 365, 2. / 365, 7. / 365, 1. / 12, 2. / 12, 0.5, 1., 1.5, 2., 3., 4., 5., 6., 7., 8., 9., 10. };
  private static final Object[] LABELS3 = new Object[] {"1D", "2D", "1W", "1M", "2M", "6M", "1Y", "18M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y" };
  private static final double[] VALUES3 = new double[] {0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0 };
  private static final DoubleLabelledMatrix1D M1 = new DoubleLabelledMatrix1D(TIMES3, LABELS3, VALUES3);
  private static final double HIGH_TOLERANCE = 0.25 / 365;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullKeys1() {
    new MyLabelledMatrix1D(null, VALUES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullKeys2() {
    new MyLabelledMatrix1D(null, LABELS1, VALUES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLabels() {
    new MyLabelledMatrix1D(KEYS, null, VALUES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValues1() {
    new MyLabelledMatrix1D(KEYS, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValues2() {
    new MyLabelledMatrix1D(KEYS, LABELS1, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength1() {
    new MyLabelledMatrix1D(KEYS, new double[] {1, 2, 3});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength2() {
    new MyLabelledMatrix1D(KEYS, LABELS1, new double[] {1, 2, 3 });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength3() {
    new MyLabelledMatrix1D(KEYS, new String[] {"1", "2", "3"}, VALUES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddWithNullKey() {
    M1.addIgnoringLabel(null, "N", 2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddWithNullLabel() {
    M1.addIgnoringLabel(2., null, 2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddSingleValueWrongLabel1() {
    M1.add(1., "12M", 0.1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddSingleValueWrongLabel2() {
    M1.add(1. + HIGH_TOLERANCE / 2, "12M", 0.1, HIGH_TOLERANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNullMatrix1() {
    M1.addIgnoringLabel(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNullMatrix2() {
    M1.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddWithWrongLabel() {
    final int n = TIMES3.length;
    final Double[] times = Arrays.copyOf(TIMES3, n);
    final Object[] labels = Arrays.copyOf(LABELS3, n);
    labels[3] = "7D";
    final double[] values = Arrays.copyOf(VALUES3, n);
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
    assertArrayEquals(m.getLabels(), LABELS1);
    assertArrayEquals(m.getValues(), VALUES, 0);
    other = new MyLabelledMatrix1D(KEYS, LABELS1, VALUES);
    assertEquals(m, other);
    assertEquals(m.hashCode(), other.hashCode());
    m = new MyLabelledMatrix1D(KEYS, LABELS2, VALUES);
    other = new MyLabelledMatrix1D(KEYS, LABELS2, VALUES);
    assertEquals(m, other);
    assertEquals(m.hashCode(), other.hashCode());
    assertArrayEquals(m.getKeys(), KEYS);
    assertArrayEquals(m.getLabels(), LABELS2);
    assertArrayEquals(m.getValues(), VALUES, 0);
    other = new MyLabelledMatrix1D(new Integer[] {4, 5, 6, 7 }, LABELS1, VALUES);
    assertFalse(m.equals(other));
    other = new MyLabelledMatrix1D(KEYS, KEYS, VALUES);
    assertFalse(m.equals(other));
    other = new MyLabelledMatrix1D(KEYS, LABELS1, new double[] {9, 10, 11, 12 });
    assertFalse(m.equals(other));
  }
  
  @Test
  public void testTitles() {
    MyLabelledMatrix1D withTitles = new MyLabelledMatrix1D(KEYS, LABELS1, "labels", VALUES, "values");
    assertEquals("labels", withTitles.getLabelsTitle());
    assertEquals("values", withTitles.getValuesTitle());
    MyLabelledMatrix1D withoutTitles = new MyLabelledMatrix1D(KEYS, LABELS1, VALUES);
    assertFalse(withTitles.equals(withoutTitles));
  }
  
  @Test
  public void testTitlesPreserved() {
    MyLabelledMatrix1D m1 = new MyLabelledMatrix1D(KEYS, LABELS1, "labels", VALUES, "values");
    LabelledMatrix1D<Integer, Integer> m2 = m1.add(KEYS[0], LABELS1[0], 0);
    assertEquals(m1.getLabelsTitle(), m2.getLabelsTitle());
    assertEquals(m1.getValuesTitle(), m2.getValuesTitle());
    assertEquals(m1, m2);
  }

  @Test
  public void testSort() {
    final Double[] keys = new Double[] {1., 3., 8., 9., 2., 0., 7., 5., 6., 4.};
    final Object[] labels = new Object[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    final double[] values = new double[] {11, 13, 18, 19, 12, 10, 17, 15, 16, 14};
    DoubleLabelledMatrix1D m = new DoubleLabelledMatrix1D(keys, values);
    final Double[] expectedKeys = new Double[] {0., 1., 2., 3., 4., 5., 6., 7., 8., 9.};
    final Object[] expectedLabels1 = new Object[] {"0.0", "1.0", "2.0", "3.0", "4.0", "5.0", "6.0", "7.0", "8.0", "9.0" };
    final Object[] expectedLabels2 = new Object[] {"F", "A", "E", "B", "J", "H", "I", "G", "C", "D" };
    final double[] expectedValues = new double[] {10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
    assertArrayEquals(m.getKeys(), expectedKeys);
    assertArrayEquals(m.getLabels(), expectedLabels1);
    assertArrayEquals(m.getValues(), expectedValues, 0);
    m = new DoubleLabelledMatrix1D(keys, labels, values);
    assertArrayEquals(m.getKeys(), expectedKeys);
    assertArrayEquals(m.getLabels(), expectedLabels2);
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

    public MyLabelledMatrix1D(final Integer[] keys, final Object[] labels, final String labelsTitle, final double[] values, final String valuesTitle) {
      super(keys, labels, labelsTitle, values, valuesTitle, TOLERANCE);
    }
    
    @Override
    public int compare(final Integer o1, final Integer o2, final Integer tolerance) {
      return o1.compareTo(o2);
    }
    
    @Override
    public LabelledMatrix1D<Integer, Integer> getMatrix(Integer[] keys, Object[] labels, String labelsTitle, double[] values, String valuesTitle) {
      return new MyLabelledMatrix1D(keys, labels, labelsTitle, values, valuesTitle);
    }

    @Override
    public LabelledMatrix1D<Integer, Integer> getMatrix(final Integer[] keys, final Object[] labels, final double[] values) {
      return new MyLabelledMatrix1D(keys, labels, values);
    }

    @Override
    public LabelledMatrix1D<Integer, Integer> getMatrix(final Integer[] keys, final double[] values) {
      return new MyLabelledMatrix1D(keys, values);
    }
  }
}
