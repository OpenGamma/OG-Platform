/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.payment.CapFloorCMSSpreadDefinition;
import com.opengamma.financial.schedule.ScheduleCalculator;

/**
 * A wrapper class for a AnnuityDefinition containing CMS spread cap/floor Definition.
 */
public class AnnuityCapFloorCMSSpreadDefinition extends AnnuityDefinition<CapFloorCMSSpreadDefinition> {

  /**
   * Constructor from a list of CMS coupons.
   * @param payments The CMS coupons.
   */
  public AnnuityCapFloorCMSSpreadDefinition(final CapFloorCMSSpreadDefinition[] payments) {
    super(payments);
  }

  /**
   * CMS spread cap/floor (or leg of CMS spread caplet/floorlet) constructor from standard description. The cap/floor have fixing in advance and payment in arrears. 
   * The CMS fixing is done at a Ibor lag before the coupon start.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index1 The first CMS index.
   * @param index2 The second CMS index.
   * @param paymentPeriod The payment period of the coupons.
   * @param dayCount The day count of the coupons.
   * @param isPayer Payer (true) / receiver (false) fleg.
   * @param strike The common strike.
   * @param isCap The cap (true) / floor (false) flag.
   * @return The CMS coupon leg.
   */
  public static AnnuityCapFloorCMSSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IndexSwap index1, final IndexSwap index2,
      final Period paymentPeriod, final DayCount dayCount, final boolean isPayer, final double strike, final boolean isCap) {
    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(maturityDate, "maturity date");
    Validate.notNull(index1, "First index");
    Validate.notNull(index2, "Second index");
    Validate.isTrue(notional > 0, "notional <= 0");
    Validate.notNull(paymentPeriod, "Payment period");
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, paymentPeriod);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, index1.getIborIndex().getBusinessDayConvention(), index1.getIborIndex().getCalendar());
    final double sign = isPayer ? -1.0 : 1.0;
    final CapFloorCMSSpreadDefinition[] coupons = new CapFloorCMSSpreadDefinition[paymentDates.length];
    coupons[0] = CapFloorCMSSpreadDefinition.from(paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0]), sign * notional, index1, index2,
        strike, isCap);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CapFloorCMSSpreadDefinition.from(paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn]), sign * notional, index1, index2, strike, isCap);
    }
    return new AnnuityCapFloorCMSSpreadDefinition(coupons);
  }
}
