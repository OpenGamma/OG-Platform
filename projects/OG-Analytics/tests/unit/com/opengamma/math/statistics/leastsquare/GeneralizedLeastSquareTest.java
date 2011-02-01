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

import org.apache.commons.lang.Validate;
import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.BasisFunctionAggregation;
import com.opengamma.math.interpolation.BasisFunctionGenerator;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * 
 */
public class GeneralizedLeastSquareTest {
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final NormalDistribution NORMAL = new NormalDistribution(0, 1.0, RANDOM);
  private static final double[] WEIGHTS = new double[] {1.0, -0.5, 2.0, 0.23, 1.45};
  private static final Double[] X;
  private static final double[] Y;
  private static final double[] SIGMA;
  private static final List<DoubleMatrix1D> X_VECT;
  private static final List<Double> Y_VECT;
  private static final List<Double> SIGMA_VECT;
  private static final List<Function1D<Double, Double>> SIN_FUNCTIONS;
  private static final Function1D<Double, Double> TEST_FUNCTION;
  private static final List<Function1D<Double, Double>> BASIS_FUNCTIONS;

  private static final List<Function1D<DoubleMatrix1D, Double>> VECTOR_TRIG_FUNCTIONS;
  private static final Function1D<DoubleMatrix1D, Double> VECTOR_TEST_FUNCTION;

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
    TEST_FUNCTION = new BasisFunctionAggregation<Double>(SIN_FUNCTIONS, WEIGHTS);

    VECTOR_TRIG_FUNCTIONS = new ArrayList<Function1D<DoubleMatrix1D, Double>>();
    for (int i = 0; i < WEIGHTS.length; i++) {
      final int k = i;
      Function1D<DoubleMatrix1D, Double> func = new Function1D<DoubleMatrix1D, Double>() {
        @Override
        public Double evaluate(DoubleMatrix1D x) {
          Validate.isTrue(x.getNumberOfElements() == 2);
          return Math.sin((2 * k + 1) * x.getEntry(0)) * Math.cos((2 * k + 1) * x.getEntry(1));
        }
      };
      VECTOR_TRIG_FUNCTIONS.add(func);
    }
    VECTOR_TEST_FUNCTION = new BasisFunctionAggregation<DoubleMatrix1D>(VECTOR_TRIG_FUNCTIONS, WEIGHTS);

    X_VECT = new ArrayList<DoubleMatrix1D>();
    Y_VECT = new ArrayList<Double>();
    SIGMA_VECT = new ArrayList<Double>();
    int n = 10;
    X = new Double[n];
    Y = new double[n];
    SIGMA = new double[n];
    for (int i = 0; i < n; i++) {
      X[i] = i / 5.0;
      double[] temp = new double[2];
      temp[0] = 2.0 * RANDOM.nextDouble();
      temp[1] = 2.0 * RANDOM.nextDouble();
      X_VECT.add(new DoubleMatrix1D(temp));
      Y[i] = TEST_FUNCTION.evaluate(X[i]);
      Y_VECT.add(VECTOR_TEST_FUNCTION.evaluate(X_VECT.get(i)));
      SIGMA[i] = 0.01;
      SIGMA_VECT.add(0.01);
    }

    BasisFunctionGenerator generator = new BasisFunctionGenerator();
    BASIS_FUNCTIONS = generator.generateSet(0.0, 2.0, 20, 3);

    for (int i = 0; i < 101; i++) {
      double xx = 0 + 2.0 * i / 100.0;
      System.out.println(xx + "\t" + TEST_FUNCTION.evaluate(xx));
    }

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
  public void testPerfectFitVector() {
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();
    LeastSquareResults results = gls.solve(X_VECT, Y_VECT, SIGMA_VECT, VECTOR_TRIG_FUNCTIONS);
    assertEquals(0.0, results.getChiSq(), 1e-8);
    DoubleMatrix1D w = results.getParameters();
    for (int i = 0; i < WEIGHTS.length; i++) {
      assertEquals(WEIGHTS[i], w.getEntry(i), 1e-8);
    }
  }

  @Test
  public void testFit() {
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();
    double[] y = new double[Y.length];
    for (int i = 0; i < Y.length; i++) {
      y[i] = Y[i] + SIGMA[i] * NORMAL.nextRandom();
    }

    LeastSquareResults results = gls.solve(X, y, SIGMA, SIN_FUNCTIONS);
    assertTrue(results.getChiSq() < 3 * Y.length);

  }

  @Test
  public void testBSplineFit() {
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();

    LeastSquareResults results = gls.solve(X, Y, SIGMA, BASIS_FUNCTIONS);
    Function1D<Double, Double> spline = new BasisFunctionAggregation(BASIS_FUNCTIONS, results.getParameters().getData());

    System.out.println("Chi^2:\t" + results.getChiSq());
    System.out.println("weights:\t" + results.getParameters());

    for (int i = 0; i < 101; i++) {
      double x = 0 + i * 2.0 / 100.0;
      System.out.println(x + "\t" + spline.evaluate(x));
    }
    for (int i = 0; i < X.length; i++) {
      System.out.println(X[i] + "\t" + Y[i]);
    }
  }

  @Test
  public void testPSplineFit() {
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();

    LeastSquareResults results = gls.solve(X, Y, SIGMA, BASIS_FUNCTIONS, 1000.0, 2);
    Function1D<Double, Double> spline = new BasisFunctionAggregation(BASIS_FUNCTIONS, results.getParameters().getData());

    System.out.println("Chi^2:\t" + results.getChiSq());
    System.out.println("weights:\t" + results.getParameters());

    for (int i = 0; i < 101; i++) {
      double x = 0 + i * 2.0 / 100.0;
      System.out.println(x + "\t" + spline.evaluate(x));
    }
    for (int i = 0; i < X.length; i++) {
      System.out.println(X[i] + "\t" + Y[i]);
    }
  }
}
