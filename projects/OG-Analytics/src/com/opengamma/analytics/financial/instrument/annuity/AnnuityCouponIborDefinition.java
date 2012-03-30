/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.analytics.financial.interestrate.payments.Coupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * A wrapper class for a AnnuityDefinition containing CouponIborDefinition.
 */
public class AnnuityCouponIborDefinition extends AnnuityCouponDefinition<CouponIborDefinition> {
  /** Empty array for array conversion of list */
  protected static final Coupon[] EMPTY_ARRAY_COUPON = new Coupon[0];
  private final IborIndex _iborIndex;

  /**
   * Constructor from a list of Ibor-like coupons.
   * @param payments The Ibor coupons.
   * @param iborIndex The underlying ibor index
   */
  public AnnuityCouponIborDefinition(final CouponIborDefinition[] payments, final IborIndex iborIndex) {
    super(payments);
    ArgumentChecker.notNull(iborIndex, "ibor index");
    _iborIndex = iborIndex;
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
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(tenor, "tenor");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime maturityDate = settlementDate.plus(tenor); // Maturity is unadjusted.
    return from(settlementDate, maturityDate, notional, index, isPayer);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The business day convention and the EOM rule are the one of the index.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index, final boolean isPayer) {
    ArgumentChecker.notNull(index, "index");
    return from(settlementDate, maturityDate, notional, index, isPayer, index.getBusinessDayConvention(), index.isEndOfMonth());
  }

  /**
   * Annuity builder from the conventions and common characteristics. The payment period and the day-count convention are the one of the Ibor index.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth) {
    ArgumentChecker.notNull(index, "index");
    return from(settlementDate, maturityDate, index.getTenor(), notional, index, isPayer, businessDayConvention, endOfMonth, index.getDayCount());
  }

  /**
   * Annuity builder from the conventions and common characteristics. 
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param paymentPeriod The payment period.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param dayCount The coupons day count.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod, final double notional, final IborIndex index,
      final boolean isPayer, final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final DayCount dayCount) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(dayCount, "Day count convention");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, true, false, businessDayConvention, index.getCalendar(), endOfMonth);
    final CouponIborDefinition[] coupons = new CouponIborDefinition[paymentDates.length];
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), index.getCalendar());
    coupons[0] = new CouponIborDefinition(index.getCurrency(), paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0]), sign * notional,
        fixingDate, index);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -index.getSpotLag(), index.getCalendar());
      coupons[loopcpn] = new CouponIborDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn], dayCount.getDayCountFraction(paymentDates[loopcpn - 1],
          paymentDates[loopcpn]), sign * notional, fixingDate, index);
    }
    return new AnnuityCouponIborDefinition(coupons, index);
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
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, index.getTenor(), true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, index.getBusinessDayConvention(), index.getCalendar(), false);
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponIborDefinition[] coupons = new CouponIborDefinition[paymentDates.length];
    //First coupon uses settlement date
    CouponFixedDefinition coupon = new CouponFixedDefinition(index.getCurrency(), paymentDates[0], settlementDate, paymentDatesUnadjusted[0], index.getDayCount().getDayCountFraction(settlementDate,
        paymentDatesUnadjusted[0]), sign * notional, 0.0);
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), index.getCalendar());
    coupons[0] = CouponIborDefinition.from(coupon, fixingDate, index);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupon = new CouponFixedDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], index.getDayCount().getDayCountFraction(
          paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn]), sign * notional, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDatesUnadjusted[loopcpn - 1], -index.getSpotLag(), index.getCalendar());
      coupons[loopcpn] = CouponIborDefinition.from(coupon, fixingDate, index);
    }
    return new AnnuityCouponIborDefinition(coupons, index);
  }

  /**
   * Builder from an Ibor annuity with spread. Ignores the spread.
   * @param annuity The Ibor annuity with spread. 
   * @return The annuity.
   */
  public static AnnuityCouponIborDefinition from(final AnnuityCouponIborSpreadDefinition annuity) {
    ArgumentChecker.notNull(annuity, "annuity");
    final CouponIborDefinition[] coupons = new CouponIborDefinition[annuity.getPayments().length];
    for (int loopcpn = 0; loopcpn < annuity.getPayments().length; loopcpn++) {
      coupons[loopcpn] = CouponIborDefinition.from(annuity.getNthPayment(loopcpn));
    }
    return new AnnuityCouponIborDefinition(coupons, annuity.getIborIndex());
  }

  /**
   * Creates a new annuity containing the coupons with start accrual date strictly before the given date.
   * @param trimDate The date.
   * @return The trimmed annuity.
   */
  public AnnuityCouponIborDefinition trimStart(final ZonedDateTime trimDate) {
    final List<CouponIborDefinition> list = new ArrayList<CouponIborDefinition>();
    for (final CouponIborDefinition payment : getPayments()) {
      if (!payment.getAccrualStartDate().isBefore(trimDate)) {
        list.add(payment);
      }
    }
    return new AnnuityCouponIborDefinition(list.toArray(new CouponIborDefinition[0]), _iborIndex);
  }

  /**
   * Returns the underlying ibor index
   * @return The underlying ibor index
   */
  public IborIndex getIborIndex() {
    return _iborIndex;
  }

  @Override
  public GenericAnnuity<? extends Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
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
    ArgumentChecker.notNull(date, "date");
    final List<Coupon> resultList = new ArrayList<Coupon>();
    for (int loopcoupon = 0; loopcoupon < getPayments().length; loopcoupon++) {
      if (!date.isAfter(getPayments()[loopcoupon].getPaymentDate())) {
        resultList.add(getPayments()[loopcoupon].toDerivative(date, yieldCurveNames));
      }
    }
    return new GenericAnnuity<Coupon>(resultList.toArray(EMPTY_ARRAY_COUPON));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _iborIndex.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AnnuityCouponIborDefinition other = (AnnuityCouponIborDefinition) obj;
    if (!ObjectUtils.equals(_iborIndex, other._iborIndex)) {
      return false;
    }
    return true;
  }

}
