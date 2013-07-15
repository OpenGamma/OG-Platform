/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.util.time.Expiry;

/**
 * Class defining an asset-or-nothing option.
 * <p>
 * Asset-or-nothing options have European-style exercise with payoff
 * $$
 * \begin{align*}
 * \mathrm{payoff} =
 * \begin{cases}
 * 0 \quad & \mathrm{if} \quad S \leq K\\\\
 * S \quad & \mathrm{otherwise}
 * \end{cases}
 * \end{align*}
 * $$
 * and
 * $$
 * \begin{align*}
 * \mathrm{payoff} =
 * \begin{cases}
 * 0 \quad & \mathrm{if} \quad S \geq K\\\\
 * S \quad & \mathrm{otherwise}
 * \end{cases}
 * \end{align*}
 * $$
 * for a put, where $K$ is the strike and $S$ is the spot.
 */
public class AssetOrNothingOptionDefinition extends OptionDefinition {
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data);
      final double s = data.getSpot();
      final double k = getStrike();
      return isCall() ? s <= k ? 0 : s : s >= k ? 0 : s;
    }
  };

  /**
   * @param strike The strike
   * @param expiry The expiry
   * @param isCall Is the option a call or put
   */
  public AssetOrNothingOptionDefinition(final double strike, final Expiry expiry, final boolean isCall) {
    super(strike, expiry, isCall);
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
}
