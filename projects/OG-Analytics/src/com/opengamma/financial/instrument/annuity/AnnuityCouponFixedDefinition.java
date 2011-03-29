/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;

/**
 * A wrapper class for a AnnuityDefinition containing CouponFixedDefinition.
 */
public class AnnuityCouponFixedDefinition extends AnnuityDefinition<CouponFixedDefinition> {

  /**
   * Constructor from a list of fixed coupons.
   * @param payments The fixed coupons.
   */
  public AnnuityCouponFixedDefinition(final CouponFixedDefinition[] payments) {
    super(payments);
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param currency The annuity currency.
   * @param settlementDate The settlement date.
   * @param tenor The annuity tenor.
   * @param paymentPeriod The period between payments.
   * @param calendar The calendar.
   * @param dayCount The day count.
   * @param businessDay The business day convention.
   * @param isEOM The end-of-month flag.
   * @param notional The notional.
   * @param fixedRate The fixed rate.
   * @param isPayer The payer flag.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition from(Currency currency, ZonedDateTime settlementDate, Period tenor, Period paymentPeriod, Calendar calendar, DayCount dayCount,
      BusinessDayConvention businessDay, boolean isEOM, double notional, double fixedRate, boolean isPayer) {
    double sign = isPayer ? -1.0 : 1.0;
    ZonedDateTime maturityDate = settlementDate.plus(tenor);
    ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, businessDay, calendar, isEOM, true);
    CouponFixedDefinition[] coupons = new CouponFixedDefinition[paymentDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedDefinition(currency, paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0]), sign * notional, fixedRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(currency, paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn], dayCount.getDayCountFraction(paymentDates[loopcpn - 1],
          paymentDates[loopcpn]), sign * notional, fixedRate);
    }
    return new AnnuityCouponFixedDefinition(coupons);
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param currency The annuity currency.
   * @param settlementDate The settlement date.
   * @param maturityDate The (unadjusted) maturity date of the annuity.
   * @param frequency The payment frequency.
   * @param calendar The calendar.
   * @param dayCount The day count.
   * @param businessDay The business day convention.
   * @param isEOM The end-of-month flag.
   * @param notional The notional.
   * @param fixedRate The fixed rate.
   * @param isPayer The payer flag.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition from(Currency currency, ZonedDateTime settlementDate, ZonedDateTime maturityDate, Frequency frequency, Calendar calendar, DayCount dayCount,
      BusinessDayConvention businessDay, boolean isEOM, double notional, double fixedRate, boolean isPayer) {
    double sign = isPayer ? -1.0 : 1.0;
    ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, frequency);
    ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar);
    CouponFixedDefinition[] coupons = new CouponFixedDefinition[paymentDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedDefinition(currency, paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0]), sign * notional, fixedRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(currency, paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn], dayCount.getDayCountFraction(paymentDates[loopcpn - 1],
          paymentDates[loopcpn]), sign * notional, fixedRate);
    }
    return new AnnuityCouponFixedDefinition(coupons);

  }

  @Override
  public AnnuityCouponFixed toDerivative(LocalDate date, String... yieldCurveNames) { // GenericAnnuity<CouponFixed>
    List<CouponFixed> resultList = new ArrayList<CouponFixed>();
    for (int loopcoupon = 0; loopcoupon < getPayments().length; loopcoupon++) {
      if (!date.isAfter(getNthPayment(loopcoupon).getPaymentDate().toLocalDate())) {
        resultList.add(getNthPayment(loopcoupon).toDerivative(date, yieldCurveNames));
      }
    }
    return new AnnuityCouponFixed(resultList.toArray(new CouponFixed[0]));
  }

}
