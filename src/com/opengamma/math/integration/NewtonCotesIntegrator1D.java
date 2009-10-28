/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import java.util.Arrays;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class NewtonCotesIntegrator1D extends Integrator1D<Double, Function1D<Double, Double>, Double> {
  private final RuleType _ruleType;
  private final int _n;

  public enum RuleType {
    RIGHT_HAND, LEFT_HAND, MID_POINT, TRAPEZOIDAL, SIMPSONS, BOOLES
  }

  public NewtonCotesIntegrator1D(final RuleType ruleType) {
    this(ruleType, 100000);
  }

  public NewtonCotesIntegrator1D(final RuleType ruleType, final int n) {
    if (ruleType == null)
      throw new IllegalArgumentException("Rule type cannot be null");
    if (n < 1)
      throw new IllegalArgumentException("Must have a positive number of divisions");
    _ruleType = ruleType;
    _n = n;
  }

  @Override
  public Double integrate(final Function1D<Double, Double> f, final Double lower, final Double upper) {
    if (f == null)
      throw new IllegalArgumentException("Function was null");
    if (lower == null)
      throw new IllegalArgumentException("Lower bound was null");
    if (upper == null)
      throw new IllegalArgumentException("Upper bound was null");
    final double dx = (upper - lower) / _n;
    final double[] x = getAbscissas(lower, dx);
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
    return result;
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
    final int rem = _n % 3;
    if (rem == 1) {
      result += 0.5 * (y[0] + y[1]) * dx;
    } else if (rem == 2) {
      result += 1. / 3. * (y[0] + 4 * y[1] + y[2]);
    }
    int j;
    for (int i = rem; i < _n / 3; i++) {
      j = i == 0 ? 0 : 3 * i - 2;
      result += 3. * (y[j] + 3 * y[j + 1] + 3 * y[j + 2] + y[j + 3]) * dx / 8.;
    }
    return result;
  }

  private double getBooles(final double dx, final double[] y) {
    double result = 0;
    final int rem = _n % 4;
    if (rem != 0) {
      result = getSimpsons(dx, Arrays.copyOfRange(y, 0, rem));
    }
    int j;
    for (int i = rem; i < _n / 4; i++) {
      j = i == 0 ? 0 : 4 * i - 3;
      result += (14 * y[j] + 64 * y[j + 1] + 24 * y[j + 2] + 64 * y[j + 3] + 14 * y[j + 4]) * dx / 45.;
    }
    return result;
  }
}
