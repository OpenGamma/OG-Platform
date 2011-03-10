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
  private final double _f;
  private final double _df;
  private final double _sigma;

  public BlackFunctionData(final double f, final double df, final double sigma) {
    Validate.isTrue(df <= 1 && df > 0, "discount factor must be <= 1 and > 0");
    _f = f;
    _df = df;
    _sigma = sigma;
  }

  public double getForward() {
    return _f;
  }

  public double getDiscountFactor() {
    return _df;
  }

  public double getBlackVolatility() {
    return _sigma;
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
    temp = Double.doubleToLongBits(_df);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_f);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_sigma);
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
    if (Double.doubleToLongBits(_df) != Double.doubleToLongBits(other._df)) {
      return false;
    }
    if (Double.doubleToLongBits(_f) != Double.doubleToLongBits(other._f)) {
      return false;
    }
    return Double.doubleToLongBits(_sigma) == Double.doubleToLongBits(other._sigma);
  }

}
