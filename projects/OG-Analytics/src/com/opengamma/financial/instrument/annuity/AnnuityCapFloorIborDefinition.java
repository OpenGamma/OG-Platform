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
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.financial.schedule.ScheduleCalculator;

/**
 * A wrapper class for a AnnuityDefinition containing Ibor cap/floor Definition.
 */
public class AnnuityCapFloorIborDefinition extends AnnuityDefinition<CapFloorIborDefinition> {

  /**
   * Constructor from a list of CMS coupons.
   * @param payments The CMS coupons.
   */
  public AnnuityCapFloorIborDefinition(final CapFloorIborDefinition[] payments) {
    super(payments);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The payments uses the index conventions and payment period.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param strike The common strike of all caplet.
   * @param isCap The cap (true) / floor (false) flag.
   * @return The Ibor annuity.
   */
  public static AnnuityCapFloorIborDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index, final boolean isPayer,
      final double strike, final boolean isCap) {
    Validate.notNull(index, "index");
    return from(settlementDate, maturityDate, notional, index, index.getDayCount(), index.getTenor(), isPayer, strike, isCap);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The payments uses the index conventions and payment period. 
   * The first caplet/floorlet (which is in practice immediately fixed) is not included.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param strike The common strike of all caplet.
   * @param isCap The cap (true) / floor (false) flag.
   * @return The Ibor annuity.
   */
  public static AnnuityCapFloorIborDefinition fromWithNoInitialCaplet(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index,
      final boolean isPayer, final double strike, final boolean isCap) {
    AnnuityCapFloorIborDefinition fullAnnuity = from(settlementDate, maturityDate, notional, index, isPayer, strike, isCap);
    CapFloorIborDefinition[] cap = new CapFloorIborDefinition[fullAnnuity.getNumberOfPayments() - 1];
    for (int loopcap = 1; loopcap < fullAnnuity.getNumberOfPayments(); loopcap++) {
      cap[loopcap - 1] = fullAnnuity.getNthPayment(loopcap);
    }
    return new AnnuityCapFloorIborDefinition(cap);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The payments index conventions and payment period are separated from the one of the underlying index.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param dayCount The day count for the coupon payments.
   * @param paymentPeriod The period between payments.
   * @param isPayer The payer flag.
   * @param strike The common strike of all caplet.
   * @param isCap The cap (true) / floor (false) flag.
   * @return The Ibor annuity.
   */
  public static AnnuityCapFloorIborDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index, final DayCount dayCount,
      final Period paymentPeriod, final boolean isPayer, final double strike, final boolean isCap) {
    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(maturityDate, "maturity date");
    Validate.notNull(index, "index");
    Validate.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, paymentPeriod);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, index.getBusinessDayConvention(), index.getCalendar());
    final double sign = isPayer ? -1.0 : 1.0;
    final CapFloorIborDefinition[] coupons = new CapFloorIborDefinition[paymentDates.length];
    //First coupon uses settlement date
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), index.getCalendar());
    coupons[0] = CapFloorIborDefinition.from(paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0]), sign * notional, fixingDate, index,
        strike, isCap);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -index.getSpotLag(), index.getCalendar());
      coupons[loopcpn] = CapFloorIborDefinition.from(paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn]), sign * notional, fixingDate, index, strike, isCap);
    }
    return new AnnuityCapFloorIborDefinition(coupons);
  }

}
