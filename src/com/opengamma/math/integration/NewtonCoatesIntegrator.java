package com.opengamma.math.integration;

import java.util.Arrays;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 */

public class NewtonCoatesIntegrator extends Integrator1D<Double, Function1D<Double, Double, MathException>, Double, MathException> {
  private final RuleType _ruleType;
  private final int _n;

  public enum RuleType {
    RIGHT_HAND, LEFT_HAND, MID_POINT, TRAPEZOIDAL, SIMPSONS, BOOLES
  }

  public NewtonCoatesIntegrator(RuleType ruleType, int n) {
    _ruleType = ruleType;
    _n = n;
  }

  @Override
  public Double integrate(Function1D<Double, Double, MathException> f, Double lower, Double upper) throws MathException {
    double dx = (upper - lower) / _n;
    double[] x = getAbscissas(lower, dx);
    double[] y = new double[x.length];
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
        throw new IllegalArgumentException("RuleType without implemented the code");
    }
    return result;
  }

  private double[] getAbscissas(double lower, double dx) {
    double[] result = new double[_n];
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

  private double getRightHand(double dx, double[] y) {
    double result = 0;
    for (int i = 1; i < _n; i++) {
      result += dx * y[i];
    }
    return result;
  }

  private double getLeftHand(double dx, double[] y) {
    double result = 0;
    for (int i = 1; i < _n; i++) {
      result += dx * y[i - 1];
    }
    return result;
  }

  private double getMidPoint(double dx, double[] y) {
    double result = 0;
    for (int i = 1; i < _n; i++) {
      result += dx * y[i - 1];
    }
    return result;
  }

  private double getTrapezoidal(double dx, double[] y) {
    double result = 0;
    for (int i = 1; i < _n; i++) {
      result += dx * (y[i] + y[i - 1]);
    }
    return result * 0.5;
  }

  private double getSimpsons(double dx, double[] y) {
    double result = 0;
    int rem = _n % 3;
    if (rem == 1) {
      result += 0.5 * (y[0] + y[1]) * dx;
    } else if (rem == 2) {
      result += 1. / 3. * (y[0] + 4 * y[1] + y[2]);
    }
    int j;
    for (int i = rem; i < _n / 3; i++) {
      // TODO doesn't work
      j = 3 * i - 2;
      result += 3. * (y[j] + 3 * y[j + 1] + 3 * y[j + 2] + y[j + 3]) * dx / 8.;
    }
    return result;
  }

  private double getBooles(double dx, double[] y) {
    double result = 0;
    int rem = _n % 4;
    if (rem != 0) {
      result = getSimpsons(dx, Arrays.copyOfRange(y, 0, rem));
    }
    int j;
    for (int i = rem; i < _n / 4; i++) {
      // TODO doesn't work
      j = i * 4 - 3;
      result += (14 * y[j] + 64 * y[j + 1] + 24 * y[j + 2] + 64 * y[j + 3] + 14 * y[j + 4]) * dx / 45.;
    }
    return result;
  }
}
