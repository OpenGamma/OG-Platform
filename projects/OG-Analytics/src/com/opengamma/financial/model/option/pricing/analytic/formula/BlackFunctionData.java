/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.BlackOptionDataBundle;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;

/**
 * 
 */
public class BlackFunctionData {
  private final double _forward;
  private final double _discountFactor;
  private final double _blackVolatility;

  public BlackFunctionData(final double forward, final double discountFactor) {
    this(forward, discountFactor, 0);
  }

  public BlackFunctionData(final double forward, final double discountFactor, final double blackVolatility) {
    Validate.isTrue(discountFactor <= 1 && discountFactor > 0, "discount factor must be <= 1 and > 0");
    _forward = forward;
    _discountFactor = discountFactor;
    _blackVolatility = blackVolatility;
  }

  public double getForward() {
    return _forward;
  }

  public double getDiscountFactor() {
    return _discountFactor;
  }

  public double getBlackVolatility() {
    return _blackVolatility;
  }

  public static BlackFunctionData fromDataBundle(final BlackOptionDataBundle bundle, final EuropeanVanillaOptionDefinition definition) {
    Validate.notNull(bundle, "bundle");
    Validate.notNull(definition, "definition");
    final double t = definition.getTimeToExpiry(bundle.getDate());
    final double k = definition.getStrike();
    return new BlackFunctionData(bundle.getForward(), bundle.getDiscountFactor(t), bundle.getVolatility(t, k));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_discountFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_forward);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_blackVolatility);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BlackFunctionData other = (BlackFunctionData) obj;
    if (Double.doubleToLongBits(_discountFactor) != Double.doubleToLongBits(other._discountFactor)) {
      return false;
    }
    if (Double.doubleToLongBits(_forward) != Double.doubleToLongBits(other._forward)) {
      return false;
    }
    return Double.doubleToLongBits(_blackVolatility) == Double.doubleToLongBits(other._blackVolatility);
  }

}
