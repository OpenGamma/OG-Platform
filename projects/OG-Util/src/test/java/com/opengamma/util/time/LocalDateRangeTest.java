/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

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

}
