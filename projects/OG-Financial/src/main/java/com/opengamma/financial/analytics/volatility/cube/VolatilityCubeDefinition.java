/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * Hold the valid range of X, Y and Z for a cube.
 * @param <X> Type of the x-data
 * @param <Y> Type of the y-data
 * @param <Z> Type of the z-data
 */
@Config(description = "Volatility cube definition", group = ConfigGroups.VOL)
public class VolatilityCubeDefinition<X, Y, Z> implements Serializable {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The definition name, not null
   */
  private String _name;

  /**
   * The x values, not null
   */
  private X[] _xs;

  /** 
   * The y values, not null
   */
  private Y[] _ys;

  /**
   * The z values, not null
   */
  private Z[] _zs;

  /**
   * @param name The definition name, not null
   * @param xs The x values, not null
   * @param ys The y values, not null
   * @param zs The z values, not null
   */
  public VolatilityCubeDefinition(final String name, final X[] xs, final Y[] ys, final Z[] zs) {
    setName(name);
    setXs(xs);
    setYs(ys);
    setZs(zs);
  }

  /**
   * Gets an iterator over all points in the definition.
   * @return The iterator
   */
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
        throw new UnsupportedOperationException("This iterator is immutable.");
      }
    };
    return new Iterable<Triple<X, Y, Z>>() {
      @Override
      public Iterator<Triple<X, Y, Z>> iterator() {
        return iterator;
      }
    };
  }

  /**
   * Gets the name.
   * @return The name
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the x values. 
   * @return The x values
   */
  public X[] getXs() {
    return _xs;
  }

  /**
   * Gets the y values.
   * @return The y values
   */
  public Y[] getYs() {
    return _ys;
  }

  /**
   * Gets the z values.
   * @return The z values
   */
  public Z[] getZs() {
    return _zs;
  }

  /**
   * Sets the name.
   * @param name The name, not null
   */
  public void setName(final String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  /**
   * Sets the x values.
   * @param xs The x values, not null
   */
  public void setXs(final X[] xs) {
    ArgumentChecker.notNull(xs, "xs");
    _xs = xs;
  }

  /**
   * Sets the y values.
   * @param ys The y values, not null
   */
  public void setYs(final Y[] ys) {
    ArgumentChecker.notNull(ys, "ys");
    _ys = ys;
  }

  /**
   * Sets the z values.
   * @param zs The z values, not null
   */
  public void setZs(final Z[] zs) {
    ArgumentChecker.notNull(zs, "zs");
    _zs = zs;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _name.hashCode();
    result = prime * result + Arrays.hashCode(_xs);
    result = prime * result + Arrays.hashCode(_ys);
    result = prime * result + Arrays.hashCode(_zs);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VolatilityCubeDefinition)) {
      return false;
    }
    final VolatilityCubeDefinition other = (VolatilityCubeDefinition) obj;
    if (_name == null) {
      if (other._name != null) {
        return false;
      }
    } else if (!_name.equals(other._name)) {
      return false;
    }
    if (!Arrays.equals(_xs, other._xs)) {
      return false;
    }
    if (!Arrays.equals(_ys, other._ys)) {
      return false;
    }
    if (!Arrays.equals(_zs, other._zs)) {
      return false;
    }
    return true;
  }

}
