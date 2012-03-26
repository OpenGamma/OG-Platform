/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.interpolation;


/**
 * TODO this belongs with interpolators
 */
public final class SineWeightingFunction extends WeightingFunction {
  private static final SineWeightingFunction s_instance = new SineWeightingFunction();

  public static SineWeightingFunction getInstance() {
    return s_instance;
  }

  private SineWeightingFunction() {
  }

  @Override
  public double getWeight(final double y) {
    return 0.5 * (Math.sin(Math.PI * (y - 0.5)) + 1);
  }

}
