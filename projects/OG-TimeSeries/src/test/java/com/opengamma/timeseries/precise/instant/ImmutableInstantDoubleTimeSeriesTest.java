/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import static org.testng.AssertJUnit.assertEquals;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;

/**
 * Test.
 */
@Test(groups = "unit")
public class ImmutableInstantDoubleTimeSeriesTest extends InstantDoubleTimeSeriesTest {

  @Override
  protected InstantDoubleTimeSeries createEmptyTimeSeries() {
    return ImmutableInstantDoubleTimeSeries.EMPTY_SERIES;
  }

  protected InstantDoubleTimeSeries createStandardTimeSeries() {
    return (InstantDoubleTimeSeries) super.createStandardTimeSeries();
  }

  protected InstantDoubleTimeSeries createStandardTimeSeries2() {
    return (InstantDoubleTimeSeries) super.createStandardTimeSeries2();
  }

  @Override
  protected InstantDoubleTimeSeries createTimeSeries(Instant[] times, double[] values) {
    return ImmutableInstantDoubleTimeSeries.of(times, values);
  }

  @Override
  protected InstantDoubleTimeSeries createTimeSeries(List<Instant> times, List<Double> values) {
    return ImmutableInstantDoubleTimeSeries.of(times, values);
  }

  @Override
  protected InstantDoubleTimeSeries createTimeSeries(DoubleTimeSeries<Instant> dts) {
    return ImmutableInstantDoubleTimeSeries.from(dts);
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  public void test_of_Instant_double() {
    InstantDoubleTimeSeries ts= ImmutableInstantDoubleTimeSeries.of(Instant.ofEpochSecond(12345), 2.0);
    assertEquals(ts.size(), 1);
    assertEquals(ts.getTimeAtIndex(0), Instant.ofEpochSecond(12345));
    assertEquals(ts.getValueAtIndex(0), 2.0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_Instant_double_null() {
    ImmutableInstantDoubleTimeSeries.of((Instant) null, 2.0);
  }

  //-------------------------------------------------------------------------
  public void test_of_InstantArray_DoubleArray() {
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    Double[] inValues = new Double[] {2.0, 3.0};
    InstantDoubleTimeSeries ts= ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), Instant.ofEpochSecond(2222));
    assertEquals(ts.getValueAtIndex(0), 2.0);
    assertEquals(ts.getTimeAtIndex(1), Instant.ofEpochSecond(3333));
    assertEquals(ts.getValueAtIndex(1), 3.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_InstantArray_DoubleArray_wrongOrder() {
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    Double[] inValues = new Double[] {2.0, 3.0, 1.0};
    ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_InstantArray_DoubleArray_mismatchedArrays() {
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222)};
    Double[] inValues = new Double[] {2.0, 3.0};
    ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_InstantArray_DoubleArray_nullDates() {
    Double[] inValues = new Double[] {2.0, 3.0, 1.0};
    ImmutableInstantDoubleTimeSeries.of((Instant[]) null, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_InstantArray_DoubleArray_nullValues() {
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    ImmutableInstantDoubleTimeSeries.of(inDates, (Double[]) null);
  }

  //-------------------------------------------------------------------------
  public void test_of_InstantArray_doubleArray() {
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    double[] inValues = new double[] {2.0, 3.0};
    InstantDoubleTimeSeries ts= ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), Instant.ofEpochSecond(2222));
    assertEquals(ts.getValueAtIndex(0), 2.0);
    assertEquals(ts.getTimeAtIndex(1), Instant.ofEpochSecond(3333));
    assertEquals(ts.getValueAtIndex(1), 3.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_InstantArray_doubleArray_wrongOrder() {
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_InstantArray_doubleArray_mismatchedArrays() {
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222)};
    double[] inValues = new double[] {2.0, 3.0};
    ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_InstantArray_doubleArray_nullDates() {
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableInstantDoubleTimeSeries.of((Instant[]) null, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_InstantArray_doubleArray_nullValues() {
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    ImmutableInstantDoubleTimeSeries.of(inDates, (double[]) null);
  }

  //-------------------------------------------------------------------------
  public void test_of_longArray_doubleArray() {
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {2.0, 3.0};
    InstantDoubleTimeSeries ts= ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), Instant.ofEpochSecond(2222));
    assertEquals(ts.getValueAtIndex(0), 2.0);
    assertEquals(ts.getTimeAtIndex(1), Instant.ofEpochSecond(3333));
    assertEquals(ts.getValueAtIndex(1), 3.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_longArray_doubleArray_wrongOrder() {
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_longArray_doubleArray_mismatchedArrays() {
    long[] inDates = new long[] {2222_000_000_000L};
    double[] inValues = new double[] {2.0, 3.0};
    ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_longArray_doubleArray_nullDates() {
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableInstantDoubleTimeSeries.of((long[]) null, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_longArray_doubleArray_nullValues() {
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    ImmutableInstantDoubleTimeSeries.of(inDates, (double[]) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_intersectionFirstValue_selectFirst() {
    final InstantDoubleTimeSeries dts = createStandardTimeSeries();
    final InstantDoubleTimeSeries dts2 = createStandardTimeSeries2();
    final InstantDoubleTimeSeries dts3 = ImmutableInstantDoubleTimeSeries.builder()
        .putAll(dts2).put(dts2.getEarliestTime(), -1.0).build();
    
    final InstantDoubleTimeSeries result1 = dts.intersectionFirstValue(dts3);
    assertEquals(3, result1.size());
    assertEquals(Double.valueOf(4.0), result1.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result1.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result1.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result1.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result1.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result1.getTimeAtIndex(2));
    
    final InstantDoubleTimeSeries result2 = dts3.intersectionFirstValue(dts);
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
    final InstantDoubleTimeSeries dts = createStandardTimeSeries();
    final InstantDoubleTimeSeries dts2 = createStandardTimeSeries2();
    final InstantDoubleTimeSeries dts3 = ImmutableInstantDoubleTimeSeries.builder()
        .putAll(dts2).put(dts2.getEarliestTime(), -1.0).build();
    
    final InstantDoubleTimeSeries result2 = dts.intersectionSecondValue(dts3);
    assertEquals(3, result2.size());
    assertEquals(Double.valueOf(-1.0), result2.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result2.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result2.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result2.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result2.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result2.getTimeAtIndex(2));
    
    final InstantDoubleTimeSeries result1 = dts3.intersectionSecondValue(dts);
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
    InstantDoubleTimeSeries ts= ImmutableInstantDoubleTimeSeries.of(Instant.ofEpochSecond(2222), 2.0);
    assertEquals("ImmutableInstantDoubleTimeSeries[(" + Instant.ofEpochSecond(2222) + ", 2.0)]", ts.toString());
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  public void test_builder_nothingAdded() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    assertEquals(ImmutableInstantDoubleTimeSeries.EMPTY_SERIES, bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_iterator() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0).put(Instant.ofEpochSecond(3333), 3.0).put(Instant.ofEpochSecond(1111), 1.0);
    InstantDoubleEntryIterator it = bld.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(Instant.ofEpochSecond(1111), 1.0d), it.next());
    assertEquals(Instant.ofEpochSecond(1111), it.currentTime());
    assertEquals(1111_000_000_000L, it.currentTimeFast());
    assertEquals(1.0d, it.currentValue());
    assertEquals(1.0d, it.currentValueFast());
    assertEquals(Instant.ofEpochSecond(2222), it.nextTime());
    assertEquals(Instant.ofEpochSecond(3333), it.nextTime());
    assertEquals(false, it.hasNext());
  }

  public void test_iterator_empty() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    assertEquals(false, bld.iterator().hasNext());
  }

  public void test_iterator_removeFirst() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0).put(Instant.ofEpochSecond(3333), 3.0).put(Instant.ofEpochSecond(1111), 1.0);
    InstantDoubleEntryIterator it = bld.iterator();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    double[] outValues = new double[] {2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_iterator_removeMid() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0).put(Instant.ofEpochSecond(3333), 3.0).put(Instant.ofEpochSecond(1111), 1.0);
    InstantDoubleEntryIterator it = bld.iterator();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(3333)};
    double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_iterator_removeLast() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0).put(Instant.ofEpochSecond(3333), 3.0).put(Instant.ofEpochSecond(1111), 1.0);
    InstantDoubleEntryIterator it = bld.iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222)};
    double[] outValues = new double[] {1.0, 2.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_put_LD() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0).put(Instant.ofEpochSecond(3333), 3.0).put(Instant.ofEpochSecond(1111), 1.0);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_put_Instant_alreadyThere() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0).put(Instant.ofEpochSecond(3333), 3.0).put(Instant.ofEpochSecond(2222), 1.0);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_put_long() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(2222_000_000_000L, 2.0).put(3333_000_000_000L, 3.0).put(1111_000_000_000L, 1.0);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_put_long_alreadyThere() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(2222_000_000_000L, 2.0).put(3333_000_000_000L, 3.0).put(2222_000_000_000L, 1.0);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_put_long_big() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    long[] outDates = new long[600];
    double[] outValues = new double[600];
    for (int i = 0; i < 600; i++) {
      bld.put(2222_000_000_000L + i, i);
      outDates[i] = 2222_000_000_000L + i;
      outValues[i] = i;
    }
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_LD() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_builder_putAll_Instant_mismatchedArrays() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_long() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_builder_putAll_long_mismatchedArrays() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_DDTS() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_DDTS_range_allNonEmptyBuilder() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.put(Instant.ofEpochSecond(0), 0.5).putAll(ddts, 0, 3);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(0), Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    double[] outValues = new double[] {0.5, 1.0, 2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_DDTS_range_fromStart() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 0, 1);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111)};
    double[] outValues = new double[] {1.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_DDTS_range_toEnd() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, 3);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    double[] outValues = new double[] {2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_DDTS_range_empty() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.put(Instant.ofEpochSecond(0), 0.5).putAll(ddts, 1, 1);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(0)};
    double[] outValues = new double[] {0.5};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startInvalidLow() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, -1, 3);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startInvalidHigh() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 4, 2);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_endInvalidLow() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, -1);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_endInvalidHigh() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 4);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startEndOrder() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    PreciseDoubleTimeSeries<?> ddts = ImmutableInstantDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 2);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_Map() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    Map<Instant, Double> map = new HashMap<>();
    map.put(Instant.ofEpochSecond(2222), 2.0d);
    map.put(Instant.ofEpochSecond(3333), 3.0d);
    map.put(Instant.ofEpochSecond(1111), 1.0d);
    bld.putAll(map);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_Map_empty() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    Map<Instant, Double> map = new HashMap<>();
    bld.put(Instant.ofEpochSecond(0), 0.5).putAll(map);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(0)};
    double[] outValues = new double[] {0.5};
    assertEquals(ImmutableInstantDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_clearEmpty() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.clear();
    assertEquals(ImmutableInstantDoubleTimeSeries.EMPTY_SERIES, bld.build());
  }

  public void test_builder_clearSomething() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    bld.put(2222_000_000_000L, 1.0).clear();
    assertEquals(ImmutableInstantDoubleTimeSeries.EMPTY_SERIES, bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_toString() {
    InstantDoubleTimeSeriesBuilder bld = ImmutableInstantDoubleTimeSeries.builder();
    assertEquals("Builder[size=1]", bld.put(2222_000_000_000L, 1.0).toString());
  }

}
