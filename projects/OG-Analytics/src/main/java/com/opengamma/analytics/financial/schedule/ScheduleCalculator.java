/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.util.time.TenorUtils;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.businessday.PrecedingBusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.rolldate.EndOfMonthRollDateAdjuster;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

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
   * @param calendar The calendar representing the good business days.
   * @return The adjusted date.
   */
  public static ZonedDateTime getAdjustedDate(final ZonedDateTime date, final int shiftDays, final Calendar calendar) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(calendar, "calendar");
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
   * Return a good business dates computed from given array of date and shifted by a certain number of business days (one return date for each input date).
   * If the number of shift days is 0, the return date is the next business day.
   * If the number of shift days is non-zero (positive or negative), a 0 shift is first applied and then a one business day shift is applied as many time as the absolute value of the shift.
   * If the shift is positive, the one business day is to the future., if the shift is negative, the one business day is to the past.
   * @param dates The initial dates.
   * @param shiftDays The number of days of the adjustment. Can be negative or positive.
   * @param calendar The calendar representing the good business days.
   * @return The adjusted dates.
   */
  public static ZonedDateTime[] getAdjustedDate(final ZonedDateTime[] dates, final int shiftDays, final Calendar calendar) {
    final int nbDates = dates.length;
    final ZonedDateTime[] result = new ZonedDateTime[nbDates];
    for (int loopdate = 0; loopdate < nbDates; loopdate++) {
      result[loopdate] = getAdjustedDate(dates[loopdate], shiftDays, calendar);
    }
    return result;
  }

  /**
   * Return a good business date computed from a given date and shifted by a certain number of business days. The number of business days is given by the getDays part of a peeriod.
   * If the number of shift days is 0, the return date is the next business day.
   * If the number of shift days is non-zero (positive or negative), a 0 shift is first applied and then a one business day shift is applied as many time as the absolute value of the shift.
   * If the shift is positive, the one business day is to the future., if the shift is negative, the one business day is to the past.
   * @param date The initial date.
   * @param shiftDays The number of days of the adjustment as a period.
   * @param calendar The calendar representing the good business days.
   * @return The adjusted dates.
   */
  public static ZonedDateTime getAdjustedDate(final ZonedDateTime date, final Period shiftDays, final Calendar calendar) {
    ArgumentChecker.notNull(shiftDays, "shift days");
    return getAdjustedDate(date, shiftDays.getDays(), calendar);
  }

  /**
   * Return a good business date computed from a given date and shifted by a certain number of business days.
   * This version uses LocalDate.
   * If the number of shift days is 0, the return date is the next business day.
   * If the number of shift days is non-zero (positive or negative), a 0 shift is first applied and then a one business day shift is applied as many time as the absolute value of the shift.
   * If the shift is positive, the one business day is to the future., if the shift is negative, the one business day is to the past.
   * @param date The initial date.
   * @param shiftDays The number of days of the adjustment. Can be negative or positive.
   * @param calendar The calendar representing the good business days.
   * @return The adjusted dates.
   */
  public static LocalDate getAdjustedDate(final LocalDate date, final int shiftDays, final Calendar calendar) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(calendar, "calendar");
    LocalDate result = date;
    while (!calendar.isWorkingDay(result)) {
      result = result.plusDays(1);
    }
    if (shiftDays > 0) {
      for (int loopday = 0; loopday < shiftDays; loopday++) {
        result = result.plusDays(1);
        while (!calendar.isWorkingDay(result)) {
          result = result.plusDays(1);
        }
      }
    } else {
      for (int loopday = 0; loopday < -shiftDays; loopday++) {
        result = result.minusDays(1);
        while (!calendar.isWorkingDay(result)) {
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
    ArgumentChecker.notNull(startDate, "start date");
    ArgumentChecker.notNull(convention, "convention");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(tenor, "tenor");
    final ZonedDateTime endDate = startDate.plus(tenor); // Unadjusted date.
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
  public static ZonedDateTime getAdjustedDate(final ZonedDateTime startDate, final Period tenor, final BusinessDayConvention convention, final Calendar calendar,
      final boolean endOfMonthRule) {
    ArgumentChecker.notNull(startDate, "Start date");
    ArgumentChecker.notNull(convention, "Convention");
    ArgumentChecker.notNull(calendar, "Calendar");
    ArgumentChecker.notNull(tenor, "Tenor");
    final ZonedDateTime endDate = startDate.plus(tenor); // Unadjusted date.
    // Adjusted to month-end: when start date is last business day of the month, the end date is the last business day of the month.
    final boolean isStartDateEOM = (startDate.getMonth() != getAdjustedDate(startDate, 1, calendar).getMonth());
    if ((tenor.getDays() == 0) & (endOfMonthRule) & (isStartDateEOM)) {
      final BusinessDayConvention preceding = new PrecedingBusinessDayConvention();
      return preceding.adjustDate(calendar, endDate.with(TemporalAdjusters.lastDayOfMonth()));
    }
    return convention.adjustDate(calendar, endDate); // Adjusted by Business day convention
  }

  /**
   * Compute the end date of a period from the start date, period, conventions and roll date adjuster. If the roll date
   * adjuster is end of month, then only apply when the start date is last business day of the month and the period is a
   * number of months or years, not days or weeks.
   *
   * @param startDate the start date
   * @param period the period between the start and end date.
   * @param convention the business day convention used to adjust the end date.
   * @param calendar the calendar used to adjust the end date.
   * @param rollDateAdjuster the roll date adjuster used to adjust the end date, before the conventions are applied.
   * @return The end date.
   */
  public static ZonedDateTime getAdjustedDate(
      final ZonedDateTime startDate,
      final Period period,
      final BusinessDayConvention convention,
      final Calendar calendar,
      final RollDateAdjuster rollDateAdjuster) {
    ArgumentChecker.notNull(startDate, "Start date");
    ArgumentChecker.notNull(convention, "Convention");
    ArgumentChecker.notNull(calendar, "Calendar");
    ArgumentChecker.notNull(period, "Tenor");
    ZonedDateTime endDate = startDate.plus(period); // Unadjusted date.
    // Adjusted to month-end: when start date is last business day of the month, the end date is the last business day of the month.
    if (rollDateAdjuster instanceof EndOfMonthRollDateAdjuster) {
      final boolean isStartDateEOM = (startDate.getMonth() != getAdjustedDate(startDate, 1, calendar).getMonth());
      if ((period.getDays() == 0) && isStartDateEOM) {
        final BusinessDayConvention preceding = new PrecedingBusinessDayConvention();
        return preceding.adjustDate(calendar, endDate.with(TemporalAdjusters.lastDayOfMonth()));
      }
    } else if (rollDateAdjuster != null) {
      /*
       * If we are rolling forward with a positive period and we have a day of month adjuster, we don't want to roll
       * backwards.
       */
      final ZonedDateTime rolledEndDate = endDate.with(rollDateAdjuster);
      if (!period.isNegative() && rolledEndDate.isAfter(endDate)) {
        endDate = rolledEndDate;
      }
    }
    return convention.adjustDate(calendar, endDate); // Adjusted by Business day convention
  }

  /**
   * Compute the end date of a period from the start date, the tenor and the conventions.
   * @param startDate The period start date.
   * @param tenor The tenor.
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param endOfMonthRule True if end-of-month rule applies, false if it does not.
   * The rule applies when the start date is the last business day of the month and the period is a number of months or years, not days or business days (ON, TN).
   * When the rule applies, the end date is the last business day of the month.
   * @return The end date.
   */
  public static ZonedDateTime getAdjustedDate(final ZonedDateTime startDate, final Tenor tenor, final BusinessDayConvention convention, final Calendar calendar,
      final boolean endOfMonthRule) {
    ArgumentChecker.notNull(startDate, "Start date");
    ArgumentChecker.notNull(convention, "Convention");
    ArgumentChecker.notNull(calendar, "Calendar");
    ArgumentChecker.notNull(tenor, "Tenor");
    final ZonedDateTime endDate = TenorUtils.adjustDateByTenor(startDate, tenor, calendar, 0);
    if (tenor.isBusinessDayTenor()) { // This handles tenor of the type ON, TN
      return endDate;
    }
    // Adjusted to month-end: when start date is last business day of the month, the end date is the last business day of the month.
    final boolean isStartDateEOM = (startDate.getMonth() != getAdjustedDate(startDate, 1, calendar).getMonth());
    if ((tenor.getPeriod().getDays() == 0) & (endOfMonthRule) & (isStartDateEOM)) {
      final BusinessDayConvention preceding = new PrecedingBusinessDayConvention();
      return preceding.adjustDate(calendar, endDate.with(TemporalAdjusters.lastDayOfMonth()));
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
    ArgumentChecker.notNull(generator, "Generator");
    return getAdjustedDate(startDate, tenor, generator.getBusinessDayConvention(), generator.getCalendar(), generator.isEndOfMonth());
  }

  /**
   * Compute the end date of a period from the start date, a period and a Ibor index. The index is used for the conventions.
   * @param startDate The period start date.
   * @param tenor The period tenor.
   * @param index The Ibor index.
   * @param calendar The holiday calendar.
   * @return The end date.
   */
  public static ZonedDateTime getAdjustedDate(final ZonedDateTime startDate, final Period tenor, final IborIndex index, final Calendar calendar) {
    ArgumentChecker.notNull(index, "Index");
    return getAdjustedDate(startDate, tenor, index.getBusinessDayConvention(), calendar, index.isEndOfMonth());
  }

  /**
   * Compute the end date of a period from the start date and a Ibor index. The period between the start date and the end date is the index tenor.
   * @param startDate The period start date.
   * @param index The Ibor index.
   * @param calendar The holiday calendar.
   * @return The end date.
   */
  public static ZonedDateTime getAdjustedDate(final ZonedDateTime startDate, final IborIndex index, final Calendar calendar) {
    ArgumentChecker.notNull(index, "Index");
    return getAdjustedDate(startDate, index.getTenor(), index.getBusinessDayConvention(), calendar, index.isEndOfMonth());
  }

  /**
   * Compute the end dates of periods from the start dates and a Ibor index. The period between the start date and the end date is the index tenor.
   *  There is one return date for each input date.
   * @param startDates The period start dates.
   * @param index The Ibor index.
   * @param calendar The holiday calendar.
   * @return The end dates.
   */
  public static ZonedDateTime[] getAdjustedDate(final ZonedDateTime[] startDates, final IborIndex index, final Calendar calendar) {
    final int nbDates = startDates.length;
    final ZonedDateTime[] result = new ZonedDateTime[nbDates];
    for (int loopdate = 0; loopdate < nbDates; loopdate++) {
      result[loopdate] = getAdjustedDate(startDates[loopdate], index, calendar);
    }
    return result;
  }

  /**
   * Compute a schedule of unadjusted dates from a start date, an end date and the period between dates.
   * @param startDate The start date.
   * @param endDate The end date.
   * @param tenorPeriod The period between each date.
   * @param stub The stub type.
   * @return The date schedule (not including the start date).
   */
  public static ZonedDateTime[] getUnadjustedDateSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final Period tenorPeriod, final StubType stub) {
    ArgumentChecker.notNull(startDate, "Start date");
    ArgumentChecker.notNull(endDate, "End date");
    ArgumentChecker.notNull(tenorPeriod, "Period tenor");
    ArgumentChecker.isTrue(startDate.isBefore(endDate), "Start date should be strictly before end date");
    final boolean stubShort = stub.equals(StubType.SHORT_END) || stub.equals(StubType.SHORT_START) || stub.equals(StubType.NONE) || stub.equals(StubType.BOTH);
    final boolean fromEnd = isGenerateFromEnd(stub); //  || stub.equals(StubType.NONE); // Implementation note: dates computed from the end.
    final List<ZonedDateTime> dates = new ArrayList<>();
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
    if (!stubShort && !date.equals(startDate) && nbPeriod > 1) { // For long stub the last date before end date, if any, is removed.
      dates.remove(nbPeriod - 1);
    }
    Collections.sort(dates); // To obtain the dates in chronological order.
    return dates.toArray(EMPTY_ARRAY);
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
  public static ZonedDateTime[] getUnadjustedDateSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final Period tenorPeriod, final boolean stubShort,
      final boolean fromEnd) {
    ArgumentChecker.notNull(startDate, "Start date");
    ArgumentChecker.notNull(endDate, "End date");
    ArgumentChecker.notNull(tenorPeriod, "Period tenor");
    ArgumentChecker.isTrue(startDate.isBefore(endDate), "Start date {} should be strictly before end date {}", startDate, endDate);
    final List<ZonedDateTime> dates = new ArrayList<>();
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
    if (!stubShort && !date.equals(startDate) && nbPeriod > 1) { // For long stub the last date before end date, if any, is removed.
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
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime[] dates, final BusinessDayConvention convention, final Calendar calendar,
      final boolean eomApply) {
    final ZonedDateTime[] result = new ZonedDateTime[dates.length];
    if (eomApply) {
      final BusinessDayConvention precedingDBC = new PrecedingBusinessDayConvention(); //To ensure that the date stays in the current month.
      for (int loopdate = 0; loopdate < dates.length; loopdate++) {
        result[loopdate] = precedingDBC.adjustDate(calendar, dates[loopdate].with(TemporalAdjusters.lastDayOfMonth()));
      }
      return result;
    }
    for (int loopdate = 0; loopdate < dates.length; loopdate++) {
      result[loopdate] = convention.adjustDate(calendar, dates[loopdate]);
    }
    return result;
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime[] dates, final BusinessDayConvention convention, final Calendar calendar,
      final boolean eomApply, final RollDateAdjuster adjuster) {
    final ZonedDateTime[] result = new ZonedDateTime[dates.length];
    if (eomApply) {
      final BusinessDayConvention precedingDBC = new PrecedingBusinessDayConvention(); //To ensure that the date stays in the current month.
      for (int loopdate = 0; loopdate < dates.length; loopdate++) {
        result[loopdate] = precedingDBC.adjustDate(calendar, dates[loopdate].with(TemporalAdjusters.lastDayOfMonth()));
      }
      return result;
    }
    if (adjuster != null && !(adjuster instanceof EndOfMonthRollDateAdjuster)) {
      for (int loopdate = 0; loopdate < dates.length; loopdate++) {
        result[loopdate] = convention.adjustDate(calendar, dates[loopdate].with(adjuster));
      }
    } else {
      for (int loopdate = 0; loopdate < dates.length; loopdate++) {
        result[loopdate] = convention.adjustDate(calendar, dates[loopdate]);
      }
    }
    // TODO workaround for PLAT-5695
    final ZonedDateTime[] treeSetResult = new TreeSet<>(Arrays.asList(result)).toArray(new ZonedDateTime[] {});
    return treeSetResult;
  }

  /**
   * Compute a schedule of adjusted dates from a start date, an end date and the period between dates.
   * @param startDate The start date.
   * @param endDate The end date.
   * @param schedulePeriod The period between each date in the schedule.
   * @param stubShort In case the the periods do not fit exactly between start and end date, is the remaining interval shorter (true) or longer (false) than the requested period.
   * @param fromEnd The dates in the schedule can be computed from the end date (true) or from the start date (false).
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param eomRule Flag indicating if the end-of-month rule should be applied.
   * @return The adjusted dates schedule.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final Period schedulePeriod, final boolean stubShort,
      final boolean fromEnd, final BusinessDayConvention convention, final Calendar calendar, final boolean eomRule) {
    final ZonedDateTime[] unadjustedDateSchedule = getUnadjustedDateSchedule(startDate, endDate, schedulePeriod, stubShort, fromEnd);
    final boolean eomApply = (eomRule && eomApplies(fromEnd, startDate, endDate, calendar));
    return getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, eomApply);
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final Period schedulePeriod, final boolean stubShort,
      final boolean fromEnd, final BusinessDayConvention convention, final Calendar calendar, final boolean eomRule, final RollDateAdjuster adjuster) {
    final ZonedDateTime[] unadjustedDateSchedule = getUnadjustedDateSchedule(startDate, endDate, schedulePeriod, stubShort, fromEnd);
    final boolean eomApply = (eomRule && eomApplies(fromEnd, startDate, endDate, calendar));
    return getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, eomApply, adjuster);
  }

  /**
   * Compute a schedule of adjusted dates from a start date, an end date and the period between dates.
   * @param startDate The start date.
   * @param endDate The end date.
   * @param schedulePeriod The period between each date in the schedule.
   * @param stub The stub type.
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param eomRule Flag indicating if the end-of-month rule should be applied.
   * @return The adjusted dates schedule.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final Period schedulePeriod, final StubType stub,
      final BusinessDayConvention convention, final Calendar calendar, final boolean eomRule) {
    final ZonedDateTime[] unadjustedDateSchedule = getUnadjustedDateSchedule(startDate, endDate, schedulePeriod, stub);
    final boolean eomApply = (eomRule && eomApplies(isGenerateFromEnd(stub), startDate, endDate, calendar));
    return getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, eomApply);
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final Period schedulePeriod, final StubType stub,
      final BusinessDayConvention convention, final Calendar calendar, final boolean eomRule, final RollDateAdjuster adjuster) {
    final ZonedDateTime[] unadjustedDateSchedule = getUnadjustedDateSchedule(startDate, endDate, schedulePeriod, stub);
    final boolean eomApply = (eomRule && eomApplies(isGenerateFromEnd(stub), startDate, endDate, calendar));
    return getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, eomApply, adjuster);
  }

  /**
   * Calculate a schedule of adjusted dates, but not the start date.
   * 
   * @param startDate  the start date
   * @param endDate  the end date
   * @param schedulePeriod  the periodic frequency
   * @param stub  the stub type
   * @param convention  the business day convention
   * @param calendar  the holiday calendar
   * @param adjuster  the roll convention
   * @return the schedule array, not including the start date
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime startDate,
      ZonedDateTime endDate,
      Period schedulePeriod,
      StubType stub,
      BusinessDayConvention convention,
      Calendar calendar,
      RollDateAdjuster adjuster) {
    ZonedDateTime[] unadjustedDateSchedule = getUnadjustedDateSchedule(startDate, endDate, schedulePeriod, stub);
    // convert roll adjuster into end-of-month flag and apply correctly
    if (adjuster instanceof EndOfMonthRollDateAdjuster) {
      // if calculating backwards, use end date to determine if rule applies, otherwise use start date
      boolean fromEnd = isGenerateFromEnd(stub);
      final boolean eomApply = eomApplies(fromEnd, startDate, endDate, calendar);
      if (fromEnd) {
        return getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, eomApply, adjuster);
      } else {
        ZonedDateTime[] adj = getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, eomApply, adjuster);
        // ensure date is not rolled beyond end date
        if (adj.length > 0 && adj[adj.length - 1].isAfter(endDate)) {
          adj[adj.length - 1] = convention.adjustDate(calendar, endDate);
        }
        return adj;
      }
    }
    return getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, false, adjuster);
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(
      final ZonedDateTime[] startDates,
      final Period schedulePeriod,
      final BusinessDayConvention businessDayConvention,
      final Calendar calendar,
      final RollDateAdjuster adjuster) {
    final ZonedDateTime[] endDates = new ZonedDateTime[startDates.length];
    for (int i = 0; i < startDates.length; i++) {
      endDates[i] = getAdjustedDate(startDates[i], schedulePeriod, businessDayConvention, calendar, adjuster);
    }
    return endDates;
  }

  /**
   * Compute a schedule of adjusted dates from a start date, an end date and the period between dates.
   * @param startDate The start date.
   * @param endDate The end date.
   * @param scheduleFrequency The frequency of dates in the schedule.
   * @param stubShort In case the the periods do not fit exactly between start and end date, is the remaining interval shorter (true) or longer (false) than the requested period.
   * @param fromEnd The dates in the schedule can be computed from the end date (true) or from the start date (false).
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param eomRule Flag indicating if the end-of-month rule should be applied.
   * @return The adjusted dates schedule.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final Frequency scheduleFrequency,
      final boolean stubShort, final boolean fromEnd, final BusinessDayConvention convention, final Calendar calendar, final boolean eomRule) {
    ArgumentChecker.notNull(scheduleFrequency, "Schedule frequency");
    final Period schedulePeriod = periodFromFrequency(scheduleFrequency);
    return getAdjustedDateSchedule(startDate, endDate, schedulePeriod, stubShort, fromEnd, convention, calendar, eomRule);
  }

  /**
   * Compute a schedule of adjusted dates from a start date, total tenor and the period between dates.
   * @param startDate The start date.
   * @param tenorTotal The total tenor.
   * @param tenorPeriod The period between each date.
   * @param stubShort In case the the periods do not fit exactly between start and end date, is the remaining interval shorter (true) or longer (false) than the requested period.
   * @param fromEnd The dates in the schedule can be computed from the end date (true) or from the start date (false).
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param eomRule Flag indicating if the end-of-month rule should be applied.
   * @return The adjusted dates schedule (not including the start date).
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, final Period tenorTotal, final Period tenorPeriod, final boolean stubShort,
      final boolean fromEnd, final BusinessDayConvention convention, final Calendar calendar, final boolean eomRule) {
    ZonedDateTime endDate = startDate.plus(tenorTotal);
    final ZonedDateTime[] unadjustedDateSchedule = getUnadjustedDateSchedule(startDate, endDate, tenorPeriod, stubShort, fromEnd);
    final boolean eomApply = (eomRule && eomApplies(fromEnd, startDate, endDate, calendar) && (tenorTotal.getDays() == 0));
    // Implementation note: the "tenorTotal.getDays() == 0" condition is required as the rule does not apply for period of less than 1 month (like 1 week).
    return getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, eomApply);
  }

  /**
   * Compute a schedule of adjusted dates from a start date, total tenor and a Ibor index.
   * @param startDate The start date.
   * @param tenorTotal The total tenor.
   * @param stubShort In case the the periods do not fit exactly between start and end date, is the remaining interval shorter (true) or longer (false) than the requested period.
   * @param fromEnd The dates in the schedule can be computed from the end date (true) or from the start date (false).
   * @param index The related ibor index. The period tenor, business day convention, calendar and EOM rule of the index are used.
   * @param calendar The holiday calendar.
   * @return The adjusted dates schedule (not including the start date).
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, final Period tenorTotal, final boolean stubShort, final boolean fromEnd,
      final IborIndex index, final Calendar calendar) {
    return getAdjustedDateSchedule(startDate, tenorTotal, index.getTenor(), stubShort, fromEnd, index.getBusinessDayConvention(), calendar,
        index.isEndOfMonth());
  }

  /**
   * Convert a Frequency to a Period when possible.
   * @param frequency The frequency.
   * @return The converted period.
   */
  private static Period periodFromFrequency(final Frequency frequency) {
    PeriodFrequency periodFrequency;
    if (frequency instanceof PeriodFrequency) {
      periodFrequency = (PeriodFrequency) frequency;
    } else if (frequency instanceof SimpleFrequency) {
      periodFrequency = ((SimpleFrequency) frequency).toPeriodFrequency();
    } else {
      throw new IllegalArgumentException("For the moment can only deal with PeriodFrequency and SimpleFrequency");
    }
    return periodFrequency.getPeriod();
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
    ArgumentChecker.notNull(effectiveDate, "effective date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(frequency, "frequency");
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
  public static ZonedDateTime[] getUnadjustedDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime accrualDate, final ZonedDateTime maturityDate,
      final Frequency frequency) {
    ArgumentChecker.notNull(effectiveDate, "effective date");
    ArgumentChecker.notNull(accrualDate, "accrual date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(frequency, "frequency");
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
    final List<ZonedDateTime> dates = new ArrayList<>();
    ZonedDateTime date = effectiveDate; // TODO this is only correct if effective date = accrual date
    date = date.plus(period);
    while (isWithinSwapLifetime(date, maturityDate)) { // REVIEW: could speed this up by working out how many periods between start and end date?
      dates.add(date);
      date = date.plus(period);
    }
    return dates.toArray(EMPTY_ARRAY);
  }

  //TODO: add doc
  public static ZonedDateTime[] getUnadjustedDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime accrualDate, final ZonedDateTime maturityDate,
      final Period period) {
    ArgumentChecker.notNull(effectiveDate, "effective date");
    ArgumentChecker.notNull(accrualDate, "accrual date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(period, "period");
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date was after maturity");
    }
    if (accrualDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Accrual date was after maturity");
    }

    // TODO what if there's no valid date between accrual date and maturity date?
    final List<ZonedDateTime> dates = new ArrayList<>();
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
    ArgumentChecker.notNull(effectiveDate, "effective date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(frequency, "frequency");
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
    final List<ZonedDateTime> dates = new ArrayList<>();
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
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime[] dates, final BusinessDayConvention convention, final Calendar calendar,
      final int settlementDays) {
    ArgumentChecker.notEmpty(dates, "dates");
    ArgumentChecker.notNull(convention, "convention");
    ArgumentChecker.notNull(calendar, "calendar");
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

  public static ZonedDateTime getAdjustedDate(final ZonedDateTime originalDate, final BusinessDayConvention convention, final Calendar calendar, final int offset) {
    ArgumentChecker.notNull(originalDate, "date");
    ArgumentChecker.notNull(convention, "convention");
    ArgumentChecker.notNull(calendar, "calendar");

    ZonedDateTime date = convention.adjustDate(calendar, originalDate);
    if (offset > 0) {
      for (int loopday = 0; loopday < offset; loopday++) {
        date = date.plusDays(1);
        while (!calendar.isWorkingDay(date.toLocalDate())) {
          date = date.plusDays(1);
        }
      }
    } else {
      for (int loopday = 0; loopday < -offset; loopday++) {
        date = date.minusDays(1);
        while (!calendar.isWorkingDay(date.toLocalDate())) {
          date = date.minusDays(1);
        }
      }
    }
    return date;
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
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final Period period,
      final BusinessDayConvention businessDayConvention, final Calendar calendar, final boolean isEOM, final boolean stubShort) {
    boolean eomApply = false;
    if (isEOM) {
      final BusinessDayConvention following = new FollowingBusinessDayConvention();
      eomApply = (following.adjustDate(calendar, startDate.plusDays(1)).getMonth() != startDate.getMonth());
    }
    // When the end-of-month rule applies and the start date is on month-end, the dates are the last business day of the month.
    BusinessDayConvention actualBDC;
    final List<ZonedDateTime> adjustedDates = new ArrayList<>();
    ZonedDateTime date = startDate;
    if (eomApply) {
      actualBDC = new PrecedingBusinessDayConvention(); //To ensure that the date stays in the current month.
      date = date.plus(period).with(TemporalAdjusters.lastDayOfMonth());
      while (date.isBefore(endDate)) { // date is strictly before endDate
        adjustedDates.add(actualBDC.adjustDate(calendar, date));
        date = date.plus(period).with(TemporalAdjusters.lastDayOfMonth());
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

  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final Frequency frequency,
      final BusinessDayConvention businessDayConvention, final Calendar calendar, final boolean isEOM) {
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
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final Period period,
      final BusinessDayConvention businessDayConvention, final Calendar calendar, final boolean isEOM) {
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
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, final Period tenor, final Period period,
      final BusinessDayConvention businessDayConvention, final Calendar calendar, final boolean isEOM, final boolean shortStub) {
    final ZonedDateTime endDate = startDate.plus(tenor);
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
  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime startDate, final Period tenorAnnuity, final Period periodPayments,
      final BusinessDayConvention businessDayConvention, final Calendar calendar, final boolean isEOM) {
    final ZonedDateTime endDate = startDate.plus(tenorAnnuity);
    return getAdjustedDateSchedule(startDate, endDate, periodPayments, businessDayConvention, calendar, isEOM, true);
  }

  public static ZonedDateTime[] getSettlementDateSchedule(final ZonedDateTime[] dates, final Calendar calendar, final BusinessDayConvention businessDayConvention,
      final int settlementDays) {
    ArgumentChecker.notEmpty(dates, "dates");
    ArgumentChecker.notNull(calendar, "calendar");
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

  public static LocalDate[] getSettlementDateSchedule(final LocalDate[] dates, final Calendar calendar, final BusinessDayConvention businessDayConvention,
      final int settlementDays) {
    ArgumentChecker.notEmpty(dates, "dates");
    ArgumentChecker.notNull(calendar, "calendar");
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

  public static ZonedDateTime[] getAdjustedResetDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime[] dates, final BusinessDayConvention convention,
      final Calendar calendar, final int settlementDays) {
    ArgumentChecker.notNull(effectiveDate, "effective date");
    ArgumentChecker.notEmpty(dates, "dates");
    ArgumentChecker.notNull(convention, "convention");
    ArgumentChecker.notNull(calendar, "calendar");

    final int n = dates.length;
    final ZonedDateTime[] result = new ZonedDateTime[n];
    result[0] = effectiveDate;
    for (int i = 1; i < n; i++) {
      result[i] = convention.adjustDate(calendar, dates[i - 1].minusDays(settlementDays));
    }
    return result;
  }

  public static ZonedDateTime[] getAdjustedMaturityDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime[] dates, final BusinessDayConvention convention,
      final Calendar calendar, final Frequency frequency) {
    ArgumentChecker.notEmpty(dates, "dates");
    ArgumentChecker.notNull(convention, "convention");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(frequency, "frequency");

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
    ArgumentChecker.notEmpty(dates, "dates");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(fromDate, "from date");

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
    ArgumentChecker.notEmpty(dates, "dates");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(fromDate, "from date");
    final int n = dates.length;

    final double[] result = new double[n];
    result[0] = dayCount.getDayCountFraction(fromDate, dates[0]);
    for (int i = 1; i < n; i++) {
      result[i] = dayCount.getDayCountFraction(dates[i - 1], dates[i]);
    }
    return result;
  }

  /**
   * Generates the start dates from the specified start date and set of end dates.
   * @param startDate the first start date.
   * @param endDates the set of end dates to generate start dates from.
   * @return the start dates relative to the end dates.
   */
  public static ZonedDateTime[] getStartDates(final ZonedDateTime startDate, final ZonedDateTime[] endDates) {
    final ZonedDateTime[] startDates = new ZonedDateTime[endDates.length];
    startDates[0] = startDate;
    System.arraycopy(endDates, 0, startDates, 1, endDates.length - 1);
    return startDates;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the schedule is generated from the end.
   * 
   * @param stub  the stub type
   * @return true if generating from the end
   */
  private static boolean isGenerateFromEnd(final StubType stub) {
    return StubType.LONG_START.equals(stub) || StubType.SHORT_START.equals(stub);
  }

  /**
   * Checks if the EOM rule applies.
   * 
   * @param fromEnd  true if generating from the end backwards
   * @param startDate  the start date
   * @param endDate  the end date
   * @param calendar  the holiday calendar
   * @return true if the rule applies
   */
  private static boolean eomApplies(boolean fromEnd, ZonedDateTime startDate, ZonedDateTime endDate, Calendar calendar) {
    if (fromEnd) {
      // end-of-month rule applies if end date is on last day of month (last business day used here)
      return getAdjustedDate(endDate, 1, calendar).getMonth() != endDate.getMonth();
    } else {
      // end-of-month rule applies if start date is on last day of month (last business day used here)
      return getAdjustedDate(startDate, 1, calendar).getMonth() != startDate.getMonth();
    }
  }

}
