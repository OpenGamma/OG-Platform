/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.BasisFunctionAggregation;
import com.opengamma.math.interpolation.BasisFunctionGenerator;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * 
 */
public class GeneralizedLeastSquareTest {
  private static final NormalDistribution NORMAL = new NormalDistribution(0, 1.0, new MersenneTwister64(MersenneTwister64.DEFAULT_SEED));
  private static final double[] WEIGHTS = new double[] {1.0, -0.5, 2.0, 0.23, 1.45};
  private static final DoubleMatrix1D X;
  private static final DoubleMatrix1D Y;
  private static final DoubleMatrix1D SIGMA;
  private static final List<Function1D<Double, Double>> SIN_FUNCTIONS;
  private static final List<Function1D<Double, Double>> BASIS_FUNCTIONS;

  static {
    SIN_FUNCTIONS = new ArrayList<Function1D<Double, Double>>();
    for (int i = 0; i < WEIGHTS.length; i++) {
      final int k = i;
      Function1D<Double, Double> func = new Function1D<Double, Double>() {

        @Override
        public Double evaluate(Double x) {
          return Math.sin((2 * k + 1) * x);
        }
      };
      SIN_FUNCTIONS.add(func);
    }

    int n = 10;
    double[] x = new double[n];
    double[] y = new double[n];
    double[] sigma = new double[n];
    for (int i = 0; i < n; i++) {
      double sum = 0;
      x[i] = i / 5.0;
      for (int j = 0; j < WEIGHTS.length; j++) {
        sum += WEIGHTS[j] * SIN_FUNCTIONS.get(j).evaluate(x[i]);
      }
      y[i] = sum;
      sigma[i] = 0.01;
    }
    X = new DoubleMatrix1D(x);
    Y = new DoubleMatrix1D(y);
    SIGMA = new DoubleMatrix1D(sigma);

    BasisFunctionGenerator generator = new BasisFunctionGenerator();
    BASIS_FUNCTIONS = generator.generateSet(0.0, 5.0, 4, 3);
  }

  @Test
  public void testPerfectFit() {
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();
    LeastSquareResults results = gls.solve(X, Y, SIGMA, SIN_FUNCTIONS);
    assertEquals(0.0, results.getChiSq(), 1e-8);
    DoubleMatrix1D w = results.getParameters();
    for (int i = 0; i < WEIGHTS.length; i++) {
      assertEquals(WEIGHTS[i], w.getEntry(i), 1e-8);
    }
  }

  @Test
  public void testFit() {
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();
    double[] y = new double[Y.getNumberOfElements()];
    for (int i = 0; i < Y.getNumberOfElements(); i++) {
      y[i] = Y.getEntry(i) + SIGMA.getEntry(i) * NORMAL.nextRandom();
    }

    LeastSquareResults results = gls.solve(X, new DoubleMatrix1D(y), SIGMA, SIN_FUNCTIONS);
    assertTrue(results.getChiSq() < 3 * Y.getNumberOfElements());

  }

  @Test
  public void testBSplineFit() {
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();

    LeastSquareResults results = gls.solve(X, Y, SIGMA, BASIS_FUNCTIONS);
    Function1D<Double, Double> spline = new BasisFunctionAggregation(BASIS_FUNCTIONS, results.getParameters().getData());

    for (int i = 0; i < 101; i++) {
      double x = 0 + i * 5.0 / 100.0;
      System.out.println(x + "\t" + spline.evaluate(x));
    }
    // for(int i = X.getNumberOfElements())
    // assertTrue(results.getChiSq() < 3 * Y.getNumberOfElements());

  }

}
