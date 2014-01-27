/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Triple;

/**
 * Hold the valid range of X, Y and Z for a cube.
 * @param <X> Type of the x-data
 * @param <Y> Type of the y-data
 * @param <Z> Type of the z-data
 */
@Config(description = "Volatility cube definition", group = ConfigGroups.VOL)
public class VolatilityCubeDefinition<X, Y, Z> {
  private final String _name;
  private final UniqueIdentifiable _target;
  private final X[] _xs;
  private final Y[] _ys;
  private final Z[] _zs;

  // TODO: can we hold a target specification here instead of the UID so that we can preserve any type information
  public VolatilityCubeDefinition(final String name,
                                  final UniqueIdentifiable target,
                                  final X[] xs,
                                  final Y[] ys,
                                  final Z[] zs) {
    Validate.notNull(name, "Name");
    Validate.notNull(target, "Target");
    Validate.notNull(xs, "xs");
    Validate.notNull(ys, "ys");
    Validate.notNull(zs, "zs");
    _name = name;
    _target = target;
    _xs = xs;
    _ys = ys;
    _zs = zs;
  }

  public X[] getXs() {
    return _xs;
  }

  public Y[] getYs() {
    return _ys;
  }

  public Z[] getZs() {
    return _zs;
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
    if (!(o instanceof VolatilityCubeDefinition)) {
      return false;
    }
    final VolatilityCubeDefinition<?, ?, ?> other = (VolatilityCubeDefinition<?, ?, ?>) o;
    return other.getTarget().equals(getTarget()) &&
        other.getName().equals(getName()) &&
        Arrays.equals(other.getXs(), getXs()) &&
        Arrays.equals(other.getYs(), getYs()) &&
        Arrays.equals(other.getZs(), getZs());
  }

  @Override
  public int hashCode() {
    return getTarget().hashCode() * getName().hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public Iterable<Triple<X, Y, Z>> getAllPoints() {
    final Iterator<Triple<X, Y, Z>> iterator = new Iterator<Triple<X, Y, Z>>() {
      private int _idx;
      @Override
      public synchronized boolean hasNext() {
        return _idx < _xs.length;
      }

      @Override
      public synchronized Triple<X, Y, Z> next() {
        int idx = _idx++;
        return Triple.of(_xs[idx], _ys[idx], _zs[idx]);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("this iterator is immutable.");
      }
    };
    return new Iterable<Triple<X, Y, Z>>() {
      @Override
      public Iterator<Triple<X, Y, Z>> iterator() {
        return iterator;
      }
    };
  }
}
