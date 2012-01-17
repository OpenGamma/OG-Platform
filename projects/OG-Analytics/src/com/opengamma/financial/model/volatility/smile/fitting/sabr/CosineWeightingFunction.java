/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.sabr;


/**
 * 
 */
public class CosineWeightingFunction extends WeightingFunction {

  @Override
  public double getWeight(final double[] strikes, final int index, final double strike) {
    final double cos = Math.cos(Math.PI / 2 * (strike - strikes[index]) / (strikes[index + 1] - strikes[index]));
    return cos * cos;
  }
}
