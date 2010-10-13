/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.MonthOfYear;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class ScheduleCalculatorFactory {
  /** Daily */
  public static final String DAILY = "Daily";
  /** Weekly */
  public static final String WEEKLY = "Weekly";
  /** Weekly on day */
  public static final String WEEKLY_ON_DAY = "WeeklyOnDay";
  /** Monthly */
  public static final String MONTHLY = "Monthly";
  /** First of month */
  public static final String FIRST_OF_MONTH = "FirstOfMonth";
  /** End of month */
  public static final String END_OF_MONTH = "EndOfMonth";
  /** Monthly on day */
  public static final String MONTHLY_ON_DAY = "MonthlyOnDay";
  /** Yearly */
  public static final String YEARLY = "Yearly";
  /** First of year */
  public static final String FIRST_OF_YEAR = "FirstOfYear";
  /** End of year */
  public static final String END_OF_YEAR = "EndOfYear";
  /** Yearly on day of month */
  public static final String YEAR_ON_DAY_OF_MONTH = "YearlyOnDayOfMonth";
  /** Daily calculator */
  public static final DailyScheduleCalculator DAILY_CALCULATOR = new DailyScheduleCalculator();
  /** Weekly calculator */
  public static final WeeklyScheduleCalculator WEEKLY_CALCULATOR = new WeeklyScheduleCalculator();
  /** Monthly calculator */
  public static final MonthlyScheduleCalculator MONTHLY_CALCULATOR = new MonthlyScheduleCalculator();
  /** First of month calculator */
  public static final FirstOfMonthScheduleCalculator FIRST_OF_MONTH_CALCULATOR = new FirstOfMonthScheduleCalculator();
  /** End of month calculator */
  public static final EndOfMonthScheduleCalculator END_OF_MONTH_CALCULATOR = new EndOfMonthScheduleCalculator();
  /** Yearly calculator */
  public static final YearlyScheduleCalculator YEARLY_CALCULATOR = new YearlyScheduleCalculator();
  /** First of year calculator */
  public static final FirstOfYearScheduleCalculator FIRST_OF_YEAR_CALCULATOR = new FirstOfYearScheduleCalculator();
  /** End of year calculator */
  public static final EndOfYearScheduleCalculator END_OF_YEAR_CALCULATOR = new EndOfYearScheduleCalculator();

  private static final Map<String, Schedule> s_instances = new HashMap<String, Schedule>();

  static {
    s_instances.put(DAILY, DAILY_CALCULATOR);
    s_instances.put(END_OF_MONTH, END_OF_MONTH_CALCULATOR);
    s_instances.put(END_OF_YEAR, END_OF_YEAR_CALCULATOR);
    s_instances.put(FIRST_OF_MONTH, FIRST_OF_MONTH_CALCULATOR);
    s_instances.put(FIRST_OF_YEAR, FIRST_OF_YEAR_CALCULATOR);
    s_instances.put(MONTHLY, MONTHLY_CALCULATOR);
    s_instances.put(WEEKLY, WEEKLY_CALCULATOR);
    s_instances.put(YEARLY, YEARLY_CALCULATOR);
  }

  public static Schedule getSchedule(final String name) {
    final Schedule schedule = s_instances.get(name);
    if (schedule == null) {
      throw new IllegalArgumentException("Could not get schedule calculator with name " + name);
    }
    return schedule;
  }

  public static Schedule getSchedule(final String name, final DayOfWeek dayOfWeek) {
    Validate.notNull(name, "name");
    if (!name.equals(WEEKLY_ON_DAY)) {
      throw new IllegalArgumentException("Can only ask for " + WEEKLY_ON_DAY + " schedule");
    }
    return new WeeklyScheduleOnDayCalculator(dayOfWeek);
  }

  public static Schedule getSchedule(final String name, final int dayOfMonth, final MonthOfYear monthOfYear) {
    Validate.notNull(name, "name");
    if (!name.equals(YEAR_ON_DAY_OF_MONTH)) {
      throw new IllegalArgumentException("Can only ask for " + YEAR_ON_DAY_OF_MONTH + " schedule");
    }
    return new YearlyScheduleOnDayAndMonthCalculator(dayOfMonth, monthOfYear);
  }
}
