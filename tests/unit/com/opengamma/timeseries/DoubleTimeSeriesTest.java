package com.opengamma.timeseries;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.Map.Entry;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.junit.Test;

import com.opengamma.util.CompareUtils;

public abstract class DoubleTimeSeriesTest {
  
  public abstract DoubleTimeSeries createEmptyTimeSeries();
  public abstract DoubleTimeSeries createTimeSeries(long[] times, double[] values);
  public abstract DoubleTimeSeries createTimeSeries(List<InstantProvider> times, List<Double> values);
  public abstract DoubleTimeSeries createTimeSeries(DoubleTimeSeries dts);
  public abstract DoubleTimeSeries createTimeSeries(SortedMap<InstantProvider, Double> initialMap);

  @Test
  public void testArrayConstructor() {
    DoubleTimeSeries dts = createTimeSeries(new long[0], new double[0]);
    assertEquals(0, dts.size());
    long[] times = {1, 2, 3, 4, 5, 6};
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
    DoubleTimeSeries dts = createTimeSeries(new ArrayList<InstantProvider>(), new ArrayList<Double>());
    assertEquals(0, dts.size());
    long[] times = {1, 2, 3, 4, 5, 6};
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    List<InstantProvider> timesList = new ArrayList<InstantProvider>();
    List<Double> valuesList = new ArrayList<Double>();
    for (int i=0; i<times.length; i++) {
      timesList.add(Instant.millisInstant(times[i]));
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
    dts = createTimeSeries(times, values);
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
    return createTimeSeries(times, values);
  }
  
  @Test
  public void testHead() {
    DoubleTimeSeries dts = createStandardTimeSeries();
    DoubleTimeSeries head5 = (DoubleTimeSeries) dts.head(5);
    Iterator<Entry<InstantProvider, Double>> iterator = head5.iterator();
    for (long i=1; i<=5; i++) {
      Entry<InstantProvider, Double> entry = iterator.next();
      assertEquals(i, entry.getKey().toInstant().toEpochMillis());
      assertEquals(Double.valueOf(i), entry.getValue());
    }
    assertEquals(dts.head(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().head(0), createEmptyTimeSeries());
  }
  @Test
  public void testTail() {
    DoubleTimeSeries dts = createStandardTimeSeries();
    DoubleTimeSeries head5 = (DoubleTimeSeries) dts.tail(5);
    Iterator<Entry<InstantProvider, Double>> iterator = head5.iterator();
    for (long i=2; i<=6; i++) {
      Entry<InstantProvider, Double> entry = iterator.next();
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
    assertEquals(6L, dts.getLatestInstant().toInstant().toEpochMillis());
    try {
      empty.getLatestInstant();
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
    assertEquals(1L, dts.getEarliestInstant().toInstant().toEpochMillis());
    try {
      empty.getEarliestInstant();
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
    Iterator<InstantProvider> emptyTimesIter = createEmptyTimeSeries().timeIterator();
    Iterator<InstantProvider> dtsTimesIter = createStandardTimeSeries().timeIterator();
    for (long i=1; i<=6.0; i+=1.0d) {
      assertTrue(dtsTimesIter.hasNext());
      InstantProvider instant = dtsTimesIter.next();
      assertEquals(i, instant.toInstant().toEpochMillis());
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
    Iterator<Entry<InstantProvider, Double>> emptyIter = createEmptyTimeSeries().iterator();
    Iterator<Entry<InstantProvider, Double>> dtsIter = createStandardTimeSeries().iterator();
    for (double i=1; i<=6.0d; i+=1.0d) {
      assertTrue(dtsIter.hasNext());
      Entry<InstantProvider, Double> entry = dtsIter.next();
      CompareUtils.closeEquals(entry.getValue(), i);
      assertEquals((long)i, entry.getKey().toInstant().toEpochMillis());
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
      Double val = dts.getDataPoint(Instant.millisInstant((long)i+1));
      CompareUtils.closeEquals(val, i+1);
      val = dts.getDataPoint(i);
      CompareUtils.closeEquals(val, i+1);
    }
    emptyTS.getDataPoint(0);
  }

  @Test
  public void testSubSeriesInstantProviderInstantProvider() {
    DoubleTimeSeries emptyTS = createEmptyTimeSeries();
    DoubleTimeSeries dts = createStandardTimeSeries();
    DoubleTimeSeries threeToFive = dts.subSeries(Instant.millisInstant(3), Instant.millisInstant(5));
    assertEquals(3, threeToFive.size());
    Iterator<Entry<InstantProvider, Double>> iterator = threeToFive.iterator();
    for (int i=3; i<=5; i++) {
      Entry<InstantProvider, Double> item = iterator.next();
      assertEquals(Instant.millisInstant((long)i), item.getKey().toInstant());
      assertTrue(CompareUtils.closeEquals((double)i, item.getValue()));
    }
    assertEquals(4, dts.subSeries(Instant.millisInstant(0), Instant.millisInstant(4)).size());
    assertEquals(3, dts.subSeries(Instant.millisInstant(4), Instant.millisInstant(7)).size());
    assertEquals(emptyTS, emptyTS.subSeries(Instant.millisInstant(1), Instant.millisInstant(1)));
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
