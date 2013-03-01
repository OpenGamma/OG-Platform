/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Month;

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
  /** Quarterly */
  public static final String QUARTERLY = "Quarterly";
  /** Quarterly EOM */
  public static final String QUARTERLY_EOM = "QuarterlyEOM";
  /** Semi-annual */
  public static final String SEMI_ANNUAL = "SemiAnnual";
  /** Semi-annual EOM*/
  public static final String SEMI_ANNUAL_EOM = "SemiAnnualEOM";
  /** Annual */
  public static final String ANNUAL = "Annual";
  /** First of year */
  public static final String FIRST_OF_YEAR = "FirstOfYear";
  /** End of year */
  public static final String END_OF_YEAR = "EndOfYear";
  /** Annual on day and month */
  public static final String ANNUAL_ON_DAY_AND_MONTH = "AnnualOnDayAndMonth";
  /** Annual EOM */
  public static final String ANNUAL_EOM = "AnnualEOM";
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
  /** Quarterly calculator */
  public static final QuarterlyScheduleCalculator QUARTERLY_CALCULATOR = new QuarterlyScheduleCalculator();
  /** Quarterly EOM calculator */
  public static final EndOfMonthQuarterlyScheduleCalculator QUARTERLY_EOM_CALCULATOR = new EndOfMonthQuarterlyScheduleCalculator();
  /** Semi-annual calculator */
  public static final SemiAnnualScheduleCalculator SEMI_ANNUAL_CALCULATOR = new SemiAnnualScheduleCalculator();
  /** Semi-annual EOM calculator */
  public static final EndOfMonthSemiAnnualScheduleCalculator SEMI_ANNUAL_EOM_CALCULATOR = new EndOfMonthSemiAnnualScheduleCalculator();
  /** Annual calculator */
  public static final AnnualScheduleCalculator ANNUAL_CALCULATOR = new AnnualScheduleCalculator();
  /** First of year calculator */
  public static final FirstOfYearScheduleCalculator FIRST_OF_YEAR_CALCULATOR = new FirstOfYearScheduleCalculator();
  /** End of year calculator */
  public static final EndOfYearScheduleCalculator END_OF_YEAR_CALCULATOR = new EndOfYearScheduleCalculator();
  /** Annual EOM calculator */
  public static final EndOfMonthAnnualScheduleCalculator ANNUAL_EOM_CALCULATOR = new EndOfMonthAnnualScheduleCalculator();

  private static final Map<String, Schedule> s_instances = new HashMap<>();

  static {
    s_instances.put(DAILY, DAILY_CALCULATOR);
    s_instances.put(END_OF_MONTH, END_OF_MONTH_CALCULATOR);
    s_instances.put(END_OF_YEAR, END_OF_YEAR_CALCULATOR);
    s_instances.put(FIRST_OF_MONTH, FIRST_OF_MONTH_CALCULATOR);
    s_instances.put(FIRST_OF_YEAR, FIRST_OF_YEAR_CALCULATOR);
    s_instances.put(MONTHLY, MONTHLY_CALCULATOR);
    s_instances.put(WEEKLY, WEEKLY_CALCULATOR);
    s_instances.put(ANNUAL, ANNUAL_CALCULATOR);
    s_instances.put(QUARTERLY, QUARTERLY_CALCULATOR);
    s_instances.put(QUARTERLY_EOM, QUARTERLY_EOM_CALCULATOR);
    s_instances.put(SEMI_ANNUAL, SEMI_ANNUAL_CALCULATOR);
    s_instances.put(SEMI_ANNUAL_EOM, SEMI_ANNUAL_EOM_CALCULATOR);
    s_instances.put(ANNUAL_EOM, ANNUAL_EOM_CALCULATOR);
  }

  public static Schedule getScheduleCalculator(final String name) {
    final Schedule schedule = s_instances.get(name);
    if (schedule == null) {
      throw new IllegalArgumentException("Could not get schedule calculator with name " + name);
    }
    return schedule;
  }

  public static Schedule getScheduleCalculator(final String name, final DayOfWeek dayOfWeek) {
    Validate.notNull(name, "name");
    if (!name.equals(WEEKLY_ON_DAY)) {
      throw new IllegalArgumentException("Can only ask for " + WEEKLY_ON_DAY + " schedule");
    }
    return new WeeklyScheduleOnDayCalculator(dayOfWeek);
  }

  public static Schedule getScheduleCalculator(final String name, final int dayOfMonth, final Month monthOfYear) {
    Validate.notNull(name, "name");
    if (!name.equals(ANNUAL_ON_DAY_AND_MONTH)) {
      throw new IllegalArgumentException("Can only ask for " + ANNUAL_ON_DAY_AND_MONTH + " schedule");
    }
    return new AnnualScheduleOnDayAndMonthCalculator(dayOfMonth, monthOfYear);
  }
}
