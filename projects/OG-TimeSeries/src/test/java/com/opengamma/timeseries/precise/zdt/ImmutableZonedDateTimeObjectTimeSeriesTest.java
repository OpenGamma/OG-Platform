/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.precise.PreciseObjectTimeSeries;

/**
 * Test.
 */
@Test(groups = "unit")
public class ImmutableZonedDateTimeObjectTimeSeriesTest extends ZonedDateTimeObjectTimeSeriesTest {

  private static final ZonedDateTime ZDT_0 = Instant.ofEpochSecond(0).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_1111 = Instant.ofEpochSecond(1111).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_2222 = Instant.ofEpochSecond(2222).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_3333 = Instant.ofEpochSecond(3333).atZone(ZoneOffset.UTC);
  private static final ZonedDateTime ZDT_12345 = Instant.ofEpochSecond(12345).atZone(ZoneOffset.UTC);

  @Override
  protected ZonedDateTimeObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return ImmutableZonedDateTimeObjectTimeSeries.ofEmpty(ZoneOffset.UTC);
  }

  protected ZonedDateTimeObjectTimeSeries<BigDecimal> createStandardTimeSeries() {
    return (ZonedDateTimeObjectTimeSeries<BigDecimal>) super.createStandardTimeSeries();
  }

  protected ZonedDateTimeObjectTimeSeries<BigDecimal> createStandardTimeSeries2() {
    return (ZonedDateTimeObjectTimeSeries<BigDecimal>) super.createStandardTimeSeries2();
  }

  @Override
  protected ObjectTimeSeries<ZonedDateTime, BigDecimal> createTimeSeries(ZonedDateTime[] times, BigDecimal[] values) {
    return ImmutableZonedDateTimeObjectTimeSeries.of(times, values, ZoneOffset.UTC);
  }

  @Override
  protected ZonedDateTimeObjectTimeSeries<BigDecimal> createTimeSeries(List<ZonedDateTime> times, List<BigDecimal> values) {
    return ImmutableZonedDateTimeObjectTimeSeries.of(times, values, ZoneOffset.UTC);
  }

  @Override
  protected ObjectTimeSeries<ZonedDateTime, BigDecimal> createTimeSeries(ObjectTimeSeries<ZonedDateTime, BigDecimal> dts) {
    return ImmutableZonedDateTimeObjectTimeSeries.from(dts, ZoneOffset.UTC);
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  public void test_of_ZonedDateTime_value() {
    ZonedDateTimeObjectTimeSeries<Float> ts= ImmutableZonedDateTimeObjectTimeSeries.of(ZDT_12345, 2.0f);
    assertEquals(ts.size(), 1);
    assertEquals(ts.getTimeAtIndex(0), ZDT_12345);
    assertEquals(ts.getValueAtIndex(0), 2.0f);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_ZonedDateTime_value_null() {
    ImmutableZonedDateTimeObjectTimeSeries.of((ZonedDateTime) null, 2.0f);
  }

  //-------------------------------------------------------------------------
  public void test_of_ZonedDateTimeArray_valueArray() {
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    Float[] inValues = new Float[] {2.0f, 3.0f};
    ZonedDateTimeObjectTimeSeries<Float> ts= ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, null);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), ZDT_2222);
    assertEquals(ts.getValueAtIndex(0), 2.0f);
    assertEquals(ts.getTimeAtIndex(1), ZDT_3333);
    assertEquals(ts.getValueAtIndex(1), 3.0f);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_ZonedDateTimeArray_valueArray_wrongOrder() {
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_ZonedDateTimeArray_valueArray_mismatchedArrays() {
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222};
    Float[] inValues = new Float[] {2.0f, 3.0f};
    ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_ZonedDateTimeArray_valueArray_nullDates() {
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableZonedDateTimeObjectTimeSeries.of((ZonedDateTime[]) null, inValues, null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_ZonedDateTimeArray_valueArray_nullValues() {
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    ImmutableZonedDateTimeObjectTimeSeries.of(inDates, (Float[]) null, null);
  }

  //-------------------------------------------------------------------------
  public void test_of_longArray_valueArray() {
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {2.0f, 3.0f};
    ZonedDateTimeObjectTimeSeries<Float> ts= ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), ZDT_2222);
    assertEquals(ts.getValueAtIndex(0), 2.0f);
    assertEquals(ts.getTimeAtIndex(1), ZDT_3333);
    assertEquals(ts.getValueAtIndex(1), 3.0f);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_longArray_valueArray_wrongOrder() {
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_longArray_valueArray_mismatchedArrays() {
    long[] inDates = new long[] {2222_000_000_000L};
    Float[] inValues = new Float[] {2.0f, 3.0f};
    ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_longArray_valueArray_nullDates() {
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableZonedDateTimeObjectTimeSeries.of((long[]) null, inValues, ZoneOffset.UTC);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_longArray_valueArray_nullValues() {
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    ImmutableZonedDateTimeObjectTimeSeries.of(inDates, (Float[]) null, ZoneOffset.UTC);
  }

  //-------------------------------------------------------------------------
  public void test_toString() {
    ZonedDateTimeObjectTimeSeries<Float> ts= ImmutableZonedDateTimeObjectTimeSeries.of(ZDT_2222, 2.0f);
    assertEquals("ImmutableZonedDateTimeObjectTimeSeries[(" + ZDT_2222 + ", 2.0)]", ts.toString());
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  public void test_builder_nothingAdded() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.ofEmpty(ZoneOffset.UTC), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_iterator() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0f).put(ZDT_3333, 3.0f).put(ZDT_1111, 1.0f);
    ZonedDateTimeObjectEntryIterator<Float> it = bld.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(ZDT_1111, 1.0f), it.next());
    assertEquals(ZDT_1111, it.currentTime());
    assertEquals(1111_000_000_000L, it.currentTimeFast());
    assertEquals(1.0f, it.currentValue());
    assertEquals(ZDT_2222, it.nextTime());
    assertEquals(ZDT_3333, it.nextTime());
    assertEquals(false, it.hasNext());
  }

  public void test_iterator_empty() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    assertEquals(false, bld.iterator().hasNext());
  }

  public void test_iterator_removeFirst() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0f).put(ZDT_3333, 3.0f).put(ZDT_1111, 1.0f);
    ZonedDateTimeObjectEntryIterator<Float> it = bld.iterator();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    Float[] outValues = new Float[] {2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_iterator_removeMid() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0f).put(ZDT_3333, 3.0f).put(ZDT_1111, 1.0f);
    ZonedDateTimeObjectEntryIterator<Float> it = bld.iterator();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_3333};
    Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_iterator_removeLast() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0f).put(ZDT_3333, 3.0f).put(ZDT_1111, 1.0f);
    ZonedDateTimeObjectEntryIterator<Float> it = bld.iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222};
    Float[] outValues = new Float[] {1.0f, 2.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_put_LD() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0f).put(ZDT_3333, 3.0f).put(ZDT_1111, 1.0f);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_builder_put_ZonedDateTime_alreadyThere() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(ZDT_2222, 2.0f).put(ZDT_3333, 3.0f).put(ZDT_2222, 1.0f);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_put_long() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(2222_000_000_000L, 2.0f).put(3333_000_000_000L, 3.0f).put(1111_000_000_000L, 1.0f);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_builder_put_long_alreadyThere() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(2222_000_000_000L, 2.0f).put(3333_000_000_000L, 3.0f).put(2222_000_000_000L, 1.0f);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_builder_put_long_big() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    long[] outDates = new long[600];
    Float[] outValues = new Float[600];
    for (int i = 0; i < 600; i++) {
      bld.put(2222_000_000_000L + i, (float) i);
      outDates[i] = 2222_000_000_000L + i;
      outValues[i] = (float) i;
    }
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, ZoneOffset.UTC), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_LD() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333, ZDT_1111};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_builder_putAll_ZonedDateTime_mismatchedArrays() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    ZonedDateTime[] inDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_long() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_builder_putAll_long_mismatchedArrays() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_DDTS() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_DDTS_range_allNonEmptyBuilder() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.put(ZDT_0, 0.5f).putAll(ddts, 0, 3);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_0, ZDT_1111, ZDT_2222, ZDT_3333};
    Float[] outValues = new Float[] {0.5f, 1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_builder_putAll_DDTS_range_fromStart() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 0, 1);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111};
    Float[] outValues = new Float[] {1.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_builder_putAll_DDTS_range_toEnd() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 1, 3);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_2222, ZDT_3333};
    Float[] outValues = new Float[] {2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_builder_putAll_DDTS_range_empty() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.put(ZDT_0, 0.5f).putAll(ddts, 1, 1);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_0};
    Float[] outValues = new Float[] {0.5f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startInvalidLow() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, -1, 3);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startInvalidHigh() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 4, 2);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_endInvalidLow() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 1, -1);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_endInvalidHigh() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 3, 4);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startEndOrder() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableZonedDateTimeObjectTimeSeries.of(inDates, inValues, ZoneOffset.UTC);
    bld.putAll(ddts, 3, 2);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_Map() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    Map<ZonedDateTime, Float> map = new HashMap<>();
    map.put(ZDT_2222, 2.0f);
    map.put(ZDT_3333, 3.0f);
    map.put(ZDT_1111, 1.0f);
    bld.putAll(map);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_1111, ZDT_2222, ZDT_3333};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  public void test_builder_putAll_Map_empty() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    Map<ZonedDateTime, Float> map = new HashMap<>();
    bld.put(ZDT_0, 0.5f).putAll(map);
    ZonedDateTime[] outDates = new ZonedDateTime[] {ZDT_0};
    Float[] outValues = new Float[] {0.5f};
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.of(outDates, outValues, null), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_clearEmpty() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.clear();
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.ofEmpty(ZoneOffset.UTC), bld.build());
  }

  public void test_builder_clearSomething() {
    ZonedDateTimeObjectTimeSeriesBuilder<Float> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    bld.put(2222_000_000_000L, 1.0f).clear();
    assertEquals(ImmutableZonedDateTimeObjectTimeSeries.ofEmpty(ZoneOffset.UTC), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_toString() {
    ZonedDateTimeObjectTimeSeriesBuilder<BigDecimal> bld = ImmutableZonedDateTimeObjectTimeSeries.builder(ZoneOffset.UTC);
    assertEquals("Builder[size=1]", bld.put(2222_000_000_000L, BigDecimal.valueOf(1.0)).toString());
  }

}
