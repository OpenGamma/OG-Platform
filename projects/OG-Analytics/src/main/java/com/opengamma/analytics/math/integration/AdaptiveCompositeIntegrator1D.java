/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.function.Function1D;

/**
 * Adaptive composite integrator: step size is set to be small if functional variation of integrand is large
 * The integrator in individual intervals (base integrator) should be specified by constructor
 */
public class AdaptiveCompositeIntegrator1D extends Integrator1D<Double, Double> {
  private static final Logger s_logger = LoggerFactory.getLogger(AdaptiveCompositeIntegrator1D.class);
  private final Integrator1D<Double, Double> _integrator;
  private static final int MAX_IT = 15;
  private final double _gain;
  private final double _tol;

  /**
   * @param integrator The base integrator 
   */
  public AdaptiveCompositeIntegrator1D(final Integrator1D<Double, Double> integrator) {
    Validate.notNull(integrator, "integrator");
    _integrator = integrator;
    _gain = 15.;
    _tol = 1.e-13;
  }

  /**
   * @param integrator The base integrator
   * @param gain The gain ratio
   * @param tol The tolerance
   */
  public AdaptiveCompositeIntegrator1D(final Integrator1D<Double, Double> integrator, final double gain, final double tol) {
    Validate.notNull(integrator, "integrator");
    _integrator = integrator;
    _gain = gain;
    _tol = tol;
  }

  @Override
  public Double integrate(final Function1D<Double, Double> f, final Double lower, final Double upper) {
    Validate.notNull(f, "f");
    Validate.notNull(lower, "lower bound");
    Validate.notNull(upper, "upper bound");
    try {
      if (lower < upper) {
        return integration(f, lower, upper);
      }
      s_logger.info("Upper bound was less than lower bound; swapping bounds and negating result");
      return -integration(f, upper, lower);
    } catch (final Exception e) {
      throw new OpenGammaRuntimeException("function evaluation returned NaN or Inf");
    }
  }

  private Double integration(final Function1D<Double, Double> f, final Double lower, final Double upper) {
    final double res = _integrator.integrate(f, lower, upper);
    return integrationRec(f, lower, upper, res, MAX_IT);
  }

  private double integrationRec(final Function1D<Double, Double> f, final double lower, final double upper, final double res, final double counter) {
    final double localTol = _gain * _tol;
    final double half = 0.5 * (lower + upper);
    final double newResDw = _integrator.integrate(f, lower, half);
    final double newResUp = _integrator.integrate(f, half, upper);
    final double newRes = newResUp + newResDw;

    if (Math.abs(res - newRes) < localTol || counter == 0 || (Math.abs(res) < 1.e-14 && Math.abs(newResUp) < 1.e-14 && Math.abs(newResDw) < 1.e-14)) {
      return newRes + (newRes - res) / _gain;
    }

    return integrationRec(f, lower, half, newResDw, counter - 1) + integrationRec(f, half, upper, newResUp, counter - 1);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_gain);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _integrator.hashCode();
    temp = Double.doubleToLongBits(_tol);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof AdaptiveCompositeIntegrator1D)) {
      return false;
    }
    AdaptiveCompositeIntegrator1D other = (AdaptiveCompositeIntegrator1D) obj;
    if (Double.doubleToLongBits(_gain) != Double.doubleToLongBits(other._gain)) {
      return false;
    }
    if (!_integrator.equals(other._integrator)) {
      return false;
    }
    if (Double.doubleToLongBits(_tol) != Double.doubleToLongBits(other._tol)) {
      return false;
    }
    return true;
  }

}
