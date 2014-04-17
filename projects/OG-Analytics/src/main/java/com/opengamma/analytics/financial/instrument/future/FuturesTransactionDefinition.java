/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract class for transactions on generic futures-like products =, i.e. financial instruments with daily marniging.
 * @param <FS> The futures type of the underlying security.
 */
public abstract class FuturesTransactionDefinition<FS extends FuturesSecurityDefinition<? extends FuturesSecurity>> {

  /**
   * Underlying future security. Not null;
   */
  private final FS _underlyingSecurity;
  /**
   * Quantity of future. Can be positive or negative.
   */
  private final long _quantity;
  /**
   * Transaction date. Not null.
   */
  private final ZonedDateTime _tradeDate;
  /**
   * Transaction price. The price is in relative number and not in percent. A standard price will be 0.985 and not 98.5.
   */
  private final double _tradePrice;

  /**
   * Constructor.
   * @param underlyingSecurity The underlying futures security.
   * @param quantity The quantity of the transaction.
   * @param tradeDate The transaction date.
   * @param tradePrice The transaction price (in the convention of the futures).
   */
  public FuturesTransactionDefinition(final FS underlyingSecurity, long quantity, ZonedDateTime tradeDate, double tradePrice) {
    super();
    ArgumentChecker.notNull(underlyingSecurity, "Underlying futures");
    ArgumentChecker.notNull(tradeDate, "Trade date");
    _underlyingSecurity = underlyingSecurity;
    _quantity = quantity;
    _tradeDate = tradeDate;
    _tradePrice = tradePrice;
  }

  /**
   * Returns the underlying futures security.
   * @return The security.
   */
  public FS getUnderlyingSecurity() {
    return _underlyingSecurity;
  }

  /**
   * Returns the transaction quantity.
   * @return The quantity.
   */
  public long getQuantity() {
    return _quantity;
  }

  /**
   * Returns the transaction date.
   * @return The date.
   */
  public ZonedDateTime getTradeDate() {
    return _tradeDate;
  }

  /**
   * Returns the transaction price (in the convention of the futures).
   * @return The price.
   */
  public double getTradePrice() {
    return _tradePrice;
  }

  /**
   * Returns the reference price of a futures transaction.
   * On the trade date, it is the trade price, on the dates (strictly) after the trade date, it is the last margin price.
   * @param dateTime The valuation date.
   * @param lastMarginPrice The lat margin price.
   * @return The price.
   */
  public double referencePrice(final ZonedDateTime dateTime, final Double lastMarginPrice) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dateLocal = dateTime.toLocalDate();
    final LocalDate transactionDateLocal = getTradeDate().toLocalDate();
    ArgumentChecker.isTrue(!dateTime.isBefore(getTradeDate()), "Valuation date, {}, is before the trade date, {} ", dateTime, getTradeDate());
    final LocalDate lastTradingDateLocal = getUnderlyingSecurity().getLastTradingDate().toLocalDate();
    ArgumentChecker.isFalse(dateLocal.isAfter(lastTradingDateLocal), "Valuation date, {}, is after last trading date, ", dateLocal, lastTradingDateLocal);
    double referencePrice;
    if (transactionDateLocal.isBefore(dateLocal)) { // Transaction was before valuation date.
      referencePrice = lastMarginPrice;
    } else { // Transaction is today
      referencePrice = getTradePrice();
    }
    return referencePrice;
  }

  @Override
  public String toString() {
    final String result = "Quantity: " + _quantity + " of " + _underlyingSecurity.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (_quantity ^ (_quantity >>> 32));
    result = prime * result + _tradeDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_tradePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingSecurity.hashCode();
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
    FuturesTransactionDefinition<?> other = (FuturesTransactionDefinition<?>) obj;
    if (_quantity != other._quantity) {
      return false;
    }
    if (!ObjectUtils.equals(_tradeDate, other._tradeDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_tradePrice) != Double.doubleToLongBits(other._tradePrice)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSecurity, other._underlyingSecurity)) {
      return false;
    }
    return true;
  }

}
