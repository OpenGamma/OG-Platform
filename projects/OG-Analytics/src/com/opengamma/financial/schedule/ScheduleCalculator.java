/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.time.calendar.DateAdjusters;
import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.businessday.PrecedingBusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.instrument.index.GeneratorDeposit;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.util.time.DateUtils;

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

  // Already reviewed

  /**
    * Return a good business date computed from a given date and shifted by a certain number of business days.
    * If the number of shift days is 0, the return date is the next business day.
    * If the number of shift days is non-zero (positive or negative), a 0 shift is first applied and then a one business day shift is applied as many time as the absolute value of the shift.
    * If the shift is positive, the one business day is to the future., if the shift is negative, the one business day is to the past.
    * @param date The initial date.
    * @param shiftDays The number of days of the adjustment. Can be negative or positive.
    * @param calendar The calendar representing the god business days.
    * @return The adjusted dates.
    */
  public static ZonedDateTime getAdjustedDate(final ZonedDateTime date, final int shiftDays, final Calendar calendar) {
    Validate.notNull(date);
    Validate.notNull(calendar);
    ZonedDateTime result = date;
    while (!calendar.isWorkingDay(result.toLocalDate())) {
      result = result.plusDays(1);
    }
    if (shiftDays > 0) {
      for (int loopday = 0; loopday < shiftDays; loopday++) {
        result = result.plusDays(1);
        while (!calendar.isWorkingDay(result.toLocalDate())) {
          result = result.plusDays(1);
        }
      }
    } else {
      for (int loopday = 0; loopday < -shiftDays; loopday++) {
        result = result.minusDays(1);
        while (!calendar.isWorkingDay(result.toLocalDate())) {
          result = result.minusDays(1);
        }
      }
    }
    return result;
  }

  /**
   * Compute the end date of a period from the start date, the tenor and the conventions without end-of-month convention.
   * @param startDate The period start date.
   * @param tenor The period tenor.
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @return The end date.
   */
  public static ZonedDateTime getAdjustedDate(final ZonedDateTime startDate, final Period tenor, final BusinessDayConvention convention, final Calendar calendar) {
    Validate.notNull(startDate);
    Validate.notNull(convention);
    Validate.notNull(calendar);
    Validate.notNull(tenor);
    ZonedDateTime endDate = startDate.plus(tenor); // Unadjusted date.
    return convention.adjustDate(calendar, endDate); // Adjusted by Business day convention
  }

  /**
   * Compute the end date of a period from the start date, the tenor and the conventions.
   * @param startDate The period start date.
   * @param tenor The period tenor.
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param endOfMonthRule True if end-of-month rule applies, false if it does not. 
   * The rule applies when the start date is the last business day of the month and the period is a number of months or years, not days or weeks.
   * When the rule applies, the end date is the last business day of the month.
   * @return The end date.
   */
  public static ZonedDateTime getAdjustedDate(final ZonedDateTime startDate, final Period tenor, final BusinessDayConvention convention, final Calendar calendar, boolean endOfMonthRule) {
    Validate.notNull(startDate);
    Validate.notNull(convention);
    Validate.notNull(calendar);
    Validate.notNull(tenor);
    ZonedDateTime endDate = startDate.plus(tenor); // Unadjusted date.
    // Adjusted to month-end: when start date is last business day of the month, the end date is the last business day of the month.
    boolean isStartDateEOM = (startDate.getMonthOfYear() != getAdjustedDate(startDate, 1, calendar).getMonthOfYear());
    if ((tenor.getDays() == 0) & (endOfMonthRule) & (isStartDateEOM)) {
      BusinessDayConvention preceding = new PrecedingBusinessDayConvention();
      return preceding.adjustDate(calendar, endDate.with(DateAdjusters.lastDayOfMonth()));
    }
    return convention.adjustDate(calendar, endDate); // Adjusted by Business day convention
  }

  /**
   * Compute the end date of a period from the start date, the tenor and the conventions.
   * @param startDate The period start date.
   * @param tenor The period tenor.
   * @param generator The deposit generator with the required conventions.
   * @return The end date.
   */
  public static ZonedDateTime getAdjustedDate(final ZonedDateTime startDate, final Period tenor, final GeneratorDeposit generator) {
    Validate.notNull(generator, "Generator");
    return getAdjustedDate(startDate, tenor, generator.getBusinessDayConvention(), generator.getCalendar(), generator.isEndOfMonth());
  }

  /**
   * Compute the end date of a period from the start date and a Ibor index. The period between the start date and the end date is the index tenor.
   * @param startDate The period start date.
   * @param index The Ibor index.
   * @return The end date.
   */
  public static ZonedDateTime getAdjustedDate(final ZonedDateTime startDate, final IborIndex index) {
    Validate.notNull(index, "Index");
    return getAdjustedDate(startDate, index.getTenor(), index.getBusinessDayConvention(), index.getCalendar(), index.isEndOfMonth());
  }

  /**
   * Compute a schedule of unadjusted dates from a start date, an end date and the period between dates.
   * @param startDate The start date.
   * @param endDate The end date.
   * @param tenorPeriod The period between each date.
   * @param stubShort In case the the periods do not fit exactly between start and end date, is the remaining interval shorter (true) or longer (false) than the requested period.
   * @param fromEnd The dates in the schedule can be computed from the end date (true) or from the start date (false).
   * @return The date schedule (not including the start date).
   */
  public static ZonedDateTime[] getUnadjustedDateSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final Period tenorPeriod, final boolean stubShort, final boolean fromEnd) {
    Validate.notNull(startDate, "Start date");
    Validate.notNull(endDate, "End date");
    Validate.notNull(tenorPeriod, "Period tenor");
    Validate.isTrue(startDate.isBefore(endDate), "Start date should be strictly before end date");
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    int nbPeriod = 0;
    if (!fromEnd) { // Add the periods from the start date
      ZonedDateTime date = startDate.plus(tenorPeriod);
      while (date.isBefore(endDate)) { // date is strictly before endDate
        dates.add(date);
        nbPeriod++;
        date = startDate.plus(tenorPeriod.multipliedBy(nbPeriod + 1));
      }
      if (!stubShort && !date.equals(endDate) && nbPeriod >= 1) { // For long stub the last date before end date, if any, is removed.
        dates.remove(nbPeriod - 1);
      }
      dates.add(endDate);
      return dates.toArray(EMPTY_ARRAY);
    }
    // From end - Subtract the periods from the end date
    ZonedDateTime date = endDate;
    while (date.isAfter(startDate)) { // date is strictly after startDate
      dates.add(date);
      nbPeriod++;
      date = endDate.minus(tenorPeriod.multipliedBy(nbPeriod));
    }
    if (!stubShort && !date.equals(startDate) && nbPeriod >= 1) { // For long stub the last date before end date, if any, is removed.
      dates.remove(nbPeriod - 1);
    }
    Collections.sort(dates); // To obtain the dates in chronological order.
    return dates.toArray(EMPTY_ARRAY);
  }

  /**
   * Adjust an array of date with a given convention and EOM flag.
   * @param dates The array of unadjusted dates.
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param eomApply The flag indicating if the EOM apply, i.e. if the flag is true, the adjusted date is the last business day of the unadjusted date.  
   * @return The adjusted dates.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime[] dates, final BusinessDayConvention convention, final Calendar calendar, final boolean eomApply) {
    ZonedDateTime[] result = new ZonedDateTime[dates.length];
    if (eomApply) {
      BusinessDayConvention precedingDBC = new PrecedingBusinessDayConvention(); //To ensure that the date stays in the current month.
      for (int loopdate = 0; loopdate < dates.length; loopdate++) {
        result[loopdate] = precedingDBC.adjustDate(calendar, dates[loopdate].with(DateAdjusters.lastDayOfMonth()));
      }
      return result;
    }
    for (int loopdate = 0; loopdate < dates.length; loopdate++) {
      result[loopdate] = convention.adjustDate(calendar, dates[loopdate]);
    }
    return result;
  }

  // TODO: review the methods below.

  // -------------------------------------------------------------------------
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

  //TODO: add doc
  public static ZonedDateTime[] getUnadjustedDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime accrualDate, final ZonedDateTime maturityDate, final Period period) {
    Validate.notNull(effectiveDate);
    Validate.notNull(accrualDate);
    Validate.notNull(maturityDate);
    Validate.notNull(period);
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date was after maturity");
    }
    if (accrualDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Accrual date was after maturity");
    }

    // TODO what if there's no valid date between accrual date and maturity date?
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    int nbPeriod = 1; // M 26-Aug
    ZonedDateTime date = effectiveDate; // TODO this is only correct if effective date = accrual date
    date = date.plus(period);
    while (isWithinSwapLifetime(date, maturityDate)) { // REVIEW: could speed this up by working out how many periods between start and end date?
      dates.add(date);
      nbPeriod++; // M 26-Aug
      date = effectiveDate.plus(period.multipliedBy(nbPeriod)); // M 26-Aug date = date.plus(period);
    }
    return dates.toArray(EMPTY_ARRAY);
  }

  // -------------------------------------------------------------------------
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

    // TODO review the tolerance given
    while (date.isAfter(effectiveDate) && DateUtils.getExactDaysBetween(effectiveDate, date) > 4.0) {
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
    if (DateUtils.getDaysBetween(date, maturity) < 7) {
      return true;
    }
    return false;
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime[] dates, final BusinessDayConvention convention, final Calendar calendar) {
    return getAdjustedDateSchedule(dates, convention, calendar, 0);
  }

  /**
   * Return the dates adjusted by a certain number of business days.
   * @param dates The initial dates.
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param settlementDays The number of days of the adjustment. Can be negative or positive.
   * @return The adjusted dates.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime[] dates, final BusinessDayConvention convention, final Calendar calendar, final int settlementDays) {
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(convention);
    Validate.notNull(calendar);
    final int n = dates.length;
    final ZonedDateTime[] result = new ZonedDateTime[n];
    for (int i = 0; i < n; i++) {
      ZonedDateTime date = convention.adjustDate(calendar, dates[i]);
      if (settlementDays > 0) {
        for (int loopday = 0; loopday < settlementDays; loopday++) {
          date = date.plusDays(1);
          while (!calendar.isWorkingDay(date.toLocalDate())) {
            date = date.plusDays(1);
          }
        }
      } else {
        for (int loopday = 0; loopday < -settlementDays; loopday++) {
          date = date.minusDays(1);
          while (!calendar.isWorkingDay(date.toLocalDate())) {
            date = date.minusDays(1);
          }
        }
      }
      result[i] = date;
    }
    return result;
  }

  /**
   * Construct an array of dates according the a start date, an end date, the period between dates and the conventions. 
   * The start date is not included in the array. The date are constructed forward and the stub period, if any, is last. 
   * The end date is always included in the schedule.
   * @param startDate The reference initial date for the construction.
   * @param endDate The end date. Usually unadjusted.
   * @param period The period between payments.
   * @param businessDayConvention The business day convention.
   * @param calendar The applicable calendar.
   * @param isEOM The end-of-month rule flag.
   * @param stubShort Flag indicating if the stub, if any, is short (true) or long (false).
   * @return The array of dates.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, ZonedDateTime endDate, Period period, BusinessDayConvention businessDayConvention, Calendar calendar,
      boolean isEOM, boolean stubShort) {
    boolean eomApply = false;
    if (isEOM) {
      BusinessDayConvention following = new FollowingBusinessDayConvention();
      eomApply = (following.adjustDate(calendar, startDate.plusDays(1)).getMonthOfYear() != startDate.getMonthOfYear());
    }
    // When the end-of-month rule applies and the start date is on month-end, the dates are the last business day of the month.
    BusinessDayConvention actualBDC;
    final List<ZonedDateTime> adjustedDates = new ArrayList<ZonedDateTime>();
    ZonedDateTime date = startDate;
    if (eomApply) {
      actualBDC = new PrecedingBusinessDayConvention(); //To ensure that the date stays in the current month.
      date = date.plus(period).with(DateAdjusters.lastDayOfMonth());
      while (date.isBefore(endDate)) { // date is strictly before endDate
        adjustedDates.add(actualBDC.adjustDate(calendar, date));
        date = date.plus(period).with(DateAdjusters.lastDayOfMonth());
      }
    } else {
      actualBDC = businessDayConvention;
      date = date.plus(period);
      while (date.isBefore(endDate)) { // date is strictly before endDate
        adjustedDates.add(businessDayConvention.adjustDate(calendar, date));
        date = date.plus(period);
      }
    }
    // For long stub the last date before end date, if any, is removed.
    if (!stubShort && adjustedDates.size() >= 1) {
      adjustedDates.remove(adjustedDates.size() - 1);
    }
    adjustedDates.add(actualBDC.adjustDate(calendar, endDate)); // the end date
    return adjustedDates.toArray(EMPTY_ARRAY);
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, ZonedDateTime endDate, final Frequency frequency, BusinessDayConvention businessDayConvention,
      Calendar calendar, boolean isEOM) {
    PeriodFrequency periodFrequency;
    if (frequency instanceof PeriodFrequency) {
      periodFrequency = (PeriodFrequency) frequency;
    } else if (frequency instanceof SimpleFrequency) {
      periodFrequency = ((SimpleFrequency) frequency).toPeriodFrequency();
    } else {
      throw new IllegalArgumentException("For the moment can only deal with PeriodFrequency and SimpleFrequency");
    }
    final Period period = periodFrequency.getPeriod();
    return getAdjustedDateSchedule(startDate, endDate, period, businessDayConvention, calendar, isEOM, true);
  }

  /**
   * Construct an array of dates according the a start date, an end date, the period between dates and the conventions. 
   * The start date is not included in the array. The date are constructed forward and the stub period, if any, is last
   * and short. The end date is always included in the schedule.
   * @param startDate The reference initial date for the construction.
   * @param endDate The end date. Usually unadjusted.
   * @param period The period between payments.
   * @param businessDayConvention The business day convention.
   * @param calendar The applicable calendar.
   * @param isEOM The end-of-month rule flag.
   * @return The array of dates.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, ZonedDateTime endDate, Period period, BusinessDayConvention businessDayConvention, Calendar calendar,
      boolean isEOM) {
    return getAdjustedDateSchedule(startDate, endDate, period, businessDayConvention, calendar, isEOM, true);
  }

  /**
   * Construct an array of dates according the a start date, an end date, the period between dates and the conventions. 
   * The start date is not included in the array. The date are constructed forward and the stub period, if any, is last. 
   * The end date is always included in the schedule.
   * @param startDate The reference initial date for the construction.
   * @param tenor The annuity tenor.
   * @param period The period between payments.
   * @param businessDayConvention The business day convention.
   * @param calendar The applicable calendar.
   * @param isEOM The end-of-month rule flag.
   * @param shortStub Flag indicating if the stub, if any, is short (true) or long (false).
   * @return The array of dates.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, Period tenor, Period period, BusinessDayConvention businessDayConvention, Calendar calendar, boolean isEOM,
      boolean shortStub) {
    ZonedDateTime endDate = startDate.plus(tenor);
    return getAdjustedDateSchedule(startDate, endDate, period, businessDayConvention, calendar, isEOM, shortStub);
  }

  /**
   * Construct an array of dates according the a start date, an end date, the period between dates and the conventions. 
   * The start date is not included in the array. The date are constructed forward and the stub period, if any, is short
   * and last. The end date is always included in the schedule.
   * @param startDate The reference initial date for the construction.
   * @param tenorAnnuity The annuity tenor.
   * @param periodPayments The period between payments.
   * @param businessDayConvention The business day convention.
   * @param calendar The applicable calendar.
   * @param isEOM The end-of-month rule flag.
   * @return The array of dates.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, Period tenorAnnuity, Period periodPayments, BusinessDayConvention businessDayConvention, Calendar calendar,
      boolean isEOM) {
    ZonedDateTime endDate = startDate.plus(tenorAnnuity);
    return getAdjustedDateSchedule(startDate, endDate, periodPayments, businessDayConvention, calendar, isEOM, true);
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
      result[i] = convention.adjustDate(calendar, dates[i - 1].minusDays(settlementDays));
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
      results[i] = convention.adjustDate(calendar, dates[i - 1].plus(period)); // TODO need to further shift these dates by a convention
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
