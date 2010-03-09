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

import org.junit.Ignore;
import org.junit.Test;

import com.opengamma.util.CompareUtils;

@Ignore
public abstract class DoubleTimeSeriesTestCase {

  public abstract DoubleTimeSeries createEmptyTimeSeries();

  public abstract DoubleTimeSeries createTimeSeries(long[] times, double[] values, TimeZone[] zones);

  public abstract DoubleTimeSeries createTimeSeries(List<ZonedDateTime> times, List<Double> values);

  public abstract DoubleTimeSeries createTimeSeries(DoubleTimeSeries dts);

  public abstract DoubleTimeSeries createTimeSeries(SortedMap<ZonedDateTime, Double> initialMap);

  @Test
  public void testArrayConstructor() {
    DoubleTimeSeries dts = createTimeSeries(new long[0], new double[0], new TimeZone[0]);
    assertEquals(0, dts.size());
    final long[] times = { 1, 2, 3, 4, 5, 6 };
    final double[] values = { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
    final TimeZone[] zones = new TimeZone[6];
    for (int i = 0; i < 6; i++) {
      zones[i] = TimeZone.UTC;
    }
    dts = createTimeSeries(times, values, zones);
    assertEquals(6, dts.size());
    final Iterator<Double> valuesIter = dts.valuesIterator();
    for (double i = 1.0; i <= 6.0; i += 1.0) {
      assertTrue(CompareUtils.closeEquals(i, valuesIter.next()));
    }
  }

  @Test
  public void testListConstructor() {
    DoubleTimeSeries dts = createTimeSeries(new ArrayList<ZonedDateTime>(), new ArrayList<Double>());
    assertEquals(0, dts.size());
    final long[] times = { 1, 2, 3, 4, 5, 6 };
    final double[] values = { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
    final List<ZonedDateTime> timesList = new ArrayList<ZonedDateTime>();
    final List<Double> valuesList = new ArrayList<Double>();
    for (int i = 0; i < times.length; i++) {
      timesList.add(ZonedDateTime.fromInstant(Instant.millis(times[i]), TimeZone.UTC));
      valuesList.add(values[i]);
    }
    dts = createTimeSeries(timesList, valuesList);
    assertEquals(6, dts.size());
    final Iterator<Double> valuesIter = dts.valuesIterator();
    for (double i = 1.0; i <= 6.0; i += 1.0) {
      assertTrue(CompareUtils.closeEquals(i, valuesIter.next()));
    }
  }

  @Test
  public void testTimeSeriesConstructor() {
    DoubleTimeSeries dts = createEmptyTimeSeries();
    DoubleTimeSeries dts2 = createTimeSeries(dts);
    assertEquals(0, dts2.size());
    final long[] times = { 1, 2, 3, 4, 5, 6 };
    final double[] values = { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
    final TimeZone[] zones = new TimeZone[6];
    for (int i = 0; i < 6; i++) {
      zones[i] = TimeZone.UTC;
    }
    dts = createTimeSeries(times, values, zones);
    dts2 = createTimeSeries(dts);
    assertEquals(6, dts2.size());
    final Iterator<Double> valuesIter = dts2.valuesIterator();
    for (double i = 1.0; i <= 6.0; i += 1.0) {
      assertTrue(CompareUtils.closeEquals(i, valuesIter.next()));
    }
  }

  public DoubleTimeSeries createStandardTimeSeries() {
    final long[] times = { 1, 2, 3, 4, 5, 6 };
    final double[] values = { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
    final TimeZone[] zones = { TimeZone.UTC, TimeZone.UTC, TimeZone.UTC, TimeZone.UTC, TimeZone.UTC, TimeZone.UTC };
    return createTimeSeries(times, values, zones);
  }

  public DoubleTimeSeries createStandardTimeSeries2() {
    final long[] times = { 4, 5, 6, 7, 8, 9 };
    final double[] values = { 4.0, 5.0, 6.0, 7.0, 8.0, 9.0 };
    final TimeZone[] zones = { TimeZone.UTC, TimeZone.UTC, TimeZone.UTC, TimeZone.UTC, TimeZone.UTC, TimeZone.UTC };
    return createTimeSeries(times, values, zones);
  }

  @Test
  public void testHead() {
    final DoubleTimeSeries dts = createStandardTimeSeries();
    final DoubleTimeSeries head5 = (DoubleTimeSeries) dts.head(5);
    final Iterator<Entry<ZonedDateTime, Double>> iterator = head5.iterator();
    for (long i = 1; i <= 5; i++) {
      final Entry<ZonedDateTime, Double> entry = iterator.next();
      assertEquals(i, entry.getKey().toInstant().toEpochMillisLong());
      assertEquals(Double.valueOf(i), entry.getValue());
    }
    assertEquals(dts.head(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().head(0), createEmptyTimeSeries());
  }

  @Test
  public void testTail() {
    final DoubleTimeSeries dts = createStandardTimeSeries();
    final DoubleTimeSeries tail5 = (DoubleTimeSeries) dts.tail(5);
    final Iterator<Entry<ZonedDateTime, Double>> iterator = tail5.iterator();
    for (long i = 2; i <= 6; i++) {
      final Entry<ZonedDateTime, Double> entry = iterator.next();
      assertEquals(i, entry.getKey().toInstant().toEpochMillisLong());
      assertEquals(Double.valueOf(i), entry.getValue());
    }
    assertEquals(dts.tail(0), createEmptyTimeSeries());
    assertEquals(createEmptyTimeSeries().tail(0), createEmptyTimeSeries());
  }

  @Test
  public void testSize() {
    final DoubleTimeSeries dts = createStandardTimeSeries();
    assertEquals(6, dts.size());
    final DoubleTimeSeries emptyTS = createEmptyTimeSeries();
    assertEquals(0, emptyTS.size());
  }

  @Test
  public void testIsEmpty() {
    final DoubleTimeSeries empty = createEmptyTimeSeries();
    final DoubleTimeSeries dts = createStandardTimeSeries();
    assertTrue(empty.isEmpty());
    assertFalse(dts.isEmpty());
  }

  @Test
  public void testGetLatestInstant() {
    final DoubleTimeSeries empty = createEmptyTimeSeries();
    final DoubleTimeSeries dts = createStandardTimeSeries();
    assertEquals(6L, dts.getLatestTime().toInstant().toEpochMillisLong());
    try {
      empty.getLatestTime();
    } catch (final NoSuchElementException nsee) {
      return;
    }
    fail();
  }

  @Test
  public void testGetLatestValue() {
    final DoubleTimeSeries empty = createEmptyTimeSeries();
    final DoubleTimeSeries dts = createStandardTimeSeries();
    assertTrue(CompareUtils.closeEquals(6.0d, dts.getLatestValue()));
    try {
      empty.getLatestValue();
    } catch (final NoSuchElementException nsee) {
      return;
    }
    fail();
  }

  @Test
  public void testGetEarliestInstant() {
    final DoubleTimeSeries empty = createEmptyTimeSeries();
    final DoubleTimeSeries dts = createStandardTimeSeries();
    assertEquals(1L, dts.getEarliestTime().toInstant().toEpochMillisLong());
    try {
      empty.getEarliestTime();
    } catch (final NoSuchElementException nsee) {
      return;
    }
    fail();
  }

  @Test
  public void testGetEarliestValue() {
    final DoubleTimeSeries empty = createEmptyTimeSeries();
    final DoubleTimeSeries dts = createStandardTimeSeries();
    assertTrue(CompareUtils.closeEquals(1d, dts.getEarliestValue()));
    try {
      empty.getEarliestValue();
    } catch (final NoSuchElementException nsee) {
      return;
    }
    fail();
  }

  @Test
  public void testValuesIterator() {
    final Iterator<Double> emptyValuesIter = createEmptyTimeSeries().valuesIterator();
    final Iterator<Double> dtsValuesIter = createStandardTimeSeries().valuesIterator();
    for (double i = 1; i <= 6.0; i += 1.0d) {
      assertTrue(dtsValuesIter.hasNext());
      final Double val = dtsValuesIter.next();
      CompareUtils.closeEquals(val, i);
    }
    try {
      dtsValuesIter.next();
    } catch (final NoSuchElementException nsee) {
      assertFalse(emptyValuesIter.hasNext());
      try {
        emptyValuesIter.next();
      } catch (final NoSuchElementException nsuchee) {
        return;
      }
    }
    fail();
  }

  @Test
  public void testTimeIterator() {
    final Iterator<ZonedDateTime> emptyTimesIter = createEmptyTimeSeries().timeIterator();
    final Iterator<ZonedDateTime> dtsTimesIter = createStandardTimeSeries().timeIterator();
    for (long i = 1; i <= 6.0; i += 1.0d) {
      assertTrue(dtsTimesIter.hasNext());
      final ZonedDateTime time = dtsTimesIter.next();
      assertEquals(i, time.toInstant().toEpochMillisLong());
      assertEquals(TimeZone.UTC, time.getZone());
    }
    try {
      dtsTimesIter.next();
    } catch (final NoSuchElementException nsee) {
      assertFalse(emptyTimesIter.hasNext());
      try {
        emptyTimesIter.next();
      } catch (final NoSuchElementException nsuchee) {
        return;
      }
    }
    fail();
  }

  @Test
  public void testIterator() {
    final Iterator<Entry<ZonedDateTime, Double>> emptyIter = createEmptyTimeSeries().iterator();
    final Iterator<Entry<ZonedDateTime, Double>> dtsIter = createStandardTimeSeries().iterator();
    for (double i = 1; i <= 6.0d; i += 1.0d) {
      assertTrue(dtsIter.hasNext());
      final Entry<ZonedDateTime, Double> entry = dtsIter.next();
      final ZonedDateTime time = entry.getKey();
      CompareUtils.closeEquals(entry.getValue(), i);
      assertEquals((long) i, time.toInstant().toEpochMillisLong());
      assertEquals(TimeZone.UTC, time.getZone());
    }
    try {
      dtsIter.next();
    } catch (final NoSuchElementException nsee) {
      assertFalse(emptyIter.hasNext());
      try {
        emptyIter.next();
      } catch (final NoSuchElementException nsuchee) {
        return;
      }
    }
    fail();
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testGetDataPoint() {
    final DoubleTimeSeries emptyTS = createEmptyTimeSeries();
    final DoubleTimeSeries dts = createStandardTimeSeries();
    for (int i = 0; i < 6; i++) {
      Double val = dts.getValue(ZonedDateTime.fromInstant(Instant.millis((long) i + 1), TimeZone.UTC));
      CompareUtils.closeEquals(val, i + 1);
      val = dts.getValue(i);
      CompareUtils.closeEquals(val, i + 1);
    }
    emptyTS.getValue(0);
  }

  @SuppressWarnings("cast")
  @Test
  public void testSubSeriesInstantProviderInstantProvider() {
    final DoubleTimeSeries emptyTS = createEmptyTimeSeries();
    final DoubleTimeSeries dts = createStandardTimeSeries();
    final DoubleTimeSeries threeToFive = dts.subSeries(ZonedDateTime.fromInstant(Instant.millis(3), TimeZone.UTC), ZonedDateTime.fromInstant(Instant.millis(5), TimeZone.UTC));
    assertEquals(3, threeToFive.size());
    final Iterator<Entry<ZonedDateTime, Double>> iterator = threeToFive.iterator();
    for (int i = 3; i <= 5; i++) {
      final Entry<ZonedDateTime, Double> item = iterator.next();
      assertEquals(Instant.millis((long) i), item.getKey().toInstant());
      assertTrue(CompareUtils.closeEquals((double) i, item.getValue()));
      assertEquals(TimeZone.UTC, item.getKey().getZone());
    }
    assertEquals(4, dts.subSeries(ZonedDateTime.fromInstant(Instant.millis(0), TimeZone.UTC), ZonedDateTime.fromInstant(Instant.millis(4), TimeZone.UTC)).size());
    assertEquals(3, dts.subSeries(ZonedDateTime.fromInstant(Instant.millis(4), TimeZone.UTC), ZonedDateTime.fromInstant(Instant.millis(7), TimeZone.UTC)).size());
    assertEquals(emptyTS, emptyTS.subSeries(ZonedDateTime.fromInstant(Instant.millis(1), TimeZone.UTC), ZonedDateTime.fromInstant(Instant.millis(1), TimeZone.UTC)));
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
    final DoubleTimeSeries dts = createStandardTimeSeries();
    final DoubleTimeSeries dts2 = createStandardTimeSeries2();
    final DoubleTimeSeries ets = createEmptyTimeSeries();
    assertEquals(ets, DoubleTimeSeriesOperations.add(dts, ets));
    assertEquals(ets, DoubleTimeSeriesOperations.add(ets, dts));
    assertEquals(dts, DoubleTimeSeriesOperations.unionAdd(dts, ets));
    assertEquals(dts, DoubleTimeSeriesOperations.unionAdd(ets, dts));
    final DoubleTimeSeries result = DoubleTimeSeriesOperations.add(dts, dts2);
    assertEquals(3, result.size());
    assertEquals(Double.valueOf(8.0), result.getValue(0));
    assertEquals(Double.valueOf(10.0), result.getValue(1));
    assertEquals(Double.valueOf(12.0), result.getValue(2));
    assertEquals(dts.getTime(3), result.getTime(0));
    assertEquals(dts.getTime(4), result.getTime(1));
    assertEquals(dts.getTime(5), result.getTime(2));
    final DoubleTimeSeries unionResult = DoubleTimeSeriesOperations.unionAdd(dts, dts2);
    assertEquals(9, unionResult.size());
    assertEquals(Double.valueOf(1.0), unionResult.getValue(0));
    assertEquals(Double.valueOf(2.0), unionResult.getValue(1));
    assertEquals(Double.valueOf(3.0), unionResult.getValue(2));
    assertEquals(Double.valueOf(8.0), unionResult.getValue(3));
    assertEquals(Double.valueOf(10.0), unionResult.getValue(4));
    assertEquals(Double.valueOf(12.0), unionResult.getValue(5));
    assertEquals(Double.valueOf(7.0), unionResult.getValue(6));
    assertEquals(Double.valueOf(8.0), unionResult.getValue(7));
    assertEquals(Double.valueOf(9.0), unionResult.getValue(8));
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
