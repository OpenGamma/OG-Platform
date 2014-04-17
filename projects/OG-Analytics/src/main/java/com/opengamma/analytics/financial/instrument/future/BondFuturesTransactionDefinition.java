/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;

/**
 * Description of a bond future transaction (definition version).
 */
public class BondFuturesTransactionDefinition extends FuturesTransactionDefinition<BondFuturesSecurityDefinition>
    implements InstrumentDefinitionWithData<BondFuturesTransaction, Double> {

  /**
   * Constructor of the future transaction.
   * @param underlyingFuture Underlying future security.
   * @param quantity Quantity of future. Can be positive or negative.
   * @param tradeDate Transaction date.
   * @param tradePrice Transaction price.
   */
  public BondFuturesTransactionDefinition(final BondFuturesSecurityDefinition underlyingFuture, final long quantity, final ZonedDateTime tradeDate, final double tradePrice) {
    super(underlyingFuture, quantity, tradeDate, tradePrice);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BondFuturesTransaction toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException("The method toDerivative of BondFutureTransactionDefinition does not support the two argument method (without margin price data).");
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BondFuturesTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice, final String... yieldCurveNames) {
    final double referencePrice = referencePrice(dateTime, lastMarginPrice);
    final BondFuturesSecurity underlyingFuture = getUnderlyingSecurity().toDerivative(dateTime, yieldCurveNames);
    final BondFuturesTransaction futureTransaction = new BondFuturesTransaction(underlyingFuture, getQuantity(), referencePrice);
    return futureTransaction;
  }

  @Override
  public BondFuturesTransaction toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of BondFutureTransactionDefinition does not support the one argument method (without margin price data).");
  }

  @Override
  public BondFuturesTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice) {
    final double referencePrice = referencePrice(dateTime, lastMarginPrice);
    final BondFuturesSecurity underlyingFuture = getUnderlyingSecurity().toDerivative(dateTime);
    final BondFuturesTransaction futureTransaction = new BondFuturesTransaction(underlyingFuture, getQuantity(), referencePrice);
    return futureTransaction;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitBondFuturesTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondFuturesTransactionDefinition(this);
  }

}
