/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.DistanceCalculator;
import com.opengamma.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.math.interpolation.data.RadialBasisFunctionInterpolatorDataBundle;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class RadialBasisFunctionInterpolatorNDSensitivityCalculator {

  public Map<double[], Double> calculate(final InterpolatorNDDataBundle data, final double[] x) {
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

}
