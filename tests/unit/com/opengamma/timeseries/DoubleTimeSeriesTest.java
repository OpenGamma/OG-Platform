package com.opengamma.timeseries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.Map.Entry;

import javax.time.Instant;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.util.CompareUtils;

public abstract class DoubleTimeSeriesTest {
  
  public abstract DoubleTimeSeries createEmptyTimeSeries();
  public abstract DoubleTimeSeries createTimeSeries(long[] times, double[] values, TimeZone[] zones);
  public abstract DoubleTimeSeries createTimeSeries(List<ZonedDateTime> times, List<Double> values);
  public abstract DoubleTimeSeries createTimeSeries(DoubleTimeSeries dts);
  public abstract DoubleTimeSeries createTimeSeries(SortedMap<ZonedDateTime, Double> initialMap);

  @Test
  public void testArrayConstructor() {
    DoubleTimeSeries dts = createTimeSeries(new long[0], new double[0], new TimeZone[0]);
    assertEquals(0, dts.size());
    long[] times = {1, 2, 3, 4, 5, 6};
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    TimeZone[] zones = new TimeZone[6];
    for(int i = 0; i < 6; i++) {
      zones[i] = TimeZone.UTC;
    }
    dts = createTimeSeries(times, values, zones);
    assertEquals(6, dts.size());
    Iterator<Double> valuesIter = dts.valuesIterator();
    for (double i=1.0; i<=6.0; i+=1.0) {
      assertTrue(CompareUtils.closeEquals(i, valuesIter.next()));
    }
  }
  
  @Test
  public void testListConstructor() {
    DoubleTimeSeries dts = createTimeSeries(new ArrayList<ZonedDateTime>(), new ArrayList<Double>());
    assertEquals(0, dts.size());
    long[] times = {1, 2, 3, 4, 5, 6};
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    List<ZonedDateTime> timesList = new ArrayList<ZonedDateTime>();
    List<Double> valuesList = new ArrayList<Double>();
    for (int i=0; i<times.length; i++) {
      timesList.add(ZonedDateTime.fromInstant(Instant.millisInstant(times[i]), TimeZone.UTC));
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
    DoubleTimeSeries dts = createEmptyTimeSeries();
    DoubleTimeSeries dts2 = createTimeSeries(dts);
    assertEquals(0, dts2.size());
    long[] times = {1, 2, 3, 4, 5, 6};
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    TimeZone[] zones = new TimeZone[6];
    for(int i = 0; i < 6; i++) {
      zones[i] = TimeZone.UTC;
    }
    dts = createTimeSeries(times, values, zones);
    dts2 = createTimeSeries(dts);
    assertEquals(6, dts2.size());
    Iterator<Double> valuesIter = dts2.valuesIterator();
    for (double i=1.0; i<=6.0; i+=1.0) {
      assertTrue(CompareUtils.closeEquals(i, valuesIter.next()));
    }    
  }
  
  public DoubleTimeSeries createStandardTimeSeries() {
    long[] times = {1, 2, 3, 4, 5, 6};
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    TimeZone[] zones = {TimeZone.UTC, TimeZone.UTC, TimeZone.UTC, TimeZone.UTC, TimeZone.UTC, TimeZone.UTC};
    return createTimeSeries(times, values, zones);
  }
  
  @Test
  public void testHead() {
    DoubleTimeSeries dts = createStandardTimeSeries();
    DoubleTimeSeries head5 = (DoubleTimeSeries) dts.head(5);
    Iterator<Entry<ZonedDateTime, Double>> iterator = head5.iterator();
    for (long i=1; i<=5; i++) {
      Entry<ZonedDateTime, Double> entry = iterator.next();
      assertEquals(i, entry.getKey().toInstant().toEpochMillis());
      assertEquals(Double.valueOf(i), entry.getValue());
    }
    assertEquals(dts.head(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().head(0), createEmptyTimeSeries());
  }
  @Test
  public void testTail() {
    DoubleTimeSeries dts = createStandardTimeSeries();
    DoubleTimeSeries tail5 = (DoubleTimeSeries) dts.tail(5);
    Iterator<Entry<ZonedDateTime, Double>> iterator = tail5.iterator();
    for (long i=2; i<=6; i++) {
      Entry<ZonedDateTime, Double> entry = iterator.next();
      assertEquals(i, entry.getKey().toInstant().toEpochMillis());
      assertEquals(Double.valueOf(i), entry.getValue());
    }
    assertEquals(dts.tail(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().tail(0), createEmptyTimeSeries());
  }
  
  @Test
  public void testSize() {
    DoubleTimeSeries dts = createStandardTimeSeries();
    assertEquals(6, dts.size());
    DoubleTimeSeries emptyTS = createEmptyTimeSeries();
    assertEquals(0, emptyTS.size());
  }

  @Test
  public void testIsEmpty() {
    DoubleTimeSeries empty = createEmptyTimeSeries();
    DoubleTimeSeries dts = createStandardTimeSeries();
    assertTrue(empty.isEmpty());
    assertFalse(dts.isEmpty());
  }

  @Test
  public void testGetLatestInstant() {
    DoubleTimeSeries empty = createEmptyTimeSeries();
    DoubleTimeSeries dts = createStandardTimeSeries();
    assertEquals(6L, dts.getLatestTime().toInstant().toEpochMillis());
    try {
      empty.getLatestTime();
    } catch (NoSuchElementException nsee) {
      return;
    }
    fail();
  }

  @Test
  public void testGetLatestValue() {
    DoubleTimeSeries empty = createEmptyTimeSeries();
    DoubleTimeSeries dts = createStandardTimeSeries();
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
    DoubleTimeSeries empty = createEmptyTimeSeries();
    DoubleTimeSeries dts = createStandardTimeSeries();
    assertEquals(1L, dts.getEarliestTime().toInstant().toEpochMillis());
    try {
      empty.getEarliestTime();
    } catch (NoSuchElementException nsee) {
      return;
    }
    fail();    
  }

  @Test
  public void testGetEarliestValue() {
    DoubleTimeSeries empty = createEmptyTimeSeries();
    DoubleTimeSeries dts = createStandardTimeSeries();
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
    for (long i=1; i<=6.0; i+=1.0d) {
      assertTrue(dtsTimesIter.hasNext());
      ZonedDateTime time = dtsTimesIter.next();
      assertEquals(i, time.toInstant().toEpochMillis());
      assertEquals(TimeZone.UTC, time.getZone());
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
    for (double i=1; i<=6.0d; i+=1.0d) {
      assertTrue(dtsIter.hasNext());
      Entry<ZonedDateTime, Double> entry = dtsIter.next();
      ZonedDateTime time = entry.getKey();
      CompareUtils.closeEquals(entry.getValue(), i);
      assertEquals((long)i, time.toInstant().toEpochMillis());
      assertEquals(TimeZone.UTC, time.getZone());
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
    DoubleTimeSeries emptyTS = createEmptyTimeSeries();
    DoubleTimeSeries dts = createStandardTimeSeries();
    for (int i=0; i<6; i++) {
      Double val = dts.getValue(ZonedDateTime.fromInstant(Instant.millisInstant((long)i+1), TimeZone.UTC));
      CompareUtils.closeEquals(val, i+1);
      val = dts.getValue(i);
      CompareUtils.closeEquals(val, i+1);
    }
    emptyTS.getValue(0);
  }

  @SuppressWarnings("cast")
  @Test
  public void testSubSeriesInstantProviderInstantProvider() {
    DoubleTimeSeries emptyTS = createEmptyTimeSeries();
    DoubleTimeSeries dts = createStandardTimeSeries();
    DoubleTimeSeries threeToFive = dts.subSeries(ZonedDateTime.fromInstant(Instant.millisInstant(3), TimeZone.UTC), ZonedDateTime.fromInstant(Instant.millisInstant(5), TimeZone.UTC));
    assertEquals(3, threeToFive.size());
    Iterator<Entry<ZonedDateTime, Double>> iterator = threeToFive.iterator();
    for (int i=3; i<=5; i++) {
      Entry<ZonedDateTime, Double> item = iterator.next();
      assertEquals(Instant.millisInstant((long)i), item.getKey().toInstant());
      assertTrue(CompareUtils.closeEquals((double)i, item.getValue()));
      assertEquals(TimeZone.UTC, item.getKey().getZone());
    }
    assertEquals(4, dts.subSeries(ZonedDateTime.fromInstant(Instant.millisInstant(0), TimeZone.UTC), ZonedDateTime.fromInstant(Instant.millisInstant(4), TimeZone.UTC)).size());
    assertEquals(3, dts.subSeries(ZonedDateTime.fromInstant(Instant.millisInstant(4), TimeZone.UTC), ZonedDateTime.fromInstant(Instant.millisInstant(7), TimeZone.UTC)).size());
    assertEquals(emptyTS, emptyTS.subSeries(ZonedDateTime.fromInstant(Instant.millisInstant(1), TimeZone.UTC), ZonedDateTime.fromInstant(Instant.millisInstant(1), TimeZone.UTC)));
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

}
