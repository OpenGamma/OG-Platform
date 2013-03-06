/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;

/**
 * @deprecated This is a temporary class until ForexDerivative and InterestRateDerivative are in some way unified
 */
@Deprecated
public class ForexForward extends Forex {

  private final double _spotFX;

  /**
   * This is the exchange of an amount of domestic currency for an amount of foreign currency at some time in the future
   * @param paymentCurrency1 domestic payment
   * @param paymentCurrency2 foreign payment
   * @param spotFX fxRate is defined such that ccy1 = fx*ccy2
   */
  public ForexForward(final PaymentFixed paymentCurrency1, final PaymentFixed paymentCurrency2, final double spotFX) {
    super(paymentCurrency1, paymentCurrency2);
    _spotFX = spotFX;

  }

  public double getSpotForexRate() {
    return _spotFX;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitForexForward(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitForexForward(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_spotFX);
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
    final ForexForward other = (ForexForward) obj;
    if (Double.doubleToLongBits(_spotFX) != Double.doubleToLongBits(other._spotFX)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ForexForward[");
    sb.append(getPaymentCurrency1());
    sb.append(" ");
    sb.append(getPaymentCurrency2());
    sb.append(" spot FX=");
    sb.append(_spotFX);
    sb.append("]");
    return sb.toString();
  }
}
