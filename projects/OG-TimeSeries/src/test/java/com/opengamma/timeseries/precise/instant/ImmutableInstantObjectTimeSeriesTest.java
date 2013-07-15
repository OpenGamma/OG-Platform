/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.precise.PreciseObjectTimeSeries;

/**
 * Test.
 */
@Test(groups = "unit")
public class ImmutableInstantObjectTimeSeriesTest extends InstantObjectTimeSeriesTest {

  @Override
  protected InstantObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return ImmutableInstantObjectTimeSeries.ofEmpty();
  }

  protected InstantObjectTimeSeries<BigDecimal> createStandardTimeSeries() {
    return (InstantObjectTimeSeries<BigDecimal>) super.createStandardTimeSeries();
  }

  protected InstantObjectTimeSeries<BigDecimal> createStandardTimeSeries2() {
    return (InstantObjectTimeSeries<BigDecimal>) super.createStandardTimeSeries2();
  }

  @Override
  protected ObjectTimeSeries<Instant, BigDecimal> createTimeSeries(Instant[] times, BigDecimal[] values) {
    return ImmutableInstantObjectTimeSeries.of(times, values);
  }

  @Override
  protected InstantObjectTimeSeries<BigDecimal> createTimeSeries(List<Instant> times, List<BigDecimal> values) {
    return ImmutableInstantObjectTimeSeries.of(times, values);
  }

  @Override
  protected ObjectTimeSeries<Instant, BigDecimal> createTimeSeries(ObjectTimeSeries<Instant, BigDecimal> dts) {
    return ImmutableInstantObjectTimeSeries.from(dts);
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  public void test_of_Instant_value() {
    InstantObjectTimeSeries<Float> ts= ImmutableInstantObjectTimeSeries.of(Instant.ofEpochSecond(12345), 2.0f);
    assertEquals(ts.size(), 1);
    assertEquals(ts.getTimeAtIndex(0), Instant.ofEpochSecond(12345));
    assertEquals(ts.getValueAtIndex(0), 2.0f);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_Instant_value_null() {
    ImmutableInstantObjectTimeSeries.of((Instant) null, 2.0f);
  }

  //-------------------------------------------------------------------------
  public void test_of_InstantArray_valueArray() {
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    Float[] inValues = new Float[] {2.0f, 3.0f};
    InstantObjectTimeSeries<Float> ts= ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), Instant.ofEpochSecond(2222));
    assertEquals(ts.getValueAtIndex(0), 2.0f);
    assertEquals(ts.getTimeAtIndex(1), Instant.ofEpochSecond(3333));
    assertEquals(ts.getValueAtIndex(1), 3.0f);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_InstantArray_valueArray_wrongOrder() {
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableInstantObjectTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_InstantArray_valueArray_mismatchedArrays() {
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222)};
    Float[] inValues = new Float[] {2.0f, 3.0f};
    ImmutableInstantObjectTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_InstantArray_valueArray_nullDates() {
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableInstantObjectTimeSeries.of((Instant[]) null, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_InstantArray_valueArray_nullValues() {
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    ImmutableInstantObjectTimeSeries.of(inDates, (Float[]) null);
  }

  //-------------------------------------------------------------------------
  public void test_of_longArray_valueArray() {
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {2.0f, 3.0f};
    InstantObjectTimeSeries<Float> ts= ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), Instant.ofEpochSecond(2222));
    assertEquals(ts.getValueAtIndex(0), 2.0f);
    assertEquals(ts.getTimeAtIndex(1), Instant.ofEpochSecond(3333));
    assertEquals(ts.getValueAtIndex(1), 3.0f);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_longArray_valueArray_wrongOrder() {
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableInstantObjectTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_longArray_valueArray_mismatchedArrays() {
    long[] inDates = new long[] {2222_000_000_000L};
    Float[] inValues = new Float[] {2.0f, 3.0f};
    ImmutableInstantObjectTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_longArray_valueArray_nullDates() {
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableInstantObjectTimeSeries.of((long[]) null, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_longArray_valueArray_nullValues() {
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    ImmutableInstantObjectTimeSeries.of(inDates, (Float[]) null);
  }

  //-------------------------------------------------------------------------
  public void test_toString() {
    InstantObjectTimeSeries<Float> ts= ImmutableInstantObjectTimeSeries.of(Instant.ofEpochSecond(2222), 2.0f);
    assertEquals("ImmutableInstantObjectTimeSeries[(" + Instant.ofEpochSecond(2222) + ", 2.0)]", ts.toString());
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  public void test_builder_nothingAdded() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    assertEquals(ImmutableInstantObjectTimeSeries.ofEmpty(), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_iterator() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0f).put(Instant.ofEpochSecond(3333), 3.0f).put(Instant.ofEpochSecond(1111), 1.0f);
    InstantObjectEntryIterator<Float> it = bld.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(Instant.ofEpochSecond(1111), 1.0f), it.next());
    assertEquals(Instant.ofEpochSecond(1111), it.currentTime());
    assertEquals(1111_000_000_000L, it.currentTimeFast());
    assertEquals(1.0f, it.currentValue());
    assertEquals(Instant.ofEpochSecond(2222), it.nextTime());
    assertEquals(Instant.ofEpochSecond(3333), it.nextTime());
    assertEquals(false, it.hasNext());
  }

  public void test_iterator_empty() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    assertEquals(false, bld.iterator().hasNext());
  }

  public void test_iterator_removeFirst() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0f).put(Instant.ofEpochSecond(3333), 3.0f).put(Instant.ofEpochSecond(1111), 1.0f);
    InstantObjectEntryIterator<Float> it = bld.iterator();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    Float[] outValues = new Float[] {2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_iterator_removeMid() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0f).put(Instant.ofEpochSecond(3333), 3.0f).put(Instant.ofEpochSecond(1111), 1.0f);
    InstantObjectEntryIterator<Float> it = bld.iterator();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(3333)};
    Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_iterator_removeLast() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0f).put(Instant.ofEpochSecond(3333), 3.0f).put(Instant.ofEpochSecond(1111), 1.0f);
    InstantObjectEntryIterator<Float> it = bld.iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222)};
    Float[] outValues = new Float[] {1.0f, 2.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_put_LD() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0f).put(Instant.ofEpochSecond(3333), 3.0f).put(Instant.ofEpochSecond(1111), 1.0f);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_put_Instant_alreadyThere() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(Instant.ofEpochSecond(2222), 2.0f).put(Instant.ofEpochSecond(3333), 3.0f).put(Instant.ofEpochSecond(2222), 1.0f);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_put_long() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(2222_000_000_000L, 2.0f).put(3333_000_000_000L, 3.0f).put(1111_000_000_000L, 1.0f);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_put_long_alreadyThere() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(2222_000_000_000L, 2.0f).put(3333_000_000_000L, 3.0f).put(2222_000_000_000L, 1.0f);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_put_long_big() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    long[] outDates = new long[600];
    Float[] outValues = new Float[600];
    for (int i = 0; i < 600; i++) {
      bld.put(2222_000_000_000L + i, (float) i);
      outDates[i] = 2222_000_000_000L + i;
      outValues[i] = (float) i;
    }
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_LD() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333), Instant.ofEpochSecond(1111)};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_builder_putAll_Instant_mismatchedArrays() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    Instant[] inDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_long() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L, 1111_000_000_000L};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_builder_putAll_long_mismatchedArrays() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    long[] inDates = new long[] {2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_DDTS() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_DDTS_range_allNonEmptyBuilder() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.put(Instant.ofEpochSecond(0), 0.5f).putAll(ddts, 0, 3);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(0), Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    Float[] outValues = new Float[] {0.5f, 1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_DDTS_range_fromStart() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 0, 1);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111)};
    Float[] outValues = new Float[] {1.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_DDTS_range_toEnd() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, 3);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    Float[] outValues = new Float[] {2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_DDTS_range_empty() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.put(Instant.ofEpochSecond(0), 0.5f).putAll(ddts, 1, 1);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(0)};
    Float[] outValues = new Float[] {0.5f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startInvalidLow() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, -1, 3);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startInvalidHigh() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 4, 2);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_endInvalidLow() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, -1);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_endInvalidHigh() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 4);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startEndOrder() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    long[] inDates = new long[] {1111_000_000_000L, 2222_000_000_000L, 3333_000_000_000L};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    PreciseObjectTimeSeries<?, Float> ddts = ImmutableInstantObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 2);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_Map() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    Map<Instant, Float> map = new HashMap<>();
    map.put(Instant.ofEpochSecond(2222), 2.0f);
    map.put(Instant.ofEpochSecond(3333), 3.0f);
    map.put(Instant.ofEpochSecond(1111), 1.0f);
    bld.putAll(map);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(1111), Instant.ofEpochSecond(2222), Instant.ofEpochSecond(3333)};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_Map_empty() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    Map<Instant, Float> map = new HashMap<>();
    bld.put(Instant.ofEpochSecond(0), 0.5f).putAll(map);
    Instant[] outDates = new Instant[] {Instant.ofEpochSecond(0)};
    Float[] outValues = new Float[] {0.5f};
    assertEquals(ImmutableInstantObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_clearEmpty() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.clear();
    assertEquals(ImmutableInstantObjectTimeSeries.ofEmpty(), bld.build());
  }

  public void test_builder_clearSomething() {
    InstantObjectTimeSeriesBuilder<Float> bld = ImmutableInstantObjectTimeSeries.builder();
    bld.put(2222_000_000_000L, 1.0f).clear();
    assertEquals(ImmutableInstantObjectTimeSeries.ofEmpty(), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_toString() {
    InstantObjectTimeSeriesBuilder<BigDecimal> bld = ImmutableInstantObjectTimeSeries.builder();
    assertEquals("Builder[size=1]", bld.put(2222_000_000_000L, BigDecimal.valueOf(1.0)).toString());
  }

}
