/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.schedule.ScheduleCalculator;

/**
 * Class describing a Ibor-like floating coupon.
 */
public class CouponIborDefinition extends CouponFloatingDefinition {

  private final IborIndex _index;
  private final ZonedDateTime _fixindPeriodStartDate;
  private final ZonedDateTime _fixindPeriodEndDate;
  private final double _fixingPeriodAccrualFactor;

  /**
   * Constructor of a Ibor-like floating coupon from the coupon details and the Ibor index.
   * 
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index.
   */
  public CouponIborDefinition(ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double accrualFactor, double notional, ZonedDateTime fixingDate, 
      IborIndex index) {
    super(paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    Validate.notNull(index, "index");
    _index = index;
    _fixindPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, _index.getBusinessDayConvention(), _index.getCalendar(), _index.getSettlementDays());
    _fixindPeriodEndDate = ScheduleCalculator.getAdjustedDate(_fixindPeriodStartDate, index.getBusinessDayConvention(), index.getCalendar(), index.isEndOfMonth(), index.getTenor());
    _fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(_fixindPeriodStartDate, _fixindPeriodEndDate);
  }

  /**
   * Builder of Ibor-like coupon from the fixing date and the index. The payment and accrual dates are the one of the fixing period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index.
   * @return The Ibor coupon.
   */
  public static CouponIborDefinition from(double notional, ZonedDateTime fixingDate, IborIndex index) {
    Validate.notNull(index, "index");
    ZonedDateTime fixindPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, index.getBusinessDayConvention(), index.getCalendar(), index.getSettlementDays());
    ZonedDateTime fixindPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixindPeriodStartDate, index.getBusinessDayConvention(), index.getCalendar(), index.isEndOfMonth(), index.getTenor());
    double fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(fixindPeriodStartDate, fixindPeriodEndDate);
    return new CouponIborDefinition(fixindPeriodEndDate, fixindPeriodStartDate, fixindPeriodEndDate, fixingPeriodAccrualFactor, notional, fixingDate, index);
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
    CouponIborDefinition other = (CouponIborDefinition) obj;
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

}
