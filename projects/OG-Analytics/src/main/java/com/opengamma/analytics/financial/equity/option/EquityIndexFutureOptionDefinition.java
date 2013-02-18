/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.equity.future.definition.EquityFutureDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class EquityIndexFutureOptionDefinition implements InstrumentDefinition<EquityIndexFutureOption> {
  /** The expiry date */
  private final ZonedDateTime _expiryDate;
  /** The underlying equity future */
  private final EquityFutureDefinition _underlying;
  /** The strike */
  private final double _strike;
  /** The exercise type */
  private final ExerciseDecisionType _exerciseType;
  /** Is the option a put or call */
  private final boolean _isCall;

  /**
   * @param expiryDate The expiry date, not null
   * @param underlying The underlying equity future, not null
   * @param strike The strike, greater than zero
   * @param exerciseType The exercise type, not null
   * @param isCall true if call, false if put
   */
  public EquityIndexFutureOptionDefinition(final ZonedDateTime expiryDate, final EquityFutureDefinition underlying, final double strike, final ExerciseDecisionType exerciseType,
      final boolean isCall) {
    ArgumentChecker.notNull(expiryDate, "expiry date");
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNegativeOrZero(strike, "strike");
    ArgumentChecker.notNull(exerciseType, "exercise type");
    _expiryDate = expiryDate;
    _underlying = underlying;
    _strike = strike;
    _exerciseType = exerciseType;
    _isCall = isCall;
  }

  public ZonedDateTime getExpiryDate() {
    return _expiryDate;
  }

  public EquityFutureDefinition getUnderlying() {
    return _underlying;
  }

  public double getStrike() {
    return _strike;
  }

  public ExerciseDecisionType getExerciseType() {
    return _exerciseType;
  }

  public boolean isCall() {
    return _isCall;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_exerciseType == null) ? 0 : _exerciseType.hashCode());
    result = prime * result + ((_expiryDate == null) ? 0 : _expiryDate.hashCode());
    result = prime * result + (_isCall ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_underlying == null) ? 0 : _underlying.hashCode());
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
    if (!(obj instanceof EquityIndexFutureOptionDefinition)) {
      return false;
    }
    final EquityIndexFutureOptionDefinition other = (EquityIndexFutureOptionDefinition) obj;
    if (_exerciseType != other._exerciseType) {
      return false;
    }
    if (_expiryDate == null) {
      if (other._expiryDate != null) {
        return false;
      }
    } else if (!_expiryDate.equals(other._expiryDate)) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    if (_underlying == null) {
      if (other._underlying != null) {
        return false;
      }
    } else if (!_underlying.equals(other._underlying)) {
      return false;
    }
    return true;
  }

  @Override
  public EquityIndexFutureOption toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    return null;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return null;
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return null;
  }



}

