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

import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.ActualActualICMA;
import com.opengamma.financial.convention.daycount.ActualActualICMANormal;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.ThirtyEThreeSixtyISDA;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A utility to create different type of annuities.
 */
public class AnnuityDefinitionBuilder {

  /*
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
    //TODO: only initial stub supported
    double accrualFactor = getDayCountFraction(paymentPeriod, calendar, dayCount, stub, StubType.NONE, settlementDate, adjustedEndAccrualDates[0], true, adjustedEndAccrualDates.length == 1);
    coupons[0] = new CouponFixedDefinition(currency, paymentDates[0], settlementDate, adjustedEndAccrualDates[0], accrualFactor, sign * notional, fixedRate);
    for (int loopcpn = 1; loopcpn < adjustedEndAccrualDates.length; loopcpn++) {
      boolean isMaturity = (adjustedEndAccrualDates.length - 1 == loopcpn);
      accrualFactor = getDayCountFraction(paymentPeriod, calendar, dayCount, stub, StubType.NONE, adjustedEndAccrualDates[loopcpn - 1], adjustedEndAccrualDates[loopcpn], false, isMaturity);
      coupons[loopcpn] = new CouponFixedDefinition(currency, paymentDates[loopcpn], adjustedEndAccrualDates[loopcpn - 1], adjustedEndAccrualDates[loopcpn], accrualFactor, sign * notional, fixedRate);
    }
    return new AnnuityCouponFixedDefinition(coupons, calendar);
  }

  /**
   * Get daycount fraction (handles some specific daycount complexities)
   *
   * @param paymentPeriod the payment period (used in some ICMA daycounts)
   * @param calendar the calendar
   * @param dayCount the daycount
   * @param startStub the stub (used in some ICMA daycounts)
   * @param endStub the stub (used in some ICMA daycounts)
   * @param from from date
   * @param to to date
   * @param isFirstCoupon is this the first coupon (needed for ICMA daycount)
   * @param isMaturity is this the last coupon (needed for 30E/360 ISDA)
   * @return the accrual factor
   */
  public static double getDayCountFraction(Period paymentPeriod, Calendar calendar, DayCount dayCount, StubType startStub, StubType endStub, ZonedDateTime from, ZonedDateTime to, boolean isFirstCoupon, boolean isMaturity) {
    // only pass in stub if at first or last period.
    StubType stub = null;
    if (isFirstCoupon) {
      stub = startStub;
    } else if (isMaturity) {
      stub = endStub;
    }
    if (stub == null) {
      stub = StubType.NONE;
    }
    double accrualFactor;
    if ((dayCount instanceof ActualActualICMA)) {
      accrualFactor = ((ActualActualICMA) dayCount).getAccruedInterest(from, to, to, 1.0d, couponPerYear(paymentPeriod), stub);
    } else {
      if ((dayCount instanceof ActualActualICMANormal)) {
        accrualFactor = ((ActualActualICMANormal) dayCount).getAccruedInterest(from, to, to, 1.0d, couponPerYear(paymentPeriod), stub);
      } else {
        if ((dayCount instanceof ThirtyEThreeSixtyISDA)) {
          accrualFactor = ((ThirtyEThreeSixtyISDA) dayCount).getDayCountFraction(from, to, isMaturity);
        } else {
          accrualFactor = dayCount.getDayCountFraction(from, to, calendar);
        }
      }
    }
    return accrualFactor;
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
   * @param adjuster the roll date adjuster (with adjustment months set to 0 - handled outside here for now), null for no day adjustment.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition couponFixed(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional,
      final double fixedRate, final boolean isPayer, final StubType stub, final int paymentLag, final RollDateAdjuster adjuster) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDay, "business day convention");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] adjustedEndAccrualDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stub,
        businessDay, calendar, isEOM, adjuster);
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
   * @param adjuster the roll date adjuster (with adjustment months set to 0 - handled outside here for now), null for no day adjustment.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition couponFixed(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final NotionalProvider notional,
      final double fixedRate, final boolean isPayer, final StubType stub, final int paymentLag, final RollDateAdjuster adjuster) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDay, "business day convention");
    final ZonedDateTime[] adjustedEndAccrualDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stub,
        businessDay, calendar, isEOM, adjuster);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDate(adjustedEndAccrualDates, paymentLag, calendar);
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[adjustedEndAccrualDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedDefinition(currency, paymentDates[0], settlementDate, adjustedEndAccrualDates[0],
        dayCount.getDayCountFraction(settlementDate, adjustedEndAccrualDates[0], calendar),
        getSignedNotional(notional, isPayer, settlementDate), fixedRate);
    for (int loopcpn = 1; loopcpn < adjustedEndAccrualDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(currency, paymentDates[loopcpn], adjustedEndAccrualDates[loopcpn - 1], adjustedEndAccrualDates[loopcpn],
          dayCount.getDayCountFraction(adjustedEndAccrualDates[loopcpn - 1], adjustedEndAccrualDates[loopcpn], calendar),
          getSignedNotional(notional, isPayer, adjustedEndAccrualDates[loopcpn - 1]), fixedRate);
    }
    return new AnnuityCouponFixedDefinition(coupons, calendar);
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
   * Examples: If the payment period is P6M and the period in the adjuster is P3M, n is 2. If the payment period is P3M and the period in the adjuster is P3M, n is 1.
   * If the payment period is P1M and the period in the adjuster is P3M, n is 1.
   * @param notional The swap notional.
   * @param rate The fixed rate.
   * @param isPayer The payer flag.
   * @param dayCount The day count for the coupon accrual factors.
   * @param calendar The calendar for the date adjustments.
   * @param stub The stub type.
   * @param paymentLag The payment lag.
   * @return The fixed coupons annuity.
   */
  public static AnnuityDefinition<CouponFixedDefinition> couponFixedRollDate(final Currency currency, final ZonedDateTime startDate, final int startNumberRollDate,
      final int endNumberRollDate, final RollDateAdjuster adjuster, final Period paymentPeriod, final double notional, final double rate, final boolean isPayer,
      final DayCount dayCount, final Calendar calendar, final StubType stub, final int paymentLag) {
    final long rollMonths = adjuster.getMonthsToAdjust();
    final long paymentMonths = paymentPeriod.toTotalMonths();
    final int rollJump = Math.max(1, (int) (paymentMonths / rollMonths)); // The roll jumps is rounded (toward 0) and is at least 1.
    final int nbRoll = endNumberRollDate - startNumberRollDate;
    final List<ZonedDateTime> legDates = rollDateSchedule(startDate, startNumberRollDate, adjuster, calendar, stub, rollJump, nbRoll);
    final double sign = isPayer ? -1.0 : 1.0;
    final int nbCpn = legDates.size() - 1;
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[nbCpn];
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) {
      final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(legDates.get(loopcpn + 1), paymentLag, calendar);
      final double accrualFactor = dayCount.getDayCountFraction(legDates.get(loopcpn), legDates.get(loopcpn + 1));
      coupons[loopcpn] = new CouponFixedDefinition(currency, paymentDate, legDates.get(loopcpn), legDates.get(loopcpn + 1), accrualFactor, notional * sign, rate);
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
   * @param paymentLag The payment lag (in days).
   * @param notionalStart Notional paid at the start date.
   * @param notionalEnd Notional paid at the end date.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition couponFixedWithNotional(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional,
      final double fixedRate, final boolean isPayer, final StubType stub, final int paymentLag, final boolean notionalStart, final boolean notionalEnd) {
    final AnnuityDefinition<CouponFixedDefinition> annuityNoNotional = couponFixed(currency, settlementDate, maturityDate, paymentPeriod, calendar, dayCount, businessDay,
        isEOM, notional, fixedRate, isPayer, stub, paymentLag);
    final double sign = (isPayer) ? -1.0 : 1.0;
    final int nbPay = annuityNoNotional.getNumberOfPayments();
    int nbNotional = 0;
    nbNotional = (notionalStart ? nbNotional + 1 : nbNotional);
    nbNotional = (notionalEnd ? nbNotional + 1 : nbNotional);
    final CouponFixedDefinition[] annuityWithNotional = new CouponFixedDefinition[nbPay + nbNotional];
    int loopnot = 0;
    if (notionalStart) {
      annuityWithNotional[0] = new CouponFixedDefinition(currency, settlementDate, settlementDate, settlementDate, 1.0, -notional * sign, 1);
      // Implementation note: Fixed amount written as a fixed coupon.
      loopnot++;
    }
    System.arraycopy(annuityNoNotional.getPayments(), 0, annuityWithNotional, loopnot, nbPay);
    if (notionalEnd) {
      final ZonedDateTime payDate = annuityNoNotional.getNthPayment(nbPay - 1).getPaymentDate();
      annuityWithNotional[nbPay + loopnot] = new CouponFixedDefinition(currency, payDate, payDate, payDate, 1.0, notional * sign, 1);
    }
    return new AnnuityCouponFixedDefinition(annuityWithNotional, calendar);
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
   * @param adjuster the roll date adjuster (with adjustment months set to 0 - handled outside here for now), null for no day adjustment.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition couponIbor(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod,
      final double notional, final IborIndex index, final boolean isPayer, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final boolean endOfMonth,
      final Calendar calendar, final StubType stub, final int paymentLag, final RollDateAdjuster adjuster) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(dayCount, "Day count convention");
    ArgumentChecker.isTrue(notional >= 0, "notional <= 0");
    final ZonedDateTime[] adjustedEndAccrualDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stub,
        businessDayConvention, calendar, endOfMonth, adjuster);
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
   * @param adjuster the roll date adjuster (with adjustment months set to 0 - handled outside here for now), null for no day adjustment.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition couponIbor(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod,
      final NotionalProvider notional, final IborIndex index, final boolean isPayer, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final boolean endOfMonth,
      final Calendar calendar, final StubType stub, final int paymentLag, final RollDateAdjuster adjuster) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(dayCount, "Day count convention");
    final ZonedDateTime[] adjustedEndAccrualDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stub,
        businessDayConvention, calendar, endOfMonth, adjuster);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDate(adjustedEndAccrualDates, paymentLag, calendar);
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponIborDefinition[] coupons = new CouponIborDefinition[adjustedEndAccrualDates.length];
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), calendar);
    coupons[0] = new CouponIborDefinition(index.getCurrency(), paymentDates[0], settlementDate, adjustedEndAccrualDates[0],
        dayCount.getDayCountFraction(settlementDate, adjustedEndAccrualDates[0], calendar), sign * notional.getAmount(settlementDate.toLocalDate()), fixingDate, index, calendar);
    for (int loopcpn = 1; loopcpn < adjustedEndAccrualDates.length; loopcpn++) {
      fixingDate = ScheduleCalculator.getAdjustedDate(adjustedEndAccrualDates[loopcpn - 1], -index.getSpotLag(), calendar);
      coupons[loopcpn] = new CouponIborDefinition(index.getCurrency(), paymentDates[loopcpn], adjustedEndAccrualDates[loopcpn - 1], adjustedEndAccrualDates[loopcpn],
          dayCount.getDayCountFraction(adjustedEndAccrualDates[loopcpn - 1], adjustedEndAccrualDates[loopcpn], calendar),
          sign * notional.getAmount(adjustedEndAccrualDates[loopcpn - 1].toLocalDate()), fixingDate, index, calendar);
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
      final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(legDates.get(loopcpn), -index.getSpotLag(), calendar);
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
   * The start and end dates of the fixing period are given by the end and start accrual dates of the coupon that are given by the adjuster.
   * The may differ from the theoretical date of the index.
   * Examples: If the index tenor is P6M and the period in the adjuster is P3M, n is 2. If the index tenor is P3M and the period in the adjuster is P3M, n is 1.
   * If the index tenor is P1M and the period in the adjuster is P3M, n is 1.
   * @param notional The swap notional.
   * @param isPayer The payer flag.
   * @param dayCount The coupons day count. Can be different from the one of the index.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborDefinition> couponIborRollDateIndexAdjusted(final ZonedDateTime startDate, final int startNumberRollDate, final int endNumberRollDate,
      final RollDateAdjuster adjuster, final IborIndex index, final double notional, final boolean isPayer, final DayCount dayCount, final Calendar calendar, final StubType stub) {
    final long rollMonths = adjuster.getMonthsToAdjust();
    final long paymentMonths = index.getTenor().toTotalMonths();
    final int rollJump = Math.max(1, (int) (paymentMonths / rollMonths)); // The roll jumps is rounded (toward 0) and is at least 1.
    final int nbRoll = endNumberRollDate - startNumberRollDate;
    final List<ZonedDateTime> legDates = rollDateSchedule(startDate, startNumberRollDate, adjuster, calendar, stub, rollJump, nbRoll);
    final double sign = isPayer ? -1.0 : 1.0;
    final int nbCpn = legDates.size() - 1;
    final CouponIborDefinition[] coupons = new CouponIborDefinition[nbCpn];
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) {
      final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(legDates.get(loopcpn), -index.getSpotLag(), calendar);
      final double af = dayCount.getDayCountFraction(legDates.get(loopcpn), legDates.get(loopcpn + 1), calendar);
      coupons[loopcpn] = new CouponIborDefinition(index.getCurrency(), legDates.get(loopcpn + 1), legDates.get(loopcpn), legDates.get(loopcpn + 1),
          af, sign * notional, fixingDate, legDates.get(loopcpn), legDates.get(loopcpn + 1), af, index, calendar);
    }
    return new AnnuityDefinition<>(coupons, calendar);
  }

  /**
   * Builder of an annuity of Ibor coupons with payment of notional at the start or at the end.
   * The initial payment of notional is with the opposite sign as the coupons and the final notional has the same sign as the coupons.
   * @param settlementDate The settlement date.
   * @param maturityDate The maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param dayCountAnnuity The day count used for the annuity accrual factors (not for the fixing period accrual factors).
   * @param bdcAnnuity The business day convention used for the annuity payment dates (not for the fixing periods).
   * @param eomAnnuity The end-of-month rule used for the annuity payment dates (not for the fixing periods).
   * @param paymentPeriod The payment period (not the tenor of the underlying index).
   * @param isPayer The payer flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @param paymentLag The payment lag (in days).
   * @param notionalStart Notional paid at the start date.
   * @param notionalEnd Notional paid at the end date.
   * @return The annuity.
   */
  public static AnnuityDefinition<CouponDefinition> couponIborWithNotional(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional,
      final IborIndex index, final DayCount dayCountAnnuity, final BusinessDayConvention bdcAnnuity, final boolean eomAnnuity,
      final Period paymentPeriod, final boolean isPayer, final Calendar calendar, final StubType stub, final int paymentLag,
      final boolean notionalStart, final boolean notionalEnd) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(index, "index");
    final AnnuityDefinition<CouponIborDefinition> annuityNoNotional = couponIbor(settlementDate, maturityDate, index.getTenor(), notional, index,
        isPayer, dayCountAnnuity, bdcAnnuity, eomAnnuity, calendar, stub, paymentLag);
    final double sign = (isPayer) ? -1.0 : 1.0;
    final int nbPay = annuityNoNotional.getNumberOfPayments();
    int nbNotional = 0;
    nbNotional = (notionalStart ? nbNotional + 1 : nbNotional);
    nbNotional = (notionalEnd ? nbNotional + 1 : nbNotional);
    final CouponDefinition[] annuityWithNotional = new CouponDefinition[nbPay + nbNotional];
    int loopnot = 0;
    if (notionalStart) {
      annuityWithNotional[0] = new CouponFixedDefinition(index.getCurrency(), settlementDate, settlementDate, settlementDate, 1.0, -notional * sign, 1);
      // Implementation note: Fixed amount written as a fixed coupon.
      loopnot++;
    }
    System.arraycopy(annuityNoNotional.getPayments(), 0, annuityWithNotional, loopnot, nbPay);
    if (notionalEnd) {
      final ZonedDateTime payDate = annuityNoNotional.getNthPayment(nbPay - 1).getPaymentDate();
      annuityWithNotional[nbPay + loopnot] = new CouponFixedDefinition(index.getCurrency(), payDate, payDate, payDate, 1.0, notional * sign, 1);
    }
    return new AnnuityDefinition<>(annuityWithNotional, calendar);
  }

  /**
   * Builder of an annuity of Ibor coupons with payment of notional at the start or at the end.
   * The initial payment of notional is with the opposite sign as the coupons and the final notional has the same sign as the coupons.
   * The day count, business day convention and EOM rule of the annuity are the same as the one of the index.
   * @param settlementDate The settlement date.
   * @param maturityDate The maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @param paymentLag The payment lag (in days).
   * @param notionalStart Notional paid at the start date.
   * @param notionalEnd Notional paid at the end date.
   * @return The annuity.
   */
  public static AnnuityDefinition<CouponDefinition> couponIborWithNotional(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional,
      final IborIndex index, final boolean isPayer, final Calendar calendar, final StubType stub, final int paymentLag,
      final boolean notionalStart, final boolean notionalEnd) {
    return couponIborWithNotional(settlementDate, maturityDate, notional, index, index.getDayCount(), index.getBusinessDayConvention(),
        index.isEndOfMonth(), index.getTenor(), isPayer, calendar, stub, paymentLag, notionalStart, notionalEnd);
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
      final double notional, final double spread, final IborIndex index, final boolean isPayer, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final boolean endOfMonth, final Calendar calendar, final StubType stub, final int paymentLag) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(dayCount, "Day count convention");
    ArgumentChecker.isTrue(notional >= 0, "notional < 0");
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
   * @param adjuster the roll date adjuster, null for no adjustment.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborSpreadDefinition> couponIborSpread(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod,
      final double notional, final double spread, final IborIndex index, final boolean isPayer, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final boolean endOfMonth, final Calendar calendar, final StubType stub, final int paymentLag, final RollDateAdjuster adjuster) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(dayCount, "Day count convention");
    ArgumentChecker.isTrue(notional >= 0, "notional < 0");
    final ZonedDateTime[] adjustedEndAccrualDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stub,
        businessDayConvention, calendar, endOfMonth, adjuster);
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
   * @param adjuster the roll date adjuster, null for no adjustment.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborSpreadDefinition> couponIborSpread(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod,
      final NotionalProvider notional, final double spread, final IborIndex index, final boolean isPayer, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final boolean endOfMonth, final Calendar calendar, final StubType stub, final int paymentLag, final RollDateAdjuster adjuster) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(dayCount, "Day count convention");
    final ZonedDateTime[] adjustedEndAccrualDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stub,
        businessDayConvention, calendar, endOfMonth, adjuster);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDate(adjustedEndAccrualDates, paymentLag, calendar);
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[adjustedEndAccrualDates.length];
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), calendar);
    coupons[0] = new CouponIborSpreadDefinition(index.getCurrency(), paymentDates[0], settlementDate, adjustedEndAccrualDates[0],
        dayCount.getDayCountFraction(settlementDate, adjustedEndAccrualDates[0], calendar), sign * notional.getAmount(settlementDate.toLocalDate()), fixingDate, index, spread, calendar);
    for (int loopcpn = 1; loopcpn < adjustedEndAccrualDates.length; loopcpn++) {
      fixingDate = ScheduleCalculator.getAdjustedDate(adjustedEndAccrualDates[loopcpn - 1], -index.getSpotLag(), calendar);
      coupons[loopcpn] = new CouponIborSpreadDefinition(index.getCurrency(), paymentDates[loopcpn], adjustedEndAccrualDates[loopcpn - 1], adjustedEndAccrualDates[loopcpn],
          dayCount.getDayCountFraction(adjustedEndAccrualDates[loopcpn - 1], adjustedEndAccrualDates[loopcpn], calendar),
          sign * notional.getAmount(adjustedEndAccrualDates[loopcpn - 1].toLocalDate()), fixingDate, index, spread, calendar);
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
  public static AnnuityDefinition<CouponIborSpreadDefinition> couponIborSpread(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional,
      final IborIndex index, final double spread, final boolean isPayer, final Calendar calendar) {
    ArgumentChecker.notNull(index, "index");
    return couponIborSpread(settlementDate, maturityDate, index.getTenor(), notional, spread, index, isPayer, index.getDayCount(), index.getBusinessDayConvention(),
        index.isEndOfMonth(), calendar, StubType.SHORT_START, 0);
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
   * Create an Ibor leg based on a roll date convention. The coupons are in line with the roll dates of the adjuster (see payment period for more details).
   * @param startDate The start/reference date of the computation.
   * @param startNumberRollDate The number of roll dates to the effective date of the swap.
   * @param endNumberRollDate The number of roll dates to the maturity of the swap.
   * @param adjuster The date adjuster, e.g. IMM quarterly dates.
   * @param index The Ibor index. There is no check that the index is coherent with the adjuster.
   * The index period is used in the following way: the ratio "n" of number of month in the index period and of the "MonthsToAdjust" of the "adjuster" is computed.
   * The ratio is computed with the long division (i.e. with rounding toward 0) and minimum at 1. The payment and accrual dates are the adjuster n-th dates.
   * The start and end dates of the fixing period are given by the start and end accrual dates of the coupon that are given by the adjuster, they may differ from the theoretical index dates.
   * Examples: If the index tenor is P6M and the period in the adjuster is P3M, n is 2. If the index tenor is P3M and the period in the adjuster is P3M, n is 1.
   * If the index tenor is P1M and the period in the adjuster is P3M, n is 1.
   * @param notional The swap notional.
   * @param spread The spread.
   * @param isPayer The payer flag.
   * @param dayCount The coupons day count. Can be different from the one of the index.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborSpreadDefinition> couponIborSpreadRollDateIndexAdjusted(final ZonedDateTime startDate, final int startNumberRollDate, final int endNumberRollDate,
      final RollDateAdjuster adjuster, final IborIndex index, final double notional, final double spread, final boolean isPayer, final DayCount dayCount, final Calendar calendar,
      final StubType stub) {
    ArgumentChecker.isTrue(startNumberRollDate > 0, "number of start roll dates negative");
    ArgumentChecker.isTrue(endNumberRollDate > 0, "number of end roll dates negative");
    final long rollMonths = adjuster.getMonthsToAdjust();
    final long paymentMonths = index.getTenor().toTotalMonths();
    final int rollJump = Math.max(1, (int) (paymentMonths / rollMonths)); // The roll jumps is rounded (toward 0) and is at least 1.
    final int nbRoll = endNumberRollDate - startNumberRollDate;
    final List<ZonedDateTime> legDates = rollDateSchedule(startDate, startNumberRollDate, adjuster, calendar, stub, rollJump, nbRoll);
    final double sign = isPayer ? -1.0 : 1.0;
    final int nbCpn = legDates.size() - 1;
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[nbCpn];
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) {
      final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(legDates.get(loopcpn), -index.getSpotLag(), calendar);
      final double af = dayCount.getDayCountFraction(legDates.get(loopcpn), legDates.get(loopcpn + 1), calendar);
      coupons[loopcpn] = new CouponIborSpreadDefinition(index.getCurrency(), legDates.get(loopcpn + 1), legDates.get(loopcpn), legDates.get(loopcpn + 1),
          af, sign * notional, fixingDate, legDates.get(loopcpn), legDates.get(loopcpn + 1), af, index, spread, calendar);
    }
    return new AnnuityDefinition<>(coupons, calendar);
  }

  /**
   * Builder of an annuity of Ibor coupons with spread and payment of notional at the start or at the end.
   * The initial payment of notional is the opposite of the final one.
   * @param settlementDate The settlement date.
   * @param maturityDate The maturity date.
   * @param notional The notional.
   * @param spread The common spread.
   * @param index The Ibor index.
   * @param dayCountAnnuity The day count used for the annuity accrual factors (not for the fixing period accrual factors).
   * @param bdcAnnuity The business day convention used for the annuity payment dates (not for the fixing periods).
   * @param eomAnnuity The end-of-month rule used for the annuity payment dates (not for the fixing periods).
   * @param paymentPeriod The payment period (not the tenor of the underlying index).
   * @param isPayer The payer flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @param paymentLag The payment lag (in days).
   * @param notionalStart Notional paid at the start date.
   * @param notionalEnd Notional paid at the end date.
   * @return The annuity.
   */
  public static AnnuityDefinition<CouponDefinition> couponIborSpreadWithNotional(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional,
      final double spread, final IborIndex index, final DayCount dayCountAnnuity, final BusinessDayConvention bdcAnnuity, final boolean eomAnnuity,
      final Period paymentPeriod, final boolean isPayer, final Calendar calendar, final StubType stub, final int paymentLag,
      final boolean notionalStart, final boolean notionalEnd) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(index, "index");
    final AnnuityDefinition<CouponIborSpreadDefinition> annuityNoNotional = couponIborSpread(settlementDate, maturityDate, paymentPeriod, notional, spread, index,
        isPayer, dayCountAnnuity, bdcAnnuity, eomAnnuity, calendar, stub, paymentLag);
    final double sign = (isPayer) ? -1.0 : 1.0;
    final int nbPay = annuityNoNotional.getNumberOfPayments();
    int nbNotional = 0;
    nbNotional = (notionalStart ? nbNotional + 1 : nbNotional);
    nbNotional = (notionalEnd ? nbNotional + 1 : nbNotional);
    final CouponDefinition[] annuityWithNotional = new CouponDefinition[nbPay + nbNotional];
    int loopnot = 0;
    if (notionalStart) {
      annuityWithNotional[0] = new CouponFixedDefinition(index.getCurrency(), settlementDate, settlementDate, settlementDate, 1.0, -notional * sign, 1);
      // Implementation note: Fixed amount written as a fixed coupon.
      loopnot++;
    }
    System.arraycopy(annuityNoNotional.getPayments(), 0, annuityWithNotional, loopnot, nbPay);
    if (notionalEnd) {
      final ZonedDateTime payDate = annuityNoNotional.getNthPayment(nbPay - 1).getPaymentDate();
      annuityWithNotional[nbPay + loopnot] = new CouponFixedDefinition(index.getCurrency(), payDate, payDate, payDate, 1.0, notional * sign, 1);
    }
    return new AnnuityDefinition<>(annuityWithNotional, calendar);
  }

  /**
   * Builder of an annuity of Ibor coupons with spread and payment of notional at the start or at the end.
   * The initial payment of notional is the opposite of the final one.
   * The payment period, day count, business day convention and EOM rule of the annuity are the same as the one of the index.
   * @param settlementDate The settlement date.
   * @param maturityDate The maturity date.
   * @param notional The notional.
   * @param spread The common spread.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @param paymentLag The payment lag (in days).
   * @param notionalStart Notional paid at the start date.
   * @param notionalEnd Notional paid at the end date.
   * @return The annuity.
   */
  public static AnnuityDefinition<CouponDefinition> couponIborSpreadWithNotional(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional,
      final double spread, final IborIndex index, final boolean isPayer, final Calendar calendar, final StubType stub, final int paymentLag,
      final boolean notionalStart, final boolean notionalEnd) {
    return couponIborSpreadWithNotional(settlementDate, maturityDate, notional, spread, index, index.getDayCount(),
        index.getBusinessDayConvention(), index.isEndOfMonth(), index.getTenor(), isPayer, calendar, stub, paymentLag, notionalStart, notionalEnd);
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
    ArgumentChecker.isTrue(notional >= 0, "notional < 0");
    final ZonedDateTime[] adjustedDateSchedule = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg, businessDayConvention, calendar, endOfMonth);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponIborCompoundingDefinition[] coupons = new CouponIborCompoundingDefinition[adjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingDefinition.from(signedNotional, settlementDate, adjustedDateSchedule[0], index, stubCompound, businessDayConvention, endOfMonth, calendar);
    for (int loopcpn = 1; loopcpn < adjustedDateSchedule.length; loopcpn++) {
      coupons[loopcpn] = CouponIborCompoundingDefinition.from(signedNotional, adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn], index, stubCompound,
          businessDayConvention, endOfMonth, calendar);
    }
    return new AnnuityDefinition<>(coupons, calendar);
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
   * @param adjuster the roll date adjuster, null for no adjustment.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborCompoundingDefinition> couponIborCompounding(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final double notional, final IborIndex index, final StubType stubCompound, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final StubType stubLeg, final RollDateAdjuster adjuster) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.isTrue(notional >= 0, "notional < 0");
    final ZonedDateTime[] adjustedDateSchedule = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg, businessDayConvention, calendar, endOfMonth);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponIborCompoundingDefinition[] coupons = new CouponIborCompoundingDefinition[adjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingDefinition.from(signedNotional, settlementDate, adjustedDateSchedule[0], index, stubCompound, businessDayConvention, endOfMonth, calendar, adjuster);
    for (int loopcpn = 1; loopcpn < adjustedDateSchedule.length; loopcpn++) {
      coupons[loopcpn] = CouponIborCompoundingDefinition.from(signedNotional, adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn], index, stubCompound,
          businessDayConvention, endOfMonth, calendar, adjuster);
    }
    return new AnnuityDefinition<>(coupons, calendar);
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
   * @param adjuster the roll date adjuster, null for no adjustment.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborCompoundingDefinition> couponIborCompounding(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod,
      final NotionalProvider notional, final IborIndex index, final StubType stubCompound, final boolean isPayer, final BusinessDayConvention businessDayConvention, final boolean endOfMonth,
      final Calendar calendar, final StubType stubLeg, final RollDateAdjuster adjuster) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    final ZonedDateTime[] adjustedDateSchedule = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg, businessDayConvention, calendar, endOfMonth);
    final CouponIborCompoundingDefinition[] coupons = new CouponIborCompoundingDefinition[adjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingDefinition.from(getSignedNotional(notional, isPayer, settlementDate), settlementDate, adjustedDateSchedule[0], index, stubCompound, businessDayConvention,
        endOfMonth, calendar, adjuster);
    final boolean isStubShort = stubCompound.equals(StubType.SHORT_END) || stubCompound.equals(StubType.SHORT_START);
    final boolean isStubStart = stubCompound.equals(StubType.LONG_START) || stubCompound.equals(StubType.SHORT_START);
    // Implementation note: dates computed from the end.
    final ZonedDateTime[] notionalDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, isStubShort, isStubStart,
        businessDayConvention, calendar, endOfMonth, adjuster);
    for (int loopcpn = 1; loopcpn < adjustedDateSchedule.length; loopcpn++) {
      coupons[loopcpn] = CouponIborCompoundingDefinition.from(getSignedNotional(notional, isPayer, notionalDates[loopcpn - 1]), adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn],
          index, stubCompound,
          businessDayConvention, endOfMonth, calendar, adjuster);
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
  public static AnnuityDefinition<CouponIborCompoundingSpreadDefinition> couponIborCompoundingSpread(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod,
      final double notional, final double spread, final IborIndex index, final StubType stubCompound, final boolean isPayer, final BusinessDayConvention businessDayConvention,
      final boolean endOfMonth, final Calendar calendar, final StubType stubLeg) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.isTrue(notional >= 0, "notional < 0");
    final ZonedDateTime[] adjustedDateSchedule = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg, businessDayConvention, calendar, endOfMonth);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponIborCompoundingSpreadDefinition[] coupons = new CouponIborCompoundingSpreadDefinition[adjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingSpreadDefinition.from(signedNotional, settlementDate, adjustedDateSchedule[0], index, spread, stubCompound, businessDayConvention, endOfMonth, calendar);
    for (int loopcpn = 1; loopcpn < adjustedDateSchedule.length; loopcpn++) {
      coupons[loopcpn] = CouponIborCompoundingSpreadDefinition.from(signedNotional, adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn], index, spread, stubCompound,
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
   * @param adjuster the roll date adjuster, null for no adjustment.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborCompoundingSpreadDefinition> couponIborCompoundingSpread(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final double notional, final double spread, final IborIndex index, final StubType stubCompound, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final StubType stubLeg, final RollDateAdjuster adjuster) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.isTrue(notional >= 0, "notional < 0");
    final ZonedDateTime[] adjustedDateSchedule = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg, businessDayConvention, calendar, endOfMonth);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponIborCompoundingSpreadDefinition[] coupons = new CouponIborCompoundingSpreadDefinition[adjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingSpreadDefinition.from(signedNotional, settlementDate, adjustedDateSchedule[0], index, spread, stubCompound, businessDayConvention, endOfMonth, calendar,
        adjuster);
    for (int loopcpn = 1; loopcpn < adjustedDateSchedule.length; loopcpn++) {
      coupons[loopcpn] = CouponIborCompoundingSpreadDefinition.from(signedNotional, adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn], index, spread, stubCompound,
          businessDayConvention, endOfMonth, calendar, adjuster);
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
   * @param adjuster the roll date adjuster, null for no adjustment.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborCompoundingSpreadDefinition> couponIborCompoundingSpread(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final NotionalProvider notional, final double spread, final IborIndex index, final StubType stubCompound, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final StubType stubLeg, final RollDateAdjuster adjuster) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    final ZonedDateTime[] adjustedDateSchedule = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg, businessDayConvention, calendar, endOfMonth);
    final CouponIborCompoundingSpreadDefinition[] coupons = new CouponIborCompoundingSpreadDefinition[adjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingSpreadDefinition.from(getSignedNotional(notional, isPayer, settlementDate), settlementDate, adjustedDateSchedule[0], index, spread, stubCompound,
        businessDayConvention, endOfMonth, calendar,
        adjuster);
    final boolean isStubShort = stubCompound.equals(StubType.SHORT_END) || stubCompound.equals(StubType.SHORT_START);
    final boolean isStubStart = stubCompound.equals(StubType.LONG_START) || stubCompound.equals(StubType.SHORT_START); // Implementation note: dates computed from the end.
    final ZonedDateTime[] notionalDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, isStubShort, isStubStart,
        businessDayConvention, calendar, endOfMonth, adjuster);
    for (int loopcpn = 1; loopcpn < adjustedDateSchedule.length; loopcpn++) {
      coupons[loopcpn] = CouponIborCompoundingSpreadDefinition.from(getSignedNotional(notional, isPayer, notionalDates[loopcpn - 1]), adjustedDateSchedule[loopcpn - 1],
          adjustedDateSchedule[loopcpn], index, spread, stubCompound,
          businessDayConvention, endOfMonth, calendar, adjuster);
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
    final ZonedDateTime[] adjustedDateSchedule = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg, businessDayConvention, calendar, endOfMonth);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponIborCompoundingFlatSpreadDefinition[] coupons = new CouponIborCompoundingFlatSpreadDefinition[adjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingFlatSpreadDefinition.from(signedNotional, settlementDate, adjustedDateSchedule[0], index, spread, stubCompound, businessDayConvention, endOfMonth, calendar);
    for (int loopcpn = 1; loopcpn < adjustedDateSchedule.length; loopcpn++) {
      coupons[loopcpn] = CouponIborCompoundingFlatSpreadDefinition.from(signedNotional, adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn], index, spread, stubCompound,
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
   * @param adjuster the roll date adjuster, null for no adjustment.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborCompoundingFlatSpreadDefinition> couponIborCompoundingFlatSpread(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final double notional, final double spread, final IborIndex index, final StubType stubCompound, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final StubType stubLeg, final RollDateAdjuster adjuster) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.isTrue(notional >= 0, "notional < 0");
    final ZonedDateTime[] adjustedDateSchedule = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg, businessDayConvention, calendar, endOfMonth);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponIborCompoundingFlatSpreadDefinition[] coupons = new CouponIborCompoundingFlatSpreadDefinition[adjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingFlatSpreadDefinition.from(signedNotional, settlementDate, adjustedDateSchedule[0], index, spread, stubCompound, businessDayConvention, endOfMonth, calendar,
        adjuster);
    for (int loopcpn = 1; loopcpn < adjustedDateSchedule.length; loopcpn++) {
      coupons[loopcpn] = CouponIborCompoundingFlatSpreadDefinition.from(signedNotional, adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn], index, spread, stubCompound,
          businessDayConvention, endOfMonth, calendar, adjuster);
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
   * @param adjuster the roll date adjuster, null for no adjustment.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborCompoundingFlatSpreadDefinition> couponIborCompoundingFlatSpread(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final NotionalProvider notional, final double spread, final IborIndex index, final StubType stubCompound, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final StubType stubLeg, final RollDateAdjuster adjuster) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    final ZonedDateTime[] adjustedDateSchedule = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, stubLeg, businessDayConvention, calendar, endOfMonth);
    double signedNotional = getSignedNotional(notional, isPayer, settlementDate);
    final CouponIborCompoundingFlatSpreadDefinition[] coupons = new CouponIborCompoundingFlatSpreadDefinition[adjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingFlatSpreadDefinition.from(signedNotional, settlementDate, adjustedDateSchedule[0], index, spread, stubCompound, businessDayConvention, endOfMonth, calendar,
        adjuster);
    final boolean isStubShort = stubCompound.equals(StubType.SHORT_END) || stubCompound.equals(StubType.SHORT_START);
    final boolean isStubStart = stubCompound.equals(StubType.LONG_START) || stubCompound.equals(StubType.SHORT_START); // Implementation note: dates computed from the end.
    final ZonedDateTime[] notionalDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, isStubShort, isStubStart,
        businessDayConvention, calendar, endOfMonth, adjuster);
    for (int loopcpn = 1; loopcpn < adjustedDateSchedule.length; loopcpn++) {
      signedNotional = getSignedNotional(notional, isPayer, notionalDates[loopcpn - 1]);
      coupons[loopcpn] = CouponIborCompoundingFlatSpreadDefinition.from(signedNotional, adjustedDateSchedule[loopcpn - 1], adjustedDateSchedule[loopcpn], index, spread, stubCompound,
          businessDayConvention, endOfMonth, calendar, adjuster);
    }
    return new AnnuityDefinition<>(coupons, calendar);
  }

  private static double getSignedNotional(final NotionalProvider notional, final boolean isPayer, final ZonedDateTime date) {
    return isPayer ? -notional.getAmount(date.toLocalDate()) : notional.getAmount(date.toLocalDate());
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
   * @param paymentLag The payment lag
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
   * Annuity of coupon with compunded of ON simple rates. Simplified version (i.e. only the start date and end date of coupon periods are computed) with spread.
   * @param startDate The start/reference date of the computation.
   * @param startNumberRollDate The number of roll dates to the effective date of the swap.
   * @param endNumberRollDate The number of roll dates to the maturity of the swap.
   * @param adjuster The date adjuster, e.g. IMM quarterly dates.
   * @param paymentPeriod The annuity payment period.
   * @param notional The notional.
   * @param spread The spread rate.
   * @param index The overnight index associated to all the coupons of the leg.
   * @param isPayer The payer flag.
   * @param calendar The calendar associated to the payments.
   * @param stub The type of stub for the leg.
   * @param paymentLag The payment lag.
   * @return The annuity.
   */
  public static AnnuityDefinition<CouponONSpreadSimplifiedDefinition> couponONSimpleCompoundedSpreadSimplifiedRollDate(final ZonedDateTime startDate,
      final int startNumberRollDate, final int endNumberRollDate, final RollDateAdjuster adjuster, final Period paymentPeriod, final double notional,
      final double spread, final IndexON index, final boolean isPayer, final Calendar calendar, final StubType stub, final int paymentLag) {
    ArgumentChecker.notNull(startDate, "settlement date");
    ArgumentChecker.notNull(adjuster, "roll date adjuster");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.isTrue(notional >= 0, "notional < 0");
    final long rollMonths = adjuster.getMonthsToAdjust();
    final long paymentMonths = paymentPeriod.toTotalMonths();
    final int rollJump = (int) (paymentMonths / rollMonths); // The roll jumps is rounded
    final int nbRoll = endNumberRollDate - startNumberRollDate;
    final List<ZonedDateTime> legDates = rollDateSchedule(startDate, startNumberRollDate, adjuster, calendar, stub, rollJump, nbRoll);
    final double sign = isPayer ? -1.0 : 1.0;
    final int nbCpn = legDates.size() - 1;
    final DayCount dayCount = index.getDayCount();
    final CouponONSpreadSimplifiedDefinition[] coupons = new CouponONSpreadSimplifiedDefinition[nbCpn];
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) {
      final double accrualFactor = dayCount.getDayCountFraction(legDates.get(loopcpn), legDates.get(loopcpn + 1));
      final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(legDates.get(loopcpn + 1), paymentLag, calendar);
      coupons[loopcpn] = new CouponONSpreadSimplifiedDefinition(index.getCurrency(), paymentDate, legDates.get(loopcpn), legDates.get(loopcpn + 1), accrualFactor,
          notional * sign, index, legDates.get(loopcpn), legDates.get(loopcpn + 1), accrualFactor, spread);
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

  /**
   * Computes the roll dates for a given adjuster.
   * @param referenceDate The computation reference date.
   * @param startNumberRollDate The number of roll to the start date.
   * @param adjuster The roll date adjuster.
   * @param calendar The calendar.
   * @param stub The stub type.
   * @param rollJump The jump between dates, i.e. 2 will create a schedule with every second date of the adjuster.
   * @param nbRoll The total number of rolls, i.e. n will create n+1 dates separated each by rollJump adjuster dates.
   * @return The list of dates.
   */
  private static List<ZonedDateTime> rollDateSchedule(final ZonedDateTime referenceDate, final int startNumberRollDate, final RollDateAdjuster adjuster, final Calendar calendar,
      final StubType stub, final int rollJump, final int nbRoll) {
    final List<ZonedDateTime> legDates = new ArrayList<>();
    ZonedDateTime currentDate = RollDateAdjusterUtils.nthDate(referenceDate, adjuster, startNumberRollDate);
    legDates.add(ScheduleCalculator.getAdjustedDate(currentDate, 0, calendar));
    final int nbPeriods = nbRoll / rollJump; // Number of full periods
    final int stubPeriod = nbRoll % rollJump;
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
    return legDates;
  }

  /**
   * Private method to compute the number of coupons per year from the payment period.
   * @param period The period.
   * @return The number of coupons per year.
   */
  private static long couponPerYear(Period period) {
    return 12 / period.toTotalMonths();
  }

}
