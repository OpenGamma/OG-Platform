/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import org.apache.commons.lang.Validate;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.matrix.OGMatrixAlgebra;
import com.opengamma.math.minimization.NonLinearParameterTransforms;

/**
 *  Container for the results of a least square (minimum chi-square) fit, where some model (with a set of parameters), is calibrated
 * to a data set, but the model parameters are first transformed to some fitting parameters (usually to impose some constants).
 */
public class LeastSquareResultsWithTransform extends LeastSquareResults {

  private static final MatrixAlgebra MA = new OGMatrixAlgebra();

  private final NonLinearParameterTransforms _transform;
  private final DoubleMatrix1D _modelParamters;
  private DoubleMatrix2D _inverseJacobianModelPararms;

  public LeastSquareResultsWithTransform(final LeastSquareResults transformedFitResult) {
    super(transformedFitResult);
    _transform = null;
    _modelParamters = transformedFitResult.getFitParameters();
    _inverseJacobianModelPararms = getFittingParameterSensitivityToData();
  }

  public LeastSquareResultsWithTransform(final LeastSquareResults transformedFitResult, final NonLinearParameterTransforms transform) {
    super(transformedFitResult);
    Validate.notNull(transform, "null transform");
    _transform = transform;
    _modelParamters = transform.inverseTransform(getFitParameters());
  }

  public DoubleMatrix1D getModelParameters() {
    return _modelParamters;
  }

  /**
   * This a matrix where the i,jth element is the (infinitesimal) sensitivity of the ith fitting parameter to the jth data
   * point (NOT the model point), when the fitting parameter are such that the chi-squared is minimised. So it is a type of (inverse)
   * Jacobian, but should not be confused with the model jacobian (sensitivity of model data points, to parameters) or its inverse.
   * @return a matrix
   */
  public DoubleMatrix2D getModelParameterSensitivityToData() {
    if (_inverseJacobianModelPararms == null) {
      setModelParameterSensitivityToData();
    }
    return _inverseJacobianModelPararms;
  }

  private void setModelParameterSensitivityToData() {
    DoubleMatrix2D invJac = _transform.inverseJacobian(getFitParameters());
    _inverseJacobianModelPararms = (DoubleMatrix2D) MA.multiply(invJac, getFittingParameterSensitivityToData());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_inverseJacobianModelPararms == null) ? 0 : _inverseJacobianModelPararms.hashCode());
    result = prime * result + ((_modelParamters == null) ? 0 : _modelParamters.hashCode());
    result = prime * result + ((_transform == null) ? 0 : _transform.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    LeastSquareResultsWithTransform other = (LeastSquareResultsWithTransform) obj;
    if (_inverseJacobianModelPararms == null) {
      if (other._inverseJacobianModelPararms != null) {
        return false;
      }
    } else if (!_inverseJacobianModelPararms.equals(other._inverseJacobianModelPararms)) {
      return false;
    }
    if (_modelParamters == null) {
      if (other._modelParamters != null) {
        return false;
      }
    } else if (!_modelParamters.equals(other._modelParamters)) {
      return false;
    }
    if (_transform == null) {
      if (other._transform != null) {
        return false;
      }
    } else if (!_transform.equals(other._transform)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "LeastSquareResults [chiSq=" + getChiSq() + ", fit parameters=" + getFitParameters().toString() + ", model parameters= " +
    getModelParameters().toString() + ", covariance=" + getCovariance().toString() + "]";
  }

}
