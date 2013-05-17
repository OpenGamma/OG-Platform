/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.analytics.math.interpolation.data.KrigingInterpolatorDataBundle;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class KrigingInterpolatorND extends InterpolatorND {
  private final double _beta;

  public KrigingInterpolatorND(final double beta) {
    Validate.isTrue(beta >= 1 && beta < 2, "Beta was not in acceptable range (1 <= beta < 2");
    _beta = beta;
  }

  @Override
  public Double interpolate(final InterpolatorNDDataBundle data, final double[] x) {      
    validateInput(data, x);
    Validate.isTrue(data instanceof KrigingInterpolatorDataBundle, "KriginInterpolatorND needs a KriginInterpolatorDataBundle");
    KrigingInterpolatorDataBundle krigingData = (KrigingInterpolatorDataBundle) data;
    final List<Pair<double[], Double>> rawData = krigingData.getData();
    final Function1D<Double, Double> variogram = krigingData.getVariogram();
    final double[] w = krigingData.getWeights();

    final int n = rawData.size();
    double sum = 0.0;
    double r;
    for (int i = 0; i < n; i++) {
      r = DistanceCalculator.getDistance(x, rawData.get(i).getFirst());
      sum += variogram.evaluate(r) * w[i];
    }
    sum += w[n];

    return sum;
  }

  @Override
  public KrigingInterpolatorDataBundle getDataBundle(final double[] x, final double[] y, final double[] z, final double[] values) {
    return new KrigingInterpolatorDataBundle(transformData(x, y, z, values), _beta);
  }

  @Override
  public KrigingInterpolatorDataBundle getDataBundle(final List<Pair<double[], Double>> data) {
    return new KrigingInterpolatorDataBundle(data, _beta);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_beta);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final KrigingInterpolatorND other = (KrigingInterpolatorND) obj;
    return Double.doubleToLongBits(_beta) == Double.doubleToLongBits(other._beta);
  }

}
