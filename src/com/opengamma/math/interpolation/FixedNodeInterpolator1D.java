/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class FixedNodeInterpolator1D implements WrappedInterpolator {

  private final double[] _x;
  private final int _n;
  private final Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult> _interpolator;

  public FixedNodeInterpolator1D(double[] xNodes, final Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult> interpolator) {
    Validate.notNull(xNodes);
    Validate.notNull(interpolator);
    _n = xNodes.length;
    _x = xNodes;
    _interpolator = interpolator;
  }

  public double[] getNodePositions() {
    return _x;
  }

  public int getNumberOfNodes() {
    return _n;
  }

  @Override
  public Interpolator<? extends Interpolator1DDataBundle, Double, ? extends InterpolationResult> getUnderlyingInterpolator() {
    return _interpolator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_interpolator == null) ? 0 : _interpolator.hashCode());
    result = prime * result + Arrays.hashCode(_x);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FixedNodeInterpolator1D other = (FixedNodeInterpolator1D) obj;
    if (_interpolator == null) {
      if (other._interpolator != null) {
        return false;
      }
    } else if (!_interpolator.equals(other._interpolator)) {
      return false;
    }
    if (!Arrays.equals(_x, other._x)) {
      return false;
    }
    return true;
  }

}
