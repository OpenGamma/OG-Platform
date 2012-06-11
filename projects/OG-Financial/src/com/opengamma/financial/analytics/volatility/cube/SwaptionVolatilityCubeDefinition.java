/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class SwaptionVolatilityCubeDefinition<X, Y, Z> {
  private final String _name;
  private final UniqueIdentifiable _target;
  private final X[] _xs;
  private final Y[] _ys;
  private final Z[] _zs;

  public SwaptionVolatilityCubeDefinition(final String name, final UniqueIdentifiable target, final X[] xs, final Y[] ys, final Z[] zs) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(xs, "xs");
    ArgumentChecker.notNull(ys, "ys");
    ArgumentChecker.notNull(zs, "zs");
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

  public UniqueIdentifiable getTarget() {
    return _target;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _name.hashCode();
    result = prime * result + _target.hashCode();
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
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SwaptionVolatilityCubeDefinition<?, ?, ?> other = (SwaptionVolatilityCubeDefinition<?, ?, ?>) obj;
    if (!ObjectUtils.equals(_name, other._name)) {
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
    if (!ObjectUtils.equals(_target, other._target)) {
      return false;
    }
    return true;
  }
}
