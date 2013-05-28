/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.List;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ShepardInterpolatorND extends InterpolatorND {
  private final Function1D<Double, Double> _basisFunction;

  public ShepardInterpolatorND(final double power) {
    _basisFunction = new ShepardNormalizedRadialBasisFunction(power);
  }

  @Override
  public Double interpolate(final InterpolatorNDDataBundle data, final double[] x) {
    validateInput(data, x);

    final List<Pair<double[], Double>> rawData = data.getData();

    final int n = rawData.size();
    double sum = 0;
    double normSum = 0;
    double[] xi;
    double yi;
    double phi;
    double dist;
    Pair<double[], Double> temp;
    for (int i = 0; i < n; i++) {
      temp = rawData.get(i);
      xi = temp.getFirst();
      yi = temp.getSecond();
      dist = DistanceCalculator.getDistance(x, xi);
      if (dist == 0.0) {
        return yi;
      }
      phi = _basisFunction.evaluate(dist);
      sum += yi * phi;
      normSum += phi;
    }

    return sum / normSum;
  }

  @Override
  public InterpolatorNDDataBundle getDataBundle(final double[] x, final double[] y, final double[] z, final double[] values) {
    return new InterpolatorNDDataBundle(transformData(x, y, z, values));
  }

  @Override
  public InterpolatorNDDataBundle getDataBundle(final List<Pair<double[], Double>> data) {
    return new InterpolatorNDDataBundle(data);
  }

}
