/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.minimization.BrentMinimizer1D;
import com.opengamma.math.minimization.ConjugateGradientVectorMinimizer;
import com.opengamma.math.minimization.ScalarMinimizer;
import com.opengamma.math.minimization.VectorMinimizer;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SmoothSurfaceTest {

  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1, RANDOM);
  private static final double EPS = 1e-5;
  private static final RadialBasisFunctionInterpolatorND INTERPOLATOR = new RadialBasisFunctionInterpolatorND();
  private static final RadialBasisFunction SMOOTHER = new RadialBasisFunction();
  protected static final List<Pair<double[], Double>> FLAT_DATA = new ArrayList<Pair<double[], Double>>();
  protected static final List<Pair<double[], Double>> NOISY_DATA = new ArrayList<Pair<double[], Double>>();

  protected static final List<double[]> NODE_POS = new ArrayList<double[]>();
  protected static final double VALUE = 0.3;

  static {

    double x, y;
    for (int i = 0; i < 20; i++) {
      x = 10 * RANDOM.nextDouble();
      y = 10 * RANDOM.nextDouble();
      NODE_POS.add(new double[] {x, y});
      FLAT_DATA.add(new ObjectsPair<double[], Double>(new double[] {x, y}, VALUE));
      NOISY_DATA.add(new ObjectsPair<double[], Double>(new double[] {x, y}, VALUE + 0.1 * NORMAL.nextRandom()));
    }
  }

  @Test
  public void test() {
    Function1D<Double, Double> basisFunction = new MultiquadraticRadialBasisFunction(0.5);
    fit(FLAT_DATA, basisFunction, true, NODE_POS, 0);
  }

  @Test
  public void testNosiy() {
    Function1D<Double, Double> basisFunction = new MultiquadraticRadialBasisFunction(0.5);
    fit(NOISY_DATA, basisFunction, true, NODE_POS, 0);
  }

  public void fit(final List<Pair<double[], Double>> data, final Function1D<Double, Double> basisFunction, final boolean isNormalized, final List<double[]> nodePos, double lambda) {
    Function1D<DoubleMatrix1D, Double> fom = new Function1D<DoubleMatrix1D, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(DoubleMatrix1D weights) {
        List<Pair<double[], Double>> weightsAndPos = combineWeightsAndPos(nodePos, weights);
        double chi2 = getChiSquared(data, basisFunction, weightsAndPos, isNormalized);
        double pelenty = getPelenty(data, basisFunction, weightsAndPos, isNormalized);
        return chi2 + 0.0 * pelenty; // TODO This is not working with a plenty!!!!
      }
    };

    ScalarMinimizer lineMinimizer = new BrentMinimizer1D();
    // VectorMinimizer minimzer = new ConjugateDirectionVectorMinimizer(lineMinimizer, 1e-5, 10000);
    VectorMinimizer minimzer = new ConjugateGradientVectorMinimizer(lineMinimizer, 1e-3, 1000);

    double[] start = new double[nodePos.size()];
    for (int i = 0; i < start.length; i++) {
      start[i] = VALUE;
    }

    DoubleMatrix1D res = minimzer.minimize(fom, new DoubleMatrix1D(start));

    double chiSquare = fom.evaluate(res);

    System.out.println(res);
    System.out.println("chi2: " + chiSquare);

    List<Pair<double[], Double>> weights = combineWeightsAndPos(nodePos, res);
    System.out.println("Pelenty: " + getPelenty(data, basisFunction, weights, isNormalized));

    printSurface(basisFunction, weights, isNormalized);
  }

  private static List<Pair<double[], Double>> combineWeightsAndPos(final List<double[]> nodePos, DoubleMatrix1D weights) {
    int n = nodePos.size();
    Validate.isTrue(n == weights.getNumberOfElements());
    List<Pair<double[], Double>> weightsAndPos = new ArrayList<Pair<double[], Double>>(n);
    for (int i = 0; i < n; i++) {
      weightsAndPos.add(new ObjectsPair<double[], Double>(nodePos.get(i), weights.getEntry(i)));
    }
    return weightsAndPos;
  }

  private void printSurface(Function1D<Double, Double> basisFunction, List<Pair<double[], Double>> weights, boolean isNormalized) {

    // printout
    double[] x = new double[2];

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
        double fit = SMOOTHER.evaluate(basisFunction, weights, x, isNormalized);

        System.out.print(fit + "\t");
      }
      System.out.print("\n");
    }
  }

  private double getChiSquared(List<Pair<double[], Double>> data, Function1D<Double, Double> basisFunction, List<Pair<double[], Double>> weights, final boolean isNormalized) {
    int n = data.size();
    double sum = 0;
    double[] x;
    for (int i = 0; i < n; i++) {
      Pair<double[], Double> dPair = data.get(i);
      x = dPair.getFirst();
      double diff = (dPair.getSecond() - SMOOTHER.evaluate(basisFunction, weights, x, isNormalized)) / 0.1;
      sum += diff * diff;
    }
    return sum;
  }

  private double getPelenty(List<Pair<double[], Double>> data, Function1D<Double, Double> basisFunction, List<Pair<double[], Double>> weights, boolean isNormalized) {

    double sum = 0;
    for (Pair<double[], Double> pair : data) {
      double[] x = pair.getFirst();
      double[][] hessian = getHessian(basisFunction, weights, x, isNormalized);
      double temp = getMatrixMaxValue(hessian);
      sum += temp * temp;
    }
    return sum;
  }

  double getMatrixMaxValue(double[][] matrix) {
    double res = 0;
    int n = matrix.length;
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
        res += matrix[i][j] * matrix[i][j];
      }
    }
    // DoubleMatrix2D temp = new DoubleMatrix2D(matrix);
    // CommonsMatrixAlgebra algebra = new CommonsMatrixAlgebra();
    // double res = algebra.getDeterminant(temp);

    return Math.sqrt(res);
  }

  private double[][] getHessian(Function1D<Double, Double> basisFunction, List<Pair<double[], Double>> weights, double[] x, boolean isNormalized) {

    int n = x.length;

    double[][] res = new double[n][n];
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

  private double getSecondDev(Function1D<Double, Double> basisFunction, List<Pair<double[], Double>> weights, double[] x, boolean isNormalized, int i, int j) {
    if (i == j) {
      double f_x = SMOOTHER.evaluate(basisFunction, weights, x, isNormalized);
      double[] temp = Arrays.copyOf(x, x.length);
      temp[i] += EPS;
      double f_p = SMOOTHER.evaluate(basisFunction, weights, temp, isNormalized);
      temp[i] -= 2 * EPS;
      double f_m = SMOOTHER.evaluate(basisFunction, weights, temp, isNormalized);

      return (f_p + f_m - 2 * f_x) / EPS / EPS;
    }

    return 0;

    // double[] temp = Arrays.copyOf(x, x.length);
    // temp[i] += EPS;
    // temp[j] += EPS;
    // double f_pp = SMOOTHER.evaluate(basisFunction, weights, temp, isNormalized);
    // temp[j] -= 2 * EPS;
    // double f_pm = SMOOTHER.evaluate(basisFunction, weights, temp, isNormalized);
    // temp[i] -= 2 * EPS;
    // double f_mm = SMOOTHER.evaluate(basisFunction, weights, temp, isNormalized);
    // temp[j] += 2 * EPS;
    // double f_mp = SMOOTHER.evaluate(basisFunction, weights, temp, isNormalized);
    //
    // return (f_pp + f_mm - f_pm - f_mp) / 4 / EPS / EPS;
  }

}
