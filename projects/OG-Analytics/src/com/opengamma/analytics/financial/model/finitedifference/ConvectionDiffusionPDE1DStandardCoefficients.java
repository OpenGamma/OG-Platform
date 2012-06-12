/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.surface.Surface;

/**
 * $$
 * This class contains the coefficients terms ($a(t,x), b(t,x), c(x,t)$) that are functions of $t$ and $x$  but NOT $V$ in the
 * PDE
 * \[
 * \frac{\partial V}{\partial t}+a(t,x)\frac{\partial^2 V}{\partial x^2}+b(t,x)\frac{\partial V}{\partial x2}+c(t,x)V=0
 * \]
 * where of course $V$ is also a function of $t$ and $x$
 * $$
 */
public class ConvectionDiffusionPDE1DStandardCoefficients implements ConvectionDiffusionPDE1DCoefficients {

  private final Surface<Double, Double, Double> _a;
  private final Surface<Double, Double, Double> _b;
  private final Surface<Double, Double, Double> _c;

  public ConvectionDiffusionPDE1DStandardCoefficients(final Surface<Double, Double, Double> a, final Surface<Double, Double, Double> b,
      final Surface<Double, Double, Double> c) {
    Validate.notNull(a, "null a");
    Validate.notNull(b, "null b");
    Validate.notNull(c, "null c");
    _a = a;
    _b = b;
    _c = c;
  }

  public double getA(final double t, final double x) {
    return _a.getZValue(t, x);
  }

  public double getB(final double t, final double x) {
    return _b.getZValue(t, x);
  }

  public double getC(final double t, final double x) {
    return _c.getZValue(t, x);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_a == null) ? 0 : _a.hashCode());
    result = prime * result + ((_b == null) ? 0 : _b.hashCode());
    result = prime * result + ((_c == null) ? 0 : _c.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ConvectionDiffusionPDE1DStandardCoefficients other = (ConvectionDiffusionPDE1DStandardCoefficients) obj;
    if (_a == null) {
      if (other._a != null) {
        return false;
      }
    } else if (!_a.equals(other._a)) {
      return false;
    }
    if (_b == null) {
      if (other._b != null) {
        return false;
      }
    } else if (!_b.equals(other._b)) {
      return false;
    }
    if (_c == null) {
      if (other._c != null) {
        return false;
      }
    } else if (!_c.equals(other._c)) {
      return false;
    }
    return true;
  }

}
