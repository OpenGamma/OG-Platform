/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.apache.commons.lang.ObjectUtils;
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
public class FederalFundsFutureTransactionDefinition implements InstrumentDefinitionWithData<FederalFundsFutureTransaction, DoubleTimeSeries<ZonedDateTime>[]> {
  //CSON
  /**
   * The underlying future security.
   */
  private final FederalFundsFutureSecurityDefinition _underlyingFuture;
  /**
   * The quantity of the transaction. Can be positive or negative.
   */
  private final int _quantity;
  /**
   * The transaction date.
   */
  private final ZonedDateTime _tradeDate;
  /**
   * The transaction price. The price is in relative number and not in percent. This is the quoted price of the future.
   */
  private final double _tradePrice;

  /**
   * Constructor.
   * @param underlyingFuture The underlying future security.
   * @param quantity The quantity of the transaction. Can be positive or negative.
   * @param tradeDate The transaction date.
   * @param tradePrice The transaction price. The price is in relative number and not in percent. This is the quoted price of the future.
   */
  public FederalFundsFutureTransactionDefinition(final FederalFundsFutureSecurityDefinition underlyingFuture, final int quantity, final ZonedDateTime tradeDate, final double tradePrice) {
    ArgumentChecker.notNull(underlyingFuture, "Future");
    ArgumentChecker.notNull(tradeDate, "Trade date");
    _underlyingFuture = underlyingFuture;
    _quantity = quantity;
    _tradeDate = tradeDate;
    _tradePrice = tradePrice;
  }

  /**
   * Gets the underlying future security.
   * @return The future.
   */
  public FederalFundsFutureSecurityDefinition getUnderlyingFuture() {
    return _underlyingFuture;
  }

  /**
   * Gets the quantity of the transaction. Can be positive or negative.
   * @return The quantity.
   */
  public int getQuantity() {
    return _quantity;
  }

  /**
   * Gets the transaction date.
   * @return The date.
   */
  public ZonedDateTime getTradeDate() {
    return _tradeDate;
  }

  /**
   * Gets the transaction price. The price is in relative number and not in percent. This is the quoted price of the future.
   * @return The trade price.
   */
  public double getTradePrice() {
    return _tradePrice;
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
    final FederalFundsFutureSecurity underlying = _underlyingFuture.toDerivative(date, data[0], yieldCurveNames);
    if (_tradeDate.equals(date)) {
      return new FederalFundsFutureTransaction(underlying, _quantity, _tradePrice);
    }
    final DoubleTimeSeries<ZonedDateTime> pastClosing = data[1].subSeries(date.minusMonths(1), date);
    ArgumentChecker.isTrue(!pastClosing.isEmpty(), "No closing price"); // There should be at least one recent margining.
    final double lastMargin = pastClosing.getLatestValue();
    return new FederalFundsFutureTransaction(underlying, _quantity, lastMargin);
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
    final FederalFundsFutureSecurity underlying = _underlyingFuture.toDerivative(date, data[0]);
    if (_tradeDate.equals(date)) {
      return new FederalFundsFutureTransaction(underlying, _quantity, _tradePrice);
    }
    final DoubleTimeSeries<ZonedDateTime> pastClosing = data[1].subSeries(date.minusMonths(1), date);
    ArgumentChecker.isTrue(!pastClosing.isEmpty(), "No closing price"); // There should be at least one recent margining.
    final double lastMargin = pastClosing.getLatestValue();
    return new FederalFundsFutureTransaction(underlying, _quantity, lastMargin);
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

  @Override
  public String toString() {
    final String result = "Quantity: " + _quantity + " of " + _underlyingFuture.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _quantity;
    result = prime * result + _tradeDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_tradePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingFuture.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FederalFundsFutureTransactionDefinition other = (FederalFundsFutureTransactionDefinition) obj;
    if (_quantity != other._quantity) {
      return false;
    }
    if (!ObjectUtils.equals(_tradeDate, other._tradeDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_tradePrice) != Double.doubleToLongBits(other._tradePrice)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingFuture, other._underlyingFuture)) {
      return false;
    }
    return true;
  }

}
