/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.securityconverters;

import javax.time.calendar.CalendricalMatchers;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.DateProvider;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

/**
 * Utility to calculate the stub type.
 */
public final class StubCalculator {

  /**
   * Restricted constructor.
   */
  private StubCalculator() {
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the start stub type from a schedule and number of payments per year.
   * <p>
   * The {@code DateProvider[]} argument allows callers to pass in arrays of any class
   * that implements {@code DateProvider}, such as {@code LocalDate[]}.
   * 
   * @param schedule  the schedule, at least size 2, not null
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @return the stub type, not null
   */
  public static StubType getStartStubType(final DateProvider[] schedule, final int paymentsPerYear) {
    return getStartStubType(schedule, paymentsPerYear, false);
  }

  /**
   * Calculates the start stub type from a schedule, number of payments per year and the end of month flag.
   * <p>
   * The {@code DateProvider[]} argument allows callers to pass in arrays of any class
   * that implements {@code DateProvider}, such as {@code LocalDate[]}.
   * 
   * @param schedule  the schedule, at least size 2, not null
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @return the stub type, not null
   */
  public static StubType getStartStubType(final DateProvider[] schedule, final double paymentsPerYear, final boolean isEndOfMonthConvention) {
    Validate.notNull(schedule, "schedule");
    Validate.noNullElements(schedule, "schedule");
    Validate.isTrue(paymentsPerYear > 0);
    Validate.isTrue(12 % paymentsPerYear == 0);

    final int months = (int) (12 / paymentsPerYear);
    final LocalDate first = LocalDate.of(schedule[0]);
    final LocalDate second = LocalDate.of(schedule[1]);
    LocalDate date;
    if (isEndOfMonthConvention && second.matches(CalendricalMatchers.lastDayOfMonth())) {
      date = second.minusMonths(months);
      date = date.with(DateAdjusters.lastDayOfMonth());
    } else {
      date = second.minusMonths(months);
    }
    if (date.equals(first)) {
      return StubType.NONE;
    }
    if (date.isBefore(first)) {
      return StubType.SHORT_START;
    }
    return StubType.LONG_START;
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the end stub type from a schedule and number of payments per year.
   * <p>
   * The {@code DateProvider[]} argument allows callers to pass in arrays of any class
   * that implements {@code DateProvider}, such as {@code LocalDate[]}.
   * 
   * @param schedule  the schedule, at least size 2, not null
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @return the stub type, not null
   */
  public static StubType getEndStubType(final DateProvider[] schedule, final int paymentsPerYear) {
    return getEndStubType(schedule, paymentsPerYear, false);
  }

  /**
   * Calculates the end stub type from a schedule, number of payments per year and the end of month flag.
   * <p>
   * The {@code DateProvider[]} argument allows callers to pass in arrays of any class
   * that implements {@code DateProvider}, such as {@code LocalDate[]}.
   * 
   * @param schedule  the schedule, at least size 2, not null
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @return the stub type, not null
   */
  public static StubType getEndStubType(final DateProvider[] schedule, final double paymentsPerYear, final boolean isEndOfMonthConvention) {
    Validate.notNull(schedule, "schedule");
    Validate.noNullElements(schedule, "schedule");
    Validate.isTrue(paymentsPerYear > 0);
    Validate.isTrue(12 % paymentsPerYear == 0);

    final int months = (int) (12 / paymentsPerYear);
    final int n = schedule.length;
    final LocalDate first = LocalDate.of(schedule[n - 2]);
    final LocalDate second = LocalDate.of(schedule[n - 1]);
    LocalDate date;
    if (isEndOfMonthConvention && first.matches(CalendricalMatchers.lastDayOfMonth())) {
      date = first.plusMonths(months);
      date = date.with(DateAdjusters.lastDayOfMonth());
    } else {
      date = first.plusMonths(months);
    }
    if (date.equals(second)) {
      return StubType.NONE;
    }
    if (date.isAfter(second)) {
      return StubType.SHORT_END;
    }
    return StubType.LONG_END;
  }

}
