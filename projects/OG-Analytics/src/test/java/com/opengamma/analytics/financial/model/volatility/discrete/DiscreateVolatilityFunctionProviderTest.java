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
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class DiscreateVolatilityFunctionProviderTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  @Test
  public void flatTest() {
    final double level = 0.4;

    final DiscreteVolatilityFunctionProvider pro = new DiscreteVolatilityFunctionProvider() {

      @Override
      public DiscreteVolatilityFunction from(final DoublesPair[] expiryStrikePoints) {
        final int n = expiryStrikePoints.length;
        final DoubleMatrix1D res = new DoubleMatrix1D(n, level);
        return new DiscreteVolatilityFunction() {

          @Override
          public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
            return res;
          }

          @Override
          public int getLengthOfRange() {
            return n;
          }

          @Override
          public int getLengthOfDomain() {
            return 0;
          }

          @Override
          public DoubleMatrix2D calculateJacobian(final DoubleMatrix1D x) {
            return DoubleMatrix2D.EMPTY_MATRIX;
          }
        };
      }
    };

    final int nSamples = 40;
    final List<DoublesPair> points = new ArrayList<>(nSamples);
    for (int i = 0; i < nSamples; i++) {
      final double t = RANDOM.nextDouble() * 20.0;
      final double k = RANDOM.nextDouble() * 0.15;
      points.add(DoublesPair.of(t, k));
    }

    final DiscreteVolatilityFunction func = pro.from(points);
    final DoubleMatrix1D nullVector = new DoubleMatrix1D(0);
    final DoubleMatrix1D y = func.evaluate(nullVector);
    assertEquals(nSamples, y.getNumberOfElements());
    for (int i = 0; i < nSamples; i++) {
      assertEquals(level, y.getEntry(i));
    }

    final DoubleMatrix2D jac = func.calculateJacobianViaFD(nullVector);
    assertEquals(0, jac.getNumberOfElements());
  }

}
