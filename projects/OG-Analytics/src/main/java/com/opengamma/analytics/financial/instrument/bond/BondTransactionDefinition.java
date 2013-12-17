/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Describes a generic single currency bond transaction.
 * @param <N> The notional type (usually FixedPayment or CouponInflationZeroCoupon).
 * @param <C> The coupon type.
 */
public abstract class BondTransactionDefinition<N extends PaymentDefinition, C extends CouponDefinition> implements
    InstrumentDefinition<BondTransaction<? extends BondSecurity<? extends Payment, ? extends Coupon>>> {

  /**
   * The bond underlying the transaction.
   */
  private final BondSecurityDefinition<N, C> _underlyingBond;
  /**
   * The number of bonds purchased (can be negative or positive).
   */
  private final double _quantity;
  /**
   * Transaction settlement date.
   */
  private final ZonedDateTime _settlementDate;
  /**
   * The ex-coupon date associated to the settlement date, i.e. ex-coupon days after settlement date.
   */
  private final ZonedDateTime _settlementExCouponDate;
  /**
   * The (quoted) price of the transaction in relative term (i.e. 0.90 if the dirty price is 90% of nominal).
   * The meaning of this number will depend on the type of bond (fixed coupon, FRN, inflation).
   */
  private final double _price;
  /**
   * The coupon index of the transaction settlement date. Take the ex-coupon period into account.
   */
  private int _couponIndex;
  /**
   * Previous accrual date. Take the ex-coupon period into account.
   */
  private final ZonedDateTime _previousAccrualDate;
  /**
   * Next accrual date.
   */
  private final ZonedDateTime _nextAccrualDate;

  /**
   * Constructor of the bond transaction from all the transaction details.
   * @param underlyingBond The bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param settlementDate Transaction settlement date.
   * @param price The (dirty) price of the transaction in relative term (i.e. 0.90 if the dirty price is 90% of nominal).
   */
  public BondTransactionDefinition(final BondSecurityDefinition<N, C> underlyingBond, final double quantity, final ZonedDateTime settlementDate, final double price) {
    ArgumentChecker.notNull(underlyingBond, "Underlying bond");
    ArgumentChecker.notNull(settlementDate, "Settlement date");
    _underlyingBond = underlyingBond;
    _quantity = quantity;
    _settlementDate = settlementDate;
    _settlementExCouponDate = ScheduleCalculator.getAdjustedDate(_settlementDate, _underlyingBond.getExCouponDays(), _underlyingBond.getCalendar());
    _price = price;
    final int nbCoupon = underlyingBond.getCoupons().getNumberOfPayments();
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (underlyingBond.getCoupons().getNthPayment(loopcpn).getAccrualEndDate().isAfter(_settlementExCouponDate)) {
        _couponIndex = loopcpn;
        break;
      }
    }
    _previousAccrualDate = underlyingBond.getCoupons().getNthPayment(getCouponIndex()).getAccrualStartDate();
    _nextAccrualDate = underlyingBond.getCoupons().getNthPayment(getCouponIndex()).getAccrualEndDate();
  }

  /**
   * Gets the bond underlying the transaction.
   * @return The underlying Bond.
   */
  public BondSecurityDefinition<N, C> getUnderlyingBond() {
    return _underlyingBond;
  }

  /**
   * Gets the number (or quantity) of bonds purchased (can be negative or positive).
   * @return The quantity.
   */
  public double getQuantity() {
    return _quantity;
  }

  /**
   * Gets the settlement date.
   * @return The settlement date.
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Gets the ex-coupon date associated to the settlement date, i.e. ex-coupon days before settlement date.
   * @return The ex-coupon date.
   */
  public ZonedDateTime getSettlementExCouponDate() {
    return _settlementExCouponDate;
  }

  /**
   * Returns the (dirty) price of the bond.
   * @return The price.
   */
  public double getPrice() {
    return _price;
  }

  /**
   * Gets the coupon index of the transaction settlement date.
   * @return The coupon index of the settlement date.
   */
  public int getCouponIndex() {
    return _couponIndex;
  }

  /**
   * Gets the previous accrual date with respect to the settlement date.
   * @return The previous accrual date.
   */
  public ZonedDateTime getPreviousAccrualDate() {
    return _previousAccrualDate;
  }

  /**
   * Gets the next accrual date with respect to the settlement date.
   * @return The next accrual date.
   */
  public ZonedDateTime getNextAccrualDate() {
    return _nextAccrualDate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_price);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_quantity);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _settlementDate.hashCode();
    result = prime * result + _underlyingBond.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BondTransactionDefinition<?, ?> other = (BondTransactionDefinition<?, ?>) obj;
    if (Double.doubleToLongBits(_price) != Double.doubleToLongBits(other._price)) {
      return false;
    }
    if (Double.doubleToLongBits(_quantity) != Double.doubleToLongBits(other._quantity)) {
      return false;
    }
    if (!ObjectUtils.equals(_settlementDate, other._settlementDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingBond, other._underlyingBond)) {
      return false;
    }
    return true;
  }

}
