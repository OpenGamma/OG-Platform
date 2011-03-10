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
public abstract class CouponDefinition extends PaymentDefinition {

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
  private final double _paymentYearFraction;
  /**
   * The coupon's notional.
   */
  private final double _notional;

  /**
   * Constructor from all the coupon details.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   */
  public CouponDefinition(ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double paymentYearFraction, double notional) {
    super(paymentDate);
    Validate.notNull(accrualStartDate, "accrual start date");
    this._accrualStartDate = accrualStartDate;
    Validate.notNull(accrualEndDate, "accrual end date");
    Validate.isTrue(accrualEndDate.isAfter(accrualStartDate), "end before start");
    this._accrualEndDate = accrualEndDate;
    Validate.isTrue(paymentYearFraction >= 0.0, "year fraction < 0");
    this._paymentYearFraction = paymentYearFraction;
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
    this._paymentYearFraction = accrualFactor;
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
  public double getPaymentYearFraction() {
    return _paymentYearFraction;
  }

  /**
   * Gets the notional field.
   * @return the notional
   */
  public double getNotional() {
    return _notional;
  }

}
