/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 * Test {@link SimpleMapTimeSeries}.
 */
@Test(groups = "unit")
public class SimpleMapTimeSeriesTest {

  private static final LocalDate DATE1 = LocalDate.of(2011, 6, 1);
  private static final LocalDate DATE2 = LocalDate.of(2011, 6, 2);

  public void test_constructor_arrays_emptyTypes() {
    final SimpleMapTimeSeries<LocalDate, String> test = new SimpleMapTimeSeries<LocalDate, String>(LocalDate.class, String.class);
    assertEquals(0, test.size());
    assertEquals(true, test.isEmpty());
    assertEquals(false, test.iterator().hasNext());
    assertEquals(false, test.timesIterator().hasNext());
    assertEquals(false, test.valuesIterator().hasNext());
    assertEquals(0, test.times().size());
    assertEquals(0, test.timesArray().length);
    assertEquals(0, test.values().size());
    assertEquals(0, test.valuesArray().length);
  }

  public void test_constructor_arrays_empty() {
    final SimpleMapTimeSeries<LocalDate, String> test = new SimpleMapTimeSeries<LocalDate, String>(new LocalDate[0], new String[0]);
    assertEquals(0, test.size());
    assertEquals(true, test.isEmpty());
    assertEquals(false, test.iterator().hasNext());
    assertEquals(false, test.timesIterator().hasNext());
    assertEquals(false, test.valuesIterator().hasNext());
    assertEquals(0, test.times().size());
    assertEquals(0, test.timesArray().length);
    assertEquals(0, test.values().size());
    assertEquals(0, test.valuesArray().length);
  }

  public void test_constructor_arrays_elements() {
    final SimpleMapTimeSeries<LocalDate, String> test = new SimpleMapTimeSeries<LocalDate, String>(
        new LocalDate[] {DATE1, DATE2 }, new String[] {"A", "B" });
    assertEquals(2, test.size());
    assertEquals(false, test.isEmpty());
    assertEquals(true, test.iterator().hasNext());
    assertEquals(true, test.timesIterator().hasNext());
    assertEquals(true, test.valuesIterator().hasNext());
    assertEquals(2, test.times().size());
    assertEquals(DATE1, test.times().get(0));
    assertEquals(DATE2, test.times().get(1));
    assertEquals(2, test.timesArray().length);
    assertEquals(DATE1, test.timesArray()[0]);
    assertEquals(DATE2, test.timesArray()[1]);
    assertEquals(2, test.values().size());
    assertEquals(2, test.valuesArray().length);
  }

  public void test_lag() {
    final SimpleMapTimeSeries<LocalDate, String> test = new SimpleMapTimeSeries<LocalDate, String>(new LocalDate[] {DATE1, DATE2 }, new String[] {"A", "B" });
    TimeSeries<LocalDate, String> lagged = test.lag(0);
    assertEquals(2, lagged.size());
    assertEquals(DATE1, lagged.getTimeAtIndex(0));
    assertEquals(DATE2, lagged.getTimeAtIndex(1));
    assertEquals("A", lagged.getValueAtIndex(0));
    assertEquals("B", lagged.getValueAtIndex(1));
    lagged = test.lag(1);
    assertEquals(1, lagged.size());
    assertEquals(DATE2, lagged.getTimeAtIndex(0));
    assertEquals("A", lagged.getValueAtIndex(0));
    lagged = test.lag(-1);
    assertEquals(1, lagged.size());
    assertEquals(DATE1, lagged.getTimeAtIndex(0));
    assertEquals("B", lagged.getValueAtIndex(0));
    lagged = test.lag(2);
    assertTrue(lagged.isEmpty());
    lagged = test.lag(-2);
    assertTrue(lagged.isEmpty());
    lagged = test.lag(1000);
    assertTrue(lagged.isEmpty());
    lagged = test.lag(-1000);
    assertTrue(lagged.isEmpty());
  }

}
