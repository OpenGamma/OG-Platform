/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * 
 */
public abstract class OptionFunctionProvider {

  private double _strike;
  private int _steps;
  private double _sign;

  public OptionFunctionProvider(final double strike, final int steps, final boolean isCall) {
    _strike = strike;
    _steps = steps;
    _sign = isCall ? 1. : -1.;
  }

  public abstract double[] getPayoffAtExpiry(final double assetPrice, final double upOverDown);

  public abstract double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice,
      final double downFactor, final double upOverDown, final int steps);

  public double getStrike() {
    return _strike;
  }

  public int getNumberOfSteps() {
    return _steps;
  }

  public double getSign() {
    return _sign;
  }
}
