/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * A wrapper class for a AnnuityDefinition containing Ibor cap/floor Definition.
 */
public class AnnuityCapFloorIborDefinition extends AnnuityDefinition<CapFloorIborDefinition> {

  /**
   * Constructor from a list of CMS coupons.
   * @param payments The CMS coupons.
   * @param calendar The holiday calendar.
   */
  public AnnuityCapFloorIborDefinition(final CapFloorIborDefinition[] payments, final Calendar calendar) {
    super(payments, calendar);
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
   * @param calendar The holiday calendar for the ibor leg.
   * @return The Ibor annuity.
   */
  public static AnnuityCapFloorIborDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index, final boolean isPayer,
      final double strike, final boolean isCap, final Calendar calendar) {
    ArgumentChecker.notNull(index, "index");
    return from(settlementDate, maturityDate, notional, index, index.getDayCount(), index.getTenor(), isPayer, strike, isCap, calendar);
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
   * @param calendar The holiday calendar for the ibor leg.
   * @return The Ibor annuity.
   */
  public static AnnuityCapFloorIborDefinition fromWithNoInitialCaplet(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index,
      final boolean isPayer, final double strike, final boolean isCap, final Calendar calendar) {
    final AnnuityCapFloorIborDefinition fullAnnuity = from(settlementDate, maturityDate, notional, index, isPayer, strike, isCap, calendar);
    final CapFloorIborDefinition[] cap = new CapFloorIborDefinition[fullAnnuity.getNumberOfPayments() - 1];
    for (int loopcap = 1; loopcap < fullAnnuity.getNumberOfPayments(); loopcap++) {
      cap[loopcap - 1] = fullAnnuity.getNthPayment(loopcap);
    }
    return new AnnuityCapFloorIborDefinition(cap, calendar);
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
   * @param calendar The holiday calendar for the ibor leg.
   * @return The Ibor annuity.
   */
  public static AnnuityCapFloorIborDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index, final DayCount dayCount,
      final Period paymentPeriod, final boolean isPayer, final double strike, final boolean isCap, final Calendar calendar) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, index.getBusinessDayConvention(), calendar, false);
    final double sign = isPayer ? -1.0 : 1.0;
    final CapFloorIborDefinition[] coupons = new CapFloorIborDefinition[paymentDates.length];
    //First coupon uses settlement date
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), calendar);
    coupons[0] = CapFloorIborDefinition.from(paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0], calendar),
        sign * notional, fixingDate, index, strike, isCap, calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -index.getSpotLag(), calendar);
      coupons[loopcpn] = CapFloorIborDefinition.from(paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, fixingDate, index, strike, isCap, calendar);
    }
    return new AnnuityCapFloorIborDefinition(coupons, calendar);
  }

}
