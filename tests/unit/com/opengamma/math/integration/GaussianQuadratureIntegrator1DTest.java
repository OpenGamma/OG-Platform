/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class GaussianQuadratureIntegrator1DTest {

  @Test
  public void test() {
    final GeneratingFunction<Double, GaussianQuadratureFunction> generator = new GaussLegendreOrthogonalPolynomialGeneratingFunction();
    final GaussianQuadratureFunction f = generator.generate(5, new Double[] { -1., 1. });
    final Double[] weights = f.getWeights();
    final Double[] abscissas = f.getAbscissas();
    for (int i = 0; i < weights.length; i++) {
      System.out.println(weights[i] + " " + abscissas[i]);
    }
  }
}
