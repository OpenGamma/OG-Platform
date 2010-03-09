package com.opengamma.util.timeseries.zoneddatetime;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.junit.Ignore;
import org.junit.Test;

import com.opengamma.util.CompareUtils;

@Ignore
public abstract class ZonedDateTimeDoubleTimeSeriesTest {
   
  public abstract ZonedDateTimeDoubleTimeSeries createEmptyTimeSeries();
  public abstract ZonedDateTimeDoubleTimeSeries createTimeSeries(ZonedDateTime[] times, double[] values);
  public abstract ZonedDateTimeDoubleTimeSeries createTimeSeries(List<ZonedDateTime> times, List<Double> values);
  public abstract ZonedDateTimeDoubleTimeSeries createTimeSeries(ZonedDateTimeDoubleTimeSeries dts);

  public ZonedDateTime makeDate(int year, int month, int day) {
    ZonedDateTime one = ZonedDateTime.from(LocalDateTime.midnight(year, month, day), TimeZone.of(java.util.TimeZone.getDefault().getID()));
    return one;
  }
  
  public ZonedDateTime[] testDates() {
    ZonedDateTime one = makeDate(2010, 2, 8);
    ZonedDateTime two = makeDate(2010, 2, 9);
    ZonedDateTime three = makeDate(2010, 2, 10);
    ZonedDateTime four = makeDate(2010, 2, 11);
    ZonedDateTime five = makeDate(2010, 2, 12);
    ZonedDateTime six = makeDate(2010, 2, 13);
    return new ZonedDateTime[] { one, two, three, four, five, six };
  }
  
  public ZonedDateTime[] testDates2() {
    ZonedDateTime one = makeDate(2010, 2, 11);
    ZonedDateTime two = makeDate(2010, 2, 12);
    ZonedDateTime three = makeDate(2010, 2, 13);
    ZonedDateTime four = makeDate(2010, 2, 14);
    ZonedDateTime five = makeDate(2010, 2, 15);
    ZonedDateTime six = makeDate(2010, 2, 16);
    return new ZonedDateTime[] { one, two, three, four, five, six };
  }
  
  @Test
  public void testArrayConstructor() {
    ZonedDateTimeDoubleTimeSeries dts = createTimeSeries(new ZonedDateTime[0], new double[0]);
    assertEquals(0, dts.size());
    ZonedDateTime[] times = testDates();
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    dts = createTimeSeries(times, values);
    assertEquals(6, dts.size());
    Iterator<Double> valuesIter = dts.valuesIterator();
    for (double i=1.0; i<=6.0; i+=1.0) {
      assertTrue(CompareUtils.closeEquals(i, valuesIter.next()));
    }
  }
  
  @Test
  public void testListConstructor() {
    ZonedDateTimeDoubleTimeSeries dts = createTimeSeries(new ArrayList<ZonedDateTime>(), new ArrayList<Double>());
    assertEquals(0, dts.size());
    ZonedDateTime[] times = testDates();
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    List<ZonedDateTime> timesList = new ArrayList<ZonedDateTime>();
    List<Double> valuesList = new ArrayList<Double>();
    for (int i=0; i<times.length; i++) {
      timesList.add(times[i]);
      valuesList.add(values[i]);
    }
    dts = createTimeSeries(timesList, valuesList);
    assertEquals(6, dts.size());
    Iterator<Double> valuesIter = dts.valuesIterator();
    for (double i=1.0; i<=6.0; i+=1.0) {
      assertTrue(CompareUtils.closeEquals(i, valuesIter.next()));
    }
  }
  
  @Test
  public void testTimeSeriesConstructor() {
    ZonedDateTimeDoubleTimeSeries dts = createEmptyTimeSeries();
    ZonedDateTimeDoubleTimeSeries dts2 = createTimeSeries(dts);
    assertEquals(0, dts2.size());
    ZonedDateTime[] times = testDates();
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    dts = createTimeSeries(times, values);
    dts2 = createTimeSeries(dts);
    assertEquals(6, dts2.size());
    Iterator<Double> valuesIter = dts2.valuesIterator();
    for (double i=1.0; i<=6.0; i+=1.0) {
      assertTrue(CompareUtils.closeEquals(i, valuesIter.next()));
    }    
  }
  
  public ZonedDateTimeDoubleTimeSeries createStandardTimeSeries() {
    ZonedDateTime[] times = testDates();
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    return createTimeSeries(times, values);
  }
  
  public ZonedDateTimeDoubleTimeSeries createStandardTimeSeries2() {
    ZonedDateTime[] times = testDates2();
    double[] values = {4.0, 5.0, 6.0, 7.0, 8.0, 9.0};
    return createTimeSeries(times, values);
  }
  
  @Test
  public void testHead() {
    ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    ZonedDateTimeDoubleTimeSeries head5 = (ZonedDateTimeDoubleTimeSeries) dts.head(5);
    Iterator<Entry<ZonedDateTime, Double>> iterator = head5.iterator();
    for (int i=0; i<5; i++) {
      Entry<ZonedDateTime, Double> entry = iterator.next();
      assertEquals(testDates()[i], entry.getKey());
      assertEquals(Double.valueOf(i+1), entry.getValue());
    }
    assertEquals(dts.head(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().head(0), createEmptyTimeSeries());
  }
  
  @Test
  public void testTail() {
    ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    ZonedDateTimeDoubleTimeSeries tail5 = (ZonedDateTimeDoubleTimeSeries) dts.tail(5);
    Iterator<Entry<ZonedDateTime, Double>> iterator = tail5.iterator();
    for (int i=1; i<6; i++) {
      Entry<ZonedDateTime, Double> entry = iterator.next();
      assertEquals(testDates()[i], entry.getKey());
      assertEquals(Double.valueOf(i+1), entry.getValue());
    }
    assertEquals(dts.tail(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().tail(0), createEmptyTimeSeries());
  }
  
  @Test
  public void testSize() {
    ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    assertEquals(6, dts.size());
    ZonedDateTimeDoubleTimeSeries emptyTS = createEmptyTimeSeries();
    assertEquals(0, emptyTS.size());
  }

  @Test
  public void testIsEmpty() {
    ZonedDateTimeDoubleTimeSeries empty = createEmptyTimeSeries();
    ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    assertTrue(empty.isEmpty());
    assertFalse(dts.isEmpty());
  }

  @Test
  public void testGetLatestInstant() {
    ZonedDateTimeDoubleTimeSeries empty = createEmptyTimeSeries();
    ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    ZonedDateTime[] testDates = testDates();
    assertEquals(testDates[5], dts.getLatestTime());
    try {
      empty.getLatestTime();
    } catch (NoSuchElementException nsee) {
      return;
    }
    fail();
  }

  @Test
  public void testGetLatestValue() {
    ZonedDateTimeDoubleTimeSeries empty = createEmptyTimeSeries();
    ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    double latestValue = dts.getLatestValue();
    assertTrue(CompareUtils.closeEquals(6.0d, latestValue));
    try {
      empty.getLatestValue();
    } catch (NoSuchElementException nsee) {
      return;
    }
    fail();
  }

  @Test
  public void testGetEarliestInstant() {
    ZonedDateTimeDoubleTimeSeries empty = createEmptyTimeSeries();
    ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    ZonedDateTime[] testDates = testDates();
    assertEquals(testDates[0], dts.getEarliestTime());
    try {
      empty.getEarliestTime();
    } catch (NoSuchElementException nsee) {
      return;
    }
    fail();    
  }

  @Test
  public void testGetEarliestValue() {
    ZonedDateTimeDoubleTimeSeries empty = createEmptyTimeSeries();
    ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    assertTrue(CompareUtils.closeEquals(1d, dts.getEarliestValue()));
    try {
      empty.getEarliestValue();
    } catch (NoSuchElementException nsee) {
      return;
    }
    fail();
  }

  @Test
  public void testValuesIterator() {
    Iterator<Double> emptyValuesIter = createEmptyTimeSeries().valuesIterator();
    Iterator<Double> dtsValuesIter = createStandardTimeSeries().valuesIterator();
    for (double i=1; i<=6.0; i+=1.0d) {
      assertTrue(dtsValuesIter.hasNext());
      Double val = dtsValuesIter.next();
      CompareUtils.closeEquals(val, i);
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

  @Test
  public void testTimeIterator() {    
    Iterator<ZonedDateTime> emptyTimesIter = createEmptyTimeSeries().timeIterator();
    Iterator<ZonedDateTime> dtsTimesIter = createStandardTimeSeries().timeIterator();
    ZonedDateTime[] testDates = testDates();
    for (int i=0; i<6; i++) {
      assertTrue(dtsTimesIter.hasNext());
      ZonedDateTime time = dtsTimesIter.next();
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

  @Test
  public void testIterator() {
    Iterator<Entry<ZonedDateTime, Double>> emptyIter = createEmptyTimeSeries().iterator();
    Iterator<Entry<ZonedDateTime, Double>> dtsIter = createStandardTimeSeries().iterator();
    ZonedDateTime[] testDates = testDates();
    for (int i=0; i<6; i++) {
      assertTrue(dtsIter.hasNext());
      Entry<ZonedDateTime, Double> entry = dtsIter.next();
      ZonedDateTime time = entry.getKey();
      CompareUtils.closeEquals(entry.getValue(), (double)i+1);
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

  @Test(expected = IndexOutOfBoundsException.class)
  public void testGetDataPoint() {
    ZonedDateTimeDoubleTimeSeries emptyTS = createEmptyTimeSeries();
    ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    ZonedDateTime[] testDates = testDates();
    for (int i=0; i<6; i++) {
      Double val = dts.getValue(testDates[i]);
      CompareUtils.closeEquals(val, i+1);
      val = dts.getValueAt(i);
      CompareUtils.closeEquals(val, i+1);
    }
    emptyTS.getValueAt(0);
  }

  @SuppressWarnings("cast")
  @Test
  public void testSubSeriesInstantProviderInstantProvider() {
    ZonedDateTimeDoubleTimeSeries emptyTS = createEmptyTimeSeries();
    ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    ZonedDateTime[] testDates = testDates();
    ZonedDateTimeDoubleTimeSeries threeToFive = (ZonedDateTimeDoubleTimeSeries) dts.subSeries(testDates[3], testDates[5]);
    assertEquals(2, threeToFive.size());
    Iterator<Entry<ZonedDateTime, Double>> iterator = threeToFive.iterator();
    for (int i=3; i<5; i++) {
      Entry<ZonedDateTime, Double> item = iterator.next();
      assertEquals(testDates[i], item.getKey());
      assertTrue(CompareUtils.closeEquals((double)i+1, item.getValue()));
    }
    assertEquals(4, dts.subSeries(testDates[0], testDates[4]).size());
    assertEquals(5, dts.subSeries(testDates[0], true, testDates[4], true).size());
    assertEquals(1, dts.subSeries(testDates[4], testDates[5]).size());
    assertEquals(0, dts.subSeries(testDates[4], false, testDates[5], false).size());
    assertEquals(emptyTS, emptyTS.subSeries(testDates[1], testDates[1]));
  }

  @Test
  public void testHashCode() {
    assertEquals(createStandardTimeSeries().hashCode(), createStandardTimeSeries().hashCode());
    assertEquals(createEmptyTimeSeries().hashCode(), createEmptyTimeSeries().hashCode());
  }

  @Test
  public void testEquals() {
    assertEquals(createStandardTimeSeries(), createStandardTimeSeries());
    assertFalse(createStandardTimeSeries().equals(createEmptyTimeSeries()));
    assertFalse(createEmptyTimeSeries().equals(createStandardTimeSeries()));
    assertEquals(createEmptyTimeSeries(), createEmptyTimeSeries());
  }
  
  @Test
  public void testOperators() {
    ZonedDateTimeDoubleTimeSeries dts = createStandardTimeSeries();
    ZonedDateTimeDoubleTimeSeries dts2 = createStandardTimeSeries2();
    ZonedDateTimeDoubleTimeSeries ets = createEmptyTimeSeries();
    assertEquals(ets, dts.add(ets));
    assertEquals(ets, ets.add(dts));
    assertEquals(dts, dts.unionAdd(ets));
    assertEquals(dts, ets.unionAdd(dts));
    ZonedDateTimeDoubleTimeSeries result = (ZonedDateTimeDoubleTimeSeries) dts.add(dts2);
    assertEquals(3, result.size());
    assertEquals(Double.valueOf(8.0), result.getValueAt(0));
    assertEquals(Double.valueOf(10.0), result.getValueAt(1));
    assertEquals(Double.valueOf(12.0), result.getValueAt(2));
    assertEquals(dts.getTime(3), result.getTime(0));
    assertEquals(dts.getTime(4), result.getTime(1));
    assertEquals(dts.getTime(5), result.getTime(2));
    ZonedDateTimeDoubleTimeSeries unionResult = (ZonedDateTimeDoubleTimeSeries) dts.unionAdd(dts2);
    assertEquals(9, unionResult.size());
    assertEquals(Double.valueOf(1.0), unionResult.getValueAt(0));
    assertEquals(Double.valueOf(2.0), unionResult.getValueAt(1));
    assertEquals(Double.valueOf(3.0), unionResult.getValueAt(2));
    assertEquals(Double.valueOf(8.0), unionResult.getValueAt(3));
    assertEquals(Double.valueOf(10.0), unionResult.getValueAt(4));
    assertEquals(Double.valueOf(12.0), unionResult.getValueAt(5));
    assertEquals(Double.valueOf(7.0), unionResult.getValueAt(6));
    assertEquals(Double.valueOf(8.0), unionResult.getValueAt(7));
    assertEquals(Double.valueOf(9.0), unionResult.getValueAt(8));
    assertEquals(dts.getTime(0), unionResult.getTime(0));
    assertEquals(dts.getTime(1), unionResult.getTime(1));
    assertEquals(dts.getTime(2), unionResult.getTime(2));
    assertEquals(dts.getTime(3), unionResult.getTime(3));
    assertEquals(dts.getTime(4), unionResult.getTime(4));
    assertEquals(dts.getTime(5), unionResult.getTime(5));
    assertEquals(dts2.getTime(3), unionResult.getTime(6));
    assertEquals(dts2.getTime(4), unionResult.getTime(7));
    assertEquals(dts2.getTime(5), unionResult.getTime(8));
  }


}
