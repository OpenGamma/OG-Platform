/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.core.config.Config;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Holds the range of X for a future price curve (to be used with volatility surfaces).
 * @param <X> Type of the x-data 
 */
@Config(description = "Future price curve definition")
public class FuturePriceCurveDefinition<X> {

  /**
   * The definition name.
   */
  private String _name;
  /**
   * The target.
   */
  private UniqueIdentifiable _target;
  /**
   * The definition values.
   */
  private X[] _xs;

  public FuturePriceCurveDefinition(final String name, final UniqueIdentifiable target, final X[] xs) {
    Validate.notNull(name, "name");
    Validate.notNull(target, "target");
    Validate.notNull(xs, "xs");
    _name = name;
    _target = target;
    _xs = xs;
  }

  public X[] getXs() {
    return _xs;
  }

  public String getName() {
    return _name;
  }

  public UniqueIdentifiable getTarget() {
    return _target;
  }

  @Override
  public int hashCode() {
    return getTarget().hashCode() * getName().hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof FuturePriceCurveDefinition)) {
      return false;
    }
    final FuturePriceCurveDefinition<?> other = (FuturePriceCurveDefinition<?>) o;
    return other.getTarget().equals(getTarget()) &&
           other.getName().equals(getName()) &&
           Arrays.equals(other.getXs(), getXs());
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
