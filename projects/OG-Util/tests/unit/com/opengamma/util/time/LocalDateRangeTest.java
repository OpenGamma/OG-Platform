/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

/**
 * Test LocalDateRange.
 */
@Test(groups = "unit")
public class LocalDateRangeTest {

  @Test
  public void test_ALL() {
    LocalDateRange test = LocalDateRange.ALL;
    assertEquals(test.getStartDateInclusive(), LocalDate.MIN_DATE);
    assertEquals(test.getEndDateInclusive(), LocalDate.MAX_DATE);
    assertEquals(test.getEndDateExclusive(), LocalDate.MAX_DATE);
  }

  @Test
  public void test_of_2_true() {
    LocalDateRange test = LocalDateRange.of(LocalDate.of(2012, 7, 28), LocalDate.of(2012, 7, 30), true);
    assertEquals(test.getStartDateInclusive(), LocalDate.of(2012, 7, 28));
    assertEquals(test.getEndDateInclusive(), LocalDate.of(2012, 7, 30));
    assertEquals(test.getEndDateExclusive(), LocalDate.of(2012, 7, 31));
  }

  @Test
  public void test_of_2_false() {
    LocalDateRange test = LocalDateRange.of(LocalDate.of(2012, 7, 28), LocalDate.of(2012, 7, 30), false);
    assertEquals(test.getStartDateInclusive(), LocalDate.of(2012, 7, 28));
    assertEquals(test.getEndDateInclusive(), LocalDate.of(2012, 7, 29));
    assertEquals(test.getEndDateExclusive(), LocalDate.of(2012, 7, 30));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_2_null1() {
    LocalDateRange.of(null, LocalDate.of(2012, 7, 30), false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_2_null2() {
    LocalDateRange.of(LocalDate.of(2012, 7, 30), null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_2_order() {
    LocalDateRange.of(LocalDate.of(2012, 7, 30), LocalDate.of(2012, 7, 20), false);
  }

}
