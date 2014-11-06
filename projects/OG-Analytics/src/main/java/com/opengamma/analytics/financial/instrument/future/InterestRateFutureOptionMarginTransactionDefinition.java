/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of transaction on an interest rate future option security with daily margining process (LIFFE and Eurex type).
 */
public class InterestRateFutureOptionMarginTransactionDefinition extends FuturesTransactionDefinition<InterestRateFutureOptionMarginSecurityDefinition>
    implements InstrumentDefinitionWithData<InterestRateFutureOptionMarginTransaction, Double> {

  /**
   * Constructor of the future option transaction from details.
   * @param underlyingOption The underlying option future security.
   * @param quantity The quantity of the transaction. Can be positive or negative.
   * @param tradeDate The transaction date.
   * @param tradePrice The transaction price.
   */
  public InterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginSecurityDefinition underlyingOption, final long quantity,
      final ZonedDateTime tradeDate, final double tradePrice) {
    super(underlyingOption, quantity, tradeDate, tradePrice);
  }

  /**
   * The lastMarginPrice is the last closing price used for margining. It is usually the official closing price of the previous business day.
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public InterestRateFutureOptionMarginTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice, final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @Override
  public InterestRateFutureOptionMarginTransaction toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of InterestRateTransactionDefinition does not support the two argument method (without margin price data).");
  }

  /**
   * {@inheritDoc}
   * The lastMarginPrice is the last closing price used for margining. It is usually the official closing price of the previous business day.
   */
  @Override
  public InterestRateFutureOptionMarginTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice) {
    final double referencePrice = referencePrice(dateTime, lastMarginPrice);
    final InterestRateFutureOptionMarginSecurity underlyingOption = getUnderlyingSecurity().toDerivative(dateTime);
    final InterestRateFutureOptionMarginTransaction optionTransaction = new InterestRateFutureOptionMarginTransaction(underlyingOption, getQuantity(), referencePrice);
    return optionTransaction;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionMarginTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionMarginTransactionDefinition(this);
  }

}
