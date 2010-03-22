/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 * 
 */
public abstract class VaRCalculator<T> extends Function1D<T, Double> {
  private double _horizon;
  private double _periods;
  private double _quantile;

  public VaRCalculator(final double horizon, final double periods, final double quantile) {
    if (horizon < 0)
      throw new IllegalArgumentException("Horizon cannot be negative");
    if (periods < 0)
      throw new IllegalArgumentException("Periods cannot be negative");
    if (quantile <= 0 || quantile >= 1)
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    _horizon = horizon;
    _periods = periods;
    _quantile = quantile;
  }

  public void setHorizon(final double horizon) {
    if (horizon < 0)
      throw new IllegalArgumentException("Horizon cannot be negative");
    _horizon = horizon;
  }

  public void setPeriods(final double periods) {
    if (periods < 0)
      throw new IllegalArgumentException("Periods cannot be negative");
    _periods = periods;
  }

  public void setQuantile(final double quantile) {
    if (quantile <= 0 || quantile >= 1)
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
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

}
