/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.historical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class HistoricalVaRDataBundleTest {

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor() {
    new HistoricalVaRDataBundle(null);
  }

  @Test
  public void testHashCodeAndEquals() {
    DoubleTimeSeries<?> ts1 = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new long[] {1, 2, 3, 4, 5}, new double[] {0, 0.1, 0.2, 0.3, 0.4});
    DoubleTimeSeries<?> ts2 = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new long[] {1, 2, 3, 4, 5}, new double[] {0, -0.1, 0.2, 0.3, 0.4});
    HistoricalVaRDataBundle data1 = new HistoricalVaRDataBundle(ts1);
    HistoricalVaRDataBundle data2 = new HistoricalVaRDataBundle(ts2);
    assertEquals(data1, new HistoricalVaRDataBundle(ts1));
    assertEquals(data1.hashCode(), new HistoricalVaRDataBundle(ts1).hashCode());
    assertFalse(data1.equals(data2));
  }
}
