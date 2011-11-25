/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.payments.PaymentFixed;

/**
 * TODO This is a temporary class until ForexDerivative and InterestRateDerivative are in some way unified 
 */
public class ForexForward extends Forex {

  private double _spotFX;

  /**
   * This is the exchange of an amount of domestic currency for an amount of foreign currency at some time in the future 
   * @param paymentCurrency1 domestic payment
   * @param paymentCurrency2 foreign payment 
   * @param spotFX fxRate is defined such that ccy1 = fx*ccy2 
   */
  public ForexForward(PaymentFixed paymentCurrency1, PaymentFixed paymentCurrency2, double spotFX) {
    super(paymentCurrency1, paymentCurrency2);
    _spotFX = spotFX;

  }

  public double getSpotForexRate() {
    return _spotFX;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitForexForward(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitForexForward(this);
  }

}
