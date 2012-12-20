/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding;

import org.apache.commons.lang.Validate;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;

import com.opengamma.analytics.math.function.RealPolynomialFunction1D;

/**
 * The eigenvalues of a matrix $\mathbf{A}$ are the roots of the characteristic
 * polynomial $P(x) = \mathrm{det}[\mathbf{A} - x\mathbb{1}]$. For a 
 * polynomial 
 * $$
 * \begin{align*}
 * P(x) = \sum_{i=0}^n a_i x^i
 * \end{align*} 
 * $$
 * an equivalent polynomial can be constructed from the characteristic polynomial of the matrix
 * $$
 * \begin{align*}
 * A = 
 * \begin{pmatrix}
 * -\frac{a_{m-1}}{a_m}  & -\frac{a_{m-2}}{a_m} & \cdots & -\frac{a_{1}}{a_m} & -\frac{a_{0}}{a_m} \\
 * 1                      & 0                     & \cdots & 0                   & 0                   \\
 * 0                      & 1                     & \cdots & 0                   & 0                   \\
 * \vdots                &                       & \cdots &                     & \vdots             \\
 * 0                      & 0                     & \cdots & 1                   & 0                   
 * \end{pmatrix}
 * \end{align*}
 * $$
 * and so the roots are found by calculating the eigenvalues of this matrix.
 */
public class EigenvaluePolynomialRootFinder implements Polynomial1DRootFinder<Double> {

  /**
   * {@inheritDoc}
   */
  @Override
  public Double[] getRoots(final RealPolynomialFunction1D function) {
    Validate.notNull(function, "function");
    final double[] coeffs = function.getCoefficients();
    final int l = coeffs.length - 1;
    final DoubleMatrix2D hessian = DoubleFactory2D.dense.make(l, l);
    for (int i = 0; i < l; i++) {
      hessian.setQuick(0, i, -coeffs[l - i - 1] / coeffs[l]);
      for (int j = 1; j < l; j++) {
        hessian.setQuick(j, i, 0);
        if (i != l - 1) {
          hessian.setQuick(i + 1, i, 1);
        }
      }
    }
    final double[] d = new EigenvalueDecomposition(hessian).getRealEigenvalues().toArray();
    final Double[] result = new Double[d.length];
    for (int i = 0; i < d.length; i++) {
      result[i] = d[i];
    }
    return result;
  }

}
