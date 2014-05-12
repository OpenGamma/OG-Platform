/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of an transaction on a Federal Funds Futures.
 */
//CSOFF Check style seems to have a problem with >[]>
public class FederalFundsFutureTransactionDefinition extends FuturesTransactionDefinition<FederalFundsFutureSecurityDefinition>
    implements InstrumentDefinitionWithData<FederalFundsFutureTransaction, DoubleTimeSeries<ZonedDateTime>[]> {
  //CSON

  /**
   * Constructor.
   * @param underlyingFuture The underlying future security.
   * @param quantity The quantity of the transaction. Can be positive or negative.
   * @param tradeDate The transaction date.
   * @param tradePrice The transaction price. The price is in relative number and not in percent. This is the quoted price of the future.
   */
  public FederalFundsFutureTransactionDefinition(final FederalFundsFutureSecurityDefinition underlyingFuture, final long quantity, final ZonedDateTime tradeDate, final double tradePrice) {
    super(underlyingFuture, quantity, tradeDate, tradePrice);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public FederalFundsFutureTransaction toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException("The method toDerivative of FederalFundsFutureTransactionDefinition does not support the two argument method (without ON fixing and margin price data).");
  }

  @Override
  public FederalFundsFutureTransaction toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of FederalFundsFutureTransactionDefinition does not support the two argument method (without ON fixing and margin price data).");
  }

  /**
   * @param date The reference date.
   * @param data Two time series. The first one with the ON index fixing; the second one with the future closing (margining) prices.
   * @param yieldCurveNames The yield curve names
   * The last closing price at a date strictly before "date" is used as last closing.
   * @return The derivative form
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public FederalFundsFutureTransaction toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime>[] data, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "Date");
    ArgumentChecker.isTrue(data.length >= 2, "At least two time series: ON index and future closing");
    final FederalFundsFutureSecurity underlying = getUnderlyingFuture().toDerivative(date, data[0], yieldCurveNames);
    if (getTradeDate().equals(date)) {
      return new FederalFundsFutureTransaction(underlying, getQuantity(), getTradePrice());
    }
    final DoubleTimeSeries<ZonedDateTime> pastClosing = data[1].subSeries(date.minusMonths(1), date);
    ArgumentChecker.isTrue(!pastClosing.isEmpty(), "No closing price"); // There should be at least one recent margining.
    final double lastMargin = pastClosing.getLatestValue();
    return new FederalFundsFutureTransaction(underlying, getQuantity(), lastMargin);
  }

  /**
   * {@inheritDoc}
   * @param date The reference date.
   * @param data Two time series. The first one with the ON index fixing; the second one with the future closing (margining) prices.
   * The last closing price at a date strictly before "date" is used as last closing.
   * @return The derivative form
   */
  @Override
  public FederalFundsFutureTransaction toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime>[] data) {
    ArgumentChecker.notNull(date, "Date");
    ArgumentChecker.isTrue(data.length >= 2, "At least two time series: ON index and future closing");
    final FederalFundsFutureSecurity underlying = getUnderlyingFuture().toDerivative(date, data[0]);
    if (getTradeDate().equals(date)) {
      return new FederalFundsFutureTransaction(underlying, getQuantity(), getTradePrice());
    }
    final DoubleTimeSeries<ZonedDateTime> pastClosing = data[1].subSeries(date.minusMonths(1), date);
    ArgumentChecker.isTrue(!pastClosing.isEmpty(), "No closing price"); // There should be at least one recent margining.
    final double lastMargin = pastClosing.getLatestValue();
    return new FederalFundsFutureTransaction(underlying, getQuantity(), lastMargin);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFederalFundsFutureTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFederalFundsFutureTransactionDefinition(this);
  }

}
