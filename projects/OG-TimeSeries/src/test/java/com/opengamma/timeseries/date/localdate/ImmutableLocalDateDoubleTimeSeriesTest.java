/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import static org.testng.AssertJUnit.assertEquals;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;

/**
 * Test.
 */
@Test(groups = "unit")
public class ImmutableLocalDateDoubleTimeSeriesTest extends LocalDateDoubleTimeSeriesTest {

  @Override
  protected LocalDateDoubleTimeSeries createEmptyTimeSeries() {
    return ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;
  }

  protected LocalDateDoubleTimeSeries createStandardTimeSeries() {
    return (LocalDateDoubleTimeSeries) super.createStandardTimeSeries();
  }

  protected LocalDateDoubleTimeSeries createStandardTimeSeries2() {
    return (LocalDateDoubleTimeSeries) super.createStandardTimeSeries2();
  }

  @Override
  protected LocalDateDoubleTimeSeries createTimeSeries(LocalDate[] times, double[] values) {
    return ImmutableLocalDateDoubleTimeSeries.of(times, values);
  }

  @Override
  protected LocalDateDoubleTimeSeries createTimeSeries(List<LocalDate> times, List<Double> values) {
    return ImmutableLocalDateDoubleTimeSeries.of(times, values);
  }

  @Override
  protected LocalDateDoubleTimeSeries createTimeSeries(DoubleTimeSeries<LocalDate> dts) {
    return ImmutableLocalDateDoubleTimeSeries.from(dts);
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  public void test_of_LD_double() {
    LocalDateDoubleTimeSeries ts= ImmutableLocalDateDoubleTimeSeries.of(LocalDate.of(2012, 6, 30), 2.0);
    assertEquals(ts.size(), 1);
    assertEquals(ts.getTimeAtIndex(0), LocalDate.of(2012, 6, 30));
    assertEquals(ts.getValueAtIndex(0), 2.0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_LD_double_null() {
    ImmutableLocalDateDoubleTimeSeries.of((LocalDate) null, 2.0);
  }

  //-------------------------------------------------------------------------
  public void test_of_LDArray_DoubleArray() {
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    Double[] inValues = new Double[] {2.0, 3.0};
    LocalDateDoubleTimeSeries ts= ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), LocalDate.of(2012, 6, 30));
    assertEquals(ts.getValueAtIndex(0), 2.0);
    assertEquals(ts.getTimeAtIndex(1), LocalDate.of(2012, 7, 1));
    assertEquals(ts.getValueAtIndex(1), 3.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_LDArray_DoubleArray_wrongOrder() {
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    Double[] inValues = new Double[] {2.0, 3.0, 1.0};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_LDArray_DoubleArray_mismatchedArrays() {
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30)};
    Double[] inValues = new Double[] {2.0, 3.0};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_LDArray_DoubleArray_nullDates() {
    Double[] inValues = new Double[] {2.0, 3.0, 1.0};
    ImmutableLocalDateDoubleTimeSeries.of((LocalDate[]) null, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_LDArray_DoubleArray_nullValues() {
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, (Double[]) null);
  }

  //-------------------------------------------------------------------------
  public void test_of_LDArray_doubleArray() {
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    double[] inValues = new double[] {2.0, 3.0};
    LocalDateDoubleTimeSeries ts= ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), LocalDate.of(2012, 6, 30));
    assertEquals(ts.getValueAtIndex(0), 2.0);
    assertEquals(ts.getTimeAtIndex(1), LocalDate.of(2012, 7, 1));
    assertEquals(ts.getValueAtIndex(1), 3.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_LDArray_doubleArray_wrongOrder() {
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_LDArray_doubleArray_mismatchedArrays() {
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30)};
    double[] inValues = new double[] {2.0, 3.0};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_LDArray_doubleArray_nullDates() {
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableLocalDateDoubleTimeSeries.of((LocalDate[]) null, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_LDArray_doubleArray_nullValues() {
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, (double[]) null);
  }

  //-------------------------------------------------------------------------
  public void test_of_intArray_doubleArray() {
    int[] inDates = new int[] {20120630, 20120701};
    double[] inValues = new double[] {2.0, 3.0};
    LocalDateDoubleTimeSeries ts= ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    assertEquals(ts.size(), 2);
    assertEquals(ts.getTimeAtIndex(0), LocalDate.of(2012, 6, 30));
    assertEquals(ts.getValueAtIndex(0), 2.0);
    assertEquals(ts.getTimeAtIndex(1), LocalDate.of(2012, 7, 1));
    assertEquals(ts.getValueAtIndex(1), 3.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_intArray_doubleArray_wrongOrder() {
    int[] inDates = new int[] {20120630, 20120701, 20120601};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_intArray_doubleArray_mismatchedArrays() {
    int[] inDates = new int[] {20120630};
    double[] inValues = new double[] {2.0, 3.0};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_intArray_doubleArray_nullDates() {
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    ImmutableLocalDateDoubleTimeSeries.of((int[]) null, inValues);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_of_intArray_doubleArray_nullValues() {
    int[] inDates = new int[] {20120630, 20120701};
    ImmutableLocalDateDoubleTimeSeries.of(inDates, (double[]) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_intersectionFirstValue_selectFirst() {
    final LocalDateDoubleTimeSeries dts = createStandardTimeSeries();
    final LocalDateDoubleTimeSeries dts2 = createStandardTimeSeries2();
    final LocalDateDoubleTimeSeries dts3 = ImmutableLocalDateDoubleTimeSeries.builder()
        .putAll(dts2).put(dts2.getEarliestTime(), -1.0).build();
    
    final LocalDateDoubleTimeSeries result1 = dts.intersectionFirstValue(dts3);
    assertEquals(3, result1.size());
    assertEquals(Double.valueOf(4.0), result1.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result1.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result1.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result1.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result1.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result1.getTimeAtIndex(2));
    
    final LocalDateDoubleTimeSeries result2 = dts3.intersectionFirstValue(dts);
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
    final LocalDateDoubleTimeSeries dts = createStandardTimeSeries();
    final LocalDateDoubleTimeSeries dts2 = createStandardTimeSeries2();
    final LocalDateDoubleTimeSeries dts3 = ImmutableLocalDateDoubleTimeSeries.builder()
        .putAll(dts2).put(dts2.getEarliestTime(), -1.0).build();
    
    final LocalDateDoubleTimeSeries result2 = dts.intersectionSecondValue(dts3);
    assertEquals(3, result2.size());
    assertEquals(Double.valueOf(-1.0), result2.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result2.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result2.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result2.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result2.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result2.getTimeAtIndex(2));
    
    final LocalDateDoubleTimeSeries result1 = dts3.intersectionSecondValue(dts);
    assertEquals(3, result1.size());
    assertEquals(Double.valueOf(4.0), result1.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result1.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result1.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result1.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result1.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result1.getTimeAtIndex(2));
  }

  //-------------------------------------------------------------------------
  public void test_subSeries_byLocalDates_single() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .put(LocalDate.of(2010, 5, 8), 8d)
        .put(LocalDate.of(2010, 6, 8), 9d)
        .build();
    final LocalDateDoubleTimeSeries singleMiddle = dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 3, 9));
    assertEquals(1, singleMiddle.size());
    assertEquals(LocalDate.of(2010, 3, 8), singleMiddle.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), singleMiddle.getValueAtIndex(0));
    
    final LocalDateDoubleTimeSeries singleStart = dts.subSeries(LocalDate.of(2010, 2, 8), LocalDate.of(2010, 2, 9));
    assertEquals(1, singleStart.size());
    assertEquals(LocalDate.of(2010, 2, 8), singleStart.getTimeAtIndex(0));
    assertEquals(Double.valueOf(2d), singleStart.getValueAtIndex(0));
    
    final LocalDateDoubleTimeSeries singleEnd = dts.subSeries(LocalDate.of(2010, 6, 8), LocalDate.of(2010, 6, 9));
    assertEquals(1, singleEnd.size());
    assertEquals(LocalDate.of(2010, 6, 8), singleEnd.getTimeAtIndex(0));
    assertEquals(Double.valueOf(9d), singleEnd.getValueAtIndex(0));
  }

  public void test_subSeries_byLocalDates_empty() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .build();
    final LocalDateDoubleTimeSeries sub = dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 3, 8));
    assertEquals(0, sub.size());
  }

  public void test_subSeries_byLocalDates_range() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .put(LocalDate.of(2010, 5, 8), 8d)
        .put(LocalDate.of(2010, 6, 8), 9d)
        .build();
    final LocalDateDoubleTimeSeries middle = dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 5, 9));
    assertEquals(3, middle.size());
    assertEquals(LocalDate.of(2010, 3, 8), middle.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), middle.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 4, 8), middle.getTimeAtIndex(1));
    assertEquals(Double.valueOf(5d), middle.getValueAtIndex(1));
    assertEquals(LocalDate.of(2010, 5, 8), middle.getTimeAtIndex(2));
    assertEquals(Double.valueOf(8d), middle.getValueAtIndex(2));
    
    final LocalDateDoubleTimeSeries fromStart = dts.subSeries(LocalDate.of(2010, 2, 8), LocalDate.of(2010, 4, 9));
    assertEquals(3, fromStart.size());
    assertEquals(LocalDate.of(2010, 2, 8), fromStart.getTimeAtIndex(0));
    assertEquals(Double.valueOf(2d), fromStart.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 3, 8), fromStart.getTimeAtIndex(1));
    assertEquals(Double.valueOf(3d), fromStart.getValueAtIndex(1));
    assertEquals(LocalDate.of(2010, 4, 8), fromStart.getTimeAtIndex(2));
    assertEquals(Double.valueOf(5d), fromStart.getValueAtIndex(2));
    
    final LocalDateDoubleTimeSeries preStart = dts.subSeries(LocalDate.of(2010, 1, 8), LocalDate.of(2010, 3, 9));
    assertEquals(2, preStart.size());
    assertEquals(LocalDate.of(2010, 2, 8), preStart.getTimeAtIndex(0));
    assertEquals(Double.valueOf(2d), preStart.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 3, 8), preStart.getTimeAtIndex(1));
    assertEquals(Double.valueOf(3d), preStart.getValueAtIndex(1));
    
    final LocalDateDoubleTimeSeries postEnd = dts.subSeries(LocalDate.of(2010, 5, 8), LocalDate.of(2010, 12, 9));
    assertEquals(2, postEnd.size());
    assertEquals(LocalDate.of(2010, 5, 8), postEnd.getTimeAtIndex(0));
    assertEquals(Double.valueOf(8d), postEnd.getValueAtIndex(0));
    assertEquals(LocalDate.of(2010, 6, 8), postEnd.getTimeAtIndex(1));
    assertEquals(Double.valueOf(9d), postEnd.getValueAtIndex(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_subSeries_byLocalDates_badRange1() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .put(LocalDate.of(2010, 5, 8), 8d)
        .put(LocalDate.of(2010, 6, 8), 9d)
        .build();
    dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 3, 7));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_subSeries_byLocalDates_badRange2() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .put(LocalDate.of(2010, 5, 8), 8d)
        .put(LocalDate.of(2010, 6, 8), 9d)
        .build();
    dts.subSeries(LocalDate.of(2010, 3, 8), LocalDate.of(2010, 2, 7));
  }

  //-------------------------------------------------------------------------
  public void test_subSeries_byLocalDatesAndBooleans_trueTrue() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .build();
    final LocalDateDoubleTimeSeries sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), true, LocalDate.of(2010, 3, 8), true);
    assertEquals(1, sub1.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub1.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub1.getValueAtIndex(0));
  }

  public void test_subSeries_byLocalDatesAndBooleans_trueFalse() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .build();
    final LocalDateDoubleTimeSeries sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), true, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub1.size());
    
    final LocalDateDoubleTimeSeries sub2 = dts.subSeries(LocalDate.of(2010, 3, 8), true, LocalDate.of(2010, 3, 9), false);
    assertEquals(1, sub2.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub2.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub2.getValueAtIndex(0));
    
    final LocalDateDoubleTimeSeries sub3 = dts.subSeries(LocalDate.of(2010, 3, 7), true, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub3.size());
  }

  public void test_subSeries_byLocalDatesAndBooleans_falseTrue() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .build();
    final LocalDateDoubleTimeSeries sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 8), true);
    assertEquals(0, sub1.size());
    
    final LocalDateDoubleTimeSeries sub2 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 9), true);
    assertEquals(0, sub2.size());
    
    final LocalDateDoubleTimeSeries sub3 = dts.subSeries(LocalDate.of(2010, 3, 7), false, LocalDate.of(2010, 3, 8), true);
    assertEquals(1, sub3.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub3.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub3.getValueAtIndex(0));
  }

  public void test_subSeries_byLocalDatesAndBooleans_falseFalse() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .build();
    final LocalDateDoubleTimeSeries sub1 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub1.size());
    
    final LocalDateDoubleTimeSeries sub2 = dts.subSeries(LocalDate.of(2010, 3, 8), false, LocalDate.of(2010, 3, 9), false);
    assertEquals(0, sub2.size());
    
    final LocalDateDoubleTimeSeries sub3 = dts.subSeries(LocalDate.of(2010, 3, 7), false, LocalDate.of(2010, 3, 8), false);
    assertEquals(0, sub3.size());
    
    final LocalDateDoubleTimeSeries sub4 = dts.subSeries(LocalDate.of(2010, 3, 7), false, LocalDate.of(2010, 3, 9), false);
    assertEquals(1, sub4.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub4.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub4.getValueAtIndex(0));
  }

  //-------------------------------------------------------------------------
  public void test_subSeries_byLocalDatesAndBooleans_maxSimple() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.of(2010, 4, 8), 5d)
        .build();
    final LocalDateDoubleTimeSeries sub1 = dts.subSeries(LocalDate.of(2010, 3, 9), true, LocalDate.MAX, false);
    assertEquals(1, sub1.size());
    assertEquals(LocalDate.of(2010, 4, 8), sub1.getTimeAtIndex(0));
    assertEquals(Double.valueOf(5d), sub1.getValueAtIndex(0));
    
    final LocalDateDoubleTimeSeries sub2 = dts.subSeries(LocalDate.of(2010, 3, 9), true, LocalDate.MAX, true);
    assertEquals(1, sub2.size());
    assertEquals(LocalDate.of(2010, 4, 8), sub2.getTimeAtIndex(0));
    assertEquals(Double.valueOf(5d), sub2.getValueAtIndex(0));
  }

  public void test_subSeries_byLocalDatesAndBooleans_maxComplex() {
    final LocalDateDoubleTimeSeries dts = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2010, 2, 8), 2d)
        .put(LocalDate.of(2010, 3, 8), 3d)
        .put(LocalDate.MAX, 5d)
        .build();
    final LocalDateDoubleTimeSeries sub1 = dts.subSeries(LocalDate.of(2010, 3, 7), true, LocalDate.MAX, false);
    assertEquals(1, sub1.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub1.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub1.getValueAtIndex(0));
    
    final LocalDateDoubleTimeSeries sub2 = dts.subSeries(LocalDate.of(2010, 3, 7), true, LocalDate.MAX, true);
    assertEquals(2, sub2.size());
    assertEquals(LocalDate.of(2010, 3, 8), sub2.getTimeAtIndex(0));
    assertEquals(Double.valueOf(3d), sub2.getValueAtIndex(0));
    assertEquals(LocalDate.MAX, sub2.getTimeAtIndex(1));
    assertEquals(Double.valueOf(5d), sub2.getValueAtIndex(1));
    
    final LocalDateDoubleTimeSeries sub3 = dts.subSeries(LocalDate.MAX, true, LocalDate.MAX, true);
    assertEquals(1, sub3.size());
    assertEquals(LocalDate.MAX, sub3.getTimeAtIndex(0));
    assertEquals(Double.valueOf(5d), sub3.getValueAtIndex(0));
    
    final LocalDateDoubleTimeSeries sub4 = dts.subSeries(LocalDate.MAX, false, LocalDate.MAX, true);
    assertEquals(0, sub4.size());
    
    final LocalDateDoubleTimeSeries sub5 = dts.subSeries(LocalDate.MAX, true, LocalDate.MAX, false);
    assertEquals(0, sub5.size());
    
    final LocalDateDoubleTimeSeries sub6 = dts.subSeries(LocalDate.MAX, false, LocalDate.MAX, false);
    assertEquals(0, sub6.size());
  }

  //-------------------------------------------------------------------------
  public void test_toString() {
    LocalDateDoubleTimeSeries ts= ImmutableLocalDateDoubleTimeSeries.of(LocalDate.of(2012, 6, 30), 2.0);
    assertEquals("ImmutableLocalDateDoubleTimeSeries[(2012-06-30, 2.0)]", ts.toString());
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  public void test_builder_nothingAdded() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    assertEquals(0, bld.size());
    assertEquals(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES, bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_iterator() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    LocalDateDoubleEntryIterator it = bld.iterator();
    assertEquals(true, it.hasNext());
    assertEquals(new AbstractMap.SimpleImmutableEntry<>(LocalDate.of(2012, 6, 1), 1.0d), it.next());
    assertEquals(LocalDate.of(2012, 6, 1), it.currentTime());
    assertEquals(20120601, it.currentTimeFast());
    assertEquals(1.0d, it.currentValue());
    assertEquals(1.0d, it.currentValueFast());
    assertEquals(LocalDate.of(2012, 6, 30), it.nextTime());
    assertEquals(LocalDate.of(2012, 7, 1), it.nextTime());
    assertEquals(false, it.hasNext());
  }

  public void test_iterator_empty() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    assertEquals(false, bld.iterator().hasNext());
  }

  public void test_iterator_removeFirst() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    LocalDateDoubleEntryIterator it = bld.iterator();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    double[] outValues = new double[] {2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_iterator_removeMid() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    LocalDateDoubleEntryIterator it = bld.iterator();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 7, 1)};
    double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_iterator_removeLast() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    LocalDateDoubleEntryIterator it = bld.iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertEquals(2, bld.size());
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30)};
    double[] outValues = new double[] {1.0, 2.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_put_LD() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 1), 1.0);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_put_LD_alreadyThere() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(LocalDate.of(2012, 6, 30), 2.0).put(LocalDate.of(2012, 7, 1), 3.0).put(LocalDate.of(2012, 6, 30), 1.0);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_put_int() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(20120630, 2.0).put(20120701, 3.0).put(20120601, 1.0);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_put_int_alreadyThere() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(20120630, 2.0).put(20120701, 3.0).put(20120630, 1.0);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    double[] outValues = new double[] {1.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_put_int_big() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    int[] outDates = new int[600];
    double[] outValues = new double[600];
    LocalDate date = LocalDate.of(2012, 6, 30);
    for (int i = 0; i < 600; i++) {
      bld.put(date, i);
      outDates[i] = LocalDateToIntConverter.convertToInt(date);
      outValues[i] = i;
      date = date.plusDays(1);
    }
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_LD() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1), LocalDate.of(2012, 6, 1)};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_builder_putAll_LD_mismatchedArrays() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    LocalDate[] inDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_int() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    int[] inDates = new int[] {20120630, 20120701, 20120601};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_builder_putAll_int_mismatchedArrays() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    int[] inDates = new int[] {20120630, 20120701};
    double[] inValues = new double[] {2.0, 3.0, 1.0};
    bld.putAll(inDates, inValues);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_DDTS() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_DDTS_range_allNonEmptyBuilder() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.put(LocalDate.of(2012, 5, 1), 0.5).putAll(ddts, 0, 3);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 5, 1), LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    double[] outValues = new double[] {0.5, 1.0, 2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_DDTS_range_fromStart() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 0, 1);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1)};
    double[] outValues = new double[] {1.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_DDTS_range_toEnd() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, 3);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    double[] outValues = new double[] {2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_DDTS_range_empty() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.put(LocalDate.of(2012, 5, 1), 0.5).putAll(ddts, 1, 1);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 5, 1)};
    double[] outValues = new double[] {0.5};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startInvalidLow() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, -1, 3);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startInvalidHigh() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 4, 2);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_endInvalidLow() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 1, -1);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_endInvalidHigh() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 4);
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void test_builder_putAll_DDTS_range_startEndOrder() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    int[] inDates = new int[] {20120601, 20120630, 20120701};
    double[] inValues = new double[] {1.0, 2.0, 3.0};
    DateDoubleTimeSeries<?> ddts = ImmutableLocalDateDoubleTimeSeries.of(inDates, inValues);
    bld.putAll(ddts, 3, 2);
  }

  //-------------------------------------------------------------------------
  public void test_builder_putAll_Map() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    Map<LocalDate, Double> map = new HashMap<>();
    map.put(LocalDate.of(2012, 6, 30), 2.0d);
    map.put(LocalDate.of(2012, 7, 1), 3.0d);
    map.put(LocalDate.of(2012, 6, 1), 1.0d);
    bld.putAll(map);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 6, 1), LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    double[] outValues = new double[] {1.0, 2.0, 3.0};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  public void test_builder_putAll_Map_empty() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    Map<LocalDate, Double> map = new HashMap<>();
    bld.put(LocalDate.of(2012, 5, 1), 0.5).putAll(map);
    LocalDate[] outDates = new LocalDate[] {LocalDate.of(2012, 5, 1)};
    double[] outValues = new double[] {0.5};
    assertEquals(ImmutableLocalDateDoubleTimeSeries.of(outDates, outValues), bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_clearEmpty() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.clear();
    assertEquals(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES, bld.build());
  }

  public void test_builder_clearSomething() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    bld.put(20120630, 1.0).clear();
    assertEquals(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES, bld.build());
  }

  //-------------------------------------------------------------------------
  public void test_builder_toString() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    assertEquals("Builder[size=1]", bld.put(20120630, 1.0).toString());
  }

  public void test_withValues() {
    int[] times = new int[] {10101, 10102, 10103, 10104};
    double[] values = new double[] {0d, 1d, 2d, 3d};
    LocalDateDoubleTimeSeries series = ImmutableLocalDateDoubleTimeSeries.of(times, values);
    assertEquals(2d, series.getValueAtIndex(2));
    values[2] = 5;
    LocalDateDoubleTimeSeries series2 = series.withValues(values);
    assertEquals(5d, series2.getValueAtIndex(2));
  }

}
