/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.riskfactor;

import static com.opengamma.analytics.financial.riskfactor.TaylorExpansionMultiplierCalculator.getMultiplier;
import static com.opengamma.analytics.financial.riskfactor.TaylorExpansionMultiplierCalculator.getTimeSeries;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.MixedOrderUnderlying;
import com.opengamma.analytics.financial.greeks.NthOrderUnderlying;
import com.opengamma.analytics.financial.greeks.Underlying;
import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

public class TaylorExpansionMultiplierCalculatorTest {
  private static final Underlying NEW_TYPE = new Underlying() {

    @Override
    public int getOrder() {
      return 0;
    }

    @Override
    public List<UnderlyingType> getUnderlyings() {
      return null;
    }

  };
  private static final Map<UnderlyingType, DoubleTimeSeries<?>> UNDERLYING_DATA = new HashMap<UnderlyingType, DoubleTimeSeries<?>>();
  private static final long[] T;
  private static final double[] X_DATA;
  private static final double[] Y_DATA;
  private static final DoubleTimeSeries<?> X;
  private static final DoubleTimeSeries<?> Y;
  private static final Underlying ZEROTH_ORDER = new NthOrderUnderlying(0, null);
  private static final Underlying FIRST_ORDER = new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE);
  private static final Underlying SECOND_ORDER = new NthOrderUnderlying(2, UnderlyingType.SPOT_PRICE);
  private static final Underlying THIRD_ORDER = new NthOrderUnderlying(3, UnderlyingType.SPOT_PRICE);
  private static final Underlying FOURTH_ORDER = new NthOrderUnderlying(4, UnderlyingType.SPOT_PRICE);
  private static final Underlying FIFTH_ORDER = new NthOrderUnderlying(5, UnderlyingType.SPOT_PRICE);
  private static final Underlying MIXED_ORDER;
  private static final double EPS = 1e-12;

  static {
    final int n = 100;
    T = new long[n];
    X_DATA = new double[n];
    Y_DATA = new double[n];
    for (int i = 0; i < n; i++) {
      T[i] = i;
      X_DATA[i] = Math.random() - 0.5;
      Y_DATA[i] = Math.random() - 0.5;
    }
    X = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, T, X_DATA);
    Y = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, T, Y_DATA);
    UNDERLYING_DATA.put(UnderlyingType.SPOT_PRICE, X);
    UNDERLYING_DATA.put(UnderlyingType.IMPLIED_VOLATILITY, Y);
    MIXED_ORDER = new MixedOrderUnderlying(Arrays.asList(new NthOrderUnderlying(4, UnderlyingType.SPOT_PRICE), new NthOrderUnderlying(5, UnderlyingType.IMPLIED_VOLATILITY)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOrder1() {
    getMultiplier(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUnderlyingType1() {
    getMultiplier(NEW_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    getTimeSeries(UNDERLYING_DATA, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMap() {
    getTimeSeries(null, FIRST_ORDER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyMap() {
    getTimeSeries(Collections.<UnderlyingType, DoubleTimeSeries<?>> emptyMap(), FIRST_ORDER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUnderlyingType2() {
    getTimeSeries(UNDERLYING_DATA, NEW_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullKey() {
    getTimeSeries(Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(null, X), FIFTH_ORDER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValue() {
    getTimeSeries(Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.SPOT_PRICE, null), FIFTH_ORDER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongTSLength() {
    final DoubleTimeSeries<?> z = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new long[] {1, 2, 3, 4, 5}, new double[] {1, 1, 1, 1, 1});
    final Map<UnderlyingType, DoubleTimeSeries<?>> m = new HashMap<UnderlyingType, DoubleTimeSeries<?>>();
    m.put(UnderlyingType.SPOT_PRICE, X);
    m.put(UnderlyingType.IMPLIED_VOLATILITY, z);
    getTimeSeries(m, MIXED_ORDER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongTSTimes() {
    final int n = 100;
    final long[] t = new long[n];
    final double[] z = new double[n];
    for (int i = 0; i < n; i++) {
      t[i] = i + 1;
      z[i] = Math.random() - 0.5;
    }
    final DoubleTimeSeries<?> ts = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, t, z);
    final Map<UnderlyingType, DoubleTimeSeries<?>> m = new HashMap<UnderlyingType, DoubleTimeSeries<?>>();
    m.put(UnderlyingType.SPOT_PRICE, X);
    m.put(UnderlyingType.IMPLIED_VOLATILITY, ts);
    getTimeSeries(m, MIXED_ORDER);
  }

  @Test
  public void test() {
    assertEquals(getMultiplier(ZEROTH_ORDER), 1, EPS);
    assertEquals(getMultiplier(FIRST_ORDER), 1, EPS);
    assertEquals(getMultiplier(SECOND_ORDER), 1. / 2, EPS);
    assertEquals(getMultiplier(THIRD_ORDER), 1. / 6, EPS);
    assertEquals(getMultiplier(FOURTH_ORDER), 1. / 24, EPS);
    assertEquals(getMultiplier(FIFTH_ORDER), 1. / 120, EPS);
    assertEquals(getMultiplier(MIXED_ORDER), 1. / 2880, EPS);
    final int n = 100;
    final double[] x2 = new double[n];
    final double[] x3 = new double[n];
    final double[] x4 = new double[n];
    final double[] x5 = new double[n];
    final double[] x6 = new double[n];
    for (int i = 0; i < n; i++) {
      final double x = X_DATA[i];
      final double y = Y_DATA[i];
      x2[i] = x * x / 2;
      x3[i] = x * x * x / 6;
      x4[i] = x * x * x * x / 24;
      x5[i] = x * x * x * x * x / 120;
      x6[i] = x4[i] * y * y * y * y * y / 120;
    }
    assertTimeSeriesEquals(getTimeSeries(UNDERLYING_DATA, FIRST_ORDER).toFastLongDoubleTimeSeries(), X.toFastLongDoubleTimeSeries());
    assertTimeSeriesEquals(getTimeSeries(UNDERLYING_DATA, SECOND_ORDER).toFastLongDoubleTimeSeries(), new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, T, x2));
    assertTimeSeriesEquals(getTimeSeries(UNDERLYING_DATA, THIRD_ORDER).toFastLongDoubleTimeSeries(), new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, T, x3));
    assertTimeSeriesEquals(getTimeSeries(UNDERLYING_DATA, FOURTH_ORDER).toFastLongDoubleTimeSeries(), new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, T, x4));
    assertTimeSeriesEquals(getTimeSeries(UNDERLYING_DATA, FIFTH_ORDER).toFastLongDoubleTimeSeries(), new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, T, x5));
    assertTimeSeriesEquals(getTimeSeries(UNDERLYING_DATA, MIXED_ORDER).toFastLongDoubleTimeSeries(), new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, T, x6));
  }

  private void assertTimeSeriesEquals(final FastLongDoubleTimeSeries ts1, final FastLongDoubleTimeSeries ts2) {
    assertEquals(ts1.size(), ts2.size());
    assertArrayEquals(ts1.timesArrayFast(), ts2.timesArrayFast());
    assertArrayEquals(ts1.valuesArrayFast(), ts2.valuesArrayFast(), EPS);
  }
}
