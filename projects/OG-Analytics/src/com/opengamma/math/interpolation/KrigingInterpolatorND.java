/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.List;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class KrigingInterpolatorND extends InterpolatorND<KrigingInterpolatorDataBundle> {

  @Override
  public Double interpolate(KrigingInterpolatorDataBundle data, double[] x) {
    validateInput(data, x);

    List<Pair<double[], Double>> rawData = data.getData();
    Function1D<Double, Double> variogram = data.getVariogram();
    double[] w = data.getWeights();

    int n = rawData.size();
    final double[] v = new double[n + 1];
    double sum = 0.0;
    double r;
    for (int i = 0; i < n; i++) {
      r = InterpolatorNDDataBundle.getDistance(x, rawData.get(i).getFirst());
      sum += variogram.evaluate(r) * w[i];
    }
    sum += w[n];

    return sum;
  }

}
