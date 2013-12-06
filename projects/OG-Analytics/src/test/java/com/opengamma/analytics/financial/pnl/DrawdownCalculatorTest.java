/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.pnl;

import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateToIntConverter;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DrawdownCalculatorTest {
  private static final int N = 100;
  private static final int[] T = new int[N];
  private static final double[] FLAT = new double[N];
  private static final double[] FLAT_DRAWDOWN = new double[N];
  private static final double[] HIGH_FIRST = new double[N];
  private static final double[] HIGH_FIRST_DRAWDOWN = new double[N];
  private static final double[] X = new double[N];
  private static final double[] X_DRAWDOWN = new double[N];
  private static final Function1D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> CALCULATOR = new DrawdownCalculator();

  static {
    for (int i = 0; i < N; i++) {
      T[i] = LocalDateToIntConverter.convertToInt(LocalDate.ofEpochDay(i));
      FLAT[i] = 100;
      FLAT_DRAWDOWN[i] = 0;
      HIGH_FIRST[i] = 100;
      HIGH_FIRST_DRAWDOWN[i] = 2. / 3;
      if (i % 10 == 0) {
        X[i] = (i + 10) * 10;
        X_DRAWDOWN[i] = 0;
      } else {
        X[i] = 100;
        X_DRAWDOWN[i] = (i / 10) * 100 / X[10 * (i / 10)];
      }
    }
    HIGH_FIRST[0] = 300;
    HIGH_FIRST_DRAWDOWN[0] = 0;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS() {
    CALCULATOR.evaluate((DateDoubleTimeSeries<?>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyTS() {
    CALCULATOR.evaluate(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test
  public void test() {
    assertTimeSeriesEquals(CALCULATOR.evaluate(ImmutableLocalDateDoubleTimeSeries.of(T, FLAT)), ImmutableLocalDateDoubleTimeSeries.of(T, FLAT_DRAWDOWN));
    assertTimeSeriesEquals(CALCULATOR.evaluate(ImmutableLocalDateDoubleTimeSeries.of(T, HIGH_FIRST)), ImmutableLocalDateDoubleTimeSeries.of(T, HIGH_FIRST_DRAWDOWN));
    assertTimeSeriesEquals(CALCULATOR.evaluate(ImmutableLocalDateDoubleTimeSeries.of(T, X)), ImmutableLocalDateDoubleTimeSeries.of(T, X_DRAWDOWN));
  }

  private void assertTimeSeriesEquals(final DateDoubleTimeSeries<?> ts1, final DateDoubleTimeSeries<?> ts2) {
    assertArrayEquals(ts1.timesArrayFast(), ts2.timesArrayFast());
    assertArrayEquals(ts1.valuesArrayFast(), ts2.valuesArrayFast(), 1e-15);
  }

}
