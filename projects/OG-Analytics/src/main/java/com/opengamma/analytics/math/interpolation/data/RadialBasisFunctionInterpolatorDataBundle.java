/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.data;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.DistanceCalculator;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.linearalgebra.DecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class RadialBasisFunctionInterpolatorDataBundle extends InterpolatorNDDataBundle {

  private final Function1D<Double, Double> _basisFunction;
  private final boolean _useNormalized;
  private final double[] _weights;
  private DecompositionResult _decompRes;
  private final Decomposition<?> _decomp = DecompositionFactory.LU_COMMONS;

  public RadialBasisFunctionInterpolatorDataBundle(final List<Pair<double[], Double>> data, final Function1D<Double, Double> basisFunction, final boolean useNormalized) {
    super(data);
    Validate.notNull(basisFunction, "basis function");
    _basisFunction = basisFunction;
    _useNormalized = useNormalized;
    _weights = calculateWeights();
  }

  public double[] getWeights() {
    return _weights;
  }

  public DecompositionResult getDecompositionResult() {
    return _decompRes;
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
    final List<Pair<double[], Double>> data = getData();
    final int n = data.size();
    double sum;
    final double[][] radii = new double[n][n];
    final double[] y = new double[n];
    double phi;
    double[] x1, x2;
    final double zeroValue = _basisFunction.evaluate(0.0);

    for (int i = 0; i < n; i++) {

      x1 = data.get(i).getFirst();
      radii[i][i] = zeroValue;

      for (int j = i + 1; j < n; j++) {
        x2 = data.get(j).getFirst();
        phi = _basisFunction.evaluate(DistanceCalculator.getDistance(x1, x2));
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

    _decompRes = _decomp.evaluate(new com.opengamma.analytics.math.matrix.DoubleMatrix2D(radii));
    final DoubleMatrix1D res = _decompRes.solve(new DoubleMatrix1D(y));

    return res.toArray();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _basisFunction.hashCode();
    result = prime * result + _decomp.hashCode();
    result = prime * result + _decompRes.hashCode();
    result = prime * result + (_useNormalized ? 1231 : 1237);
    result = prime * result + Arrays.hashCode(_weights);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof RadialBasisFunctionInterpolatorDataBundle)) {
      return false;
    }
    final RadialBasisFunctionInterpolatorDataBundle other = (RadialBasisFunctionInterpolatorDataBundle) obj;
    if (_useNormalized != other._useNormalized) {
      return false;
    }
    if (!Arrays.equals(_weights, other._weights)) {
      return false;
    }
    if (!ObjectUtils.equals(_basisFunction, other._basisFunction)) {
      return false;
    }
    if (!ObjectUtils.equals(_decomp, other._decomp)) {
      return false;
    }
    if (!ObjectUtils.equals(_decompRes, other._decompRes)) {
      return false;
    }
    return true;
  }

}
