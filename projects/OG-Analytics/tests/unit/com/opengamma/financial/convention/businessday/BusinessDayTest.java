/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;

import org.junit.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.CalendarFactory;

/**
 * Test BusinessDayConvention.
 */
public class BusinessDayTest {

  private final Calendar _calendar_UK = CalendarFactory.INSTANCE.getCalendar("UK Bank Holidays");

  private void test(final DateAdjuster adjuster, final LocalDate testDate, final LocalDate expectedDate) {
    assertEquals(expectedDate, adjuster.adjustDate(testDate));
  }

  @Test
  public void testPrecedingDay() {
    final BusinessDayConvention convention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Preceding");
    assertNotNull(convention);
    final DateAdjuster adjuster = convention.getDateAdjuster(_calendar_UK);
    assertNotNull(adjuster);
    test(adjuster, LocalDate.of(2009, 12, 31), LocalDate.of(2009, 12, 31));
    test(adjuster, LocalDate.of(2010, 1, 1), LocalDate.of(2009, 12, 31)); // Fri 1 Jan -> Thu 31 Dec
    test(adjuster, LocalDate.of(2010, 1, 2), LocalDate.of(2009, 12, 31));
    test(adjuster, LocalDate.of(2010, 1, 3), LocalDate.of(2009, 12, 31));
    test(adjuster, LocalDate.of(2010, 1, 4), LocalDate.of(2010, 1, 4));
    test(adjuster, LocalDate.of(2010, 5, 28), LocalDate.of(2010, 5, 28));
    test(adjuster, LocalDate.of(2010, 5, 29), LocalDate.of(2010, 5, 28));
    test(adjuster, LocalDate.of(2010, 5, 30), LocalDate.of(2010, 5, 28));
    test(adjuster, LocalDate.of(2010, 5, 31), LocalDate.of(2010, 5, 28)); // Mon 31 May -> Fri 28 May
    test(adjuster, LocalDate.of(2010, 6, 1), LocalDate.of(2010, 6, 1));
  }

  @Test
  public void testFollowingDay() {
    final BusinessDayConvention convention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    assertNotNull(convention);
    final DateAdjuster adjuster = convention.getDateAdjuster(_calendar_UK);
    assertNotNull(adjuster);
    test(adjuster, LocalDate.of(2009, 12, 31), LocalDate.of(2009, 12, 31));
    test(adjuster, LocalDate.of(2010, 1, 1), LocalDate.of(2010, 1, 4)); // Fri 1 Jan -> Mon 4 Jan
    test(adjuster, LocalDate.of(2010, 1, 2), LocalDate.of(2010, 1, 4));
    test(adjuster, LocalDate.of(2010, 1, 3), LocalDate.of(2010, 1, 4));
    test(adjuster, LocalDate.of(2010, 1, 4), LocalDate.of(2010, 1, 4));
    test(adjuster, LocalDate.of(2010, 5, 28), LocalDate.of(2010, 5, 28));
    test(adjuster, LocalDate.of(2010, 5, 29), LocalDate.of(2010, 6, 1)); // Sat 29 May -> Tue 1 Jun
    test(adjuster, LocalDate.of(2010, 5, 30), LocalDate.of(2010, 6, 1));
    test(adjuster, LocalDate.of(2010, 5, 31), LocalDate.of(2010, 6, 1));
    test(adjuster, LocalDate.of(2010, 6, 1), LocalDate.of(2010, 6, 1));
  }

  @Test
  public void testModifiedFollowingDay() {
    final BusinessDayConvention convention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    assertNotNull(convention);
    final DateAdjuster adjuster = convention.getDateAdjuster(_calendar_UK);
    assertNotNull(adjuster);
    test(adjuster, LocalDate.of(2009, 12, 31), LocalDate.of(2009, 12, 31));
    test(adjuster, LocalDate.of(2010, 1, 1), LocalDate.of(2010, 1, 4)); // Fri 1 Jan -> Mon 4 Jan
    test(adjuster, LocalDate.of(2010, 1, 2), LocalDate.of(2010, 1, 4));
    test(adjuster, LocalDate.of(2010, 1, 3), LocalDate.of(2010, 1, 4));
    test(adjuster, LocalDate.of(2010, 1, 4), LocalDate.of(2010, 1, 4));
    test(adjuster, LocalDate.of(2010, 5, 28), LocalDate.of(2010, 5, 28));
    test(adjuster, LocalDate.of(2010, 5, 29), LocalDate.of(2010, 5, 28)); // Sat 29 May -> Fri 28 May
    test(adjuster, LocalDate.of(2010, 5, 30), LocalDate.of(2010, 5, 28));
    test(adjuster, LocalDate.of(2010, 5, 31), LocalDate.of(2010, 5, 28));
    test(adjuster, LocalDate.of(2010, 6, 1), LocalDate.of(2010, 6, 1));
  }

  @Test
  public void testModifiedPrecedingDay() {
    final BusinessDayConvention convention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Preceding");
    assertNotNull(convention);
    final DateAdjuster adjuster = convention.getDateAdjuster(_calendar_UK);
    assertNotNull(adjuster);
    test(adjuster, LocalDate.of(2009, 12, 31), LocalDate.of(2009, 12, 31));
    test(adjuster, LocalDate.of(2010, 1, 1), LocalDate.of(2010, 1, 4)); // Fri 1 Jan -> Mon 4 Jan
    test(adjuster, LocalDate.of(2010, 1, 2), LocalDate.of(2010, 1, 4));
    test(adjuster, LocalDate.of(2010, 1, 3), LocalDate.of(2010, 1, 4));
    test(adjuster, LocalDate.of(2010, 1, 4), LocalDate.of(2010, 1, 4));
    test(adjuster, LocalDate.of(2010, 5, 1), LocalDate.of(2010, 5, 4)); // Sat 1 May -> Tue 4 May
    test(adjuster, LocalDate.of(2010, 5, 2), LocalDate.of(2010, 5, 4));
    test(adjuster, LocalDate.of(2010, 5, 3), LocalDate.of(2010, 5, 4));
    test(adjuster, LocalDate.of(2010, 5, 4), LocalDate.of(2010, 5, 4));
    test(adjuster, LocalDate.of(2010, 5, 31), LocalDate.of(2010, 5, 28)); // Mon 1 May -> Fri 28 May 
  }

}
