/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.apache.commons.lang.ObjectUtils;
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
public class InterestRateFutureOptionMarginTransactionDefinition implements InstrumentDefinitionWithData<InterestRateFutureOptionMarginTransaction, Double> {

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
   * The transaction price. The price is in relative number and not in percent. This is the quoted price of the option.
   */
  private final double _tradePrice;

  /**
   * Constructor of the future option transaction from details.
   * @param underlyingOption The underlying option future security.
   * @param quantity The quantity of the transaction. Can be positive or negative.
   * @param tradeDate The transaction date.
   * @param tradePrice The transaction price.
   */
  public InterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginSecurityDefinition underlyingOption, final int quantity, final ZonedDateTime tradeDate,
      final double tradePrice) {
    ArgumentChecker.notNull(underlyingOption, "underlying option");
    ArgumentChecker.notNull(tradeDate, "trade date");
    _underlyingOption = underlyingOption;
    _quantity = quantity;
    _tradeDate = tradeDate;
    _tradePrice = tradePrice;
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

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public InterestRateFutureOptionMarginTransaction toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException("The method toDerivative of InterestRateTransactionDefinition does not support the two argument method (without margin price data).");
  }

  /**
   * The lastMarginPrice is the last closing price used for margining. It is usually the official closing price of the previous business day.
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public InterestRateFutureOptionMarginTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice, final String... yieldCurveNames) {
    ArgumentChecker.notNull(dateTime, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    final LocalDate date = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!date.isAfter(_underlyingOption.getUnderlyingFuture().getFixingPeriodStartDate().toLocalDate()), "Date is after last margin date");
    final LocalDate tradeDateLocal = _tradeDate.toLocalDate();
    ArgumentChecker.isTrue(!date.isBefore(tradeDateLocal), "Valuation date {} is before the trade date {} ", date, tradeDateLocal);
    final InterestRateFutureOptionMarginSecurity underlyingOption = _underlyingOption.toDerivative(dateTime, yieldCurveNames);
    double referencePrice;
    if (tradeDateLocal.isBefore(dateTime.toLocalDate())) { // Transaction was before last margining.
      referencePrice = lastMarginPrice;
    } else { // Transaction is today
      referencePrice = _tradePrice;
    }
    final InterestRateFutureOptionMarginTransaction optionTransaction = new InterestRateFutureOptionMarginTransaction(underlyingOption, _quantity, referencePrice);
    return optionTransaction;
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
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate date = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!date.isAfter(_underlyingOption.getUnderlyingFuture().getFixingPeriodStartDate().toLocalDate()), "Date is after last margin date");
    final LocalDate tradeDateLocal = _tradeDate.toLocalDate();
    ArgumentChecker.isTrue(!date.isBefore(tradeDateLocal), "Valuation date {} is before the trade date {} ", date, tradeDateLocal);
    final InterestRateFutureOptionMarginSecurity underlyingOption = _underlyingOption.toDerivative(dateTime);
    double referencePrice;
    if (tradeDateLocal.isBefore(dateTime.toLocalDate())) { // Transaction was before last margining.
      referencePrice = lastMarginPrice;
    } else { // Transaction is today
      referencePrice = _tradePrice;
    }
    final InterestRateFutureOptionMarginTransaction optionTransaction = new InterestRateFutureOptionMarginTransaction(underlyingOption, _quantity, referencePrice);
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
    final InterestRateFutureOptionMarginTransactionDefinition other = (InterestRateFutureOptionMarginTransactionDefinition) obj;
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
