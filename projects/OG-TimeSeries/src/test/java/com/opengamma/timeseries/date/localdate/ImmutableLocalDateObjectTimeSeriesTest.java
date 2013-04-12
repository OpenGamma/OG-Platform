/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.date.DateObjectTimeSeries;

/**
 * Test.
 */
@Test(groups = "unit")
public class ImmutableLocalDateObjectTimeSeriesTest extends LocalDateObjectTimeSeriesTest {

  @Override
  protected LocalDateObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return ImmutableLocalDateObjectTimeSeries.ofEmpty();
  }

  protected LocalDateObjectTimeSeries<BigDecimal> createStandardTimeSeries() {
    return (LocalDateObjectTimeSeries<BigDecimal>) super.createStandardTimeSeries();
  }

  protected LocalDateObjectTimeSeries<BigDecimal> createStandardTimeSeries2() {
    return (LocalDateObjectTimeSeries<BigDecimal>) super.createStandardTimeSeries2();
  }

  @Override
  protected ObjectTimeSeries<LocalDate, BigDecimal> createTimeSeries(LocalDate[] times, BigDecimal[] values) {
    return ImmutableLocalDateObjectTimeSeries.of(times, values);
  }

  @Override
  protected LocalDateObjectTimeSeries<BigDecimal> createTimeSeries(List<LocalDate> times, List<BigDecimal> values) {
    return ImmutableLocalDateObjectTimeSeries.of(times, values);
  }

  @Override
  protected ObjectTimeSeries<LocalDate, BigDecimal> createTimeSeries(ObjectTimeSeries<LocalDate, BigDecimal> dts) {
    return ImmutableLocalDateObjectTimeSeries.from(dts);
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  public void test_of_LD_value() {
    LocalDateObjectTimeSeries<Float> ts= ImmutableLocalDateObjectTimeSeries.of(LocalDate.of(2012, 6, 30), 2.0f);
    assertEquals(ts.size(), 1);
    assertEquals(ts.getTimeAtIndex(0), LocalDate.of(2012, 6, 30));
    assertEquals(ts.getValueAtIndex(0), 2.0f);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_LD_double_null() {
    ImmutableLocalDateDoubleTimeSeries.of((LocalDate) null, 2.0);
  }

  //-------------------------------------------------------------------------
  public void test_of_LDArray_valueArray() {
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    Float[] inValues = new Float[] {2.0f, 3.0f};
    LocalDateObjectTimeSeries<Float> ts= ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), LocalDate.of(2012, 6, 30));
    assertEquals(ts.getValueAtIndex(0), 2.0f);
    assertEquals(ts.getTimeAtIndex(1), LocalDate.of(2012, 7, 1));
    assertEquals(ts.getValueAtIndex(1), 3.0f);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_LDArray_valueArray_wrongOrder() {
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_LDArray_valueArray_mismatchedArrays() {
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30)};
    Float[] inValues = new Float[] {2.0f, 3.0f};
    ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_LDArray_valueArray_nullDates() {
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableLocalDateObjectTimeSeries.of((LocalDate[]) null, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_LDArray_valueArray_nullValues() {
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    ImmutableLocalDateObjectTimeSeries.of(inDates, (Float[]) null);
  }

  //-------------------------------------------------------------------------
  public void test_of_intArray_valueArray() {
    int[] inDates = new int[] {20120630, 20120701};
    Float[] inValues = new Float[] {2.0f, 3.0f};
    LocalDateObjectTimeSeries<Float> ts= ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), LocalDate.of(2012, 6, 30));
    assertEquals(ts.getValueAtIndex(0), 2.0f);
    assertEquals(ts.getTimeAtIndex(1), LocalDate.of(2012, 7, 1));
    assertEquals(ts.getValueAtIndex(1), 3.0f);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_intArray_valueArray_wrongOrder() {
    int[] inDates = new int[] {20120630, 20120701, 20120601};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_intArray_valueArray_mismatchedArrays() {
    int[] inDates = new int[] {20120630};
    Float[] inValues = new Float[] {2.0f, 3.0f};
    ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_intArray_valueArray_nullDates() {
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    ImmutableLocalDateObjectTimeSeries.of((int[]) null, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_intArray_valueArray_nullValues() {
    int[] inDates = new int[] {20120630, 20120701};
    ImmutableLocalDateObjectTimeSeries.of(inDates, (Float[]) null);
  }

  //-------------------------------------------------------------------------
  public void test_toString() {
    LocalDateObjectTimeSeries<Float> ts= ImmutableLocalDateObjectTimeSeries.of(LocalDate.of(2012, 6, 30), 2.0f);
    assertEquals("ImmutableLocalDateObjectTimeSeries[(2012-06-30, 2.0)]", ts.toString());
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  public void test_builder_nothingAdded() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    assertEquals(ImmutableLocalDateObjectTimeSeries.ofEmpty(), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_iterator() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    LocalDateObjectEntryIterator<Float> it = bld.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(LocalDate.of(2012, 6, 1), 1.0f), it.next());
    assertEquals(LocalDate.of(2012, 6, 1), it.currentTime());
    assertEquals(20120601, it.currentTimeFast());
    assertEquals(1.0f, it.currentValue());
    assertEquals(LocalDate.of(2012, 6, 30), it.nextTime());
    assertEquals(LocalDate.of(2012, 7, 1), it.nextTime());
    assertEquals(false, it.hasNext());
  }

  public void test_iterator_empty() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    assertEquals(false, bld.iterator().hasNext());
  }

  public void test_iterator_removeFirst() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    LocalDateObjectEntryIterator<Float> it = bld.iterator();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    Float[] outValues = new Float[] {2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_iterator_removeMid() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    LocalDateObjectEntryIterator<Float> it = bld.iterator();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 7, 1)};
    Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_iterator_removeLast() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    LocalDateObjectEntryIterator<Float> it = bld.iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30)};
    Float[] outValues = new Float[] {1.0f, 2.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_put_LD() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 1), 1.0f);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_put_LD_alreadyThere() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0f).put(LocalDate.of(2012, 7, 1), 3.0f).put(LocalDate.of(2012, 6, 30), 1.0f);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_put_int() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(20120630, 2.0f).put(20120701, 3.0f).put(20120601, 1.0f);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_put_int_alreadyThere() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(20120630, 2.0f).put(20120701, 3.0f).put(20120630, 1.0f);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    Float[] outValues = new Float[] {1.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_put_int_big() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    int[] outDates = new int[600];
    Float[] outValues = new Float[600];
    for (int i = 0; i < 600; i++) {
      bld.put(20120630 + i, (float) i);
      outDates[i] = 20120630 + i;
      outValues[i] = (float) i;
    }
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_LD() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_builder_putAll_LD_mismatchedArrays() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_int() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    int[] inDates = new int[] {20120630, 20120701, 20120601};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_builder_putAll_int_mismatchedArrays() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    int[] inDates = new int[] {20120630, 20120701};
    Float[] inValues = new Float[] {2.0f, 3.0f, 1.0f};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_DDTS() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_DDTS_range_allNonEmptyBuilder() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.put(LocalDate.of(2012, 5, 1), 0.5f).putAll(ddts, 0, 3);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 5, 1), LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    Float[] outValues = new Float[] {0.5f, 1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_DDTS_range_fromStart() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 0, 1);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1)};
    Float[] outValues = new Float[] {1.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_DDTS_range_toEnd() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, 3);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    Float[] outValues = new Float[] {2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_DDTS_range_empty() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.put(LocalDate.of(2012, 5, 1), 0.5f).putAll(ddts, 1, 1);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 5, 1)};
    Float[] outValues = new Float[] {0.5f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startInvalidLow() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, -1, 3);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startInvalidHigh() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 4, 2);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_endInvalidLow() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, -1);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_endInvalidHigh() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 4);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startEndOrder() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    Float[] inValues = new Float[] {1.0f, 2.0f, 3.0f};
    DateObjectTimeSeries<?, Float> ddts = ImmutableLocalDateObjectTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 2);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_Map() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    Map<LocalDate, Float> map = new HashMap<>();
    map.put(LocalDate.of(2012, 6, 30), 2.0f);
    map.put(LocalDate.of(2012, 7, 1), 3.0f);
    map.put(LocalDate.of(2012, 6, 1), 1.0f);
    bld.putAll(map);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    Float[] outValues = new Float[] {1.0f, 2.0f, 3.0f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_Map_empty() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    Map<LocalDate, Float> map = new HashMap<>();
    bld.put(LocalDate.of(2012, 5, 1), 0.5f).putAll(map);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 5, 1)};
    Float[] outValues = new Float[] {0.5f};
    assertEquals(ImmutableLocalDateObjectTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_clearEmpty() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.clear();
    assertEquals(ImmutableLocalDateObjectTimeSeries.ofEmpty(), bld.build());
  }

  public void test_builder_clearSomething() {
    LocalDateObjectTimeSeriesBuilder<Float> bld = ImmutableLocalDateObjectTimeSeries.builder();
    bld.put(20120630, 1.0f).clear();
    assertEquals(ImmutableLocalDateObjectTimeSeries.ofEmpty(), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_toString() {
    LocalDateObjectTimeSeriesBuilder<BigDecimal> bld = ImmutableLocalDateObjectTimeSeries.builder();
    assertEquals("Builder[size=1]", bld.put(20120630, BigDecimal.valueOf(1.0f)).toString());
  }

}
