/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract commodity future option definition.
 *
 * @param <T> concrete underlying derivative definition
 * @param <U> concrete underlying derivative
 */
public abstract class CommodityFutureOptionDefinition<T extends CommodityFutureDefinition<?>, U extends InstrumentDerivative> implements InstrumentDefinition<U> {
  /** Expiry date */
  private final ZonedDateTime _expiryDate;
  /** Identifier of the underlying commodity */
  private final T _underlying;
  /** Strike price */
  private final double _strike;
  /** Exercise type - European or American */
  private final ExerciseDecisionType _exerciseType;
  /** Call if true, Put if false */
  private final boolean _isCall;

  /**
   * Constructor for future options
   *
   * @param expiryDate is the time and the day that a particular delivery month of a futures contract stops trading, as well as the final settlement price for that contract.
   * @param underlying underlying
   * @param strike Strike price
   * @param exerciseType Exercise type - European or American
   * @param isCall Call if true, Put if false
   */
  public CommodityFutureOptionDefinition(final ZonedDateTime expiryDate, final T underlying, final double strike,
      final ExerciseDecisionType exerciseType, final boolean isCall) {
    ArgumentChecker.notNull(expiryDate, "expiry time");
    ArgumentChecker.notNull(exerciseType, "exercise Type");

    _expiryDate = expiryDate;
    _underlying = underlying;
    _strike = strike;
    _exerciseType = exerciseType;
    _isCall = isCall;
  }

  /**
   * Gets the expiryDate.
   * @return the expiryDate
   */
  public ZonedDateTime getExpiryDate() {
    return _expiryDate;
  }

  /**
   * Gets the underlying.
   * @return the underlying
   */
  public T getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the strike price
   * @return the strike.
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * Gets the exercise type
   * @return the exerciseType.
   */
  public ExerciseDecisionType getExerciseType() {
    return _exerciseType;
  }

  /**
   * @return True if the option is a Call, false if it is a Put.
   */
  public boolean isCall() {
    return _isCall;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _expiryDate.hashCode();
    result = prime * result + _underlying.hashCode();
    result = prime * result + _exerciseType.hashCode();
    result = prime * result + (_isCall ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommodityFutureOptionDefinition)) {
      return false;
    }
    final CommodityFutureOptionDefinition<?, ?> other = (CommodityFutureOptionDefinition<?, ?>) obj;
    if (!ObjectUtils.equals(_expiryDate, other._expiryDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlying, other._underlying)) {
      return false;
    }
    if (!ObjectUtils.equals(_exerciseType, other._exerciseType)) {
      return false;
    }
    if (Double.compare(_strike, other._strike) != 0) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    return true;
  }

}
