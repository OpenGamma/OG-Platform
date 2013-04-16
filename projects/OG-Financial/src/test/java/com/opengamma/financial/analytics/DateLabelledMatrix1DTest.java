/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class DateLabelledMatrix1DTest {

  private static final LocalDate[] D1 = new LocalDate[] {LocalDate.of(2011, 1, 1), LocalDate.of(2011, 3, 1), LocalDate.of(2011, 5, 1), LocalDate.of(2011, 7, 1) };
  private static final Object[] D1_LABELS = new Object[] {"2011-01-01", "2011-03-01", "2011-05-01", "2011-07-01" };
  private static final ZonedDateTime[] Z1 = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 1), DateUtils.getUTCDate(2011, 3, 1), DateUtils.getUTCDate(2011, 5, 1), DateUtils.getUTCDate(2011, 7, 1) };
  private static final Object[] Z1_LABELS = new Object[] {"2011-01-01T00:00Z", "2011-03-01T00:00Z", "2011-05-01T00:00Z", "2011-07-01T00:00Z" };
  private static final LocalDate[] D2 = new LocalDate[] {LocalDate.of(2011, 1, 1), LocalDate.of(2011, 2, 1), LocalDate.of(2011, 3, 1), LocalDate.of(2011, 4, 1) };
  private static final ZonedDateTime[] Z2 = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 1), DateUtils.getUTCDate(2011, 2, 1), DateUtils.getUTCDate(2011, 3, 1), DateUtils.getUTCDate(2011, 4, 1) };
  private static final LocalDate[] D3 = new LocalDate[] {LocalDate.of(2011, 1, 1), LocalDate.of(2011, 3, 2), LocalDate.of(2011, 4, 30), LocalDate.of(2011, 7, 1) };
  private static final ZonedDateTime[] Z3 = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 1), DateUtils.getUTCDate(2011, 3, 2), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 7, 1) };
  private static final LocalDate[] D4 = new LocalDate[] {LocalDate.of(2011, 1, 2), LocalDate.of(2011, 2, 2), LocalDate.of(2011, 2, 28), LocalDate.of(2011, 4, 1) };
  private static final ZonedDateTime[] Z4 = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 2), DateUtils.getUTCDate(2011, 2, 2), DateUtils.getUTCDate(2011, 2, 28), DateUtils.getUTCDate(2011, 4, 1) };
  private static final double[] V1 = new double[] {1, 2, 3, 4 };
  private static final double[] V2 = new double[] {5, 6, 7, 8 };
  private static final double[] V3 = new double[] {9, 10, 11, 12 };
  private static final Period TOLERANCE = Period.ofDays(2);
  private static final LocalDateLabelledMatrix1D LD1 = new LocalDateLabelledMatrix1D(D1, V1);
  private static final LocalDateLabelledMatrix1D LD2 = new LocalDateLabelledMatrix1D(D1, V2);
  private static final LocalDateLabelledMatrix1D LD3 = new LocalDateLabelledMatrix1D(D2, V3);
  private static final LocalDateLabelledMatrix1D LD4 = new LocalDateLabelledMatrix1D(D3, V2);
  private static final LocalDateLabelledMatrix1D LD5 = new LocalDateLabelledMatrix1D(D4, V3);
  private static final ZonedDateTimeLabelledMatrix1D ZDT1 = new ZonedDateTimeLabelledMatrix1D(Z1, V1);
  private static final ZonedDateTimeLabelledMatrix1D ZDT2 = new ZonedDateTimeLabelledMatrix1D(Z1, V2);
  private static final ZonedDateTimeLabelledMatrix1D ZDT3 = new ZonedDateTimeLabelledMatrix1D(Z2, V3);
  private static final ZonedDateTimeLabelledMatrix1D ZDT4 = new ZonedDateTimeLabelledMatrix1D(Z3, V2);
  private static final ZonedDateTimeLabelledMatrix1D ZDT5 = new ZonedDateTimeLabelledMatrix1D(Z4, V3);

  @Test
  public void addSingleValueNewDate1() {
    final LocalDate d = LocalDate.of(2011, 1, 2);
    final double v = 10;
    LabelledMatrix1D<LocalDate, Period> newMatrix = LD1.addIgnoringLabel(d, d, v);
    final LocalDate[] newDates = new LocalDate[] {LocalDate.of(2011, 1, 1), d, LocalDate.of(2011, 3, 1), LocalDate.of(2011, 5, 1), LocalDate.of(2011, 7, 1) };
    final Object[] newDatesLabels = new Object[] {"2011-01-01", d, "2011-03-01", "2011-05-01", "2011-07-01" };
    final double[] newValues = new double[] {1, 10, 2, 3, 4 };
    assertArrayEquals(newDates, newMatrix.getKeys());
    assertArrayEquals(newDatesLabels, newMatrix.getLabels());
    assertArrayEquals(newValues, newMatrix.getValues(), 0);
    newMatrix = LD1.add(d, d, v);
    assertArrayEquals(newDates, newMatrix.getKeys());
    assertArrayEquals(newDatesLabels, newMatrix.getLabels());
    assertArrayEquals(newValues, newMatrix.getValues(), 0);
  }

  @Test
  public void addSingleValueNewDate2() {
    final ZonedDateTime d = DateUtils.getUTCDate(2011, 1, 2);
    final double v = 10;
    LabelledMatrix1D<ZonedDateTime, Period> newMatrix = ZDT1.addIgnoringLabel(d, d, v);
    final ZonedDateTime[] newDates = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 1), d, DateUtils.getUTCDate(2011, 3, 1), DateUtils.getUTCDate(2011, 5, 1), DateUtils.getUTCDate(2011, 7, 1) };
    final Object[] newDatesLabels = new Object[] {"2011-01-01T00:00Z", d, "2011-03-01T00:00Z", "2011-05-01T00:00Z", "2011-07-01T00:00Z" };
    final double[] newValues = new double[] {1, 10, 2, 3, 4 };
    assertArrayEquals(newDates, newMatrix.getKeys());
    assertArrayEquals(newDatesLabels, newMatrix.getLabels());
    assertArrayEquals(newValues, newMatrix.getValues(), 0);
    newMatrix = ZDT1.add(d, d, v);
    assertArrayEquals(newDates, newMatrix.getKeys());
    assertArrayEquals(newDatesLabels, newMatrix.getLabels());
    assertArrayEquals(newValues, newMatrix.getValues(), 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addSingleValueExistingDate1() {
    LocalDate d = LocalDate.of(2011, 1, 1);
    final double v = 10;
    LabelledMatrix1D<LocalDate, Period> newMatrix = LD1.addIgnoringLabel(d, d, v);
    final LocalDate[] newDates = new LocalDate[] {LocalDate.of(2011, 1, 1), LocalDate.of(2011, 3, 1), LocalDate.of(2011, 5, 1), LocalDate.of(2011, 7, 1) };
    final Object[] newDatesLabels = new Object[] {"2011-01-01", "2011-03-01", "2011-05-01", "2011-07-01" };
    final double[] newValues = new double[] {11, 2, 3, 4 };
    assertArrayEquals(newDates, newMatrix.getKeys());
    assertArrayEquals(newDatesLabels, newMatrix.getLabels());
    assertArrayEquals(newValues, newMatrix.getValues(), 0);
    newMatrix = LD1.add(d, d, v);
    assertArrayEquals(newDates, newMatrix.getKeys());
    assertArrayEquals(newDatesLabels, newMatrix.getLabels());
    assertArrayEquals(newValues, newMatrix.getValues(), 0);
    d = LocalDate.of(2011, 1, 2);
    newMatrix = LD1.addIgnoringLabel(d, d, v, TOLERANCE);
    assertArrayEquals(newDates, newMatrix.getKeys());
    assertArrayEquals(newDatesLabels, newMatrix.getLabels());
    assertArrayEquals(newValues, newMatrix.getValues(), 0);
    LD1.add(d, d, v, TOLERANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addSingleValueExistingDate2() {
    ZonedDateTime d = DateUtils.getUTCDate(2011, 1, 1);
    final double v = 10;
    LabelledMatrix1D<ZonedDateTime, Period> newMatrix = ZDT1.addIgnoringLabel(d, d, v);
    final ZonedDateTime[] newDates = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 1), DateUtils.getUTCDate(2011, 3, 1), DateUtils.getUTCDate(2011, 5, 1), DateUtils.getUTCDate(2011, 7, 1) };
    final Object[] newDatesLabels = new Object[] {"2011-01-01T00:00Z", "2011-03-01T00:00Z", "2011-05-01T00:00Z", "2011-07-01T00:00Z" };
    final double[] newValues = new double[] {11, 2, 3, 4 };
    assertArrayEquals(newDates, newMatrix.getKeys());
    assertArrayEquals(newDatesLabels, newMatrix.getLabels());
    assertArrayEquals(newValues, newMatrix.getValues(), 0);
    newMatrix = ZDT1.add(d, d, v);
    assertArrayEquals(newDates, newMatrix.getKeys());
    assertArrayEquals(newDatesLabels, newMatrix.getLabels());
    assertArrayEquals(newValues, newMatrix.getValues(), 0);
    newMatrix = ZDT1.add(d, d, v);
    assertArrayEquals(newDates, newMatrix.getKeys());
    assertArrayEquals(newDatesLabels, newMatrix.getLabels());
    assertArrayEquals(newValues, newMatrix.getValues(), 0);
    d = DateUtils.getUTCDate(2011, 1, 2);
    newMatrix = ZDT1.addIgnoringLabel(d, d, v, TOLERANCE);
    assertArrayEquals(newDates, newMatrix.getKeys());
    assertArrayEquals(newDatesLabels, newMatrix.getLabels());
    assertArrayEquals(newValues, newMatrix.getValues(), 0);
    ZDT1.add(d, d, v, TOLERANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddMatrixAllExisting1() {
    LabelledMatrix1D<LocalDate, Period> sum = LD1.add(LD2);
    assertArrayEquals(sum.getKeys(), D1);
    assertArrayEquals(sum.getLabels(), D1_LABELS);
    for (int i = 0; i < D1.length; i++) {
      assertEquals(V1[i] + V2[i], sum.getValues()[i], 0);
    }
    sum = LD1.addIgnoringLabel(LD2);
    assertArrayEquals(sum.getKeys(), D1);
    assertArrayEquals(sum.getLabels(), D1_LABELS);
    for (int i = 0; i < D1.length; i++) {
      assertEquals(V1[i] + V2[i], sum.getValues()[i], 0);
    }
    sum = LD1.addIgnoringLabel(LD4, TOLERANCE);
    assertArrayEquals(sum.getKeys(), D1);
    assertArrayEquals(sum.getLabels(), D1_LABELS);
    for (int i = 0; i < D1.length; i++) {
      assertEquals(V1[i] + V2[i], sum.getValues()[i], 0);
    }
    LD1.add(LD4, TOLERANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddMatrixAllExisting2() {
    LabelledMatrix1D<ZonedDateTime, Period> sum = ZDT1.add(ZDT2);
    assertArrayEquals(sum.getKeys(), Z1);
    assertArrayEquals(sum.getLabels(), Z1_LABELS);
    for (int i = 0; i < Z1.length; i++) {
      assertEquals(V1[i] + V2[i], sum.getValues()[i], 0);
    }
    sum = ZDT1.addIgnoringLabel(ZDT2);
    assertArrayEquals(sum.getKeys(), Z1);
    assertArrayEquals(sum.getLabels(), Z1_LABELS);
    for (int i = 0; i < Z1.length; i++) {
      assertEquals(V1[i] + V2[i], sum.getValues()[i], 0);
    }
    sum = ZDT1.addIgnoringLabel(ZDT4, TOLERANCE);
    assertArrayEquals(sum.getKeys(), Z1);
    assertArrayEquals(sum.getLabels(), Z1_LABELS);
    for (int i = 0; i < Z1.length; i++) {
      assertEquals(V1[i] + V2[i], sum.getValues()[i], 0);
    }
    ZDT1.add(ZDT4, TOLERANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddMatrixSomeExisting1() {
    LocalDate[] expectedDates = new LocalDate[] {LocalDate.of(2011, 1, 1), LocalDate.of(2011, 2, 1), LocalDate.of(2011, 3, 1), LocalDate.of(2011, 4, 1), LocalDate.of(2011, 5, 1),
        LocalDate.of(2011, 7, 1) };
    Object[] expectedDatesLabels = new Object[] {"2011-01-01", "2011-02-01", "2011-03-01", "2011-04-01", "2011-05-01", "2011-07-01" };
    final double[] expectedValues = new double[] {10, 10, 13, 12, 3, 4 };
    LabelledMatrix1D<LocalDate, Period> sum = LD1.add(LD3);
    assertArrayEquals(expectedDates, sum.getKeys());
    assertArrayEquals(expectedDatesLabels, sum.getLabels());
    assertArrayEquals(expectedValues, sum.getValues(), 0);
    sum = LD1.addIgnoringLabel(LD3);
    assertArrayEquals(expectedDates, sum.getKeys());
    assertArrayEquals(expectedDatesLabels, sum.getLabels());
    assertArrayEquals(expectedValues, sum.getValues(), 0);
    expectedDates = new LocalDate[] {LocalDate.of(2011, 1, 1), LocalDate.of(2011, 2, 2), LocalDate.of(2011, 3, 1), LocalDate.of(2011, 4, 1), LocalDate.of(2011, 5, 1), LocalDate.of(2011, 7, 1) };
    expectedDatesLabels = new Object[] {"2011-01-01", "2011-02-02", "2011-03-01", "2011-04-01", "2011-05-01", "2011-07-01" };
    sum = LD1.addIgnoringLabel(LD5, TOLERANCE);
    assertArrayEquals(expectedDates, sum.getKeys());
    assertArrayEquals(expectedDatesLabels, sum.getLabels());
    assertArrayEquals(expectedValues, sum.getValues(), 0);
    LD1.add(LD5, TOLERANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddMatrixSomeExisting2() {
    ZonedDateTime[] expectedDates = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 1), DateUtils.getUTCDate(2011, 2, 1), DateUtils.getUTCDate(2011, 3, 1), DateUtils.getUTCDate(2011, 4, 1),
        DateUtils.getUTCDate(2011, 5, 1), DateUtils.getUTCDate(2011, 7, 1) };
    Object[] expectedDatesLabels = new Object[] {"2011-01-01T00:00Z", "2011-02-01T00:00Z", "2011-03-01T00:00Z", "2011-04-01T00:00Z", "2011-05-01T00:00Z",
        "2011-07-01T00:00Z" };
    final double[] expectedValues = new double[] {10, 10, 13, 12, 3, 4 };
    LabelledMatrix1D<ZonedDateTime, Period> sum = ZDT1.add(ZDT3);
    assertArrayEquals(expectedDates, sum.getKeys());
    assertArrayEquals(expectedDatesLabels, sum.getLabels());
    assertArrayEquals(expectedValues, sum.getValues(), 0);
    sum = ZDT1.addIgnoringLabel(ZDT3);
    assertArrayEquals(expectedDates, sum.getKeys());
    assertArrayEquals(expectedDatesLabels, sum.getLabels());
    assertArrayEquals(expectedValues, sum.getValues(), 0);
    expectedDates = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 1), DateUtils.getUTCDate(2011, 2, 2), DateUtils.getUTCDate(2011, 3, 1), DateUtils.getUTCDate(2011, 4, 1),
        DateUtils.getUTCDate(2011, 5, 1), DateUtils.getUTCDate(2011, 7, 1) };
    expectedDatesLabels = new Object[] {"2011-01-01T00:00Z", "2011-02-02T00:00Z", "2011-03-01T00:00Z", "2011-04-01T00:00Z", "2011-05-01T00:00Z", "2011-07-01T00:00Z" };
    sum = ZDT1.addIgnoringLabel(ZDT5, TOLERANCE);
    assertArrayEquals(expectedDates, sum.getKeys());
    assertArrayEquals(expectedDatesLabels, sum.getLabels());
    assertArrayEquals(expectedValues, sum.getValues(), 0);
    ZDT1.add(ZDT5, TOLERANCE);
  }
}
