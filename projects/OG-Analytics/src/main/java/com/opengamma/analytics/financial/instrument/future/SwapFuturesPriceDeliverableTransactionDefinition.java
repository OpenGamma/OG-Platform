/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExpiredException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of an interest rate future security.
 */
public class SwapFuturesPriceDeliverableTransactionDefinition extends FuturesTransactionDefinition<SwapFuturesPriceDeliverableSecurityDefinition>
    implements InstrumentDefinitionWithData<SwapFuturesPriceDeliverableTransaction, Double> {

  /**
   * Constructor.
   * @param underlyingFuture The underlying futures security.
   * @param quantity The quantity of the transaction.
   * @param tradeDate The transaction date.
   * @param tradePrice The transaction price (in the convention of the futures).
   */
  public SwapFuturesPriceDeliverableTransactionDefinition(final SwapFuturesPriceDeliverableSecurityDefinition underlyingFuture, final long quantity,
      final ZonedDateTime tradeDate, final double tradePrice) {
    super(underlyingFuture, quantity, tradeDate, tradePrice);
  }

  /**
   * {@inheritDoc}
   * @param lastMarginPrice The price on which the last margining was done.
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public SwapFuturesPriceDeliverableTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice, final String... yieldCurveNames) {
    ArgumentChecker.notNull(dateTime, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    final LocalDate date = dateTime.toLocalDate();
    final LocalDate transactionDateLocal = getTradeDate().toLocalDate();
    final LocalDate deliveryDateLocal = getUnderlyingSecurity().getDeliveryDate().toLocalDate();
    if (date.isAfter(deliveryDateLocal)) {
      throw new ExpiredException("Valuation date, " + date + ", is after last trading date, " + deliveryDateLocal);
    }
    double referencePrice;
    if (transactionDateLocal.isBefore(date)) { // Transaction was before last margining.
      referencePrice = lastMarginPrice;
    } else { // Transaction is today
      referencePrice = getTradePrice();
    }
    final SwapFuturesPriceDeliverableSecurity underlying = getUnderlyingSecurity().toDerivative(dateTime, yieldCurveNames);
    final SwapFuturesPriceDeliverableTransaction future = new SwapFuturesPriceDeliverableTransaction(underlying, referencePrice, getQuantity());
    return future;
  }

  /**
   * {@inheritDoc}
   * @param lastMarginPrice The price on which the last margining was done.
   */
  @Override
  public SwapFuturesPriceDeliverableTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate date = dateTime.toLocalDate();
    final LocalDate transactionDateLocal = getTradeDate().toLocalDate();
    final LocalDate deliveryDateLocal = getUnderlyingSecurity().getDeliveryDate().toLocalDate();
    if (date.isAfter(deliveryDateLocal)) {
      throw new ExpiredException("Valuation date, " + date + ", is after last trading date, " + deliveryDateLocal);
    }
    double referencePrice;
    if (transactionDateLocal.isBefore(date)) { // Transaction was before last margining.
      referencePrice = lastMarginPrice;
    } else { // Transaction is today
      referencePrice = getTradePrice();
    }
    final SwapFuturesPriceDeliverableSecurity underlying = getUnderlyingSecurity().toDerivative(dateTime);
    final SwapFuturesPriceDeliverableTransaction future = new SwapFuturesPriceDeliverableTransaction(underlying, referencePrice, getQuantity());
    return future;
  }

  @Override
  public SwapFuturesPriceDeliverableTransaction toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException("The method toDerivative of " + this.getClass().getSimpleName() + " does not support the two argument method (without margin price data).");
  }

  @Override
  public SwapFuturesPriceDeliverableTransaction toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of " + this.getClass().getSimpleName() + " does not support the one argument method (without margin price data).");
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitDeliverableSwapFuturesTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitDeliverableSwapFuturesTransactionDefinition(this);
  }

}
