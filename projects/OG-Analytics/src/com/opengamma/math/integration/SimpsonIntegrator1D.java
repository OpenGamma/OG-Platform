/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math.analysis.integration.UnivariateRealIntegrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * 
 */
public class SimpsonIntegrator1D extends Integrator1D<Double, Function1D<Double, Double>, Double> {
  private static final Logger s_logger = LoggerFactory.getLogger(SimpsonIntegrator1D.class);
  private final UnivariateRealIntegrator _integrator = new SimpsonIntegrator();

  @Override
  public Double integrate(final Function1D<Double, Double> f, final Double lower, final Double upper) {
    if (f == null) {
      throw new IllegalArgumentException("Function was null");
    }
    if (lower == null) {
      throw new IllegalArgumentException("Lower bound was null");
    }
    if (upper == null) {
      throw new IllegalArgumentException("Upper bound was null");
    }
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
