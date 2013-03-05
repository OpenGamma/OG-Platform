/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.util.time.Expiry;

/**
 * A capped power option is a power option with a cap on the maximum payoff.
 * <p>
 * The exercise style is European. The payoff of these options is:
 * $$
 * \begin{align*}
 * c &= \min(\max(S^i - K, 0), C) \\
 * p &= \min(\max(K - S^i, 0), C)
 * \end{align*}
 * $$
 * where $K$ is the strike, $i$ is the power ($i > 0$), $C$ is the cap ($C > 0$)
 * and $S$ is the spot.
 */

public class CappedPowerOptionDefinition extends OptionDefinition {
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data);
      final double spot = data.getSpot();
      return isCall() ? Math.min(Math.max(Math.pow(spot, getPower()) - getStrike(), 0), getCap()) : Math.min(Math.max(getStrike() - Math.pow(spot, getPower()), 0), getCap());
    }
  };
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  private final double _power;
  private final double _cap;

  /**
   * 
   * @param strike The strike
   * @param expiry The expiry
   * @param power The power, not negative
   * @param cap The cap, not negative
   * @param isCall Is the option a put or call
   */
  public CappedPowerOptionDefinition(final double strike, final Expiry expiry, final double power, final double cap, final boolean isCall) {
    super(strike, expiry, isCall);
    Validate.isTrue(power > 0, "power must be > 0");
    Validate.isTrue(cap > 0, "cap must be > 0");
    if (!isCall && cap > strike) {
      throw new IllegalArgumentException("Cannot have cap larger than strike for a put");
    }
    _power = power;
    _cap = cap;
  }

  /**
   * @return The power.
   */
  public double getPower() {
    return _power;
  }

  /**
   * @return The cap.
   */
  public double getCap() {
    return _cap;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OptionExerciseFunction<StandardOptionDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OptionPayoffFunction<StandardOptionDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_cap);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_power);
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
    final CappedPowerOptionDefinition other = (CappedPowerOptionDefinition) obj;
    if (Double.doubleToLongBits(_cap) != Double.doubleToLongBits(other._cap)) {
      return false;
    }
    if (Double.doubleToLongBits(_power) != Double.doubleToLongBits(other._power)) {
      return false;
    }
    return true;
  }
}
