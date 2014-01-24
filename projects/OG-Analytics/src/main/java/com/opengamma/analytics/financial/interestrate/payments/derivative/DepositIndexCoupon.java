/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Interface for coupons with deposit-like indices.
 * 
 * @param <I> The index type.
 */
public interface DepositIndexCoupon<I extends IndexDeposit> {
  
  Currency getCurrency();
  
  double getNotional();
  
  double getPaymentTime();
  
  double getPaymentYearFraction();
  
  I getIndex();
  
  <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data);

  <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor);
}
