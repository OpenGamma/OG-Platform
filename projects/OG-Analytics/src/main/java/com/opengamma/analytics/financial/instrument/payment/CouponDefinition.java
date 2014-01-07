/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

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
   * The accrual factor (or year fraction) associated to the coupon accrual period.
   */
  private final double _paymentYearFraction;
  /**
   * The coupon's notional.
   */
  private final double _notional;

  /**
   * Constructor from all the coupon details.
   * @param currency The coupon currency.
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period.
   * @param accrualEndDate The end date of the accrual period.
   * @param paymentAccrualFactor The accrual factor of the accrual period.
   * @param notional The coupon notional.
   */
  public CouponDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional) {
    super(currency, paymentDate);
    ArgumentChecker.notNull(accrualStartDate, "accrual start date");
    _accrualStartDate = accrualStartDate;
    ArgumentChecker.notNull(accrualEndDate, "accrual end date");
    ArgumentChecker.isTrue(!accrualEndDate.isBefore(accrualStartDate), "end before start"); // REVIEW
    _accrualEndDate = accrualEndDate;
    ArgumentChecker.isTrue(paymentAccrualFactor >= 0.0, "year fraction < 0");
    _paymentYearFraction = paymentAccrualFactor;
    _notional = notional;
  }

  /**
   * Constructor with reduced number of dates. The payment date is used for the coupon accrual end date.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   */
  public CouponDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final double accrualFactor,
      final double notional) {
    super(currency, paymentDate);
    ArgumentChecker.notNull(accrualStartDate, "accrual start date");
    _accrualStartDate = accrualStartDate;
    _accrualEndDate = paymentDate;
    _paymentYearFraction = accrualFactor;
    _notional = notional;
  }

  /**
   * Gets the accrual start date.
   * @return The accrual start date.
   */
  public ZonedDateTime getAccrualStartDate() {
    return _accrualStartDate;
  }

  /**
   * Gets the accrual end date.
   * @return The accrual end date.
   */
  public ZonedDateTime getAccrualEndDate() {
    return _accrualEndDate;
  }

  /**
   * Gets the accrual factor (or year fraction).
   * @return The accrual factor.
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

  @Override
  public String toString() {
    return super.toString() + ", Coupon period = [" + _accrualStartDate.toString() + " - " + _accrualEndDate.toString() + " - " + _paymentYearFraction + "], Notional = "
        + _notional;
  }

  @Override
  public double getReferenceAmount() {
    return _notional;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _accrualEndDate.hashCode();
    result = prime * result + _accrualStartDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentYearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final CouponDefinition other = (CouponDefinition) obj;
    if (!ObjectUtils.equals(_accrualEndDate, other._accrualEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_accrualStartDate, other._accrualStartDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentYearFraction) != Double.doubleToLongBits(other._paymentYearFraction)) {
      return false;
    }
    return true;
  }

}
