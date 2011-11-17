/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.BitSet;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.financial.model.volatility.smile.function.SmileModelData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
public abstract class SmileModelFitterTest<T extends SmileModelData> {
  private static final double TIME_TO_EXPIRY = 7.0;
  private static final double F = 0.03;
  private static ProbabilityDistribution<Double> RANDOM = new NormalDistribution(0, 1, new MersenneTwister(12));

  //  protected EuropeanOptionMarketData[] _marketData;
  // protected EuropeanOptionMarketData[] _noisyMarketData;
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

  abstract BitSet[] getFixedValues();

  public SmileModelFitterTest() {
    VolatilityFunctionProvider<T> model = getModel();
    T data = getModelData();
    final double[] strikes = new double[] {0.005, 0.01, 0.02, 0.03, 0.04, 0.05, 0.07, 0.1 };
    final int n = strikes.length;
    _noisyVols = new double[n];

    _errors = new double[n];
    _cleanVols = model.getVolatilitySetFunction(F, strikes, TIME_TO_EXPIRY).evaluate(data);
    Arrays.fill(_errors, 0.0001); //1bps error
    for (int i = 0; i < n; i++) {
      _noisyVols[i] = _cleanVols[i] + RANDOM.nextRandom() * _errors[i];
    }

    _fitter = getFitter(F, strikes, TIME_TO_EXPIRY, _cleanVols, _errors, model);
    _nosiyFitter = getFitter(F, strikes, TIME_TO_EXPIRY, _noisyVols, _errors, model);
  }

  @Test
  public void testExactFit() {

    final double[][] start = getStartValues();
    final BitSet[] fixed = getFixedValues();
    int nStartPoints = start.length;
    Validate.isTrue(fixed.length == nStartPoints);
    for (int trys = 0; trys < nStartPoints; trys++) {
      final LeastSquareResults results = _fitter.solve(new DoubleMatrix1D(start[trys]), fixed[trys]);
      final double[] res = results.getParameters().getData();

      assertEquals(0.0, results.getChiSq(), _chiSqEps);
      final int n = res.length;
      T data = getModelData();
      assertEquals(data.getNumberOfparameters(), n);
      for (int i = 0; i < n; i++) {
        assertEquals(data.getParameter(i), res[i], _paramValueEps);
      }
    }
  }

  @Test
  public void testNoisyFit() {

    final double[][] start = getStartValues();
    final BitSet[] fixed = getFixedValues();
    int nStartPoints = start.length;
    Validate.isTrue(fixed.length == nStartPoints);
    for (int trys = 0; trys < nStartPoints; trys++) {
      final LeastSquareResults results = _fitter.solve(new DoubleMatrix1D(start[trys]), fixed[trys]);
      final double[] res = results.getParameters().getData();
      final double eps = 1e-2;
      assertTrue(results.getChiSq() < 7);
      final int n = res.length;
      T data = getModelData();
      assertEquals(data.getNumberOfparameters(), n);
      for (int i = 0; i < n; i++) {
        assertEquals(data.getParameter(i), res[i], eps);
      }
    }
  }

  @Test
      (enabled = false)
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
      long time = timer.finished();
      getlogger().info("time per fit: " + ((double) time) / benchmarkCycles / nStarts + "ms");

    }
  }

  @Test
  public void testJacobian() {

    T data = getModelData();

    final int n = data.getNumberOfparameters();
    final double[] temp = new double[n];
    for (int i = 0; i < n; i++) {
      temp[i] = data.getParameter(i);
    }
    DoubleMatrix1D x = new DoubleMatrix1D(temp);

    Function1D<DoubleMatrix1D, DoubleMatrix1D> func = _fitter.getModelValueFunction();
    Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = _fitter.getModelJacobianFunction();

    VectorFieldFirstOrderDifferentiator differ = new VectorFieldFirstOrderDifferentiator();
    Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFuncFD = differ.differentiate(func);

    DoubleMatrix2D jac = jacFunc.evaluate(x);
    DoubleMatrix2D jacFD = jacFuncFD.evaluate(x);
    final int rows = jacFD.getNumberOfRows();
    final int cols = jacFD.getNumberOfColumns();
    assertEquals("incorrect rows in FD matrix", _cleanVols.length, rows);
    assertEquals("incorrect columns in FD matrix", n, cols);
    assertEquals("incorrect rows in matrix", rows, jac.getNumberOfRows());
    assertEquals("incorrect columns in matrix", cols, jac.getNumberOfColumns());

    //    System.out.println(jac);
    //  System.out.println(jacFD);
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        assertEquals("row: " + i + ", column: " + j, jacFD.getEntry(i, j), jac.getEntry(i, j), 1e-2);
      }
    }
  }

}
