/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class LabelledMatrix1DTest {
  private static final Integer[] KEYS = new Integer[] {1, 2, 3, 4};
  private static final String[] LABELS = new String[] {"1", "2", "3", "4"};
  private static final double[] VALUES = new double[] {5, 6, 7, 8};

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

  private static class MyLabelledMatrix1D extends LabelledMatrix1D<Integer> {

    public MyLabelledMatrix1D(final Integer[] keys, final double[] values) {
      super(keys, values);
    }

    public MyLabelledMatrix1D(final Integer[] keys, final Object[] labels, final double[] values) {
      super(keys, labels, values);
    }

    @Override
    public LabelledMatrix1D<Integer> addIgnoringLabel(final LabelledMatrix1D<Integer> other) {
      throw new UnsupportedOperationException();
    }

    @Override
    public LabelledMatrix1D<Integer> addIgnoringLabel(final Integer key, final Object label, final double value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public LabelledMatrix1D<Integer> add(final LabelledMatrix1D<Integer> other) {
      throw new UnsupportedOperationException();
    }

    @Override
    public LabelledMatrix1D<Integer> add(final Integer key, final Object label, final double value) {
      throw new UnsupportedOperationException();
    }

  }
}
