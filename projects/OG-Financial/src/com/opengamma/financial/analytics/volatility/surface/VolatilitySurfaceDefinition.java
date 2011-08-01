/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;

/**
 * Hold the valid range of X and Y for a surface.  E.g. VolatilitySurfaceDefinition<Tenor, Double> (tenor vs strike, z is volatility), where Tenors go from 1YR..10YR, 
 * strikes from 220.0d to 240.0d with deltas of 5
 * @param <X> Type of the x-data
 * @param <Y> Type of the y-data
 */
public class VolatilitySurfaceDefinition<X, Y> {
  private String _name;
  private UniqueIdentifiable _target;
  private X[] _xs;
  private Y[] _ys;

  public VolatilitySurfaceDefinition(final String name, final UniqueIdentifiable target, final X[] xs, final Y[] ys) {
    Validate.notNull(name, "Name");
    Validate.notNull(target, "Target");
    Validate.notNull(xs, "xs");
    Validate.notNull(ys, "ys");
    _name = name;
    _target = target;
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

  /**
   * @deprecated use getTarget()
   * @throws ClassCastException if target not a currency
   * @return currency assuming that the target is a currency
   */
  @Deprecated
  public Currency getCurrency() {
    return (Currency) _target;
  }

  public UniqueIdentifiable getTarget() {
    return _target;
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
    return other.getTarget().equals(getTarget()) &&
           other.getName().equals(getName()) &&
           Arrays.equals(other.getXs(), getXs()) &&
           Arrays.equals(other.getYs(), getYs());
  }

  @Override
  public int hashCode() {
    return getTarget().hashCode() * getName().hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
