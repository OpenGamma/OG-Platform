/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.apache.activemq.util.BitArray;
import org.apache.commons.lang.Validate;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * For a set of <i>n</i> function parameters, this takes <i>n</i> ParameterLimitsTransform (which can be the NullTransform which does NOT transform the parameter) which transform
 * a constrained function parameter (e.g. must be between -1 and 1) to a unconstrained fit parameter. It also takes a BitArray (of length <i>n</i>) with an element set to <b>true</b> if
 * that parameter is fixed - a set of <i>n</i> startValues must also be provided, with only those corresponding to fixed parameters being used (i.e. the parameter is fixed at the startValue).
 * The purpose is to allow an optimiser to work with unconstrained parameters without modifying the function that one wishes to optimise. 
 */
// TODO not tested
public class TransformParameters {
  private final DoubleMatrix1D _startValues;
  private final ParameterLimitsTransform[] _transforms;
  private final BitArray _fixed;
  private final int _nMP;
  private final int _nFP;

  /**
   * 
   * @param startValues fixed parameter values (if no parameters are fixed this is completely ignored)
   * @param transforms Array of ParameterLimitsTransform (which can be the NullTransform which does NOT transform the parameter) which transform
   * a constrained function parameter (e.g. must be between -1 and 1) to a unconstrained fit parameter.
   * @param fixed BitArray  with an element set to <b>true</b> if that parameter is fixed
   */
  public TransformParameters(final DoubleMatrix1D startValues, final ParameterLimitsTransform[] transforms, final BitArray fixed) {
    Validate.notNull(startValues, "null start values");
    Validate.notEmpty(transforms, "must specify transforms");
    Validate.notNull(fixed, "must specify what is fixed (even if none)");
    _nMP = startValues.getNumberOfElements();
    Validate.isTrue(_nMP == transforms.length, "must give a transform for each model parameter");

    int count = 0;
    for (int i = 0; i < _nMP; i++) {
      if (fixed.get(i)) {
        count++;
      }
    }
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
  public int getNumFunctionParameters() {
    return _nMP;
  }

  /**
   * 
   * @return The number of fitting parameters (equals the number of model parameters minus the number of fixed parameters) 
   */
  public int getNumFittingParameters() {
    return _nFP;
  }

  /**
   * Transforms from a set of function parameters (some of which may have constrained range and/or be fixed) to a (possibly smaller) set of unconstrained fitting parameters
   * <b>Note:</b> If a parameter is fixed, it is its value as provided by <i>startValues<\i> not the value given here that will be returned by inverseTransform (and thus used in the function)  
   * @param functionParameters The function parameters 
   * @return The fitting parameters
   */
  public DoubleMatrix1D transform(final DoubleMatrix1D functionParameters) {
    Validate.isTrue(functionParameters.getNumberOfElements() == _nMP, "functionParameters wrong dimension");
    double[] fittingParameter = new double[_nFP];
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
   * @param fittingParameter The fitting parameters
   * @return The function parameters 
   */
  public DoubleMatrix1D inverseTransform(final DoubleMatrix1D fittingParameter) {
    Validate.isTrue(fittingParameter.getNumberOfElements() == _nFP, "fititngParameter wrong dimension");
    double[] modelParameter = new double[_nMP];
    for (int i = 0, j = 0; i < _nMP; i++) {
      if (_fixed.get(i)) {
        modelParameter[i] = _startValues.getEntry(i);
      } else {
        modelParameter[i] = _transforms[i].inverseTrasfrom(fittingParameter.getEntry(j));
        j++;
      }
    }
    return new DoubleMatrix1D(modelParameter);
  }

  /**
   * Calculated the jacobian of the transform from function parameters to fitting parameters - the i,j element will be the partial derivative of i^th fitting parameter with respect 
   * to the j^th function parameter 
   * @param functionParameters The function parameters 
   * @return matrix of partial derivative of fitting parameter with respect to function parameters 
   */
  // TODO not tested
  public DoubleMatrix2D jacobian(final DoubleMatrix1D functionParameters) {
    Validate.isTrue(functionParameters.getNumberOfElements() == _nMP, "functionParameters wrong dimension");
    double[][] jac = new double[_nFP][_nMP];
    for (int i = 0, j = 0; i < _nMP; i++) {
      if (!_fixed.get(i)) {
        jac[j][i] = _transforms[i].transformGrdient(functionParameters.getEntry(i));
        j++;
      }
    }
    return new DoubleMatrix2D(jac);
  }

  /**
   * Calculated the jacobian of the transform from fitting parameters to function parameters - the i,j element will be the partial derivative of i^th function parameter with respect 
   * to the j^th  fitting parameter 
   * @param fittingParameters  The fitting parameters
   * @return  matrix of partial derivative of function parameter with respect to fitting parameters 
   */
  // TODO not tested
  public DoubleMatrix2D inverseJacobian(final DoubleMatrix1D fittingParameters) {
    Validate.isTrue(fittingParameters.getNumberOfElements() == _nFP, "fititngParameter wrong dimension");
    double[][] jac = new double[_nMP][_nFP];
    for (int i = 0, j = 0; i < _nMP; i++) {
      if (!_fixed.get(i)) {
        jac[i][j] = _transforms[i].inverseTrasfromGradient(fittingParameters.getEntry(j));
        j++;
      }
    }
    return new DoubleMatrix2D(jac);
  }

}
