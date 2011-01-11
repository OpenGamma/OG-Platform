/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class BondOptionDefinition extends OptionDefinition {
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      final double spot = data.getSpot();
      return isCall() ? Math.max(0, spot - getStrike()) : Math.max(0, getStrike() - spot);
    }
  };
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<StandardOptionDataBundle>();
  private final Expiry _bondMaturity;

  public BondOptionDefinition(final double strike, final Expiry expiry, final boolean isCall, final Expiry bondMaturity) {
    super(strike, expiry, isCall);
    _bondMaturity = bondMaturity;
  }

  public double getTimeToBondMaturity(final ZonedDateTime date) {
    return DateUtil.getDifferenceInYears(date, getBondMaturity().getExpiry());
  }

  public Expiry getBondMaturity() {
    return _bondMaturity;
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
    result = prime * result + (_bondMaturity == null ? 0 : _bondMaturity.hashCode());
    result = prime * result + (_exerciseFunction == null ? 0 : _exerciseFunction.hashCode());
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
    final BondOptionDefinition other = (BondOptionDefinition) obj;
    if (_bondMaturity == null) {
      if (other._bondMaturity != null) {
        return false;
      }
    } else if (!_bondMaturity.equals(other._bondMaturity)) {
      return false;
    }
    if (_exerciseFunction == null) {
      if (other._exerciseFunction != null) {
        return false;
      }
    } else if (!_exerciseFunction.equals(other._exerciseFunction)) {
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
