/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math.analysis.integration.UnivariateRealIntegrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;

/**
 * The trapezoid integration rule is a two-point Newton-Cotes formula that
 * approximates the area under the curve as a trapezoid. For a function $f(x)$,
 * $$
 * \begin{align*}
 * \int^{x_2} _{x_1} f(x)dx \approx \frac{1}{2}(x_2 - x_1)(f(x_1) + f(x_2))
 * \end{align*}
 * $$
 * <p> 
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/analysis/integration/TrapezoidIntegrator.html">Commons Math library implementation</a> 
 * of trapezoidal integration.
 */
public class ExtendedTrapezoidIntegrator1D extends Integrator1D<Double, Double> {
  private static final Logger s_logger = LoggerFactory.getLogger(ExtendedTrapezoidIntegrator1D.class);
  private static final UnivariateRealIntegrator INTEGRATOR = new TrapezoidIntegrator();

  /**
   * Trapezoid integration method. Note that the Commons implementation fails if the lower bound is larger than the upper - 
   * in this case, the bounds are reversed and the result negated. 
   * {@inheritDoc}
   */
  @Override
  public Double integrate(final Function1D<Double, Double> f, final Double lower, final Double upper) {
    Validate.notNull(f);
    Validate.notNull(lower);
    Validate.notNull(upper);
    try {
      if (lower < upper) {
        return INTEGRATOR.integrate(CommonsMathWrapper.wrapUnivariate(f), lower, upper);
      }
      s_logger.info("Upper bound was less than lower bound; swapping bounds and negating result");
      return -INTEGRATOR.integrate(CommonsMathWrapper.wrapUnivariate(f), upper, lower);
    } catch (final FunctionEvaluationException e) {
      throw new MathException(e);
    } catch (final org.apache.commons.math.ConvergenceException e) {
      throw new MathException(e);
    }
  }
}
