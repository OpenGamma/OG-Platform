/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * @param <T> Type of the data
 */
public abstract class VaRCalculator<T> extends Function1D<T, Double> {
  private double _horizon;
  private double _periods;
  private double _quantile;

  public VaRCalculator(final double horizon, final double periods, final double quantile) {
    ArgumentChecker.notNegative(horizon, "horizon");
    ArgumentChecker.notNegative(periods, "periods");
    if (!ArgumentChecker.isInRangeInclusive(0, 1, quantile)) {
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    }
    _horizon = horizon;
    _periods = periods;
    _quantile = quantile;
  }

  public void setHorizon(final double horizon) {
    ArgumentChecker.notNegative(horizon, "horizon");
    _horizon = horizon;
  }

  public void setPeriods(final double periods) {
    ArgumentChecker.notNegative(periods, "periods");
    _periods = periods;
  }

  public void setQuantile(final double quantile) {
    if (!ArgumentChecker.isInRangeInclusive(0, 1, quantile)) {
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    }
    _quantile = quantile;
  }

  public double getHorizon() {
    return _horizon;
  }

  public double getPeriods() {
    return _periods;
  }

  public double getQuantile() {
    return _quantile;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_horizon);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_periods);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_quantile);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    VaRCalculator<?> other = (VaRCalculator<?>) obj;
    if (Double.doubleToLongBits(_horizon) != Double.doubleToLongBits(other._horizon)) {
      return false;
    }
    if (Double.doubleToLongBits(_periods) != Double.doubleToLongBits(other._periods)) {
      return false;
    }
    if (Double.doubleToLongBits(_quantile) != Double.doubleToLongBits(other._quantile)) {
      return false;
    }
    return true;
  }

}
