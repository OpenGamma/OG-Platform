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
public class ShepardInterpolatorND extends InterpolatorND<InterpolatorNDDataBundle> {
  private final Function1D<Double, Double> _basisFunction;

  public ShepardInterpolatorND(final double power) {
    _basisFunction = new ShepardNormalizedRadialBasisFunction(power);
  }

  @Override
  public Double interpolate(InterpolatorNDDataBundle data, double[] x) {
    validateInput(data, x);

    List<Pair<double[], Double>> rawData = data.getData();

    int n = rawData.size();
    double sum = 0;
    double normSum = 0;
    double[] xi;
    double yi;
    double phi;
    Pair<double[], Double> temp;
    for (int i = 0; i < n; i++) {
      temp = rawData.get(i);
      xi = temp.getFirst();
      yi = temp.getSecond();
      phi = _basisFunction.evaluate(InterpolatorNDDataBundle.getDistance(x, xi));
      sum += yi * phi;
      normSum += phi;
    }

    return sum / normSum;
  }

}
