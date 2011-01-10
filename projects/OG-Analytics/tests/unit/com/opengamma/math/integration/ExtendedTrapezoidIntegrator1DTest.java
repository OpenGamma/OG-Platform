/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

public class ExtendedTrapezoidIntegrator1DTest extends Integrator1DTestCase {
  private static final Integrator1D<Double, Function1D<Double, Double>, Double> INTEGRATOR = new ExtendedTrapezoidIntegrator1D();

  @Override
  public Integrator1D<Double, Function1D<Double, Double>, Double> getIntegrator() {
    return INTEGRATOR;
  }

  @Override
  @Test
  public void test() {
    super.testNullFunction();
    super.testNullLowerBound();
    super.testNullUpperBound();
    super.test();
  }
}
