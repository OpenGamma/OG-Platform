/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.pnl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.sensitivity.Sensitivity;
import com.opengamma.analytics.financial.sensitivity.ValueGreek;
import com.opengamma.analytics.financial.sensitivity.ValueGreekSensitivity;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SensitivityAndReturnDataBundleTest {
  private static final Sensitivity<ValueGreek> S1 = new ValueGreekSensitivity(new ValueGreek(Greek.DELTA), "ValueDelta");
  private static final Sensitivity<ValueGreek> S2 = new ValueGreekSensitivity(new ValueGreek(Greek.GAMMA), "ValueGamma");
  private static final double VALUE_DELTA = 1000;
  private static final double VALUE_GAMMA = -1234;
  private static final DoubleTimeSeries<?> TS1;
  private static final DoubleTimeSeries<?> TS2;
  private static final Map<UnderlyingType, DoubleTimeSeries<?>> M1;
  private static final Map<UnderlyingType, DoubleTimeSeries<?>> M2;
  private static final SensitivityAndReturnDataBundle DATA;

  static {
    final int n = 100;
    final long[] times = new long[n];
    final double[] x1 = new double[n];
    final double[] x2 = new double[n];
    for (int i = 0; i < n; i++) {
      times[i] = i;
      x1[i] = Math.random() - 0.5;
      x2[i] = Math.random() - 0.5;
    }
    TS1 = ImmutableInstantDoubleTimeSeries.of(times, x1);
    TS2 = ImmutableInstantDoubleTimeSeries.of(times, x2);
    M1 = new HashMap<>();
    M1.put(UnderlyingType.SPOT_PRICE, TS1);
    M2 = new HashMap<>();
    M2.put(UnderlyingType.SPOT_PRICE, TS2);
    DATA = new SensitivityAndReturnDataBundle(S1, VALUE_GAMMA, M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSensitivity() {
    new SensitivityAndReturnDataBundle(null, VALUE_DELTA, M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullReturns() {
    new SensitivityAndReturnDataBundle(S1, VALUE_DELTA, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyReturns() {
    new SensitivityAndReturnDataBundle(S1, VALUE_DELTA, Collections.<UnderlyingType, DoubleTimeSeries<?>> emptyMap());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullKey() {
    final Map<UnderlyingType, DoubleTimeSeries<?>> m = new HashMap<>();
    m.put(null, TS1);
    new SensitivityAndReturnDataBundle(S1, VALUE_GAMMA, m);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEntry() {
    final Map<UnderlyingType, DoubleTimeSeries<?>> m = new HashMap<>();
    m.put(UnderlyingType.SPOT_PRICE, null);
    new SensitivityAndReturnDataBundle(S1, VALUE_GAMMA, m);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongUnderlyings1() {
    final Sensitivity<ValueGreek> s = new ValueGreekSensitivity(new ValueGreek(Greek.VANNA), "ValueVanna");
    new SensitivityAndReturnDataBundle(s, VALUE_DELTA, M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongUnderlyings2() {
    final Sensitivity<ValueGreek> s = new ValueGreekSensitivity(new ValueGreek(Greek.VEGA), "ValueVega");
    new SensitivityAndReturnDataBundle(s, VALUE_DELTA, M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingType() {
    DATA.getReturnTimeSeriesForUnderlying(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongUnderlyingType() {
    DATA.getReturnTimeSeriesForUnderlying(UnderlyingType.BOND_YIELD);
  }

  @Test
  public void testEqualsAndHashCode() {
    SensitivityAndReturnDataBundle other = new SensitivityAndReturnDataBundle(S1, VALUE_GAMMA, M1);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new SensitivityAndReturnDataBundle(S2, VALUE_GAMMA, M1);
    assertFalse(other.equals(DATA));
    other = new SensitivityAndReturnDataBundle(S1, VALUE_DELTA, M1);
    assertFalse(other.equals(DATA));
    other = new SensitivityAndReturnDataBundle(S1, VALUE_GAMMA, M2);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getReturnTimeSeriesForUnderlying(UnderlyingType.SPOT_PRICE), TS1);
    assertEquals(DATA.getSensitivity(), S1);
    assertEquals(DATA.getUnderlying(), S1.getUnderlying());
    assertEquals(DATA.getUnderlyingReturnTS(), M1);
    final List<UnderlyingType> underlyings1 = DATA.getUnderlyingTypes();
    final Set<UnderlyingType> underlyings2 = M1.keySet();
    assertEquals(underlyings1.size(), underlyings2.size());
    for (final UnderlyingType u : underlyings1) {
      assertTrue(underlyings2.contains(u));
    }
    assertEquals(DATA.getValue(), VALUE_GAMMA, 0);
  }
}
