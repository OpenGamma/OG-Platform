/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of transaction on an interest rate future option security with daily margining process (LIFFE and Eurex type).
 */
public class BondFuturesOptionMarginTransactionDefinition extends FuturesTransactionDefinition<BondFuturesOptionMarginSecurityDefinition>
    implements InstrumentDefinitionWithData<BondFuturesOptionMarginTransaction, Double> {

  /**
   * Constructor of the future option transaction from details.
   * @param underlyingOption The underlying option future security.
   * @param quantity The quantity of the transaction. Can be positive or negative.
   * @param tradeDate The transaction date.
   * @param tradePrice The transaction price.
   */
  public BondFuturesOptionMarginTransactionDefinition(final BondFuturesOptionMarginSecurityDefinition underlyingOption, final int quantity, final ZonedDateTime tradeDate,
      final double tradePrice) {
    super(underlyingOption, quantity, tradeDate, tradePrice);
  }

  @Override
  public BondFuturesOptionMarginTransaction toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of InterestRateTransactionDefinition does not support the one argument method (without margin price data).");
  }

  /**
   * {@inheritDoc}
   * The lastMarginPrice is the last closing price used for margining. It is usually the official closing price of the previous business day.
   */
  @Override
  public BondFuturesOptionMarginTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice) {
    final double referencePrice = referencePrice(dateTime, lastMarginPrice);
    final BondFuturesOptionMarginSecurity underlyingOption = getUnderlyingSecurity().toDerivative(dateTime);
    final BondFuturesOptionMarginTransaction optionTransaction = new BondFuturesOptionMarginTransaction(underlyingOption, getQuantity(), referencePrice);
    return optionTransaction;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondFuturesOptionMarginTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondFuturesOptionMarginTransactionDefinition(this);
  }

}
