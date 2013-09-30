/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A utility to create different type of annuities.
 */
public class AnnuityDefinitionBuilder {

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
  public static AnnuityDefinition<CouponFixedDefinition> annuityCouponFixedFrom(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate,
      final boolean isPayer) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDay, "business day convention");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, true, false, businessDay, calendar, isEOM);
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[paymentDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedDefinition(currency, paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0], calendar),
        sign * notional, fixedRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(currency, paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, fixedRate);
    }
    return new AnnuityDefinition<>(coupons, calendar);
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
  public static AnnuityDefinition<CouponFixedDefinition> annuityCouponFixedFrom(final Currency currency, final ZonedDateTime settlementDate, final Period tenor, final Period paymentPeriod,
      final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate, final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenor, "Tenor");
    final ZonedDateTime maturityDate = settlementDate.plus(tenor);
    return annuityCouponFixedFrom(currency, settlementDate, maturityDate, paymentPeriod, calendar, dayCount, businessDay, isEOM, notional, fixedRate, isPayer);
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
  public static AnnuityDefinition<PaymentDefinition> annuityCouponFixedWithNotional(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate,
      final boolean isPayer) {
    final AnnuityDefinition<CouponFixedDefinition> annuityNoNotional = annuityCouponFixedFrom(currency, settlementDate, maturityDate, paymentPeriod, calendar, dayCount, businessDay, isEOM, notional,
        fixedRate, isPayer);
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
   * @return The fixed annuity.
   */
  public static AnnuityDefinition<PaymentDefinition> annuityCouponFixedWithNotional(final Currency currency, final ZonedDateTime settlementDate, final Period tenor,
      final Period paymentPeriod, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate,
      final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    final ZonedDateTime maturityDate = settlementDate.plus(tenor);
    return annuityCouponFixedWithNotional(currency, settlementDate, maturityDate, paymentPeriod, calendar, dayCount, businessDay, isEOM, notional, fixedRate, isPayer);
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
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param dayCount The coupons day count.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborDefinition> annuityIborFrom(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod,
      final double notional, final IborIndex index, final boolean isPayer, final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final DayCount dayCount,
      final Calendar calendar, final StubType stub) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(dayCount, "Day count convention");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final boolean isStubShort = stub.equals(StubType.SHORT_END) || stub.equals(StubType.SHORT_START);
    final boolean isStubStart = stub.equals(StubType.LONG_START) || stub.equals(StubType.SHORT_START); // Implementation note: dates computed from the end.
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, isStubShort,
        isStubStart, businessDayConvention, calendar, endOfMonth);
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
    return new AnnuityDefinition<CouponIborDefinition>(coupons, calendar);
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
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param dayCount The coupons day count.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @return The Ibor annuity.
   */
  public static AnnuityDefinition<CouponIborSpreadDefinition> annuityIborSpreadFrom(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod,
      final double notional, final double spread, final IborIndex index, final boolean isPayer, final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final DayCount dayCount,
      final Calendar calendar, final StubType stub) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(dayCount, "Day count convention");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final boolean isStubShort = stub.equals(StubType.SHORT_END) || stub.equals(StubType.SHORT_START);
    final boolean isStubStart = stub.equals(StubType.LONG_START) || stub.equals(StubType.SHORT_START); // Implementation note: dates computed from the end.
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, isStubShort,
        isStubStart, businessDayConvention, calendar, endOfMonth);
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
    return new AnnuityDefinition<CouponIborSpreadDefinition>(coupons, calendar);
  }

  /**
   * Annuity coupon ibor with spread builder. The annuity business day convention, end-of-month and day count are the one of the Ibor.
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
  public static AnnuityDefinition<CouponIborSpreadDefinition> annuityIborSpreadFrom(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index,
      final double spread, final boolean isPayer, final Calendar calendar) {
    ArgumentChecker.notNull(index, "index");
    return annuityIborSpreadFrom(settlementDate, maturityDate, index.getTenor(), notional, spread, index, isPayer, index.getBusinessDayConvention(), index.isEndOfMonth(),
        index.getDayCount(), calendar, StubType.SHORT_START);
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
  public static AnnuityDefinition<CouponIborSpreadDefinition> annuityIborSpreadFrom(final ZonedDateTime settlementDate, final Period tenor, final double notional, final IborIndex index,
      final double spread, final boolean isPayer, final Calendar calendar) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    final ZonedDateTime maturityDate = settlementDate.plus(tenor);
    return annuityIborSpreadFrom(settlementDate, maturityDate, notional, index, spread, isPayer, calendar);
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
  public static AnnuityDefinition<PaymentDefinition> annuityIborSpreadWithNotionalFrom(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional,
      final IborIndex index, final double spread, final boolean isPayer, final Calendar calendar) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(index, "index");
    final AnnuityDefinition<CouponIborSpreadDefinition> legNoNotional = annuityIborSpreadFrom(settlementDate, maturityDate, notional, index, spread, isPayer, calendar);
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
  public static AnnuityDefinition<PaymentDefinition> annuityIborSpreadWithNotionalFrom(final ZonedDateTime settlementDate, final Period tenor, final double notional, final IborIndex index,
      final double spread, final boolean isPayer, final Calendar calendar) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(tenor, "tenor");
    final ZonedDateTime maturityDate = settlementDate.plus(tenor);
    return annuityIborSpreadWithNotionalFrom(settlementDate, maturityDate, notional, index, spread, isPayer, calendar);
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
  public static AnnuityDefinition<CouponIborCompoundingDefinition> annuityIborCompoundingFrom(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final double notional, final IborIndex index, final StubType stubCompound, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final StubType stubLeg) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final boolean isStubLegShort = stubLeg.equals(StubType.SHORT_END) || stubLeg.equals(StubType.SHORT_START);
    final boolean isStubLegStart = stubLeg.equals(StubType.LONG_START) || stubLeg.equals(StubType.SHORT_START); // Implementation note: dates computed from the end.
    final ZonedDateTime[] unadjustedDateSchedule = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, isStubLegShort, isStubLegStart);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponIborCompoundingDefinition[] coupons = new CouponIborCompoundingDefinition[unadjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingDefinition.from(signedNotional, settlementDate, unadjustedDateSchedule[0], index, stubLeg, businessDayConvention, endOfMonth, calendar);
    for (int loopcpn = 1; loopcpn < unadjustedDateSchedule.length; loopcpn++) {
      coupons[loopcpn] = CouponIborCompoundingDefinition.from(signedNotional, unadjustedDateSchedule[loopcpn - 1], unadjustedDateSchedule[loopcpn], index, stubLeg,
          businessDayConvention, endOfMonth, calendar);
    }
    return new AnnuityDefinition<CouponIborCompoundingDefinition>(coupons, calendar);
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
  public static AnnuityDefinition<CouponIborCompoundingSpreadDefinition> annuityIborCompoundingSpreadFrom(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final double notional, final double spread, final IborIndex index, final StubType stubCompound, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final StubType stubLeg) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final boolean isStubLegShort = stubLeg.equals(StubType.SHORT_END) || stubLeg.equals(StubType.SHORT_START);
    final boolean isStubLegStart = stubLeg.equals(StubType.LONG_START) || stubLeg.equals(StubType.SHORT_START); // Implementation note: dates computed from the end.
    final ZonedDateTime[] unadjustedDateSchedule = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, isStubLegShort, isStubLegStart);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponIborCompoundingSpreadDefinition[] coupons = new CouponIborCompoundingSpreadDefinition[unadjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingSpreadDefinition.from(signedNotional, settlementDate, unadjustedDateSchedule[0], index, spread, stubLeg, businessDayConvention, endOfMonth, calendar);
    for (int loopcpn = 1; loopcpn < unadjustedDateSchedule.length; loopcpn++) {
      coupons[loopcpn] = CouponIborCompoundingSpreadDefinition.from(signedNotional, unadjustedDateSchedule[loopcpn - 1], unadjustedDateSchedule[loopcpn], index, spread, stubLeg,
          businessDayConvention, endOfMonth, calendar);
    }
    return new AnnuityDefinition<CouponIborCompoundingSpreadDefinition>(coupons, calendar);
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
  public static AnnuityDefinition<CouponIborCompoundingFlatSpreadDefinition> annuityIborCompoundingFlatSpreadFrom(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final double notional, final double spread, final IborIndex index, final StubType stubCompound, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final StubType stubLeg) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final boolean isStubLegShort = stubLeg.equals(StubType.SHORT_END) || stubLeg.equals(StubType.SHORT_START);
    final boolean isStubLegStart = stubLeg.equals(StubType.LONG_START) || stubLeg.equals(StubType.SHORT_START); // Implementation note: dates computed from the end.
    final ZonedDateTime[] unadjustedDateSchedule = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, isStubLegShort, isStubLegStart);
    final double signedNotional = isPayer ? -notional : notional;
    final CouponIborCompoundingFlatSpreadDefinition[] coupons = new CouponIborCompoundingFlatSpreadDefinition[unadjustedDateSchedule.length];
    coupons[0] = CouponIborCompoundingFlatSpreadDefinition.from(signedNotional, settlementDate, unadjustedDateSchedule[0], index, spread, stubLeg, businessDayConvention, endOfMonth, calendar);
    for (int loopcpn = 1; loopcpn < unadjustedDateSchedule.length; loopcpn++) {
      coupons[loopcpn] = CouponIborCompoundingFlatSpreadDefinition.from(signedNotional, unadjustedDateSchedule[loopcpn - 1], unadjustedDateSchedule[loopcpn], index, spread, stubLeg,
          businessDayConvention, endOfMonth, calendar);
    }
    return new AnnuityDefinition<CouponIborCompoundingFlatSpreadDefinition>(coupons, calendar);
  }

}
