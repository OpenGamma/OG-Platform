/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.PolynomialFunction1D;

/**
 * 
 * @author emcleod
 */
public class PolynomialInterpolator1DTest {

  @Test(expected=IllegalArgumentException.class)
  public void illegalDegree() {
    new PolynomialInterpolator1D(0);
  }

  @Test
  public void testWithBadInputs() {
    final Interpolator1D interpolator = new PolynomialInterpolator1D(3);
    try {
      interpolator.interpolate((Map<Double, Double>)null, 3.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final Map<Double, Double> data = new HashMap<Double, Double>();
    data.put(1., 2.);
    data.put(3., 4.);
    try {
      interpolator.interpolate(data, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    data.put(5., 6.);
    try {
      interpolator.interpolate(data, 1.4);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    data.put(7., 8.);
    data.put(9., 10.);
    try {
      interpolator.interpolate(data, 8.5);
      fail();
    } catch (final InterpolationException e) {
      // Expected
    }
    data.put(7. + 1e-15, 11.);
    try {
      interpolator.interpolate(data, 3.);
      fail();
    } catch (final InterpolationException e) {
      // Expected
    }
  }

  @Test
  public void testInterpolation() {
    final double eps = 1e-9;
    final Function1D<Double, Double> quadratic = new PolynomialFunction1D(new Double[] { -4., 3., 1. });
    final Function1D<Double, Double> quartic = new PolynomialFunction1D(new Double[] { -4., 3., 1., 1., 1. });
    final Map<Double, Double> quadraticData = new HashMap<Double, Double>();
    final Map<Double, Double> quarticData = new HashMap<Double, Double>();
    double x;
    for (int i = 0; i < 10; i++) {
      x = i / 10.;
      quadraticData.put(x, quadratic.evaluate(x));
      quarticData.put(x, quartic.evaluate(x));
    }
    x = 0.35;
    final Interpolator1D quadraticInterpolator = new PolynomialInterpolator1D(2);
    final InterpolationResult<Double> quadraticResult = quadraticInterpolator.interpolate(quadraticData, x);
    final Interpolator1D quarticInterpolator = new PolynomialInterpolator1D(4);
    final InterpolationResult<Double> quarticResult = quarticInterpolator.interpolate(quarticData, x);
    assertEquals(quadraticResult.getResult(), quadratic.evaluate(x), eps);
    assertEquals(quarticResult.getResult(), quartic.evaluate(x), eps);
    final InterpolationResult<Double> underFittedEstimate = quadraticInterpolator.interpolate(quarticData, x);
    assertEquals(underFittedEstimate.getResult(), quartic.evaluate(x), Math.abs(underFittedEstimate.getErrorEstimate()));
    
    final InterpolationResult<Double> overFittedEstimate = quarticInterpolator.interpolate(quadraticData, x);
    assertTrue((overFittedEstimate.getErrorEstimate() > 3e-16) && (overFittedEstimate.getErrorEstimate() < 3.2e-16));
    assertEquals(overFittedEstimate.getResult(), quadratic.evaluate(x), eps);
    assertTrue(Math.abs(overFittedEstimate.getErrorEstimate()) < Math.abs(underFittedEstimate.getErrorEstimate()));
  }
}
