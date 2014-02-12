/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.io.Serializable;
import java.util.Iterator;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.PropertyDefinition;

import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.util.tuple.Triple;

/**
 * Hold the valid range of X, Y and Z for a cube.
 * @param <X> Type of the x-data
 * @param <Y> Type of the y-data
 * @param <Z> Type of the z-data
 */
@Config(description = "Volatility cube definition", group = ConfigGroups.VOL)
@BeanDefinition
public class VolatilityCubeDefinition<X, Y, Z> implements Bean, Serializable {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The definition name.
   */
  @PropertyDefinition(validate = "notNull")
  private final String _name;
  private final X[] _xs;
  private final Y[] _ys;
  private final Z[] _zs;

  public VolatilityCubeDefinition(final String name,
                                  final X[] xs,
                                  final Y[] ys,
                                  final Z[] zs) {
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
        final int idx = _idx++;
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
