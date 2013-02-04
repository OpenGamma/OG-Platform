/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class EuropeanOptionOnEuropeanVanillaOptionDefinition extends OptionDefinition {
  private static final BlackScholesMertonModel UNDERLYING_MODEL = new BlackScholesMertonModel();
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      Validate.notNull(data, "data");
      final double underlyingPrice = UNDERLYING_MODEL.getPricingFunction(_underlyingOption).evaluate(data);
      return isCall() ? Math.max(underlyingPrice - getStrike(), 0) : Math.max(getStrike() - underlyingPrice, 0);
    }
  };
  private final EuropeanVanillaOptionDefinition _underlyingOption;

  public EuropeanOptionOnEuropeanVanillaOptionDefinition(final double strike, final Expiry expiry, final boolean isCall, final double underlyingStrike, final Expiry underlyingExpiry,
      final boolean isUnderlyingCall) {
    this(strike, expiry, isCall, new EuropeanVanillaOptionDefinition(underlyingStrike, underlyingExpiry, isUnderlyingCall));
  }

  public EuropeanOptionOnEuropeanVanillaOptionDefinition(final double strike, final Expiry expiry, final boolean isCall, final EuropeanVanillaOptionDefinition underlyingOption) {
    super(strike, expiry, isCall);
    Validate.notNull(underlyingOption, "underlying definition");
    if (expiry.getExpiry().isAfter(underlyingOption.getExpiry().getExpiry())) {
      throw new IllegalArgumentException("Underlying option expiry must be after option expiry");
    }
    _underlyingOption = underlyingOption;
  }

  @Override
  public OptionExerciseFunction<StandardOptionDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  @Override
  public OptionPayoffFunction<StandardOptionDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }

  public OptionDefinition getUnderlyingOption() {
    return _underlyingOption;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_underlyingOption == null) ? 0 : _underlyingOption.hashCode());
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
    final EuropeanOptionOnEuropeanVanillaOptionDefinition other = (EuropeanOptionOnEuropeanVanillaOptionDefinition) obj;
    return ObjectUtils.equals(_underlyingOption, other._underlyingOption);
  }

}
