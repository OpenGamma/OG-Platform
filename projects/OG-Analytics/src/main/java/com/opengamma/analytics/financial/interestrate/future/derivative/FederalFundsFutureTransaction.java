/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of an transaction on a Federal Funds Futures.
 */
public class FederalFundsFutureTransaction extends FuturesTransaction<FederalFundsFutureSecurity> {

  /**
   * Constructor.
   * @param underlyingFuture The underlying future security.
   * @param quantity The quantity of the transaction. Can be positive or negative.
   * @param referencePrice The reference price. It is the transaction price on the transaction date and the last close (margining) price afterward.
   */
  public FederalFundsFutureTransaction(final FederalFundsFutureSecurity underlyingFuture, final long quantity, final double referencePrice) {
    super(underlyingFuture, quantity, referencePrice);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFederalFundsFutureTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFederalFundsFutureTransaction(this);
  }

}
