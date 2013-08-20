/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import static com.opengamma.analytics.math.utilities.Epsilon.epsilon;
import static com.opengamma.analytics.math.utilities.Epsilon.epsilonP;

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;

/**
 * Curve described by the Nelson-Siegle function. The function is one of the most used functional form to describe a bond yield curve.
 */
public class DoublesCurveNelsonSiegel extends DoublesCurve {

  /**
   * The array with the four parameters used to describe the function. The parameters are beta0, beta1, beta2 and lambda (in that order).
   */
  private final double[] _parameters;
  /**
   * The number of parameters of the curve.
   */
  private static final int NB_PARAMETERS = 4;

  /**
   * Constructor from the four parameters and a name.
   * @param name The curve name.
   * @param beta0 The beta0 parameter.
   * @param beta1 The beta1 parameter.
   * @param beta2 The beta2 parameter.
   * @param lambda The lambda parameter.
   */
  public DoublesCurveNelsonSiegel(final String name, final double beta0, final double beta1, final double beta2, final double lambda) {
    super(name);
    _parameters = new double[] {beta0, beta1, beta2, lambda };
  }

  /**
   * Constructor from the four parameters as an array and a name.
   * @param name The curve name.
   * @param parameters The array with the four parameters used to describe the function. The parameters are beta0, beta1, beta2 and lambda (in that order).
   */
  public DoublesCurveNelsonSiegel(final String name, final double[] parameters) {
    super(name);
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.isTrue(parameters.length == NB_PARAMETERS, "Incorrect number of parameters");
    _parameters = parameters;
  }

  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException("Cannot get x data for Nelson-Siegle curve");
  }

  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException("Cannot get x data for Nelson-Siegle curve");
  }

  @Override
  public int size() {
    return NB_PARAMETERS;
  }

  @Override
  public Double getYValue(final Double x) {
    final double x1 = x / _parameters[3];
    final double x2 = epsilon(-x1);
    return _parameters[0] + _parameters[1] * x2 + _parameters[2] * (x2 - Math.exp(-x1));
  }

  @Override
  public double getDyDx(final double x) {
    final double x1 = x / _parameters[3]; //TODO untested
    final double eP = epsilonP(-x1);
    return -(_parameters[1] * eP + _parameters[2] * (eP - Math.exp(-x1))) / _parameters[3];
  }

  @Override
  public Double[] getYValueParameterSensitivity(final Double x) {
    // Forward sweep
    final double x1 = x / _parameters[3];
    final double expx1 = Math.exp(-x1);
    final double x2;
    if (x1 < 1.0E-6) {
      x2 = 1.0 - 0.5 * x1;
    } else {
      x2 = (1 - expx1) / x1;
    }
    //    final double value = _parameters[0] + _parameters[1] * x2 + _parameters[2] * (x2 - expx1);
    // Backward sweep
    final double valueBar = 1.0;
    final double x2Bar = (_parameters[1] + _parameters[2]) * valueBar;
    double expx1Bar;
    double x1Bar;
    if (x1 < 1.0E-6) {
      expx1Bar = -_parameters[2] * valueBar;
      x1Bar = -0.5 * x2Bar + -expx1 * expx1Bar;
    } else {
      expx1Bar = -_parameters[2] * valueBar + -1.0 / x1 * x2Bar;
      x1Bar = -(1 - expx1) / (x1 * x1) * x2Bar + -expx1 * expx1Bar;
    }
    final Double[] parametersBar = new Double[4];
    parametersBar[0] = valueBar;
    parametersBar[1] = x2 * valueBar;
    parametersBar[2] = (x2 - expx1) * valueBar;
    parametersBar[3] = -x / (_parameters[3] * _parameters[3]) * x1Bar;
    return parametersBar;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_parameters);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DoublesCurveNelsonSiegel other = (DoublesCurveNelsonSiegel) obj;
    if (!Arrays.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
