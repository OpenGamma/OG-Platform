/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.Surface;

/**
 * 
 */
public class ExtendedConvectionDiffusionPDEDataBundle extends ConvectionDiffusionPDEDataBundle {
  private final Surface<Double, Double, Double> _alpha;
  private final Surface<Double, Double, Double> _beta;

  /**
   * @param a a
   * @param b b
   * @param c c
   * @param alpha alpha
   * @param beta beta
   * @param initialCondition The initial condition
   */
  public ExtendedConvectionDiffusionPDEDataBundle(final Surface<Double, Double, Double> a, final Surface<Double, Double, Double> b, final Surface<Double, Double, Double> c,
      final Surface<Double, Double, Double> alpha, final Surface<Double, Double, Double> beta, final Function1D<Double, Double> initialCondition) {
    super(a, b, c, initialCondition);
    Validate.notNull(alpha, "null alpha");
    Validate.notNull(beta, "null beta");
    _alpha = alpha;
    _beta = beta;
  }

  public double getAlpha(final double t, final double x) {
    return _alpha.getZValue(t, x);
  }

  public double getBeta(final double t, final double x) {
    return _beta.getZValue(t, x);
  }
}
