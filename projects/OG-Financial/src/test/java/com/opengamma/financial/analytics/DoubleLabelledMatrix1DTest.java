/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class DoubleLabelledMatrix1DTest {

  private static final Double[] TIMES1 = new Double[] {1. / 365, 2. / 365, 7. / 365, 1. / 12, 2. / 12, 0.5, 1., 1.5, 2., 3., 4., 5., 6., 7., 8., 9., 10.};
  private static final Object[] LABELS1 = new Object[] {"1D", "2D", "1W", "1M", "2M", "6M", "1Y", "18M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y"};
  private static final double[] VALUES1 = new double[] {0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0};
  private static final Double[] TIMES2 = new Double[] {1. / 365, 2. / 365, 7. / 365, 1. / 12, 2. / 12, 0.5, 1., 1.5, 2., 3., 4., 5., 6., 7., 8., 9., 10.};
  private static final Object[] LABELS2 = new Object[] {"1D", "2D", "1W", "1M", "2M", "6M", "1Y", "18M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y"};
  private static final double[] VALUES2 = new double[] {0, 4, 0, 5, 0, 0, 6, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0};
  private static final Double[] TIMES3 = new Double[] {3. / 365, 4. / 365, 6. / 365};
  private static final Object[] LABELS3 = new Object[] {"3D", "4D", "5D"};
  private static final double[] VALUES3 = new double[] {9, 10, 11};
  private static final Double[] TIMES4 = new Double[] {1. / 365, 2. / 365, 7. / 365, 1. / 12, 2. / 12, 3. / 12};
  private static final Object[] LABELS4 = new Object[] {"1D", "2D", "1W", "1M", "2M", "3M"};
  private static final double[] VALUES4 = new double[] {0, 12, 0, 13, 14, 0};
  private static final double HIGH_TOLERANCE = 0.25 / 365;
  private static final Double[] TIMES5 = new Double[TIMES1.length];
  private static final Double[] TIMES6 = new Double[TIMES4.length];
  static {
    for (int i = 0; i < TIMES1.length; i++) {
      TIMES5[i] = TIMES1[i] + HIGH_TOLERANCE * Math.random();
    }
    for (int i = 0; i < TIMES4.length; i++) {
      TIMES6[i] = TIMES4[i] - HIGH_TOLERANCE * Math.random();
    }
  }
  private static final double[] VALUES5 = new double[] {0, 15, 0, 16, 0, 17, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0};
  private static final double[] VALUES6 = new double[] {19, 20, 0, 0, 0, 21};
  private static final DoubleLabelledMatrix1D M1 = new DoubleLabelledMatrix1D(TIMES1, LABELS1, VALUES1);
  private static final DoubleLabelledMatrix1D M2 = new DoubleLabelledMatrix1D(TIMES2, LABELS2, VALUES2);
  private static final DoubleLabelledMatrix1D M3 = new DoubleLabelledMatrix1D(TIMES3, LABELS3, VALUES3);
  private static final DoubleLabelledMatrix1D M4 = new DoubleLabelledMatrix1D(TIMES4, LABELS4, VALUES4);
  private static final DoubleLabelledMatrix1D M5 = new DoubleLabelledMatrix1D(TIMES5, LABELS1, VALUES5);
  private static final DoubleLabelledMatrix1D M6 = new DoubleLabelledMatrix1D(TIMES6, LABELS4, VALUES6);
  private static final double EPS = 1e-15;

  @Test
  public void testAddSingleValueAlreadyPresent() {
    final double x = 0.4;
    LabelledMatrix1D<Double, Double> m = M1.add(1., "1Y", x);
    Double[] newTimes = m.getKeys();
    Object[] newLabels = m.getLabels();
    double[] newValues = m.getValues();
    int n = TIMES1.length;
    assertEquals(newTimes.length, n);
    assertArrayEquals(newTimes, TIMES1);
    assertArrayEquals(newLabels, LABELS1);
    for (int i = 0; i < n; i++) {
      if (i == 6) {
        assertEquals(newValues[i], VALUES1[i] + x, EPS);
      } else {
        assertEquals(newValues[i], VALUES1[i], EPS);
      }
    }
    m = M1.add(1. + HIGH_TOLERANCE / 2, "1Y", x, HIGH_TOLERANCE);
    newTimes = m.getKeys();
    newLabels = m.getLabels();
    newValues = m.getValues();
    n = TIMES1.length;
    assertEquals(newTimes.length, n);
    assertArrayEquals(newTimes, TIMES1);
    assertArrayEquals(newLabels, LABELS1);
    for (int i = 0; i < n; i++) {
      if (i == 6) {
        assertEquals(newValues[i], VALUES1[i] + x, EPS);
      } else {
        assertEquals(newValues[i], VALUES1[i], EPS);
      }
    }
  }

  @Test
  public void testAddSingleValueNotPresent() {
    final double x = 0.4;
    LabelledMatrix1D<Double, Double> m = M1.add(2.5, "30M", x);
    Double[] newTimes = m.getKeys();
    Object[] newLabels = m.getLabels();
    double[] newValues = m.getValues();
    int n = TIMES1.length;
    assertEquals(newTimes.length, n + 1);
    for (int i = 0; i < n + 1; i++) {
      if (i < 9) {
        assertEquals(newTimes[i], TIMES1[i], EPS);
        assertEquals(newLabels[i], LABELS1[i]);
        assertEquals(newValues[i], VALUES1[i], EPS);
      } else if (i == 9) {
        assertEquals(newTimes[i], 2.5, EPS);
        assertEquals(newLabels[i], "30M");
        assertEquals(newValues[i], x, EPS);
      } else {
        assertEquals(newTimes[i], TIMES1[i - 1], EPS);
        assertEquals(newLabels[i], LABELS1[i - 1]);
        assertEquals(newValues[i], VALUES1[i - 1], EPS);
      }
    }
    m = M1.add(1. + 1.5 * HIGH_TOLERANCE, "1+Y", x, HIGH_TOLERANCE);
    newTimes = m.getKeys();
    newLabels = m.getLabels();
    newValues = m.getValues();
    n = TIMES1.length;
    assertEquals(newTimes.length, n + 1);
    for (int i = 0; i < n; i++) {
      if (i < 7) {
        assertEquals(newTimes[i], TIMES1[i], EPS);
        assertEquals(newLabels[i], LABELS1[i]);
        assertEquals(newValues[i], VALUES1[i], EPS);
      } else if (i == 7) {
        assertEquals(newTimes[i], 1. + 1.5 * HIGH_TOLERANCE, EPS);
        assertEquals(newLabels[i], "1+Y");
        assertEquals(newValues[i], x, EPS);
      } else {
        assertEquals(newTimes[i], TIMES1[i - 1], EPS);
        assertEquals(newLabels[i], LABELS1[i - 1]);
        assertEquals(newValues[i], VALUES1[i - 1], EPS);
      }
    }
  }

  @Test
  public void testAddSingleValueAlreadyPresentWithError() {
    final double x = 0.4;
    final LabelledMatrix1D<Double, Double> m1 = M1.add(1. + HIGH_TOLERANCE / 2, "1Y", x, HIGH_TOLERANCE);
    final Double[] newTimes1 = m1.getKeys();
    final Object[] newLabels1 = m1.getLabels();
    final double[] newValues1 = m1.getValues();
    final int n = TIMES1.length;
    assertEquals(newTimes1.length, n);
    assertArrayEquals(newTimes1, TIMES1);
    assertArrayEquals(newLabels1, LABELS1);
    for (int i = 0; i < n; i++) {
      if (i == 6) {
        assertEquals(newValues1[i], VALUES1[i] + x, EPS);
      } else {
        assertEquals(newValues1[i], VALUES1[i], EPS);
      }
    }
  }

  @Test
  public void testAddSameTimes() {
    LabelledMatrix1D<Double, Double> sum = M1.add(M2);
    Double[] times = sum.getKeys();
    Object[] labels = sum.getLabels();
    double[] values = sum.getValues();
    int n = times.length;
    assertArrayEquals(times, TIMES1);
    assertArrayEquals(labels, LABELS1);
    for (int i = 0; i < n; i++) {
      assertEquals(values[i], VALUES1[i] + VALUES2[i], EPS);
    }
    sum = M1.add(M5, HIGH_TOLERANCE);
    times = sum.getKeys();
    labels = sum.getLabels();
    values = sum.getValues();
    n = times.length;
    assertArrayEquals(times, TIMES1);
    assertArrayEquals(labels, LABELS1);
    for (int i = 0; i < n; i++) {
      assertEquals(values[i], VALUES1[i] + VALUES5[i], EPS);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddSameTimesNoTolerance() {
    final LabelledMatrix1D<Double, Double> sum = M1.addIgnoringLabel(M5);
    final Double[] times = sum.getKeys();
    Object[] labels = sum.getLabels();
    final int n = TIMES1.length;
    final int m = TIMES5.length;
    assertEquals(times.length, n + m);
    labels = Arrays.copyOf(LABELS1, n);
    labels[0] = "O/N";
    M1.add(new DoubleLabelledMatrix1D(TIMES2, labels, VALUES2));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNewTimes() {
    LabelledMatrix1D<Double, Double> sum = M1.add(M3);
    Double[] times = sum.getKeys();
    Object[] labels = sum.getLabels();
    double[] values = sum.getValues();
    final int n = TIMES1.length;
    final int m = TIMES3.length;
    assertEquals(times.length, n + m);
    for (int i = 0; i < n + m; i++) {
      if (i < 2) {
        assertEquals(times[i], TIMES1[i], EPS);
        assertEquals(labels[i], LABELS1[i]);
        assertEquals(values[i], VALUES1[i], EPS);
      } else if (i < 5) {
        assertEquals(times[i], TIMES3[i - 2], EPS);
        assertEquals(labels[i], LABELS3[i - 2]);
        assertEquals(values[i], VALUES3[i - 2], EPS);
      } else {
        assertEquals(times[i], TIMES1[i - 3], EPS);
        assertEquals(labels[i], LABELS1[i - 3]);
        assertEquals(values[i], VALUES1[i - 3], EPS);
      }
    }
    sum = M1.addIgnoringLabel(M3, 1.5 / 365);
    times = sum.getKeys();
    labels = sum.getLabels();
    values = sum.getValues();
    assertEquals(times.length, n + 1);
    assertEquals(times[0], TIMES1[0], EPS);
    assertEquals(labels[0], LABELS1[0]);
    assertEquals(values[0], VALUES1[0], EPS);
    assertEquals(times[1], TIMES1[1], EPS);
    assertEquals(labels[1], LABELS1[1]);
    assertEquals(values[1], VALUES1[1] + VALUES3[0], EPS);
    assertEquals(times[2], TIMES3[1], EPS);
    assertEquals(labels[2], LABELS3[1]);
    assertEquals(values[2], VALUES3[1], EPS);
    assertEquals(times[3], TIMES1[2], EPS);
    assertEquals(labels[3], LABELS1[2]);
    assertEquals(values[3], VALUES1[2] + VALUES3[2], EPS);
    for (int i = 4; i < n + 1; i++) {
      assertEquals(times[i], TIMES1[i - 1], EPS);
      assertEquals(labels[i], LABELS1[i - 1]);
      assertEquals(values[i], VALUES1[i - 1], EPS);
    }
    sum = M1.add(M3, 1.5 / 365);
  }

  @Test
  public void testSomeIdenticalTimes() {
    LabelledMatrix1D<Double, Double> sum = M1.add(M4);
    Double[] times = sum.getKeys();
    Object[] labels = sum.getLabels();
    double[] values = sum.getValues();
    final int n = TIMES1.length;
    assertEquals(times.length, n + 1);
    for (int i = 0; i < n + 1; i++) {
      if (i < 5) {
        assertEquals(times[i], TIMES1[i], EPS);
        assertEquals(labels[i], LABELS1[i]);
        assertEquals(values[i], VALUES1[i] + VALUES4[i], EPS);
      } else if (i == 5) {
        assertEquals(times[i], TIMES4[i], EPS);
        assertEquals(labels[i], LABELS4[i]);
        assertEquals(values[i], VALUES4[i], EPS);
      } else {
        assertEquals(times[i], TIMES1[i - 1], EPS);
        assertEquals(labels[i], LABELS1[i - 1]);
        assertEquals(values[i], VALUES1[i - 1], EPS);
      }
    }
    sum = M1.addIgnoringLabel(M6, HIGH_TOLERANCE);
    times = sum.getKeys();
    labels = sum.getLabels();
    values = sum.getValues();
    assertEquals(times.length, n + 1);
    for (int i = 0; i < n + 1; i++) {
      if (i < 5) {
        assertEquals(times[i], TIMES1[i], EPS);
        assertEquals(labels[i], LABELS1[i]);
        assertEquals(values[i], VALUES1[i] + VALUES6[i], EPS);
      } else if (i == 5) {
        assertEquals(times[i], TIMES6[i], EPS);
        assertEquals(labels[i], LABELS4[i]);
        assertEquals(values[i], VALUES6[i], EPS);
      } else {
        assertEquals(times[i], TIMES1[i - 1], EPS);
        assertEquals(labels[i], LABELS1[i - 1]);
        assertEquals(values[i], VALUES1[i - 1], EPS);
      }
    }
  }
}
