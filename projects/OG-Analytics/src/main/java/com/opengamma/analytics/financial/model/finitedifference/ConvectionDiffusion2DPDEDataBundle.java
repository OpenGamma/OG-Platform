/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.cube.Cube;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Describes a partial differential for a function $V(t,x,y)$, with the initial condition $V(0,x,y) = g(x,y)$
 *  $\frac{\partial V}{\partial t} + a(t,x,y) \frac{\partial^2 V}{\partial x^2} + b(t,x,y) \frac{\partial V}{\partial x} + c(t,x,y)V 
 *  + d(t,x,y)\frac{\partial^2 V}{\partial y^2} + e(t,x,y) \frac{\partial^2 V}{\partial x \partial y} + f(t,x,y) \frac{\partial V}{\partial y}  = 0$
 * @deprecated replace with a PDE2DDateBundle
 */
@Deprecated
public class ConvectionDiffusion2DPDEDataBundle {

  private final Cube<Double, Double, Double, Double> _a;
  private final Cube<Double, Double, Double, Double> _b;
  private final Cube<Double, Double, Double, Double> _c;
  private final Cube<Double, Double, Double, Double> _d;
  private final Cube<Double, Double, Double, Double> _e;
  private final Cube<Double, Double, Double, Double> _f;

  private final Surface<Double, Double, Double> _initialCondition;

  public ConvectionDiffusion2DPDEDataBundle(final Cube<Double, Double, Double, Double> a,
      final Cube<Double, Double, Double, Double> b,
      final Cube<Double, Double, Double, Double> c,
      final Cube<Double, Double, Double, Double> d,
      final Cube<Double, Double, Double, Double> e,
      final Cube<Double, Double, Double, Double> f,
      final Surface<Double, Double, Double> initialCondition) {
    Validate.notNull(a, "null a");
    Validate.notNull(b, "null b");
    Validate.notNull(c, "null c");
    Validate.notNull(d, "null d");
    Validate.notNull(e, "null e");
    Validate.notNull(f, "null f");
    Validate.notNull(initialCondition, "null initial Condition");
    _a = a;
    _b = b;
    _c = c;
    _d = d;
    _e = e;
    _f = f;
    _initialCondition = initialCondition;
  }

  public double getA(final double t, final double x, final double y) {
    return _a.getValue(t, x, y);
  }

  public double getB(final double t, final double x, final double y) {
    return _b.getValue(t, x, y);
  }

  public double getC(final double t, final double x, final double y) {
    return _c.getValue(t, x, y);
  }

  public double getD(final double t, final double x, final double y) {
    return _d.getValue(t, x, y);
  }

  public double getE(final double t, final double x, final double y) {
    return _e.getValue(t, x, y);
  }

  public double getF(final double t, final double x, final double y) {
    return _f.getValue(t, x, y);
  }

  public double getInitialValue(final double x, final double y) {
    return _initialCondition.getZValue(x, y);
  }
}
