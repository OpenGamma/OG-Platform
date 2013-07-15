/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Expiry;

/**
 */

public class EuropeanStandardBarrierOptionDefinition extends OptionDefinition {
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction;
  private final Barrier _barrier;
  private final double _rebate;

  public EuropeanStandardBarrierOptionDefinition(final double strike, final Expiry expiry, final boolean isCall, final Barrier barrier) {
    this(strike, expiry, isCall, barrier, 0);
  }

  public EuropeanStandardBarrierOptionDefinition(final double strike, final Expiry expiry, final boolean isCall, final Barrier barrier, final double rebate) {
    super(strike, expiry, isCall);
    Validate.notNull(barrier);
    ArgumentChecker.notNegative(rebate, "rebate");
    _barrier = barrier;
    _payoffFunction = new MyOptionPayoffFunction(barrier, new EuropeanVanillaOptionDefinition(strike, expiry, isCall), rebate);
    _rebate = rebate;
  }

  public Barrier getBarrier() {
    return _barrier;
  }

  public double getRebate() {
    return _rebate;
  }

  @Override
  public OptionExerciseFunction<StandardOptionDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  @Override
  public OptionPayoffFunction<StandardOptionDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }

  @Override
  public String toString() {
    return _barrier.getBarrierType() + " and " + _barrier.getKnockType() + " " + (isCall() ? "call" : "put");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_barrier == null) ? 0 : _barrier.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_rebate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final EuropeanStandardBarrierOptionDefinition other = (EuropeanStandardBarrierOptionDefinition) obj;
    return ObjectUtils.equals(_barrier, other._barrier) && Double.doubleToLongBits(_rebate) == Double.doubleToLongBits(other._rebate);
  }

  //TODO promote to its own class
  private static final class MyOptionPayoffFunction implements OptionPayoffFunction<StandardOptionDataBundle> {
    private boolean _isAlive;
    private final Barrier _b;
    private final EuropeanVanillaOptionDefinition _vanillaOption;
    private final double _r;

    public MyOptionPayoffFunction(final Barrier barrier, final EuropeanVanillaOptionDefinition vanillaOption, final double rebate) {
      _isAlive = barrier.getKnockType() == KnockType.IN ? false : true;
      _b = barrier;
      _vanillaOption = vanillaOption;
      _r = rebate;
    }

    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      final double spot = data.getSpot();
      if (_b.getKnockType() == KnockType.IN) {
        if (_b.getBarrierType() == BarrierType.DOWN && spot <= _b.getBarrierLevel()) {
          _isAlive = true;
        } else if (_b.getBarrierType() == BarrierType.UP && spot >= _b.getBarrierLevel()) {
          _isAlive = true;
        }
      } else {
        if (_b.getBarrierType() == BarrierType.DOWN && spot < _b.getBarrierLevel()) {
          _isAlive = false;
        } else if (_b.getBarrierType() == BarrierType.UP && spot > _b.getBarrierLevel()) {
          _isAlive = false;
        }
      }
      return _isAlive ? _vanillaOption.getPayoffFunction().getPayoff(data, optionPrice) : _r;
    }
  }
}
