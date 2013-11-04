/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A utility to create different type of annuities.
 */
public class AnnuityDefinitionBuilder {

  /**
   *  TODO:
   *  Add versions with payment lag
   *  Add CouponONSimpleCoumpounded (standard, simplified, spread, simplified spread)
   *  Add CouponONCompoundedCompounded (i.e. BRL swaps)
   *  Add CouponONArithmeticAverage (i.e. Fed Fund swaps)
   */

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
   * @param stub The stub type.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition couponFixed(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional,
      final double fixedRate, final boolean isPayer, final StubType stub) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDay, "business day convention");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stub,
        businessDay, calendar, isEOM);
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[paymentDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedDefinition(currency, paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0], calendar),
        sign * notional, fixedRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(currency, paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, fixedRate);
    }
    return new AnnuityCouponFixedDefinition(coupons, calendar);
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
   * @param stub The stub type.
   * @param paymentLag The payment lag (in days).
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition couponFixed(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional,
      final double fixedRate, final boolean isPayer, final StubType stub, final int paymentLag) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDay, "business day convention");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] adjustedEndAccrualDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stub,
        businessDay, calendar, isEOM);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDate(adjustedEndAccrualDates, paymentLag, calendar);
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[adjustedEndAccrualDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedDefinition(currency, paymentDates[0], settlementDate, adjustedEndAccrualDates[0],
        dayCount.getDayCountFraction(settlementDate, adjustedEndAccrualDates[0], calendar), sign * notional, fixedRate);
    for (int loopcpn = 1; loopcpn < adjustedEndAccrualDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(currency, paymentDates[loopcpn], adjustedEndAccrualDates[loopcpn - 1], adjustedEndAccrualDates[loopcpn],
          dayCount.getDayCountFraction(adjustedEndAccrualDates[loopcpn - 1], adjustedEndAccrualDates[loopcpn], calendar), sign * notional, fixedRate);
    }
    return new AnnuityCouponFixedDefinition(coupons, calendar);
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
   * @param stub The stub type.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition couponFixed(final Currency currency, final ZonedDateTime settlementDate, final Period tenor, final Period paymentPeriod,
      final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate, final boolean isPayer,
      final StubType stub) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenor, "Tenor");
    final ZonedDateTime maturityDate = settlementDate.plus(tenor);
    return couponFixed(currency, settlementDate, maturityDate, paymentPeriod, calendar, dayCount, businessDay, isEOM, notional, fixedRate, isPayer, stub);
  }

  /**
   * Create an fixed coupon leg based on a roll date convention. The coupons are in line with the roll dates of the adjuster (see payment period for more details).
   * @param currency The leg currency
   * @param startDate The start/reference date of the computation.
   * @param startNumberRollDate The number of roll dates to the effective date of the swap. 
   * @param endNumberRollDate The number of roll dates to the maturity of the swap.
   * @param adjuster The date adjuster, e.g. IMM quarterly dates.
   * @param paymentPeriod The payment period. The payment period is used in the following way: the ratio "n" of number of month in the payment period 
   * and of the "MonthsToAdjust" of the "adjuster" is computed. The ratio is computed with the long division. The payment dates are the adjuster n-th dates.
   * For example if paymentPeriod is P6M and the period in the adjuster is P3M, n is 2.
   * @param notional The swap notional.
   * @param rate The fixed rate.
   * @param isPayer The payer flag.
   * @param dayCount The day count for the coupon accrual factors.
   * @param calendar The calendar for the date adjustments.
   * @param stub The stub type.
   * @return The fixed coupons annuity.
   */
  public static AnnuityDefinition<CouponFixedDefinition> couponFixedRollDate(final Currency currency, final ZonedDateTime startDate, final int startNumberRollDate,
      final int endNumberRollDate, final RollDateAdjuster adjuster, final Period paymentPeriod, final double notional, final double rate, final boolean isPayer,
      final DayCount dayCount, final Calendar calendar, final StubType stub) {
    long rollMonths = adjuster.getMonthsToAdjust();
    long paymentMonths = paymentPeriod.toTotalMonths();
    int rollJump = (int) (paymentMonths / rollMonths); // The roll jumps is rounded
    int nbRoll = endNumberRollDate - startNumberRollDate;
    // Start -- Construct the list of dates
    final List<ZonedDateTime> legDates = new ArrayList<>();
    ZonedDateTime currentDate = RollDateAdjusterUtils.nthDate(startDate, adjuster, startNumberRollDate);
    legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
    int nbPeriods = nbRoll / rollJump; // Number of full periods
    int stubPeriod = nbRoll % rollJump;
    if (stubPeriod == 0) { // No stub, the number of periods from the adjuster is in line with the required number.
      for (int loopperiod = 1; loopperiod <= nbPeriods; loopperiod++) {
        currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
        legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
      }
    } else {
      switch (stub) {
        case SHORT_START:
          currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod);
          legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          for (int loopperiod = 1; loopperiod <= nbPeriods; loopperiod++) {
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          }
          break;
        case LONG_START:
          if (nbPeriods == 0) { // Not enough full periods to have a "long" period, only one short period
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          } else {
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod + rollJump);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
            for (int loopperiod = 1; loopperiod <= nbPeriods - 1; loopperiod++) {
              currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
              legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
            }
          }
          break;
        case SHORT_END:
          for (int loopperiod = 1; loopperiod <= nbPeriods; loopperiod++) {
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          }
          currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod);
          legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          break;
        case LONG_END:
          if (nbPeriods == 0) { // Not enough full periods to have a "long" period, only one short period
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          } else {
            for (int loopperiod = 1; loopperiod <= nbPeriods - 1; loopperiod++) {
              currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
              legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
            }
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod + rollJump);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          }
          break;
      }
    }
    // End -- Construct the list of dates
    final double sign = isPayer ? -1.0 : 1.0;
    final int nbCpn = legDates.size() - 1;
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[nbCpn];
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) {
      double accrualFactor = dayCount.getDayCountFraction(legDates.get(loopcpn), legDates.get(loopcpn + 1));
      coupons[loopcpn] = new CouponFixedDefinition(currency, legDates.get(loopcpn + 1), legDates.get(loopcpn), legDates.get(loopcpn + 1), accrualFactor, notional * sign, rate);
    }
    return new AnnuityDefinition<>(coupons, calendar);
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
   * @param stub The stub type.
   * @return The fixed annuity.
   */
  public static AnnuityDefinition<PaymentDefinition> couponFixedWithNotional(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate,
      final boolean isPayer, final StubType stub) {
    final AnnuityDefinition<CouponFixedDefinition> annuityNoNotional = couponFixed(currency, settlementDate, maturityDate, paymentPeriod, calendar, dayCount, businessDay, isEOM, notional,
        fixedRate, isPayer, stub);
    final double sign = (isPayer) ? -1.0 : 1.0;
    final int nbPay = annuityNoNotional.getNumberOfPayments();
    final PaymentDefinition[] legWithNotional = new PaymentDefinition[nbPay + 2];
    legWithNotional[0] = new PaymentFixedDefinition(annuityNoNotional.getCurrency(), settlementDate, -notional * sign);
    for (int loopp = 0; loopp < nbPay; loopp++) {
      legWithNotional[loopp + 1] = annuityNoNotional.getNthPayment(loopp);
    }
    legWithNotional[nbPay + 1] = new PaymentFixedDefinition(annuityNoNotional.getCurrency(), annuityNoNotional.getNthPayment(nbPay - 1).getPaymentDate(), notional * sign);
    return new AnnuityDefinition<>(legWithNotional, calendar);
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
   * @param stub The stub type.
   * @return The fixed annuity.
   */
  public static AnnuityDefinition<PaymentDefinition> couponFixedWithNotional(final Currency currency, final ZonedDateTime settlementDate, final Period tenor,
      final Period paymentPeriod, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate,
      final boolean isPayer, final StubType stub) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    final ZonedDateTime maturityDate = settlementDate.plus(tenor);
    return couponFixedWithNotional(currency, settlementDate, maturityDate, paymentPeriod, calendar, dayCount, businessDay, isEOM, notional, fixedRate, isPayer, stub);
  }

  /**
   * Annuity coupon ibor with spread builder.
   * When a stub is used, the fixing index and fixing period dates are the one of the underlying index. The index is not "shorten" or "lengthen".
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param paymentPeriod The payment period.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param dayCount The coupons day count.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition couponIbor(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod,
      final double notional, final IborIndex index, final boolean isPayer, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final boolean endOfMonth,
      final Calendar calendar, final StubType stub) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(dayCount, "Day count convention");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stub,
        businessDayConvention, calendar, endOfMonth);
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponIborDefinition[] coupons = new CouponIborDefinition[paymentDates.length];
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), calendar);
    coupons[0] = new CouponIborDefinition(index.getCurrency(), paymentDates[0], settlementDate, paymentDates[0],
        dayCount.getDayCountFraction(settlementDate, paymentDates[0], calendar), sign * notional, fixingDate, index, calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -index.getSpotLag(), calendar);
      coupons[loopcpn] = new CouponIborDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, fixingDate, index, calendar);
    }
    return new AnnuityCouponIborDefinition(coupons, index, calendar);
  }

  /**
   * Annuity coupon ibor with spread builder.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param paymentPeriod The payment period.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param dayCount The coupons day count.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @param paymentLag The payment lag (in days).
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition couponIbor(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod,
      final double notional, final IborIndex index, final boolean isPayer, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final boolean endOfMonth,
      final Calendar calendar, final StubType stub, final int paymentLag) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(dayCount, "Day count convention");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime[] adjustedEndAccrualDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stub,
        businessDayConvention, calendar, endOfMonth);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDate(adjustedEndAccrualDates, paymentLag, calendar);
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponIborDefinition[] coupons = new CouponIborDefinition[adjustedEndAccrualDates.length];
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), calendar);
    coupons[0] = new CouponIborDefinition(index.getCurrency(), paymentDates[0], settlementDate, adjustedEndAccrualDates[0],
        dayCount.getDayCountFraction(settlementDate, adjustedEndAccrualDates[0], calendar), sign * notional, fixingDate, index, calendar);
    for (int loopcpn = 1; loopcpn < adjustedEndAccrualDates.length; loopcpn++) {
      fixingDate = ScheduleCalculator.getAdjustedDate(adjustedEndAccrualDates[loopcpn - 1], -index.getSpotLag(), calendar);
      coupons[loopcpn] = new CouponIborDefinition(index.getCurrency(), paymentDates[loopcpn], adjustedEndAccrualDates[loopcpn - 1], adjustedEndAccrualDates[loopcpn],
          dayCount.getDayCountFraction(adjustedEndAccrualDates[loopcpn - 1], adjustedEndAccrualDates[loopcpn], calendar), sign * notional, fixingDate, index, calendar);
    }
    return new AnnuityCouponIborDefinition(coupons, index, calendar);
  }

  /**
   * Create an Ibor leg based on a roll date convention. The coupons are in line with the roll dates of the adjuster.
   * @param startDate The start/reference date of the computation.
   * @param startNumberRollDate The number of roll dates to the effective date of the swap. 
   * @param endNumberRollDate The number of roll dates to the maturity of the swap.
   * @param adjuster The date adjuster, e.g. IMM quarterly dates.
   * @param index The Ibor index. There is no check that the index is coherent with the adjuster. It is possible to use a monthly adjuster with a three month index.
   * The start and end dates of the fixing period are given by the index, they may be different from the end and start accrual dates of the coupon that are given by the adjuster.
   * @param notional The swap notional.
   * @param isPayer The payer flag.
   * @param dayCount The coupons day count. Can be different from the one of the index.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition couponIborRollDate(final ZonedDateTime startDate, final int startNumberRollDate, final int endNumberRollDate,
      final RollDateAdjuster adjuster, final IborIndex index, final double notional, final boolean isPayer, final DayCount dayCount, final Calendar calendar) {
    final List<ZonedDateTime> legDates = new ArrayList<>();
    ZonedDateTime currentDate = RollDateAdjusterUtils.nthDate(startDate, adjuster, startNumberRollDate);
    legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
    for (int looproll = 1; looproll <= endNumberRollDate - startNumberRollDate; looproll++) {
      currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, 1);
      legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
    }
    final double sign = isPayer ? -1.0 : 1.0;
    final int nbCpn = legDates.size() - 1;
    final CouponIborDefinition[] coupons = new CouponIborDefinition[nbCpn];
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) {
      ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(legDates.get(loopcpn), -index.getSpotLag(), calendar);
      coupons[loopcpn] = new CouponIborDefinition(index.getCurrency(), legDates.get(loopcpn), legDates.get(loopcpn), legDates.get(loopcpn + 1),
          dayCount.getDayCountFraction(legDates.get(loopcpn), legDates.get(loopcpn + 1), calendar), sign * notional, fixingDate, index, calendar);
    }
    return new AnnuityCouponIborDefinition(coupons, index, calendar);
  }

  /**
   * Create an Ibor leg based on a roll date convention. The coupons are in line with the roll dates of the adjuster (see index for more details).
   * @param startDate The start/reference date of the computation.
   * @param startNumberRollDate The number of roll dates to the effective date of the swap. 
   * @param endNumberRollDate The number of roll dates to the maturity of the swap.
   * @param adjuster The date adjuster, e.g. IMM quarterly dates.
   * @param index The Ibor index. There is no check that the index is coherent with the adjuster. 
   * The index period is used in the following way: the ratio "n" of number of month in the index period and of the "MonthsToAdjust" of the "adjuster" is computed. 
   * The ratio is computed with the long division (i.e. with rounding). The payment and accrual dates are the adjuster n-th dates.
   * For example if the index tenor is P6M and the period in the adjuster is P3M, n is 2.
   * The start and end dates of the fixing period are given by the end and start accrual dates of the coupon that are given by the adjuster. 
   * The may differ from the theoretical date of the index.
   * @param notional The swap notional.
   * @param isPayer The payer flag.
   * @param dayCount The coupons day count. Can be different from the one of the index.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborDefinition> couponIborRollDateIndexAdjusted(final ZonedDateTime startDate, final int startNumberRollDate, final int endNumberRollDate,
      final RollDateAdjuster adjuster, final IborIndex index, final double notional, final boolean isPayer, final DayCount dayCount, final Calendar calendar, final StubType stub) {
    long rollMonths = adjuster.getMonthsToAdjust();
    long paymentMonths = index.getTenor().toTotalMonths();
    int rollJump = (int) (paymentMonths / rollMonths); // The roll jumps is rounded
    int nbRoll = endNumberRollDate - startNumberRollDate;
    // Start -- Construct the list of dates
    final List<ZonedDateTime> legDates = new ArrayList<>();
    ZonedDateTime currentDate = RollDateAdjusterUtils.nthDate(startDate, adjuster, startNumberRollDate);
    legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
    int nbPeriods = nbRoll / rollJump; // Number of full periods
    int stubPeriod = nbRoll % rollJump;
    if (stubPeriod == 0) { // No stub, the number of periods from the adjuster is in line with the required number.
      for (int loopperiod = 1; loopperiod <= nbPeriods; loopperiod++) {
        currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
        legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
      }
    } else {
      switch (stub) {
        case SHORT_START:
          currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod);
          legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          for (int loopperiod = 1; loopperiod <= nbPeriods; loopperiod++) {
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          }
          break;
        case LONG_START:
          if (nbPeriods == 0) { // Not enough full periods to have a "long" period, only one short period
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          } else {
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod + rollJump);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
            for (int loopperiod = 1; loopperiod <= nbPeriods - 1; loopperiod++) {
              currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
              legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
            }
          }
          break;
        case SHORT_END:
          for (int loopperiod = 1; loopperiod <= nbPeriods; loopperiod++) {
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          }
          currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod);
          legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          break;
        case LONG_END:
          if (nbPeriods == 0) { // Not enough full periods to have a "long" period, only one short period
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          } else {
            for (int loopperiod = 1; loopperiod <= nbPeriods - 1; loopperiod++) {
              currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
              legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
            }
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod + rollJump);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          }
          break;
      }
    }
    // End -- Construct the list of dates
    final double sign = isPayer ? -1.0 : 1.0;
    final int nbCpn = legDates.size() - 1;
    final CouponIborDefinition[] coupons = new CouponIborDefinition[nbCpn];
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) {
      ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(legDates.get(loopcpn), -index.getSpotLag(), calendar);
      double af = dayCount.getDayCountFraction(legDates.get(loopcpn), legDates.get(loopcpn + 1), calendar);
      coupons[loopcpn] = new CouponIborDefinition(index.getCurrency(), legDates.get(loopcpn + 1), legDates.get(loopcpn), legDates.get(loopcpn + 1),
          af, sign * notional, fixingDate, legDates.get(loopcpn), legDates.get(loopcpn + 1), af, index, calendar);
    }
    return new AnnuityDefinition<>(coupons, calendar);
  }

  /**
   * Create an Ibor leg based on a roll date convention. The coupons are in line with the roll dates of the adjuster (see payment period for more details).
   * @param startDate The start/reference date of the computation.
   * @param startNumberRollDate The number of roll dates to the effective date of the swap. 
   * @param endNumberRollDate The number of roll dates to the maturity of the swap.
   * @param adjuster The date adjuster, e.g. IMM quarterly dates.
   * @param index The Ibor index. There is no check that the index is coherent with the adjuster. 
   * The index period is used in the following way: the ratio "n" of number of month in the index period and of the "MonthsToAdjust" of the "adjuster" is computed. 
   * The ratio is computed with the long division (i.e. with rounding). The payemnt and accrual dates are the adjuster n-th dates.
   * For example if the index tenor is P6M and the period in the adjuster is P3M, n is 2.
   * The start and end dates of the fixing period are given by the index, they may be different from the end and start accrual dates of the coupon that are given by the adjuster.
   * @param spread The spread.
   * @param notional The swap notional.
   * @param isPayer The payer flag.
   * @param dayCount The coupons day count. Can be different from the one of the index.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborSpreadDefinition> couponIborSpreadRollDateIndexAdjusted(final ZonedDateTime startDate, final int startNumberRollDate, final int endNumberRollDate,
      final RollDateAdjuster adjuster, final IborIndex index, final double spread, final double notional, final boolean isPayer, final DayCount dayCount, final Calendar calendar,
      final StubType stub) {
    long rollMonths = adjuster.getMonthsToAdjust();
    long paymentMonths = index.getTenor().toTotalMonths();
    int rollJump = (int) (paymentMonths / rollMonths); // The roll jumps is rounded
    int nbRoll = endNumberRollDate - startNumberRollDate;
    // Start -- Construct the list of dates
    final List<ZonedDateTime> legDates = new ArrayList<>();
    ZonedDateTime currentDate = RollDateAdjusterUtils.nthDate(startDate, adjuster, startNumberRollDate);
    legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
    int nbPeriods = nbRoll / rollJump; // Number of full periods
    int stubPeriod = nbRoll % rollJump;
    if (stubPeriod == 0) { // No stub, the number of periods from the adjuster is in line with the required number.
      for (int loopperiod = 1; loopperiod <= nbPeriods; loopperiod++) {
        currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
        legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
      }
    } else {
      switch (stub) {
        case SHORT_START:
          currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod);
          legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          for (int loopperiod = 1; loopperiod <= nbPeriods; loopperiod++) {
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          }
          break;
        case LONG_START:
          if (nbPeriods == 0) { // Not enough full periods to have a "long" period, only one short period
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          } else {
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod + rollJump);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
            for (int loopperiod = 1; loopperiod <= nbPeriods - 1; loopperiod++) {
              currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
              legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
            }
          }
          break;
        case SHORT_END:
          for (int loopperiod = 1; loopperiod <= nbPeriods; loopperiod++) {
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          }
          currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod);
          legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          break;
        case LONG_END:
          if (nbPeriods == 0) { // Not enough full periods to have a "long" period, only one short period
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          } else {
            for (int loopperiod = 1; loopperiod <= nbPeriods - 1; loopperiod++) {
              currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, rollJump);
              legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
            }
            currentDate = RollDateAdjusterUtils.nthDate(currentDate.plusDays(1), adjuster, stubPeriod + rollJump);
            legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
          }
          break;
      }
    }
    // End -- Construct the list of dates
    final double sign = isPayer ? -1.0 : 1.0;
    final int nbCpn = legDates.size() - 1;
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[nbCpn];
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) {
      ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(legDates.get(loopcpn), -index.getSpotLag(), calendar);
      coupons[loopcpn] = new CouponIborSpreadDefinition(index.getCurrency(), legDates.get(loopcpn), legDates.get(loopcpn), legDates.get(loopcpn + 1),
          dayCount.getDayCountFraction(legDates.get(loopcpn), legDates.get(loopcpn + 1), calendar), sign * notional, fixingDate, index, spread, calendar);
    }
    return new AnnuityDefinition<>(coupons, calendar);
  }

  /**
   * Annuity coupon ibor with spread builder.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param paymentPeriod The payment period.
   * @param notional The notional.
   * @param spread The spread rate.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param dayCount The coupons day count.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborSpreadDefinition> couponIborSpread(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod,
      final double notional, final double spread, final IborIndex index, final boolean isPayer, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final boolean endOfMonth,
      final Calendar calendar, final StubType stub) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(dayCount, "Day count convention");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stub,
        businessDayConvention, calendar, endOfMonth);
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[paymentDates.length];
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), calendar);
    coupons[0] = new CouponIborSpreadDefinition(index.getCurrency(), paymentDates[0], settlementDate, paymentDates[0],
        dayCount.getDayCountFraction(settlementDate, paymentDates[0], calendar), sign * notional, fixingDate, index, spread, calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -index.getSpotLag(), calendar);
      coupons[loopcpn] = new CouponIborSpreadDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, fixingDate, index, spread, calendar);
    }
    return new AnnuityDefinition<>(coupons, calendar);
  }

  /**
   * Annuity coupon ibor with spread builder.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param paymentPeriod The payment period.
   * @param notional The notional.
   * @param spread The spread rate.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param dayCount The coupons day count.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @param paymentLag The payment lag (in days).
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborSpreadDefinition> couponIborSpread(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod,
      final double notional, final double spread, final IborIndex index, final boolean isPayer, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final boolean endOfMonth,
      final Calendar calendar, final StubType stub, final int paymentLag) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(dayCount, "Day count convention");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime[] adjustedEndAccrualDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stub,
        businessDayConvention, calendar, endOfMonth);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDate(adjustedEndAccrualDates, paymentLag, calendar);
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[adjustedEndAccrualDates.length];
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), calendar);
    coupons[0] = new CouponIborSpreadDefinition(index.getCurrency(), paymentDates[0], settlementDate, adjustedEndAccrualDates[0],
        dayCount.getDayCountFraction(settlementDate, adjustedEndAccrualDates[0], calendar), sign * notional, fixingDate, index, spread, calendar);
    for (int loopcpn = 1; loopcpn < adjustedEndAccrualDates.length; loopcpn++) {
      fixingDate = ScheduleCalculator.getAdjustedDate(adjustedEndAccrualDates[loopcpn - 1], -index.getSpotLag(), calendar);
      coupons[loopcpn] = new CouponIborSpreadDefinition(index.getCurrency(), paymentDates[loopcpn], adjustedEndAccrualDates[loopcpn - 1], adjustedEndAccrualDates[loopcpn],
          dayCount.getDayCountFraction(adjustedEndAccrualDates[loopcpn - 1], adjustedEndAccrualDates[loopcpn], calendar), sign * notional, fixingDate, index, spread, calendar);
    }
    return new AnnuityDefinition<>(coupons, calendar);
  }

  /**
   * Annuity coupon ibor with spread builder. The annuity payment period, business day convention, end-of-month and day count are the one of the Ibor.
   * The stub is short-start.
   * @param settlementDate The settlement date.
   * @param maturityDate The maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param spread The common spread.
   * @param isPayer The payer flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborSpreadDefinition> couponIborSpread(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index,
      final double spread, final boolean isPayer, final Calendar calendar) {
    ArgumentChecker.notNull(index, "index");
    return couponIborSpread(settlementDate, maturityDate, index.getTenor(), notional, spread, index, isPayer, index.getDayCount(), index.getBusinessDayConvention(),
        index.isEndOfMonth(), calendar, StubType.SHORT_START);
  }

  /**
   * Builder of an annuity of Ibor coupons with spread from the conventions and common characteristics.
   * @param settlementDate The settlement date.
   * @param tenor The tenor.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param spread The common spread.
   * @param isPayer The payer flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborSpreadDefinition> couponIborSpread(final ZonedDateTime settlementDate, final Period tenor, final double notional, final IborIndex index,
      final double spread, final boolean isPayer, final Calendar calendar) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    final ZonedDateTime maturityDate = settlementDate.plus(tenor);
    return couponIborSpread(settlementDate, maturityDate, notional, index, spread, isPayer, calendar);
  }

  /**
   * Builder of an annuity of Ibor coupons with spread and payment of notional at the start and at the end.
   * The initial payment of notional is the opposite of the final one.
   * @param settlementDate The settlement date.
   * @param maturityDate The maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param spread The common spread.
   * @param isPayer The payer flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The annuity.
   */
  public static AnnuityDefinition<PaymentDefinition> couponIborSpreadWithNotional(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional,
      final IborIndex index, final double spread, final boolean isPayer, final Calendar calendar) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(index, "index");
    final AnnuityDefinition<CouponIborSpreadDefinition> legNoNotional = couponIborSpread(settlementDate, maturityDate, notional, index, spread, isPayer, calendar);
    final double sign = (isPayer) ? -1.0 : 1.0;
    final int nbPay = legNoNotional.getNumberOfPayments();
    final PaymentDefinition[] legWithNotional = new PaymentDefinition[nbPay + 2];
    legWithNotional[0] = new PaymentFixedDefinition(legNoNotional.getCurrency(), settlementDate, -notional * sign);
    for (int loopp = 0; loopp < nbPay; loopp++) {
      legWithNotional[loopp + 1] = legNoNotional.getNthPayment(loopp);
    }
    legWithNotional[nbPay + 1] = new PaymentFixedDefinition(legNoNotional.getCurrency(), legNoNotional.getNthPayment(nbPay - 1).getPaymentDate(), notional * sign);
    return new AnnuityDefinition<>(legWithNotional, calendar);
  }

  /**
   * Builder of an annuity of Ibor coupons with spread and payment of notional at the start and at the end.
   * The initial payment of notional is the opposite of the final one.
   * @param settlementDate The settlement date.
   * @param tenor The tenor.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param spread The common spread.
   * @param isPayer The payer flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The annuity.
   */
  public static AnnuityDefinition<PaymentDefinition> couponIborSpreadWithNotional(final ZonedDateTime settlementDate, final Period tenor, final double notional, final IborIndex index,
      final double spread, final boolean isPayer, final Calendar calendar) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(tenor, "tenor");
    final ZonedDateTime maturityDate = settlementDate.plus(tenor);
    return couponIborSpreadWithNotional(settlementDate, maturityDate, notional, index, spread, isPayer, calendar);
  }

  /**
   * Annuity of compounded coupon ibor without spread.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param paymentPeriod The payment period.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param stubCompound The stub type used in each coupon for the compounding. Not null.
   * @param isPayer The payer flag.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stubLeg The stub type used in the leg for the different coupons. Not null
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborCompoundingDefinition> couponIborCompounding(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final double notional, final IborIndex index, final StubType stubCompound, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final StubType stubLeg) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime[] unadjustedDateSchedule = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponIborCompoundingDefinition[] coupons = new CouponIborCompoundingDefinition[unadjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingDefinition.from(signedNotional, settlementDate, unadjustedDateSchedule[0], index, stubCompound, businessDayConvention, endOfMonth, calendar);
    for (int loopcpn = 1; loopcpn < unadjustedDateSchedule.length; loopcpn++) {
      coupons[loopcpn] = CouponIborCompoundingDefinition.from(signedNotional, unadjustedDateSchedule[loopcpn - 1], unadjustedDateSchedule[loopcpn], index, stubCompound,
          businessDayConvention, endOfMonth, calendar);
    }
    return new AnnuityDefinition<>(coupons, calendar);
  }

  /**
   * Annuity of compounded coupon ibor with spread and compounding type "Compounding".
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param paymentPeriod The payment period.
   * @param notional The notional.
   * @param spread The spread rate.
   * @param index The Ibor index.
   * @param stubCompound The stub type used in each coupon for the compounding. Not null.
   * @param isPayer The payer flag.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stubLeg The stub type used in the leg for the different coupons. Not null
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborCompoundingSpreadDefinition> couponIborCompoundingSpread(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final double notional, final double spread, final IborIndex index, final StubType stubCompound, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final StubType stubLeg) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime[] unadjustedDateSchedule = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponIborCompoundingSpreadDefinition[] coupons = new CouponIborCompoundingSpreadDefinition[unadjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingSpreadDefinition.from(signedNotional, settlementDate, unadjustedDateSchedule[0], index, spread, stubCompound, businessDayConvention, endOfMonth, calendar);
    for (int loopcpn = 1; loopcpn < unadjustedDateSchedule.length; loopcpn++) {
      coupons[loopcpn] = CouponIborCompoundingSpreadDefinition.from(signedNotional, unadjustedDateSchedule[loopcpn - 1], unadjustedDateSchedule[loopcpn], index, spread, stubCompound,
          businessDayConvention, endOfMonth, calendar);
    }
    return new AnnuityDefinition<>(coupons, calendar);
  }

  /**
   * Annuity of compounded coupon ibor with spread and compounding type "Flat Compounding".
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param paymentPeriod The payment period.
   * @param notional The notional.
   * @param spread The spread rate.
   * @param index The Ibor index.
   * @param stubCompound The stub type used in each coupon for the compounding. Not null.
   * @param isPayer The payer flag.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stubLeg The stub type used in the leg for the different coupons. Not null
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborCompoundingFlatSpreadDefinition> couponIborCompoundingFlatSpread(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final double notional, final double spread, final IborIndex index, final StubType stubCompound, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final StubType stubLeg) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.isTrue(notional >= 0, "notional < 0");
    final ZonedDateTime[] unadjustedDateSchedule = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponIborCompoundingFlatSpreadDefinition[] coupons = new CouponIborCompoundingFlatSpreadDefinition[unadjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingFlatSpreadDefinition.from(signedNotional, settlementDate, unadjustedDateSchedule[0], index, spread, stubCompound, businessDayConvention, endOfMonth, calendar);
    for (int loopcpn = 1; loopcpn < unadjustedDateSchedule.length; loopcpn++) {
      coupons[loopcpn] = CouponIborCompoundingFlatSpreadDefinition.from(signedNotional, unadjustedDateSchedule[loopcpn - 1], unadjustedDateSchedule[loopcpn], index, spread, stubCompound,
          businessDayConvention, endOfMonth, calendar);
    }
    return new AnnuityDefinition<>(coupons, calendar);
  }

  /**
   * Annuity of coupon with compunded of ON simple rates. Simplified version (i.e. only the start date and end date of coupon periods are computed) with spread.
   * @param settlementDate The annuity settlement date. The date is not adjusted for the calendar.
   * @param maturityDate The annuity maturity date. The date is adjusted according to the calendar and conventions.
   * @param paymentPeriod The annuity payment period.
   * @param notional The notional.
   * @param spread The spread rate.
   * @param index The overnight index associated to all the coupons of the leg.
   * @param isPayer The payer flag.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param calendar The calendar associated to the payments.
   * @param stubLeg The type of stub for the leg.
   * @return The annuity.
   */
  public static AnnuityDefinition<CouponONSpreadSimplifiedDefinition> couponONSimpleCompoundedSpreadSimplified(final ZonedDateTime settlementDate,
      final ZonedDateTime maturityDate, final Period paymentPeriod, final double notional, final double spread, final IndexON index, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final StubType stubLeg) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.isTrue(notional >= 0, "notional < 0");
    final ZonedDateTime[] adjustedDateSchedule = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg,
        businessDayConvention, calendar, endOfMonth);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponONSpreadSimplifiedDefinition[] coupons = new CouponONSpreadSimplifiedDefinition[adjustedDateSchedule.length];
    double af = index.getDayCount().getDayCountFraction(settlementDate, adjustedDateSchedule[0]);
    coupons[0] = new CouponONSpreadSimplifiedDefinition(index.getCurrency(), adjustedDateSchedule[0], settlementDate, adjustedDateSchedule[0], af, signedNotional,
        index, settlementDate, adjustedDateSchedule[0], af, spread);
    for (int loopcpn = 1; loopcpn < adjustedDateSchedule.length; loopcpn++) {
      af = index.getDayCount().getDayCountFraction(adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn]);
      coupons[loopcpn] = new CouponONSpreadSimplifiedDefinition(index.getCurrency(), adjustedDateSchedule[loopcpn], adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn],
          af, signedNotional, index, adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn], af, spread);
    }
    return new AnnuityDefinition<>(coupons, calendar);
  }

  /**
   * Annuity of coupon with compunded of ON simple rates. Simplified version (i.e. only the start date and end date of coupon periods are computed) with spread.
   * @param settlementDate The annuity settlement date. The date is not adjusted for the calendar.
   * @param maturityDate The annuity maturity date. The date is adjusted according to the calendar and conventions.
   * @param paymentPeriod The annuity payment period.
   * @param notional The notional.
   * @param spread The spread rate.
   * @param index The overnight index associated to all the coupons of the leg.
   * @param isPayer The payer flag.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param calendar The calendar associated to the payments.
   * @param stubLeg The type of stub for the leg.
   * @param paymentLag 
   * @return The annuity.
   */
  public static AnnuityDefinition<CouponONSpreadSimplifiedDefinition> couponONSimpleCompoundedSpreadSimplified(final ZonedDateTime settlementDate,
      final ZonedDateTime maturityDate, final Period paymentPeriod, final double notional, final double spread, final IndexON index, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final StubType stubLeg, final int paymentLag) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.isTrue(notional >= 0, "notional < 0");
    final ZonedDateTime[] adjustedDateSchedule = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg,
        businessDayConvention, calendar, endOfMonth);
    final ZonedDateTime[] paymentDateSchedule = ScheduleCalculator.getAdjustedDate(adjustedDateSchedule, paymentLag, calendar);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponONSpreadSimplifiedDefinition[] coupons = new CouponONSpreadSimplifiedDefinition[adjustedDateSchedule.length];
    double af = index.getDayCount().getDayCountFraction(settlementDate, adjustedDateSchedule[0]);
    coupons[0] = new CouponONSpreadSimplifiedDefinition(index.getCurrency(), paymentDateSchedule[0], settlementDate, adjustedDateSchedule[0], af, signedNotional,
        index, settlementDate, adjustedDateSchedule[0], af, spread);
    for (int loopcpn = 1; loopcpn < adjustedDateSchedule.length; loopcpn++) {
      af = index.getDayCount().getDayCountFraction(adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn]);
      coupons[loopcpn] = new CouponONSpreadSimplifiedDefinition(index.getCurrency(), paymentDateSchedule[loopcpn], adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn],
          af, signedNotional, index, adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn], af, spread);
    }
    return new AnnuityDefinition<>(coupons, calendar);
  }

  /**
   * Annuity of coupon with arithmetic average of ON rates. Simplified version (i.e. only the start date and end date of coupon periods are computed) with spread.
   * @param settlementDate The annuity settlement date. The date is not adjusted for the calendar.
   * @param maturityDate The annuity maturity date. The date is adjusted according to the calendar and conventions.
   * @param paymentPeriod The annuity payment period.
   * @param notional The notional.
   * @param spread The spread rate.
   * @param index The overnight index associated to all the coupons of the leg.
   * @param isPayer The payer flag.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param calendar The calendar associated to the payments.
   * @param stubLeg The type of stub for the leg.
   * @return The annuity.
   */
  public static AnnuityDefinition<CouponONArithmeticAverageSpreadSimplifiedDefinition> couponONArithmeticAverageSpreadSimplified(final ZonedDateTime settlementDate,
      final ZonedDateTime maturityDate, final Period paymentPeriod, final double notional, final double spread, final IndexON index, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final StubType stubLeg) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.isTrue(notional >= 0, "notional < 0");
    final ZonedDateTime[] adjustedDateSchedule = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg,
        businessDayConvention, calendar, endOfMonth);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponONArithmeticAverageSpreadSimplifiedDefinition[] coupons = new CouponONArithmeticAverageSpreadSimplifiedDefinition[adjustedDateSchedule.length];
    double af = index.getDayCount().getDayCountFraction(settlementDate, adjustedDateSchedule[0]);
    coupons[0] = new CouponONArithmeticAverageSpreadSimplifiedDefinition(index.getCurrency(), adjustedDateSchedule[0], settlementDate, adjustedDateSchedule[0],
        af, signedNotional, index, spread);
    for (int loopcpn = 1; loopcpn < adjustedDateSchedule.length; loopcpn++) {
      af = index.getDayCount().getDayCountFraction(adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn]);
      coupons[loopcpn] = new CouponONArithmeticAverageSpreadSimplifiedDefinition(index.getCurrency(), adjustedDateSchedule[loopcpn], adjustedDateSchedule[loopcpn - 1],
          adjustedDateSchedule[loopcpn], af, signedNotional, index, spread);
    }
    return new AnnuityDefinition<>(coupons, calendar);
  }

}
