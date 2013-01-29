/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

/**
 * {@code DatAdjuster} that finds the next given day of month in the next IMM Future Expiry Month.
 * e.g. For IR Future Options, this is  typically the 3rd Wednesday. For USD and GBP Equity Index Options, this is the Saturday after the Third Friday.
 */
public class NextExpiryAdjuster implements TemporalAdjuster {

/** Specify the day and week in the month. (eg 3rd Wednesday) Adjuster returns that day in next IMM Expiry Month
   * @param week Ordinal of week in month, beginning from 1.
   * @param day DayOfWeek
   */
  public NextExpiryAdjuster(int week, DayOfWeek day) {
    _dayOfMonthAdjuster =  TemporalAdjusters.dayOfWeekInMonth(week, day);
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
  public NextExpiryAdjuster(TemporalAdjuster dayOfMonthAdjuster) {
    Validate.notNull(dayOfMonthAdjuster, "Expecting a TemporalAdjuster that provides a certain day in the month.");
    _dayOfMonthAdjuster = dayOfMonthAdjuster;
  }

  /** An adjuster finding the DayOfWeek in the given week in a month. May be before or after date. */
  private final TemporalAdjuster _dayOfMonthAdjuster;

  /**
   * Gets the dayOfMonthAdjuster.
   * @return the dayOfMonthAdjuster
   */
  public final TemporalAdjuster getDayOfMonthAdjuster() {
    return _dayOfMonthAdjuster;
  }

  /** An adjuster moving to the next quarter. */
  private static final TemporalAdjuster s_nextQuarterAdjuster = new NextQuarterAdjuster();

  /** The IMM Expiry months  */
  private final Set<Month> _futureQuarters = EnumSet.of(Month.MARCH, Month.JUNE, Month.SEPTEMBER, Month.DECEMBER);

  @Override
  public Temporal adjustInto(Temporal temporal) {
    LocalDate date = LocalDate.from(temporal);
    LocalDate result = date.with(_dayOfMonthAdjuster);
    if (_futureQuarters.contains(date.getMonth()) && result.isAfter(date)) {  // in a quarter  // CSIGNORE
      // do nothing
    } else {
      result = date.with(s_nextQuarterAdjuster).with(_dayOfMonthAdjuster);
    }
    return temporal.with(result);
  }

}
