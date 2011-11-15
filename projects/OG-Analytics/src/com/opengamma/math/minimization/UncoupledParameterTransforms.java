/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import java.util.Arrays;
import java.util.BitSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * For a set of <i>n</i> function parameters, this takes <i>n</i> ParameterLimitsTransform (which can be the NullTransform which does NOT transform the parameter) which transform
 * a constrained function parameter (e.g. must be between -1 and 1) to a unconstrained fit parameter. It also takes a BitSet (of length <i>n</i>) with an element set to <b>true</b> if
 * that parameter is fixed - a set of <i>n</i> startValues must also be provided, with only those corresponding to fixed parameters being used (i.e. the parameter is fixed at the startValue).
 * The purpose is to allow an optimiser to work with unconstrained parameters without modifying the function that one wishes to optimise. 
 */
// TODO not tested
public class UncoupledParameterTransforms implements NonLinearParameterTransforms {
  private final DoubleMatrix1D _startValues;
  private final ParameterLimitsTransform[] _transforms;
  private final BitSet _fixed;
  private final int _nMP;
  private final int _nFP;

  /**
   * 
   * @param startValues fixed parameter values (if no parameters are fixed this is completely ignored)
   * @param transforms Array of ParameterLimitsTransform (which can be the NullTransform which does NOT transform the parameter) which transform
   * a constrained function parameter (e.g. must be between -1 and 1) to a unconstrained fit parameter.
   * @param fixed BitSet with an element set to <b>true</b> if that parameter is fixed
   */
  public UncoupledParameterTransforms(final DoubleMatrix1D startValues, final ParameterLimitsTransform[] transforms, final BitSet fixed) {
    Validate.notNull(startValues, "null start values");
    Validate.notEmpty(transforms, "must specify transforms");
    Validate.notNull(fixed, "must specify what is fixed (even if none)");
    _nMP = startValues.getNumberOfElements();
    Validate.isTrue(_nMP == transforms.length, "must give a transform for each model parameter");
    final int count = fixed.cardinality();
    Validate.isTrue(count < _nMP, "all parameters are fixed");
    _nFP = _nMP - count;
    _startValues = startValues;
    _transforms = transforms;
    _fixed = fixed;
  }

  /**
   * 
   * @return The number of function parameters 
   */
  public int getNumberOfModelParameters() {
    return _nMP;
  }

  /**
   * 
   * @return The number of fitting parameters (equals the number of model parameters minus the number of fixed parameters) 
   */
  public int getNumberOfFittingParameters() {
    return _nFP;
  }

  /**
   * Transforms from a set of function parameters (some of which may have constrained range and/or be fixed) to a (possibly smaller) set of unconstrained fitting parameters
   * <b>Note:</b> If a parameter is fixed, it is its value as provided by <i>startValues<\i> not the value given here that will be returned by inverseTransform (and thus used in the function)  
   * @param functionParameters The function parameters 
   * @return The fitting parameters
   */
  public DoubleMatrix1D transform(final DoubleMatrix1D functionParameters) {
    Validate.notNull(functionParameters, "function parameters");
    Validate.isTrue(functionParameters.getNumberOfElements() == _nMP, "functionParameters wrong dimension");
    final double[] fittingParameter = new double[_nFP];
    for (int i = 0, j = 0; i < _nMP; i++) {
      if (!_fixed.get(i)) {
        fittingParameter[j] = _transforms[i].transform(functionParameters.getEntry(i));
        j++;
      }
    }
    return new DoubleMatrix1D(fittingParameter);
  }

  /**
   * Transforms from a set of unconstrained fitting parameters to a (possibly larger) set of function parameters (some of which may have constrained range and/or be fixed).
   * @param fittingParameters The fitting parameters
   * @return The function parameters 
   */
  public DoubleMatrix1D inverseTransform(final DoubleMatrix1D fittingParameters) {
    Validate.notNull(fittingParameters, "fitting parameters");
    Validate.isTrue(fittingParameters.getNumberOfElements() == _nFP, "fititngParameter wrong dimension");
    final double[] modelParameter = new double[_nMP];
    for (int i = 0, j = 0; i < _nMP; i++) {
      if (_fixed.get(i)) {
        modelParameter[i] = _startValues.getEntry(i);
      } else {
        modelParameter[i] = _transforms[i].inverseTransform(fittingParameters.getEntry(j));
        j++;
      }
    }
    return new DoubleMatrix1D(modelParameter);
  }

  /**
   * Calculated the Jacobian of the transform from function parameters to fitting parameters - the i,j element will be the partial derivative of i^th fitting parameter with respect 
   * to the j^th function parameter 
   * @param functionParameters The function parameters 
   * @return matrix of partial derivative of fitting parameter with respect to function parameters 
   */
  // TODO not tested
  public DoubleMatrix2D jacobian(final DoubleMatrix1D functionParameters) {
    Validate.notNull(functionParameters, "function parameters");
    Validate.isTrue(functionParameters.getNumberOfElements() == _nMP, "functionParameters wrong dimension");
    final double[][] jac = new double[_nFP][_nMP];
    for (int i = 0, j = 0; i < _nMP; i++) {
      if (!_fixed.get(i)) {
        jac[j][i] = _transforms[i].transformGradient(functionParameters.getEntry(i));
        j++;
      }
    }
    return new DoubleMatrix2D(jac);
  }

  /**
   * Calculated the Jacobian of the transform from fitting parameters to function parameters - the i,j element will be the partial derivative of i^th function parameter with respect 
   * to the j^th  fitting parameter 
   * @param fittingParameters  The fitting parameters
   * @return  matrix of partial derivative of function parameter with respect to fitting parameters 
   */
  // TODO not tested
  public DoubleMatrix2D inverseJacobian(final DoubleMatrix1D fittingParameters) {
    Validate.notNull(fittingParameters, "fitting parameters");
    Validate.isTrue(fittingParameters.getNumberOfElements() == _nFP, "fititngParameter wrong dimension");
    final double[][] jac = new double[_nMP][_nFP];
    for (int i = 0, j = 0; i < _nMP; i++) {
      if (!_fixed.get(i)) {
        jac[i][j] = _transforms[i].inverseTransformGradient(fittingParameters.getEntry(j));
        j++;
      }
    }
    return new DoubleMatrix2D(jac);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _fixed.hashCode();
    result = prime * result + _startValues.hashCode();
    result = prime * result + Arrays.hashCode(_transforms);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final UncoupledParameterTransforms other = (UncoupledParameterTransforms) obj;
    if (!ObjectUtils.equals(_fixed, other._fixed)) {
      return false;
    }
    if (!ObjectUtils.equals(_startValues, other._startValues)) {
      return false;
    }
    return Arrays.equals(_transforms, other._transforms);
  }

}
