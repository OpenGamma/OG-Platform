/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * A wrapper class for a {@link AnnuityDefinition} containing {@link CouponONSpreadSimplifiedDefinition}.
 */
public class AnnuityCouponONSpreadSimplifiedDefinition extends AnnuityDefinition<CouponONSpreadSimplifiedDefinition> {
  /** The overnight reference index */
  private final IndexON _index;

  /**
   * Constructor from a list of overnight coupons.
   * @param payments The coupons.
   * @param index The underlying overnight index.
   * @param calendar The holiday calendar
   */
  public AnnuityCouponONSpreadSimplifiedDefinition(final CouponONSpreadSimplifiedDefinition[] payments, final IndexON index, final Calendar calendar) {
    super(payments, calendar);
    _index = index;
  }

  /**
   * Annuity builder from the financial details.
   * @param settlementDate The settlement date, not null
   * @param tenorAnnuity The annuity tenor, not null
   * @param notional The annuity notional.
   * @param spread The annuity spread. Same spread for all coupons.
   * @param generator The overnight generator, not null
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponONSpreadSimplifiedDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional, final double spread,
      final GeneratorSwapFixedON generator,
      final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenorAnnuity, "tenor annuity");
    ArgumentChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, generator.getLegsPeriod(), generator.getBusinessDayConvention(),
        generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONSpreadSimplifiedDefinition.from(settlementDate, endFixingPeriodDate, notional, spread, isPayer, generator.getIndex(), generator.getPaymentLag(),
        generator.getOvernightCalendar());
  }

  /**
   * Annuity builder from the financial details.
   * @param settlementDate The settlement date, not null
   * @param maturityDate The maturity date. The maturity date is the end date of the last fixing period, not null
   * @param notional The notional.
   * @param spread The annuity spread. Same spread for all coupons.
   * @param generator The generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponONSpreadSimplifiedDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final double spread,
      final GeneratorSwapFixedON generator, final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, generator.getLegsPeriod(), generator.getBusinessDayConvention(),
        generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONSpreadSimplifiedDefinition.from(settlementDate, endFixingPeriodDate, notional, spread, isPayer, generator.getIndex(), generator.getPaymentLag(),
        generator.getOvernightCalendar());
  }

  /**
   * Build a annuity of overnight coupons from financial details. The stub convention is short at the start.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param spread The annuity spread. Same spread for all coupons.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param paymentPeriod The payment period.
   * @param indexON The overnight index.
   * @param paymentLag The payment lag.
   * @param businessDayConvention The business day convention.
   * @param isEOM Is EOM.
   * @param indexCalendar The calendar for the overnight index.
   * @return The annuity.
   */
  public static AnnuityCouponONSpreadSimplifiedDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final double spread,
      final boolean isPayer, final Period paymentPeriod, final IndexON indexON, final int paymentLag, final BusinessDayConvention businessDayConvention, final boolean isEOM,
      final Calendar indexCalendar) {
    return from(settlementDate, endFixingPeriodDate, notional, spread, isPayer, paymentPeriod, indexON, paymentLag, businessDayConvention, isEOM, indexCalendar, StubType.SHORT_START);
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param spread The annuity spread. Same spread for all coupons.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param paymentPeriod The payment period.
   * @param indexON The overnight index.
   * @param paymentLag The payment lag.
   * @param businessDayConvention The business day convention.
   * @param isEOM Is EOM.
   * @param indexCalendar The calendar for the overnight index.
   * @param stub The stub type.
   * @return The annuity.
   */
  public static AnnuityCouponONSpreadSimplifiedDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final double spread,
      final boolean isPayer, final Period paymentPeriod, final IndexON indexON, final int paymentLag, final BusinessDayConvention businessDayConvention, final boolean isEOM,
      final Calendar indexCalendar, final StubType stub) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgumentChecker.notNull(indexON, "overnight index");
    ArgumentChecker.notNull(indexCalendar, "index calendar");
    ArgumentChecker.notNull(businessDayConvention, "business day convention");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    final boolean isStubShort = stub.equals(StubType.SHORT_END) || stub.equals(StubType.SHORT_START);
    final boolean isStubStart = stub.equals(StubType.LONG_START) || stub.equals(StubType.SHORT_START); // Implementation note: dates computed from the end.
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, paymentPeriod, isStubShort,
        isStubStart, businessDayConvention, indexCalendar, isEOM);
    return AnnuityCouponONSpreadSimplifiedDefinition.from(settlementDate, endFixingPeriodDates, notional, spread, isPayer, indexON, paymentLag, indexCalendar);
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param tenorAnnuity The annuity tenor, not null
   * @param notional The annuity notional.
   * @param spread The annuity spread. Same spread for all coupons.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param indexON The overnight index.
   * @param paymentLag The payment lag.
   * @param indexCalendar The calendar for the overnight index.
   * @param businessDayConvention The business day convention.
   * @param paymentPeriod The payment period.
   * @param isEOM Is EOM.
   * @return The annuity.
   */
  public static AnnuityCouponONSpreadSimplifiedDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional, final double spread, final boolean isPayer,
      final IndexON indexON, final int paymentLag, final Calendar indexCalendar, final BusinessDayConvention businessDayConvention, final Period paymentPeriod, final boolean isEOM) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenorAnnuity, "tenor annuity");
    ArgumentChecker.notNull(indexON, "overnight index");
    ArgumentChecker.notNull(indexCalendar, "index calendar");
    ArgumentChecker.notNull(businessDayConvention, "business day convention");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, paymentPeriod, true,
        false, businessDayConvention, indexCalendar, isEOM); //TODO get rid of hard-codings
    return AnnuityCouponONSpreadSimplifiedDefinition.from(settlementDate, endFixingPeriodDates, notional, spread, isPayer, indexON, paymentLag, indexCalendar);
  }

  /**
   * Creates an annuity of overnight coupons
   * @param settlementDate The annuity settlement date
   * @param endFixingPeriodDate The fixing period end date
   * @param notional The notional
   * @param spread The spread
   * @param isPayer True if the annuity is paid
   * @param indexON The overnight reference index
   * @param paymentLag The payment lag
   * @param indexCalendar The index calendar
   * @return An overnight annuity
   */
  private static AnnuityCouponONSpreadSimplifiedDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime[] endFixingPeriodDate, final double notional, final double spread,
      final boolean isPayer, final IndexON indexON, final int paymentLag, final Calendar indexCalendar) {
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponONSpreadSimplifiedDefinition[] coupons = new CouponONSpreadSimplifiedDefinition[endFixingPeriodDate.length];
    coupons[0] = CouponONSpreadSimplifiedDefinition.from(indexON, settlementDate, endFixingPeriodDate[0], notionalSigned, spread, paymentLag, indexCalendar);
    for (int loopcpn = 1; loopcpn < endFixingPeriodDate.length; loopcpn++) {
      coupons[loopcpn] = CouponONSpreadSimplifiedDefinition.from(indexON, endFixingPeriodDate[loopcpn - 1], endFixingPeriodDate[loopcpn], notionalSigned, spread, paymentLag,
          indexCalendar);
    }
    return new AnnuityCouponONSpreadSimplifiedDefinition(coupons, indexON, indexCalendar);
  }

  /**
   * Gets the overnight reference index.
   * @return The overnight reference index
   */
  public IndexON getIndex() {
    return _index;
  }
}
