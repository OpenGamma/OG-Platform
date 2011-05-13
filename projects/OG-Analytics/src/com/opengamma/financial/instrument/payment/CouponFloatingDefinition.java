/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.util.money.Currency;

/**
 * Class describing a generic floating payment coupon.
 */
public class CouponFloatingDefinition extends CouponDefinition {

  /**
   * The coupon fixing date.
   */
  private final ZonedDateTime _fixingDate;
  /**
   * The flag indicating if the coupon has already fixed (true) or not (false).
   */
  private boolean _isFixed;
  /**
   * If isFixed, contains the fixing (reset) rate.
   */
  private double _fixedRate;

  /**
   * Floating coupon constructor from all details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   */
  public CouponFloatingDefinition(Currency currency, ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double accrualFactor, double notional,
      ZonedDateTime fixingDate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional);
    Validate.notNull(fixingDate, "fixing date");
    Validate.isTrue(!fixingDate.isAfter(paymentDate), "payment date strictly before fixing");
    this._fixingDate = fixingDate;
    this._isFixed = false;
  }

  /**
   * Builder from a coupon and a fixing date.
   * @param coupon A coupon with the details of the coupon to construct.
   * @param fixingDate The floating coupon fixing date.
   * @return The constructed floating coupon.
   */
  public static CouponFloatingDefinition from(CouponDefinition coupon, ZonedDateTime fixingDate) {
    Validate.notNull(coupon, "coupon");
    return new CouponFloatingDefinition(coupon.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional(),
        fixingDate);
  }

  /** 
   * Process to fix (reset) the floating coupon.
   * @param rate The fixed rate.
   */
  public void fixingProcess(double rate) {
    this._fixedRate = rate;
    this._isFixed = true;
  }

  /**
   * Gets the fixing date.
   * @return The fixing date.
   */
  public ZonedDateTime getFixingDate() {
    return _fixingDate;
  }

  /**
   * Gets the isFixed status.
   * @return the isFixed
   */
  public boolean isFixed() {
    return _isFixed;
  }

  /**
   * Gets the fixed rate. If isFixed is false, the result is undefined.
   * @return The fixed rate.
   */
  public double getFixedRate() {
    return _fixedRate;
  }

  @Override
  public String toString() {
    String result = super.toString() + ", Fixing date = " + _fixingDate + ", is fixed: " + _isFixed;
    if (_isFixed) {
      result += ", Fixed rate = " + _fixedRate;
    }
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixedRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _fixingDate.hashCode();
    result = prime * result + (_isFixed ? 1231 : 1237);
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
    CouponFloatingDefinition other = (CouponFloatingDefinition) obj;
    if (Double.doubleToLongBits(_fixedRate) != Double.doubleToLongBits(other._fixedRate)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingDate, other._fixingDate)) {
      return false;
    }
    if (_isFixed != other._isFixed) {
      return false;
    }
    return true;
  }

  //TODO: remove when abstract?
  @Override
  public Payment toDerivative(LocalDate date, String... yieldCurveNames) {
    return null;
  }

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return null;
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return null;
  }

}
