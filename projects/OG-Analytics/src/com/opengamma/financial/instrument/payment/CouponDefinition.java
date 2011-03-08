/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

/**
 * Class describing a generic coupon.
 */
public class CouponDefinition extends PaymentDefinition {

  // TODO: abstract
  /**
   * The start date of the coupon accrual period.
   */
  private final ZonedDateTime _accrualStartDate;
  /**
   * The end date of the coupon accrual period.
   */
  private final ZonedDateTime _accrualEndDate;
  /**
   * The accrual factor associated to the coupon accrual period.
   */
  private final double _accrualFactor;
  /**
   * The coupon's notional.
   */
  private final double _notional;

  /**
   * Constructor from all the coupon details.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   */
  public CouponDefinition(ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double accrualFactor, double notional) {
    super(paymentDate);
    Validate.notNull(accrualStartDate, "accrual start date");
    this._accrualStartDate = accrualStartDate;
    Validate.notNull(accrualEndDate, "accrual end date");
    this._accrualEndDate = accrualEndDate;
    this._accrualFactor = accrualFactor;
    this._notional = notional;
  }

  /**
   * Constructor with reduced number of dates. The payment date is used for the coupon accrual end date.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   */
  public CouponDefinition(ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, double accrualFactor, double notional) {
    super(paymentDate);
    Validate.notNull(accrualStartDate, "accrual start date");
    this._accrualStartDate = accrualStartDate;
    this._accrualEndDate = paymentDate;
    this._accrualFactor = accrualFactor;
    this._notional = notional;
  }

  /**
   * Gets the accrualStartDate field.
   * @return the accrualStartDate
   */
  public ZonedDateTime getAccrualStartDate() {
    return _accrualStartDate;
  }

  /**
   * Gets the accrualEndDate field.
   * @return the accrualEndDate
   */
  public ZonedDateTime getAccrualEndDate() {
    return _accrualEndDate;
  }

  /**
   * Gets the accrualFactor field.
   * @return the accrualFactor
   */
  public double getAccrualFactor() {
    return _accrualFactor;
  }

  /**
   * Gets the notional field.
   * @return the notional
   */
  public double getNotional() {
    return _notional;
  }

}
