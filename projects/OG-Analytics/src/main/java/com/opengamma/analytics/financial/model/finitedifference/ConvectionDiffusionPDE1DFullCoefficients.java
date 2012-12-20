/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.surface.Surface;

/**
* PDE Data bundle representing PDEs of the type
* $\frac{\partial f}{\partial t} + a(t,x)\frac{\partial^2}{\partial x^2}\left[ \alpha(t,x) f \right] +  b(t,x)\frac{\partial}{\partial x}\left[\beta(t,x) f \right] + c(t,x)f = 0$
* , which includes the Fokker-Planck PDE.
*/
public class ConvectionDiffusionPDE1DFullCoefficients implements ConvectionDiffusionPDE1DCoefficients {

  private final Surface<Double, Double, Double> _a;
  private final Surface<Double, Double, Double> _b;
  private final Surface<Double, Double, Double> _c;
  private final Surface<Double, Double, Double> _alpha;
  private final Surface<Double, Double, Double> _beta;

  /**
   * PDE Data bundle representing PDEs of the type
   * $\frac{\partial f}{\partial t} + a(t,x)\frac{\partial^2}{\partial x^2}\left[ \alpha(t,x) f \right] +  b(t,x)\frac{\partial}{\partial x}\left[\beta(t,x) f \right] + c(t,x)f = 0$
   * @param a $a(t,x)$
   * @param b $b(t,x)$
   * @param c $c(t,x)$
   * @param alpha $\alpha(t,x)$
   * @param beta $\beta(t,x)$
   */
  public ConvectionDiffusionPDE1DFullCoefficients(final Surface<Double, Double, Double> a, final Surface<Double, Double, Double> b,
      final Surface<Double, Double, Double> c, final Surface<Double, Double, Double> alpha, final Surface<Double, Double, Double> beta) {

    Validate.notNull(alpha, "null alpha");
    Validate.notNull(beta, "null beta");
    Validate.notNull(a, "null a");
    Validate.notNull(b, "null b");
    Validate.notNull(c, "null c");
    _alpha = alpha;
    _beta = beta;
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

  /**
   * 
   * @param t Time value
   * @param x Space value
   * @return value of $\alpha(t,x)$
   */
  public double getAlpha(final double t, final double x) {
    return _alpha.getZValue(t, x);
  }

  /**
   * 
   * @param t Time value
   * @param x Space value
   * @return value of $\beta(t,x)$
   */
  public double getBeta(final double t, final double x) {
    return _beta.getZValue(t, x);
  }

  /**
   * Gets the coefficients a, b and c as a ParabolicPDECoefficients object. <b> This does not convert ParabolicPDEExtendedCoefficients to ParabolicPDECoefficients</b>
   * @return ParabolicPDECoefficients
   */
  public ConvectionDiffusionPDE1DStandardCoefficients getStandardCoefficients() {
    return new ConvectionDiffusionPDE1DStandardCoefficients(_a, _b, _c);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_a == null) ? 0 : _a.hashCode());
    result = prime * result + ((_alpha == null) ? 0 : _alpha.hashCode());
    result = prime * result + ((_b == null) ? 0 : _b.hashCode());
    result = prime * result + ((_beta == null) ? 0 : _beta.hashCode());
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
    ConvectionDiffusionPDE1DFullCoefficients other = (ConvectionDiffusionPDE1DFullCoefficients) obj;
    if (_a == null) {
      if (other._a != null) {
        return false;
      }
    } else if (!_a.equals(other._a)) {
      return false;
    }
    if (_alpha == null) {
      if (other._alpha != null) {
        return false;
      }
    } else if (!_alpha.equals(other._alpha)) {
      return false;
    }
    if (_b == null) {
      if (other._b != null) {
        return false;
      }
    } else if (!_b.equals(other._b)) {
      return false;
    }
    if (_beta == null) {
      if (other._beta != null) {
        return false;
      }
    } else if (!_beta.equals(other._beta)) {
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
