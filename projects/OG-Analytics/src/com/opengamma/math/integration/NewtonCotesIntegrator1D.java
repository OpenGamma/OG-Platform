/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.function.Function1D;

/**
 * 
 * This class defines various Newton-Cotes formulae. For closed formulae, the integral is approximated by
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{equation*}
 * \\int_a^b f(x) dx \\approx \\sum_{i=0}^n w_i f(x_i)
 * \\end{equation*}}
 * and for the open formulae is approximated by
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{equation*}
 * \\int_a^b f(x) dx \\approx \\sum_{i=1}^{n-1} w_i f(x_i)
 * \\end{equation*}}
 * 
 * If {@latex.inline $n$} is the degree, {@latex.inline $x_i=a + i\\frac{(b-a)}{n}$} 
 * and {@latex.inline $f(x_i)$} is the value of the function at {@latex.inline $x_i$}, the formulae are given by:
 * <ul>
 * <li> Right hand
 * <li> Left hand
 * <li> Mid point
 * <li> Trapezoidal
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{equation*}
 * \\frac{b-a}{2}\\left(f(x_0) + f(x_1)\\right)
 * \\end{equation*}}
 * <li> Simpson's
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{equation*}
 * \\frac{b-a}{8}\\left(f(x_0) + 3 f(x_1) + 3 f(x_2) + f(x_3)\\right) 
 * \\end{equation*}}
 * <li> Boole's
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{equation*}
 * \\frac{b-a}{45}\\left(14 f(x_0) + 64 f(x_1) + 24 f(x_2) + 64 f(x_3) + 14 f(x_4)\\right) 
 * \\end{equation*}}
 * </ul>
 */
public class NewtonCotesIntegrator1D extends Integrator1D<Double, Function1D<Double, Double>, Double> {
  private static final Logger s_logger = LoggerFactory.getLogger(NewtonCotesIntegrator1D.class);
  private final RuleType _ruleType;
  private final int _n;

  /**
   * 
   */
  public enum RuleType {
    /**
     * 
     */
    RIGHT_HAND,
    /**
     * 
     */
    LEFT_HAND,
    /**
     * 
     */
    MID_POINT,
    /**
     * 
     */
    TRAPEZOIDAL,
    /**
     * 
     */
    SIMPSONS,
    /**
     * 
     */
    BOOLES
  }

  public NewtonCotesIntegrator1D(final RuleType ruleType) {
    this(ruleType, 64);
  }

  public NewtonCotesIntegrator1D(final RuleType ruleType, final int n) {
    if (ruleType == null) {
      throw new IllegalArgumentException("Rule type cannot be null");
    }
    if (n < 1) {
      throw new IllegalArgumentException("Must have a positive number of divisions");
    }
    _ruleType = ruleType;
    _n = n;
  }

  @Override
  public Double integrate(final Function1D<Double, Double> f, final Double lower, final Double upper) {
    if (f == null) {
      throw new IllegalArgumentException("Function was null");
    }
    if (lower == null) {
      throw new IllegalArgumentException("Lower bound was null");
    }
    if (upper == null) {
      throw new IllegalArgumentException("Upper bound was null");
    }
    double x1, x2;
    int multiplier;
    if (lower < upper) {
      x1 = lower;
      x2 = upper;
      multiplier = 1;
    } else {
      x2 = lower;
      x1 = upper;
      multiplier = -1;
      s_logger.info("Upper bound was less than lower bound; swapping bounds and negating result");
    }
    final double dx = (x2 - x1) / _n;
    final double[] x = getAbscissas(x1, dx);
    final double[] y = new double[x.length];
    for (int i = 0; i < _n; i++) {
      y[i] = f.evaluate(x[i]);
    }
    double result = 0;
    switch (_ruleType) {
      case RIGHT_HAND:
        result = getRightHand(dx, y);
        break;
      case LEFT_HAND:
        result = getLeftHand(dx, y);
        break;
      case MID_POINT:
        result = getMidPoint(dx, y);
        break;
      case TRAPEZOIDAL:
        result = getTrapezoidal(dx, y);
        break;
      case SIMPSONS:
        result = getSimpsons(dx, y);
        break;
      case BOOLES:
        result = getBooles(dx, y);
        break;
      default:
        throw new IllegalArgumentException("RuleType does not have corresponding code");
    }
    return multiplier * result;
  }

  private double[] getAbscissas(final double lower, final double dx) {
    final double[] result = new double[_n];
    switch (_ruleType) {
      case RIGHT_HAND:
        result[0] = dx + lower;
        break;
      case MID_POINT:
        result[0] = lower + dx / 2.;
        break;
      default:
        result[0] = lower;
    }
    for (int i = 1; i < _n; i++) {
      result[i] = result[i - 1] + dx;
    }
    return result;
  }

  private double getRightHand(final double dx, final double[] y) {
    double result = 0;
    for (int i = 1; i < _n; i++) {
      result += dx * y[i];
    }
    return result;
  }

  private double getLeftHand(final double dx, final double[] y) {
    double result = 0;
    for (int i = 1; i < _n; i++) {
      result += dx * y[i - 1];
    }
    return result;
  }

  private double getMidPoint(final double dx, final double[] y) {
    double result = 0;
    for (int i = 1; i < _n; i++) {
      result += dx * y[i - 1];
    }
    return result;
  }

  private double getTrapezoidal(final double dx, final double[] y) {
    double result = 0;
    for (int i = 1; i < _n; i++) {
      result += dx * (y[i] + y[i - 1]);
    }
    return result * 0.5;
  }

  private double getSimpsons(final double dx, final double[] y) {
    double result = 0;
    final int remainder = _n % 3;
    if (remainder == 1) {
      result += 0.5 * (y[0] + y[1]) * dx;
    } else if (remainder == 2) {
      result += 1. / 3. * (y[0] + 4 * y[1] + y[2]);
    }
    int j;
    for (int i = remainder; i < _n / 3; i++) {
      j = i == 0 ? 0 : 3 * i - 2;
      result += 3. * (y[j] + 3 * y[j + 1] + 3 * y[j + 2] + y[j + 3]) * dx / 8.;
    }
    return result;
  }

  private double getBooles(final double dx, final double[] y) {
    double result = 0;
    final int remainder = _n % 4;
    if (remainder != 0) {
      result = getSimpsons(dx, Arrays.copyOfRange(y, 0, remainder));
    }
    int j;
    for (int i = remainder; i < _n / 4; i++) {
      j = i == 0 ? 0 : 4 * i - 3;
      result += (14 * y[j] + 64 * y[j + 1] + 24 * y[j + 2] + 64 * y[j + 3] + 14 * y[j + 4]) * dx / 45.;
    }
    return result;
  }
}
