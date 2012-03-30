/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import com.opengamma.analytics.math.integration.Integrator1D;
import com.opengamma.analytics.math.integration.RombergIntegrator1D;


public class RombergIntegrator1DTest extends Integrator1DTestCase {
  private static final Integrator1D<Double, Double> INTEGRATOR = new RombergIntegrator1D();

  @Override
  public Integrator1D<Double, Double> getIntegrator() {
    return INTEGRATOR;
  }
}
