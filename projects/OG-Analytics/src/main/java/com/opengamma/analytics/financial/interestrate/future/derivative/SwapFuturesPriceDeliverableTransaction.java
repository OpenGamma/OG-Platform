/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of an interest rate future security.
 */
public class SwapFuturesPriceDeliverableTransaction extends FuturesTransaction<SwapFuturesPriceDeliverableSecurity> {

  /**
   * Constructor.
   * @param underlyingFuture The underlying future security.
   * @param quantity The quantity of future.
   * @param referencePrice The reference price.
   */
  public SwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableSecurity underlyingFuture, final double referencePrice, final long quantity) {
    super(underlyingFuture, quantity, referencePrice);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapFuturesPriceDeliverableTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapFuturesPriceDeliverableTransaction(this);
  }

}
