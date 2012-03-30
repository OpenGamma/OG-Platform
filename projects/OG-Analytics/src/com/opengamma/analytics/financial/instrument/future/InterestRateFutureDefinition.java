/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;

/**
 * Description of an interest rate future security.
 */
public class InterestRateFutureDefinition implements InstrumentDefinitionWithData<InstrumentDerivative, Double> {

  /**
   * The date at which the transaction was done.
   */
  private final ZonedDateTime _transactionDate;
  /**
   * The price at which the transaction was done.
   */
  private final double _transactionPrice;
  /**
   * Future last trading date. Usually the date for which the third Wednesday of the month is the spot date.
   */
  private final ZonedDateTime _lastTradingDate;
  /**
   * Ibor index associated to the future.
   */
  private final IborIndex _iborIndex;
  /**
   * Fixing period of the reference Ibor starting date.
   */
  private final ZonedDateTime _fixingPeriodStartDate;
  /**
   * Fixing period of the reference Ibor end date.
   */
  private final ZonedDateTime _fixingPeriodEndDate;
  /**
   * Fixing period of the reference Ibor accrual factor.
   */
  private final double _fixingPeriodAccrualFactor;
  /**
   * Future notional.
   */
  private final double _notional;
  /**
   * Future payment accrual factor. Usually a standardized number of 0.25 for a 3M future.
   */
  private final double _paymentAccrualFactor;
  /**
   * The quantity/number of contract.
   */
  private final int _quantity;
  /**
   * Future name.
   */
  private final String _name;

  /**
   * Constructor of the interest rate future security.
   * @param transactionDate The date at which the transaction was done.
   * @param transactionPrice The price at which the transaction was done.
   * @param lastTradingDate Future last trading date.
   * @param iborIndex Ibor index associated to the future.
   * @param referencePrice TODO
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor. 
   * @param quantity The quantity/number of contract.
   * @param name Future name.
   */
  public InterestRateFutureDefinition(final ZonedDateTime transactionDate, final double transactionPrice, final ZonedDateTime lastTradingDate, final IborIndex iborIndex, double referencePrice,
      final double notional, final double paymentAccrualFactor, final int quantity, final String name) {
    Validate.notNull(lastTradingDate, "Last trading date");
    Validate.notNull(iborIndex, "Ibor index");
    Validate.notNull(name, "Name");
    _transactionDate = transactionDate;
    _transactionPrice = transactionPrice;
    this._lastTradingDate = lastTradingDate;
    this._iborIndex = iborIndex;
    _fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(_lastTradingDate, _iborIndex.getSpotLag(), _iborIndex.getCalendar());
    _fixingPeriodEndDate = ScheduleCalculator
        .getAdjustedDate(_fixingPeriodStartDate, _iborIndex.getTenor(), _iborIndex.getBusinessDayConvention(), _iborIndex.getCalendar(), _iborIndex.isEndOfMonth());
    _fixingPeriodAccrualFactor = _iborIndex.getDayCount().getDayCountFraction(_fixingPeriodStartDate, _fixingPeriodEndDate);
    this._notional = notional;
    this._paymentAccrualFactor = paymentAccrualFactor;
    _quantity = quantity;
    _name = name;
  }

  /**
   * Constructor of the interest rate future security.
   * @param transactionDate The date at which the transaction was done.
   * @param transactionPrice The price at which the transaction was done.
   * @param lastTradingDate Future last trading date.
   * @param iborIndex Ibor index associated to the future.
   * @param referencePrice TODO
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor. 
   * @param quantity The quantity/number of contract.
   */
  public InterestRateFutureDefinition(final ZonedDateTime transactionDate, final double transactionPrice, final ZonedDateTime lastTradingDate, final IborIndex iborIndex, double referencePrice,
      final double notional, final double paymentAccrualFactor, final int quantity) {
    this(transactionDate, transactionPrice, lastTradingDate, iborIndex, referencePrice, notional, paymentAccrualFactor, quantity, "RateFuture " + iborIndex.getName());
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
    return _lastTradingDate;
  }

  /**
   * Gets the Ibor index associated to the future.
   * @return The Ibor index
   */
  public IborIndex getIborIndex() {
    return _iborIndex;
  }

  /**
   * Gets the fixing period of the reference Ibor starting date.
   * @return The fixing period starting date
   */
  public ZonedDateTime getFixingPeriodStartDate() {
    return _fixingPeriodStartDate;
  }

  /**
   * Gets the fixing period of the reference Ibor end date.
   * @return The fixing period end date.
   */
  public ZonedDateTime getFixingPeriodEndDate() {
    return _fixingPeriodEndDate;
  }

  /**
   * Gets the fixing period of the reference Ibor accrual factor.
   * @return The Fixing period accrual factor.
   */
  public double getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  /**
   * Gets the future notional.
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the future payment accrual factor.
   * @return The future payment accrual factor.
   */
  public double getPaymentAccrualFactor() {
    return _paymentAccrualFactor;
  }

  /**
   * Gets the future name.
   * @return The name
   */
  public String getName() {
    return _name;
  }

  /**
   * The future currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _iborIndex.getCurrency();
  }

  /**
   * Gets the quantity/number of contract.
   * @return The quantity.
   */
  public int getQuantity() {
    return _quantity;
  }

  @Override
  /**
   * @param lastMarginPrice The price on which the last margining was done.
   */
  public InterestRateFuture toDerivative(ZonedDateTime dateTime, Double lastMarginPrice, String... yieldCurveNames) {
    Validate.notNull(dateTime, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    LocalDate date = dateTime.toLocalDate();
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    LocalDate transactionDateLocal = _transactionDate.toLocalDate();
    Validate.isTrue(!date.isAfter(getFixingPeriodStartDate().toLocalDate()), "Date is after last margin date");
    double referencePrice;
    if (transactionDateLocal.isBefore(date)) { // Transaction was before last margining.
      referencePrice = lastMarginPrice;
    } else { // Transaction is today
      referencePrice = _transactionPrice;
    }
    final String discountingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final double lastTradingTime = TimeCalculator.getTimeBetween(dateTime, getLastTradingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate());
    InterestRateFuture future = new InterestRateFuture(lastTradingTime, _iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, _fixingPeriodAccrualFactor, referencePrice, _notional,
        _paymentAccrualFactor, _quantity, _name, discountingCurveName, forwardCurveName);
    return future;
  }

  @Override
  public InstrumentDerivative toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    throw new UnsupportedOperationException("The method toDerivative of " + this.getClass().getSimpleName() + " does not support the two argument method (without margin price data).");
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitInterestRateFutureSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitInterestRateFutureSecurityDefinition(this);
  }

  @Override
  public String toString() {
    String result = "IRFuture Security: " + _name;
    result += " Last trading date: " + _lastTradingDate.toString();
    result += " Ibor Index: " + _iborIndex.getName();
    result += " Notional: " + _notional;
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _fixingPeriodEndDate.hashCode();
    result = prime * result + _fixingPeriodStartDate.hashCode();
    result = prime * result + _iborIndex.hashCode();
    result = prime * result + _lastTradingDate.hashCode();
    result = prime * result + _name.hashCode();
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    InterestRateFutureDefinition other = (InterestRateFutureDefinition) obj;
    if (!ObjectUtils.equals(_iborIndex, other._iborIndex)) {
      return false;
    }
    if (!ObjectUtils.equals(_lastTradingDate, other._lastTradingDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentAccrualFactor) != Double.doubleToLongBits(other._paymentAccrualFactor)) {
      return false;
    }
    return true;
  }

}
