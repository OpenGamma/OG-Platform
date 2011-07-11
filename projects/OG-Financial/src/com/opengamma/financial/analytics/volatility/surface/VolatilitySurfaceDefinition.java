/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.util.money.Currency;

/**
 * Hold the valid range of X and Y for a surface.  E.g. VolatilitySurfaceDefinition<Tenor, Double> (tenor vs strike, z is volatility), where Tenors go from 1YR..10YR, 
 * strikes from 220.0d to 240.0d with deltas of 5
 * @param <X> Type of the x-data
 * @param <Y> Type of the y-data
 */
public class VolatilitySurfaceDefinition<X, Y> {
  private String _name;
  private Currency _currency;
  private X[] _xs;
  private Y[] _ys;

  public VolatilitySurfaceDefinition(final String name, final Currency currency, final X[] xs, final Y[] ys) {
    Validate.notNull(name, "Name");
    Validate.notNull(currency, "Currency");
    Validate.notNull(xs, "xs");
    Validate.notNull(ys, "ys");
    _name = name;
    _currency = currency;
    _xs = xs;
    _ys = ys;
  }

  public X[] getXs() {
    return _xs;
  }

  public Y[] getYs() {
    return _ys;
  }

  public String getName() {
    return _name;
  }

  public Currency getCurrency() {
    return _currency;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof VolatilitySurfaceDefinition)) {
      return false;
    }
    final VolatilitySurfaceDefinition<?, ?> other = (VolatilitySurfaceDefinition<?, ?>) o;
    return other.getCurrency().equals(getCurrency()) &&
           other.getName().equals(getName()) &&
           Arrays.equals(other.getXs(), getXs()) &&
           Arrays.equals(other.getYs(), getYs());
  }

  @Override
  public int hashCode() {
    return getCurrency().hashCode() * getName().hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
