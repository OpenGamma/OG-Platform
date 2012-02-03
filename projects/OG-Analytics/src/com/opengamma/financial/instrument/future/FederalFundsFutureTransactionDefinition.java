/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.util.timeseries.DoubleTimeSeries;

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
  public FederalFundsFutureTransactionDefinition(final FederalFundsFutureSecurityDefinition underlyingFuture, int quantity, final ZonedDateTime tradeDate, double tradePrice) {
    Validate.notNull(underlyingFuture, "Future");
    Validate.notNull(tradeDate, "Trade date");
    this._underlyingFuture = underlyingFuture;
    this._quantity = quantity;
    this._tradeDate = tradeDate;
    this._tradePrice = tradePrice;
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

  @Override
  public FederalFundsFutureTransaction toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    throw new UnsupportedOperationException("The method toDerivative of FederalFundsFutureTransactionDefinition does not support the two argument method (without ON fixing and margin price data).");
  }

  @Override
  /**
   * @param date The reference date.
   * @param data Two time series. The first one with the ON index fixing; the second one with the future closing (margining) prices.
   * The last closing price at a date strictly before "date" is used as last closing.
   */
  public FederalFundsFutureTransaction toDerivative(ZonedDateTime date, DoubleTimeSeries<ZonedDateTime>[] data, String... yieldCurveNames) {
    Validate.notNull(date, "Date");
    Validate.isTrue(data.length >= 2, "At least two time series: ON index and future closing");
    FederalFundsFutureSecurity underlying = _underlyingFuture.toDerivative(date, data[0], yieldCurveNames);
    if (_tradeDate.equals(date)) {
      return new FederalFundsFutureTransaction(underlying, _quantity, _tradePrice);
    }
    DoubleTimeSeries<ZonedDateTime> pastClosing = data[1].subSeries(date.minusMonths(1), date);
    Validate.isTrue(!pastClosing.isEmpty(), "No closing price"); // There should be at least one recent margining.
    double lastMargin = pastClosing.getLatestValue();
    return new FederalFundsFutureTransaction(underlying, _quantity, lastMargin);
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitFederalFundsFutureTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitFederalFundsFutureTransactionDefinition(this);
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FederalFundsFutureTransactionDefinition other = (FederalFundsFutureTransactionDefinition) obj;
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
