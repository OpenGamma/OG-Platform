/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class EuropeanVanillaOption {
  private boolean _isCall;
  private double _t;
  private double _k;

  public EuropeanVanillaOption(final double k, final double t, final boolean isCall) {
    Validate.isTrue(k > 0.0, "k must be > 0.0");
    Validate.isTrue(t >= 0.0, "t must be >= 0.0");
    _k = k;
    _t = t;
    _isCall = isCall;
  }

  public boolean isCall() {
    return _isCall;
  }

  public double getT() {
    return _t;
  }

  public double getK() {
    return _k;
  }

  public void setCall(final boolean isCall) {
    _isCall = isCall;
  }

  public void setT(final double t) {
    _t = t;
  }

  public void setK(final double k) {
    _k = k;
  }

}
