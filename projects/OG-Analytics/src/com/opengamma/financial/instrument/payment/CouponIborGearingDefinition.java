/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.util.money.Currency;

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
