/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * A wrapper class for a AnnuityDefinition containing CouponIborDefinition.
 */
public class AnnuityCouponIborDefinition extends AnnuityCouponDefinition<CouponIborDefinition> {
  /** Empty array for array conversion of list */
  protected static final Coupon[] EMPTY_ARRAY_COUPON = new Coupon[0];

  /**
   * Constructor from a list of Ibor-like coupons.
   * @param payments The Ibor coupons.
   */
  public AnnuityCouponIborDefinition(final CouponIborDefinition[] payments) {
    super(payments);
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param settlementDate The settlement date.
   * @param tenor The tenor.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition from(final ZonedDateTime settlementDate, final Period tenor, final double notional, final IborIndex index, final boolean isPayer) {
    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(index, "index");
    Validate.notNull(tenor, "tenor");
    Validate.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime maturityDate = settlementDate.plus(tenor); // Maturity is unadjusted.
    // ScheduleCalculator.getAdjustedDate(settlementDate, tenor, index.getBusinessDayConvention(), index.getCalendar(), index.isEndOfMonth());
    return from(settlementDate, maturityDate, notional, index, isPayer);
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index, final boolean isPayer) {
    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(maturityDate, "maturity date");
    Validate.notNull(index, "index");
    Validate.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, index.getTenor(), true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, index.getBusinessDayConvention(), index.getCalendar(), false);
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponIborDefinition[] coupons = new CouponIborDefinition[paymentDates.length];
    //First coupon uses settlement date
    CouponFixedDefinition coupon = new CouponFixedDefinition(index.getCurrency(), paymentDates[0], settlementDate, paymentDates[0], index.getDayCount().getDayCountFraction(settlementDate,
        paymentDates[0]), sign * notional, 0.0);
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), index.getCalendar());
    coupons[0] = CouponIborDefinition.from(coupon, fixingDate, index);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupon = new CouponFixedDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn], index.getDayCount().getDayCountFraction(
          paymentDates[loopcpn - 1], paymentDates[loopcpn]), sign * notional, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -index.getSpotLag(), index.getCalendar());
      coupons[loopcpn] = CouponIborDefinition.from(coupon, fixingDate, index);
    }
    return new AnnuityCouponIborDefinition(coupons);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The accrual dates are unadjusted. Often used for bonds.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition fromAccrualUnadjusted(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index,
      final boolean isPayer) {
    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(maturityDate, "maturity date");
    Validate.notNull(index, "index");
    Validate.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, index.getTenor(), true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, index.getBusinessDayConvention(), index.getCalendar(), false);
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponIborDefinition[] coupons = new CouponIborDefinition[paymentDates.length];
    //First coupon uses settlement date
    CouponFixedDefinition coupon = new CouponFixedDefinition(index.getCurrency(), paymentDates[0], settlementDate, paymentDatesUnadjusted[0], index.getDayCount().getDayCountFraction(settlementDate,
        paymentDatesUnadjusted[0]), sign * notional, 0.0);
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), index.getCalendar());
    //    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, index.getBusinessDayConvention(), index.getCalendar(), -index.getSpotLag());
    coupons[0] = CouponIborDefinition.from(coupon, fixingDate, index);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupon = new CouponFixedDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], index.getDayCount().getDayCountFraction(
          paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn]), sign * notional, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDatesUnadjusted[loopcpn - 1], -index.getSpotLag(), index.getCalendar());
      //      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDatesUnadjusted[loopcpn - 1], index.getBusinessDayConvention(), index.getCalendar(), -index.getSpotLag());
      coupons[loopcpn] = CouponIborDefinition.from(coupon, fixingDate, index);
    }
    return new AnnuityCouponIborDefinition(coupons);
  }

  /**
   * Builder from an Ibor annuity with spread. Ignores the spread.
   * @param annuity The Ibor annuity zith spread. 
   * @return The annuity.
   */
  public static AnnuityCouponIborDefinition from(final AnnuityCouponIborSpreadDefinition annuity) {
    Validate.notNull(annuity, "annuity");
    final CouponIborDefinition[] coupons = new CouponIborDefinition[annuity.getPayments().length];
    for (int loopcpn = 0; loopcpn < annuity.getPayments().length; loopcpn++) {
      coupons[loopcpn] = CouponIborDefinition.from(annuity.getNthPayment(loopcpn));
    }
    return new AnnuityCouponIborDefinition(coupons);
  }

  /**
   * Creates a new annuity containing the coupons with start accrual date strictly before the given date.
   * @param trimDate The date.
   * @return The trimmed annuity.
   */
  public AnnuityCouponIborDefinition trimStart(ZonedDateTime trimDate) {
    List<CouponIborDefinition> list = new ArrayList<CouponIborDefinition>();
    for (CouponIborDefinition payment : getPayments()) {
      if (!payment.getAccrualStartDate().isBefore(trimDate)) {
        list.add(payment);
      }
    }
    return new AnnuityCouponIborDefinition(list.toArray(new CouponIborDefinition[0]));
  }

  @Override
  public GenericAnnuity<? extends Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    final List<Coupon> resultList = new ArrayList<Coupon>();
    final CouponIborDefinition[] payments = getPayments();
    for (int loopcoupon = 0; loopcoupon < payments.length; loopcoupon++) {
      if (!date.isAfter(payments[loopcoupon].getPaymentDate())) {
        resultList.add(payments[loopcoupon].toDerivative(date, indexFixingTS, yieldCurveNames));
      }
    }
    return new GenericAnnuity<Coupon>(resultList.toArray(EMPTY_ARRAY_COUPON));
  }

  @Override
  public GenericAnnuity<? extends Coupon> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    final List<Coupon> resultList = new ArrayList<Coupon>();
    for (int loopcoupon = 0; loopcoupon < getPayments().length; loopcoupon++) {
      if (!date.isAfter(getPayments()[loopcoupon].getPaymentDate())) {
        resultList.add(getPayments()[loopcoupon].toDerivative(date, yieldCurveNames));
      }
    }
    return new GenericAnnuity<Coupon>(resultList.toArray(EMPTY_ARRAY_COUPON));
  }

}
