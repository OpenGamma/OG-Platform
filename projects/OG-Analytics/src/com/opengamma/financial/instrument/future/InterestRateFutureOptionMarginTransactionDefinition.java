/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

/**
 * Description of transaction on an interest rate future option security with daily margining process (LIFFE and Eurex type).
 */
public class InterestRateFutureOptionMarginTransactionDefinition {

  /**
   * The underlying option future security.
   */
  private final InterestRateFutureOptionMarginSecurityDefinition _underlyingOption;
  /**
   * The quantity of the transaction. Can be positive or negative.
   */
  private final int _quantity;
  /**
   * The transaction date.
   */
  private final ZonedDateTime _tradeDate;
  /**
   * The transaction price. The price is in relative number and not in percent. A standard price will be 0.985 and not 98.5.
   */
  private final double _tradePrice;

  /**
   * Constructor of the future option transaction from details.
   * @param underlyingOption The underlying option future security.
   * @param quantity The quantity of the transaction. Can be positive or negative.
   * @param tradeDate The transaction date.
   * @param tradePrice The transaction price.
   */
  public InterestRateFutureOptionMarginTransactionDefinition(InterestRateFutureOptionMarginSecurityDefinition underlyingOption, int quantity, ZonedDateTime tradeDate, double tradePrice) {
    Validate.notNull(underlyingOption, "underlying option");
    Validate.notNull(tradeDate, "trade date");
    this._underlyingOption = underlyingOption;
    this._quantity = quantity;
    this._tradeDate = tradeDate;
    this._tradePrice = tradePrice;
  }

  /**
   * Gets the underlying option future security.
   * @return The underlying.
   */
  public InterestRateFutureOptionMarginSecurityDefinition getUnderlyingOption() {
    return _underlyingOption;
  }

  /**
   * Gets the quantity of the transaction. Can be positive or negative.
   * @return The quantity of the transaction. 
   */
  public int getQuantity() {
    return _quantity;
  }

  /**
   * Gets the transaction date.
   * @return The transaction date.
   */
  public ZonedDateTime getTradeDate() {
    return _tradeDate;
  }

  /**
   * Gets the transaction price.
   * @return The transaction price.
   */
  public double getTradePrice() {
    return _tradePrice;
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
    result = prime * result + _underlyingOption.hashCode();
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
    InterestRateFutureOptionMarginTransactionDefinition other = (InterestRateFutureOptionMarginTransactionDefinition) obj;
    if (_quantity != other._quantity) {
      return false;
    }
    if (!ObjectUtils.equals(_tradeDate, other._tradeDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_tradePrice) != Double.doubleToLongBits(other._tradePrice)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingOption, other._underlyingOption)) {
      return false;
    }
    return true;
  }

}
