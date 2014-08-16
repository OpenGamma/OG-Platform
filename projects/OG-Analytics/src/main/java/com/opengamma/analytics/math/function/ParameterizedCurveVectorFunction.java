/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * This is simply a {@link VectorFunction} backed by a {@link ParameterizedCurve}
 */
public class ParameterizedCurveVectorFunction extends VectorFunction {

  private final double[] _samplePoints;
  private final ParameterizedCurve _curve;

  /**
   * Set up a sampled (parameterised) curve 
   * @param samplePoints points where we sample the curve 
   * @param curve a parameterised curve 
   */
  public ParameterizedCurveVectorFunction(final double[] samplePoints, final ParameterizedCurve curve) {
    ArgumentChecker.notEmpty(samplePoints, "samplePoints");
    ArgumentChecker.notNull(curve, "curve");
    _samplePoints = samplePoints;
    _curve = curve;
  }

  @Override
  public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
    final Function1D<Double, DoubleMatrix1D> sense = _curve.getYParameterSensitivity(x);
    final int n = getSizeOfRange();
    final DoubleMatrix2D jac = new DoubleMatrix2D(n, getSizeOfDomain());
    for (int i = 0; i < n; i++) {
      jac.getData()[i] = sense.evaluate(_samplePoints[i]).getData();
    }
    return jac;
  }

  @Override
  public int getSizeOfDomain() {
    return _curve.getNumParamters();
  }

  @Override
  public int getSizeOfRange() {
    return _samplePoints.length;
  }

  /**
   * Build a curve given the parameters, then return its value at the sample points
   * @param curveParameters curve parameters 
   * @return the curve value at the sample points 
   */
  @Override
  public DoubleMatrix1D evaluate(final DoubleMatrix1D curveParameters) {
    final Function1D<Double, Double> func = _curve.asFunctionOfArguments(curveParameters);
    final int n = _samplePoints.length;
    final DoubleMatrix1D y = new DoubleMatrix1D(n);
    for (int i = 0; i < n; i++) {
      y.getData()[i] = func.evaluate(_samplePoints[i]);
    }
    return y;
  }

}
