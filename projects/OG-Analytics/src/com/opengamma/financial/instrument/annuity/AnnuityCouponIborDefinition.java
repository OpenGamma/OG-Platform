/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.schedule.ScheduleCalculator;

/**
 * A wrapper class for a AnnuityDefinition containing CouponIborDefinition.
 */
public class AnnuityCouponIborDefinition extends AnnuityDefinition<CouponIborDefinition> {

  /**
   * Constructor from a list of fixed coupons.
   * @param payments The fixed coupons.
   */
  public AnnuityCouponIborDefinition(final CouponIborDefinition[] payments) {
    super(payments);
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param settlementDate The settlement date.
   * @param tenor The tenor.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition from(ZonedDateTime settlementDate, Period tenor, double notional, IborIndex index, boolean isPayer) {

    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(index, "index");
    Validate.notNull(tenor, "tenor");
    Validate.isTrue(notional > 0, "notional <= 0");

    ZonedDateTime maturityDate = ScheduleCalculator.getAdjustedDate(settlementDate, index.getBusinessDayConvention(), index.getCalendar(), index.isEndOfMonth(), tenor);
    return from(settlementDate, maturityDate, notional, index, isPayer);
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition from(ZonedDateTime settlementDate, ZonedDateTime maturityDate, double notional, IborIndex index, boolean isPayer) {

    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(maturityDate, "maturity date");
    Validate.notNull(index, "index");
    Validate.isTrue(notional > 0, "notional <= 0");

    ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, index.getTenor());
    ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, index.getBusinessDayConvention(), index.getCalendar());

    double sign = isPayer ? -1.0 : 1.0;
    CouponIborDefinition[] coupons = new CouponIborDefinition[paymentDates.length];
    //First coupon uses settlement date
    CouponFixedDefinition coupon = new CouponFixedDefinition(index.getCurrency(), paymentDates[0], settlementDate, paymentDates[0], index.getDayCount().getDayCountFraction(settlementDate,
        paymentDates[0]), sign * notional, 0.0);
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, index.getBusinessDayConvention(), index.getCalendar(), -index.getSettlementDays());
    coupons[0] = CouponIborDefinition.from(coupon, fixingDate, index);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupon = new CouponFixedDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn], index.getDayCount().getDayCountFraction(
          paymentDates[loopcpn - 1], paymentDates[loopcpn]), sign * notional, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], index.getBusinessDayConvention(), index.getCalendar(), -index.getSettlementDays());
      coupons[loopcpn] = CouponIborDefinition.from(coupon, fixingDate, index);
    }

    return new AnnuityCouponIborDefinition(coupons);
  }

}
