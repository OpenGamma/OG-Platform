/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * @author emcleod
 * 
 */
public class NormalLinearParametricVaRCalculator extends ParametricVaRCalculator {
  private final Algebra _algebra = new Algebra();
  private final ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.financial.var.parametric.ParametricVaRCalculator#evaluate
   * (cern.colt.matrix.DoubleMatrix2D, cern.colt.matrix.DoubleMatrix1D, double,
   * double, double)
   */
  @Override
  public Double evaluate(final DoubleMatrix2D m, final DoubleMatrix1D v, final double periods, final double horizon, final double quantile) {
    if (m == null)
      throw new IllegalArgumentException("Covariance matrix was null");
    if (v == null)
      throw new IllegalArgumentException("Sensitivities vector was null");
    if (periods <= 0)
      throw new IllegalArgumentException("Number of periods must be greater than zero");
    if (horizon <= 0)
      throw new IllegalArgumentException("Horizon must be greater than zero");
    if (quantile <= 0 || quantile >= 1)
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    return _normal.getInverseCDF(quantile) * Math.sqrt(horizon * _algebra.mult(v, _algebra.mult(m, v)) / periods);
  }
}
