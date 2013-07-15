/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.util.time.Expiry;

/**
 * Class defining a cash-or-nothing option.
 * <p>
 * Cash-or-nothing options have European-style exercise with payoff
 * $$
 * \begin{align*}
 * \mathrm{payoff} =
 * \begin{cases}
 * 0 \quad & \mathrm{if} \quad S \leq K\\
 * P \quad & \mathrm{otherwise}
 * \end{cases}
 * \end{align*}
 * $$
 * and
 * $$
 * \begin{align*}
 * \mathrm{payoff} =
 * \begin{cases}
 * 0 \quad & \mathrm{if} \quad S \geq K\\
 * P \quad & \mathrm{otherwise}
 * \end{cases}
 * \end{align*}
 * $$
 * for a put, where $K$ is the strike, $P$ is the payment amount and $S$ is the spot.
 */
public class CashOrNothingOptionDefinition extends OptionDefinition {
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data, "data");
      final double s = data.getSpot();
      final double k = getStrike();
      return isCall() ? (s < k ? _payment : 0) : s > k ? _payment : 0;
    }

  };
  private final double _payment;

  /**
   * @param strike The strike
   * @param expiry The expiry
   * @param isCall Is the option a call or put
   * @param payment The payment amount, greater than zero
   */
  public CashOrNothingOptionDefinition(final double strike, final Expiry expiry, final boolean isCall, final double payment) {
    super(strike, expiry, isCall);
    Validate.isTrue(payment >= 0, "payment");
    _payment = payment;
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
   * @return The payment
   */
  public double getPayment() {
    return _payment;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_payment);
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
    final CashOrNothingOptionDefinition other = (CashOrNothingOptionDefinition) obj;
    if (Double.doubleToLongBits(_payment) != Double.doubleToLongBits(other._payment)) {
      return false;
    }
    return true;
  }

}
