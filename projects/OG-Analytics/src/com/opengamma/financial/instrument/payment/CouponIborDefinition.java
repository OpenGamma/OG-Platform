/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Ibor-like floating coupon.
 */
public class CouponIborDefinition extends CouponFloatingDefinition {

  /**
   * Ibor-like index on which the coupon fixes. The index currency should be the same as the index currency.
   */
  private final IborIndex _index;
  /**
   * The start date of the fixing period.
   */
  private final ZonedDateTime _fixindPeriodStartDate;
  /**
   * The end date of the fixing period.
   */
  private final ZonedDateTime _fixindPeriodEndDate;
  /**
   * The year fraction associated to the fixing period in the Index day count convention.
   */
  private final double _fixingPeriodAccrualFactor;

  /**
   * Constructor of a Ibor-like floating coupon from the coupon details and the Ibor index. The payment currency is the index currency.
   * 
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   */
  public CouponIborDefinition(Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final ZonedDateTime fixingDate, final IborIndex index) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    Validate.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    Validate.notNull(index, "index");
    _index = index;
    _fixindPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, _index.getBusinessDayConvention(), _index.getCalendar(), _index.getSettlementDays());
    _fixindPeriodEndDate = ScheduleCalculator.getAdjustedDate(_fixindPeriodStartDate, index.getBusinessDayConvention(), index.getCalendar(), index.isEndOfMonth(), index.getTenor());
    _fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(_fixindPeriodStartDate, _fixindPeriodEndDate);
  }

  /**
   * Constructor of a Ibor-like floating coupon from the coupon details and the Ibor index. The payment currency is the index currency.
   * 
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index.
   * @return The Ibor coupon.
   */
  public static CouponIborDefinition from(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor, final double notional,
      final ZonedDateTime fixingDate, final IborIndex index) {
    Validate.notNull(index, "index");
    return new CouponIborDefinition(index.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, index);
  }

  /**
   * Builder of Ibor-like coupon from the fixing date and the index. The payment and accrual dates are the one of the fixing period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index.
   * @return The Ibor coupon.
   */
  public static CouponIborDefinition from(final double notional, final ZonedDateTime fixingDate, final IborIndex index) {
    Validate.notNull(fixingDate, "fixing date");
    Validate.notNull(index, "index");
    final ZonedDateTime fixindPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, index.getBusinessDayConvention(), index.getCalendar(), index.getSettlementDays());
    final ZonedDateTime fixindPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixindPeriodStartDate, index.getBusinessDayConvention(), index.getCalendar(), index.isEndOfMonth(), index.getTenor());
    final double fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(fixindPeriodStartDate, fixindPeriodEndDate);
    return new CouponIborDefinition(index.getCurrency(), fixindPeriodEndDate, fixindPeriodStartDate, fixindPeriodEndDate, fixingPeriodAccrualFactor, notional, fixingDate, index);
  }

  /**
   * Builder of Ibor-like coupon from an underlying coupon, the fixing date and the index. The fixing period dates are deduced from the index and the fixing date.
   * @param coupon Underlying coupon.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index.
   * @return The Ibor coupon.
   */
  public static CouponIborDefinition from(CouponDefinition coupon, ZonedDateTime fixingDate, IborIndex index) {
    Validate.notNull(coupon, "coupon");
    Validate.notNull(fixingDate, "fixing date");
    Validate.notNull(index, "index");
    return new CouponIborDefinition(index.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional(),
        fixingDate, index);
  }

  /**
   * Builder from an Ibor coupon with spread. The spread is ignored.
   * @param coupon The coupon with spread.
   * @return The ibor coupon.
   */
  public static CouponIborDefinition from(CouponIborSpreadDefinition coupon) {
    Validate.notNull(coupon, "coupon");
    return new CouponIborDefinition(coupon.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional(),
        coupon.getFixingDate(), coupon.getIndex());

  }

  /**
   * Gets the index field.
   * @return the index
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the fixindPeriodStartDate field.
   * @return the fixindPeriodStartDate
   */
  public ZonedDateTime getFixindPeriodStartDate() {
    return _fixindPeriodStartDate;
  }

  /**
   * Gets the fixindPeriodEndDate field.
   * @return the fixindPeriodEndDate
   */
  public ZonedDateTime getFixindPeriodEndDate() {
    return _fixindPeriodEndDate;
  }

  /**
   * Gets the fixingPeriodAccrualFactor field.
   * @return the fixingPeriodAccrualFactor
   */
  public double getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  @Override
  public String toString() {
    return super.toString() + " *Ibor coupon* Index = " + _index + ", Fixing period = [" + _fixindPeriodStartDate + " - " + _fixindPeriodEndDate + " - " + _fixingPeriodAccrualFactor + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _fixindPeriodEndDate.hashCode();
    result = prime * result + _fixindPeriodStartDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
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
    final CouponIborDefinition other = (CouponIborDefinition) obj;
    if (!ObjectUtils.equals(_fixindPeriodEndDate, other._fixindPeriodEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixindPeriodStartDate, other._fixindPeriodStartDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodAccrualFactor) != Double.doubleToLongBits(other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    return true;
  }

  @Override
  public Payment toDerivative(final LocalDate date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least one curve required");
    Validate.isTrue(!date.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final String fundingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, getPaymentDate());
    if (isFixed()) { // The Ibor coupon has already fixed, it is now a fixed coupon.
      return new CouponFixed(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), getFixedRate());
    }
    // Ibor is not fixed yet, all the details are required.
    final double fixingTime = actAct.getDayCountFraction(zonedDate, getFixingDate());
    final double fixingPeriodStartTime = actAct.getDayCountFraction(zonedDate, getFixindPeriodStartDate());
    final double fixingPeriodEndTime = actAct.getDayCountFraction(zonedDate, getFixindPeriodEndDate());
    //TODO: Definition has no spread and time version has one: to be standardized.
    return new CouponIbor(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), forwardCurveName);
  }

  @Override
  public <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitCouponIbor(this, data);
  }

  @Override
  public <V> V accept(final FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitCouponIbor(this);
  }

}
