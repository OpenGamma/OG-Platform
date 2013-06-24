/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDAPremiumLegScheduleTest {

  // private static final DayCount ACT360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final Calendar CALENDAR = new MondayToFridayCalendar("TestCalendar");

  // TODO all the null input tests. startDate after endDate etc
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void undefinedStubTest() {
    final LocalDate startDate = LocalDate.of(2012, 6, 7);
    final LocalDate endDate = LocalDate.of(2015, 11, 29); // sunday
    final Period step = Period.ofMonths(3);
    final StubType stubType = StubType.NONE;
    final boolean protectionStart = false;
    @SuppressWarnings("unused")
    ISDAPremiumLegSchedule schedule = new ISDAPremiumLegSchedule(startDate, endDate, step, stubType, FOLLOWING, CALENDAR, protectionStart);
  }

  /**
   * short front stub and end on a weekend at EoM 
   */
  @Test
  public void scheduleTest1() {
    final LocalDate[] accStart = new LocalDate[] {LocalDate.of(2012, 6, 7), LocalDate.of(2012, 8, 29), LocalDate.of(2012, 11, 29), LocalDate.of(2013, 2, 28), LocalDate.of(2013, 5, 29),
        LocalDate.of(2013, 8, 29), LocalDate.of(2013, 11, 29), LocalDate.of(2014, 2, 28), LocalDate.of(2014, 5, 29), LocalDate.of(2014, 8, 29), LocalDate.of(2014, 12, 1), LocalDate.of(2015, 3, 2),
        LocalDate.of(2015, 5, 29), LocalDate.of(2015, 8, 31)};
    final LocalDate[] accEnd = new LocalDate[] {LocalDate.of(2012, 8, 29), LocalDate.of(2012, 11, 29), LocalDate.of(2013, 2, 28), LocalDate.of(2013, 5, 29), LocalDate.of(2013, 8, 29),
        LocalDate.of(2013, 11, 29), LocalDate.of(2014, 2, 28), LocalDate.of(2014, 5, 29), LocalDate.of(2014, 8, 29), LocalDate.of(2014, 12, 1), LocalDate.of(2015, 3, 2), LocalDate.of(2015, 5, 29),
        LocalDate.of(2015, 8, 31), LocalDate.of(2015, 11, 30)};
    final LocalDate[] pay = new LocalDate[] {LocalDate.of(2012, 8, 29), LocalDate.of(2012, 11, 29), LocalDate.of(2013, 2, 28), LocalDate.of(2013, 5, 29), LocalDate.of(2013, 8, 29),
        LocalDate.of(2013, 11, 29), LocalDate.of(2014, 2, 28), LocalDate.of(2014, 5, 29), LocalDate.of(2014, 8, 29), LocalDate.of(2014, 12, 1), LocalDate.of(2015, 3, 2), LocalDate.of(2015, 5, 29),
        LocalDate.of(2015, 8, 31), LocalDate.of(2015, 11, 30)};
    final int n = pay.length;
    // data check
    ArgumentChecker.isTrue(n == accStart.length, null);
    ArgumentChecker.isTrue(n == accEnd.length, null);

    final LocalDate startDate = LocalDate.of(2012, 6, 7);
    final LocalDate endDate = LocalDate.of(2015, 11, 29); // sunday
    final Period step = Period.ofMonths(3);
    final StubType stubType = StubType.FRONTSHORT;
    final boolean protectionStart = true;

    ISDAPremiumLegSchedule schedule = new ISDAPremiumLegSchedule(startDate, endDate, step, stubType, FOLLOWING, CALENDAR, protectionStart);
    assertEquals(n, schedule.getNumPayments());
    for (int i = 0; i < n; i++) {
      assertEquals(accStart[i], schedule.getAccStartDate(i));
      assertEquals(accEnd[i], schedule.getAccEndDate(i));
      assertEquals(pay[i], schedule.getPaymentDate(i));
    }

  }

  /**
   * Long front stub, start on weekend and end on IMM date 
   */
  @Test
  public void scheduleTest2() {
    final LocalDate[] accStart = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 12, 20), LocalDate.of(2013, 3, 20), LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 20)};
    final LocalDate[] accEnd = new LocalDate[] {LocalDate.of(2012, 12, 20), LocalDate.of(2013, 3, 20), LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 20), LocalDate.of(2013, 12, 21)};
    final LocalDate[] pay = new LocalDate[] {LocalDate.of(2012, 12, 20), LocalDate.of(2013, 3, 20), LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 20), LocalDate.of(2013, 12, 20)};
    final int n = pay.length;
    // data check
    ArgumentChecker.isTrue(n == accStart.length, null);
    ArgumentChecker.isTrue(n == accEnd.length, null);

    final LocalDate startDate = LocalDate.of(2012, 6, 30); // Saturday
    final LocalDate endDate = LocalDate.of(2013, 12, 20); // IMM date
    final Period step = Period.ofMonths(3);
    final StubType stubType = StubType.FRONTLONG;
    final boolean protectionStart = true;

    ISDAPremiumLegSchedule schedule = new ISDAPremiumLegSchedule(startDate, endDate, step, stubType, FOLLOWING, CALENDAR, protectionStart);
    assertEquals(n, schedule.getNumPayments());
    for (int i = 0; i < n; i++) {
      assertEquals(accStart[i], schedule.getAccStartDate(i));
      assertEquals(accEnd[i], schedule.getAccEndDate(i));
      assertEquals(pay[i], schedule.getPaymentDate(i));
    }
  }

  /**
   * short back stub, start and end on IMM date 
   */
  @Test
  public void scheduleTest3() {
    final LocalDate[] accStart = new LocalDate[] {LocalDate.of(2012, 6, 20), LocalDate.of(2012, 9, 20), LocalDate.of(2012, 12, 20), LocalDate.of(2013, 3, 20), LocalDate.of(2013, 6, 20)};
    final LocalDate[] accEnd = new LocalDate[] {LocalDate.of(2012, 9, 20), LocalDate.of(2012, 12, 20), LocalDate.of(2013, 3, 20), LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 21)};
    final LocalDate[] pay = new LocalDate[] {LocalDate.of(2012, 9, 20), LocalDate.of(2012, 12, 20), LocalDate.of(2013, 3, 20), LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 20)};
    final int n = pay.length;
    // data check
    ArgumentChecker.isTrue(n == accStart.length, null);
    ArgumentChecker.isTrue(n == accEnd.length, null);

    final LocalDate startDate = LocalDate.of(2012, 6, 20); // IMM date
    final LocalDate endDate = LocalDate.of(2013, 9, 20); // IMM date
    final Period step = Period.ofMonths(3);
    final StubType stubType = StubType.BACKSHORT;
    final boolean protectionStart = true;

    ISDAPremiumLegSchedule schedule = new ISDAPremiumLegSchedule(startDate, endDate, step, stubType, FOLLOWING, CALENDAR, protectionStart);
    assertEquals(n, schedule.getNumPayments());
    for (int i = 0; i < n; i++) {
      assertEquals(accStart[i], schedule.getAccStartDate(i));
      assertEquals(accEnd[i], schedule.getAccEndDate(i));
      assertEquals(pay[i], schedule.getPaymentDate(i));
    }
  }

  /**
   * long back stub, start and end NOT on IMM date 
   */
  @Test
  public void scheduleTest4() {
    final LocalDate[] accStart = new LocalDate[] {LocalDate.of(2012, 5, 10), LocalDate.of(2012, 8, 10), LocalDate.of(2012, 11, 12), LocalDate.of(2013, 2, 11), LocalDate.of(2013, 5, 10)};
    final LocalDate[] accEnd = new LocalDate[] {LocalDate.of(2012, 8, 10), LocalDate.of(2012, 11, 12), LocalDate.of(2013, 2, 11), LocalDate.of(2013, 5, 10), LocalDate.of(2013, 10, 21)};
    final LocalDate[] pay = new LocalDate[] {LocalDate.of(2012, 8, 10), LocalDate.of(2012, 11, 12), LocalDate.of(2013, 2, 11), LocalDate.of(2013, 5, 10), LocalDate.of(2013, 10, 21)};
    final int n = pay.length;
    // data check
    ArgumentChecker.isTrue(n == accStart.length, null);
    ArgumentChecker.isTrue(n == accEnd.length, null);

    final LocalDate startDate = LocalDate.of(2012, 5, 10);
    final LocalDate endDate = LocalDate.of(2013, 10, 20);
    final Period step = Period.ofMonths(3);
    final StubType stubType = StubType.BACKLONG;
    final boolean protectionStart = true;

    ISDAPremiumLegSchedule schedule = new ISDAPremiumLegSchedule(startDate, endDate, step, stubType, FOLLOWING, CALENDAR, protectionStart);
    assertEquals(n, schedule.getNumPayments());
    for (int i = 0; i < n; i++) {
      assertEquals(accStart[i], schedule.getAccStartDate(i));
      assertEquals(accEnd[i], schedule.getAccEndDate(i));
      assertEquals(pay[i], schedule.getPaymentDate(i));
    }
  }

  // TODO other inputs: Period different from 3M, convention "proceeding", proctectionStart = false etc.
}
