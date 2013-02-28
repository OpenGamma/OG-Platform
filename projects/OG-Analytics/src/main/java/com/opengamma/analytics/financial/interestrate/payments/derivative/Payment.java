/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a generic payment.
 */
public abstract class Payment implements InstrumentDerivative {

  /**
   * The index currency.
   */
  private final Currency _currency;
  /**
   * The payment time.
   */
  private final double _paymentTime;
  /**
   * The funding curve name used in pricing.
   */
  private final String _fundingCurveName;

  /**
   * Constructor for a Payment.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   */
  public Payment(final Currency currency, final double paymentTime, final String fundingCurveName) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(fundingCurveName, "funding curve name");
    ArgumentChecker.isTrue(paymentTime >= 0.0, "payment time < 0");
    _currency = currency;
    _paymentTime = paymentTime;
    _fundingCurveName = fundingCurveName;
  }

  /**
   * Gets the _paymentTime field.
   * @return the _paymentTime
   */
  public double getPaymentTime() {
    return _paymentTime;
  }

  /**
   * Gets the _fundingCurveName field.
   * @return the _fundingCurveName
   */
  public String getFundingCurveName() {
    return _fundingCurveName;
  }

  /**
   * Gets the _currency field.
   * @return The currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Return a reference amount. For coupon it is the notional, for simple payments it is the paid amount. Used mainly to assess if the amount is paid or received.
   * @return The amount.
   */
  public abstract double getReferenceAmount();

  /**
   * Check if the payment is of the type CouponFixed or CouponIbor. Used to check that payment are of vanilla type.
   * @return  True if IborCoupon or FixedCoupon
   */
  public boolean isIborOrFixed() { //TODO: is this method necessary?
    return (this instanceof CouponFixed) || (this instanceof CouponIbor);
  }

  @Override
  public String toString() {
    return "Currency=" + _currency + ", Payment time=" + _paymentTime + ", Funding curve=" + _fundingCurveName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _fundingCurveName.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_paymentTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final Payment other = (Payment) obj;
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_fundingCurveName, other._fundingCurveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentTime) != Double.doubleToLongBits(other._paymentTime)) {
      return false;
    }
    return true;
  }

}
