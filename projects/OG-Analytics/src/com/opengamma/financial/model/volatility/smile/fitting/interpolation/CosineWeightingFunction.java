/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.interpolation;



/**
 * 
 */
public final class CosineWeightingFunction extends WeightingFunction {
  private static final CosineWeightingFunction s_instance = new CosineWeightingFunction();

  public static CosineWeightingFunction getInstance() {
    return s_instance;
  }

  private CosineWeightingFunction() {
  }

  @Override
  public double getWeight(final double y) {

    final double cos = Math.cos(Math.PI / 2 * y);
    return cos * cos;
  }
}
