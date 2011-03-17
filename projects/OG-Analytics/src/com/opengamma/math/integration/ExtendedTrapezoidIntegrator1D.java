/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math.analysis.integration.UnivariateRealIntegrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * The trapezoid integration rule is a two-point Newton-Cotes formula that approximates the area under the curve
 * as a trapezoid. For a function {@latex.inline $f(x)$}
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\int^{x_2} _{x_1} f(x)dx \\approx \\frac{1}{2}(x_2 - x_1)(f(x_1) + f(x_2))
 * }
 * <p> 
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/analysis/integration/TrapezoidIntegrator.html">Commons Math library implementation</a> 
 * of trapezoidal integration.
 */
public class ExtendedTrapezoidIntegrator1D extends Integrator1D<Double, Double> {
  private static final Logger s_logger = LoggerFactory.getLogger(ExtendedTrapezoidIntegrator1D.class);
  private final UnivariateRealIntegrator _integrator = new TrapezoidIntegrator();

  /**
   * Trapezoid integration method. Note that the Commons implementation fails if the lower bound is larger than the upper - 
   * in this case, the bounds are reversed and the result negated. 
   * @param f The function to integrate, not null
   * @param lower The lower bound, not null
   * @param upper The upper bound, not null
   * @return The result of the integration
   */
  @Override
  public Double integrate(final Function1D<Double, Double> f, final Double lower, final Double upper) {
    Validate.notNull(f);
    Validate.notNull(lower);
    Validate.notNull(upper);
    try {
      if (lower < upper) {
        return _integrator.integrate(CommonsMathWrapper.wrapUnivariate(f), lower, upper);
      }
      s_logger.info("Upper bound was less than lower bound; swapping bounds and negating result");
      return -_integrator.integrate(CommonsMathWrapper.wrapUnivariate(f), upper, lower);
    } catch (final FunctionEvaluationException e) {
      throw new MathException(e);
    } catch (final org.apache.commons.math.ConvergenceException e) {
      throw new MathException(e);
    }
  }
}
