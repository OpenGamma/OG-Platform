/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import static org.testng.AssertJUnit.assertEquals;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;

/**
 * Test.
 */
@Test(groups = "unit")
public class ImmutableZonedDateTimeDoubleTimeSeriesTest extends ZonedDateTimeDoubleTimeSeriesTest {

  private static final ZonedDateTime ZDT_0 = Instant.ofEpochSecond(0).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_1111 = Instant.ofEpochSecond(1111).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_2222 = Instant.ofEpochSecond(2222).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_3333 = Instant.ofEpochSecond(3333).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_12345 = Instant.ofEpochSecond(12345).atZone(ZoneOffset.UTC);

  @Override
  protected ZonedDateTimeDoubleTimeSeries createEmptyTimeSeries() {
    return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC);
  }

  protected ZonedDateTimeDoubleTimeSeries createStandardTimeSeries() {
    return (ZonedDateTimeDoubleTimeSeries) super.createStandardTimeSeries();
  }

  protected ZonedDateTimeDoubleTimeSeries createStandardTimeSeries2() {
    return (ZonedDateTimeDoubleTimeSeries) super.createStandardTimeSeries2();
  }

  @Override
  protected ZonedDateTimeDoubleTimeSeries createTimeSeries(ZonedDateTime[] times, double[] values) {
    return ImmutableZonedDateTimeDoubleTimeSeries.of(times, values, ZoneOffset.UTC);
  }

  @Override
  protected ZonedDateTimeDoubleTimeSeries createTimeSeries(List<ZonedDateTime> times, List<Double> values) {
    return ImmutableZonedDateTimeDoubleTimeSeries.of(times, values, ZoneOffset.UTC);
  }

  @Override
  protected ZonedDateTimeDoubleTimeSeries createTimeSeries(DoubleTimeSeries<ZonedDateTime> dts) {
    return ImmutableZonedDateTimeDoubleTimeSeries.from(dts, ZoneOffset.UTC);
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  public void test_of_ZonedDateTime_double() {
    ZonedDateTimeDoubleTimeSeries ts= ImmutableZonedDateTimeDoubleTimeSeries.of(ZDT_12345, 2.0);
    assertEquals(ts.size(), 1);
    assertEquals(ts.getTimeAtIndex(0), ZDT_12345);
    assertEquals(ts.getValueAtIndex(0), 2.0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_ZonedDateTime_double_null() {
    ImmutableZonedDateTimeDoubleTimeSeries.of((ZonedDateTime) null, 2.0);
  }

  //-------------------------------------------------------------------------
  public void test_of_ZonedDateTimeArray_DoubleArray() {
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    Double[] inValues = new Double[] {2.0, 3.0};
    ZonedDateTimeDoubleTimeSeries ts= ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, null);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), ZDT_2222);
    assertEquals(ts.getValueAtIndex(0), 2.0);
    assertEquals(ts.getTimeAtIndex(1), ZDT_3333);
    assertEquals(ts.getValueAtIndex(1), 3.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_ZonedDateTimeArray_DoubleArray_wrongOrder() {
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    Double[] inValues = new Double[] {2.0, 3.0, 1.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_ZonedDateTimeArray_DoubleArray_mismatchedArrays() {
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222};
    Double[] inValues = new Double[] {2.0, 3.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_ZonedDateTimeArray_DoubleArray_nullDates() {
    Double[] inValues = new Double[] {2.0, 3.0, 1.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of((ZonedDateTime[]) null, inValues, null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_ZonedDateTimeArray_DoubleArray_nullValues() {
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, (Double[]) null, null);
  }

  //-------------------------------------------------------------------------
  public void test_of_ZonedDateTimeArray_doubleArray() {
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    double[] inValues = new double[] {2.0, 3.0};
    ZonedDateTimeDoubleTimeSeries ts= ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, null);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), ZDT_2222);
    assertEquals(ts.getValueAtIndex(0), 2.0);
    assertEquals(ts.getTimeAtIndex(1), ZDT_3333);
    assertEquals(ts.getValueAtIndex(1), 3.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_ZonedDateTimeArray_doubleArray_wrongOrder() {
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_ZonedDateTimeArray_doubleArray_mismatchedArrays() {
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222};
    double[] inValues = new double[] {2.0, 3.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_ZonedDateTimeArray_doubleArray_nullDates() {
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of((ZonedDateTime[]) null, inValues, null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_ZonedDateTimeArray_doubleArray_nullValues() {
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, (double[]) null, null);
  }

  //-------------------------------------------------------------------------
  public void test_of_longArray_doubleArray() {
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {2.0, 3.0};
    ZonedDateTimeDoubleTimeSeries ts= ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), ZDT_2222);
    assertEquals(ts.getValueAtIndex(0), 2.0);
    assertEquals(ts.getTimeAtIndex(1), ZDT_3333);
    assertEquals(ts.getValueAtIndex(1), 3.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_longArray_doubleArray_wrongOrder() {
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_longArray_doubleArray_mismatchedArrays() {
    long[] inDates = new long[] {2222_000_000_000L};
    double[] inValues = new double[] {2.0, 3.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_longArray_doubleArray_nullDates() {
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableZonedDateTimeDoubleTimeSeries.of((long[]) null, inValues, ZoneOffset.UTC);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_longArray_doubleArray_nullValues() {
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, (double[]) null, ZoneOffset.UTC);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_intersectionFirstValue_selectFirst() {
    final ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    final ZonedDateTimeDoubleTimeSeries dts2 = createStandardTimeSeries2();
    final ZonedDateTimeDoubleTimeSeries dts3 = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .putAll(dts2).put(dts2.getEarliestTime(), -1.0).build();
    
    final ZonedDateTimeDoubleTimeSeries result1 = dts.intersectionFirstValue(dts3);
    assertEquals(3, result1.size());
    assertEquals(Double.valueOf(4.0), result1.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result1.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result1.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result1.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result1.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result1.getTimeAtIndex(2));
    
    final ZonedDateTimeDoubleTimeSeries result2 = dts3.intersectionFirstValue(dts);
    assertEquals(3, result2.size());
    assertEquals(Double.valueOf(-1.0), result2.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result2.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result2.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result2.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result2.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result2.getTimeAtIndex(2));
  }

  @Test
  public void test_intersectionSecondValue_selectSecond() {
    final ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    final ZonedDateTimeDoubleTimeSeries dts2 = createStandardTimeSeries2();
    final ZonedDateTimeDoubleTimeSeries dts3 = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC)
        .putAll(dts2).put(dts2.getEarliestTime(), -1.0).build();
    
    final ZonedDateTimeDoubleTimeSeries result2 = dts.intersectionSecondValue(dts3);
    assertEquals(3, result2.size());
    assertEquals(Double.valueOf(-1.0), result2.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result2.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result2.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result2.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result2.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result2.getTimeAtIndex(2));
    
    final ZonedDateTimeDoubleTimeSeries result1 = dts3.intersectionSecondValue(dts);
    assertEquals(3, result1.size());
    assertEquals(Double.valueOf(4.0), result1.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result1.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result1.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result1.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result1.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result1.getTimeAtIndex(2));
  }

  //-------------------------------------------------------------------------
  public void test_toString() {
    ZonedDateTimeDoubleTimeSeries ts= ImmutableZonedDateTimeDoubleTimeSeries.of(ZDT_2222, 2.0);
    assertEquals("ImmutableZonedDateTimeDoubleTimeSeries[(" + ZDT_2222 + ", 2.0)]", ts.toString());
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  public void test_builder_nothingAdded() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_iterator() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    ZonedDateTimeDoubleEntryIterator it = bld.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(ZDT_1111, 1.0d), it.next());
    assertEquals(ZDT_1111, it.currentTime());
    assertEquals(1111_000_000_000L, it.currentTimeFast());
    assertEquals(1.0d, it.currentValue());
    assertEquals(1.0d, it.currentValueFast());
    assertEquals(ZDT_2222, it.nextTime());
    assertEquals(ZDT_3333, it.nextTime());
    assertEquals(false, it.hasNext());
  }

  public void test_iterator_empty() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    assertEquals(false, bld.iterator().hasNext());
  }

  public void test_iterator_removeFirst() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    ZonedDateTimeDoubleEntryIterator it = bld.iterator();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    double[] outValues = new double[] {2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_iterator_removeMid() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    ZonedDateTimeDoubleEntryIterator it = bld.iterator();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_3333};
    double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_iterator_removeLast() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    ZonedDateTimeDoubleEntryIterator it = bld.iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222};
    double[] outValues = new double[] {1.0, 2.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_put_LD() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_1111, 1.0);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_builder_put_ZonedDateTime_alreadyThere() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0).put(ZDT_3333, 3.0).put(ZDT_2222, 1.0);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_put_long() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(2222_000_000_000L, 2.0).put(3333_000_000_000L, 3.0).put(1111_000_000_000L, 1.0);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_builder_put_long_alreadyThere() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(2222_000_000_000L, 2.0).put(3333_000_000_000L, 3.0).put(2222_000_000_000L, 1.0);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_builder_put_long_big() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    long[] outDates = new long[600];
    double[] outValues = new double[600];
    for (int i = 0; i < 600; i++) {
      bld.put(2222_000_000_000L + i, i);
      outDates[i] = 2222_000_000_000L + i;
      outValues[i] = i;
    }
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, ZoneOffset.UTC), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_LD() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_builder_putAll_ZonedDateTime_mismatchedArrays() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_long() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_builder_putAll_long_mismatchedArrays() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_DDTS() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_DDTS_range_allNonEmptyBuilder() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.put(ZDT_0, 0.5).putAll(ddts, 0, 3);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_0, ZDT_1111, ZDT_2222, ZDT_3333};
    double[] outValues = new double[] {0.5, 1.0, 2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_builder_putAll_DDTS_range_fromStart() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 0, 1);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111};
    double[] outValues = new double[] {1.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_builder_putAll_DDTS_range_toEnd() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 1, 3);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    double[] outValues = new double[] {2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_builder_putAll_DDTS_range_empty() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.put(ZDT_0, 0.5).putAll(ddts, 1, 1);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_0};
    double[] outValues = new double[] {0.5};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startInvalidLow() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, -1, 3);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startInvalidHigh() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 4, 2);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_endInvalidLow() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 1, -1);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_endInvalidHigh() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 3, 4);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startEndOrder() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableZonedDateTimeDoubleTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 3, 2);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_Map() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    Map<ZonedDateTime, Double> map = new HashMap<>();
    map.put(ZDT_2222, 2.0d);
    map.put(ZDT_3333, 3.0d);
    map.put(ZDT_1111, 1.0d);
    bld.putAll(map);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_builder_putAll_Map_empty() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    Map<ZonedDateTime, Double> map = new HashMap<>();
    bld.put(ZDT_0, 0.5).putAll(map);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_0};
    double[] outValues = new double[] {0.5};
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_clearEmpty() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.clear();
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC), bld.build());
  }

  public void test_builder_clearSomething() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    bld.put(2222_000_000_000L, 1.0).clear();
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_toString() {
    ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(ZoneOffset.UTC);
    assertEquals("Builder[size=1]", bld.put(2222_000_000_000L, 1.0).toString());
  }

}
