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
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AnnuityCouponONCompoundedDefinition extends AnnuityDefinition<CouponONCompoundedDefinition> {

  private final IndexON _index;

  /**
   * Constructor from a list of OIS coupons.
   * @param payments The coupons.
   * @param index The underlying overnight index.
   * @param calendar The holiday calendar
   */
  public AnnuityCouponONCompoundedDefinition(final CouponONCompoundedDefinition[] payments, final IndexON index, final Calendar calendar) {
    super(payments, calendar);
    _index = index;
  }

  /**
   * Annuity builder from the financial details.
   * @param settlementDate The settlement date, not null
   * @param tenorAnnuity The annuity tenor, not null
   * @param notional The annuity notional.
   * @param generator The OIS generator, not null
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponONCompoundedDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional,
      final GeneratorSwapFixedON generator, final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenorAnnuity, "tenor annuity");
    ArgumentChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, generator.getLegsPeriod(), generator.getBusinessDayConvention(),
        generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONCompoundedDefinition.from(settlementDate, endFixingPeriodDate, notional, isPayer, generator.getIndex(), generator.getPaymentLag(), generator.getOvernightCalendar());
  }

  /**
   * Annuity builder from the financial details.
   * @param settlementDate The settlement date, not null
   * @param maturityDate The maturity date. The maturity date is the end date of the last fixing period, not null
   * @param notional The notional.
   * @param generator The generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponONCompoundedDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional,
      final GeneratorSwapFixedON generator, final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, generator.getLegsPeriod(), generator.getBusinessDayConvention(),
        generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONCompoundedDefinition.from(settlementDate, endFixingPeriodDate, notional, isPayer, generator.getIndex(), generator.getPaymentLag(),
        generator.getOvernightCalendar());
  }

  /**
   * Build a annuity of OIS coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the OIS accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param indexON The overnight index.
   * @param paymentLag The payment lag.
   * @param indexCalendar The calendar for the overnight index.
   * @param businessDayConvention The business day convention.
   * @param paymentPeriod The payment period.
   * @param isEOM Is EOM.
   * @return The annuity.
   */
  public static AnnuityCouponONCompoundedDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional,
      final boolean isPayer, final IndexON indexON, final int paymentLag, final Calendar indexCalendar, final BusinessDayConvention businessDayConvention, final Period paymentPeriod,
      final boolean isEOM) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgumentChecker.notNull(indexON, "overnight index");
    ArgumentChecker.notNull(indexCalendar, "index calendar");
    ArgumentChecker.notNull(businessDayConvention, "business day convention");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, paymentPeriod, true,
        false, businessDayConvention, indexCalendar, isEOM); //TODO get rid of hard-codings
    return AnnuityCouponONCompoundedDefinition.from(settlementDate, endFixingPeriodDates, notional, isPayer, indexON, paymentLag, indexCalendar);
  }

  /**
   * Build a annuity of OIS coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param tenorAnnuity The annuity tenor, not null
   * @param notional The annuity notional.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param indexON The overnight index.
   * @param paymentLag The payment lag.
   * @param indexCalendar The calendar for the overnight index.
   * @param businessDayConvention The business day convention.
   * @param paymentPeriod The payment period.
   * @param isEOM Is EOM.
   * @return The annuity.
   */
  public static AnnuityCouponONCompoundedDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional, final boolean isPayer,
      final IndexON indexON, final int paymentLag, final Calendar indexCalendar, final BusinessDayConvention businessDayConvention, final Period paymentPeriod, final boolean isEOM) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenorAnnuity, "tenor annuity");
    ArgumentChecker.notNull(indexON, "overnight index");
    ArgumentChecker.notNull(indexCalendar, "index calendar");
    ArgumentChecker.notNull(businessDayConvention, "business day convention");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, paymentPeriod, true,
        false, businessDayConvention, indexCalendar, isEOM); //TODO get rid of hard-codings
    return AnnuityCouponONCompoundedDefinition.from(settlementDate, endFixingPeriodDates, notional, isPayer, indexON, paymentLag, indexCalendar);
  }

  private static AnnuityCouponONCompoundedDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime[] endFixingPeriodDate, final double notional,
      final boolean isPayer, final IndexON indexON, final int paymentLag, final Calendar indexCalendar) {
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponONCompoundedDefinition[] coupons = new CouponONCompoundedDefinition[endFixingPeriodDate.length];
    coupons[0] = CouponONCompoundedDefinition.from(indexON, settlementDate, endFixingPeriodDate[0], notionalSigned, paymentLag, indexCalendar);
    for (int loopcpn = 1; loopcpn < endFixingPeriodDate.length; loopcpn++) {
      coupons[loopcpn] = CouponONCompoundedDefinition.from(indexON, endFixingPeriodDate[loopcpn - 1], endFixingPeriodDate[loopcpn], notionalSigned, paymentLag,
          indexCalendar);
    }
    return new AnnuityCouponONCompoundedDefinition(coupons, indexON, indexCalendar);
  }

  public IndexON getIndex() {
    return _index;
  }

}
