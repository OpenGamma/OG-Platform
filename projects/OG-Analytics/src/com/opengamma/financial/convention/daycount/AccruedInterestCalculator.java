/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import java.util.Arrays;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.StubCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Utility to calculate the accrued interest.
 */
public final class AccruedInterestCalculator {

  /**
   * Restricted constructor.
   */
  private AccruedInterestCalculator() {
  }

  /**
   * Calculates the accrued interest for a {@code ZonedDateTime}.
   * 
   * @param dayCount  the day count convention, not null
   * @param settlementDate  the settlement date, not null
   * @param nominalDates  the nominalDates, not null, no null elements
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @param exDividendDays the number of ex-dividend days
   * @param calendar The working day calendar to be used in calculating ex-dividend dates, not null
   * @return the accrued interest
   */
  public static double getAccruedInterest(final DayCount dayCount, final ZonedDateTime settlementDate, final ZonedDateTime[] nominalDates, final double coupon, final int paymentsPerYear,
      final boolean isEndOfMonthConvention, final int exDividendDays, final Calendar calendar) {
    Validate.notNull(dayCount, "day-count");
    Validate.notNull(settlementDate, "date");
    Validate.noNullElements(nominalDates, "nominalDates");
    Validate.notNull(calendar, "calendar");
    Validate.isTrue(paymentsPerYear > 0);
    Validate.isTrue(exDividendDays >= 0);
    final int i = Arrays.binarySearch(nominalDates, settlementDate);
    if (i > 0) {
      return 0;
    }
    final int index = -i - 2;
    final int length = nominalDates.length;
    Validate.isTrue(index >= 0, "Settlement date is before first accrual date");
    Validate.isTrue(index < length, "Settlement date is after maturity date");
    final double accruedInterest = getAccruedInterest(dayCount, index, length, nominalDates[index], settlementDate, nominalDates[index + 1], coupon, paymentsPerYear, isEndOfMonthConvention);
    ZonedDateTime exDividendDate = nominalDates[index + 1];
    for (int j = 0; j < exDividendDays; j++) {
      while (!calendar.isWorkingDay(exDividendDate.toLocalDate())) {
        exDividendDate = exDividendDate.minusDays(1);
      }
      exDividendDate = exDividendDate.minusDays(1);
    }
    if (exDividendDays != 0 && exDividendDate.isBefore(settlementDate)) {
      return accruedInterest - coupon;
    }
    return accruedInterest;
  }

  /**
   * Calculates the accrued interest for a {@code ZonedDateTime}.
   * 
   * @param dayCount  the day count convention, not null
   * @param settlementDate  the settlement date, not null
   * @param nominalDates  the nominalDates, not null, no null elements
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @param exDividendDays the number of ex-dividend days
   * @param index The index of the previous coupon in the nominalDates array
   * @param calendar The working day calendar to be used in calculating ex-dividend dates, not null
   * @return the accrued interest
   */
  public static double getAccruedInterest(final DayCount dayCount, final ZonedDateTime settlementDate, final ZonedDateTime[] nominalDates, final double coupon, final double paymentsPerYear,
      final boolean isEndOfMonthConvention, final int exDividendDays, final int index, final Calendar calendar) {
    Validate.notNull(dayCount, "day-count");
    Validate.notNull(settlementDate, "date");
    Validate.noNullElements(nominalDates, "nominalDates");
    Validate.notNull(calendar, "calendar");
    Validate.isTrue(paymentsPerYear > 0);
    Validate.isTrue(exDividendDays >= 0);
    final int length = nominalDates.length;
    Validate.isTrue(index >= 0 && index < length);
    final double accruedInterest = getAccruedInterest(dayCount, index, length, nominalDates[index], settlementDate, nominalDates[index + 1], coupon, paymentsPerYear, isEndOfMonthConvention);
    ZonedDateTime exDividendDate = nominalDates[index + 1];
    for (int i = 0; i < exDividendDays; i++) {
      while (!calendar.isWorkingDay(exDividendDate.toLocalDate())) {
        exDividendDate = exDividendDate.minusDays(1);
      }
      exDividendDate = exDividendDate.minusDays(1);
    }
    if (exDividendDays != 0 && exDividendDate.isBefore(settlementDate)) {
      return accruedInterest - coupon;
    }
    return accruedInterest;
  }

  /**
   * Calculates the accrued interest for a {@code LocalDate}.
   * 
   * @param dayCount  the day count convention, not null
   * @param settlementDate  the settlement date, not null
   * @param nominalDates  the nominalDates, not null, no null elements
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @param exDividendDays the number of ex-dividend days
   * @param calendar The working day calendar to be used in calculating ex-dividend dates, not null
   * @return the accrued interest
   */
  //TODO one where you can pass in array of coupons
  public static double getAccruedInterest(final DayCount dayCount, final LocalDate settlementDate, final LocalDate[] nominalDates, final double coupon, final double paymentsPerYear,
      final boolean isEndOfMonthConvention, final int exDividendDays, final Calendar calendar) {
    Validate.notNull(dayCount, "day-count");
    Validate.notNull(settlementDate, "date");
    Validate.noNullElements(nominalDates, "nominalDates");
    Validate.notNull(calendar, "calendar");
    Validate.isTrue(paymentsPerYear > 0);
    Validate.isTrue(exDividendDays >= 0);
    final int i = Arrays.binarySearch(nominalDates, settlementDate);
    if (i > 0) {
      return 0;
    }
    final int index = -i - 2;
    final int length = nominalDates.length;
    if (index < 0) {
      throw new IllegalArgumentException("Settlement date is before first accrual date");
    }
    if (index == length) {
      throw new IllegalArgumentException("Settlement date is after maturity date");
    }
    final ZonedDateTime previousCouponDate = ZonedDateTime.of(LocalDateTime.ofMidnight(nominalDates[index]), TimeZone.UTC);
    final ZonedDateTime date = ZonedDateTime.of(LocalDateTime.ofMidnight(settlementDate), TimeZone.UTC);
    final ZonedDateTime nextCouponDate = ZonedDateTime.of(LocalDateTime.ofMidnight(nominalDates[index + 1]), TimeZone.UTC);
    final double accruedInterest = getAccruedInterest(dayCount, index, length, previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, isEndOfMonthConvention);
    LocalDate exDividendDate = nominalDates[index + 1];
    for (int j = 0; j < exDividendDays; j++) {
      while (!calendar.isWorkingDay(exDividendDate)) {
        exDividendDate = exDividendDate.minusDays(1);
      }
      exDividendDate = exDividendDate.minusDays(1);
    }
    if (exDividendDays != 0 && exDividendDate.isBefore(settlementDate)) {
      return accruedInterest - coupon;
    }
    return accruedInterest;
  }

  /**
   * Calculates the accrued interest for a {@code LocalDate}.
   * 
   * @param dayCount  the day count convention, not null
   * @param settlementDate  the settlement date, not null
   * @param nominalDates  the nominalDates, not null, no null elements
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @param exDividendDays the number of ex-dividend days
   * @param index The index of the previous coupon in the nominalDates
   * @param calendar The working day calendar to be used in calculating ex-dividend dates, not null
   * @return the accrued interest
   */
  public static double getAccruedInterest(final DayCount dayCount, final LocalDate settlementDate, final LocalDate[] nominalDates, final double coupon, final double paymentsPerYear,
      final boolean isEndOfMonthConvention, final int exDividendDays, final int index, final Calendar calendar) {
    Validate.notNull(dayCount, "day-count");
    Validate.notNull(settlementDate, "date");
    Validate.noNullElements(nominalDates, "nominalDates");
    Validate.notNull(calendar, "calendar");
    Validate.isTrue(paymentsPerYear > 0);
    Validate.isTrue(exDividendDays >= 0);
    final int length = nominalDates.length;
    Validate.isTrue(index >= 0 && index < length);
    final ZonedDateTime previousCouponDate = ZonedDateTime.of(LocalDateTime.ofMidnight(nominalDates[index]), TimeZone.UTC);
    final ZonedDateTime date = ZonedDateTime.of(LocalDateTime.ofMidnight(settlementDate), TimeZone.UTC);
    final ZonedDateTime nextCouponDate = ZonedDateTime.of(LocalDateTime.ofMidnight(nominalDates[index + 1]), TimeZone.UTC);
    double accruedInterest;
    if (date.isAfter(nextCouponDate)) {
      accruedInterest = 0;
    } else {
      accruedInterest = getAccruedInterest(dayCount, index, length, previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, isEndOfMonthConvention);
    }
    LocalDate exDividendDate = nominalDates[index + 1];
    for (int i = 0; i < exDividendDays; i++) {
      while (!calendar.isWorkingDay(exDividendDate)) {
        exDividendDate = exDividendDate.minusDays(1);
      }
      exDividendDate = exDividendDate.minusDays(1);
    }
    if (exDividendDays != 0 && exDividendDate.isBefore(settlementDate)) {
      return accruedInterest - coupon;
    }
    return accruedInterest;
  }

  /**
   * Calculates the accrued interest for a {@code LocalDate}.
   * 
   * @param dayCount  the day count convention, not null
   * @param settlementDate  the settlement date, not null
   * @param nominalDates  the nominalDates, not null, no null elements
   * @param settlementDates  the settlement dates, not null, no null elements
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @param exDividendDays the number of ex-dividend days
   * @param index The index of the previous coupon in the nominalDates
   * @param calendar The working day calendar used to calculate the ex-dividend date, not null
   * @return the accrued interest
   */
  public static double getAccruedInterest(final DayCount dayCount, final LocalDate settlementDate, final LocalDate[] nominalDates, final LocalDate[] settlementDates, final double coupon,
      final double paymentsPerYear, final boolean isEndOfMonthConvention, final int exDividendDays, final int index, final Calendar calendar) {
    Validate.notNull(dayCount, "day-count");
    Validate.notNull(settlementDate, "date");
    Validate.notNull(calendar, "calendar");
    Validate.noNullElements(nominalDates, "nominalDates");
    Validate.noNullElements(settlementDates, "settlementDates");
    Validate.isTrue(paymentsPerYear > 0);
    Validate.isTrue(exDividendDays >= 0);
    final int length = nominalDates.length;
    Validate.isTrue(index >= 0 && index < length);
    final ZonedDateTime previousCouponDate = ZonedDateTime.of(LocalDateTime.ofMidnight(nominalDates[index]), TimeZone.UTC);
    final ZonedDateTime date = ZonedDateTime.of(LocalDateTime.ofMidnight(settlementDate), TimeZone.UTC);
    final ZonedDateTime nextCouponDate = ZonedDateTime.of(LocalDateTime.ofMidnight(nominalDates[index + 1]), TimeZone.UTC);
    double accruedInterest;
    if (date.isAfter(nextCouponDate)) {
      if (date.isBefore(ZonedDateTime.of(LocalDateTime.ofMidnight(settlementDates[index + 1]), TimeZone.UTC))) {
        accruedInterest = coupon;
      } else {
        accruedInterest = 0;
      }
    } else {
      accruedInterest = getAccruedInterest(dayCount, index, length, previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, isEndOfMonthConvention);
    }
    LocalDate exDividendDate = nominalDates[index + 1];
    for (int i = 0; i < exDividendDays; i++) {
      while (!calendar.isWorkingDay(exDividendDate)) {
        exDividendDate = exDividendDate.minusDays(1);
      }
      exDividendDate = exDividendDate.minusDays(1);
    }
    if (exDividendDays != 0 && exDividendDate.isBefore(settlementDate)) {
      return accruedInterest - coupon;
    }
    return accruedInterest;
  }

  public static double getAccruedInterest(final DayCount dayCount, final int index, final int length, final ZonedDateTime previousCouponDate, final ZonedDateTime date,
      final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear, final boolean isEndOfMonthConvention) {
    if (dayCount instanceof ActualActualICMANormal) {
      if (isEndOfMonthConvention) {
        throw new IllegalArgumentException("Inconsistent definition; asked for accrual with EOM convention but are not using Actual/Actual ICMA");
      }
      final StubType stubType = getStubType(index, length, previousCouponDate, nextCouponDate, paymentsPerYear, isEndOfMonthConvention);
      return ((ActualActualICMANormal) dayCount).getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, stubType);

    } else if (dayCount instanceof ActualActualICMA) {
      final StubType stubType = getStubType(index, length, previousCouponDate, nextCouponDate, paymentsPerYear, isEndOfMonthConvention);
      return ((ActualActualICMA) dayCount).getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, stubType);

    } else if (dayCount instanceof ThirtyUThreeSixty) {
      return ((ThirtyUThreeSixty) dayCount).getAccruedInterest(previousCouponDate, date, coupon, isEndOfMonthConvention);
    }
    return dayCount.getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear);

  }

  private static StubType getStubType(final int index, final int length, final ZonedDateTime previousCouponDate, final ZonedDateTime nextCouponDate, final double paymentsPerYear,
      final boolean isEndOfMonthConvention) {
    StubType stubType;
    if (index == 0) {
      stubType = StubCalculator.getStartStubType(new ZonedDateTime[] {previousCouponDate, nextCouponDate}, paymentsPerYear, isEndOfMonthConvention);

    } else if (index == length - 2) {
      stubType = StubCalculator.getEndStubType(new ZonedDateTime[] {previousCouponDate, nextCouponDate}, paymentsPerYear, isEndOfMonthConvention);

    } else {
      stubType = StubType.NONE;
    }
    return stubType;
  }

}
