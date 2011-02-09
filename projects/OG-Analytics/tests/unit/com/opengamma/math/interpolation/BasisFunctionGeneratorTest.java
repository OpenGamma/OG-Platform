/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * 
 */
public class BasisFunctionGeneratorTest {

  private static final Boolean PRINT = false;
  private static final NormalDistribution NORMAL = new NormalDistribution(0, 1.0, new MersenneTwister64(MersenneTwister64.DEFAULT_SEED));
  private static final BasisFunctionGenerator GENERATOR = new BasisFunctionGenerator();
  private static final Function1D<Double, Double> BASIS_FUNCTION;
  private static final double[] KNOTS;

  static {
    int n = 10;
    KNOTS = new double[n + 1];
    for (int i = 0; i < n + 1; i++) {
      KNOTS[i] = 0 + i * 1.0;
    }

    BASIS_FUNCTION = GENERATOR.generate(KNOTS, 3, 3);
  }

  // @Test
  // public void test() {
  // double x, y;
  // for (int i = 0; i < 101; i++) {
  // x = 0. + 10. * i / 100.0;
  // y = BASIS_FUNCTION.evaluate(x);
  // System.out.println(x + "\t" + y);
  // }
  // }

  @Test(expected = IllegalArgumentException.class)
  public void testNullKnots() {
    @SuppressWarnings("unused")
    final Function1D<Double, Double> func = GENERATOR.generate(null, 2, 4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegDegree() {
    @SuppressWarnings("unused")
    final Function1D<Double, Double> func = GENERATOR.generate(KNOTS, -1, 4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFunctionIndexOutOfRange1() {
    @SuppressWarnings("unused")
    final Function1D<Double, Double> func = GENERATOR.generate(KNOTS, 2, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFunctionIndexOutOfRange2() {
    @SuppressWarnings("unused")
    final Function1D<Double, Double> func = GENERATOR.generate(KNOTS, 5, KNOTS.length - 5);
  }

  @Test
  public void testZeroOrder() {
    final Function1D<Double, Double> func = GENERATOR.generate(KNOTS, 0, 4);
    assertEquals(0.0, func.evaluate(3.5), 0.0);
    assertEquals(1.0, func.evaluate(4.78), 0.0);
    assertEquals(1.0, func.evaluate(4.0), 0.0);
    assertEquals(0.0, func.evaluate(5.0), 0.0);
  }

  @Test
  public void testFirstOrder() {
    final Function1D<Double, Double> func = GENERATOR.generate(KNOTS, 1, 3);
    assertEquals(0.0, func.evaluate(1.76), 0.0);
    assertEquals(1.0, func.evaluate(4.0), 0.0);
    assertEquals(0, func.evaluate(5.0), 0.0);
    assertEquals(0.5, func.evaluate(3.5), 0.0);
  }

  @Test
  public void testSecondOrder() {
    final Function1D<Double, Double> func = GENERATOR.generate(KNOTS, 2, 3);
    assertEquals(0.0, func.evaluate(1.76), 0.0);
    assertEquals(0.125, func.evaluate(3.5), 0.0);
    assertEquals(0.5, func.evaluate(4.0), 0.0);
    assertEquals(0.75, func.evaluate(4.5), 0.0);
    assertEquals(0.0, func.evaluate(6.0), 0.0);
  }

  @Test
  public void testThirdOrder() {
    final Function1D<Double, Double> func = GENERATOR.generate(KNOTS, 3, 3);
    assertEquals(0.0, func.evaluate(1.76), 0.0);
    assertEquals(1. / 6., func.evaluate(4.0), 0.0);
    assertEquals(2. / 3., func.evaluate(5.0), 0.0);
    assertEquals(1 / 48., func.evaluate(6.5), 0.0);
    assertEquals(0.0, func.evaluate(7.0), 0.0);
  }

  @Test
  public void test2D() {

    double[][] knots = new double[2][];
    knots[0] = KNOTS;
    knots[1] = KNOTS;
    Function1D<double[], Double> func = GENERATOR.generate(knots, new int[] {2, 3}, new int[] {4, 4});
    double[] x = new double[2];

    if (PRINT) {
      for (int i = 0; i < 101; i++) {
        x[0] = 0 + i * 10.0 / 100.0;
        System.out.print("\t" + x[0]);
      }
      System.out.print("\n");
      for (int i = 0; i < 101; i++) {
        x[0] = 0 + i * 10.0 / 100.0;
        System.out.print(x[0]);
        for (int j = 0; j < 101; j++) {
          x[1] = 0 + j * 10.0 / 100.0;
          double y = func.evaluate(x);
          System.out.print("\t" + y);
        }
        System.out.print("\n");
      }
    }

  }

  @Test
  public void testSet() {
    java.util.List<Function1D<Double, Double>> functions = GENERATOR.generateSet(-3, 5, 17, 3);
    int n = functions.size();
    double[] w = new double[n];
    for (int i = 0; i < n; i++) {
      w[i] = 1 + 0.1 * NORMAL.nextRandom();
    }
    Function1D<Double, Double> fun = new BasisFunctionAggregation(functions, w);

    for (int i = 0; i < 101; i++) {
      double x = -3 + i * 8.0 / 100.0;
      double y = fun.evaluate(x);
      // System.out.println(x + "\t" + y);
    }
  }

  @Test
  public void testSet2() {

    double[] iKnots = new double[] {0, 0.25, 0.75, 1, 2, 5, 7, 10, 15, 20, 30};
    // for (int i = 0; i < 11; i++) {
    // iKnots[i] = i / 10.0;
    // }
    List<Function1D<Double, Double>> functions = GENERATOR.generateSet(iKnots, 3);
    int n = functions.size();
    double[] w = new double[n];
    for (int i = 0; i < n; i++) {
      w[i] = 1 + 0.1 * NORMAL.nextRandom();
    }
    Function1D<Double, Double> fun = new BasisFunctionAggregation(functions, w);

    for (int i = 0; i < 100; i++) {
      double x = -1 + i * 6 / 100.0;
      double y = fun.evaluate(x);
      // System.out.println(x + "\t" + y);
    }
    for (int i = 0; i < 101; i++) {
      double x = 6 + i * 54 / 100.0;
      double y = fun.evaluate(x);
      // System.out.println(x + "\t" + y);
    }
  }

  @Test
  public void testSet3() {
    double[] xa = new double[] {0.0, 0.0};
    double[] xb = new double[] {1.0, 1.0};
    int[] nknots = new int[] {10, 15};
    int[] degree = new int[] {3, 4};
    List<Function1D<double[], Double>> functions = GENERATOR.generateSet(xa, xb, nknots, degree);
    int n = functions.size();
    double[] w = new double[n];
    for (int i = 0; i < n; i++) {
      w[i] = 1 + 0.1 * NORMAL.nextRandom();
    }
    Function1D<double[], Double> fun = new BasisFunctionAggregation<double[]>(functions, w);

    double[] x = new double[2];

    if (PRINT) {

      for (int i = 0; i < 101; i++) {
        x[0] = -0.4 + i * 1.8 / 100.0;
        System.out.print("\t" + x[0]);
      }
      System.out.print("\n");
      for (int i = 0; i < 101; i++) {
        x[0] = -0.4 + i * 1.8 / 100.0;
        System.out.print(x[0]);
        for (int j = 0; j < 101; j++) {
          x[1] = -0.4 + j * 1.8 / 100.0;
          double y = fun.evaluate(x);
          System.out.print("\t" + y);
        }
        System.out.print("\n");
      }
    }
  }

}
