/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationYearOnYearInterpolationDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 *  A wrapper class for a AnnuityDefinition containing CapFloorInflationYearOnYearInterpolationDefinition.
 */
public class AnnuityCapFloorInflationYearOnYearInterpolationDefinition extends AnnuityCouponDefinition<CapFloorInflationYearOnYearInterpolationDefinition> {

  /**
   * Constructor from a list of inflation year on year cap/floor.
   * @param payments The InflationYearOnYearMonthly coupons.
   * @param calendar The calendar
   */
  public AnnuityCapFloorInflationYearOnYearInterpolationDefinition(final CapFloorInflationYearOnYearInterpolationDefinition[] payments,
      final Calendar calendar) {
    super(payments, calendar);
  }

  /**
   * Year on year annuity (or Year on year coupon leg) constructor from standard description. The coupon are fixing in advance and payment in arrears.
   * The month lag is the conventional one.
   * @param priceIndex The price index.
   * @param settlementDate The settlement date.
   * @param notional The notional.
   * @param totalTenor The  period of the annuity.
   * @param paymentPeriod The payment period of the coupons.
   * @param businessDayConvention the business day convention.
   * @param calendar the calendar.
   * @param endOfMonth The end-of-month flag.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The day count of the coupons.
   * @param payNotional Payer (true) / receiver (false) flag.
   * @param weightStart The weight on the first month index in the interpolation of the index at the denominator.
   * @param weightEnd The weight on the first month index in the interpolation of the index at the numerator.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The Year on year coupon leg.
   */
  public static AnnuityCapFloorInflationYearOnYearInterpolationDefinition from(final IndexPrice priceIndex, final ZonedDateTime settlementDate,
      final double notional, final Period totalTenor, final Period paymentPeriod, final BusinessDayConvention businessDayConvention, final Calendar calendar,
      final boolean endOfMonth, final int conventionalMonthLag, final int monthLag, final boolean payNotional, final double weightStart, final double weightEnd,
      final ZonedDateTime lastKnownFixingDate, final double strike, final boolean isCap) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    ArgumentChecker.notNull(paymentPeriod, "Payment period");
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, totalTenor, paymentPeriod, true, false, businessDayConvention, calendar, endOfMonth);

    final CapFloorInflationYearOnYearInterpolationDefinition[] coupons = new CapFloorInflationYearOnYearInterpolationDefinition[paymentDates.length];
    coupons[0] = CapFloorInflationYearOnYearInterpolationDefinition.from(settlementDate, paymentDates[0], notional, priceIndex, lastKnownFixingDate, conventionalMonthLag, monthLag, weightStart,
        weightEnd, strike, isCap);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CapFloorInflationYearOnYearInterpolationDefinition.from(paymentDates[loopcpn - 1], paymentDates[loopcpn], notional, priceIndex, lastKnownFixingDate, conventionalMonthLag,
          monthLag, weightStart, weightEnd, strike, isCap);
    }
    return new AnnuityCapFloorInflationYearOnYearInterpolationDefinition(coupons, calendar);
  }

  /**
   * Year on year annuity (or Year on year coupon leg) constructor without weights (this calculation is done in the coupon). The coupon are fixing in advance and payment in arrears.
   * @param priceIndex The price index.
   * @param settlementDate The settlement date.
   * @param notional The notional.
   * @param totalTenor The  period of the annuity.
   * @param paymentPeriod The payment period of the coupons.
   * @param businessDayConvention the business day convention.
   * @param calendar the calendar.
   * @param endOfMonth The end-of-month flag.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The day count of the coupons.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The Year on year coupon leg.
   */
  public static AnnuityCapFloorInflationYearOnYearInterpolationDefinition from(final IndexPrice priceIndex, final ZonedDateTime settlementDate,
      final double notional, final Period totalTenor, final Period paymentPeriod, final BusinessDayConvention businessDayConvention, final Calendar calendar,
      final boolean endOfMonth, final int conventionalMonthLag, final int monthLag, final ZonedDateTime lastKnownFixingDate, final double strike,
      final boolean isCap) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    ArgumentChecker.notNull(paymentPeriod, "Payment period");
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, totalTenor, paymentPeriod, true, false, businessDayConvention, calendar, endOfMonth);

    final CapFloorInflationYearOnYearInterpolationDefinition[] coupons = new CapFloorInflationYearOnYearInterpolationDefinition[paymentDates.length];
    coupons[0] = CapFloorInflationYearOnYearInterpolationDefinition.from(settlementDate, paymentDates[0], notional, priceIndex, conventionalMonthLag, monthLag, lastKnownFixingDate,
        strike, isCap);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CapFloorInflationYearOnYearInterpolationDefinition.from(paymentDates[loopcpn - 1], paymentDates[loopcpn], notional, priceIndex, conventionalMonthLag, monthLag,
          lastKnownFixingDate,
          strike, isCap);
    }
    return new AnnuityCapFloorInflationYearOnYearInterpolationDefinition(coupons, calendar);
  }
}
