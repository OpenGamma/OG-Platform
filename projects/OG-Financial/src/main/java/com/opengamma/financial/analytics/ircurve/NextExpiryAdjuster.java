/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.util.ArgumentChecker;

/**
 * {@code DatAdjuster} that finds the next given day of month in the next IMM Future Expiry Month. (EnumSet.of(Month.MARCH, Month.JUNE, Month.SEPTEMBER, Month.DECEMBER)) <p>
 * e.g. For IR Future Options, this is typically the 3rd Wednesday. For USD and GBP Equity Index Options, this is the Saturday after the Third Friday. <p>
 */
public class NextExpiryAdjuster implements TemporalAdjuster {

  /** An adjuster finding the DayOfWeek in the given week in a month. May be before or after date. */
  private final TemporalAdjuster _dayOfMonthAdjuster;

  /** An adjuster moving to the next quarter. */
  private final NextQuarterAdjuster _nextQuarterAdjuster;

  /** Specify the day and week in the month. (eg 3rd Wednesday) Adjuster returns that day in next IMM Expiry Month
     * @param week Ordinal of week in month, beginning from 1.
     * @param day DayOfWeek
     */
  public NextExpiryAdjuster(final int week, final DayOfWeek day) {
    _dayOfMonthAdjuster = TemporalAdjusters.dayOfWeekInMonth(week, day);
    _nextQuarterAdjuster = new NextQuarterAdjuster();

  }

  /** Specify the day and week in the month, plus an offset in number of days. Adjuster returns that day in next IMM Expiry Month. <p>
   *  e.g. (3,DayOfWeek.FRIDAY,1) is the Saturday after the 3rd Friday in the month. This is different from the 3rd Saturday.
   * @param week Ordinal of week in month, beginning from 1.
   * @param day DayOfWeek
   * @param offset Integer offset, positive or negative from the result of week,day.
   */
  public NextExpiryAdjuster(final int week, final DayOfWeek day, final int offset) {
    _dayOfMonthAdjuster = new DayOfWeekInMonthPlusOffsetAdjuster(week, day, offset);
    _nextQuarterAdjuster = new NextQuarterAdjuster();
  }

  /**
   * Specify the day and week in the month, plus an offset in number of days. Adjuster returns that day in next IMM Expiry Month. <p>
   * @param dayOfMonthAdjuster provide the day of month adjuster directly
   */
  public NextExpiryAdjuster(final TemporalAdjuster dayOfMonthAdjuster) {
    ArgumentChecker.notNull(dayOfMonthAdjuster, "dayOfMonthAdjuster");
    _dayOfMonthAdjuster = dayOfMonthAdjuster;
    _nextQuarterAdjuster = new NextQuarterAdjuster();
  }

  /**
   * @return the dayOfMonthAdjuster
   */
  public final TemporalAdjuster getDayOfMonthAdjuster() {
    return _dayOfMonthAdjuster;
  }

  /**
   * @return the nextQuarterAdjuster
   */
  public TemporalAdjuster getNextQuarterAdjuster() {
    return _nextQuarterAdjuster;
  }

  @Override
  public Temporal adjustInto(final Temporal temporal) {
    final LocalDate date = LocalDate.from(temporal);
    LocalDate result = date.with(_dayOfMonthAdjuster);
    if (_nextQuarterAdjuster.getFutureQuarters().contains(date.getMonth()) && result.isAfter(date)) {  // in a quarter  // CSIGNORE
      // do nothing
    } else {
      result = date.with(_nextQuarterAdjuster).with(_dayOfMonthAdjuster);
    }
    return temporal.with(result);
  }

}
