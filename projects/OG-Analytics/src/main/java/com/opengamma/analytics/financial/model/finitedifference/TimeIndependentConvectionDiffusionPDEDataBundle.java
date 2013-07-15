/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.function.Function1D;

/**
 * Describes a partial differential for a function $V(t,x)$, with the initial
 * $\frac{\partial V}{\partial t} + a(x) \frac{\partial^2 V}{\partial x^2} + b(x) \frac{\partial V}{\partial x} + c(x)V = 0$
 * Note that $a$, $b$ and $c$ are functions of $x$ only so the matrix system of the PDE
 * solver need only be solved once (provided that the boundary conditions are
 * only time independent) 
 */
public class TimeIndependentConvectionDiffusionPDEDataBundle {

  private final Curve<Double, Double> _a;
  private final Curve<Double, Double> _b;
  private final Curve<Double, Double> _c;
  private final Function1D<Double, Double> _initialCondition;

  public TimeIndependentConvectionDiffusionPDEDataBundle(final Curve<Double, Double> a, final Curve<Double, Double> b, final Curve<Double, Double> c,
      final Function1D<Double, Double> initialCondition) {
    Validate.notNull(a, "null a");
    Validate.notNull(b, "null b");
    Validate.notNull(c, "null c");
    Validate.notNull(initialCondition, "null initial Condition");
    _a = a;
    _b = b;
    _c = c;
    _initialCondition = initialCondition;
  }

  public double getA(final double x) {
    return _a.getYValue(x);
  }

  public double getB(final double x) {
    return _b.getYValue(x);
  }

  public double getC(final double x) {
    return _c.getYValue(x);
  }

  public double getInitialValue(final double x) {
    return _initialCondition.evaluate(x);
  }

}
