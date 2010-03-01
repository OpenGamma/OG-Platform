package com.opengamma.timeseries;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import org.junit.Ignore;
import org.junit.Test;

import com.opengamma.util.CompareUtils;
import com.opengamma.util.timeseries.date.DateDoubleTimeSeries;

@Ignore
public abstract class DateDoubleTimeSeriesTest {
   
  public abstract DateDoubleTimeSeries createEmptyTimeSeries();
  public abstract DateDoubleTimeSeries createTimeSeries(Date[] times, double[] values);
  public abstract DateDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values);
  public abstract DateDoubleTimeSeries createTimeSeries(DateDoubleTimeSeries dts);

  public Date[] testDates() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2010, 1, 8); // feb
    Date one = cal.getTime();
    cal.set(2010, 1, 9);
    Date two = cal.getTime();
    cal.set(2010, 1, 10);
    Date three = cal.getTime();
    cal.set(2010, 1, 11);
    Date four = cal.getTime();
    cal.set(2010, 1, 12);
    Date five = cal.getTime();
    cal.set(2010, 1, 13);
    Date six = cal.getTime();
    return new Date[] { one, two, three, four, five, six };
  }
  
  public Date[] testDates2() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2010, 1, 11); // feb
    Date one = cal.getTime();
    cal.set(2010, 1, 12);
    Date two = cal.getTime();
    cal.set(2010, 1, 13);
    Date three = cal.getTime();
    cal.set(2010, 1, 14);
    Date four = cal.getTime();
    cal.set(2010, 1, 15);
    Date five = cal.getTime();
    cal.set(2010, 1, 16);
    Date six = cal.getTime();
    return new Date[] { one, two, three, four, five, six };
  }
  
  @Test
  public void testArrayConstructor() {
    DateDoubleTimeSeries dts = createTimeSeries(new Date[0], new double[0]);
    assertEquals(0, dts.size());
    Date[] times = testDates();
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
    DateDoubleTimeSeries dts = createTimeSeries(new ArrayList<Date>(), new ArrayList<Double>());
    assertEquals(0, dts.size());
    Date[] times = testDates();
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    List<Date> timesList = new ArrayList<Date>();
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
    DateDoubleTimeSeries dts = createEmptyTimeSeries();
    DateDoubleTimeSeries dts2 = createTimeSeries(dts);
    assertEquals(0, dts2.size());
    Date[] times = testDates();
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    dts = createTimeSeries(times, values);
    dts2 = createTimeSeries(dts);
    assertEquals(6, dts2.size());
    Iterator<Double> valuesIter = dts2.valuesIterator();
    for (double i=1.0; i<=6.0; i+=1.0) {
      assertTrue(CompareUtils.closeEquals(i, valuesIter.next()));
    }    
  }
  
  public DateDoubleTimeSeries createStandardTimeSeries() {
    Date[] times = testDates();
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    return createTimeSeries(times, values);
  }
  
  public DateDoubleTimeSeries createStandardTimeSeries2() {
    Date[] times = testDates2();
    double[] values = {4.0, 5.0, 6.0, 7.0, 8.0, 9.0};
    return createTimeSeries(times, values);
  }
  
  @Test
  public void testHead() {
    DateDoubleTimeSeries dts = createStandardTimeSeries();
    DateDoubleTimeSeries head5 = (DateDoubleTimeSeries) dts.head(5);
    Iterator<Entry<Date, Double>> iterator = head5.iterator();
    for (int i=0; i<5; i++) {
      Entry<Date, Double> entry = iterator.next();
      assertEquals(testDates()[i], entry.getKey());
      assertEquals(Double.valueOf(i+1), entry.getValue());
    }
    assertEquals(dts.head(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().head(0), createEmptyTimeSeries());
  }
  
  @Test
  public void testTail() {
    DateDoubleTimeSeries dts = createStandardTimeSeries();
    DateDoubleTimeSeries tail5 = (DateDoubleTimeSeries) dts.tail(5);
    Iterator<Entry<Date, Double>> iterator = tail5.iterator();
    for (int i=1; i<6; i++) {
      Entry<Date, Double> entry = iterator.next();
      assertEquals(testDates()[i], entry.getKey());
      assertEquals(Double.valueOf(i+1), entry.getValue());
    }
    assertEquals(dts.tail(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().tail(0), createEmptyTimeSeries());
  }
  
  @Test
  public void testSize() {
    DateDoubleTimeSeries dts = createStandardTimeSeries();
    assertEquals(6, dts.size());
    DateDoubleTimeSeries emptyTS = createEmptyTimeSeries();
    assertEquals(0, emptyTS.size());
  }

  @Test
  public void testIsEmpty() {
    DateDoubleTimeSeries empty = createEmptyTimeSeries();
    DateDoubleTimeSeries dts = createStandardTimeSeries();
    assertTrue(empty.isEmpty());
    assertFalse(dts.isEmpty());
  }

  @Test
  public void testGetLatestInstant() {
    DateDoubleTimeSeries empty = createEmptyTimeSeries();
    DateDoubleTimeSeries dts = createStandardTimeSeries();
    Date[] testDates = testDates();
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
    DateDoubleTimeSeries empty = createEmptyTimeSeries();
    DateDoubleTimeSeries dts = createStandardTimeSeries();
    assertTrue(CompareUtils.closeEquals(6.0d, dts.getLatestValue()));
    try {
      empty.getLatestValue();
    } catch (NoSuchElementException nsee) {
      return;
    }
    fail();
  }

  @Test
  public void testGetEarliestInstant() {
    DateDoubleTimeSeries empty = createEmptyTimeSeries();
    DateDoubleTimeSeries dts = createStandardTimeSeries();
    Date[] testDates = testDates();
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
    DateDoubleTimeSeries empty = createEmptyTimeSeries();
    DateDoubleTimeSeries dts = createStandardTimeSeries();
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
    Iterator<Date> emptyTimesIter = createEmptyTimeSeries().timeIterator();
    Iterator<Date> dtsTimesIter = createStandardTimeSeries().timeIterator();
    Date[] testDates = testDates();
    for (int i=0; i<6; i++) {
      assertTrue(dtsTimesIter.hasNext());
      Date time = dtsTimesIter.next();
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
    Iterator<Entry<Date, Double>> emptyIter = createEmptyTimeSeries().iterator();
    Iterator<Entry<Date, Double>> dtsIter = createStandardTimeSeries().iterator();
    Date[] testDates = testDates();
    for (int i=0; i<6; i++) {
      assertTrue(dtsIter.hasNext());
      Entry<Date, Double> entry = dtsIter.next();
      Date time = entry.getKey();
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
    DateDoubleTimeSeries emptyTS = createEmptyTimeSeries();
    DateDoubleTimeSeries dts = createStandardTimeSeries();
    Date[] testDates = testDates();
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
    DateDoubleTimeSeries emptyTS = createEmptyTimeSeries();
    DateDoubleTimeSeries dts = createStandardTimeSeries();
    Date[] testDates = testDates();
    DateDoubleTimeSeries threeToFive = (DateDoubleTimeSeries) dts.subSeries(testDates[3], testDates[5]);
    assertEquals(2, threeToFive.size());
    Iterator<Entry<Date, Double>> iterator = threeToFive.iterator();
    for (int i=3; i<5; i++) {
      Entry<Date, Double> item = iterator.next();
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
    DateDoubleTimeSeries dts = createStandardTimeSeries();
    DateDoubleTimeSeries dts2 = createStandardTimeSeries2();
    DateDoubleTimeSeries ets = createEmptyTimeSeries();
    assertEquals(ets, dts.add(ets));
    assertEquals(ets, ets.add(dts));
    assertEquals(dts, dts.unionAdd(ets));
    assertEquals(dts, ets.unionAdd(dts));
    DateDoubleTimeSeries result = (DateDoubleTimeSeries) dts.add(dts2);
    assertEquals(3, result.size());
    assertEquals(Double.valueOf(8.0), result.getValueAt(0));
    assertEquals(Double.valueOf(10.0), result.getValueAt(1));
    assertEquals(Double.valueOf(12.0), result.getValueAt(2));
    assertEquals(dts.getTime(3), result.getTime(0));
    assertEquals(dts.getTime(4), result.getTime(1));
    assertEquals(dts.getTime(5), result.getTime(2));
    DateDoubleTimeSeries unionResult = (DateDoubleTimeSeries) dts.unionAdd(dts2);
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
