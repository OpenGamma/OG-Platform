package com.opengamma.financial.var.parametric;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.time.TenorUtil;

public class DeltaNormalParametricVaRCalculator {
  private final Algebra _algebra;
  private final Double _samplingPeriodScale;
  private final ProbabilityDistribution<Double> _normal = new NormalProbabilityDistribution(0, 1);

  public DeltaNormalParametricVaRCalculator(final Tenor samplingPeriod) {
    _algebra = new Algebra();
    _samplingPeriodScale = Math.sqrt(TenorUtil.getDaysInTenor(samplingPeriod));
  }

  private Double getVaR(final DoubleMatrix1D vector, final DoubleMatrix2D matrix) {
    if (vector.size() != matrix.rows())
      throw new IllegalArgumentException("Vector size (" + vector.size() + ") does not match matrix size (" + matrix.rows() + " x " + matrix.columns() + ")");
    if (matrix.rows() != matrix.columns())
      throw new IllegalArgumentException("Matrix was not square (" + matrix.rows() + " x " + matrix.columns());
    return Math.sqrt(_algebra.mult(vector, _algebra.mult(matrix, vector)));
  }

  public Double getStaticVaR(final DoubleMatrix1D vector, final DoubleMatrix2D matrix, final Tenor horizon, final double confidenceLevel) {
    final Double var = getVaR(vector, matrix);
    System.out.println(Math.sqrt(TenorUtil.getDaysInTenor(horizon)) + " " + _samplingPeriodScale);
    return _normal.getInverseCDF(confidenceLevel) * var * Math.sqrt(TenorUtil.getDaysInTenor(horizon)) / _samplingPeriodScale;
  }

  public Double getDynamicVaR(final DoubleMatrix1D vector, final DoubleMatrix2D matrix, final Tenor horizon, final double confidenceLevel) {
    return getStaticVaR(vector, matrix, horizon, confidenceLevel);
  }
}
