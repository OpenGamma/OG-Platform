/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.instrument.index.IndexON;
import com.opengamma.financial.instrument.payment.CouponOISSimplifiedDefinition;
import com.opengamma.financial.schedule.ScheduleCalculator;

/**
 * A wrapper class for a AnnuityDefinition containing CouponOISSimplifiedDefinition.
 */
public class AnnuityCouponOISSimplifiedDefinition extends AnnuityDefinition<CouponOISSimplifiedDefinition> {

  /**
   * Constructor from a list of OIS coupons.
   * @param payments The coupons.
   */
  public AnnuityCouponOISSimplifiedDefinition(CouponOISSimplifiedDefinition[] payments) {
    super(payments);
  }

  /**
   * Annuity builder from the financial details.
   * @param settlementDate The settlement date.
   * @param tenorAnnuity The annuity tenor.
   * @param tenorCoupon The coupons tenor.
   * @param notional The notional.
   * @param index The OIS index.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param settlementDays The number of days between last fixing of each coupon and the coupon payment (also called spot lag). 
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @return The annuity.
   */
  public static AnnuityCouponOISSimplifiedDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final Period tenorCoupon, final double notional, final IndexON index,
      final boolean isPayer, final int settlementDays, final BusinessDayConvention businessDayConvention, final boolean isEOM) {
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, tenorCoupon, businessDayConvention, index.getCalendar(), isEOM);
    return AnnuityCouponOISSimplifiedDefinition.from(settlementDate, endFixingPeriodDate, notional, index, isPayer, settlementDays);
  }

  /**
   * Annuity builder from the financial details.
   * @param settlementDate The settlement date.
   * @param maturityDate The maturity date. The maturity date is the end date of the last fixing period.
   * @param frequency The coupons frequency.
   * @param notional The notional.
   * @param index The OIS index.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param settlementDays The number of days between last fixing of each coupon and the coupon payment (also called spot lag). 
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @return The annuity.
   */
  public static AnnuityCouponOISSimplifiedDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Frequency frequency, final double notional, final IndexON index,
      final boolean isPayer, final int settlementDays, final BusinessDayConvention businessDayConvention, final boolean isEOM) {
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, frequency, businessDayConvention, index.getCalendar(), isEOM);
    return AnnuityCouponOISSimplifiedDefinition.from(settlementDate, endFixingPeriodDate, notional, index, isPayer, settlementDays);
  }

  /**
   * Annuity builder from the financial details.
   * @param settlementDate The settlement date.
   * @param endFixingPeriodDate An array of date with the end fixing period date for each coupon.
   * @param notional The notional.
   * @param index The OIS index.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param settlementDays The number of days between last fixing of each coupon and the coupon payment (also called spot lag). 
   * @return
   */
  private static AnnuityCouponOISSimplifiedDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime[] endFixingPeriodDate, final double notional, final IndexON index,
      final boolean isPayer, final int settlementDays) {
    final double sign = isPayer ? -1.0 : 1.0;
    double notionalSigned = sign * notional;
    final CouponOISSimplifiedDefinition[] coupons = new CouponOISSimplifiedDefinition[endFixingPeriodDate.length];
    coupons[0] = CouponOISSimplifiedDefinition.from(index, settlementDate, endFixingPeriodDate[0], notionalSigned, settlementDays);
    for (int loopcpn = 1; loopcpn < endFixingPeriodDate.length; loopcpn++) {
      coupons[loopcpn] = CouponOISSimplifiedDefinition.from(index, endFixingPeriodDate[loopcpn - 1], endFixingPeriodDate[loopcpn], notionalSigned, settlementDays);
    }
    return new AnnuityCouponOISSimplifiedDefinition(coupons);
  }

}
