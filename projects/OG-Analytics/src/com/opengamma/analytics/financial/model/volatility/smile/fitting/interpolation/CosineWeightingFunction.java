/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import com.opengamma.util.ArgumentChecker;

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
    ArgumentChecker.isInRangeInclusive(0, 1, y);
    final double cos = Math.cos(Math.PI / 2 * y);
    return cos * cos;
  }
}
