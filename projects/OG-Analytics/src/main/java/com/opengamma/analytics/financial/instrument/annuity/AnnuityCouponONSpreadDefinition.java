/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborON;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * A wrapper class for an annuity containing overnight coupons with a spread.
 */
public class AnnuityCouponONSpreadDefinition extends AnnuityCouponDefinition<CouponONSpreadDefinition> {

  /**
   * Constructor from a list of overnight coupons.
   * @param payments The coupons.
   * @param calendar The holiday calendar
   */
  public AnnuityCouponONSpreadDefinition(final CouponONSpreadDefinition[] payments, final Calendar calendar) {
    super(payments, calendar);
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param tenorAnnuity The total tenor of the annuity, not null.
   * @param notional The annuity notional.
   * @param generator The overnight generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param spread The spread
   * @return The annuity.
   */
  public static AnnuityCouponONSpreadDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional,
      final GeneratorSwapFixedON generator, final boolean isPayer, final double spread) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenorAnnuity, "tenor annuity");
    ArgumentChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, generator.getLegsPeriod(), generator.isStubShort(), generator.isFromEnd(),
        generator.getBusinessDayConvention(), generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONSpreadDefinition.from(settlementDate, endFixingPeriodDate, notional, generator, isPayer, spread);
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param tenorAnnuity The total tenor of the annuity, not null.
   * @param notional The annuity notional.
   * @param generator The Ibor/ON generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param spread The spread
   * @return The annuity.
   */
  public static AnnuityCouponONSpreadDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional,
      final GeneratorSwapIborON generator, final boolean isPayer, final double spread) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenorAnnuity, "tenor annuity");
    ArgumentChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, generator.getIndexIbor().getTenor(), generator.isStubShort(),
        generator.isFromEnd(), generator.getBusinessDayConvention(), generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONSpreadDefinition.from(settlementDate, endFixingPeriodDate, notional, generator, isPayer, spread);
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param generator The overnight generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param spread The spread
   * @return The annuity.
   */
  public static AnnuityCouponONSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final GeneratorSwapFixedON generator,
      final boolean isPayer, final double spread) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgumentChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, generator.getLegsPeriod(), generator.isStubShort(),
        generator.isFromEnd(), generator.getBusinessDayConvention(), generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONSpreadDefinition.from(settlementDate, endFixingPeriodDates, notional, generator, isPayer, spread);
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param indexON The overnight index.
   * @param paymentLag The payment lag.
   * @param indexCalendar The calendar for the overnight index.
   * @param businessDayConvention The business day convention.
   * @param paymentPeriod The payment period.
   * @param isEOM Is EOM.
   * @param spread The spread
   * @return The annuity.
   */
  public static AnnuityCouponONSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final boolean isPayer,
      final IndexON indexON, final int paymentLag, final Calendar indexCalendar, final BusinessDayConvention businessDayConvention, final Period paymentPeriod, final boolean isEOM,
      final double spread) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgumentChecker.notNull(indexON, "overnight index");
    ArgumentChecker.notNull(indexCalendar, "index calendar");
    ArgumentChecker.notNull(businessDayConvention, "business day convention");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, paymentPeriod, true,
        false, businessDayConvention, indexCalendar, isEOM); //TODO get rid of hard-codings
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponONSpreadDefinition[] coupons = new CouponONSpreadDefinition[endFixingPeriodDates.length];
    coupons[0] = CouponONSpreadDefinition.from(indexON, settlementDate, endFixingPeriodDates[0], notionalSigned, paymentLag, indexCalendar, spread);
    for (int loopcpn = 1; loopcpn < endFixingPeriodDates.length; loopcpn++) {
      coupons[loopcpn] = CouponONSpreadDefinition.from(indexON, endFixingPeriodDates[loopcpn - 1], endFixingPeriodDates[loopcpn], notionalSigned, paymentLag,
          indexCalendar, spread);
    }
    return new AnnuityCouponONSpreadDefinition(coupons, indexCalendar);
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param generator The Ibor/ON generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param spread The spread
   * @return The annuity.
   */
  public static AnnuityCouponONSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final GeneratorSwapIborON generator,
      final boolean isPayer, final double spread) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgumentChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, generator.getIndexIbor().getTenor(), generator.isStubShort(),
        generator.isFromEnd(), generator.getBusinessDayConvention(), generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONSpreadDefinition.from(settlementDate, endFixingPeriodDates, notional, generator, isPayer, spread);
  }

  /**
   * Creates an overnight annuity with spread.
   * @param settlementDate The setttlement date
   * @param endFixingPeriodDate The end fixing period dates
   * @param notional The notional
   * @param generator A fixed / overnight swap generator
   * @param isPayer True if the annuity is paid
   * @param spread The spread
   * @return An overnight annuity with spread
   */
  private static AnnuityCouponONSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime[] endFixingPeriodDate, final double notional, final GeneratorSwapFixedON generator,
      final boolean isPayer, final double spread) {
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponONSpreadDefinition[] coupons = new CouponONSpreadDefinition[endFixingPeriodDate.length];
    coupons[0] = CouponONSpreadDefinition.from(generator.getIndex(), settlementDate, endFixingPeriodDate[0], notionalSigned, generator.getPaymentLag(), generator.getOvernightCalendar(),
        spread);
    for (int loopcpn = 1; loopcpn < endFixingPeriodDate.length; loopcpn++) {
      coupons[loopcpn] = CouponONSpreadDefinition.from(generator.getIndex(), endFixingPeriodDate[loopcpn - 1], endFixingPeriodDate[loopcpn], notionalSigned, generator.getPaymentLag(),
          generator.getOvernightCalendar(), spread);
    }
    return new AnnuityCouponONSpreadDefinition(coupons, generator.getOvernightCalendar());
  }

  /**
   * Creates an overnight annuity with spread.
   * @param settlementDate The setttlement date
   * @param endFixingPeriodDate The end fixing period dates
   * @param notional The notional
   * @param generator A ibor / overnight swap generator
   * @param isPayer True if the annuity is paid
   * @param spread The spread
   * @return An overnight annuity with spread
   */
  private static AnnuityCouponONSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime[] endFixingPeriodDate, final double notional, final GeneratorSwapIborON generator,
      final boolean isPayer, final double spread) {
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponONSpreadDefinition[] coupons = new CouponONSpreadDefinition[endFixingPeriodDate.length];
    coupons[0] = CouponONSpreadDefinition.from(generator.getIndexON(), settlementDate, endFixingPeriodDate[0], notionalSigned, generator.getPaymentLag(), generator.getOvernightCalendar(), spread);
    for (int loopcpn = 1; loopcpn < endFixingPeriodDate.length; loopcpn++) {
      coupons[loopcpn] = CouponONSpreadDefinition.from(generator.getIndexON(), endFixingPeriodDate[loopcpn - 1], endFixingPeriodDate[loopcpn], notionalSigned, generator.getPaymentLag(),
          generator.getOvernightCalendar(), spread);
    }
    return new AnnuityCouponONSpreadDefinition(coupons, generator.getOvernightCalendar());
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Annuity<? extends Coupon> toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Annuity<? extends Coupon> toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTS) {
    ArgumentChecker.notNull(valZdt, "date");
    ArgumentChecker.notNull(indexFixingTS, "index fixing time series");
    final List<Coupon> resultList = new ArrayList<>();
    final CouponONSpreadDefinition[] payments = getPayments();
    final ZonedDateTime valZdtInPaymentZone = valZdt.withZoneSameInstant(payments[0].getPaymentDate().getZone());
    final LocalDate valDate = valZdtInPaymentZone.toLocalDate();

    for (int loopcoupon = 0; loopcoupon < payments.length; loopcoupon++) {
      if (!valDate.isAfter(payments[loopcoupon].getPaymentDate().toLocalDate())) {
        resultList.add(payments[loopcoupon].toDerivative(valZdt, indexFixingTS));
      }
    }
    return new Annuity<>(resultList.toArray(new Coupon[resultList.size()]));
  }
}
