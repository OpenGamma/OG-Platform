/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.InterpolatedVectorFunctionProvider;
import com.opengamma.analytics.math.function.VectorFunction;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.AssertMatrix;

/**
 * 
 */
public class ProductInterpolator1DTest {
  private static final VectorFieldFirstOrderDifferentiator VECTOR_DIFF = new VectorFieldFirstOrderDifferentiator();
  private static final ScalarFirstOrderDifferentiator SCALAR_DIFF = new ScalarFirstOrderDifferentiator();
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final Interpolator1D NATURAL_CUBIC_SPLINE = Interpolator1DFactory.NATURAL_CUBIC_SPLINE_INSTANCE;
  private static final Interpolator1D PRODUCT_INTERPOLATOR = new ProductInterpolator1D(NATURAL_CUBIC_SPLINE);

  private static final double[] X = new double[] {0.3, 1.2, 2.3, 5.6, 7.8, 10 };
  private static final double[] Y = new double[] {0.6, 0.5, 0.3, 0.4, 0.8, 1.0 };

  @Test
  public void testValues() {
    int n = X.length;
    double[] xy = new double[n];
    for (int i = 0; i < n; i++) {
      xy[i] = X[i] * Y[i];
    }
    double[] xMixed = X.clone();
    double[] yMixed = Y.clone();
    for (int i = 0; i < n; i++) {
      int index = (int) (RANDOM.nextDouble() * n);
      swap(xMixed, i, index);
      swap(yMixed, i, index);
    }

    Interpolator1DDataBundle db1 = NATURAL_CUBIC_SPLINE.getDataBundleFromSortedArrays(X, xy);
    Interpolator1DDataBundle db2 = PRODUCT_INTERPOLATOR.getDataBundleFromSortedArrays(X, Y);
    Interpolator1DDataBundle db3 = PRODUCT_INTERPOLATOR.getDataBundle(xMixed, yMixed);
    assertEquals(db2, db3);
    double start = X[0];
    double range = X[n - 1] - X[0];
    int samples = 100;
    for (int i = 0; i < samples; i++) {
      double x = start + range * RANDOM.nextDouble();
      double expected = NATURAL_CUBIC_SPLINE.interpolate(db1, x) / x;
      double actual = PRODUCT_INTERPOLATOR.interpolate(db2, x);
      assertEquals(expected, actual, 1e-15);
    }
  }

  @Test
  void testNodeSensitivity() {
    int n = X.length;

    double start = X[0];
    double range = X[n - 1] - X[0];
    int samples = 100;
    double[] x = new double[samples];
    for (int i = 0; i < samples; i++) {
      x[i] = start + range * RANDOM.nextDouble();
    }
    InterpolatedVectorFunctionProvider pro = new InterpolatedVectorFunctionProvider(PRODUCT_INTERPOLATOR, X);
    VectorFunction func = pro.from(x);
    Function1D<DoubleMatrix1D, DoubleMatrix2D> fdSenseFunc = VECTOR_DIFF.differentiate(func);
    DoubleMatrix2D fdSense = fdSenseFunc.evaluate(new DoubleMatrix1D(Y));

    Interpolator1DDataBundle db = PRODUCT_INTERPOLATOR.getDataBundleFromSortedArrays(X, Y);
    for (int i = 0; i < samples; i++) {
      double[] sense = PRODUCT_INTERPOLATOR.getNodeSensitivitiesForValue(db, x[i]);
      AssertMatrix.assertEqualsVectors(fdSense.getRowVector(i), new DoubleMatrix1D(sense), 1e-10);
    }
  }

  @Test
  void testGradiant() {
    final Interpolator1D prodInterpolator = new ProductInterpolator1D(NATURAL_CUBIC_SPLINE, true);
    final Interpolator1DDataBundle db = prodInterpolator.getDataBundleFromSortedArrays(X, Y);
    Function1D<Double, Double> func = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        return prodInterpolator.interpolate(db, x);
      }
    };
    Function1D<Double, Boolean> domain = new Function1D<Double, Boolean>() {
      @Override
      public Boolean evaluate(Double x) {
        return x >= 0.0;
      }
    };
    Function1D<Double, Double> fdGradFunc = SCALAR_DIFF.differentiate(func, domain);
    int n = X.length;

    double start = X[0];
    double range = X[n - 1] - X[0];
    int samples = 100;
    for (int i = 0; i < samples; i++) {
      double x = start + range * RANDOM.nextDouble();
      double expected = fdGradFunc.evaluate(x);
      double actual = prodInterpolator.firstDerivative(db, x);
      assertEquals(expected, actual, 1e-10);
    }

    //now test at zero
    double expected = fdGradFunc.evaluate(0.0);
    double actual = prodInterpolator.firstDerivative(db,0.0);
    assertEquals(expected, actual, 1e-10);
  }

  @Test
  void testZeroValue() {
    Interpolator1D linear = Interpolator1DFactory.LINEAR_INSTANCE;
    Interpolator1D prodInterpolator = new ProductInterpolator1D(linear, true);
    Interpolator1DDataBundle db = prodInterpolator.getDataBundleFromSortedArrays(X, Y);
    double y0 = prodInterpolator.interpolate(db, 0.0);
    assertEquals(Y[0], y0, 1e-15);
    double[] sense = prodInterpolator.getNodeSensitivitiesForValue(db, 0.0);
    DoubleMatrix1D expected = new DoubleMatrix1D(0, 1, 0, 0, 0, 0, 0);
    AssertMatrix.assertEqualsVectors(expected, new DoubleMatrix1D(sense), 1e-3);
  }

  private void swap(double[] array, int i1, int i2) {
    double temp = array[i1];
    array[i1] = array[i2];
    array[i2] = temp;
  }

}
