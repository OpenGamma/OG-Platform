/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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
public class RadialBasisFunctionInterpolatorND extends InterpolatorND<RadialBasisFunctionInterpolatorDataBundle> {

  @Override
  public Double interpolate(RadialBasisFunctionInterpolatorDataBundle data, double[] x) {
    validateInput(data, x);
    List<Pair<double[], Double>> rawData = data.getData();
    double[] w = data.getWeights();
    Function1D<Double, Double> basisFunction = data.getBasisFunction();
    int n = rawData.size();
    double sum = 0;
    double normSum = 0;
    double[] xi;
    double phi;
    for (int i = 0; i < n; i++) {
      xi = rawData.get(i).getFirst();
      phi = basisFunction.evaluate(InterpolatorNDDataBundle.getDistance(x, xi));
      sum += w[i] * phi;
      normSum += phi;
    }

    return data.isNormalized() ? sum / normSum : sum;
  }

}
