/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.equity.future.definition.IndexFutureDefinition;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexFuture;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class EquityIndexFutureOptionDefinition implements InstrumentDefinition<EquityIndexFutureOption> {
  /** The expiry date */
  private final ZonedDateTime _expiryDate;
  /** The underlying equity future */
  //TODO probably best to create a separate type for equity index futures
  private final IndexFutureDefinition _underlying;
  /** The strike */
  private final double _strike;
  /** The exercise type */
  private final ExerciseDecisionType _exerciseType;
  /** Is the option a put or call */
  private final boolean _isCall;
  /** The point value */
  private final double _pointValue;
  /** The reference price is the transaction price on the transaction date and the last close price afterward */
  private final double _referencePrice;

  /**
   * @param expiryDate The expiry date, not null
   * @param underlying The underlying equity future, not null
   * @param strike The strike, greater than zero
   * @param exerciseType The exercise type, not null
   * @param isCall true if call, false if put
   * @param pointValue The point value
   * @param referencePrice TODO
   */
  public EquityIndexFutureOptionDefinition(final ZonedDateTime expiryDate, final IndexFutureDefinition underlying, final double strike, final ExerciseDecisionType exerciseType,
      final boolean isCall, final double pointValue, double referencePrice) {
    ArgumentChecker.notNull(expiryDate, "expiry date");
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNegativeOrZero(strike, "strike");
    ArgumentChecker.notNull(exerciseType, "exercise type");
    _expiryDate = expiryDate;
    _underlying = underlying;
    _strike = strike;
    _exerciseType = exerciseType;
    _isCall = isCall;
    _pointValue = pointValue;
    _referencePrice = referencePrice;
  }

  /**
   * Gets the expiry date.
   * @return The expiry date
   */
  public ZonedDateTime getExpiryDate() {
    return _expiryDate;
  }

  /**
   * Gets the definition of the underlying.
   * @return The underlying definition
   */
  public IndexFutureDefinition getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the strike.
   * @return The strike
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * Gets the exercise type.
   * @return The exercise type
   */
  public ExerciseDecisionType getExerciseType() {
    return _exerciseType;
  }

  /**
   * Gets the option type (put or call)
   * @return true if the option is a call, false if the option is a put
   */
  public boolean isCall() {
    return _isCall;
  }

  /**
   * Gets the point value.
   * @return The point value
   */
  public double getPointValue() {
    return _pointValue;
  }

  /**
   * Gets the referencePrice.
   * @return the referencePrice
   */
  public double getReferencePrice() {
    return _referencePrice;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _exerciseType.hashCode();
    result = prime * result + _expiryDate.hashCode();
    result = prime * result + (_isCall ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_pointValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_referencePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlying.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EquityIndexFutureOptionDefinition)) {
      return false;
    }
    final EquityIndexFutureOptionDefinition other = (EquityIndexFutureOptionDefinition) obj;
    if (_exerciseType != other._exerciseType) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    if (Double.compare(_strike, other._strike) != 0) {
      return false;
    }
    if (Double.compare(_referencePrice, other._referencePrice) != 0) {
      return false;
    }
    if (Double.compare(_pointValue, other._pointValue) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_expiryDate, other._expiryDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlying, other._underlying)) {
      return false;
    }
    return true;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names.
   */
  @Deprecated
  @Override
  public EquityIndexFutureOption toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException(this.getClass().getCanonicalName());
  }

  @Override
  public EquityIndexFutureOption toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.inOrderOrEqual(date.toLocalDate(), _expiryDate.toLocalDate(), "valuation date", "expiry");
    double timeToExpiry = TimeCalculator.getTimeBetween(date, getExpiryDate());
    if (timeToExpiry == 0) { // Day of expiration: Still time value if option has not expired.
      // REVIEW Stephen and Casey - This essentially assumes an Expiry with accuracy of 1 day.
      // The intended behaviour is that an option is still alive on the expiry date
      timeToExpiry = 0.0015; // Approximately half a day
    }
    double timeToFutureFixing = TimeCalculator.getTimeBetween(date, _underlying.getExpiryDate());
    if (timeToFutureFixing == 0) {
      timeToFutureFixing = 0.0015;
    }
    double timeToFutureDelivery = TimeCalculator.getTimeBetween(date, _underlying.getSettlementDate());
    if (timeToFutureDelivery == 0) {
      timeToFutureDelivery = 0.0015;
    }
    final double futureStrike = _underlying.getStrikePrice();
    final Currency currency = _underlying.getCurrency();
    final double unitValue = _underlying.getUnitAmount();
    final EquityIndexFuture underlying = new EquityIndexFuture(timeToFutureFixing, timeToFutureDelivery, futureStrike, currency, unitValue);
    return new EquityIndexFutureOption(timeToExpiry, underlying, _strike, _exerciseType, _isCall, _pointValue, _referencePrice);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityIndexFutureOptionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityIndexFutureOptionDefinition(this);
  }



}

