/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.apache.commons.lang.NotImplementedException;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageTransaction;

/**
 * Transaction on a bond future security with cash settlement against a price deduced from a yield average. 
 * In particular used for AUD-SFE bond futures.
 */
public class BondFuturesYieldAverageTransactionDefinition extends FuturesTransactionDefinition<BondFuturesYieldAverageSecurityDefinition>
    implements InstrumentDefinitionWithData<BondFuturesYieldAverageTransaction, Double> {

  /**
   * Constructor.
   * @param underlyingFuture The underlying futures security.
   * @param quantity The quantity of the transaction.
   * @param tradeDate The transaction date.
   * @param tradePrice The transaction price (in the convention of the futures).
   */
  public BondFuturesYieldAverageTransactionDefinition(final BondFuturesYieldAverageSecurityDefinition underlyingFuture, final long quantity,
      final ZonedDateTime tradeDate, final double tradePrice) {
    super(underlyingFuture, quantity, tradeDate, tradePrice);
  }

  @Override
  public BondFuturesYieldAverageTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice) {
    final double referencePrice = referencePrice(dateTime, lastMarginPrice);
    final BondFuturesYieldAverageSecurity underlyingFuture = getUnderlyingSecurity().toDerivative(dateTime);
    return new BondFuturesYieldAverageTransaction(underlyingFuture, getQuantity(), referencePrice);
  }

  @Override
  public BondFuturesYieldAverageTransaction toDerivative(ZonedDateTime date, Double data, String... yieldCurveNames) {
    throw new NotImplementedException("The method toDerivative of YieldAverageBondFuturesTransactionDefinition is not implemented with curve names.");
  }

  @Override
  public BondFuturesYieldAverageTransaction toDerivative(ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of YieldAverageBondFuturesTransactionDefinition does not support the one argument method (without margin price data).");
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitYieldAverageBondFuturesTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitYieldAverageBondFuturesTransactionDefinition(this);
  }

}
