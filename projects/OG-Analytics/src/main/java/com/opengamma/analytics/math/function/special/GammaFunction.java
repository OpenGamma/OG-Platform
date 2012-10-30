/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import org.apache.commons.math.special.Gamma;

import com.opengamma.analytics.math.function.Function1D;

/**
 * 
 * The gamma function is a generalization of the factorial to complex and real
 * numbers. It is defined by the integral:
 * $$
 * \begin{equation*}
 * \Gamma(z)=\int_0^\infty t^{z-1}e^{-t}dt
 * \end{equation*}
 * $$
 * and is related to the factorial by
 * $$
 * \begin{equation*}
 * \Gamma(n+1)=n!
 * \end{equation*}
 * $$
 * It is analytic everywhere but $z=0, -1, -2, \ldots$
 * <p>
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/special/Gamma.html">Commons Math library implementation</a> 
 * of the Gamma function.
 * 
 */
public class GammaFunction extends Function1D<Double, Double> {

  @Override
  public Double evaluate(final Double x) {
    if (x > 0.0) {
      return Math.exp(Gamma.logGamma(x));
    }
    return Math.PI / Math.sin(Math.PI * x) / evaluate(1 - x);
  }

}
