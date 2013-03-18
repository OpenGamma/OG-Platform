/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public abstract class DoubleTimeSeriesTest<E> {

  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(DoubleTimeSeriesTest.class);

  protected abstract DoubleTimeSeries<E> createEmptyTimeSeries();
  protected abstract DoubleTimeSeries<E> createTimeSeries(E[] times, double[] values);
  protected abstract DoubleTimeSeries<E> createTimeSeries(List<E> times, List<Double> values);
  protected abstract DoubleTimeSeries<E> createTimeSeries(DoubleTimeSeries<E> dts);

  protected abstract E[] emptyTimes();
  protected abstract E[] testTimes();
  protected abstract E[] testTimes2();

  @Test
  public void testArrayConstructor() {
    DoubleTimeSeries<E> dts = createTimeSeries(emptyTimes(), new double[0]);
    AssertJUnit.assertEquals(0, dts.size());
    final E[] times = testTimes();
    final double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    dts = createTimeSeries(times, values);
    AssertJUnit.assertEquals(6, dts.size());
    final Iterator<Double> valuesIter = dts.valuesIterator();
    for (double i=1.0; i<=6.0; i+=1.0) {
      AssertJUnit.assertTrue(CompareUtils.closeEquals(i, valuesIter.next()));
    }
  }

  @Test
  public void testListConstructor() {
    DoubleTimeSeries<E> dts = createTimeSeries(new ArrayList<E>(), new ArrayList<Double>());
    AssertJUnit.assertEquals(0, dts.size());
    final E[] times = testTimes();
    final double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    final List<E> timesList = new ArrayList<E>();
    final List<Double> valuesList = new ArrayList<Double>();
    for (int i=0; i<times.length; i++) {
      timesList.add(times[i]);
      valuesList.add(values[i]);
    }
    dts = createTimeSeries(timesList, valuesList);
    AssertJUnit.assertEquals(6, dts.size());
    final Iterator<Double> valuesIter = dts.valuesIterator();
    for (double i=1.0; i<=6.0; i+=1.0) {
      AssertJUnit.assertTrue(CompareUtils.closeEquals(i, valuesIter.next()));
    }
  }

  @Test
  public void testTimeSeriesConstructor() {
    DoubleTimeSeries<E> dts = createEmptyTimeSeries();
    DoubleTimeSeries<E> dts2 = createTimeSeries(dts);
    AssertJUnit.assertEquals(0, dts2.size());
    final E[] times = testTimes();
    final double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    dts = createTimeSeries(times, values);
    dts2 = createTimeSeries(dts);
    AssertJUnit.assertEquals(6, dts2.size());
    final Iterator<Double> valuesIter = dts2.valuesIterator();
    for (double i=1.0; i<=6.0; i+=1.0) {
      AssertJUnit.assertTrue(CompareUtils.closeEquals(i, valuesIter.next()));
    }
  }

  @Test
  public void testMutabilityOrdering() {
    final DoubleTimeSeries<E> dts = createEmptyTimeSeries();
    if (dts instanceof MutableDoubleTimeSeries) {
      final MutableDoubleTimeSeries<E> mts = (MutableDoubleTimeSeries<E>) dts;
      final List<E> times = Arrays.asList(testTimes());
      for (int test = 0; test < 100; test++) {
        Collections.shuffle(times);
        @SuppressWarnings("unchecked")
        final E[] shuffledTimes = (E[]) times.toArray();
        for (int i=0; i < times.size(); i++) {
          mts.putDataPoint(shuffledTimes[i], Double.valueOf(i));
        }
        Assert.assertEquals(mts.timesArray(), testTimes());
      }
    }
  }

  protected DoubleTimeSeries<E> createStandardTimeSeries() {
    final E[] times = testTimes();
    final double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    return createTimeSeries(times, values);
  }

  protected DoubleTimeSeries<E> createStandardTimeSeries2() {
    final E[] times = testTimes2();
    final double[] values = {4.0, 5.0, 6.0, 7.0, 8.0, 9.0};
    return createTimeSeries(times, values);
  }

  @Test
  public void testHead() {
    final DoubleTimeSeries<E> dts = createStandardTimeSeries();
    final DoubleTimeSeries<E> head5 = dts.head(5);
    final Iterator<Entry<E, Double>> iterator = head5.iterator();
    for (int i=0; i<5; i++) {
      final Entry<E, Double> entry = iterator.next();
      AssertJUnit.assertEquals(testTimes()[i], entry.getKey());
      AssertJUnit.assertEquals(Double.valueOf(i+1), entry.getValue());
    }
    AssertJUnit.assertEquals(dts.head(0), createEmptyTimeSeries());
    AssertJUnit.assertEquals(createEmptyTimeSeries().head(0), createEmptyTimeSeries());
  }

  @Test
  public void testTail() {
    final DoubleTimeSeries<E> dts = createStandardTimeSeries();
    final DoubleTimeSeries<E> tail5 = dts.tail(5);
    final Iterator<Entry<E, Double>> iterator = tail5.iterator();
    for (int i=1; i<6; i++) {
      final Entry<E, Double> entry = iterator.next();
      AssertJUnit.assertEquals(testTimes()[i], entry.getKey());
      AssertJUnit.assertEquals(Double.valueOf(i+1), entry.getValue());
    }
    AssertJUnit.assertEquals(dts.tail(0), createEmptyTimeSeries());
    AssertJUnit.assertEquals(createEmptyTimeSeries().tail(0), createEmptyTimeSeries());
  }

  @Test
  public void testSize() {
    final DoubleTimeSeries<E> dts = createStandardTimeSeries();
    AssertJUnit.assertEquals(6, dts.size());
    final DoubleTimeSeries<E> emptyTS = createEmptyTimeSeries();
    AssertJUnit.assertEquals(0, emptyTS.size());
  }

  @Test
  public void testIsEmpty() {
    final DoubleTimeSeries<E> empty = createEmptyTimeSeries();
    final DoubleTimeSeries<E> dts = createStandardTimeSeries();
    AssertJUnit.assertTrue(empty.isEmpty());
    AssertJUnit.assertFalse(dts.isEmpty());
  }

  @Test
  public void testGetLatestInstant() {
    final DoubleTimeSeries<E> empty = createEmptyTimeSeries();
    final DoubleTimeSeries<E> dts = createStandardTimeSeries();
    final E[] testDates = testTimes();
    AssertJUnit.assertEquals(testDates[5], dts.getLatestTime());
    try {
      empty.getLatestTime();
    } catch (final NoSuchElementException nsee) {
      return;
    }
    Assert.fail();
  }

  @Test
  public void testGetLatestValue() {
    final DoubleTimeSeries<E> empty = createEmptyTimeSeries();
    final DoubleTimeSeries<E> dts = createStandardTimeSeries();
    AssertJUnit.assertTrue(CompareUtils.closeEquals(6.0d, dts.getLatestValue()));
    try {
      empty.getLatestValue();
    } catch (final NoSuchElementException nsee) {
      return;
    }
    Assert.fail();
  }

  @Test
  public void testGetEarliestInstant() {
    final DoubleTimeSeries<E> empty = createEmptyTimeSeries();
    final DoubleTimeSeries<E> dts = createStandardTimeSeries();
    final E[] testDates = testTimes();
    AssertJUnit.assertEquals(testDates[0], dts.getEarliestTime());
    try {
      empty.getEarliestTime();
    } catch (final NoSuchElementException nsee) {
      return;
    }
    Assert.fail();
  }

  @Test
  public void testGetEarliestValue() {
    final DoubleTimeSeries<E> empty = createEmptyTimeSeries();
    final DoubleTimeSeries<E> dts = createStandardTimeSeries();
    AssertJUnit.assertTrue(CompareUtils.closeEquals(1d, dts.getEarliestValue()));
    try {
      empty.getEarliestValue();
    } catch (final NoSuchElementException nsee) {
      return;
    }
    Assert.fail();
  }

  @Test
  public void testValuesIterator() {
    final Iterator<Double> emptyValuesIter = createEmptyTimeSeries().valuesIterator();
    final Iterator<Double> dtsValuesIter = createStandardTimeSeries().valuesIterator();
    for (double i=1; i<=6.0; i+=1.0d) {
      AssertJUnit.assertTrue(dtsValuesIter.hasNext());
      final Double val = dtsValuesIter.next();
      CompareUtils.closeEquals(val, i);
    }
    try {
      dtsValuesIter.next();
    } catch (final NoSuchElementException nsee) {
      AssertJUnit.assertFalse(emptyValuesIter.hasNext());
      try {
        emptyValuesIter.next();
      } catch (final NoSuchElementException nsuchee) {
        return;
      }
    }
    Assert.fail();
  }

  @Test
  public void testTimeIterator() {
    final Iterator<E> emptyTimesIter = createEmptyTimeSeries().timeIterator();
    final Iterator<E> dtsTimesIter = createStandardTimeSeries().timeIterator();
    final E[] testDates = testTimes();
    for (int i=0; i<6; i++) {
      AssertJUnit.assertTrue(dtsTimesIter.hasNext());
      final E time = dtsTimesIter.next();
      AssertJUnit.assertEquals(testDates[i], time);
    }
    try {
      dtsTimesIter.next();
    } catch (final NoSuchElementException nsee) {
      AssertJUnit.assertFalse(emptyTimesIter.hasNext());
      try {
        emptyTimesIter.next();
      } catch (final NoSuchElementException nsuchee) {
        return;
      }
    }
    Assert.fail();
  }

  @Test
  public void testIterator() {
    final Iterator<Entry<E, Double>> emptyIter = createEmptyTimeSeries().iterator();
    final Iterator<Entry<E, Double>> dtsIter = createStandardTimeSeries().iterator();
    final E[] testDates = testTimes();
    for (int i=0; i<6; i++) {
      AssertJUnit.assertTrue(dtsIter.hasNext());
      final Entry<E, Double> entry = dtsIter.next();
      final E time = entry.getKey();
      CompareUtils.closeEquals(entry.getValue(), (double)i+1);
      AssertJUnit.assertEquals(testDates[i], time);
    }
    try {
      dtsIter.next();
    } catch (final NoSuchElementException nsee) {
      AssertJUnit.assertFalse(emptyIter.hasNext());
      try {
        emptyIter.next();
      } catch (final NoSuchElementException nsuchee) {
        return;
      }
    }
    Assert.fail();
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void testGetDataPoint() {
    final DoubleTimeSeries<E> emptyTS = createEmptyTimeSeries();
    final DoubleTimeSeries<E> dts = createStandardTimeSeries();
    final E[] testDates = testTimes();
    for (int i=0; i<6; i++) {
      Double val = dts.getValue(testDates[i]);
      CompareUtils.closeEquals(val, i+1);
      val = dts.getValueAt(i);
      CompareUtils.closeEquals(val, i+1);
    }
    emptyTS.getValueAt(0);
  }

  @Test
  @SuppressWarnings("cast")
  public void testSubSeriesInstantProviderInstantProvider() {
    final DoubleTimeSeries<E> emptyTS = createEmptyTimeSeries();
    final DoubleTimeSeries<E> dts = createStandardTimeSeries();
    final E[] testDates = testTimes();
    final DoubleTimeSeries<E> threeToFive = dts.subSeries(testDates[3], testDates[5]);
    AssertJUnit.assertEquals(2, threeToFive.size());
    final Iterator<Entry<E, Double>> iterator = threeToFive.iterator();
    for (int i=3; i<5; i++) {
      final Entry<E, Double> item = iterator.next();
      AssertJUnit.assertEquals(testDates[i], item.getKey());
      AssertJUnit.assertTrue(CompareUtils.closeEquals((double)i+1, item.getValue()));
    }
    AssertJUnit.assertEquals(4, dts.subSeries(testDates[0], testDates[4]).size());
    AssertJUnit.assertEquals(5, dts.subSeries(testDates[0], true, testDates[4], true).size());
    AssertJUnit.assertEquals(4, dts.subSeries(testDates[0], true, testDates[4], false).size());
    AssertJUnit.assertEquals(1, dts.subSeries(testDates[4], testDates[5]).size());
    AssertJUnit.assertEquals(1, dts.subSeries(testDates[4], false, testDates[5], true).size());
    AssertJUnit.assertEquals(0, dts.subSeries(testDates[5], true, testDates[5], false).size());
    AssertJUnit.assertEquals(emptyTS, emptyTS.subSeries(testDates[1], testDates[1]));
  }

  @Test
  public void testHashCode() {
    AssertJUnit.assertEquals(createStandardTimeSeries().hashCode(), createStandardTimeSeries().hashCode());
    AssertJUnit.assertEquals(createEmptyTimeSeries().hashCode(), createEmptyTimeSeries().hashCode());
  }

  @Test
  public void testEquals() {
    AssertJUnit.assertEquals(createStandardTimeSeries(), createStandardTimeSeries());
    AssertJUnit.assertFalse(createStandardTimeSeries().equals(createEmptyTimeSeries()));
    AssertJUnit.assertFalse(createEmptyTimeSeries().equals(createStandardTimeSeries()));
    AssertJUnit.assertEquals(createEmptyTimeSeries(), createEmptyTimeSeries());
    //    FastBackedDoubleTimeSeries<E> createStandardTimeSeries = (FastBackedDoubleTimeSeries<E>) createStandardTimeSeries();
    //    FastBackedDoubleTimeSeries<E> createStandardTimeSeries2 = (FastBackedDoubleTimeSeries<E>) (createStandardTimeSeries().toDateTimeDoubleTimeSeries());
    //    s_logger.info(createStandardTimeSeries.getFastSeries().toString());
    //    s_logger.info(createStandardTimeSeries2.getFastSeries().toString());
    //    assertEquals(createStandardTimeSeries.getFastSeries(), createStandardTimeSeries2.getFastSeries());
    //    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toDateDoubleTimeSeries());
    //    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toMutableDateDoubleTimeSeries());
    //    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toDateTimeDoubleTimeSeries());
    //    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toMutableDateTimeDoubleTimeSeries());
    //    try {
    //      assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toFastIntDoubleTimeSeries());
    //      assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toFastMutableIntDoubleTimeSeries());
    //    } catch (OpenGammaRuntimeException ogre) {
    //      // some combinations of classes don't support converting to fast int time series (e.g. things with millis precision).
    //    }
    //    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toFastLongDoubleTimeSeries());
    //    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toFastMutableLongDoubleTimeSeries());
    //    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toLocalDateDoubleTimeSeries());
    //    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toMutableLocalDateDoubleTimeSeries());
    //    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toZonedDateTimeDoubleTimeSeries());
    //    assertEquals(createStandardTimeSeries(), createStandardTimeSeries().toMutableZonedDateTimeDoubleTimeSeries());
  }

  @Test
  public void testOperators() {
    final DoubleTimeSeries<E> dts = createStandardTimeSeries();
    final DoubleTimeSeries<E> dts2 = createStandardTimeSeries2();
    final DoubleTimeSeries<E> ets = createEmptyTimeSeries();
    AssertJUnit.assertEquals(ets, dts.add(ets));
    AssertJUnit.assertEquals(ets, ets.add(dts));
    AssertJUnit.assertEquals(dts, dts.unionAdd(ets));
    AssertJUnit.assertEquals(dts, ets.unionAdd(dts));
    final DoubleTimeSeries<E> result = dts.add(dts2);
    AssertJUnit.assertEquals(3, result.size());
    AssertJUnit.assertEquals(Double.valueOf(8.0), result.getValueAt(0));
    AssertJUnit.assertEquals(Double.valueOf(10.0), result.getValueAt(1));
    AssertJUnit.assertEquals(Double.valueOf(12.0), result.getValueAt(2));
    AssertJUnit.assertEquals(dts.getTimeAt(3), result.getTimeAt(0));
    AssertJUnit.assertEquals(dts.getTimeAt(4), result.getTimeAt(1));
    AssertJUnit.assertEquals(dts.getTimeAt(5), result.getTimeAt(2));
    final DoubleTimeSeries<E> unionResult = dts.unionAdd(dts2);
    AssertJUnit.assertEquals(9, unionResult.size());
    AssertJUnit.assertEquals(Double.valueOf(1.0), unionResult.getValueAt(0));
    AssertJUnit.assertEquals(Double.valueOf(2.0), unionResult.getValueAt(1));
    AssertJUnit.assertEquals(Double.valueOf(3.0), unionResult.getValueAt(2));
    AssertJUnit.assertEquals(Double.valueOf(8.0), unionResult.getValueAt(3));
    AssertJUnit.assertEquals(Double.valueOf(10.0), unionResult.getValueAt(4));
    AssertJUnit.assertEquals(Double.valueOf(12.0), unionResult.getValueAt(5));
    AssertJUnit.assertEquals(Double.valueOf(7.0), unionResult.getValueAt(6));
    AssertJUnit.assertEquals(Double.valueOf(8.0), unionResult.getValueAt(7));
    AssertJUnit.assertEquals(Double.valueOf(9.0), unionResult.getValueAt(8));
    AssertJUnit.assertEquals(dts.getTimeAt(0), unionResult.getTimeAt(0));
    AssertJUnit.assertEquals(dts.getTimeAt(1), unionResult.getTimeAt(1));
    AssertJUnit.assertEquals(dts.getTimeAt(2), unionResult.getTimeAt(2));
    AssertJUnit.assertEquals(dts.getTimeAt(3), unionResult.getTimeAt(3));
    AssertJUnit.assertEquals(dts.getTimeAt(4), unionResult.getTimeAt(4));
    AssertJUnit.assertEquals(dts.getTimeAt(5), unionResult.getTimeAt(5));
    AssertJUnit.assertEquals(dts2.getTimeAt(3), unionResult.getTimeAt(6));
    AssertJUnit.assertEquals(dts2.getTimeAt(4), unionResult.getTimeAt(7));
    AssertJUnit.assertEquals(dts2.getTimeAt(5), unionResult.getTimeAt(8));

    AssertJUnit.assertEquals(dts, ets.noIntersectionOperation(dts));
    AssertJUnit.assertEquals(dts, dts.noIntersectionOperation(ets));
    try {
      dts.noIntersectionOperation(dts2);
      Assert.fail();
    } catch(final OpenGammaRuntimeException ex) {
      //do nothing - expected exception because the two timeseries have overlapping dates which will require intersection operation
    }
    final DoubleTimeSeries<E> dts3 = dts2.subSeries(dts.getLatestTime(), false, dts2.getLatestTime(), false);
    final DoubleTimeSeries<E> noIntersecOp = dts.noIntersectionOperation(dts3);
    AssertJUnit.assertEquals(dts.getValueAt(0), noIntersecOp.getValueAt(0));
    AssertJUnit.assertEquals(dts.getValueAt(1), noIntersecOp.getValueAt(1));
    AssertJUnit.assertEquals(dts.getValueAt(2), noIntersecOp.getValueAt(2));
    AssertJUnit.assertEquals(dts.getValueAt(3), noIntersecOp.getValueAt(3));
    AssertJUnit.assertEquals(dts.getValueAt(4), noIntersecOp.getValueAt(4));
    AssertJUnit.assertEquals(dts.getValueAt(5), noIntersecOp.getValueAt(5));
    AssertJUnit.assertEquals(dts3.getValueAt(0), noIntersecOp.getValueAt(6));
    AssertJUnit.assertEquals(dts3.getValueAt(1), noIntersecOp.getValueAt(7));
  }

  @Test
  public void testScalarOperators() {
    assertOperationSuccessful(createStandardTimeSeries().add(10.0), new double[] {11.0, 12.0, 13.0, 14.0, 15.0, 16.0});
    assertOperationSuccessful(createStandardTimeSeries().subtract(1.0), new double[] {0.0, 1.0, 2.0, 3.0, 4.0, 5.0});
    assertOperationSuccessful(createStandardTimeSeries().multiply(2.0), new double[] {2.0, 4.0, 6.0, 8.0, 10.0, 12.0});
    assertOperationSuccessful(createStandardTimeSeries().divide(2.0), new double[] {0.5, 1.0, 1.5, 2.0, 2.5, 3.0});
    assertOperationSuccessful(createStandardTimeSeries().power(2.0), new double[] {1.0, 4.0, 9.0, 16.0, 25.0, 36.0});
    assertOperationSuccessful(createStandardTimeSeries().minimum(2.0), new double[] {1.0, 2.0, 2.0, 2.0, 2.0, 2.0});
    assertOperationSuccessful(createStandardTimeSeries().maximum(2.5), new double[] {2.5, 2.5, 3.0, 4.0, 5.0, 6.0});
    assertOperationSuccessful(createStandardTimeSeries().average(2.0), new double[] {1.5, 2.0, 2.5, 3.0, 3.5, 4.0});
  }

  @Test
  public void testLagOperator() {
    final DoubleTimeSeries<E> dts = createStandardTimeSeries();
    DoubleTimeSeries<E> lagged = dts.lag(0);
    assertOperationSuccessful(lagged, new double[] {1d, 2d, 3d, 4d, 5d, 6d });
    AssertJUnit.assertEquals(lagged.getEarliestTime(), testTimes()[0]);
    AssertJUnit.assertEquals(lagged.getLatestTime(), testTimes()[5]);
    AssertJUnit.assertEquals(dts, lagged);
    lagged = dts.lag(1);
    assertOperationSuccessful(lagged, new double[] {1d, 2d, 3d, 4d, 5d });
    AssertJUnit.assertEquals(lagged.getEarliestTime(), testTimes()[1]);
    AssertJUnit.assertEquals(lagged.getLatestTime(), testTimes()[5]);
    lagged = dts.lag(-1);
    assertOperationSuccessful(lagged, new double[] {2d, 3d, 4d, 5d, 6d });
    AssertJUnit.assertEquals(lagged.getEarliestTime(), testTimes()[0]);
    AssertJUnit.assertEquals(lagged.getLatestTime(), testTimes()[4]);
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

  @Test
  public void testFudgeSerialization() {
    final FudgeMsg msg = OpenGammaFudgeContext.getInstance().toFudgeMsg(createStandardTimeSeries()).getMessage();
    // FudgeMsgFormatter.outputToSystemOut(msg);
    final Object o = OpenGammaFudgeContext.getInstance().fromFudgeMsg(msg);
    AssertJUnit.assertEquals(createStandardTimeSeries(), o);
  }

  protected static void assertOperationSuccessful(final DoubleTimeSeries<?> result, final double[] expected) {
    AssertJUnit.assertEquals(expected.length, result.size());
    for (int i = 0; i < expected.length; i++) {
      AssertJUnit.assertEquals(expected[i], result.getValueAt(i), 0.001);
    }
  }


}
