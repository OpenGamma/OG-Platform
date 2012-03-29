/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.Arrays;

/**
 * 
 */
public class SurfaceArrayUtils {

  public static int getLowerBoundIndex(final double[] strikes, final double strike) {
    final int n = strikes.length;
    if (strike < strikes[0]) {
      return 0;
    }
    if (strike > strikes[n - 1]) {
      return n - 1;
    }

    int index = Arrays.binarySearch(strikes, strike);
    if (index >= 0) {
      // Fast break out if it's an exact match.
      return index;
    }
    if (index < 0) {
      index = -(index + 1);
      index--;
    }
    return index;
  }
}
