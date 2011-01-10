/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class Integrator1DTest {
  private static final Integrator1D<Double, Function1D<Double, Double>, Double> INTEGRATOR = new Integrator1D<Double, Function1D<Double, Double>, Double>() {

    @Override
    public Double integrate(final Function1D<Double, Double> f, final Double lower, final Double upper) {
      return 0.;
    }

  };
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 0.;
    }

  };
  private static final Double[] L = new Double[] {1.3};
  private static final Double[] U = new Double[] {3.4};

  @Test
  public void testInputs() {
    try {
      INTEGRATOR.integrate(null, L, U);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      INTEGRATOR.integrate(F, null, U);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      INTEGRATOR.integrate(F, new Double[0], U);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      INTEGRATOR.integrate(F, new Double[] {null}, U);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      INTEGRATOR.integrate(F, L, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      INTEGRATOR.integrate(F, L, new Double[0]);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      INTEGRATOR.integrate(F, L, new Double[] {null});
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
