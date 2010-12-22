/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormula;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormulaHagan;
import com.opengamma.math.TrigonometricFunctionUtils;
import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class SABRFittingTest {

  private static final double F = 0.03;
  private static final double T = 7.0;
  private static final double ALPHA = 0.05;
  private static final double BETA = 0.5;
  private static double NU = 0.2;
  private static double RHO = -0.3;

  private static DoubleMatrix1D STRIKES;
  private static DoubleMatrix1D VOLS;
  private static DoubleMatrix1D ERRORS;

  private static final ParameterizedFunction<Double, DoubleMatrix1D, Double> SABR_FUNCTION = new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {
    private final SABRFormula sabr = new SABRFormulaHagan();

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(Double x, DoubleMatrix1D transformedParameters) {
      DoubleMatrix1D parameters = inverseTransformParameters(transformedParameters);
      return sabr.impliedVolitility(F, parameters.getEntry(0), parameters.getEntry(1), parameters.getEntry(2), parameters.getEntry(3), x, T);
    }
  };

  static {
    STRIKES = new DoubleMatrix1D(new double[] {0.005, 0.01, 0.02, 0.03, 0.04, 0.05, 0.07});
    int n = STRIKES.getNumberOfElements();
    double[] temp = new double[n];
    double[] e = new double[n];
    DoubleMatrix1D parms = new DoubleMatrix1D(new double[] {ALPHA, BETA, NU, RHO});
    for (int i = 0; i < n; i++) {
      temp[i] = SABR_FUNCTION.evaluate(STRIKES.getEntry(i), transformParameters(parms));
      e[i] = 0.001;
    }
    VOLS = new DoubleMatrix1D(temp);
    ERRORS = new DoubleMatrix1D(e);
  }

  @Test
  public void TestExactFit() {
    NonLinearLeastSquare solver = new NonLinearLeastSquare();
    DoubleMatrix1D startPos = new DoubleMatrix1D(new double[] {0.3, 0.9, 0.1, 0.0});
    LeastSquareResults lsRes = solver.solve(STRIKES, VOLS, ERRORS, SABR_FUNCTION, transformParameters(startPos));
    DoubleMatrix1D res = inverseTransformParameters(lsRes.getParameters());
    assertEquals(ALPHA, res.getEntry(0), 1e-7);
    assertEquals(BETA, res.getEntry(1), 1e-7);
    assertEquals(NU, res.getEntry(2), 1e-7);
    assertEquals(RHO, res.getEntry(3), 1e-7);
  }

  private static DoubleMatrix1D transformParameters(DoubleMatrix1D x) {
    double[] y = new double[4];
    y[0] = Math.log(x.getEntry(0));
    y[1] = Math.log(x.getEntry(1));
    y[2] = Math.log(x.getEntry(2));
    y[3] = TrigonometricFunctionUtils.atanh(x.getEntry(3)).doubleValue();
    return new DoubleMatrix1D(y);
  }

  private static DoubleMatrix1D inverseTransformParameters(DoubleMatrix1D y) {
    double[] x = new double[4];
    x[0] = Math.exp(y.getEntry(0));
    x[1] = Math.exp(y.getEntry(1));
    x[2] = Math.exp(y.getEntry(2));
    x[3] = TrigonometricFunctionUtils.tanh(y.getEntry(3)).doubleValue();
    return new DoubleMatrix1D(x);
  }

}
