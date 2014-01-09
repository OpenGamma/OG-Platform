/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.CalendarFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Test BusinessDayConvention.
 */
@Test(groups = TestGroup.UNIT)
public class BusinessDayTest {

  private final Calendar _calendar_UK = CalendarFactory.of("UK Bank Holidays");

  private void assertDate(final TemporalAdjuster adjuster, final LocalDate testDate, final LocalDate expectedDate) {
    assertEquals(expectedDate, testDate.with(adjuster));
  }

  @Test
  public void testPrecedingDay() {
    final BusinessDayConvention convention = BusinessDayConventions.PRECEDING;
    assertNotNull(convention);
    final TemporalAdjuster adjuster = convention.getTemporalAdjuster(_calendar_UK);
    assertNotNull(adjuster);
    assertDate(adjuster, LocalDate.of(2009, 12, 31), LocalDate.of(2009, 12, 31));
    assertDate(adjuster, LocalDate.of(2010, 1, 1), LocalDate.of(2009, 12, 31)); // Fri 1 Jan -> Thu 31 Dec
    assertDate(adjuster, LocalDate.of(2010, 1, 2), LocalDate.of(2009, 12, 31));
    assertDate(adjuster, LocalDate.of(2010, 1, 3), LocalDate.of(2009, 12, 31));
    assertDate(adjuster, LocalDate.of(2010, 1, 4), LocalDate.of(2010, 1, 4));
    assertDate(adjuster, LocalDate.of(2010, 5, 28), LocalDate.of(2010, 5, 28));
    assertDate(adjuster, LocalDate.of(2010, 5, 29), LocalDate.of(2010, 5, 28));
    assertDate(adjuster, LocalDate.of(2010, 5, 30), LocalDate.of(2010, 5, 28));
    assertDate(adjuster, LocalDate.of(2010, 5, 31), LocalDate.of(2010, 5, 28)); // Mon 31 May -> Fri 28 May
    assertDate(adjuster, LocalDate.of(2010, 6, 1), LocalDate.of(2010, 6, 1));
  }

  @Test
  public void testFollowingDay() {
    final BusinessDayConvention convention = BusinessDayConventions.FOLLOWING;
    assertNotNull(convention);
    final TemporalAdjuster adjuster = convention.getTemporalAdjuster(_calendar_UK);
    assertNotNull(adjuster);
    assertDate(adjuster, LocalDate.of(2009, 12, 31), LocalDate.of(2009, 12, 31));
    assertDate(adjuster, LocalDate.of(2010, 1, 1), LocalDate.of(2010, 1, 4)); // Fri 1 Jan -> Mon 4 Jan
    assertDate(adjuster, LocalDate.of(2010, 1, 2), LocalDate.of(2010, 1, 4));
    assertDate(adjuster, LocalDate.of(2010, 1, 3), LocalDate.of(2010, 1, 4));
    assertDate(adjuster, LocalDate.of(2010, 1, 4), LocalDate.of(2010, 1, 4));
    assertDate(adjuster, LocalDate.of(2010, 5, 28), LocalDate.of(2010, 5, 28));
    assertDate(adjuster, LocalDate.of(2010, 5, 29), LocalDate.of(2010, 6, 1)); // Sat 29 May -> Tue 1 Jun
    assertDate(adjuster, LocalDate.of(2010, 5, 30), LocalDate.of(2010, 6, 1));
    assertDate(adjuster, LocalDate.of(2010, 5, 31), LocalDate.of(2010, 6, 1));
    assertDate(adjuster, LocalDate.of(2010, 6, 1), LocalDate.of(2010, 6, 1));
  }

  @Test
  public void testModifiedFollowingDay() {
    final BusinessDayConvention convention = BusinessDayConventions.MODIFIED_FOLLOWING;
    assertNotNull(convention);
    final TemporalAdjuster adjuster = convention.getTemporalAdjuster(_calendar_UK);
    assertNotNull(adjuster);
    assertDate(adjuster, LocalDate.of(2009, 12, 31), LocalDate.of(2009, 12, 31));
    assertDate(adjuster, LocalDate.of(2010, 1, 1), LocalDate.of(2010, 1, 4)); // Fri 1 Jan -> Mon 4 Jan
    assertDate(adjuster, LocalDate.of(2010, 1, 2), LocalDate.of(2010, 1, 4));
    assertDate(adjuster, LocalDate.of(2010, 1, 3), LocalDate.of(2010, 1, 4));
    assertDate(adjuster, LocalDate.of(2010, 1, 4), LocalDate.of(2010, 1, 4));
    assertDate(adjuster, LocalDate.of(2010, 5, 28), LocalDate.of(2010, 5, 28));
    assertDate(adjuster, LocalDate.of(2010, 5, 29), LocalDate.of(2010, 5, 28)); // Sat 29 May -> Fri 28 May
    assertDate(adjuster, LocalDate.of(2010, 5, 30), LocalDate.of(2010, 5, 28));
    assertDate(adjuster, LocalDate.of(2010, 5, 31), LocalDate.of(2010, 5, 28));
    assertDate(adjuster, LocalDate.of(2010, 6, 1), LocalDate.of(2010, 6, 1));
  }

  @Test
  public void testModifiedPrecedingDay() {
    final BusinessDayConvention convention = BusinessDayConventions.MODIFIED_PRECEDING;
    assertNotNull(convention);
    final TemporalAdjuster adjuster = convention.getTemporalAdjuster(_calendar_UK);
    assertNotNull(adjuster);
    assertDate(adjuster, LocalDate.of(2009, 12, 31), LocalDate.of(2009, 12, 31));
    assertDate(adjuster, LocalDate.of(2010, 1, 1), LocalDate.of(2010, 1, 4)); // Fri 1 Jan -> Mon 4 Jan
    assertDate(adjuster, LocalDate.of(2010, 1, 2), LocalDate.of(2010, 1, 4));
    assertDate(adjuster, LocalDate.of(2010, 1, 3), LocalDate.of(2010, 1, 4));
    assertDate(adjuster, LocalDate.of(2010, 1, 4), LocalDate.of(2010, 1, 4));
    assertDate(adjuster, LocalDate.of(2010, 5, 1), LocalDate.of(2010, 5, 4)); // Sat 1 May -> Tue 4 May
    assertDate(adjuster, LocalDate.of(2010, 5, 2), LocalDate.of(2010, 5, 4));
    assertDate(adjuster, LocalDate.of(2010, 5, 3), LocalDate.of(2010, 5, 4));
    assertDate(adjuster, LocalDate.of(2010, 5, 4), LocalDate.of(2010, 5, 4));
    assertDate(adjuster, LocalDate.of(2010, 5, 31), LocalDate.of(2010, 5, 28)); // Mon 1 May -> Fri 28 May 
  }

}
