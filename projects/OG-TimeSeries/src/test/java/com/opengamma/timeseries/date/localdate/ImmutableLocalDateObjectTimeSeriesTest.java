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
  public void test_subSeries_byLocalDates_single() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .put(LocalDate.of(2010, 5, 8), 8)
        .put(LocalDate.of(2010, 6, 8), 9)
        .build();
    final LocalDateObjectTimeSeries<Integer> singleMiddle = dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 3, 9));
    assertEquals(1, singleMiddle.size());
    assertEquals(LocalDate.of(2010, 3, 8), singleMiddle.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), singleMiddle.getValueAtIndex(0));
    
    final LocalDateObjectTimeSeries<Integer> singleStart = dts.subSeries(LocalDate.of(2010, 2, 8), LocalDate.of(2010, 2, 9));
    assertEquals(1, singleStart.size());
    assertEquals(LocalDate.of(2010, 2, 8), singleStart.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(2), singleStart.getValueAtIndex(0));
    
    final LocalDateObjectTimeSeries<Integer> singleEnd = dts.subSeries(LocalDate.of(2010, 6, 8), LocalDate.of(2010, 6, 9));
    assertEquals(1, singleEnd.size());
    assertEquals(LocalDate.of(2010, 6, 8), singleEnd.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(9), singleEnd.getValueAtIndex(0));
  }

  public void test_subSeries_byLocalDates_empty() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .build();
    final LocalDateObjectTimeSeries<Integer> sub = dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 3, 8));
    assertEquals(0, sub.size());
  }

  public void test_subSeries_byLocalDates_range() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .put(LocalDate.of(2010, 5, 8), 8)
        .put(LocalDate.of(2010, 6, 8), 9)
        .build();
    final LocalDateObjectTimeSeries<Integer> middle = dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 5, 9));
    assertEquals(3, middle.size());
    assertEquals(LocalDate.of(2010, 3, 8), middle.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), middle.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 4, 8), middle.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(5), middle.getValueAtIndex(1));
    assertEquals(LocalDate.of(2010, 5, 8), middle.getTimeAtIndex(2));
    assertEquals(Integer.valueOf(8), middle.getValueAtIndex(2));
    
    final LocalDateObjectTimeSeries<Integer> fromStart = dts.subSeries(LocalDate.of(2010, 2, 8), LocalDate.of(2010, 4, 9));
    assertEquals(3, fromStart.size());
    assertEquals(LocalDate.of(2010, 2, 8), fromStart.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(2), fromStart.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 3, 8), fromStart.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(3), fromStart.getValueAtIndex(1));
    assertEquals(LocalDate.of(2010, 4, 8), fromStart.getTimeAtIndex(2));
    assertEquals(Integer.valueOf(5), fromStart.getValueAtIndex(2));
    
    final LocalDateObjectTimeSeries<Integer> preStart = dts.subSeries(LocalDate.of(2010, 1, 8), LocalDate.of(2010, 3, 9));
    assertEquals(2, preStart.size());
    assertEquals(LocalDate.of(2010, 2, 8), preStart.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(2), preStart.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 3, 8), preStart.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(3), preStart.getValueAtIndex(1));
    
    final LocalDateObjectTimeSeries<Integer> postEnd = dts.subSeries(LocalDate.of(2010, 5, 8), LocalDate.of(2010, 12, 9));
    assertEquals(2, postEnd.size());
    assertEquals(LocalDate.of(2010, 5, 8), postEnd.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(8), postEnd.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 6, 8), postEnd.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(9), postEnd.getValueAtIndex(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_subSeries_byLocalDates_badRange1() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .put(LocalDate.of(2010, 5, 8), 8)
        .put(LocalDate.of(2010, 6, 8), 9)
        .build();
    dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 3, 7));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_subSeries_byLocalDates_badRange2() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .put(LocalDate.of(2010, 5, 8), 8)
        .put(LocalDate.of(2010, 6, 8), 9)
        .build();
    dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 2, 7));
  }

  //-------------------------------------------------------------------------
  public void test_subSeries_byLocalDatesAndBooleans_trueTrue() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .build();
    final LocalDateObjectTimeSeries<Integer> sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), true, LocalDate.of(2010, 3, 8), true);
    assertEquals(1, sub1.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub1.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub1.getValueAtIndex(0));
  }

  public void test_subSeries_byLocalDatesAndBooleans_trueFalse() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .build();
    final LocalDateObjectTimeSeries<Integer> sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), true, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub1.size());
    
    final LocalDateObjectTimeSeries<Integer> sub2 = dts.subSeries(LocalDate.of(2010, 3, 8), true, LocalDate.of(2010, 3, 9), false);
    assertEquals(1, sub2.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub2.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub2.getValueAtIndex(0));
    
    final LocalDateObjectTimeSeries<Integer> sub3 = dts.subSeries(LocalDate.of(2010, 3, 7), true, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub3.size());
  }

  public void test_subSeries_byLocalDatesAndBooleans_falseTrue() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .build();
    final LocalDateObjectTimeSeries<Integer> sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 8), true);
    assertEquals(0, sub1.size());
    
    final LocalDateObjectTimeSeries<Integer> sub2 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 9), true);
    assertEquals(0, sub2.size());
    
    final LocalDateObjectTimeSeries<Integer> sub3 = dts.subSeries(LocalDate.of(2010, 3, 7), false, LocalDate.of(2010, 3, 8), true);
    assertEquals(1, sub3.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub3.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub3.getValueAtIndex(0));
  }

  public void test_subSeries_byLocalDatesAndBooleans_falseFalse() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .build();
    final LocalDateObjectTimeSeries<Integer> sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub1.size());
    
    final LocalDateObjectTimeSeries<Integer> sub2 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 9), false);
    assertEquals(0, sub2.size());
    
    final LocalDateObjectTimeSeries<Integer> sub3 = dts.subSeries(LocalDate.of(2010, 3, 7), false, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub3.size());
    
    final LocalDateObjectTimeSeries<Integer> sub4 = dts.subSeries(LocalDate.of(2010, 3, 7), false, LocalDate.of(2010, 3, 9), false);
    assertEquals(1, sub4.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub4.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub4.getValueAtIndex(0));
  }

  //-------------------------------------------------------------------------
  public void test_subSeries_byLocalDatesAndBooleans_maxSimple() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.of(2010, 4, 8), 5)
        .build();
    final LocalDateObjectTimeSeries<Integer> sub1 = dts.subSeries(LocalDate.of(2010, 3, 9), true, LocalDate.MAX, false);
    assertEquals(1, sub1.size());
    assertEquals(LocalDate.of(2010, 4, 8), sub1.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(5), sub1.getValueAtIndex(0));
    
    final LocalDateObjectTimeSeries<Integer> sub2 = dts.subSeries(LocalDate.of(2010, 3, 9), true, LocalDate.MAX, true);
    assertEquals(1, sub2.size());
    assertEquals(LocalDate.of(2010, 4, 8), sub2.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(5), sub2.getValueAtIndex(0));
  }

  public void test_subSeries_byLocalDatesAndBooleans_maxComplex() {
    final LocalDateObjectTimeSeries<Integer> dts = ImmutableLocalDateObjectTimeSeries.<Integer>builder()
        .put(LocalDate.of(2010, 2, 8), 2)
        .put(LocalDate.of(2010, 3, 8), 3)
        .put(LocalDate.MAX, 5)
        .build();
    final LocalDateObjectTimeSeries<Integer> sub1 = dts.subSeries(LocalDate.of(2010, 3, 7), true, LocalDate.MAX, false);
    assertEquals(1, sub1.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub1.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub1.getValueAtIndex(0));
    
    final LocalDateObjectTimeSeries<Integer> sub2 = dts.subSeries(LocalDate.of(2010, 3, 7), true, LocalDate.MAX, true);
    assertEquals(2, sub2.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub2.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(3), sub2.getValueAtIndex(0));
    assertEquals(LocalDate.MAX, sub2.getTimeAtIndex(1));
    assertEquals(Integer.valueOf(5), sub2.getValueAtIndex(1));
    
    final LocalDateObjectTimeSeries<Integer> sub3 = dts.subSeries(LocalDate.MAX, true, LocalDate.MAX, true);
    assertEquals(1, sub3.size());
    assertEquals(LocalDate.MAX, sub3.getTimeAtIndex(0));
    assertEquals(Integer.valueOf(5), sub3.getValueAtIndex(0));
    
    final LocalDateObjectTimeSeries<Integer> sub4 = dts.subSeries(LocalDate.MAX, false, LocalDate.MAX, true);
    assertEquals(0, sub4.size());
    
    final LocalDateObjectTimeSeries<Integer> sub5 = dts.subSeries(LocalDate.MAX, true, LocalDate.MAX, false);
    assertEquals(0, sub5.size());
    
    final LocalDateObjectTimeSeries<Integer> sub6 = dts.subSeries(LocalDate.MAX, false, LocalDate.MAX, false);
    assertEquals(0, sub6.size());
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
