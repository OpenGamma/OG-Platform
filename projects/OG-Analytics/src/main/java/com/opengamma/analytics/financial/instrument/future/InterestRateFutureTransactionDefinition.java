/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExpiredException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of an interest rate future security.
 */
public class InterestRateFutureTransactionDefinition implements InstrumentDefinitionWithData<InstrumentDerivative, Double> {

  /**
   * The underlying STIR futures security.
   */
  private final InterestRateFutureSecurityDefinition _underlying;
  /**
   * The date at which the transaction was done.
   */
  private final ZonedDateTime _transactionDate;
  /**
   * The price at which the transaction was done.
   */
  private final double _transactionPrice;
  /**
   * The quantity/number of contract.
   */
  private final int _quantity;

  /**
   * Constructor.
   * @param underlying The underlying futures.
   * @param transactionDate The date at which the transaction was done.
   * @param transactionPrice The price at which the transaction was done.
   * @param quantity The quantity/number of contract.
   */
  public InterestRateFutureTransactionDefinition(final InterestRateFutureSecurityDefinition underlying, final ZonedDateTime transactionDate, final double transactionPrice, final int quantity) {
    _underlying = underlying;
    _transactionDate = transactionDate;
    _transactionPrice = transactionPrice;
    _quantity = quantity;
  }

  /**
   * @param transactionDate The transaction date of the future, not null
   * @param transactionPrice The transaction price
   * @param quantity The quantity
   * @param lastTradingDate The last trading date, not null
   * @param fixingPeriodStartDate The start date of the Ibor fixing period, not null
   * @param fixingPeriodEndDate The end date of the Ibor fixing period, not null. Must be after the fixing period start date
   * @param iborIndex The Ibor index, not null
   * @param notional  The notional
   * @param paymentAccrualFactor The payment accrual factor, not negative or zero
   * @param name The name, not null
   * @param calendar The holiday calendar, not null
   */
  public InterestRateFutureTransactionDefinition(final ZonedDateTime transactionDate, final double transactionPrice, final int quantity, final ZonedDateTime lastTradingDate,
      final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate, final IborIndex iborIndex, final double notional, final double paymentAccrualFactor,
      final String name, final Calendar calendar) {
    ArgumentChecker.notNull(lastTradingDate, "Last trading date");
    ArgumentChecker.notNull(fixingPeriodStartDate, "Fixing period start date");
    ArgumentChecker.notNull(fixingPeriodEndDate, "Fixing period end date");
    ArgumentChecker.isTrue(fixingPeriodEndDate.isAfter(fixingPeriodStartDate), "Fixing start date must be after the fixing end date");
    ArgumentChecker.notNull(iborIndex, "Ibor index");
    ArgumentChecker.notNegativeOrZero(paymentAccrualFactor, "payment accrual factor");
    ArgumentChecker.notNull(name, "Name");
    _transactionDate = transactionDate;
    _transactionPrice = transactionPrice;
    _quantity = quantity;
    _underlying = new InterestRateFutureSecurityDefinition(lastTradingDate, fixingPeriodStartDate, fixingPeriodEndDate, iborIndex, notional, paymentAccrualFactor,
        name, calendar);
  }

  /**
   * Build a interest rate futures transaction from the fixing period start date.
   * @param transactionDate The date at which the transaction was done.
   * @param transactionPrice The price at which the transaction was done.
   * @param quantity The quantity/number of contract.
   * @param fixingPeriodStartDate The start date of the fixing period.
   * @param iborIndex The Ibor index associated to the future.
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor.
   * @param name The future name.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The interest rate futures.
   */
  public static InterestRateFutureTransactionDefinition fromFixingPeriodStartDate(final ZonedDateTime transactionDate, final double transactionPrice, final int quantity,
      final ZonedDateTime fixingPeriodStartDate, final IborIndex iborIndex, final double notional, final double paymentAccrualFactor, final String name,
      final Calendar calendar) {
    ArgumentChecker.notNull(fixingPeriodStartDate, "Fixing period start date");
    ArgumentChecker.notNull(iborIndex, "Ibor index");
    final ZonedDateTime lastTradingDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, -iborIndex.getSpotLag(), calendar);
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, iborIndex, calendar);
    return new InterestRateFutureTransactionDefinition(transactionDate, transactionPrice, quantity, lastTradingDate, fixingPeriodStartDate, fixingPeriodEndDate, iborIndex, notional,
        paymentAccrualFactor, name, calendar);
  }

  /**
   * Gets the date at which the transaction was done.
   * @return The transaction date.
   */
  public ZonedDateTime getTransactionDate() {
    return _transactionDate;
  }

  /**
   * Gets the price at which the transaction was done.
   * @return The transaction price.
   */
  public double getTransactionPrice() {
    return _transactionPrice;
  }

  /**
   * Gets the future last trading date.
   * @return The last trading date.
   */
  public ZonedDateTime getLastTradingDate() {
    return _underlying.getLastTradingDate();
  }

  /**
   * Gets the Ibor index associated to the future.
   * @return The Ibor index
   */
  public IborIndex getIborIndex() {
    return _underlying.getIborIndex();
  }

  /**
   * Gets the fixing period of the reference Ibor starting date.
   * @return The fixing period starting date
   */
  public ZonedDateTime getFixingPeriodStartDate() {
    return _underlying.getFixingPeriodStartDate();
  }

  /**
   * Gets the fixing period of the reference Ibor end date.
   * @return The fixing period end date.
   */
  public ZonedDateTime getFixingPeriodEndDate() {
    return _underlying.getFixingPeriodEndDate();
  }

  /**
   * Gets the fixing period of the reference Ibor accrual factor.
   * @return The Fixing period accrual factor.
   */
  public double getFixingPeriodAccrualFactor() {
    return _underlying.getFixingPeriodAccrualFactor();
  }

  /**
   * Gets the future notional.
   * @return The notional.
   */
  public double getNotional() {
    return _underlying.getNotional();
  }

  /**
   * Gets the future payment accrual factor.
   * @return The future payment accrual factor.
   */
  public double getPaymentAccrualFactor() {
    return _underlying.getPaymentAccrualFactor();
  }

  /**
   * Gets the future name.
   * @return The name
   */
  public String getName() {
    return _underlying.getName();
  }

  /**
   * The future currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _underlying.getCurrency();
  }

  /**
   * Gets the quantity/number of contract.
   * @return The quantity.
   */
  public int getQuantity() {
    return _quantity;
  }

  public InterestRateFutureTransactionDefinition withNewNotionalAndTransactionPrice(final double notional, final double transactionPrice) {
    return new InterestRateFutureTransactionDefinition(_transactionDate, transactionPrice, _quantity, getLastTradingDate(), getFixingPeriodStartDate(), getFixingPeriodEndDate(), getIborIndex(),
        notional, getPaymentAccrualFactor(), getName(), _underlying.getCalendar());
  }

  /**
   * {@inheritDoc}
   * @param lastMarginPrice The price on which the last margining was done.
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public InterestRateFutureTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice, final String... yieldCurveNames) {
    ArgumentChecker.notNull(dateTime, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    final LocalDate date = dateTime.toLocalDate();
    ArgumentChecker.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    final LocalDate transactionDateLocal = _transactionDate.toLocalDate();
    final LocalDate lastMarginDateLocal = getFixingPeriodStartDate().toLocalDate();
    if (date.isAfter(lastMarginDateLocal)) {
      throw new ExpiredException("Valuation date, " + date + ", is after last margin date, " + lastMarginDateLocal);
    }
    double referencePrice;
    if (transactionDateLocal.isBefore(date)) { // Transaction was before last margining.
      referencePrice = lastMarginPrice;
    } else { // Transaction is today
      referencePrice = _transactionPrice;
    }
    final InterestRateFutureSecurity underlying = _underlying.toDerivative(dateTime, yieldCurveNames);
    final InterestRateFutureTransaction future = new InterestRateFutureTransaction(underlying, referencePrice, _quantity);
    return future;
  }

  @Override
  public InstrumentDerivative toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of " + this.getClass().getSimpleName() + " does not support the two argument method (without margin price data).");
  }

  /**
   * {@inheritDoc}
   * @param lastMarginPrice The price on which the last margining was done.
   */
  @Override
  public InterestRateFutureTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate date = dateTime.toLocalDate();
    final LocalDate transactionDateLocal = _transactionDate.toLocalDate();
    final LocalDate lastMarginDateLocal = getFixingPeriodStartDate().toLocalDate();
    if (date.isAfter(lastMarginDateLocal)) {
      throw new ExpiredException("Valuation date, " + date + ", is after last margin date, " + lastMarginDateLocal);
    }
    double referencePrice;
    if (transactionDateLocal.isBefore(date)) { // Transaction was before last margining.
      referencePrice = lastMarginPrice;
    } else { // Transaction is today
      referencePrice = _transactionPrice;
    }
    final InterestRateFutureSecurity underlying = _underlying.toDerivative(dateTime);
    final InterestRateFutureTransaction future = new InterestRateFutureTransaction(underlying, referencePrice, _quantity);
    return future;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public InstrumentDerivative toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException("The method toDerivative of " + this.getClass().getSimpleName() + " does not support the two argument method (without margin price data).");
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureTransactionDefinition(this);
  }

  @Override
  public String toString() {
    final String result = "Quantity: " + _quantity + " of " + _underlying.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _quantity;
    result = prime * result + _transactionDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_transactionPrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlying.hashCode();
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
    final InterestRateFutureTransactionDefinition other = (InterestRateFutureTransactionDefinition) obj;
    if (_quantity != other._quantity) {
      return false;
    }
    if (!ObjectUtils.equals(_transactionDate, other._transactionDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_transactionPrice) != Double.doubleToLongBits(other._transactionPrice)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlying, other._underlying)) {
      return false;
    }
    return true;
  }

}
