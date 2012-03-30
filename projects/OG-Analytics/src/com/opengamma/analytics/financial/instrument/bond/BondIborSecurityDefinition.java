/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.payments.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Describes a floating coupon bond (or Floating Rate Note) issue with Ibor-like coupon.
 */
public class BondIborSecurityDefinition extends BondSecurityDefinition<PaymentFixedDefinition, CouponIborDefinition> implements
    InstrumentDefinitionWithData<BondSecurity<? extends Payment, ? extends Coupon>, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The default notional for the security.
   */
  private static final double DEFAULT_NOTIONAL = 1.0;
  /**
   * The default ex-coupn number of days.
   */
  private static final int DEFAULT_EX_COUPON_DAYS = 0;
  /**
   * The coupon day count convention.
   */
  private final DayCount _dayCount;

  /**
   * Fixed coupon bond constructor from all the bond details.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond Ibor coupons. The coupons notional and currency should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param dayCount The coupon day count convention.
   */
  public BondIborSecurityDefinition(final AnnuityPaymentFixedDefinition nominal, final AnnuityCouponIborDefinition coupon, final int exCouponDays, final int settlementDays, final Calendar calendar,
      final DayCount dayCount) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar);
    _dayCount = dayCount;
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention.
   * @param maturityDate The maturity date.
   * @param firstAccrualDate The first accrual date (bond start date).
   * @param index The coupon Ibor index.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param dayCount The coupon day count convention.
   * @param businessDay The business day convention for the payments.
   * @param isEOM The end-of-month flag.
   * @return The fixed coupon bond.
   */
  public static BondIborSecurityDefinition from(final ZonedDateTime maturityDate, final ZonedDateTime firstAccrualDate, final IborIndex index, final int settlementDays, final DayCount dayCount,
      final BusinessDayConvention businessDay,
      final boolean isEOM) {
    Validate.notNull(maturityDate, "Maturity date");
    Validate.notNull(firstAccrualDate, "First accrual date");
    Validate.notNull(index, "Ibor index");
    Validate.notNull(dayCount, "Day count");
    Validate.notNull(businessDay, "Business day convention");
    final AnnuityCouponIborDefinition coupon = AnnuityCouponIborDefinition.fromAccrualUnadjusted(firstAccrualDate, maturityDate, DEFAULT_NOTIONAL, index, false);
    final PaymentFixedDefinition[] nominalPayment = new PaymentFixedDefinition[] {new PaymentFixedDefinition(index.getCurrency(), businessDay.adjustDate(index.getCalendar(), maturityDate),
        DEFAULT_NOTIONAL) };
    final AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(nominalPayment);
    return new BondIborSecurityDefinition(nominal, coupon, DEFAULT_EX_COUPON_DAYS, settlementDays, index.getCalendar(), dayCount);
  }

  /**
   * Gets the coupon day count.
   * @return The day count.
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  @Override
  public BondIborSecurity toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getSettlementDays(), getCalendar());
    return toDerivative(date, new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(1800, 1, 1) }, new double[] {0.0 }), spot, yieldCurveNames);

  }

  @Override
  public BondIborSecurity toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getSettlementDays(), getCalendar());
    return toDerivative(date, indexFixingTS, spot, yieldCurveNames);
  }

  public BondIborSecurity toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final ZonedDateTime settlementDate, final String... yieldCurveNames) {
    // Implementation note: First yield curve used for coupon and notional (credit), the second for risk free settlement.
    Validate.notNull(date, "date");
    Validate.notNull(indexFixingTS, "fixing time series");
    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    final String creditCurveName = yieldCurveNames[0];
    final String riskFreeCurveName = yieldCurveNames[1];
    double settlementTime;
    if (settlementDate.isBefore(date)) {
      settlementTime = 0.0;
    } else {
      settlementTime = TimeCalculator.getTimeBetween(date, settlementDate);
    }
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) getNominal().toDerivative(date, creditCurveName);
    final GenericAnnuity<Coupon> coupon = (GenericAnnuity<Coupon>) getCoupon().toDerivative(date, indexFixingTS, yieldCurveNames);
    return new BondIborSecurity(nominal.trimBefore(settlementTime), coupon.trimBefore(settlementTime), settlementTime, riskFreeCurveName);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitBondIborSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondIborSecurityDefinition(this);
  }

}
