/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.object;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.util.timeseries.ObjectTimeSeries;

@Test
public abstract class ObjectTimeSeriesTest<E, T> {
  
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(ObjectTimeSeriesTest.class);
   
  public abstract ObjectTimeSeries<E, T> createEmptyTimeSeries();
  public abstract ObjectTimeSeries<E, T> createTimeSeries(E[] times, T[] values);
  public abstract ObjectTimeSeries<E, T> createTimeSeries(List<E> times, List<T> values);
  public abstract ObjectTimeSeries<E, T> createTimeSeries(ObjectTimeSeries<E, T> dts);

  public abstract E[] emptyTimes();
  public abstract E[] testTimes();
  public abstract E[] testTimes2();
  
  public abstract T[] emptyValues();
  public abstract T[] testValues();
  
  public void testArrayConstructor() {
    ObjectTimeSeries<E, T> dts = createTimeSeries(emptyTimes(), emptyValues());
    assertEquals(0, dts.size());
    E[] times = testTimes();
    T[] values = testValues();
    dts = createTimeSeries(times, values);
    assertEquals(6, dts.size());
    Iterator<T> valuesIter = dts.valuesIterator();
    for (int i=0; i<values.length; i++) {
      assertTrue(ObjectUtils.equals(values[i], valuesIter.next()));
    }
  }
  
  public void testListConstructor() {
    ObjectTimeSeries<E, T> dts = createTimeSeries(new ArrayList<E>(), new ArrayList<T>());
    assertEquals(0, dts.size());
    E[] times = testTimes();
    T[] values = testValues();
    List<E> timesList = new ArrayList<E>();
    List<T> valuesList = new ArrayList<T>();
    for (int i=0; i<times.length; i++) {
      timesList.add(times[i]);
      valuesList.add(values[i]);
    }
    dts = createTimeSeries(timesList, valuesList);
    assertEquals(6, dts.size());
    Iterator<T> valuesIter = dts.valuesIterator();
    for (int i=0; i<6; i++) {
      assertTrue(ObjectUtils.equals(values[i], valuesIter.next()));
    }
  }
  
  public void testTimeSeriesConstructor() {
    ObjectTimeSeries<E, T> dts = createEmptyTimeSeries();
    ObjectTimeSeries<E, T> dts2 = createTimeSeries(dts);
    assertEquals(0, dts2.size());
    E[] times = testTimes();
    T[] values = testValues();
    dts = createTimeSeries(times, values);
    dts2 = createTimeSeries(dts);
    assertEquals(6, dts2.size());
    Iterator<T> valuesIter = dts2.valuesIterator();
    for (int i=0; i<6; i++) {
      assertTrue(ObjectUtils.equals(values[i], valuesIter.next()));
    }    
  }
  
  public ObjectTimeSeries<E, T> createStandardTimeSeries() {
    E[] times = testTimes();
    T[] values = testValues();
    return createTimeSeries(times, values);
  }
  
  public ObjectTimeSeries<E, T> createStandardTimeSeries2() {
    E[] times = testTimes2();
    T[] values = testValues();
    return createTimeSeries(times, values);
  }
  
  public void testHead() {
    ObjectTimeSeries<E, T> dts = createStandardTimeSeries();
    ObjectTimeSeries<E, T> head5 = (ObjectTimeSeries<E, T>) dts.head(5);
    Iterator<Entry<E, T>> iterator = head5.iterator();
    for (int i=0; i<5; i++) {
      Entry<E, T> entry = iterator.next();
      assertEquals(testTimes()[i], entry.getKey());
      assertEquals(testValues()[i], entry.getValue());
    }
    assertEquals(dts.head(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().head(0), createEmptyTimeSeries());
  }
  
  public void testTail() {
    ObjectTimeSeries<E, T> dts = createStandardTimeSeries();
    ObjectTimeSeries<E, T> tail5 = (ObjectTimeSeries<E, T>) dts.tail(5);
    Iterator<Entry<E, T>> iterator = tail5.iterator();
    for (int i=1; i<6; i++) {
      Entry<E, T> entry = iterator.next();
      assertEquals(testTimes()[i], entry.getKey());
      assertEquals(testValues()[i], entry.getValue());
    }
    assertEquals(dts.tail(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().tail(0), createEmptyTimeSeries());
  }
  
  public void testSize() {
    ObjectTimeSeries<E, T> dts = createStandardTimeSeries();
    assertEquals(6, dts.size());
    ObjectTimeSeries<E, T> emptyTS = createEmptyTimeSeries();
    assertEquals(0, emptyTS.size());
  }

  public void testIsEmpty() {
    ObjectTimeSeries<E, T> empty = createEmptyTimeSeries();
    ObjectTimeSeries<E, T> dts = createStandardTimeSeries();
    assertTrue(empty.isEmpty());
    assertFalse(dts.isEmpty());
  }

  public void testGetLatestInstant() {
    ObjectTimeSeries<E, T> empty = createEmptyTimeSeries();
    ObjectTimeSeries<E, T> dts = createStandardTimeSeries();
    E[] testDates = testTimes();
    assertEquals(testDates[5], dts.getLatestTime());
    try {
      empty.getLatestTime();
    } catch (NoSuchElementException nsee) {
      return;
    }
    fail();
  }

  public void testGetLatestValue() {
    ObjectTimeSeries<E, T> empty = createEmptyTimeSeries();
    ObjectTimeSeries<E, T> dts = createStandardTimeSeries();
    T[] values = testValues();
    assertEquals(values[values.length-1], dts.getLatestValue());
    try {
      empty.getLatestValue();
    } catch (NoSuchElementException nsee) {
      return;
    }
    fail();
  }

  public void testGetEarliestInstant() {
    ObjectTimeSeries<E, T> empty = createEmptyTimeSeries();
    ObjectTimeSeries<E, T> dts = createStandardTimeSeries();
    E[] testDates = testTimes();
    assertEquals(testDates[0], dts.getEarliestTime());
    try {
      empty.getEarliestTime();
    } catch (NoSuchElementException nsee) {
      return;
    }
    fail();    
  }

  public void testGetEarliestValue() {
    ObjectTimeSeries<E, T> empty = createEmptyTimeSeries();
    ObjectTimeSeries<E, T> dts = createStandardTimeSeries();
    T[] values = testValues();
    assertEquals(values[0], dts.getEarliestValue());
    try {
      empty.getEarliestValue();
    } catch (NoSuchElementException nsee) {
      return;
    }
    fail();
  }

  public void testValuesIterator() {
    Iterator<T> emptyValuesIter = createEmptyTimeSeries().valuesIterator();
    Iterator<T> dtsValuesIter = createStandardTimeSeries().valuesIterator();
    T[] values = testValues();
    for (int i=0; i<6; i++) {
      assertTrue(dtsValuesIter.hasNext());
      T val = dtsValuesIter.next();
      assertEquals(values[i], val);
    }
    try {
      dtsValuesIter.next();
    } catch (NoSuchElementException nsee) {
      assertFalse(emptyValuesIter.hasNext());
      try {
        emptyValuesIter.next();
      } catch (NoSuchElementException nsuchee) {
        return;
      }      
    }
    fail();
  }

  public void testTimeIterator() {    
    Iterator<E> emptyTimesIter = createEmptyTimeSeries().timeIterator();
    Iterator<E> dtsTimesIter = createStandardTimeSeries().timeIterator();
    E[] testDates = testTimes();
    for (int i=0; i<6; i++) {
      assertTrue(dtsTimesIter.hasNext());
      E time = dtsTimesIter.next();
      assertEquals(testDates[i], time);
    }
    try {
      dtsTimesIter.next();
    } catch (NoSuchElementException nsee) {
      assertFalse(emptyTimesIter.hasNext());
      try {
        emptyTimesIter.next();
      } catch (NoSuchElementException nsuchee) {
        return;
      }      
    }
    fail();
  }

  public void testIterator() {
    Iterator<Entry<E, T>> emptyIter = createEmptyTimeSeries().iterator();
    Iterator<Entry<E, T>> dtsIter = createStandardTimeSeries().iterator();
    E[] testDates = testTimes();
    T[] testValues = testValues();
    for (int i=0; i<6; i++) {
      assertTrue(dtsIter.hasNext());
      Entry<E, T> entry = dtsIter.next();
      E time = entry.getKey();
      assertEquals(entry.getValue(), testValues[i]);
      assertEquals(testDates[i], time);
    }
    try {
      dtsIter.next();
    } catch (NoSuchElementException nsee) {
      assertFalse(emptyIter.hasNext());
      try {
        emptyIter.next();
      } catch (NoSuchElementException nsuchee) {
        return;
      }      
    }
    fail();
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testGetDataPoint() {
    ObjectTimeSeries<E, T> emptyTS = createEmptyTimeSeries();
    ObjectTimeSeries<E, T> dts = createStandardTimeSeries();
    E[] testDates = testTimes();
    T[] testValues = testValues();
    for (int i=0; i<6; i++) {
      T val = dts.getValue(testDates[i]);
      assertEquals(val, testValues[i]);
      val = dts.getValueAt(i);
      assertEquals(val, testValues[i]);
    }
    emptyTS.getValueAt(0);
  }

  @DataProvider(name = "subSeries")
  Object[][] data_subSeries() {
    return new Object[][] {
//        {0, true, 4, false, 4, 0},
//        {0, true, 4, true, 5, 0},
//        {0, false, 4, false, 3, 1},
//        {0, false, 4, true, 4, 1},
//        
//        {4, true, 5, false, 1, 4},
//        {4, true, 5, true, 2, 4},
//        {4, false, 5, false, 0, -1},
//        {4, false, 5, true, 1, 5},
//        
//        {4, true, 4, false, 0, -1},
//        {4, true, 4, true, 1, 4},
        {4, false, 4, false, 0, -1},  // matches TreeMap definition
        {4, false, 4, true, 0, -1},
    };
  }

  @SuppressWarnings("cast")
  @Test(dataProvider = "subSeries")
  public void testSubSeriesInstantProviderInstantProvider(int startIndex, boolean startInclude, int endIndex, boolean endInclude, int expectedSize, int expectedFirstIndex) {
    ObjectTimeSeries<E, T> dts = createStandardTimeSeries();
    E[] testDates = testTimes();
    T[] testValues = testValues();
    ObjectTimeSeries<E, T> threeToFive = dts.subSeries(testDates[3], testDates[5]);
    assertEquals(2, threeToFive.size());
    Iterator<Entry<E, T>> iterator = threeToFive.iterator();
    for (int i=3; i<5; i++) {
      Entry<E, T> item = iterator.next();
      assertEquals(testDates[i], item.getKey());
      assertEquals(testValues[i], item.getValue());
    }
    
    ObjectTimeSeries<E, T> sub = dts.subSeries(testDates[startIndex], startInclude, testDates[endIndex], endInclude);
    assertEquals(expectedSize, sub.size());
    if (expectedFirstIndex >= 0) {
      assertEquals(testDates[expectedFirstIndex], sub.getTimeAt(0));
    }
    
    if (startInclude && endInclude == false) {
      sub = dts.subSeries(testDates[startIndex], testDates[endIndex]);
      assertEquals(expectedSize, sub.size());
      if (expectedFirstIndex >= 0) {
        assertEquals(testDates[expectedFirstIndex], sub.getTimeAt(0));
      }
    }
  }

  public void testHashCode() {
    assertEquals(createStandardTimeSeries().hashCode(), createStandardTimeSeries().hashCode());
    assertEquals(createEmptyTimeSeries().hashCode(), createEmptyTimeSeries().hashCode());
  }

  public void testEquals() {
    assertEquals(createStandardTimeSeries(), createStandardTimeSeries());
    assertFalse(createStandardTimeSeries().equals(createEmptyTimeSeries()));
    assertFalse(createEmptyTimeSeries().equals(createStandardTimeSeries()));
    assertEquals(createEmptyTimeSeries(), createEmptyTimeSeries());
//    FastBackedObjectTimeSeries<E> createStandardTimeSeries = (FastBackedObjectTimeSeries<E>) createStandardTimeSeries();
//    FastBackedObjectTimeSeries<E> createStandardTimeSeries2 = (FastBackedObjectTimeSeries<E>) (createStandardTimeSeries().toDateTimeObjectTimeSeries());
//    s_logger.info(createStandardTimeSeries.getFastSeries().toString());
//    s_logger.info(createStandardTimeSeries2.getFastSeries().toString());
//    assertEquals(createStandardTimeSeries.getFastSeries(), createStandardTimeSeries2.getFastSeries());
//    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toDateObjectTimeSeries());
//    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toMutableDateObjectTimeSeries());
//    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toDateTimeObjectTimeSeries());
//    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toMutableDateTimeObjectTimeSeries());
//    try {
//      assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toFastIntObjectTimeSeries());
//      assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toFastMutableIntObjectTimeSeries());
//    } catch (OpenGammaRuntimeException ogre) {
//      // some combinations of classes don't support converting to fast int time series (e.g. things with millis precision).
//    }
//    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toFastLongObjectTimeSeries());
//    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toFastMutableLongObjectTimeSeries());
//    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toLocalDateObjectTimeSeries());
//    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toMutableLocalDateObjectTimeSeries());
//    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toZonedDateTimeObjectTimeSeries());
//    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toMutableZonedDateTimeObjectTimeSeries());
  }
//  
//  @Test
//  public void testOperators() {
//    ObjectTimeSeries<E, T> dts = createStandardTimeSeries();
//    ObjectTimeSeries<E, T> dts2 = createStandardTimeSeries2();
//    ObjectTimeSeries<E, T> ets = createEmptyTimeSeries();
//    assertEquals(ets, dts.intersectionFirstValue(ets));
//    assertEquals(ets, ets.intersectionFirstValue(dts));
//    assertEquals(dts, dts.unionAdd(ets));
//    assertEquals(dts, ets.unionAdd(dts));
//    ObjectTimeSeries<E, T> result = (ObjectTimeSeries<E, T>) dts.add(dts2);
//    assertEquals(3, result.size());
//    assertEquals(Double.valueOf(8.0), result.getValueAt(0));
//    assertEquals(Double.valueOf(10.0), result.getValueAt(1));
//    assertEquals(Double.valueOf(12.0), result.getValueAt(2));
//    assertEquals(dts.getTime(3), result.getTime(0));
//    assertEquals(dts.getTime(4), result.getTime(1));
//    assertEquals(dts.getTime(5), result.getTime(2));
//    ObjectTimeSeries<E, T> unionResult = (ObjectTimeSeries<E, T>) dts.unionAdd(dts2);
//    assertEquals(9, unionResult.size());
//    assertEquals(Double.valueOf(1.0), unionResult.getValueAt(0));
//    assertEquals(Double.valueOf(2.0), unionResult.getValueAt(1));
//    assertEquals(Double.valueOf(3.0), unionResult.getValueAt(2));
//    assertEquals(Double.valueOf(8.0), unionResult.getValueAt(3));
//    assertEquals(Double.valueOf(10.0), unionResult.getValueAt(4));
//    assertEquals(Double.valueOf(12.0), unionResult.getValueAt(5));
//    assertEquals(Double.valueOf(7.0), unionResult.getValueAt(6));
//    assertEquals(Double.valueOf(8.0), unionResult.getValueAt(7));
//    assertEquals(Double.valueOf(9.0), unionResult.getValueAt(8));
//    assertEquals(dts.getTime(0), unionResult.getTime(0));
//    assertEquals(dts.getTime(1), unionResult.getTime(1));
//    assertEquals(dts.getTime(2), unionResult.getTime(2));
//    assertEquals(dts.getTime(3), unionResult.getTime(3));
//    assertEquals(dts.getTime(4), unionResult.getTime(4));
//    assertEquals(dts.getTime(5), unionResult.getTime(5));
//    assertEquals(dts2.getTime(3), unionResult.getTime(6));
//    assertEquals(dts2.getTime(4), unionResult.getTime(7));
//    assertEquals(dts2.getTime(5), unionResult.getTime(8));
//  }
  
  
//  protected <T> static void assertOperationSuccessful(ObjectTimeSeries<?, T> result, T[] expected) {
//    assert expected.length == 6;
//    assertEquals(expected[0], result.getValueAt(0), 0.001);
//    assertEquals(expected[1], result.getValueAt(1), 0.001);
//    assertEquals(expected[2], result.getValueAt(2), 0.001);
//    assertEquals(expected[3], result.getValueAt(3), 0.001);
//    assertEquals(expected[4], result.getValueAt(4), 0.001);
//    assertEquals(expected[5], result.getValueAt(5), 0.001);
//    assertEquals(6, result.size());
//  }


}
