/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.util.time.DateUtil;

/**
 * Utility to calculate schedules.
 */
public final class ScheduleCalculator {

  /**
   * A singleton empty array.
   */
  private static final ZonedDateTime[] EMPTY_ARRAY = new ZonedDateTime[0];

  /**
   * Restricted constructor.
   */
  private ScheduleCalculator() {
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the unadjusted date schedule.
   * 
   * @param effectiveDate  the effective date, not null
   * @param maturityDate  the maturity date, not null
   * @param frequency  how many times a year dates occur, not null
   * @return the schedule, not null
   */
  public static ZonedDateTime[] getUnadjustedDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final Frequency frequency) {
    Validate.notNull(effectiveDate);
    Validate.notNull(maturityDate);
    Validate.notNull(frequency);
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date was after maturity");
    }
    return getUnadjustedDateSchedule(effectiveDate, effectiveDate, maturityDate, frequency);
  }

  /**
   * Calculates the unadjusted date schedule.
   * 
   * @param effectiveDate  the effective date, not null
   * @param accrualDate  the accrual date, not null
   * @param maturityDate  the maturity date, not null
   * @param frequency  how many times a year dates occur, not null
   * @return the schedule, not null
   */
  public static ZonedDateTime[] getUnadjustedDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime accrualDate, final ZonedDateTime maturityDate, final Frequency frequency) {
    Validate.notNull(effectiveDate);
    Validate.notNull(accrualDate);
    Validate.notNull(maturityDate);
    Validate.notNull(frequency);
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date was after maturity");
    }
    if (accrualDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Accrual date was after maturity");
    }

    // TODO what if there's no valid date between accrual date and maturity date?
    PeriodFrequency periodFrequency;
    if (frequency instanceof PeriodFrequency) {
      periodFrequency = (PeriodFrequency) frequency;
    } else if (frequency instanceof SimpleFrequency) {
      periodFrequency = ((SimpleFrequency) frequency).toPeriodFrequency();
    } else {
      throw new IllegalArgumentException("For the moment can only deal with PeriodFrequency and SimpleFrequency");
    }
    final Period period = periodFrequency.getPeriod();
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    ZonedDateTime date = effectiveDate; // TODO this is only correct if effective date = accrual date
    date = date.plus(period);
    while (isWithinSwapLifetime(date, maturityDate)) { // REVIEW: could speed this up by working out how many periods between start and end date?
      dates.add(date);
      date = date.plus(period);
    }
    return dates.toArray(EMPTY_ARRAY);
  }

  //-------------------------------------------------------------------------
  /**
   * Counts back from maturityDate, filling to equally spaced dates frequency
   * times a year until the last date <b>after</b> effective date.
   * 
   * @param effectiveDate  the date that terminates to back counting (i.e. the first date is after this date), not null
   * @param maturityDate  the date to count back from, not null
   * @param frequency  how many times a year dates occur, not null
   * @return the first date after effectiveDate (i.e. effectiveDate is <b>not</b> included to the maturityDate (included) 
   */
  public static ZonedDateTime[] getBackwardsUnadjustedDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final Frequency frequency) {
    Validate.notNull(effectiveDate);
    Validate.notNull(maturityDate);
    Validate.notNull(frequency);
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date was after maturity");
    }

    PeriodFrequency periodFrequency;
    if (frequency instanceof PeriodFrequency) {
      periodFrequency = (PeriodFrequency) frequency;
    } else if (frequency instanceof SimpleFrequency) {
      periodFrequency = ((SimpleFrequency) frequency).toPeriodFrequency();
    } else {
      throw new IllegalArgumentException("For the moment can only deal with PeriodFrequency and SimpleFrequency");
    }
    final Period period = periodFrequency.getPeriod();
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    ZonedDateTime date = maturityDate;

    //TODO review the tolerance given 
    while (date.isAfter(effectiveDate) && DateUtil.getExactDaysBetween(effectiveDate, date) > 4.0) {
      dates.add(date);
      date = date.minus(period);
    }

    Collections.sort(dates);
    return dates.toArray(EMPTY_ARRAY);
  }

  private static boolean isWithinSwapLifetime(final ZonedDateTime date, final ZonedDateTime maturity) {
    // TODO change me urgently
    if (date.isBefore(maturity)) {
      return true;
    }
    if (DateUtil.getDaysBetween(date, maturity) < 7) {
      return true;
    }
    return false;
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime[] dates, final BusinessDayConvention convention, final Calendar calendar) {
    return getAdjustedDateSchedule(dates, convention, calendar, 0);
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime[] dates, final BusinessDayConvention convention, final Calendar calendar, final int settlementDays) {
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(convention);
    Validate.notNull(calendar);

    final int n = dates.length;
    final ZonedDateTime[] result = new ZonedDateTime[n];
    for (int i = 0; i < n; i++) {
      final ZonedDateTime date = convention.adjustDate(calendar, dates[i]);
      int adjustDays = settlementDays;
      for (int j = 0; j < settlementDays; j++) {
        if (!calendar.isWorkingDay(date.toLocalDate())) {
          adjustDays++;
        }
      }
      result[i] = date.plusDays(adjustDays);
    }
    return result;
  }

  public static ZonedDateTime[] getSettlementDateSchedule(final ZonedDateTime[] dates, final Calendar calendar, final BusinessDayConvention businessDayConvention, final int settlementDays) {
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(calendar);
    final int n = dates.length;
    final ZonedDateTime[] result = new ZonedDateTime[n];
    for (int i = 0; i < n; i++) {
      ZonedDateTime date = businessDayConvention.adjustDate(calendar, dates[i].plusDays(1));
      for (int j = 0; j < settlementDays; j++) {
        date = businessDayConvention.adjustDate(calendar, date.plusDays(1));
      }
      result[i] = date;
    }
    return result;
  }

  public static LocalDate[] getSettlementDateSchedule(final LocalDate[] dates, final Calendar calendar, final BusinessDayConvention businessDayConvention, final int settlementDays) {
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(calendar);
    final int n = dates.length;
    final LocalDate[] result = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      LocalDate date = businessDayConvention.adjustDate(calendar, dates[i].plusDays(1));
      for (int j = 0; j < settlementDays; j++) {
        date = businessDayConvention.adjustDate(calendar, date.plusDays(1));
      }
      result[i] = date;
    }
    return result;
  }

  public static ZonedDateTime[] getAdjustedResetDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime[] dates, final BusinessDayConvention convention, final Calendar calendar,
      final int settlementDays) {
    Validate.notNull(effectiveDate);
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(convention);
    Validate.notNull(calendar);

    final int n = dates.length;
    final ZonedDateTime[] result = new ZonedDateTime[n];
    result[0] = effectiveDate;
    for (int i = 1; i < n; i++) {
      result[i] = convention.adjustDate(calendar, dates[i - 1].plusDays(settlementDays));
    }
    return result;
  }

  public static ZonedDateTime[] getAdjustedMaturityDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime[] dates, final BusinessDayConvention convention, final Calendar calendar,
      final Frequency frequency) {
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(convention);
    Validate.notNull(calendar);
    Validate.notNull(frequency);

    PeriodFrequency periodFrequency;
    if (frequency instanceof PeriodFrequency) {
      periodFrequency = (PeriodFrequency) frequency;
    } else if (frequency instanceof SimpleFrequency) {
      periodFrequency = ((SimpleFrequency) frequency).toPeriodFrequency();
    } else {
      throw new IllegalArgumentException("For the moment can only deal with PeriodFrequency and SimpleFrequency");
    }
    final Period period = periodFrequency.getPeriod();

    final int n = dates.length;
    final ZonedDateTime[] results = new ZonedDateTime[n];
    results[0] = effectiveDate.plus(period);
    for (int i = 1; i < n; i++) {
      results[i] = convention.adjustDate(calendar, dates[i - 1].plus(period)); //TODO need to further shift these dates by a convention 
    }

    return results;

  }

  /**
   * Converts a set of dates into time periods in years for a specified date and using a specified day count convention.
   * 
   * @param dates  a set of dates, not null
   * @param dayCount  the day count convention, not null
   * @param fromDate  the date from which to measure the time period to the dates, not null
   * @return a double array of time periods (in years) - if a date is <b>before</b> the fromDate as negative value is returned, not null
   */
  public static double[] getTimes(final ZonedDateTime[] dates, final DayCount dayCount, final ZonedDateTime fromDate) {
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(dayCount);
    Validate.notNull(fromDate);

    final int n = dates.length;
    final double[] result = new double[n];
    double yearFrac;
    for (int i = 0; i < (n); i++) {
      if (dates[i].isAfter(fromDate)) {
        yearFrac = dayCount.getDayCountFraction(fromDate, dates[i]);
      } else {
        yearFrac = -dayCount.getDayCountFraction(dates[i], fromDate);

      }
      result[i] = yearFrac;
    }

    return result;
  }

  public static int numberOfNegativeValues(final double[] periods) {
    int count = 0;
    for (final double period : periods) {
      if (period < 0.0) {
        count++;
      }
    }
    return count;
  }

  public static double[] removeFirstNValues(final double[] data, final int n) {
    return Arrays.copyOfRange(data, n, data.length);
  }

  public static double[] getYearFractions(final ZonedDateTime[] dates, final DayCount dayCount, final ZonedDateTime fromDate) {
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(dayCount);
    Validate.notNull(fromDate);
    final int n = dates.length;

    final double[] result = new double[n];
    result[0] = dayCount.getDayCountFraction(fromDate, dates[0]);
    for (int i = 1; i < n; i++) {
      result[i] = dayCount.getDayCountFraction(dates[i - 1], dates[i]);
    }
    return result;
  }
}
