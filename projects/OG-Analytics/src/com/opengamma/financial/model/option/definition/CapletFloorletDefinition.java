/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class CapletFloorletDefinition extends OptionDefinition {
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<StandardOptionDataBundle>();
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double r) {
      if (isCap() == true) {
        return Math.max(r - getStrike(), 0);
      }
      return Math.max(getStrike() - r, 0);
    }
  };
  private final boolean _isCap;
  private final Tenor _capletTenor;

  public CapletFloorletDefinition(final double strike, final boolean isCap, final Tenor capletTenor) {
    super(strike, null, null);
    _isCap = isCap;
    _capletTenor = capletTenor;
  }

  public boolean isCap() {
    return _isCap;
  }

  public Tenor getCapletTenor() {
    return _capletTenor;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionExerciseFunction<StandardOptionDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionPayoffFunction<StandardOptionDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_capletTenor == null ? 0 : _capletTenor.hashCode());
    result = prime * result + (_exerciseFunction == null ? 0 : _exerciseFunction.hashCode());
    result = prime * result + (_isCap ? 1231 : 1237);
    result = prime * result + (_payoffFunction == null ? 0 : _payoffFunction.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CapletFloorletDefinition other = (CapletFloorletDefinition) obj;
    if (_capletTenor == null) {
      if (other._capletTenor != null) {
        return false;
      }
    } else if (!_capletTenor.equals(other._capletTenor)) {
      return false;
    }
    if (_exerciseFunction == null) {
      if (other._exerciseFunction != null) {
        return false;
      }
    } else if (!_exerciseFunction.equals(other._exerciseFunction)) {
      return false;
    }
    if (_isCap != other._isCap) {
      return false;
    }
    if (_payoffFunction == null) {
      if (other._payoffFunction != null) {
        return false;
      }
    } else if (!_payoffFunction.equals(other._payoffFunction)) {
      return false;
    }
    return true;
  }

}
