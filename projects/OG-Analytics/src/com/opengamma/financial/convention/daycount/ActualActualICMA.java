/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.CalendricalMatchers;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.analytics.securityconverters.StubType;

/**
 * The 'Actual/Actual ICMA' day count.
 */
public class ActualActualICMA extends ActualTypeDayCount {

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    throw new NotImplementedException("Cannot get daycount fraction; need information about the coupon and payment frequency");
  }

  @Override
  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear) {
    return getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, StubType.NONE);
  }

  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear,
      final StubType stubType) {
    testDates(previousCouponDate, date, nextCouponDate);
    Validate.notNull(stubType, "stub type");

    final LocalDate previous = previousCouponDate.toLocalDate();
    final LocalDate next = nextCouponDate.toLocalDate();
    long daysBetween, daysBetweenCoupons;
    final long previousCouponDateJulian = previous.toModifiedJulianDays();
    final long nextCouponDateJulian = next.toModifiedJulianDays();
    final long dateJulian = date.toLocalDate().toModifiedJulianDays();
    final int months = (int) (12 / paymentsPerYear);
    switch (stubType) {
      case NONE: {
        daysBetween = dateJulian - previousCouponDateJulian;
        daysBetweenCoupons = next.toModifiedJulianDays() - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case SHORT_START: {
        final LocalDate notionalStart = getEOMAdjustedDate(next, next.minusMonths(months));
        daysBetweenCoupons = nextCouponDateJulian - notionalStart.toLocalDate().toModifiedJulianDays();
        daysBetween = dateJulian - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case LONG_START: {
        final long firstNotionalJulian = getEOMAdjustedDate(next, next.minusMonths(months * 2)).toModifiedJulianDays();
        final long secondNotionalJulian = getEOMAdjustedDate(next, next.minusMonths(months)).toModifiedJulianDays();
        final long daysBetweenStub = secondNotionalJulian - previousCouponDateJulian;
        final double daysBetweenTwoNotionalCoupons = secondNotionalJulian - firstNotionalJulian;
        if (dateJulian > secondNotionalJulian) {
          daysBetween = dateJulian - secondNotionalJulian;
          return coupon * (daysBetweenStub / daysBetweenTwoNotionalCoupons + 1) / paymentsPerYear;
        }
        daysBetween = dateJulian - firstNotionalJulian;
        return coupon * (daysBetween / daysBetweenTwoNotionalCoupons) / paymentsPerYear;
      }
      case SHORT_END: {
        final LocalDate notionalEnd = getEOMAdjustedDate(previous, previous.plusMonths(months));
        daysBetweenCoupons = notionalEnd.toModifiedJulianDays() - previousCouponDateJulian;
        daysBetween = dateJulian - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case LONG_END: {
        final long firstNotionalJulian = getEOMAdjustedDate(previous, previous.plusMonths(months)).toModifiedJulianDays();
        final long secondNotionalJulian = getEOMAdjustedDate(previous, previous.plusMonths(2 * months)).toModifiedJulianDays();
        final long daysBetweenPreviousAndFirstNotional = firstNotionalJulian - previousCouponDateJulian;
        if (dateJulian < firstNotionalJulian) {
          daysBetween = dateJulian - previousCouponDateJulian;
          return coupon * daysBetween / daysBetweenPreviousAndFirstNotional / paymentsPerYear;
        }
        final long daysBetweenStub = dateJulian - firstNotionalJulian;
        final double daysBetweenTwoNotionalCoupons = secondNotionalJulian - firstNotionalJulian;
        return coupon * (1 + daysBetweenStub / daysBetweenTwoNotionalCoupons) / paymentsPerYear;
      }
      default:
        throw new IllegalArgumentException("Cannot handle stub type " + stubType);
    }
  }

  @Override
  public String getConventionName() {
    return "Actual/Actual ICMA";
  }

  //-------------------------------------------------------------------------
  /**
   * Adjusts the date to the last day of month if necessary.
   * 
   * @param comparison  the date to check as to being the last day of month, not null
   * @param date  the date to adjust, not null
   * @return the adjusted date, not null
   */
  private static LocalDate getEOMAdjustedDate(final LocalDate comparison, final LocalDate date) {
    if (comparison.matches(CalendricalMatchers.lastDayOfMonth())) {
      return date.with(DateAdjusters.lastDayOfMonth());
    }
    return date;
  }

}
