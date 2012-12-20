/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class RadialBasisFunction {

  public double evaluate(final Function1D<Double, Double> basisFunction, final List<Pair<double[], Double>> weights, final double[] x, final boolean isNormalized) {

    validateInput(weights, x);

    final int n = weights.size();
    double sum = 0;
    double normSum = 0;
    double[] xi;
    double wi;
    double phi;
    for (int i = 0; i < n; i++) {
      final Pair<double[], Double> pair = weights.get(i);
      xi = pair.getFirst();
      wi = pair.getSecond();
      phi = basisFunction.evaluate(DistanceCalculator.getDistance(x, xi));
      sum += wi * phi;
      normSum += phi;
    }

    return isNormalized ? sum / normSum : sum;
  }

  protected void validateInput(final List<Pair<double[], Double>> weights, final double[] x) {
    Validate.notNull(x, "null position");
    Validate.notNull(weights, "null data");

    final int dim = x.length;
    Validate.isTrue(dim > 0, "0 dimension");
    Validate.isTrue(weights.get(0).getFirst().length == dim, "data and requested point different dimension");
  }

}
