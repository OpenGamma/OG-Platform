/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a fixed compounded coupon. The fixed rate is compounded over several sub-periods.
 * The amount paid is equal to
 * $$
 * \begin{equation*}
 *  (1+ r)^(\delta)
 * \end{equation*}
 * $$
 * where $\delta$ is the accrual factor of the period and the $r$ the fixed rate for the same periods.
 */
public class CouponFixedAccruedCompoundingDefinition extends CouponDefinition {

  /**
   * The fixed rate.
   * All the coupon sub-periods use the same fixed rate.
   */
  private final double _rate;
  /**
   * The amount to be paid by the fixed coupon (=getNotional() * (1+_rate) ^ getPaymentYearFraction())
   */
  private final double _amount;

  /**
   * Constructor from all details
   * @param currency The coupon currency.
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period.
   * @param accrualEndDate The end date of the accrual period.
   * @param paymentYearFraction The accrual factor of the accrual period.
   * @param notional The coupon notional.
   * @param rate Fixed rate.
   */
  public CouponFixedAccruedCompoundingDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentYearFraction, final double notional, final double rate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional);
    _rate = rate;
    _amount = notional * Math.pow(1 + rate, paymentYearFraction);
  }

  /**
   * Fixed coupon constructor from a coupon and the fixed rate.
   * @param coupon Underlying coupon.
   * @param rate Fixed rate.
   */
  public CouponFixedAccruedCompoundingDefinition(final CouponDefinition coupon, final double rate) {
    super(coupon.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional());
    _rate = rate;
    _amount = coupon.getNotional() * Math.pow(1 + rate, coupon.getPaymentYearFraction());
  }

  /**
   * Static constructor for a fixed coupon definition.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param rate Fixed rate.
   * @return The fixed coupon definition
   */
  public static CouponFixedAccruedCompoundingDefinition from(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double paymentYearFraction, final double notional, final double rate) {
    return new CouponFixedAccruedCompoundingDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional, rate);
  }

  /**
   * Gets the fixed rate.
   * @return The fixed rate
   */
  public double getRate() {
    return _rate;
  }

  /**
   * Gets the amount.
   * @return The amount
   */
  public double getAmount() {
    return _amount;
  }

  @Override
  public String toString() {
    return "CouponFixedAccruedCompoundingDefinition [_rate=" + _rate + "]";
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public CouponFixedAccruedCompounding toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date {} is after payment date {}", date, getPaymentDate()); // Required: reference date <= payment date
    final String fundingCurveName = yieldCurveNames[0];
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    return new CouponFixedAccruedCompounding(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), getRate(), getAccrualStartDate(), getAccrualEndDate());
  }

  @Override
  public CouponFixedAccruedCompounding toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date {} is after payment date {}", date, getPaymentDate()); // Required: reference date <= payment date
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    return new CouponFixedAccruedCompounding(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getRate(), getAccrualStartDate(), getAccrualEndDate());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixedAccruedCompoundingDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixedAccruedCompoundingDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rate);
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
    final CouponFixedAccruedCompoundingDefinition other = (CouponFixedAccruedCompoundingDefinition) obj;
    if (Double.doubleToLongBits(_amount) != Double.doubleToLongBits(other._amount)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return true;
  }

}
