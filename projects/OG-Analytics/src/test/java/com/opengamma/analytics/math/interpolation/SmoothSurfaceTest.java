/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.BrentMinimizer1D;
import com.opengamma.analytics.math.minimization.ConjugateGradientVectorMinimizer;
import com.opengamma.analytics.math.minimization.ScalarMinimizer;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SmoothSurfaceTest {
//FIXME this test does nothing - either delete or add some real tests
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1, RANDOM);
  private static final double EPS = 1e-5;
  private static final RadialBasisFunction SMOOTHER = new RadialBasisFunction();
  protected static final List<Pair<double[], Double>> FLAT_DATA = new ArrayList<>();
  protected static final List<Pair<double[], Double>> NOISY_DATA = new ArrayList<>();

  protected static final List<double[]> NODE_POS = new ArrayList<>();
  protected static final double VALUE = 0.3;

  static {

    double x, y;
    for (int i = 0; i < 20; i++) {
      x = 10 * RANDOM.nextDouble();
      y = 10 * RANDOM.nextDouble();
      NODE_POS.add(new double[] {x, y});
      FLAT_DATA.add(Pairs.of(new double[] {x, y}, VALUE));
      NOISY_DATA.add(Pairs.of(new double[] {x, y}, VALUE + 0.1 * NORMAL.nextRandom()));
    }
  }

  @Test(enabled = false)
  public void test() {
    final Function1D<Double, Double> basisFunction = new MultiquadraticRadialBasisFunction(0.5);
    fit(FLAT_DATA, basisFunction, true, NODE_POS);
  }

  @Test(enabled = false)
  public void testNoisy() {
    final Function1D<Double, Double> basisFunction = new MultiquadraticRadialBasisFunction(0.5);
    fit(NOISY_DATA, basisFunction, true, NODE_POS);
  }

  public void fit(final List<Pair<double[], Double>> data, final Function1D<Double, Double> basisFunction, final boolean isNormalized, final List<double[]> nodePos) {
    final Function1D<DoubleMatrix1D, Double> fom = new Function1D<DoubleMatrix1D, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final DoubleMatrix1D weights) {
        final List<Pair<double[], Double>> weightsAndPos = combineWeightsAndPos(nodePos, weights);
        final double chi2 = getChiSquared(data, basisFunction, weightsAndPos, isNormalized);
        final double plenty = getPlenty(data, basisFunction, weightsAndPos, isNormalized);
        return chi2 + 0.0 * plenty; // TODO This is not working with a plenty!!!!
      }
    };

    final ScalarMinimizer lineMinimizer = new BrentMinimizer1D();
    final ConjugateGradientVectorMinimizer minimizer = new ConjugateGradientVectorMinimizer(lineMinimizer, 1e-3, 1000);

        final double[] start = new double[nodePos.size()];
        for (int i = 0; i < start.length; i++) {
          start[i] = VALUE;
        }

    final DoubleMatrix1D res = minimizer.minimize(fom, new DoubleMatrix1D(start));
    final double chiSquare = fom.evaluate(res);
     System.out.println(res);
     System.out.println("chi2: " + chiSquare);

     final List<Pair<double[], Double>> weights = combineWeightsAndPos(nodePos, res);
     System.out.println("Pelenty: " + getPlenty(data, basisFunction, weights, isNormalized));

     printSurface(basisFunction, weights, isNormalized);
  }

  private static List<Pair<double[], Double>> combineWeightsAndPos(final List<double[]> nodePos, final DoubleMatrix1D weights) {
    final int n = nodePos.size();
    Validate.isTrue(n == weights.getNumberOfElements());
    final List<Pair<double[], Double>> weightsAndPos = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      weightsAndPos.add(Pairs.of(nodePos.get(i), weights.getEntry(i)));
    }
    return weightsAndPos;
  }

  private void printSurface(final Function1D<Double, Double> basisFunction, final List<Pair<double[], Double>> weights, final boolean isNormalized) {

    // printout
    final double[] x = new double[2];

    System.out.print("\t");
    for (int j = 0; j < 99; j++) {
      System.out.print(j / 10.0 + "\t");
    }
    System.out.print(99 / 10.0 + "\n");

    for (int i = 0; i < 100; i++) {
      System.out.print(i / 10.0 + "\t");
      x[0] = i / 10.0;
      for (int j = 0; j < 100; j++) {
        x[1] = j / 10.0;
        final double fit = SMOOTHER.evaluate(basisFunction, weights, x, isNormalized);

        System.out.print(fit + "\t");
      }
      System.out.print("\n");
    }
  }

  private double getChiSquared(final List<Pair<double[], Double>> data, final Function1D<Double, Double> basisFunction, final List<Pair<double[], Double>> weights, final boolean isNormalized) {
    final int n = data.size();
    double sum = 0;
    double[] x;
    for (int i = 0; i < n; i++) {
      final Pair<double[], Double> dPair = data.get(i);
      x = dPair.getFirst();
      final double diff = (dPair.getSecond() - SMOOTHER.evaluate(basisFunction, weights, x, isNormalized)) / 0.1;
      sum += diff * diff;
    }
    return sum;
  }

  private double getPlenty(final List<Pair<double[], Double>> data, final Function1D<Double, Double> basisFunction, final List<Pair<double[], Double>> weights, final boolean isNormalized) {

    double sum = 0;
    for (final Pair<double[], Double> pair : data) {
      final double[] x = pair.getFirst();
      final double[][] hessian = getHessian(basisFunction, weights, x, isNormalized);
      final double temp = getMatrixMaxValue(hessian);
      sum += temp * temp;
    }
    return sum;
  }

  double getMatrixMaxValue(final double[][] matrix) {
    double res = 0;
    final int n = matrix.length;
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
        res += matrix[i][j] * matrix[i][j];
      }
    }
    return Math.sqrt(res);
  }

  private double[][] getHessian(final Function1D<Double, Double> basisFunction, final List<Pair<double[], Double>> weights, final double[] x, final boolean isNormalized) {

    final int n = x.length;

    final double[][] res = new double[n][n];
    for (int i = 0; i < n; i++) {
      for (int j = i; j < n; j++) {
        res[i][j] = getSecondDev(basisFunction, weights, x, isNormalized, i, j);
      }
      for (int j = i + 1; j < n; j++) {
        res[j][i] = res[i][j];
      }
    }
    return res;
  }

  private double getSecondDev(final Function1D<Double, Double> basisFunction, final List<Pair<double[], Double>> weights, final double[] x, final boolean isNormalized, final int i, final int j) {
    if (i == j) {
      final double f_x = SMOOTHER.evaluate(basisFunction, weights, x, isNormalized);
      final double[] temp = Arrays.copyOf(x, x.length);
      temp[i] += EPS;
      final double f_p = SMOOTHER.evaluate(basisFunction, weights, temp, isNormalized);
      temp[i] -= 2 * EPS;
      final double f_m = SMOOTHER.evaluate(basisFunction, weights, temp, isNormalized);

      return (f_p + f_m - 2 * f_x) / EPS / EPS;
    }

    return 0;
  }

}
