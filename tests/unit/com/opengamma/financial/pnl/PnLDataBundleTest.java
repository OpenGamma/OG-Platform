/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.pnl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.riskfactor.RiskFactorResult;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.financial.sensitivity.ValueGreekSensitivity;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 *
 */
public class PnLDataBundleTest {
  private static final ValueGreekSensitivity DELTA = new ValueGreekSensitivity(new ValueGreek(Greek.DELTA), "A");
  private static final RiskFactorResult DELTA_RESULT = new RiskFactorResult(100.);
  private static final ValueGreekSensitivity VANNA = new ValueGreekSensitivity(new ValueGreek(Greek.VANNA), "A");
  private static final RiskFactorResult VANNA_RESULT = new RiskFactorResult(400.);
  private static final Map<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>> TS_RETURNS = new HashMap<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>>();
  private static final Map<Sensitivity<?>, RiskFactorResult> SENSITIVITIES = new HashMap<Sensitivity<?>, RiskFactorResult>();
  private static final DateTimeNumericEncoding ENCODING = DateTimeNumericEncoding.TIME_EPOCH_MILLIS;
  private static final long[] TIMES = new long[] {400, 401};
  private static final double[] TS_SPOT_DATA = new double[] {0.4, 0.45};
  private static final double[] TS_IMP_VOL_DATA = new double[] {0.7, 0.75};
  private static final DoubleTimeSeries<?> TS_SPOT = new FastArrayLongDoubleTimeSeries(ENCODING, TIMES, TS_SPOT_DATA);
  private static final DoubleTimeSeries<?> TS_IMP_VOL = new FastArrayLongDoubleTimeSeries(ENCODING, TIMES, TS_IMP_VOL_DATA);

  static {
    SENSITIVITIES.put(DELTA, DELTA_RESULT);
    SENSITIVITIES.put(VANNA, VANNA_RESULT);

    Map<Object, DoubleTimeSeries<?>> data = new HashMap<Object, DoubleTimeSeries<?>>();
    data.put(UnderlyingType.SPOT_PRICE, TS_SPOT);
    TS_RETURNS.put(DELTA, data);
    data = new HashMap<Object, DoubleTimeSeries<?>>();
    data.put(UnderlyingType.SPOT_PRICE, TS_SPOT);
    data.put(UnderlyingType.IMPLIED_VOLATILITY, TS_IMP_VOL);
    TS_RETURNS.put(VANNA, data);
  }

  //  @Test(expected = IllegalArgumentException.class)
  //  public void testNullSensitivities() {
  //    new PnLDataBundle(null, TS_RETURNS);
  //  }
  //
  //  @Test(expected = IllegalArgumentException.class)
  //  public void testEmptySensitivities() {
  //    new PnLDataBundle(Collections.<Sensitivity<?>, RiskFactorResult> emptyMap(), TS_RETURNS);
  //  }
  //
  //  @Test(expected = IllegalArgumentException.class)
  //  public void testNullReturns() {
  //    new PnLDataBundle(SENSITIVITIES, null);
  //  }
  //
  //  @Test(expected = IllegalArgumentException.class)
  //  public void testEmptyReturns() {
  //    new PnLDataBundle(SENSITIVITIES, Collections.<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>> emptyMap());
  //  }
  //
  //  @Test(expected = IllegalArgumentException.class)
  //  public void testDifferentSensitivities() {
  //    final Map<Sensitivity<?>, RiskFactorResult> map = new HashMap<Sensitivity<?>, RiskFactorResult>();
  //    map.put(DELTA, DELTA_RESULT);
  //    new PnLDataBundle(map, TS_RETURNS);
  //  }
  //
  //  @Test(expected = IllegalArgumentException.class)
  //  public void testNullTimeSeriesForSensitivity() {
  //    final Map<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>> map = new HashMap<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>>();
  //    Map<Object, DoubleTimeSeries<?>> data = new HashMap<Object, DoubleTimeSeries<?>>();
  //    data.put(UnderlyingType.SPOT_PRICE, null);
  //    map.put(DELTA, data);
  //    data = new HashMap<Object, DoubleTimeSeries<?>>();
  //    data.put(UnderlyingType.SPOT_PRICE, TS_SPOT);
  //    data.put(UnderlyingType.IMPLIED_VOLATILITY, TS_IMP_VOL);
  //    map.put(VANNA, data);
  //    new PnLDataBundle(SENSITIVITIES, map);
  //  }
  //
  //  @Test(expected = IllegalArgumentException.class)
  //  public void testEmptyTimeSeriesForSensitivity() {
  //    final Map<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>> map = new HashMap<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>>();
  //    Map<Object, DoubleTimeSeries<?>> data = new HashMap<Object, DoubleTimeSeries<?>>();
  //    data.put(UnderlyingType.SPOT_PRICE, FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  //    map.put(DELTA, data);
  //    data = new HashMap<Object, DoubleTimeSeries<?>>();
  //    data.put(UnderlyingType.SPOT_PRICE, TS_SPOT);
  //    data.put(UnderlyingType.IMPLIED_VOLATILITY, TS_IMP_VOL);
  //    map.put(VANNA, data);
  //    new PnLDataBundle(SENSITIVITIES, map);
  //  }
  //
  //  @Test
  //  public void test() {
  //    final PnLDataBundle data = new PnLDataBundle(SENSITIVITIES, TS_RETURNS);
  //    assertEquals(data.getEncoding(), ENCODING);
  //    final long[] times = data.getTimes();
  //    for (int i = 0; i < TIMES.length; i++) {
  //      assertEquals(times[i], TIMES[i]);
  //    }
  //    assertEquals(data.getSensitivities(), SENSITIVITIES);
  //    final Map<Sensitivity<?>, Map<Object, double[]>> tsReturns = data.getTimeSeriesReturns();
  //    assertEquals(tsReturns.keySet(), SENSITIVITIES.keySet());
  //    final Map<Object, double[]> deltaData = tsReturns.get(DELTA);
  //    assertEquals(deltaData.size(), 1);
  //    assertEquals(deltaData.keySet().iterator().next(), UnderlyingType.SPOT_PRICE);
  //    assertEquals(deltaData.values().iterator().next()[0], TS_SPOT_DATA[0], 0);
  //    assertEquals(deltaData.values().iterator().next()[1], TS_SPOT_DATA[1], 0);
  //    final Map<Object, double[]> vannaData = tsReturns.get(VANNA);
  //    assertEquals(vannaData.size(), 2);
  //    assertTrue(vannaData.containsKey(UnderlyingType.SPOT_PRICE));
  //    assertTrue(vannaData.containsKey(UnderlyingType.IMPLIED_VOLATILITY));
  //    assertEquals(vannaData.get(UnderlyingType.SPOT_PRICE)[0], TS_SPOT_DATA[0], 0);
  //    assertEquals(vannaData.get(UnderlyingType.SPOT_PRICE)[1], TS_SPOT_DATA[1], 0);
  //    assertEquals(vannaData.get(UnderlyingType.IMPLIED_VOLATILITY)[0], TS_IMP_VOL_DATA[0], 0);
  //    assertEquals(vannaData.get(UnderlyingType.IMPLIED_VOLATILITY)[1], TS_IMP_VOL_DATA[1], 0);
  //  }

  @Test
  public void testHashCodeAndEquals() {
    final PnLDataBundle data = new PnLDataBundle(SENSITIVITIES, TS_RETURNS);
    PnLDataBundle other = new PnLDataBundle(SENSITIVITIES, TS_RETURNS);
    assertEquals(data, other);
    assertEquals(data.hashCode(), other.hashCode());
    final Map<Sensitivity<?>, RiskFactorResult> sensitivities = new HashMap<Sensitivity<?>, RiskFactorResult>();
    sensitivities.put(DELTA, DELTA_RESULT);
    sensitivities.put(VANNA, DELTA_RESULT);
    other = new PnLDataBundle(sensitivities, TS_RETURNS);
    assertFalse(other.equals(data));
    final Map<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>> tsReturns = new HashMap<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>>();
    Map<Object, DoubleTimeSeries<?>> tsData = new HashMap<Object, DoubleTimeSeries<?>>();
    tsData.put(UnderlyingType.SPOT_PRICE, TS_IMP_VOL);
    tsReturns.put(DELTA, tsData);
    tsData = new HashMap<Object, DoubleTimeSeries<?>>();
    tsData.put(UnderlyingType.SPOT_PRICE, TS_SPOT);
    tsData.put(UnderlyingType.IMPLIED_VOLATILITY, TS_IMP_VOL);
    tsReturns.put(VANNA, tsData);
    other = new PnLDataBundle(SENSITIVITIES, tsReturns);
    assertFalse(other.equals(data));
  }
}
