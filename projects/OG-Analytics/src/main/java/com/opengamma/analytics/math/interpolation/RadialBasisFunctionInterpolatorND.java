/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.analytics.math.interpolation.data.RadialBasisFunctionInterpolatorDataBundle;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class RadialBasisFunctionInterpolatorND extends InterpolatorND {
 //TODO R White 14/07/2011 These are only used by getDataBundle, the actual interpolate method used the information in the RadialBasisFunctionInterpolatorDataBundle
 // should remove them altogether and just pass in the information in the getDataBundle method
  private final Function1D<Double, Double> _basisFunction;
  private final boolean _useNormalized;

  public RadialBasisFunctionInterpolatorND(final Function1D<Double, Double> basisFunction, final boolean useNormalized) {
    Validate.notNull(basisFunction, "basis function");
    _basisFunction = basisFunction;
    _useNormalized = useNormalized;
  }

  @Override
  public Double interpolate(final InterpolatorNDDataBundle data, final double[] x) {
    validateInput(data, x);
    Validate.isTrue(data instanceof RadialBasisFunctionInterpolatorDataBundle, "RadialBasisFunctionInterpolatorND needs a RadialBasisFunctionInterpolatorDataBundle");
    RadialBasisFunctionInterpolatorDataBundle radialData = (RadialBasisFunctionInterpolatorDataBundle) data;
    final List<Pair<double[], Double>> rawData = radialData.getData();
    final double[] w = radialData.getWeights();
    final Function1D<Double, Double> basisFunction = radialData.getBasisFunction();
    final int n = rawData.size();
    double sum = 0;
    double normSum = 0;
    double[] xi;
    double phi;
    for (int i = 0; i < n; i++) {
      xi = rawData.get(i).getFirst();
      phi = basisFunction.evaluate(DistanceCalculator.getDistance(x, xi));
      sum += w[i] * phi;
      normSum += phi;
    }

    return radialData.isNormalized() ? sum / normSum : sum;
  }

  @Override
  public Map<double[], Double> getNodeSensitivitiesForValue(final InterpolatorNDDataBundle data, final double[] x) {
    Validate.notNull(data, "data");
    Validate.notNull(x, "x array");
    Validate.isTrue(data instanceof RadialBasisFunctionInterpolatorDataBundle, "RadialBasisFunctionInterpolatorNDSensitivityCalculator needs a RadialBasisFunctionInterpolatorDataBundle");
    RadialBasisFunctionInterpolatorDataBundle radialData = (RadialBasisFunctionInterpolatorDataBundle) data;
    final List<Pair<double[], Double>> rawData = radialData.getData();
    final Function1D<Double, Double> basisFunction = radialData.getBasisFunction();
    final int n = rawData.size();
    double[] xi;
    double[] phi = new double[n];
    double normSum = 0;
    double[] phiNorm = new double[n];
    for (int i = 0; i < n; i++) {
      xi = rawData.get(i).getFirst();
      phi[i] = basisFunction.evaluate(DistanceCalculator.getDistance(x, xi));

      if (radialData.isNormalized()) {
        normSum += phi[i];

        double sum = 0;
        double[] xj;
        for (int j = 0; j < n; j++) {
          xj = rawData.get(j).getFirst();
          sum += basisFunction.evaluate(DistanceCalculator.getDistance(xj, xi));
        }
        phiNorm[i] = sum;
      }
    }
    double[] temp = radialData.getDecompositionResult().solve(phi);
    double sense = 0;
    Map<double[], Double> res = new HashMap<double[], Double>(n);
    for (int i = 0; i < n; i++) {
      if (radialData.isNormalized()) {
        sense = temp[i] * phiNorm[i] / normSum;
      } else {
        sense = temp[i];
      }
      res.put(rawData.get(i).getFirst(), sense);
    }

    return res;

  }
  @Override
  public RadialBasisFunctionInterpolatorDataBundle getDataBundle(final double[] x, final double[] y, final double[] z, final double[] values) {
    return new RadialBasisFunctionInterpolatorDataBundle(transformData(x, y, z, values), _basisFunction, _useNormalized);
  }

  @Override
  public RadialBasisFunctionInterpolatorDataBundle getDataBundle(final List<Pair<double[], Double>> data) {
    return new RadialBasisFunctionInterpolatorDataBundle(data, _basisFunction, _useNormalized);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _basisFunction.hashCode();
    result = prime * result + (_useNormalized ? 1231 : 1237);
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
    final RadialBasisFunctionInterpolatorND other = (RadialBasisFunctionInterpolatorND) obj;
    if (!ObjectUtils.equals(_basisFunction, other._basisFunction)) {
      return false;
    }
    return _useNormalized == other._useNormalized;
  }
}
