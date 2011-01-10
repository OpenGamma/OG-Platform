/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.analytics.securityconverters.StubCalculator;
import com.opengamma.financial.analytics.securityconverters.StubType;

/**
 * Utility to calculate the accrued interest.
 */
public final class AccruedInterestCalculator {

  /**
   * Restricted constructor.
   */
  private AccruedInterestCalculator() {
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the accrued interest for a {@code ZonedDateTime}.
   * 
   * @param dayCount  the day count convention, not null
   * @param settlementDate  the settlement date, not null
   * @param schedule  the schedule, not null
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @param exDividendDays the number of ex-dividend days
   * @return the accrued interest
   */
  public static double getAccruedInterest(
      final DayCount dayCount, final ZonedDateTime settlementDate, final ZonedDateTime[] schedule,
      final double coupon, final int paymentsPerYear, final boolean isEndOfMonthConvention, final int exDividendDays) {
    Validate.notNull(dayCount, "day-count");
    Validate.notNull(settlementDate, "date");
    Validate.notNull(schedule, "schedule");
    Validate.noNullElements(schedule, "schedule");
    Validate.isTrue(paymentsPerYear > 0);
    Validate.isTrue(exDividendDays >= 0);
    
    boolean foundDates = false;
    int index = 0;
    final int length = schedule.length;
    for (int i = 0; i < length - 1; i++) {
      if (schedule[i].isBefore(settlementDate) && schedule[i + 1].isAfter(settlementDate)) {
        foundDates = true;
        index = i;
      }
    }
    if (!foundDates) {
      throw new IllegalArgumentException("Could not get previous and next coupon for date " + settlementDate);
    }
    final double accruedInterest = getAccruedInterest(dayCount, index, length, schedule[index], settlementDate, schedule[index + 1], coupon, paymentsPerYear, isEndOfMonthConvention);
    if (exDividendDays != 0 && schedule[index + 1].minusDays(exDividendDays).isBefore(settlementDate)) {
      return accruedInterest - coupon;
    }
    return accruedInterest;
  }

  /**
   * Calculates the accrued interest for a {@code LocalDate}.
   * 
   * @param dayCount  the day count convention, not null
   * @param settlementDate  the settlement date, not null
   * @param schedule  the schedule, not null
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @param exDividendDays the number of ex-dividend days
   * @return the accrued interest
   */
  public static double getAccruedInterest(
      final DayCount dayCount, final LocalDate settlementDate, final LocalDate[] schedule,
      final double coupon, final int paymentsPerYear, final boolean isEndOfMonthConvention, final int exDividendDays) {
    Validate.notNull(dayCount, "day-count");
    Validate.notNull(settlementDate, "date");
    Validate.notNull(schedule, "schedule");
    Validate.noNullElements(schedule, "schedule");
    Validate.isTrue(paymentsPerYear > 0);
    Validate.isTrue(exDividendDays >= 0);
    
    boolean foundDates = false;
    int index = 0;
    final int length = schedule.length;
    for (int i = 0; i < length - 1; i++) {
      if (schedule[i].isBefore(settlementDate) && schedule[i + 1].isAfter(settlementDate)) {
        foundDates = true;
        index = i;
        break;
      }
    }
    if (!foundDates) {
      throw new IllegalArgumentException("Could not get previous and next coupon for date " + settlementDate);
    }
    final ZonedDateTime previousCouponDate = ZonedDateTime.of(LocalDateTime.ofMidnight(schedule[index]), TimeZone.UTC);
    final ZonedDateTime date = ZonedDateTime.of(LocalDateTime.ofMidnight(settlementDate), TimeZone.UTC);
    final ZonedDateTime nextCouponDate = ZonedDateTime.of(LocalDateTime.ofMidnight(schedule[index + 1]), TimeZone.UTC);
    final double accruedInterest = getAccruedInterest(dayCount, index, length, previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, isEndOfMonthConvention);
    if (exDividendDays != 0 && schedule[index + 1].minusDays(exDividendDays).isBefore(settlementDate)) {
      return accruedInterest - coupon;
    }
    return accruedInterest;
  }

  private static double getAccruedInterest(
      final DayCount dayCount, final int index, final int length, final ZonedDateTime previousCouponDate, final ZonedDateTime date,
      final ZonedDateTime nextCouponDate, final double coupon, final int paymentsPerYear, final boolean isEndOfMonthConvention) {
    
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
      return ((ThirtyUThreeSixty) dayCount).getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, isEndOfMonthConvention);
    }
    return dayCount.getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear);

  }

  private static StubType getStubType(
      final int index, final int length, final ZonedDateTime previousCouponDate, final ZonedDateTime nextCouponDate,
      final int paymentsPerYear, final boolean isEndOfMonthConvention) {
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
