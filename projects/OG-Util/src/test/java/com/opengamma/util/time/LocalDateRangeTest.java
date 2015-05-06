/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.util.test.TestGroup;

/**
 * Test LocalDateRange.
 */
@Test(groups = TestGroup.UNIT)
public class LocalDateRangeTest {

  @Test
  public void test_ALL() {
    LocalDateRange test = LocalDateRange.ALL;
    assertEquals(LocalDate.MIN, test.getStartDateInclusive());
    assertEquals(LocalDate.MAX, test.getEndDateInclusive());
    assertEquals(LocalDate.MAX, test.getEndDateExclusive());
    assertEquals(true, test.isStartDateMinimum());
    assertEquals(true, test.isEndDateMaximum());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_of_2_true() {
    LocalDateRange test = LocalDateRange.of(LocalDate.of(2012, 7, 28), LocalDate.of(2012, 7, 30), true);
    assertEquals(LocalDate.of(2012, 7, 28), test.getStartDateInclusive());
    assertEquals(LocalDate.of(2012, 7, 30), test.getEndDateInclusive());
    assertEquals(LocalDate.of(2012, 7, 31), test.getEndDateExclusive());
    assertEquals(false, test.isStartDateMinimum());
    assertEquals(false, test.isEndDateMaximum());
  }

  @Test
  public void test_of_2_false() {
    LocalDateRange test = LocalDateRange.of(LocalDate.of(2012, 7, 28), LocalDate.of(2012, 7, 30), false);
    assertEquals(LocalDate.of(2012, 7, 28), test.getStartDateInclusive());
    assertEquals(LocalDate.of(2012, 7, 29), test.getEndDateInclusive());
    assertEquals(LocalDate.of(2012, 7, 30), test.getEndDateExclusive());
    assertEquals(false, test.isStartDateMinimum());
    assertEquals(false, test.isEndDateMaximum());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_2_null1() {
    LocalDateRange.of(null, LocalDate.of(2012, 7, 30), false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_2_null2() {
    LocalDateRange.of(LocalDate.of(2012, 7, 28), null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_2_order() {
    LocalDateRange.of(LocalDate.of(2012, 7, 30), LocalDate.of(2012, 7, 20), false);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_ofNullUnbounded_2_true() {
    LocalDateRange test = LocalDateRange.ofNullUnbounded(LocalDate.of(2012, 7, 28), LocalDate.of(2012, 7, 30), true);
    assertEquals(LocalDate.of(2012, 7, 28), test.getStartDateInclusive());
    assertEquals(LocalDate.of(2012, 7, 30), test.getEndDateInclusive());
    assertEquals(LocalDate.of(2012, 7, 31), test.getEndDateExclusive());
    assertEquals(LocalDate.of(2012, 7, 31), test.getEndDateExclusive());
    assertEquals(false, test.isStartDateMinimum());
    assertEquals(false, test.isEndDateMaximum());
  }

  @Test
  public void test_ofNullUnbounded_2_false() {
    LocalDateRange test = LocalDateRange.ofNullUnbounded(LocalDate.of(2012, 7, 28), LocalDate.of(2012, 7, 30), false);
    assertEquals(LocalDate.of(2012, 7, 28), test.getStartDateInclusive());
    assertEquals(LocalDate.of(2012, 7, 29), test.getEndDateInclusive());
    assertEquals(LocalDate.of(2012, 7, 30), test.getEndDateExclusive());
    assertEquals(false, test.isStartDateMinimum());
    assertEquals(false, test.isEndDateMaximum());
  }

  @Test
  public void test_ofNullUnbounded_2_null1() {
    LocalDateRange test = LocalDateRange.ofNullUnbounded(null, LocalDate.of(2012, 7, 30), false);
    assertEquals(LocalDate.MIN, test.getStartDateInclusive());
    assertEquals(LocalDate.of(2012, 7, 29), test.getEndDateInclusive());
    assertEquals(LocalDate.of(2012, 7, 30), test.getEndDateExclusive());
    assertEquals(true, test.isStartDateMinimum());
    assertEquals(false, test.isEndDateMaximum());
  }

  @Test
  public void test_ofNullUnbounded_2_null2() {
    LocalDateRange test = LocalDateRange.ofNullUnbounded(LocalDate.of(2012, 7, 28), null, false);
    assertEquals(LocalDate.of(2012, 7, 28), test.getStartDateInclusive());
    assertEquals(LocalDate.MAX, test.getEndDateInclusive());
    assertEquals(LocalDate.MAX, test.getEndDateExclusive());
    assertEquals(false, test.isStartDateMinimum());
    assertEquals(true, test.isEndDateMaximum());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ofNullUnbounded_2_order() {
    LocalDateRange.ofNullUnbounded(LocalDate.of(2012, 7, 30), LocalDate.of(2012, 7, 20), false);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_resolveStartUnbounded() {
    LocalDateRange base = LocalDateRange.ofNullUnbounded(null, LocalDate.of(2012, 7, 30), false);
    LocalDateRange test = base.resolveUnboundedStartDate(LocalDate.of(2012, 7, 28));
    assertEquals(LocalDate.of(2012, 7, 28), test.getStartDateInclusive());
    assertEquals(LocalDate.of(2012, 7, 29), test.getEndDateInclusive());
    assertEquals(LocalDate.of(2012, 7, 30), test.getEndDateExclusive());
    assertEquals(false, test.isStartDateMinimum());
    assertEquals(false, test.isEndDateMaximum());
  }

  @Test
  public void test_resolveEndUnbounded() {
    LocalDateRange base = LocalDateRange.ofNullUnbounded(LocalDate.of(2012, 7, 28), null, false);
    LocalDateRange test = base.resolveUnboundedEndDate(LocalDate.of(2012, 7, 30), false);
    assertEquals(LocalDate.of(2012, 7, 28), test.getStartDateInclusive());
    assertEquals(LocalDate.of(2012, 7, 29), test.getEndDateInclusive());
    assertEquals(LocalDate.of(2012, 7, 30), test.getEndDateExclusive());
    assertEquals(false, test.isStartDateMinimum());
    assertEquals(false, test.isEndDateMaximum());
  }

  @Test
  public void test_iteratorEndInclusive() {
    LocalDate start = LocalDate.of(2011, 3, 8);
    LocalDate end = LocalDate.of(2011, 3, 10);
    LocalDateRange dateRange = LocalDateRange.of(start, end, true);
    List<LocalDate> expected = ImmutableList.of(start, LocalDate.of(2011, 3, 9), end);
    assertEquals(expected, Lists.newArrayList(dateRange));
  }

  @Test
  public void test_iteratorEndExclusive() {
    LocalDate start = LocalDate.of(2011, 3, 8);
    LocalDate end = LocalDate.of(2011, 3, 11);
    LocalDateRange dateRange = LocalDateRange.of(start, end, false);
    List<LocalDate> expected = ImmutableList.of(start, LocalDate.of(2011, 3, 9), LocalDate.of(2011, 3, 10));
    assertEquals(expected, Lists.newArrayList(dateRange));
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void test_iteratorIllegalState() {
    LocalDate start = LocalDate.of(2011, 3, 8);
    LocalDate end = LocalDate.of(2011, 3, 9);
    LocalDateRange dateRange = LocalDateRange.of(start, end, true);
    Iterator<LocalDate> itr = dateRange.iterator();
    itr.next();
    itr.next();
    itr.next();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test_iteratorRemove() {
    LocalDate start = LocalDate.of(2011, 3, 8);
    LocalDate end = LocalDate.of(2011, 3, 9);
    LocalDateRange dateRange = LocalDateRange.of(start, end, true);
    Iterator<LocalDate> itr = dateRange.iterator();
    itr.remove();
  }
}
