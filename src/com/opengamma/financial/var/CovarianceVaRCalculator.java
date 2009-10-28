/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

/**
 * 
 * @author emcleod
 */
public class CovarianceVaRCalculator {
  private final Algebra _algebra = new Algebra();

  public Double getDeltaVaR(final DoubleMatrix1D deltaVector, final DoubleMatrix2D covarianceMatrix) {
    return Math.sqrt(_algebra.mult(deltaVector, _algebra.mult(covarianceMatrix, deltaVector)));
  }

  public Double getGammaVaR(final DoubleMatrix1D gammaVector, final DoubleMatrix2D covarianceMatrix) {
    return Math.sqrt(_algebra.mult(gammaVector, _algebra.mult(covarianceMatrix, gammaVector)));
  }

  public Double getVegaVaR(final DoubleMatrix1D vegaVector, final DoubleMatrix2D covarianceMatrix) {
    return Math.sqrt(_algebra.mult(vegaVector, _algebra.mult(covarianceMatrix, vegaVector)));
  }

  public Double getThetaVaR(final Double theta, final Double dt) {
    return theta * dt;
  }
}
