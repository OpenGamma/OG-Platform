/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.sabr;


/**
 * 
 */
public class LinearWeightingFunction extends WeightingFunction {

  @Override
  public double getWeight(final double[] strikes, final int index, final double strike) {
    return (strikes[index + 1] - strike) / (strikes[index + 1] - strikes[index]);
  }
}
