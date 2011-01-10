/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.DecompositionResult;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class RadialBasisFunctionInterpolatorDataBundle extends InterpolatorNDDataBundle {

  private final Function1D<Double, Double> _basisFunction;
  private final boolean _useNormalized;
  private final double[] _weights;
  private final Decomposition<?> _decomp = new LUDecompositionCommons();

  public RadialBasisFunctionInterpolatorDataBundle(List<Pair<double[], Double>> data, final Function1D<Double, Double> basisFunction, final boolean useNormalized) {
    super(data);
    Validate.notNull(basisFunction, "basis function");
    _basisFunction = basisFunction;
    _useNormalized = useNormalized;
    _weights = calculateWeights();
  }

  public double[] getWeights() {
    return _weights;
  }

  /**
   * Gets the basisFunction field.
   * @return the basisFunction
   */
  public Function1D<Double, Double> getBasisFunction() {
    return _basisFunction;
  }

  /**
   * Gets the useNormalized field.
   * @return the useNormalized
   */
  public boolean isNormalized() {
    return _useNormalized;
  }

  private double[] calculateWeights() {
    List<Pair<double[], Double>> data = getData();
    final int n = data.size();
    double sum;
    final double[][] radii = new double[n][n];
    final double[] y = new double[n];
    double phi;
    double[] x1, x2;
    double zeroValue = _basisFunction.evaluate(0.0);

    for (int i = 0; i < n; i++) {

      x1 = data.get(i).getFirst();
      radii[i][i] = zeroValue;

      for (int j = i + 1; j < n; j++) {
        x2 = data.get(j).getFirst();
        phi = _basisFunction.evaluate(getDistance(x1, x2));
        Validate.isTrue(!Double.isNaN(phi) || !Double.isInfinite(phi), "basis function return invalide number");
        radii[i][j] = phi;
        radii[j][i] = phi; // matrix symmetric since basis function depends on distance only
      }
      if (_useNormalized) {
        sum = 0.0;
        for (int j = 0; j < n; j++) {
          sum += radii[i][j];
        }
        y[i] = sum * data.get(i).getSecond();
      } else {
        y[i] = data.get(i).getSecond();
      }
    }

    DecompositionResult decompRes = _decomp.evaluate(new com.opengamma.math.matrix.DoubleMatrix2D(radii));
    DoubleMatrix1D res = decompRes.solve(new DoubleMatrix1D(y));

    return res.toArray();
  }

}
