/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;

import com.opengamma.math.function.RealPolynomialFunction1D;

/**
 * 
 */
public class EigenvaluePolynomialRootFinder implements Polynomial1DRootFinder<Double> {

  @Override
  public Double[] getRoots(final RealPolynomialFunction1D function) {
    if (function == null) {
      throw new IllegalArgumentException("Function was null");
    }
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
