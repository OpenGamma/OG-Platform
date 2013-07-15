/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition.twoasset;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.EuropeanExerciseFunction;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionExerciseFunction;
import com.opengamma.analytics.financial.model.option.definition.OptionPayoffFunction;
import com.opengamma.util.time.Expiry;

/**
 * Defines a European-style two-asset correlation option.
 * <p>
 * The payoff of a two-asset correlation call option is:
 * $$
 * \begin{eqnarray*}
 * max\left(S_2 - P, 0\right) \quad\quad\text{if}\quad S_1 > K
 * \end{eqnarray*}
 * $$
 * and 0 otherwise. The payoff of a put is:
 * $$
 * \begin{eqnarray*}
 * max\left(P - S_2, 0\right) \quad\quad\text{if}\quad S_1 < K
 * \end{eqnarray*}
 * $$
 * and 0 otherwise, where $K$ is the strike, $P$ is the payout level, $S_1$ is
 * the spot price of the first underlying and $S_2$ is the spot price of the
 * second underlying.
 */
public class TwoAssetCorrelationOptionDefinition extends OptionDefinition {
  private final OptionExerciseFunction<StandardTwoAssetOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  private final OptionPayoffFunction<StandardTwoAssetOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardTwoAssetOptionDataBundle>() {

    @Override
    public double getPayoff(final StandardTwoAssetOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data, "data");
      final double s1 = data.getFirstSpot();
      final double s2 = data.getSecondSpot();
      final double k = getStrike();
      final double p = getPayoutLevel();
      if (isCall()) {
        return s1 > k ? Math.max(s2 - p, 0) : 0;
      }
      return s1 < k ? Math.max(p - s2, 0) : 0;
    }
  };
  private final double _payoutLevel;

  /**
   * 
   * @param strike The strike
   * @param expiry The expiry
   * @param isCall Is the option a call
   * @param payoutLevel The payout level of the option
   */
  public TwoAssetCorrelationOptionDefinition(final double strike, final Expiry expiry, final boolean isCall, final double payoutLevel) {
    super(strike, expiry, isCall);
    _payoutLevel = payoutLevel;
  }

  /**
   * The exercise function of this option is European (see {@link EuropeanExerciseFunction})
   * @return The exercise function
   */
  @SuppressWarnings("unchecked")
  @Override
  public OptionExerciseFunction<StandardTwoAssetOptionDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  /**
   * @return The payoff function
   */
  @SuppressWarnings("unchecked")
  @Override
  public OptionPayoffFunction<StandardTwoAssetOptionDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }

  /**
   * 
   * @return The payout level
   */
  public double getPayoutLevel() {
    return _payoutLevel;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_payoutLevel);
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
    final TwoAssetCorrelationOptionDefinition other = (TwoAssetCorrelationOptionDefinition) obj;
    if (Double.doubleToLongBits(_payoutLevel) != Double.doubleToLongBits(other._payoutLevel)) {
      return false;
    }
    return true;
  }

}
