/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class FlatVolatility implements VolatilityModel1D {

  private double _vol;

  public FlatVolatility(final double vol) {
    ArgumentChecker.isTrue(vol >= 0.0, "negative vol");
    _vol = vol;

  }

  @Override
  public Double getVolatility(double[] fwdKT) {
    return _vol;
  }

  @Override
  public double getVolatility(double forward, double strike, double timeToExpiry) {
    return _vol;
  }

  @Override
  public double getVolatility(SimpleOptionData option) {
    return _vol;
  }

}
