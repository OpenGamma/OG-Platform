/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class DistanceCalculator {

  public static double getDistance(final double[] x1, final double[] x2) {
    final int dim = x1.length;
    Validate.isTrue(dim == x2.length, "different dimensions");
    double sum = 0;
    double diff;
    for (int i = 0; i < dim; i++) {
      diff = x1[i] - x2[i];
      sum += diff * diff;
    }
    return Math.sqrt(sum);
  }
}
