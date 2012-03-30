/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.MonthOfYear;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.schedule.AnnualScheduleOnDayAndMonthCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.WeeklyScheduleOnDayCalculator;

/**
 * 
 */
public class ScheduleCalculatorFactoryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadName1() {
    ScheduleCalculatorFactory.getScheduleCalculator("A");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName1() {
    ScheduleCalculatorFactory.getScheduleCalculator(null, DayOfWeek.WEDNESDAY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadName2() {
    ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.DAILY, DayOfWeek.WEDNESDAY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName2() {
    ScheduleCalculatorFactory.getScheduleCalculator(null, 31, MonthOfYear.DECEMBER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadName3() {
    ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.DAILY, 23, MonthOfYear.APRIL);
  }

  @Test
  public void test() {
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.DAILY), ScheduleCalculatorFactory.DAILY_CALCULATOR);
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.WEEKLY), ScheduleCalculatorFactory.WEEKLY_CALCULATOR);
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.MONTHLY), ScheduleCalculatorFactory.MONTHLY_CALCULATOR);
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.FIRST_OF_MONTH), ScheduleCalculatorFactory.FIRST_OF_MONTH_CALCULATOR);
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.END_OF_MONTH), ScheduleCalculatorFactory.END_OF_MONTH_CALCULATOR);
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.QUARTERLY), ScheduleCalculatorFactory.QUARTERLY_CALCULATOR);
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.QUARTERLY_EOM), ScheduleCalculatorFactory.QUARTERLY_EOM_CALCULATOR);
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.SEMI_ANNUAL), ScheduleCalculatorFactory.SEMI_ANNUAL_CALCULATOR);
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.SEMI_ANNUAL_EOM), ScheduleCalculatorFactory.SEMI_ANNUAL_EOM_CALCULATOR);
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.ANNUAL), ScheduleCalculatorFactory.ANNUAL_CALCULATOR);
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.FIRST_OF_YEAR), ScheduleCalculatorFactory.FIRST_OF_YEAR_CALCULATOR);
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.END_OF_YEAR), ScheduleCalculatorFactory.END_OF_YEAR_CALCULATOR);
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.ANNUAL_EOM), ScheduleCalculatorFactory.ANNUAL_EOM_CALCULATOR);
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.WEEKLY_ON_DAY, DayOfWeek.MONDAY), new WeeklyScheduleOnDayCalculator(DayOfWeek.MONDAY));
    assertEquals(ScheduleCalculatorFactory.getScheduleCalculator(ScheduleCalculatorFactory.ANNUAL_ON_DAY_AND_MONTH, 11, MonthOfYear.APRIL), new AnnualScheduleOnDayAndMonthCalculator(11,
        MonthOfYear.APRIL));
  }
}
