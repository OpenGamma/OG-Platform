/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a Ibor-like floating coupon with a gearing (multiplicative) factor and a spread. The coupon payment is: notional * accrual factor * (factor * Ibor + spread).
 */
public class CouponIborGearingDefinition extends CouponIborDefinition {

  /**
   * The spread paid above the Ibor rate.
   */
  private final double _spread;
  /**
   * The fixed amount related to the spread.
   */
  private final double _spreadAmount;
  /**
   * The gearing (multiplicative) factor applied to the Ibor rate.
   */
  private final double _factor;

  /**
   * Constructor of a Ibor-like floating coupon from the coupon details and the Ibor index.
   * 
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param spread The spread paid above the Ibor rate.
   * @param factor The gearing (multiplicative) factor applied to the Ibor rate.
   */
  public CouponIborGearingDefinition(Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final ZonedDateTime fixingDate, final IborIndex index, double spread, double factor) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, index);
    _spread = spread;
    _spreadAmount = spread * getNotional() * getPaymentYearFraction();
    _factor = factor;
  }

  /**
   * Builder from an Ibor coupon, the spread and the factor.
   * @param couponIbor An Ibor coupon.
   * @param spread The spread.
   * @param factor The gearing (multiplicative) factor applied to the Ibor rate.
   * @return The Ibor coupon with spread.
   */
  public static CouponIborGearingDefinition from(CouponIborDefinition couponIbor, double spread, double factor) {
    Validate.notNull(couponIbor, "Ibor coupon");
    return new CouponIborGearingDefinition(couponIbor.getCurrency(), couponIbor.getPaymentDate(), couponIbor.getAccrualStartDate(), couponIbor.getAccrualEndDate(),
        couponIbor.getPaymentYearFraction(), couponIbor.getNotional(), couponIbor.getFixingDate(), couponIbor.getIndex(), spread, factor);
  }

  /**
   * Builder from the coupon accrual dates and accrual factor. The fixing period will use the index convention, starting on the accrual start date.
   * @param accrualStartDate The accrual start date.
   * @param accrualEndDate The accrual end date.
   * @param accrualFactor The payment accrual factor.
   * @param notional The coupon notional.
   * @param index The Ibor index.
   * @param spread The spread.
   * @param factor The gearing factor.
   * @return The coupon.
   */
  public static CouponIborGearingDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor, final double notional, final IborIndex index,
      double spread, double factor) {
    Validate.notNull(accrualStartDate, "Fixing date");
    Validate.notNull(accrualEndDate, "Fixing date");
    Validate.notNull(index, "Index");
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, index.getCalendar(), -index.getSettlementDays());
    return new CouponIborGearingDefinition(index.getCurrency(), accrualEndDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, index, spread, factor);
  }

  /**
   * Gets the spread.
   * @return The spread
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Gets the fixed amount related to the spread.
   * @return The spread amount.
   */
  public double getSpreadAmount() {
    return _spreadAmount;
  }

  /**
   * Gets the gearing (multiplicative) factor applied to the Ibor rate.
   * @return The factor.
   */
  public double getFactor() {
    return _factor;
  }

  @Override
  public Payment toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.isTrue(!date.isAfter(getFixingDate()), "Do not have any fixing data but are asking for a derivative after the fixing date " + getFixingDate() + " " + date);
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final String fundingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final double paymentTime = actAct.getDayCountFraction(date, getPaymentDate());
    final double fixingTime = actAct.getDayCountFraction(date, getFixingDate());
    final double fixingPeriodStartTime = actAct.getDayCountFraction(date, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = actAct.getDayCountFraction(date, getFixingPeriodEndDate());
    return new CouponIborGearing(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixingTime, getIndex(), fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), _spread, _factor, forwardCurveName);
  }

  @Override
  public Payment toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(indexFixingTimeSeries, "Index fixing time series");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final String fundingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final double paymentTime = actAct.getDayCountFraction(date, getPaymentDate());
    final Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate());
    if (date.isAfter(getFixingDate()) || (date.equals(getFixingDate()) && (fixedRate != null))) {
      return new CouponFixed(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), _factor * fixedRate + _spread);
    }
    final double fixingTime = actAct.getDayCountFraction(date, getFixingDate());
    final double fixingPeriodStartTime = actAct.getDayCountFraction(date, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = actAct.getDayCountFraction(date, getFixingPeriodEndDate());
    return new CouponIborGearing(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixingTime, getIndex(), fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), _spread, _factor, forwardCurveName);
  }

  @Override
  public String toString() {
    return super.toString() + ", factor=" + _factor + ", spread=" + _spread + ", spread amount=" + _spreadAmount;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_factor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spreadAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CouponIborGearingDefinition other = (CouponIborGearingDefinition) obj;
    if (Double.doubleToLongBits(_factor) != Double.doubleToLongBits(other._factor)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    if (Double.doubleToLongBits(_spreadAmount) != Double.doubleToLongBits(other._spreadAmount)) {
      return false;
    }
    return true;
  }

}
