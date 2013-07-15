/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.util.time.Expiry;

/**
 * A simple chooser option gives the holder the right to choose whether the
 * option is to be a standard call or put (both with the same expiry) after a
 * certain time. The exercise style of the option, once the choice has been
 * made, is European.
 * <p>
 * The payoff of this option is:
 * $$
 * \begin{align*}
 * \mathrm{payoff} = \max(c_{BSM}(S, K, T_2), p_{BSM}(S, K, T_2))
 * \end{align*}
 * $$
 * where $c_{BSM}$ is the general Black-Scholes Merton call price, $c_{BSM}$ is
 * the general Black-Scholes Merton put price (see {@link BlackScholesMertonModel}),
 * $K$ is the strike, $S$ is the spot and $T_2$ is the time to expiry of the
 * underlying option.
 */

public class SimpleChooserOptionDefinition extends OptionDefinition {
  /** The payoff function */
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data);
      final double callPrice = BSM.getGreeks(getCallDefinition(), data, GREEKS).get(Greek.FAIR_PRICE);
      final double putPrice = BSM.getGreeks(getPutDefinition(), data, GREEKS).get(Greek.FAIR_PRICE);
      return Math.max(callPrice, putPrice);
    }
  };
  /** The exercise function */
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  /** The strike of the underlying option */
  private final double _underlyingStrike;
  /** The expiry of the underlying option */
  private final Expiry _underlyingExpiry;
  /** The underlying call */
  private final OptionDefinition _callDefinition;
  /** The underlying put */
  private final OptionDefinition _putDefinition;
  /** Black-Scholes Merton model */
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  /** The greeks that can be computed */
  private static final Set<Greek> GREEKS = Collections.singleton(Greek.FAIR_PRICE);

  /**
   * @param chooseDate The date when the choice is to be made (i.e. the chooser option expiry)
   * @param underlyingStrike The strike of the underlying option
   * @param underlyingExpiry The expiry of the underlying European option
   */
  public SimpleChooserOptionDefinition(final Expiry chooseDate, final double underlyingStrike, final Expiry underlyingExpiry) {
    super(null, chooseDate, null);
    Validate.notNull(underlyingExpiry);
    Validate.isTrue(underlyingStrike > 0, "underlying strike");
    if (underlyingExpiry.getExpiry().isBefore(chooseDate.getExpiry())) {
      throw new IllegalArgumentException("Underlying option expiry must be after the choice date");
    }
    _underlyingStrike = underlyingStrike;
    _underlyingExpiry = underlyingExpiry;
    _callDefinition = new EuropeanVanillaOptionDefinition(underlyingStrike, underlyingExpiry, true);
    _putDefinition = new EuropeanVanillaOptionDefinition(underlyingStrike, underlyingExpiry, false);
  }

  /**
   * @return The underlying call definition
   */
  public OptionDefinition getCallDefinition() {
    return _callDefinition;
  }

  /**
   * @return The underlying put definition
   */
  public OptionDefinition getPutDefinition() {
    return _putDefinition;
  }

  /**
   * @return The strike of the underlying option
   */
  public double getUnderlyingStrike() {
    return _underlyingStrike;
  }

  /**
   * @return The expiry of the underlying option
   */
  public Expiry getUnderlyingExpiry() {
    return _underlyingExpiry;
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
    result = prime * result + ((_underlyingExpiry == null) ? 0 : _underlyingExpiry.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_underlyingStrike);
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
    final SimpleChooserOptionDefinition other = (SimpleChooserOptionDefinition) obj;
    if (Double.doubleToLongBits(_underlyingStrike) != Double.doubleToLongBits(other._underlyingStrike)) {
      return false;
    }
    return ObjectUtils.equals(_underlyingExpiry, other._underlyingExpiry);
  }
}
