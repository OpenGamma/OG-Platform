/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import org.apache.commons.lang.Validate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * A wrapper class for a AnnuityDefinition containing CapFloorInflationYearOnYearMonthlyDefinition
 */
public class AnnuityCapFloorInflationYearOnYearMonthlyDefinition extends AnnuityCouponDefinition<CapFloorInflationYearOnYearMonthlyDefinition> {

  /**
   * Constructor from a list of CapFloorInflationYearOnYearMonthlyDefinition.
   * @param payments The InflationYearOnYearMonthly coupons.
  
   */
  public AnnuityCapFloorInflationYearOnYearMonthlyDefinition(final CapFloorInflationYearOnYearMonthlyDefinition[] payments) {
    super(payments);
  }

  /**
   * Year on year annuity (or Year on year coupon leg) constructor from standard description. The coupon are fixing in advance and payment in arrears. 
   * @param priceIndex The price index.
   * @param settlementDate The settlement date.
   * @param notional The notional.
   * @param totalPeriod The  period of the annuity.
   * @param paymentPeriod The payment period of the coupons.
   * @param businessDayConvention the business day convention. 
   * @param calendar the calendar.
   * @param endOfMonth The end-of-month flag.
   * @param conventionalMonthLag TODO
   * @param monthLag The day count of the coupons.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The Year on year coupon leg.
   */
  public static AnnuityCapFloorInflationYearOnYearMonthlyDefinition from(final IndexPrice priceIndex, final ZonedDateTime settlementDate,
      final double notional, final Period totalPeriod, final Period paymentPeriod, final BusinessDayConvention businessDayConvention, final Calendar calendar,
      final boolean endOfMonth, int conventionalMonthLag, final int monthLag, final ZonedDateTime lastKnownFixingDate, final double strike, final boolean isCap) {
    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(paymentPeriod, "Payment period");
    Validate.notNull(lastKnownFixingDate, "Last known fixing date");
    ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, paymentPeriod, totalPeriod, true, false, businessDayConvention, calendar, endOfMonth);

    final CapFloorInflationYearOnYearMonthlyDefinition[] coupons = new CapFloorInflationYearOnYearMonthlyDefinition[paymentDates.length];
    coupons[0] = CapFloorInflationYearOnYearMonthlyDefinition.from(settlementDate, paymentDates[0], notional, priceIndex, conventionalMonthLag, monthLag, lastKnownFixingDate, strike, isCap);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CapFloorInflationYearOnYearMonthlyDefinition.from(paymentDates[loopcpn - 1], paymentDates[loopcpn], notional, priceIndex, conventionalMonthLag, monthLag, lastKnownFixingDate,
          strike, isCap);
    }
    return new AnnuityCapFloorInflationYearOnYearMonthlyDefinition(coupons);
  }
}
