/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.EnumSet;
import java.util.Set;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.apache.commons.lang.Validate;

/**
 * {@code DatAdjuster} that finds the next given day of month in the next IMM Future Expiry Month.
 * e.g. For IR Future Options, this is  typically the 3rd Wednesday. For USD and GBP Equity Index Options, this is the Saturday after the Third Friday.
 */
public class NextExpiryAdjuster implements DateAdjuster {

/** Specify the day and week in the month. (eg 3rd Wednesday) Adjuster returns that day in next IMM Expiry Month
   * @param week Ordinal of week in month, beginning from 1.
   * @param day DayOfWeek
   */
  public NextExpiryAdjuster(int week, DayOfWeek day) {
    _dayOfMonthAdjuster =  DateAdjusters.dayOfWeekInMonth(week, day);
  }

  /** Specify the day and week in the month, plus an offset in number of days. Adjuster returns that day in next IMM Expiry Month. <p>
   *  e.g. (3,DayOfWeek.FRIDAY,1) is the Saturday after the 3rd Friday in the month. This is different from the 3rd Saturday.
   * @param week Ordinal of week in month, beginning from 1.
   * @param day DayOfWeek
   * @param offset Integer offset, positive or negative from the result of week,day.
   */
  public NextExpiryAdjuster(int week, DayOfWeek day, int offset) {
    _dayOfMonthAdjuster = new DayOfWeekInMonthPlusOffsetAdjuster(week, day, offset);
  }

  /**
   * Specify the day and week in the month, plus an offset in number of days. Adjuster returns that day in next IMM Expiry Month. <p>
   * @param dayOfMonthAdjuster provide the day of month adjuster directly
   */
  public NextExpiryAdjuster(DateAdjuster dayOfMonthAdjuster) {
    Validate.notNull(dayOfMonthAdjuster, "Expecting a DateAdjuster that provides a certain day in the month.");
    _dayOfMonthAdjuster = dayOfMonthAdjuster;
  }

  /** An adjuster finding the DayOfWeek in the given week in a month. May be before or after date. */
  private final DateAdjuster _dayOfMonthAdjuster;

  /**
   * Gets the dayOfMonthAdjuster.
   * @return the dayOfMonthAdjuster
   */
  public final DateAdjuster getDayOfMonthAdjuster() {
    return _dayOfMonthAdjuster;
  }

  /** An adjuster moving to the next quarter. */
  private static final DateAdjuster s_nextQuarterAdjuster = new NextQuarterAdjuster();

  /** The IMM Expiry months  */
  private final Set<MonthOfYear> _futureQuarters = EnumSet.of(MonthOfYear.MARCH, MonthOfYear.JUNE, MonthOfYear.SEPTEMBER, MonthOfYear.DECEMBER);

  @Override
  public LocalDate adjustDate(final LocalDate date) {
    LocalDate result = date.with(_dayOfMonthAdjuster);
    if (_futureQuarters.contains(date.getMonthOfYear()) &&
        result.isAfter(date)) { // in a quarter
      return result;
    }
    return date.with(s_nextQuarterAdjuster).with(_dayOfMonthAdjuster);
  }

}
