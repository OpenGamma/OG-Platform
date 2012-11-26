/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.derivative;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.util.ArgumentChecker;

import org.apache.commons.lang.ObjectUtils;

/**
 * Abstract commodity future derivative.
 *
 * @param <T>
 */
public abstract class CommodityFutureOption<T extends CommodityFuture> implements InstrumentDerivative {
  /** Time (in years as a double) until the date-time at which the future expires */
  private final double _expiry;
  /** Identifier of the underlying commodity */
  private final T _underlying;
  /** Strike price */
  private final double _strike;
  /** Exercise type - European or American */
  private final ExerciseDecisionType _exerciseType;
  /** Call if true, Put if false */
  private final boolean _isCall;

  /**
   * @param expiry Time (in years as a double) until the date-time at which the future expires
   * @param underlying Underlying future
   * @param strike Strike price
   * @param exerciseType Exercise type - European or American
   * @param isCall Call if true, Put if false
   */
  public CommodityFutureOption(final double expiry, final T underlying, final double strike, final ExerciseDecisionType exerciseType, final boolean isCall) {
    ArgumentChecker.isTrue(expiry >= 0, "time to expiry must be positive");

    _expiry = expiry;
    _underlying = underlying;
    _strike = strike;
    _exerciseType = exerciseType;
    _isCall = isCall;
  }

  /**
   * Gets the expiry.
   * @return the expiry
   */
  public double getExpiry() {
    return _expiry;
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
    result = prime * result + _underlying.hashCode();
    result = prime * result + _exerciseType.hashCode();
    result = prime * result + (_isCall ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_expiry);
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
    if (!(obj instanceof CommodityFutureOption)) {
      return false;
    }
    final CommodityFutureOption<?> other = (CommodityFutureOption<?>) obj;
    if (!ObjectUtils.equals(_underlying, other._underlying)) {
      return false;
    }
    if (!ObjectUtils.equals(_exerciseType, other._exerciseType)) {
      return false;
    }
    if (Double.compare(_expiry, other._expiry) != 0) {
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
