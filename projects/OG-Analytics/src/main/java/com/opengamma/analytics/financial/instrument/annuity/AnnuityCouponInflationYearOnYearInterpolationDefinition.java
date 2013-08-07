/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearInterpolationDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 *  A wrapper class for a AnnuityDefinition containing CouponInflationYearOnYearInterpolationDefinition.
 */
public class AnnuityCouponInflationYearOnYearInterpolationDefinition extends AnnuityCouponDefinition<CouponInflationYearOnYearInterpolationDefinition> {

  /**
   * Constructor from a list of inflation year on year coupons.
   * @param payments The InflationYearOnYearMonthly coupons.
   * @param calendar The calendar
   */
  public AnnuityCouponInflationYearOnYearInterpolationDefinition(final CouponInflationYearOnYearInterpolationDefinition[] payments,
      final Calendar calendar) {
    super(payments, calendar);
  }

  /**
   * Year on year annuity (or Year on year coupon leg) constructor from standard description. The coupon are fixing in advance and payment in arrears.
   * The month lag is the conventional one.
   * @param priceIndex The price index.
   * @param settlementDate The settlement date.
   * @param notional The notional.
   * @param totalPeriod The  period of the annuity.
   * @param paymentPeriod The payment period of the coupons.
   * @param businessDayConvention the business day convention.
   * @param calendar the calendar.
   * @param endOfMonth The end-of-month flag.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param payNotional Payer (true) / receiver (false) flag.
   * @param weightStart The weight on the first month index in the interpolation of the index at the denominator.
   * @param weightEnd The weight on the first month index in the interpolation of the index at the numerator.
   * @return The Year on year coupon leg.
   */
  public static AnnuityCouponInflationYearOnYearInterpolationDefinition from(final IndexPrice priceIndex, final ZonedDateTime settlementDate,
      final double notional, final Period totalPeriod, final Period paymentPeriod, final BusinessDayConvention businessDayConvention, final Calendar calendar,
      final boolean endOfMonth, final int conventionalMonthLag, final boolean payNotional, final double weightStart, final double weightEnd) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    ArgumentChecker.notNull(paymentPeriod, "Payment period");
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, paymentPeriod, totalPeriod, true, false, businessDayConvention, calendar, endOfMonth);

    final CouponInflationYearOnYearInterpolationDefinition[] coupons = new CouponInflationYearOnYearInterpolationDefinition[paymentDates.length];
    coupons[0] = CouponInflationYearOnYearInterpolationDefinition.from(settlementDate, paymentDates[0], notional, priceIndex, conventionalMonthLag, payNotional, weightStart, weightEnd);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CouponInflationYearOnYearInterpolationDefinition.from(paymentDates[loopcpn - 1], paymentDates[loopcpn], notional, priceIndex, conventionalMonthLag, payNotional,
          weightStart, weightEnd);
    }
    return new AnnuityCouponInflationYearOnYearInterpolationDefinition(coupons, calendar);
  }

  /**
   * Year on year annuity (or Year on year coupon leg) constructor without weights (this calculation is done in the coupon). The coupon are fixing in advance and payment in arrears.
   * @param priceIndex The price index.
   * @param settlementDate The settlement date.
   * @param notional The notional.
   * @param totalPeriod The  period of the annuity.
   * @param paymentPeriod The payment period of the coupons.
   * @param businessDayConvention the business day convention.
   * @param calendar the calendar.
   * @param endOfMonth The end-of-month flag.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The day count of the coupons.
   * @param payNotional Payer (true) / receiver (false) flag.
   * @return The Year on year coupon leg.
   */
  public static AnnuityCouponInflationYearOnYearInterpolationDefinition from(final IndexPrice priceIndex, final ZonedDateTime settlementDate,
      final double notional, final Period totalPeriod, final Period paymentPeriod, final BusinessDayConvention businessDayConvention, final Calendar calendar,
      final boolean endOfMonth, final int conventionalMonthLag, final int monthLag, final boolean payNotional) {
    ArgumentChecker.notNull(settlementDate, "settlement date");

    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    ArgumentChecker.notNull(paymentPeriod, "Payment period");
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, paymentPeriod, totalPeriod, true, false, businessDayConvention, calendar, endOfMonth);

    final CouponInflationYearOnYearInterpolationDefinition[] coupons = new CouponInflationYearOnYearInterpolationDefinition[paymentDates.length];
    coupons[0] = CouponInflationYearOnYearInterpolationDefinition.from(settlementDate, paymentDates[0], notional, priceIndex, conventionalMonthLag, monthLag, payNotional);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CouponInflationYearOnYearInterpolationDefinition.from(paymentDates[loopcpn - 1], paymentDates[loopcpn], notional, priceIndex, conventionalMonthLag, monthLag, payNotional);
    }
    return new AnnuityCouponInflationYearOnYearInterpolationDefinition(coupons, calendar);
  }

}
