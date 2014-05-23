/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BasisFunctionGeneratorTest {
  private static final Logger s_logger = LoggerFactory.getLogger(BasisFunctionGeneratorTest.class);
  private static final Boolean PRINT = false;
  private static final NormalDistribution NORMAL = new NormalDistribution(0, 1.0, new MersenneTwister64(MersenneTwister.DEFAULT_SEED));
  private static final BasisFunctionGenerator GENERATOR = new BasisFunctionGenerator();
  private static final double[] KNOTS;

  static {
    final int n = 10;
    KNOTS = new double[n + 1];
    for (int i = 0; i < n + 1; i++) {
      KNOTS[i] = 0 + i * 1.0;
    }

  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullKnots() {
    GENERATOR.generate(null, 2, 4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegDegree() {
    GENERATOR.generate(KNOTS, -1, 4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFunctionIndexOutOfRange1() {
    GENERATOR.generate(KNOTS, 2, -1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFunctionIndexOutOfRange2() {
    GENERATOR.generate(KNOTS, 5, KNOTS.length - 5);
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

    final double[][] knots = new double[2][];
    knots[0] = KNOTS;
    knots[1] = KNOTS;
    final Function1D<double[], Double> func = GENERATOR.generate(knots, new int[] {2, 3}, new int[] {4, 4});
    final double[] x = new double[2];

    if (PRINT) {
      for (int i = 0; i < 101; i++) {
        x[0] = 0 + i * 10.0 / 100.0;
        s_logger.debug("\t" + x[0]);
      }
      System.out.print("\n");
      for (int i = 0; i < 101; i++) {
        x[0] = 0 + i * 10.0 / 100.0;
        System.out.print(x[0]);
        for (int j = 0; j < 101; j++) {
          x[1] = 0 + j * 10.0 / 100.0;
          final double y = func.evaluate(x);
          System.out.print("\t" + y);
        }
        System.out.print("\n");
      }
    }

  }

  @SuppressWarnings("unused")
  @Test
  public void testSet() {
    final java.util.List<Function1D<Double, Double>> functions = GENERATOR.generateSet(-3, 5, 17, 3);
    final int n = functions.size();
    final double[] w = new double[n];
    for (int i = 0; i < n; i++) {
      w[i] = 1 + 0.1 * NORMAL.nextRandom();
    }
    final Function1D<Double, Double> fun = new BasisFunctionAggregation<Double>(functions, w);

    for (int i = 0; i < 101; i++) {
      final double x = -3 + i * 8.0 / 100.0;
      final double y = fun.evaluate(x);
      // System.out.println(x + "\t" + y);
    }
  }

  @SuppressWarnings("unused")
  @Test
  public void testSet2() {

    final double[] iKnots = new double[] {0, 0.25, 0.75, 1, 2, 5, 7, 10, 15, 20, 30};
    // for (int i = 0; i < 11; i++) {
    // iKnots[i] = i / 10.0;
    // }
    final List<Function1D<Double, Double>> functions = GENERATOR.generateSet(iKnots, 3);
    final int n = functions.size();
    final double[] w = new double[n];
    for (int i = 0; i < n; i++) {
      w[i] = 1 + 0.1 * NORMAL.nextRandom();
    }
    final Function1D<Double, Double> fun = new BasisFunctionAggregation<Double>(functions, w);

    for (int i = 0; i < 100; i++) {
      final double x = -1 + i * 6 / 100.0;
      final double y = fun.evaluate(x);
      // System.out.println(x + "\t" + y);
    }
    for (int i = 0; i < 101; i++) {
      final double x = 6 + i * 54 / 100.0;
      final double y = fun.evaluate(x);
      // System.out.println(x + "\t" + y);
    }
  }

  @Test
  public void testSet3() {
    final double[] xa = new double[] {0.0, 0.0};
    final double[] xb = new double[] {1.0, 1.0};
    final int[] nknots = new int[] {10, 15};
    final int[] degree = new int[] {3, 4};
    final List<Function1D<double[], Double>> functions = GENERATOR.generateSet(xa, xb, nknots, degree);
    final int n = functions.size();
    final double[] w = new double[n];
    for (int i = 0; i < n; i++) {
      w[i] = 1 + 0.1 * NORMAL.nextRandom();
    }
    final Function1D<double[], Double> fun = new BasisFunctionAggregation<>(functions, w);

    final double[] x = new double[2];

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
          final double y = fun.evaluate(x);
          System.out.print("\t" + y);
        }
        System.out.print("\n");
      }
    }
  }

}
