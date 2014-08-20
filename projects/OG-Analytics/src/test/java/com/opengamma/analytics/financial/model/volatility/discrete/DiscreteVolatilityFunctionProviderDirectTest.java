/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.discrete;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.IdentityMatrix;
import com.opengamma.analytics.util.AssertMatrix;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class DiscreteVolatilityFunctionProviderDirectTest {

  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  @Test
  public void test() {
    final int nSamples = 10;
    final List<DoublesPair> points = new ArrayList<>(nSamples);
    final DoubleMatrix1D x = new DoubleMatrix1D(nSamples);
    for (int i = 0; i < nSamples; i++) {
      final double t = RANDOM.nextDouble() * 20.0;
      final double k = RANDOM.nextDouble() * 0.15;
      points.add(DoublesPair.of(t, k));
      x.getData()[i] = RANDOM.nextDouble();
    }

    final DiscreteVolatilityFunctionProvider pro = new DiscreteVolatilityFunctionProviderDirect();
    final DiscreteVolatilityFunction func = pro.from(points);
    final DoubleMatrix1D y = func.evaluate(x);
    AssertMatrix.assertEqualsVectors(x, y, 0.0);
    final DoubleMatrix2D jac = func.calculateJacobian(x);
    AssertMatrix.assertEqualsMatrix(new IdentityMatrix(nSamples), jac, 0.0);

    assertEquals(nSamples, func.getLengthOfDomain());
    assertEquals(nSamples, func.getLengthOfRange());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthTest() {
    final int nSamples = 10;
    final List<DoublesPair> points = new ArrayList<>(nSamples);
    final DoubleMatrix1D x = new DoubleMatrix1D(nSamples + 1);
    x.getData()[0] = RANDOM.nextDouble();
    for (int i = 0; i < nSamples; i++) {
      final double t = RANDOM.nextDouble() * 20.0;
      final double k = RANDOM.nextDouble() * 0.15;
      points.add(DoublesPair.of(t, k));
      x.getData()[i + 1] = RANDOM.nextDouble();
    }

    final DiscreteVolatilityFunctionProvider pro = new DiscreteVolatilityFunctionProviderDirect();
    final DiscreteVolatilityFunction func = pro.from(points);
    func.evaluate(x);
  }
}
