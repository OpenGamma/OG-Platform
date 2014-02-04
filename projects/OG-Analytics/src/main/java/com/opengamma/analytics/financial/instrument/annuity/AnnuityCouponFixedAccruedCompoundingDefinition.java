/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.payment.CouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.ActualActualICMA;
import com.opengamma.financial.convention.daycount.ActualActualICMANormal;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *  A wrapper class for a AnnuityDefinition containing CouponFixedAccruedCompoundingDefinition.
 */
public class AnnuityCouponFixedAccruedCompoundingDefinition extends AnnuityDefinition<CouponFixedAccruedCompoundingDefinition> {

  /**
   * Constructor from a list of fixed coupons.
   * @param payments The fixed coupons.
   * @param calendar The calendar.
   */
  public AnnuityCouponFixedAccruedCompoundingDefinition(final CouponFixedAccruedCompoundingDefinition[] payments, final Calendar calendar) {
    super(payments, calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The dates are constructed from the settlement date. If required the stub will be short.
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
  public static AnnuityCouponFixedAccruedCompoundingDefinition from(final Currency currency, final ZonedDateTime settlementDate, final Period tenor, final Period paymentPeriod,
      final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate, final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenor, "Tenor");
    final ZonedDateTime maturityDate = settlementDate.plus(tenor);
    return from(currency, settlementDate, maturityDate, paymentPeriod, calendar, dayCount, businessDay, isEOM, notional, fixedRate, isPayer);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The dates are constructed from the settlement date. If required the stub will be short.
   * @param currency The annuity currency.
   * @param settlementDate The settlement date.
   * @param maturityDate The (unadjusted) maturity date of the annuity.
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
  public static AnnuityCouponFixedAccruedCompoundingDefinition from(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod,
      final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate, final boolean isPayer) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDay, "business day convention");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, true, false, businessDay, calendar, isEOM);
    final CouponFixedAccruedCompoundingDefinition[] coupons = new CouponFixedAccruedCompoundingDefinition[paymentDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedAccruedCompoundingDefinition(currency, paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0], calendar),
        sign * notional, fixedRate, calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedAccruedCompoundingDefinition(currency, paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, fixedRate, calendar);
    }
    return new AnnuityCouponFixedAccruedCompoundingDefinition(coupons, calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The dates are constructed from the settlement date. If required the stub will be short.
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
  public static AnnuityCouponFixedAccruedCompoundingDefinition from(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Frequency frequency,
      final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate, final boolean isPayer) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(frequency, "frequency");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDay, "business day convention");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, frequency, true, false, businessDay, calendar, isEOM);
    final CouponFixedAccruedCompoundingDefinition[] coupons = new CouponFixedAccruedCompoundingDefinition[paymentDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedAccruedCompoundingDefinition(currency, paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0], calendar),
        sign * notional, fixedRate, calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedAccruedCompoundingDefinition(currency, paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, fixedRate, calendar);
    }
    return new AnnuityCouponFixedAccruedCompoundingDefinition(coupons, calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param currency The annuity currency.
   * @param settlementDate The settlement date.
   * @param paymentDatesUnadjusted The (unadjusted) payment dates of the annuity.
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
  public static AnnuityCouponFixedAccruedCompoundingDefinition from(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime[] paymentDatesUnadjusted,
      final Frequency frequency,
      final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate, final boolean isPayer) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(paymentDatesUnadjusted, "payment dates");
    ArgumentChecker.isTrue(paymentDatesUnadjusted.length > 0, "payment dates length");
    ArgumentChecker.notNull(frequency, "frequency");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDay, "business day convention");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar);
    final CouponFixedAccruedCompoundingDefinition[] coupons = new CouponFixedAccruedCompoundingDefinition[paymentDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedAccruedCompoundingDefinition(currency, paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0], calendar),
        sign * notional, fixedRate, calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedAccruedCompoundingDefinition(currency, paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, fixedRate, calendar);
    }
    return new AnnuityCouponFixedAccruedCompoundingDefinition(coupons, calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The accrual dates are unadjusted. Often used for bonds.
   * @param currency The annuity currency.
   * @param settlementDate The settlement date.
   * @param maturityDate The (unadjusted) maturity date of the annuity.
   * @param period The period between payments.
   * @param stubShort In case the the periods do not fit exactly between start and end date, is the remaining interval shorter (true) or longer (false) than the requested period.
   * @param fromEnd The dates in the schedule can be computed from the end date (true) or from the start date (false).
   * @param calendar The calendar.
   * @param dayCount The day count.
   * @param businessDay The business day convention.
   * @param isEOM The end-of-month flag.
   * @param notional The notional.
   * @param fixedRate The fixed rate.
   * @param isPayer The payer flag.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedAccruedCompoundingDefinition fromAccrualUnadjusted(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period period, final boolean stubShort, final boolean fromEnd, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM,
      final double notional, final double fixedRate, final boolean isPayer) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(period, "period");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDay, "business day convention");
    ArgumentChecker.isTrue(!(dayCount instanceof ActualActualICMA) | !(dayCount instanceof ActualActualICMANormal), "Coupon per year required for Actua lActual ICMA");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, period, stubShort, fromEnd);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar);
    final CouponFixedAccruedCompoundingDefinition[] coupons = new CouponFixedAccruedCompoundingDefinition[paymentDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedAccruedCompoundingDefinition(currency, paymentDates[0], settlementDate, paymentDatesUnadjusted[0],
        dayCount.getDayCountFraction(settlementDate, paymentDates[0], calendar), sign * notional, fixedRate, calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedAccruedCompoundingDefinition(currency, paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn],
          dayCount.getDayCountFraction(paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], calendar), sign * notional, fixedRate, calendar);
    }

    return new AnnuityCouponFixedAccruedCompoundingDefinition(coupons, calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The accrual dates are unadjusted. Often used for bonds.
   * @param currency The annuity currency.
   * @param settlementDate The settlement date.
   * @param maturityDate The (unadjusted) maturity date of the annuity.
   * @param period The period between payments.
   * @param nbPaymentPerYear The number of coupon per year. Used for some day count conventions.
   * @param stubShort In case the the periods do not fit exactly between start and end date, is the remaining interval shorter (true) or longer (false) than the requested period.
   * @param fromEnd The dates in the schedule can be computed from the end date (true) or from the start date (false).
   * @param calendar The calendar.
   * @param dayCount The day count.
   * @param businessDay The business day convention.
   * @param isEOM The end-of-month flag.
   * @param notional The notional.
   * @param fixedRate The fixed rate.
   * @param isPayer The payer flag.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedAccruedCompoundingDefinition fromAccrualUnadjusted(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period period, final int nbPaymentPerYear, final boolean stubShort, final boolean fromEnd, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay,
      final boolean isEOM, final double notional, final double fixedRate, final boolean isPayer) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(period, "period");
    ArgumentChecker.isTrue(nbPaymentPerYear > 0, "need greater than zero payments per year");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDay, "business day convention");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, period, stubShort, fromEnd);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar);
    final CouponFixedAccruedCompoundingDefinition[] coupons = new CouponFixedAccruedCompoundingDefinition[paymentDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedAccruedCompoundingDefinition(currency, paymentDates[0], settlementDate, paymentDatesUnadjusted[0], dayCount.getAccruedInterest(settlementDate, paymentDates[0],
        paymentDates[0], 1.0, nbPaymentPerYear), sign * notional, fixedRate, calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedAccruedCompoundingDefinition(currency, paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn],
          dayCount.getAccruedInterest(paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], paymentDatesUnadjusted[loopcpn], 1.0, nbPaymentPerYear), sign * notional, fixedRate,
          calendar);
    }
    return new AnnuityCouponFixedAccruedCompoundingDefinition(coupons, calendar);
  }

  /**
   * Remove the payments paying on or before the given date.
   * @param trimDate The date.
   * @return The trimmed annuity.
   */
  @Override
  public AnnuityCouponFixedAccruedCompoundingDefinition trimBefore(final ZonedDateTime trimDate) {
    final List<CouponFixedAccruedCompoundingDefinition> list = new ArrayList<>();
    for (final CouponFixedAccruedCompoundingDefinition payment : getPayments()) {
      if (payment.getPaymentDate().isAfter(trimDate)) {
        list.add(payment);
      }
    }
    return new AnnuityCouponFixedAccruedCompoundingDefinition(list.toArray(new CouponFixedAccruedCompoundingDefinition[list.size()]), getCalendar());
  }

  /**
   * Creates a new annuity containing the coupons with start accrual date strictly before the given date.
   * @param trimDate The date.
   * @return The trimmed annuity.
   */
  public AnnuityCouponFixedAccruedCompoundingDefinition trimStart(final ZonedDateTime trimDate) {
    final List<CouponFixedAccruedCompoundingDefinition> list = new ArrayList<>();
    for (final CouponFixedAccruedCompoundingDefinition payment : getPayments()) {
      if (!payment.getAccrualStartDate().isBefore(trimDate)) {
        list.add(payment);
      }
    }
    return new AnnuityCouponFixedAccruedCompoundingDefinition(list.toArray(new CouponFixedAccruedCompoundingDefinition[list.size()]), getCalendar());
  }

}
