/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import org.apache.commons.math.special.Gamma;

import com.opengamma.math.function.Function1D;

/**
 * 
 * The gamma function is a generalization of the factorial to complex and real numbers and is defined by the integral
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{equation*}
 * \\Gamma(z)=\\int_0^\\infty t^{z-1}e^{-t}dt
 * \\end{equation*}}
 * and is related to the factorial by
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{equation*}
 * \\Gamma(n+1)=n!
 * \\end{equation*}}
 * It is analytic everywhere but {@latex.inline $z=0, -1, -2, \\ldots$}. 
 * <p>
 * This class is a wrapper for the Commons Math library implementation of the gamma function <a href="http://commons.apache.org/math/api-2.1/index.html">
 * 
 */
public class GammaFunction extends Function1D<Double, Double> {

  @Override
  public Double evaluate(final Double x) {
    return Math.exp(Gamma.logGamma(x));
  }

}
