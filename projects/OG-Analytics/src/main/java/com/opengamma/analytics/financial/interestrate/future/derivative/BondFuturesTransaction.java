/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;

/**
 * Description of transaction on an bond future security.
 */
public class BondFuturesTransaction extends FuturesTransaction<BondFuturesSecurity> {

  /**
   * The future transaction constructor.
   * @param underlyingFuture The underlying future security.
   * @param quantity The quantity of future.
   * @param referencePrice The reference price.
   */
  public BondFuturesTransaction(final BondFuturesSecurity underlyingFuture, final long quantity, final double referencePrice) {
    super(underlyingFuture, quantity, referencePrice);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitBondFuturesTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondFuturesTransaction(this);
  }

}
