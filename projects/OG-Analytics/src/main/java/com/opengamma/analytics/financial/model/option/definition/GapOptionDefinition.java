/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.util.time.Expiry;

/**
 * Class defining a gap option.
 * <p>
 * Gap options have European-style exercise with payoff
 * $$
 * \begin{align*}
 * \mathrm{payoff} =
 * \begin{cases}
 * 0 \quad & \mathrm{if} \quad S \leq K_1\\
 * S - K_2 \quad & \mathrm{otherwise}
 * \end{cases}
 * \end{align*}
 * $$
 * for a call and
 * $$
 * \begin{align*}
 * \mathrm{payoff} =
 * \begin{cases}
 * 0 \quad & \mathrm{if} \quad S \geq K_1\\
 * K_2 - S \quad & \mathrm{otherwise}
 * \end{cases}
 * \end{align*}
 * $$
 * for a put, where $K_1$ is the strike, $K_2$ is the payoff strike and $S$ is
 * the spot.
 */
public class GapOptionDefinition extends OptionDefinition {
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data);
      final double s = data.getSpot();
      final double k = getStrike();
      final double x = getPayoffStrike();
      return isCall() ? s <= k ? 0 : s - x : s >= k ? 0 : x - s;
    }
  };
  private final double _payoffStrike;

  /**
   * @param strike The strike
   * @param expiry The expiry
   * @param isCall Is the option a call or put
   * @param payoffStrike The payoff strike of the option, greater than zero
   */
  public GapOptionDefinition(final double strike, final Expiry expiry, final boolean isCall, final double payoffStrike) {
    super(strike, expiry, isCall);
    Validate.isTrue(payoffStrike >= 0, "payoff strike");
    _payoffStrike = payoffStrike;
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

  public double getPayoffStrike() {
    return _payoffStrike;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_payoffStrike);
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
    final GapOptionDefinition other = (GapOptionDefinition) obj;
    if (Double.doubleToLongBits(_payoffStrike) != Double.doubleToLongBits(other._payoffStrike)) {
      return false;
    }
    return true;
  }

}
