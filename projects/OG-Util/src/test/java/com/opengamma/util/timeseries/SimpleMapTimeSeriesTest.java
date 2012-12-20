/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

/**
 * Test {@link SimpleMapTimeSeries}.
 */
@Test
public class SimpleMapTimeSeriesTest {

  private static final LocalDate DATE1 = LocalDate.of(2011, 6, 1);
  private static final LocalDate DATE2 = LocalDate.of(2011, 6, 2);

  public void test_constructor_arrays_emptyTypes() {
    SimpleMapTimeSeries<LocalDate, String> test = new SimpleMapTimeSeries<LocalDate, String>(LocalDate.class, String.class);
    assertEquals(0, test.size());
    assertEquals(true, test.isEmpty());
    assertEquals(false, test.iterator().hasNext());
    assertEquals(false, test.timeIterator().hasNext());
    assertEquals(false, test.valuesIterator().hasNext());
    assertEquals(0, test.times().size());
    assertEquals(0, test.timesArray().length);
    assertEquals(0, test.values().size());
    assertEquals(0, test.valuesArray().length);
  }

  public void test_constructor_arrays_empty() {
    SimpleMapTimeSeries<LocalDate, String> test = new SimpleMapTimeSeries<LocalDate, String>(new LocalDate[0], new String[0]);
    assertEquals(0, test.size());
    assertEquals(true, test.isEmpty());
    assertEquals(false, test.iterator().hasNext());
    assertEquals(false, test.timeIterator().hasNext());
    assertEquals(false, test.valuesIterator().hasNext());
    assertEquals(0, test.times().size());
    assertEquals(0, test.timesArray().length);
    assertEquals(0, test.values().size());
    assertEquals(0, test.valuesArray().length);
  }

  public void test_constructor_arrays_elements() {
    SimpleMapTimeSeries<LocalDate, String> test = new SimpleMapTimeSeries<LocalDate, String>(
        new LocalDate[] {DATE1, DATE2}, new String[] {"A", "B"});
    assertEquals(2, test.size());
    assertEquals(false, test.isEmpty());
    assertEquals(true, test.iterator().hasNext());
    assertEquals(true, test.timeIterator().hasNext());
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

}
