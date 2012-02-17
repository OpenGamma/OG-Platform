/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.StubType;

/**
 * The 'Actual/Actual ICMA Normal' day count.
 */
public class ActualActualICMANormal extends ActualTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    throw new NotImplementedException("Cannot get daycount fraction; need information about the coupon and payment frequency");
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    return getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, StubType.NONE);
  }

  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear,
      final StubType stubType) {
    return getAccruedInterest(previousCouponDate.toLocalDate(), date.toLocalDate(), nextCouponDate.toLocalDate(), coupon, paymentsPerYear, stubType);
  }

  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear,
      final StubType stubType) {
    testDates(previousCouponDate, date, nextCouponDate);
    Validate.notNull(stubType, "stub type");

    long daysBetween, daysBetweenCoupons;
    final long previousCouponDateJulian = previousCouponDate.toModifiedJulianDays();
    final long nextCouponDateJulian = nextCouponDate.toModifiedJulianDays();
    final long dateJulian = date.toModifiedJulianDays();
    final int months = (int) (12 / paymentsPerYear);
    switch (stubType) {
      case NONE: {
        daysBetween = dateJulian - previousCouponDateJulian;
        daysBetweenCoupons = nextCouponDateJulian - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case SHORT_START: {
        final LocalDate notionalStart = nextCouponDate.minusMonths(months);
        daysBetweenCoupons = nextCouponDateJulian - notionalStart.toModifiedJulianDays();
        daysBetween = dateJulian - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case LONG_START: {
        final long firstNotionalJulian = nextCouponDate.minusMonths(months * 2).toModifiedJulianDays();
        final long secondNotionalJulian = nextCouponDate.minusMonths(months).toModifiedJulianDays();
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
        final LocalDate notionalEnd = previousCouponDate.plusMonths(months);
        daysBetweenCoupons = notionalEnd.toModifiedJulianDays() - previousCouponDateJulian;
        daysBetween = dateJulian - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case LONG_END: {
        final long firstNotionalJulian = previousCouponDate.plusMonths(months).toModifiedJulianDays();
        final long secondNotionalJulian = previousCouponDate.plusMonths(2 * months).toModifiedJulianDays();
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
    return "Actual/Actual ICMA Normal";
  }

}
