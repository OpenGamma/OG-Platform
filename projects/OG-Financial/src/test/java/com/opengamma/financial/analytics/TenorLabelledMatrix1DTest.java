/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class TenorLabelledMatrix1DTest {
  private static final Tenor[] TENORS1 = new Tenor[] {Tenor.ONE_DAY, Tenor.ONE_WEEK, Tenor.ONE_MONTH, Tenor.ONE_YEAR};
  private static final Object[] LABELS1 = new Object[] {"P1D", "P7D", "P1M", "P1Y"};
  private static final double[] VALUES1 = new double[] {1, 2, 3, 4};
  private static final Tenor[] TENORS2 = new Tenor[] {Tenor.TWO_DAYS, Tenor.TWO_WEEKS, Tenor.TWO_MONTHS, Tenor.TWO_YEARS};
  private static final Object[] LABELS2 = new Object[] {"P2D", "P14D", "P2M", "P2Y"};
  private static final double[] VALUES2 = new double[] {10, 20, 30, 40};

  @Test
  public void testAddNewValue() {
    final TenorLabelledMatrix1D matrix = new TenorLabelledMatrix1D(TENORS1, LABELS1, VALUES1);
    final LabelledMatrix1D<Tenor, Period> newMatrix = matrix.add(Tenor.TEN_YEARS, "P10Y", 5);
    assertObjectArrayEquals(new Tenor[] {Tenor.ONE_DAY, Tenor.ONE_WEEK, Tenor.ONE_MONTH, Tenor.ONE_YEAR, Tenor.TEN_YEARS}, newMatrix.getKeys());
    assertObjectArrayEquals(new Object[] {"P1D", "P7D", "P1M", "P1Y", "P10Y"}, newMatrix.getLabels());
    assertArrayEquals(new double[] {1, 2, 3, 4, 5}, newMatrix.getValues(), 1e-15);
  }

  @Test
  public void testAddExistingValue() {
    final TenorLabelledMatrix1D matrix = new TenorLabelledMatrix1D(TENORS1, LABELS1, VALUES1);
    final LabelledMatrix1D<Tenor, Period> newMatrix = matrix.add(Tenor.ONE_YEAR, "P1Y", 5);
    assertObjectArrayEquals(new Tenor[] {Tenor.ONE_DAY, Tenor.ONE_WEEK, Tenor.ONE_MONTH, Tenor.ONE_YEAR}, newMatrix.getKeys());
    assertObjectArrayEquals(new Object[] {"P1D", "P7D", "P1M", "P1Y"}, newMatrix.getLabels());
    assertArrayEquals(new double[] {1, 2, 3, 9}, newMatrix.getValues(), 1e-15);
  }

  @Test
  public void testAddWithinTolerance() {
    final TenorLabelledMatrix1D matrix = new TenorLabelledMatrix1D(TENORS1, LABELS1, VALUES1);
    final LabelledMatrix1D<Tenor, Period> newMatrix = matrix.addIgnoringLabel(Tenor.of(Period.ofDays(8)), "P8D", 5, Period.ofDays(3));
    assertObjectArrayEquals(new Tenor[] {Tenor.ONE_DAY, Tenor.ONE_WEEK, Tenor.ONE_MONTH, Tenor.ONE_YEAR}, newMatrix.getKeys());
    assertObjectArrayEquals(new Object[] {"P1D", "P7D", "P1M", "P1Y"}, newMatrix.getLabels());
    assertArrayEquals(new double[] {1, 7, 3, 4}, newMatrix.getValues(), 1e-15);
  }

  @Test
  public void testAddMatrix() {
    final TenorLabelledMatrix1D matrix1 = new TenorLabelledMatrix1D(TENORS1, LABELS1, VALUES1);
    final TenorLabelledMatrix1D matrix2 = new TenorLabelledMatrix1D(TENORS2, LABELS2, VALUES2);
    LabelledMatrix1D<Tenor, Period> newMatrix = matrix1.add(matrix2);
    assertObjectArrayEquals(new Tenor[] {Tenor.ONE_DAY, Tenor.TWO_DAYS, Tenor.ONE_WEEK, Tenor.TWO_WEEKS, Tenor.ONE_MONTH, Tenor.TWO_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS}, newMatrix.getKeys());
    assertObjectArrayEquals(new Object[] {"P1D", "P2D", "P7D", "P14D", "P1M", "P2M", "P1Y", "P2Y"}, newMatrix.getLabels());
    assertArrayEquals(new double[] {1, 10, 2, 20, 3, 30, 4, 40}, newMatrix.getValues(), 1e-15);
    newMatrix = matrix1.add(matrix1);
    assertObjectArrayEquals(new Tenor[] {Tenor.ONE_DAY, Tenor.ONE_WEEK, Tenor.ONE_MONTH, Tenor.ONE_YEAR}, newMatrix.getKeys());
    assertObjectArrayEquals(new Object[] {"P1D", "P7D", "P1M", "P1Y"}, newMatrix.getLabels());
    assertArrayEquals(new double[] {2, 4, 6, 8}, newMatrix.getValues(), 1e-15);
  }

  private void assertObjectArrayEquals(final Object[] expected, final Object[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }
}
