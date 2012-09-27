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
// TODO this belongs with interpolators
public final class LinearWeightingFunction extends WeightingFunction {
  private static final LinearWeightingFunction s_instance = new LinearWeightingFunction();

  public static LinearWeightingFunction getInstance() {
    return s_instance;
  }

  private LinearWeightingFunction() {
  }

  @Override
  public double getWeight(final double y) {
    ArgumentChecker.isInRangeInclusive(0, 1, y);
    return y;
  }

  @Override
  public String toString() {
    return "Linear weighting function";
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return true;
  }
}
