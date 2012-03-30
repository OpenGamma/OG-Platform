/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;




/**
 * 
 */
public final class LinearWeightingFunction extends WeightingFunction {
  private static final LinearWeightingFunction s_instance = new LinearWeightingFunction();

  public static LinearWeightingFunction getInstance() {
    return s_instance;
  }

  private LinearWeightingFunction() {
  }

  @Override
  public double getWeight(final double y) {
    return y;
  }
}
