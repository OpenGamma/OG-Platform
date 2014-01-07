/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.testng.annotations.Test;

/**
 * Abstract test class for {@code DoubleTimeSeries}.
 * 
 * @param <T>  the time type
 */
@Test(groups = "unit")
public abstract class DoubleTimeSeriesTest<T> {

  protected abstract DoubleTimeSeries<T> createEmptyTimeSeries();

  protected abstract DoubleTimeSeries<T> createTimeSeries(T[] times, double[] values);

  protected abstract DoubleTimeSeries<T> createTimeSeries(List<T> times, List<Double> values);

  protected abstract DoubleTimeSeries<T> createTimeSeries(DoubleTimeSeries<T> dts);

  protected abstract T[] emptyTimes();

  protected abstract T[] testTimes();

  protected abstract T[] testTimes2();

  @Test
  public void test_arrayConstructor() {
    DoubleTimeSeries<T> dts = createTimeSeries(emptyTimes(), new double[0]);
    assertEquals(0, dts.size());
    T[] times = testTimes();
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
    dts = createTimeSeries(times, values);
    assertEquals(6, dts.size());
    Iterator<Double> valuesIter = dts.valuesIterator();
    for (double i = 1.0; i <= 6.0; i += 1.0) {
      assertTrue(TimeSeriesUtils.closeEquals(i, valuesIter.next()));
    }
  }

  @Test
  public void test_listConstructor() {
    DoubleTimeSeries<T> dts = createTimeSeries(new ArrayList<T>(), new ArrayList<Double>());
    assertEquals(0, dts.size());
    T[] times = testTimes();
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
    List<T> timesList = new ArrayList<T>();
    List<Double> valuesList = new ArrayList<Double>();
    for (int i = 0; i < times.length; i++) {
      timesList.add(times[i]);
      valuesList.add(values[i]);
    }
    dts = createTimeSeries(timesList, valuesList);
    assertEquals(6, dts.size());
    Iterator<Double> valuesIter = dts.valuesIterator();
    for (double i = 1.0; i <= 6.0; i += 1.0) {
      assertTrue(TimeSeriesUtils.closeEquals(i, valuesIter.next()));
    }
  }

  @Test
  public void test_timeSeriesConstructor() {
    DoubleTimeSeries<T> dts = createEmptyTimeSeries();
    DoubleTimeSeries<T> dts2 = createTimeSeries(dts);
    assertEquals(0, dts2.size());
    T[] times = testTimes();
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
    dts = createTimeSeries(times, values);
    dts2 = createTimeSeries(dts);
    assertEquals(6, dts2.size());
    Iterator<Double> valuesIter = dts2.valuesIterator();
    for (double i = 1.0; i <= 6.0; i += 1.0) {
      assertTrue(TimeSeriesUtils.closeEquals(i, valuesIter.next()));
    }
  }

  protected DoubleTimeSeries<T> createStandardTimeSeries() {
    T[] times = testTimes();
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
    return createTimeSeries(times, values);
  }

  protected DoubleTimeSeries<T> createStandardTimeSeries2() {
    T[] times = testTimes2();
    double[] values = {4.0, 5.0, 6.0, 7.0, 8.0, 9.0 };
    return createTimeSeries(times, values);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_head() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    DoubleTimeSeries<T> head5 = dts.head(5);
    Iterator<Entry<T, Double>> iterator = head5.iterator();
    for (int i = 0; i < 5; i++) {
      Entry<T, Double> entry = iterator.next();
      assertEquals(testTimes()[i], entry.getKey());
      assertEquals(Double.valueOf(i + 1), entry.getValue());
    }
    assertEquals(dts.head(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().head(0), createEmptyTimeSeries());
  }

  @Test
  public void test_tail() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    DoubleTimeSeries<T> tail5 = dts.tail(5);
    Iterator<Entry<T, Double>> iterator = tail5.iterator();
    for (int i = 1; i < 6; i++) {
      Entry<T, Double> entry = iterator.next();
      assertEquals(testTimes()[i], entry.getKey());
      assertEquals(Double.valueOf(i + 1), entry.getValue());
    }
    assertEquals(dts.tail(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().tail(0), createEmptyTimeSeries());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_size() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    assertEquals(6, dts.size());
    DoubleTimeSeries<T> emptyTS = createEmptyTimeSeries();
    assertEquals(0, emptyTS.size());
  }

  @Test
  public void test_isEmpty() {
    DoubleTimeSeries<T> empty = createEmptyTimeSeries();
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    assertTrue(empty.isEmpty());
    assertFalse(dts.isEmpty());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_containsTime() {
    DoubleTimeSeries<T> emptyTS = createEmptyTimeSeries();
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    T[] testDates = testTimes();
    for (int i = 0; i < 6; i++) {
      assertEquals(true, dts.containsTime(testDates[i]));
      assertEquals(false, emptyTS.containsTime(testDates[i]));
    }
  }

  @Test
  public void test_getValue() {
    DoubleTimeSeries<T> emptyTS = createEmptyTimeSeries();
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    T[] testDates = testTimes();
    for (int i = 0; i < 6; i++) {
      Double val = dts.getValue(testDates[i]);
      TimeSeriesUtils.closeEquals(val, i + 1);
      assertEquals(null, emptyTS.getValue(testDates[i]));
    }
  }

  @Test
  public void test_getTimeAtIndex() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    T[] testDates = testTimes();
    for (int i = 0; i < 6; i++) {
      T val = dts.getTimeAtIndex(i);
      assertEquals(testDates[i], val);
    }
    try {
      dts.getTimeAtIndex(-1);
      fail();
    } catch (IndexOutOfBoundsException ex) {
      // expected
    }
    try {
      dts.getTimeAtIndex(6);
      fail();
    } catch (IndexOutOfBoundsException ex) {
      // expected
    }
  }

  @Test
  public void test_getValueAtIndex() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    for (int i = 0; i < 6; i++) {
      Double val = dts.getValueAtIndex(i);
      TimeSeriesUtils.closeEquals(val, i + 1);
    }
    try {
      dts.getValueAtIndex(-1);
      fail();
    } catch (IndexOutOfBoundsException ex) {
      // expected
    }
    try {
      dts.getValueAtIndex(6);
      fail();
    } catch (IndexOutOfBoundsException ex) {
      // expected
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getLatestTime() {
    DoubleTimeSeries<T> empty = createEmptyTimeSeries();
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    T[] testDates = testTimes();
    assertEquals(testDates[5], dts.getLatestTime());
    try {
      empty.getLatestTime();
      fail();
    } catch (NoSuchElementException ex) {
      // expected
    }
  }

  @Test
  public void test_getLatestValue() {
    DoubleTimeSeries<T> empty = createEmptyTimeSeries();
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    assertTrue(TimeSeriesUtils.closeEquals(6.0d, dts.getLatestValue()));
    try {
      empty.getLatestValue();
      fail();
    } catch (NoSuchElementException ex) {
      // expected
    }
  }

  @Test
  public void test_getEarliestTime() {
    DoubleTimeSeries<T> empty = createEmptyTimeSeries();
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    T[] testDates = testTimes();
    assertEquals(testDates[0], dts.getEarliestTime());
    try {
      empty.getEarliestTime();
      fail();
    } catch (NoSuchElementException ex) {
      // expected
    }
  }

  @Test
  public void test_getEarliestValue() {
    DoubleTimeSeries<T> empty = createEmptyTimeSeries();
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    assertTrue(TimeSeriesUtils.closeEquals(1d, dts.getEarliestValue()));
    try {
      empty.getEarliestValue();
      fail();
    } catch (NoSuchElementException ex) {
      // expected
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_timesIterator() {
    Iterator<T> emptyTimesIter = createEmptyTimeSeries().timesIterator();
    Iterator<T> dtsTimesIter = createStandardTimeSeries().timesIterator();
    T[] testDates = testTimes();
    for (int i = 0; i < 6; i++) {
      assertTrue(dtsTimesIter.hasNext());
      T time = dtsTimesIter.next();
      assertEquals(testDates[i], time);
    }
    try {
      dtsTimesIter.next();
    } catch (NoSuchElementException ex) {
      assertFalse(emptyTimesIter.hasNext());
      try {
        emptyTimesIter.next();
        fail();
      } catch (NoSuchElementException ex2) {
        // expected
      }
    }
  }

  @Test
  public void test_valuesIterator() {
    Iterator<Double> emptyValuesIter = createEmptyTimeSeries().valuesIterator();
    Iterator<Double> dtsValuesIter = createStandardTimeSeries().valuesIterator();
    for (double i = 1; i <= 6.0; i += 1.0d) {
      assertTrue(dtsValuesIter.hasNext());
      Double val = dtsValuesIter.next();
      TimeSeriesUtils.closeEquals(val, i);
    }
    try {
      dtsValuesIter.next();
    } catch (NoSuchElementException nsee) {
      assertFalse(emptyValuesIter.hasNext());
      try {
        emptyValuesIter.next();
        fail();
      } catch (NoSuchElementException ex2) {
        // expected
      }
    }
  }

  @Test
  public void test_iterator() {
    Iterator<Entry<T, Double>> emptyIter = createEmptyTimeSeries().iterator();
    Iterator<Entry<T, Double>> dtsIter = createStandardTimeSeries().iterator();
    T[] testDates = testTimes();
    for (int i = 0; i < 6; i++) {
      assertTrue(dtsIter.hasNext());
      Entry<T, Double> entry = dtsIter.next();
      T time = entry.getKey();
      TimeSeriesUtils.closeEquals(entry.getValue(), (double) i + 1);
      assertEquals(testDates[i], time);
    }
    try {
      dtsIter.next();
    } catch (NoSuchElementException ex) {
      assertFalse(emptyIter.hasNext());
      try {
        emptyIter.next();
        fail();
      } catch (NoSuchElementException ex2) {
        // expected
      }
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_times() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    T[] testDates = testTimes();
    assertEquals(6, dts.times().size());
    for (int i = 0; i < 6; i++) {
      assertEquals(testDates[i], dts.times().get(i));
    }
  }

  @Test
  public void test_timesArray() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    T[] testDates = testTimes();
    assertEquals(6, dts.timesArray().length);
    for (int i = 0; i < 6; i++) {
      assertEquals(testDates[i], dts.timesArray()[i]);
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_values() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    assertEquals(6, dts.values().size());
    for (int i = 0; i < 6; i++) {
      assertEquals(i + 1.0d, dts.values().get(i).doubleValue(), 0.01d);
    }
  }

  @Test
  public void test_valuesArray() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    assertEquals(6, dts.valuesArray().length);
    for (int i = 0; i < 6; i++) {
      assertEquals(i + 1.0d, dts.valuesArray()[i].doubleValue(), 0.01d);
    }
  }

  //-------------------------------------------------------------------------
  @Test
  @SuppressWarnings("cast")
  public void test_subSeries() {
    DoubleTimeSeries<T> emptyTS = createEmptyTimeSeries();
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    T[] testDates = testTimes();
    DoubleTimeSeries<T> threeToFive = dts.subSeries(testDates[3], testDates[5]);
    assertEquals(2, threeToFive.size());
    Iterator<Entry<T, Double>> iterator = threeToFive.iterator();
    for (int i = 3; i < 5; i++) {
      Entry<T, Double> item = iterator.next();
      assertEquals(testDates[i], item.getKey());
      assertTrue(TimeSeriesUtils.closeEquals((double) i + 1, item.getValue()));
    }
    assertEquals(4, dts.subSeries(testDates[0], testDates[4]).size());
    assertEquals(5, dts.subSeries(testDates[0], true, testDates[4], true).size());
    assertEquals(4, dts.subSeries(testDates[0], true, testDates[4], false).size());
    assertEquals(1, dts.subSeries(testDates[4], testDates[5]).size());
    assertEquals(1, dts.subSeries(testDates[4], false, testDates[5], true).size());
    assertEquals(0, dts.subSeries(testDates[5], true, testDates[5], false).size());
    assertEquals(0, dts.subSeries(testDates[5], false, testDates[5], false).size());
    assertEquals(emptyTS, emptyTS.subSeries(testDates[1], testDates[1]));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equals() {
    assertEquals(createStandardTimeSeries(), createStandardTimeSeries());
    assertFalse(createStandardTimeSeries().equals(createEmptyTimeSeries()));
    assertFalse(createEmptyTimeSeries().equals(createStandardTimeSeries()));
    assertEquals(createEmptyTimeSeries(), createEmptyTimeSeries());
  }

  @Test
  public void test_hashCode() {
    assertEquals(createStandardTimeSeries().hashCode(), createStandardTimeSeries().hashCode());
    assertEquals(createEmptyTimeSeries().hashCode(), createEmptyTimeSeries().hashCode());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_add_unionAdd() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    DoubleTimeSeries<T> dts2 = createStandardTimeSeries2();
    DoubleTimeSeries<T> ets = createEmptyTimeSeries();
    assertEquals(ets, dts.add(ets));
    assertEquals(ets, ets.add(dts));
    assertEquals(dts, dts.unionAdd(ets));
    assertEquals(dts, ets.unionAdd(dts));
    
    DoubleTimeSeries<T> result = dts.add(dts2);
    assertEquals(3, result.size());
    assertEquals(Double.valueOf(8.0), result.getValueAtIndex(0));
    assertEquals(Double.valueOf(10.0), result.getValueAtIndex(1));
    assertEquals(Double.valueOf(12.0), result.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result.getTimeAtIndex(2));
    
    DoubleTimeSeries<T> unionResult = dts.unionAdd(dts2);
    assertEquals(9, unionResult.size());
    assertEquals(Double.valueOf(1.0), unionResult.getValueAtIndex(0));
    assertEquals(Double.valueOf(2.0), unionResult.getValueAtIndex(1));
    assertEquals(Double.valueOf(3.0), unionResult.getValueAtIndex(2));
    assertEquals(Double.valueOf(8.0), unionResult.getValueAtIndex(3));
    assertEquals(Double.valueOf(10.0), unionResult.getValueAtIndex(4));
    assertEquals(Double.valueOf(12.0), unionResult.getValueAtIndex(5));
    assertEquals(Double.valueOf(7.0), unionResult.getValueAtIndex(6));
    assertEquals(Double.valueOf(8.0), unionResult.getValueAtIndex(7));
    assertEquals(Double.valueOf(9.0), unionResult.getValueAtIndex(8));
    assertEquals(dts.getTimeAtIndex(0), unionResult.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(1), unionResult.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(2), unionResult.getTimeAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), unionResult.getTimeAtIndex(3));
    assertEquals(dts.getTimeAtIndex(4), unionResult.getTimeAtIndex(4));
    assertEquals(dts.getTimeAtIndex(5), unionResult.getTimeAtIndex(5));
    assertEquals(dts2.getTimeAtIndex(3), unionResult.getTimeAtIndex(6));
    assertEquals(dts2.getTimeAtIndex(4), unionResult.getTimeAtIndex(7));
    assertEquals(dts2.getTimeAtIndex(5), unionResult.getTimeAtIndex(8));
  }

  @Test
  public void test_subtract_unionSubtract() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    DoubleTimeSeries<T> dts2 = createStandardTimeSeries2();
    DoubleTimeSeries<T> ets = createEmptyTimeSeries();
    assertEquals(ets, dts.subtract(ets));
    assertEquals(ets, ets.subtract(dts));
    assertEquals(dts, dts.unionSubtract(ets));
    assertEquals(dts, ets.unionSubtract(dts));
    
    DoubleTimeSeries<T> result = dts.subtract(dts2);
    assertEquals(3, result.size());
    assertEquals(Double.valueOf(0.0), result.getValueAtIndex(0));
    assertEquals(Double.valueOf(0.0), result.getValueAtIndex(1));
    assertEquals(Double.valueOf(0.0), result.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result.getTimeAtIndex(2));
    
    DoubleTimeSeries<T> unionResult = dts.unionSubtract(dts2);
    assertEquals(9, unionResult.size());
    assertEquals(Double.valueOf(1.0), unionResult.getValueAtIndex(0));
    assertEquals(Double.valueOf(2.0), unionResult.getValueAtIndex(1));
    assertEquals(Double.valueOf(3.0), unionResult.getValueAtIndex(2));
    assertEquals(Double.valueOf(0.0), unionResult.getValueAtIndex(3));
    assertEquals(Double.valueOf(0.0), unionResult.getValueAtIndex(4));
    assertEquals(Double.valueOf(0.0), unionResult.getValueAtIndex(5));
    assertEquals(Double.valueOf(7.0), unionResult.getValueAtIndex(6));
    assertEquals(Double.valueOf(8.0), unionResult.getValueAtIndex(7));
    assertEquals(Double.valueOf(9.0), unionResult.getValueAtIndex(8));
    assertEquals(dts.getTimeAtIndex(0), unionResult.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(1), unionResult.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(2), unionResult.getTimeAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), unionResult.getTimeAtIndex(3));
    assertEquals(dts.getTimeAtIndex(4), unionResult.getTimeAtIndex(4));
    assertEquals(dts.getTimeAtIndex(5), unionResult.getTimeAtIndex(5));
    assertEquals(dts2.getTimeAtIndex(3), unionResult.getTimeAtIndex(6));
    assertEquals(dts2.getTimeAtIndex(4), unionResult.getTimeAtIndex(7));
    assertEquals(dts2.getTimeAtIndex(5), unionResult.getTimeAtIndex(8));
  }

  @Test
  public void test_multiply_unionMultiply() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    DoubleTimeSeries<T> dts2 = createStandardTimeSeries2();
    DoubleTimeSeries<T> ets = createEmptyTimeSeries();
    assertEquals(ets, dts.multiply(ets));
    assertEquals(ets, ets.multiply(dts));
    assertEquals(dts, dts.unionMultiply(ets));
    assertEquals(dts, ets.unionMultiply(dts));
    
    DoubleTimeSeries<T> result = dts.multiply(dts2);
    assertEquals(3, result.size());
    assertEquals(Double.valueOf(16.0), result.getValueAtIndex(0));
    assertEquals(Double.valueOf(25.0), result.getValueAtIndex(1));
    assertEquals(Double.valueOf(36.0), result.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result.getTimeAtIndex(2));
    
    DoubleTimeSeries<T> unionResult = dts.unionMultiply(dts2);
    assertEquals(9, unionResult.size());
    assertEquals(Double.valueOf(1.0), unionResult.getValueAtIndex(0));
    assertEquals(Double.valueOf(2.0), unionResult.getValueAtIndex(1));
    assertEquals(Double.valueOf(3.0), unionResult.getValueAtIndex(2));
    assertEquals(Double.valueOf(16.0), unionResult.getValueAtIndex(3));
    assertEquals(Double.valueOf(25.0), unionResult.getValueAtIndex(4));
    assertEquals(Double.valueOf(36.0), unionResult.getValueAtIndex(5));
    assertEquals(Double.valueOf(7.0), unionResult.getValueAtIndex(6));
    assertEquals(Double.valueOf(8.0), unionResult.getValueAtIndex(7));
    assertEquals(Double.valueOf(9.0), unionResult.getValueAtIndex(8));
    assertEquals(dts.getTimeAtIndex(0), unionResult.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(1), unionResult.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(2), unionResult.getTimeAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), unionResult.getTimeAtIndex(3));
    assertEquals(dts.getTimeAtIndex(4), unionResult.getTimeAtIndex(4));
    assertEquals(dts.getTimeAtIndex(5), unionResult.getTimeAtIndex(5));
    assertEquals(dts2.getTimeAtIndex(3), unionResult.getTimeAtIndex(6));
    assertEquals(dts2.getTimeAtIndex(4), unionResult.getTimeAtIndex(7));
    assertEquals(dts2.getTimeAtIndex(5), unionResult.getTimeAtIndex(8));
  }

  @Test
  public void test_divide_unionDivide() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    DoubleTimeSeries<T> dts2 = createStandardTimeSeries2();
    DoubleTimeSeries<T> ets = createEmptyTimeSeries();
    assertEquals(ets, dts.divide(ets));
    assertEquals(ets, ets.divide(dts));
    assertEquals(dts, dts.unionDivide(ets));
    assertEquals(dts, ets.unionDivide(dts));
    
    DoubleTimeSeries<T> result = dts.divide(dts2);
    assertEquals(3, result.size());
    assertEquals(Double.valueOf(1.0), result.getValueAtIndex(0));
    assertEquals(Double.valueOf(1.0), result.getValueAtIndex(1));
    assertEquals(Double.valueOf(1.0), result.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result.getTimeAtIndex(2));
    
    DoubleTimeSeries<T> unionResult = dts.unionDivide(dts2);
    assertEquals(9, unionResult.size());
    assertEquals(Double.valueOf(1.0), unionResult.getValueAtIndex(0));
    assertEquals(Double.valueOf(2.0), unionResult.getValueAtIndex(1));
    assertEquals(Double.valueOf(3.0), unionResult.getValueAtIndex(2));
    assertEquals(Double.valueOf(1.0), unionResult.getValueAtIndex(3));
    assertEquals(Double.valueOf(1.0), unionResult.getValueAtIndex(4));
    assertEquals(Double.valueOf(1.0), unionResult.getValueAtIndex(5));
    assertEquals(Double.valueOf(7.0), unionResult.getValueAtIndex(6));
    assertEquals(Double.valueOf(8.0), unionResult.getValueAtIndex(7));
    assertEquals(Double.valueOf(9.0), unionResult.getValueAtIndex(8));
    assertEquals(dts.getTimeAtIndex(0), unionResult.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(1), unionResult.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(2), unionResult.getTimeAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), unionResult.getTimeAtIndex(3));
    assertEquals(dts.getTimeAtIndex(4), unionResult.getTimeAtIndex(4));
    assertEquals(dts.getTimeAtIndex(5), unionResult.getTimeAtIndex(5));
    assertEquals(dts2.getTimeAtIndex(3), unionResult.getTimeAtIndex(6));
    assertEquals(dts2.getTimeAtIndex(4), unionResult.getTimeAtIndex(7));
    assertEquals(dts2.getTimeAtIndex(5), unionResult.getTimeAtIndex(8));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_minValue() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    DoubleTimeSeries<T> ets = createEmptyTimeSeries();
    assertEquals(1.0, dts.minValue(), 0.01d);
    assertEquals(6.0, dts.maxValue(), 0.01d);
    try {
      ets.minValue();
      fail("Should have failed");
    } catch (NoSuchElementException ex) {
      // expected
    }
    try {
      ets.maxValue();
      fail("Should have failed");
    } catch (NoSuchElementException ex) {
      // expected
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_noIntersectionOperation() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    DoubleTimeSeries<T> dts2 = createStandardTimeSeries2();
    DoubleTimeSeries<T> ets = createEmptyTimeSeries();
    assertEquals(dts, ets.noIntersectionOperation(dts));
    assertEquals(dts, dts.noIntersectionOperation(ets));
    try {
      dts.noIntersectionOperation(dts2);
      fail("Should have failed");
    } catch (IllegalStateException ex) {
      //do nothing - expected exception because the two timeseries have overlapping dates which will require intersection operation
    }
    DoubleTimeSeries<T> dts3 = dts2.subSeries(dts.getLatestTime(), false, dts2.getLatestTime(), false);
    DoubleTimeSeries<T> noIntersecOp = dts.noIntersectionOperation(dts3);
    assertEquals(dts.getValueAtIndex(0), noIntersecOp.getValueAtIndex(0));
    assertEquals(dts.getValueAtIndex(1), noIntersecOp.getValueAtIndex(1));
    assertEquals(dts.getValueAtIndex(2), noIntersecOp.getValueAtIndex(2));
    assertEquals(dts.getValueAtIndex(3), noIntersecOp.getValueAtIndex(3));
    assertEquals(dts.getValueAtIndex(4), noIntersecOp.getValueAtIndex(4));
    assertEquals(dts.getValueAtIndex(5), noIntersecOp.getValueAtIndex(5));
    assertEquals(dts3.getValueAtIndex(0), noIntersecOp.getValueAtIndex(6));
    assertEquals(dts3.getValueAtIndex(1), noIntersecOp.getValueAtIndex(7));
  }

  @Test
  public void test_intersectionFirstValue() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    DoubleTimeSeries<T> dts2 = createStandardTimeSeries2();
    DoubleTimeSeries<T> ets = createEmptyTimeSeries();
    assertEquals(ets, ets.intersectionFirstValue(dts));
    assertEquals(ets, dts.intersectionFirstValue(ets));
    
    DoubleTimeSeries<T> result = dts.intersectionFirstValue(dts2);
    assertEquals(3, result.size());
    assertEquals(Double.valueOf(4.0), result.getValueAtIndex(0));
    assertEquals(Double.valueOf(5.0), result.getValueAtIndex(1));
    assertEquals(Double.valueOf(6.0), result.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result.getTimeAtIndex(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_scalarOperators() {
    assertOperationSuccessful(createStandardTimeSeries().add(10.0), new double[] {11.0, 12.0, 13.0, 14.0, 15.0, 16.0 });
    assertOperationSuccessful(createStandardTimeSeries().subtract(1.0), new double[] {0.0, 1.0, 2.0, 3.0, 4.0, 5.0 });
    assertOperationSuccessful(createStandardTimeSeries().multiply(2.0), new double[] {2.0, 4.0, 6.0, 8.0, 10.0, 12.0 });
    assertOperationSuccessful(createStandardTimeSeries().divide(2.0), new double[] {0.5, 1.0, 1.5, 2.0, 2.5, 3.0 });
    assertOperationSuccessful(createStandardTimeSeries().power(2.0), new double[] {1.0, 4.0, 9.0, 16.0, 25.0, 36.0 });
    assertOperationSuccessful(createStandardTimeSeries().minimum(2.0), new double[] {1.0, 2.0, 2.0, 2.0, 2.0, 2.0 });
    assertOperationSuccessful(createStandardTimeSeries().maximum(2.5), new double[] {2.5, 2.5, 3.0, 4.0, 5.0, 6.0 });
    assertOperationSuccessful(createStandardTimeSeries().average(2.0), new double[] {1.5, 2.0, 2.5, 3.0, 3.5, 4.0 });
    assertOperationSuccessful(createStandardTimeSeries().negate(), new double[] {-1.0, -2.0, -3.0, -4.0, -5.0, -6.0 });
    assertOperationSuccessful(createStandardTimeSeries().abs(), new double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0 });
    assertOperationSuccessful(createStandardTimeSeries().reciprocal(), new double[] {1.0 / 1.0, 1.0 / 2.0, 1.0 / 3.0, 1.0 / 4.0, 1.0 / 5.0, 1.0 / 6.0 });
    assertOperationSuccessful(createStandardTimeSeries().log(), new double[] {Math.log(1.0), Math.log(2.0), Math.log(3.0), Math.log(4.0), Math.log(5.0), Math.log(6.0) });
    assertOperationSuccessful(createStandardTimeSeries().log10(), new double[] {Math.log10(1.0), Math.log10(2.0), Math.log10(3.0), Math.log10(4.0), Math.log10(5.0), Math.log10(6.0) });
  }

  @Test
  public void test_lag() {
    DoubleTimeSeries<T> dts = createStandardTimeSeries();
    DoubleTimeSeries<T> lagged = dts.lag(0);
    assertOperationSuccessful(lagged, new double[] {1d, 2d, 3d, 4d, 5d, 6d });
    assertEquals(lagged.getEarliestTime(), testTimes()[0]);
    assertEquals(lagged.getLatestTime(), testTimes()[5]);
    assertEquals(dts, lagged);
    lagged = dts.lag(1);
    assertOperationSuccessful(lagged, new double[] {1d, 2d, 3d, 4d, 5d });
    assertEquals(lagged.getEarliestTime(), testTimes()[1]);
    assertEquals(lagged.getLatestTime(), testTimes()[5]);
    lagged = dts.lag(-1);
    assertOperationSuccessful(lagged, new double[] {2d, 3d, 4d, 5d, 6d });
    assertEquals(lagged.getEarliestTime(), testTimes()[0]);
    assertEquals(lagged.getLatestTime(), testTimes()[4]);
    lagged = dts.lag(5);
    assertOperationSuccessful(lagged, new double[] {1d });
    lagged = dts.lag(-5);
    assertOperationSuccessful(lagged, new double[] {6d });
    lagged = dts.lag(6);
    assertOperationSuccessful(lagged, new double[0]);
    lagged = dts.lag(-6);
    assertOperationSuccessful(lagged, new double[0]);
    lagged = dts.lag(1000);
    assertOperationSuccessful(lagged, new double[0]);
    lagged = dts.lag(-1000);
    assertOperationSuccessful(lagged, new double[0]);
  }

  protected static void assertOperationSuccessful(DoubleTimeSeries<?> result, double[] expected) {
    assertEquals(expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], result.getValueAtIndex(i), 0.001);
    }
  }

}
