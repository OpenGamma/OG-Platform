/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * A wrapper class for a AnnuityDefinition containing CMS coupon Definition.
 */
public class AnnuityCouponCMSDefinition extends AnnuityDefinition<CouponCMSDefinition> {

  /**
   * Constructor from a list of CMS coupons.
   * @param payments The CMS coupons.
   * @param calendar The calendar
   */
  public AnnuityCouponCMSDefinition(final CouponCMSDefinition[] payments, final Calendar calendar) {
    super(payments, calendar);
  }

  /**
   * CMS annuity (or CMS coupon leg) constructor from standard description. The coupon are fixing in advance and payment in arrears.
   * The CMS fixing is done at a standard lag before the coupon start.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The CMS index.
   * @param paymentPeriod The payment period of the coupons.
   * @param dayCount The day count of the coupons.
   * @param isPayer Payer (true) / receiver (false) flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The CMS coupon leg.
   */
  public static AnnuityCouponCMSDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IndexSwap index, final Period paymentPeriod,
      final DayCount dayCount, final boolean isPayer, final Calendar calendar) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    ArgumentChecker.notNull(paymentPeriod, "Payment period");
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, index.getIborIndex().getBusinessDayConvention(), calendar, false);
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponCMSDefinition[] coupons = new CouponCMSDefinition[paymentDates.length];
    coupons[0] = CouponCMSDefinition.from(paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0], calendar),
        sign * notional, index, calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CouponCMSDefinition.from(paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, index, calendar);
    }
    return new AnnuityCouponCMSDefinition(coupons, calendar);
  }

}
