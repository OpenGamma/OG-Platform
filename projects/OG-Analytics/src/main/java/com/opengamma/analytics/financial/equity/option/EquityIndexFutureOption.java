/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexFuture;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;

/**
 * An equity index future option.
 */
public class EquityIndexFutureOption implements InstrumentDerivative {
  /** The time to expiry in years */
  private final double _expiry;
  /** The underlying index future */
  private final EquityIndexFuture _underlying;
  /** The strike */
  private final double _strike;
  /** The exercise type */
  private final ExerciseDecisionType _exerciseType;
  /** Is the option a call or put */
  private final boolean _isCall;
  /** The point value of the option */
  private final double _pointValue;
  /** The reference price is the transaction price on the transaction date and the last close price afterward */
  private final double _referencePrice;
  
  /**
   * @param expiry The time to expiry in years, greater than zero.
   * @param underlying The underlying equity index future, not null
   * @param strike The strike, greater than zero
   * @param exerciseType The exercise type, not null
   * @param isCall true if the option is a call, false if the option is a put
   * @param pointValue The point value of the option
   * @param referencePrice last close price (margin price) except on trade date on which it is the trade price
   */
  public EquityIndexFutureOption(final double expiry, final EquityIndexFuture underlying, final double strike, final ExerciseDecisionType exerciseType, final boolean isCall,
      final double pointValue, double referencePrice) {
    if (expiry < 0.0) {
      throw new OpenGammaRuntimeException("Expired");
    }
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNegativeOrZero(strike, "strike");
    ArgumentChecker.notNull(exerciseType, "exercise type");
    _expiry = expiry;
    _underlying = underlying;
    _strike = strike;
    _exerciseType = exerciseType;
    _isCall = isCall;
    _pointValue = pointValue;
    _referencePrice = referencePrice;
  }

  /**
   * Gets the time to expiry.
   * @return The time to expiry
   */
  public double getExpiry() {
    return _expiry;
  }

  /**
   * Gets the underlying equity index future.
   * @return The underlying
   */
  public EquityIndexFuture getUnderlying() {
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
   * Is the option a put or call.
   * @return true if the option is a call
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
   * Gets the reference price, the trade price on trade date. or the last close price thereafter.
   * @return The reference price
   */
  public double getReferencePrice() {
    return _referencePrice;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityIndexFutureOption(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityIndexFutureOption(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _exerciseType.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_expiry);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_isCall ? 1231 : 1237);
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlying.hashCode();
    temp = Double.doubleToLongBits(_pointValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_referencePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));   
    return result;
  }


  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EquityIndexFutureOption)) {
      return false;
    }
    final EquityIndexFutureOption other = (EquityIndexFutureOption) obj;
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
    if (Double.compare(_expiry, other._expiry) != 0) {
      return false;
    }
    if (Double.compare(_pointValue, other._pointValue) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_underlying, other._underlying)) {
      return false;
    }
    return true;
  }



}
