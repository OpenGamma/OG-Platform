/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;

/**
 * Transaction on a bond future security with cash settlement against a price deduced from a yield average. 
 * In particular used for AUD-SFE bond futures.
 */
public class BondFuturesYieldAverageTransaction extends FuturesTransaction<BondFuturesYieldAverageSecurity> {

  /**
   * Constructor
   * @param underlyingFuture The underlying yield average futures security.
   * @param quantity The transaction quantity.
   * @param referencePrice The reference price.
   */
  public BondFuturesYieldAverageTransaction(final BondFuturesYieldAverageSecurity underlyingFuture, final long quantity, final double referencePrice) {
    super(underlyingFuture, quantity, referencePrice);
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitYieldAverageBondFuturesTransaction(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitYieldAverageBondFuturesTransaction(this);
  }

}
