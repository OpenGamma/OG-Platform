/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;

/**
 * Transaction on bond option futures with daily margin on the option.
 */
public class BondFuturesOptionMarginTransaction extends FuturesTransaction<BondFuturesOptionMarginSecurity> {

  /**
   * The future transaction constructor.
   * @param underlyingFuture The underlying future security.
   * @param quantity The quantity of future.
   * @param referencePrice The reference price.
   */
  public BondFuturesOptionMarginTransaction(final BondFuturesOptionMarginSecurity underlyingFuture, final long quantity, final double referencePrice) {
    super(underlyingFuture, quantity, referencePrice);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitBondFuturesOptionMarginTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondFuturesOptionMarginTransaction(this);
  }

}
