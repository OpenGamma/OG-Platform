/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math.analysis.integration.UnivariateRealIntegrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;

/**
 * Simpson's integration rule is a Newton-Cotes formula that approximates the
 * function to be integrated with quadratic polynomials before performing the
 * integration. For a function $f(x)$, if three points $x_1$, $x_2$ and $x_3$
 * are equally spaced on the abscissa with $x_2 - x_1 = h$ then
 * $$
 * \begin{align*}
 * \int^{x_3} _{x_1} f(x)dx \approx \frac{1}{3}h(f(x_1) + 4f(x_2) + f(x_3))
 * \end{align*}
 * $$
 * <p> 
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/analysis/integration/SimpsonIntegrator.html">Commons Math library implementation</a> 
 * of Simpson integration.
 */
public class SimpsonIntegrator1D extends Integrator1D<Double, Double> {
  private static final Logger s_logger = LoggerFactory.getLogger(SimpsonIntegrator1D.class);
  private final UnivariateRealIntegrator _integrator = new SimpsonIntegrator();

  /**
   * Simpson's integration method. Note that the Commons implementation fails if the lower bound is larger than the upper - 
   * in this case, the bounds are reversed and the result negated. 
   * @param f The function to integrate, not null
   * @param lower The lower bound, not null
   * @param upper The upper bound, not null
   * @return The result of the integration
   */
  @Override
  public Double integrate(final Function1D<Double, Double> f, final Double lower, final Double upper) {
    Validate.notNull(f, "function");
    Validate.notNull(lower, "lower bound");
    Validate.notNull(upper, "upper bound");
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
