/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.Surface;

/**
 * PDE Data bundle representing PDEs of the type
 * $\frac{\partial f}{\partial t} + a(t,x)\frac{\partial^2}{\partial x^2}\left[ \alpha(t,x) f \right] + * b(t,x)\frac{\partial}{\partial x}\left[\beta(t,x) f \right] + c(t,x)f = 0$
 * , which includes the Fokker-Planck PDE.
 */
public class ExtendedConvectionDiffusionPDEDataBundle extends ConvectionDiffusionPDEDataBundle {
  private final Surface<Double, Double, Double> _alpha;
  private final Surface<Double, Double, Double> _beta;

  /**
   * PDE Data bundle representing PDEs of the type
   * $\frac{\partial f}{\partial t} + a(t,x)\frac{\partial^2}{\partial x^2}\left[ \alpha(t,x) f \right] + * b(t,x)\frac{\partial}{\partial x}\left[\beta(t,x) f \right] + c(t,x)f = 0$
   * @param a $a(t,x)$
   * @param b $b(t,x)$
   * @param c $c(t,x)$
   * @param alpha $\alpha(t,x)$
   * @param beta $\beta(t,x)$
   * @param initialCondition The initial condition $f(0,x)$
   */
  public ExtendedConvectionDiffusionPDEDataBundle(final Surface<Double, Double, Double> a, final Surface<Double, Double, Double> b, final Surface<Double, Double, Double> c,
      final Surface<Double, Double, Double> alpha, final Surface<Double, Double, Double> beta, final Function1D<Double, Double> initialCondition) {
    super(a, b, c, initialCondition);
    Validate.notNull(alpha, "null alpha");
    Validate.notNull(beta, "null beta");
    _alpha = alpha;
    _beta = beta;
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
}
