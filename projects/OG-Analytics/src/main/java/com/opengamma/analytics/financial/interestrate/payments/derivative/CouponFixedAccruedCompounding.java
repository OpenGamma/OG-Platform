/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a fixed compounded coupon.
 * The amount paid is equal to
 * $$
 * \begin{equation*}
 *  (1+r)^ \delta -\epsilon
 * \end{equation*}
 * $$
 * where the $\delta$ is the accrual factor of the period and the $r$ the fixed rate.
 * dates used to compute the coupon accrual factors.
 */
public class CouponFixedAccruedCompounding extends Coupon {

  /**
  * The coupon fixed rate.
  */
  private final double _fixedRate;
  /**
  * The paid amount.
  */
  private final double _amount;
  /**
   * The start date of the coupon accrual period. Can be null if of no use.
   */
  private final ZonedDateTime _accrualStartDate;
  /**
   * The end date of the coupon accrual period. Can be null if of no use.
   */
  private final ZonedDateTime _accrualEndDate;

  /**
   * Constructor from all details but accrual dates. deprecated version.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional The coupon notional.
   * @param rate the fixed rate.
   * @deprecated Use the constructor that does not refer to curve names
   */
  @Deprecated
  public CouponFixedAccruedCompounding(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional, final double rate) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional);
    _fixedRate = rate;
    _accrualStartDate = null;
    _accrualEndDate = null;
    _amount = notional * getAmount(rate, paymentYearFraction);
  }

  private double getAmount(final double rate, final double paymentYearFraction) {
    return Math.pow(1 + rate, paymentYearFraction) - 1;
  }

  /**
   * Constructor from all details but accrual dates. not deprecated version.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param rate The coupon fixed rate.
   */
  public CouponFixedAccruedCompounding(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double rate) {
    super(currency, paymentTime, paymentYearFraction, notional);
    _fixedRate = rate;
    _accrualStartDate = null;
    _accrualEndDate = null;
    _amount = notional * getAmount(rate, paymentYearFraction);
  }

  /**
   * Constructor from all details. deprecated version.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param rate The coupon fixed rate.
   * @param accrualStartDate The start date of the coupon accrual period.
   * @param accrualEndDate The end date of the coupon accrual period.
   * @deprecated Use the constructor that does not take a curve name
   */
  @Deprecated
  public CouponFixedAccruedCompounding(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional, final double rate,
      final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional);
    _fixedRate = rate;
    _amount = notional * getAmount(rate, paymentYearFraction);
    _accrualStartDate = accrualStartDate;
    _accrualEndDate = accrualEndDate;
  }

  /**
   * Constructor from all details. not deprecated version.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param rate The coupon fixed rate.
   * @param accrualStartDate The start date of the coupon accrual period.
   * @param accrualEndDate The end date of the coupon accrual period.
   */
  public CouponFixedAccruedCompounding(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double rate,
      final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate) {
    super(currency, paymentTime, paymentYearFraction, notional);
    _fixedRate = rate;
    _amount = notional * getAmount(rate, paymentYearFraction);
    _accrualStartDate = accrualStartDate;
    _accrualEndDate = accrualEndDate;
  }

  /**
   * Gets the coupon fixed rate.
   * @return The fixed rate.
   */
  public double getFixedRate() {
    return _fixedRate;
  }

  /**
   * Gets the start date of the coupon accrual period.
   * @return The accrual start date.
   */
  public ZonedDateTime getAccrualStartDate() {
    return _accrualStartDate;
  }

  /**
   * Gets the end date of the coupon accrual period.
   * @return The accrual end date.
   */
  public ZonedDateTime getAccrualEndDate() {
    return _accrualEndDate;
  }

  /**
   * Gets the paid amount.
   * @return The amount.
   */
  public double getAmount() {
    return _amount;
  }

  /**
   * Creates a new coupon with the same characteristics, except the rate which is 1.0.
   * @return The new coupon.
   */
  @SuppressWarnings("deprecation")
  public CouponFixedAccruedCompounding withUnitCoupon() {
    return new CouponFixedAccruedCompounding(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), getNotional(), 1);
  }

  /**
   * Create a new fixed coupon with all the details unchanged except that the rate is the one provided.
   * @param rate The new rate.
   * @return The coupon.
   */
  @SuppressWarnings("deprecation")
  public CouponFixedAccruedCompounding withRate(final double rate) {
    return new CouponFixedAccruedCompounding(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), getNotional(), rate, getAccrualStartDate(), getAccrualEndDate());
  }

  /**
   * Create a new fixed coupon with all the details unchanged except that the rate is shifted by the spread.
   * @param spread The rate spread.
   * @return The coupon.
   */
  @SuppressWarnings("deprecation")
  public CouponFixedAccruedCompounding withRateShifted(final double spread) {
    return new CouponFixedAccruedCompounding(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), getNotional(), getFixedRate() + spread, getAccrualStartDate(),
        getAccrualEndDate());
  }

  @SuppressWarnings("deprecation")
  @Override
  public CouponFixedAccruedCompounding withNotional(final double notional) {
    return new CouponFixedAccruedCompounding(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), notional, getFixedRate(), getAccrualStartDate(), getAccrualEndDate());
  }

  /**
   * Returns a fixed payment with the same features (currency, payment time, amount) as the fixed coupon.
   * @return A fixed payment.
   */
  @SuppressWarnings("deprecation")
  public PaymentFixed toPaymentFixed() {
    return new PaymentFixed(getCurrency(), getPaymentTime(), _amount, getFundingCurveName());
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixedAccruedCompounding(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixedAccruedCompounding(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_accrualEndDate == null) ? 0 : _accrualEndDate.hashCode());
    result = prime * result + ((_accrualStartDate == null) ? 0 : _accrualStartDate.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixedRate);
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
    final CouponFixedAccruedCompounding other = (CouponFixedAccruedCompounding) obj;
    if (_accrualEndDate == null) {
      if (other._accrualEndDate != null) {
        return false;
      }
    } else if (!_accrualEndDate.equals(other._accrualEndDate)) {
      return false;
    }
    if (_accrualStartDate == null) {
      if (other._accrualStartDate != null) {
        return false;
      }
    } else if (!_accrualStartDate.equals(other._accrualStartDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_amount) != Double.doubleToLongBits(other._amount)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixedRate) != Double.doubleToLongBits(other._fixedRate)) {
      return false;
    }
    return true;
  }

}
