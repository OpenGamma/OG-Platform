/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Expiry;

/**
 * Class defining a supershare option.
 * <p>
 * Supershare options have European-style exercise with payoff
 * $$
 * \begin{align*}
 * \mathrm{payoff} =
 * \begin{cases}
 * \frac{S}{K_L} \quad & \mathrm{if} \quad K_L \leq S \leq K_H\\
 * 0\quad & \mathrm{otherwise}
 * \end{cases}
 * \end{align*}
 * $$
 * where $K_L$ is the lower bound, $K_H$ the upper bound and $S$ the spot.
 */
public class SupershareOptionDefinition extends OptionDefinition {
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data, "data");
      final double s = data.getSpot();
      return ArgumentChecker.isInRangeExcludingHigh(_lowerBound, _upperBound, s) ? s / _lowerBound : 0;
    }
  };
  private final double _lowerBound;
  private final double _upperBound;

  /**
   * @param expiry The expiry
   * @param lowerBound The lower bound
   * @param upperBound The upper bound
   */
  public SupershareOptionDefinition(final Expiry expiry, final double lowerBound, final double upperBound) {
    super(null, expiry, null);
    Validate.isTrue(lowerBound >= 0, "lower bound must be >= 0");
    Validate.isTrue(upperBound >= 0, "upper bound must be >= 0");
    Validate.isTrue(upperBound > lowerBound, "Lower bound must be less than upper bound");
    _lowerBound = lowerBound;
    _upperBound = upperBound;
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

  /**
   * @return The lower bound
   */
  public double getLowerBound() {
    return _lowerBound;
  }

  /**
   * @return The upper bound
   */
  public double getUpperBound() {
    return _upperBound;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_lowerBound);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_upperBound);
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
    final SupershareOptionDefinition other = (SupershareOptionDefinition) obj;
    if (Double.doubleToLongBits(_lowerBound) != Double.doubleToLongBits(other._lowerBound)) {
      return false;
    }
    if (Double.doubleToLongBits(_upperBound) != Double.doubleToLongBits(other._upperBound)) {
      return false;
    }
    return true;
  }

}
