/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.BitSet;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.volatility.smile.function.SmileModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public abstract class SmileModelFitterTest<T extends SmileModelData> {
  private static final double TIME_TO_EXPIRY = 7.0;
  private static final double F = 0.03;
  private static RandomEngine UNIFORM = new MersenneTwister();
  protected double[] _cleanVols;
  protected double[] _noisyVols;
  protected double[] _errors;
  protected VolatilityFunctionProvider<T> _model;
  protected SmileModelFitter<T> _fitter;
  protected SmileModelFitter<T> _nosiyFitter;

  protected double _chiSqEps = 1e-6;
  protected double _paramValueEps = 1e-6;

  abstract Logger getlogger();

  abstract VolatilityFunctionProvider<T> getModel();

  abstract T getModelData();

  abstract SmileModelFitter<T> getFitter(final double forward, final double[] strikes, final double timeToExpiry, final double[] impliedVols, double[] error, VolatilityFunctionProvider<T> model);

  abstract double[][] getStartValues();

  abstract double[] getRandomStartValues();

  abstract BitSet[] getFixedValues();

  public SmileModelFitterTest() {
    final VolatilityFunctionProvider<T> model = getModel();
    final T data = getModelData();
    final double[] strikes = new double[] {0.005, 0.01, 0.02, 0.03, 0.04, 0.05, 0.07, 0.1 };
    final int n = strikes.length;
    _noisyVols = new double[n];

    _errors = new double[n];
    _cleanVols = model.getVolatilityFunction(F, strikes, TIME_TO_EXPIRY).evaluate(data);
    Arrays.fill(_errors, 1e-4);
    for (int i = 0; i < n; i++) {
      _noisyVols[i] = _cleanVols[i] + UNIFORM.nextDouble() * _errors[i];
    }

    _fitter = getFitter(F, strikes, TIME_TO_EXPIRY, _cleanVols, _errors, model);
    _nosiyFitter = getFitter(F, strikes, TIME_TO_EXPIRY, _noisyVols, _errors, model);
  }

  public void testExactFit() {

    final double[][] start = getStartValues();
    final BitSet[] fixed = getFixedValues();
    final int nStartPoints = start.length;
    Validate.isTrue(fixed.length == nStartPoints);
    for (int trys = 0; trys < nStartPoints; trys++) {
      final LeastSquareResultsWithTransform results = _fitter.solve(new DoubleMatrix1D(start[trys]), fixed[trys]);
      final DoubleMatrix1D res = toStandardForm(results.getModelParameters());

      //debug
      final T fittedModel = _fitter.toSmileModelData(res);
      fittedModel.toString();

      assertEquals(0.0, results.getChiSq(), _chiSqEps);

      final int n = res.getNumberOfElements();
      final T data = getModelData();
      assertEquals(data.getNumberOfParameters(), n);
      for (int i = 0; i < n; i++) {
        assertEquals(data.getParameter(i), res.getEntry(i), _paramValueEps);
      }
    }
  }

  /**
   * Convert the fitted parameters to standard form - useful if there is degeneracy in the solution
   * @param from
   * @return The matrix in standard form
   */
  protected DoubleMatrix1D toStandardForm(final DoubleMatrix1D from) {
    return from;
  }

  public void testNoisyFit() {
    final double[][] start = getStartValues();
    final BitSet[] fixed = getFixedValues();
    final int nStartPoints = start.length;
    Validate.isTrue(fixed.length == nStartPoints);
    for (int trys = 0; trys < nStartPoints; trys++) {
      final LeastSquareResultsWithTransform results = _fitter.solve(new DoubleMatrix1D(start[trys]), fixed[trys]);
      final DoubleMatrix1D res = toStandardForm(results.getModelParameters());
      final double eps = 1e-2;
      assertTrue(results.getChiSq() < 7);
      final int n = res.getNumberOfElements();
      final T data = getModelData();
      assertEquals(data.getNumberOfParameters(), n);
      for (int i = 0; i < n; i++) {
        assertEquals(data.getParameter(i), res.getEntry(i), eps);
      }
    }
  }

  public void timeTest() {
    final int hotspotWarmupCycles = 200;
    final int benchmarkCycles = 1000;
    final int nStarts = getStartValues().length;
    for (int i = 0; i < hotspotWarmupCycles; i++) {
      testNoisyFit();
    }
    if (benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(getlogger(), "processing {} cycles fitting smile", nStarts * benchmarkCycles);
      for (int i = 0; i < benchmarkCycles; i++) {
        testNoisyFit();
      }
      final long time = timer.finished();
      getlogger().info("time per fit: " + ((double) time) / benchmarkCycles / nStarts + "ms");

    }
  }

  public void horribleMarketDataTest() {
    final double forward = 0.0059875;
    final double[] strikes = new double[] {0.0012499999999999734, 0.0024999999999999467, 0.003750000000000031, 0.0050000000000000044, 0.006249999999999978, 0.007499999999999951, 0.008750000000000036,
        0.010000000000000009, 0.011249999999999982, 0.012499999999999956, 0.01375000000000004, 0.015000000000000013, 0.016249999999999987, 0.01749999999999996, 0.018750000000000044,
        0.020000000000000018, 0.02124999999999999, 0.022499999999999964, 0.02375000000000005, 0.025000000000000022, 0.026249999999999996, 0.02749999999999997, 0.028750000000000053,
        0.030000000000000027 };
    final double expiry = 0.09041095890410959;
    final double[] vols = new double[] {2.7100433855959642, 1.5506135190088546, 0.9083977239618538, 0.738416513934868, 0.8806973450124451, 1.0906290439592792, 1.2461975189027226, 1.496275983572826,
        1.5885915338673156, 1.4842142974195722, 1.7667347426399058, 1.4550288621444052, 1.0651798188736166, 1.143318270172714, 1.216215092528441, 1.2845258218014657, 1.3488224665755535,
        1.9259326343836376, 1.9868728791190922, 2.0441767092857317, 2.0982583238541026, 2.1494622372820675, 2.198020785622251, 2.244237863291375 };
    final int n = strikes.length;
    final double[] errors = new double[n];
    Arrays.fill(errors, 0.01); //1% error
    final SmileModelFitter<T> fitter = getFitter(forward, strikes, expiry, vols, errors, getModel());
    LeastSquareResults best = null;
    final BitSet fixed = new BitSet();
    for (int i = 0; i < 5; i++) {
      final double[] start = getRandomStartValues();

      //   int nStartPoints = start.length;
      final LeastSquareResults lsRes = fitter.solve(new DoubleMatrix1D(start), fixed);
      //     System.out.println(this.toString() + lsRes.toString());
      if (best == null) {
        best = lsRes;
      } else {
        if (lsRes.getChiSq() < best.getChiSq()) {
          best = lsRes;
        }
      }
    }
    //
    //    Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = fitter.getModelJacobianFunction();
    //    System.out.println("model Jac: " + jacFunc.evaluate(best.getParameters()));
    //    System.out.println("fit invJac: " + best.getInverseJacobian());
    //    System.out.println("best" + this.toString() + best.toString());
    if (best != null) {
      assertTrue("chi square", best.getChiSq() < 24000); //average error 31.6% - not a good fit, but the data is horrible
    }
  }

  public void testJacobian() {

    final T data = getModelData();

    final int n = data.getNumberOfParameters();
    final double[] temp = new double[n];
    for (int i = 0; i < n; i++) {
      temp[i] = data.getParameter(i);
    }
    final DoubleMatrix1D x = new DoubleMatrix1D(temp);

    testJacobian(x);
  }

  public void testRandomJacobian() {
    for (int i = 0; i < 10; i++) {
      final double[] temp = getRandomStartValues();
      final DoubleMatrix1D x = new DoubleMatrix1D(temp);
      try {
        testJacobian(x);
      } catch (final AssertionError e) {
        System.out.println("Jacobian test failed at " + x.toString());
        throw e;
      }
    }
  }

  private void testJacobian(final DoubleMatrix1D x) {

    final int n = x.getNumberOfElements();

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = _fitter.getModelValueFunction();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = _fitter.getModelJacobianFunction();

    final VectorFieldFirstOrderDifferentiator differ = new VectorFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFuncFD = differ.differentiate(func);

    final DoubleMatrix2D jac = jacFunc.evaluate(x);
    final DoubleMatrix2D jacFD = jacFuncFD.evaluate(x);
    final int rows = jacFD.getNumberOfRows();
    final int cols = jacFD.getNumberOfColumns();

    assertEquals("incorrect rows in FD matrix", _cleanVols.length, rows);
    assertEquals("incorrect columns in FD matrix", n, cols);
    assertEquals("incorrect rows in matrix", rows, jac.getNumberOfRows());
    assertEquals("incorrect columns in matrix", cols, jac.getNumberOfColumns());

    //  System.out.println(jac);
    //   System.out.println(jacFD);
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        assertEquals("row: " + i + ", column: " + j, jacFD.getEntry(i, j), jac.getEntry(i, j), 2e-2);
      }
    }
  }

}
