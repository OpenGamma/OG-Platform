/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.StudentTTwoTailedCriticalValueCalculator;

/**
 * @author emcleod
 * 
 */
public class ParametricStudentTLinearVaRCalculator extends ParametricVaRCalculator {
  private Function1D<Double, Double> _calculator;
  private double _nu;
  private final Algebra _algebra = new Algebra();

  public ParametricStudentTLinearVaRCalculator(final double nu) {
    _nu = nu;
    _calculator = new StudentTTwoTailedCriticalValueCalculator(nu);
  }

  public void setNu(final double nu) {
    _nu = nu;
    _calculator = new StudentTTwoTailedCriticalValueCalculator(nu);
  }

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
    System.out.println(quantile + " " + _calculator.evaluate(quantile));
    return _calculator.evaluate(quantile) * Math.sqrt(_algebra.mult(v, _algebra.mult(m, v)) * (_nu - 2) / _nu);
  }

}
