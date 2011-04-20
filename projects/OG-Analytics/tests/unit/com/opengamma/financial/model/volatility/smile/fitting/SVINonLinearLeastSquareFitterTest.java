/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.function.SVIFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SVIVolatilityFunction;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.MatrixAlgebraFactory;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;

/**
 * 
 */
public class SVINonLinearLeastSquareFitterTest extends LeastSquareSmileFitterTestCase {
  private static final BlackFunctionData[] SMILE_DATA;
  private static final SVIVolatilityFunction SVI_VOL_FUNCTION = new SVIVolatilityFunction();
  private static final SVIFormulaData SVI_DATA = new SVIFormulaData(0.6, 0.3, 0.5, 1.5, 70);
  private static final SVINonLinearLeastSquareFitter FITTER = new SVINonLinearLeastSquareFitter(new NonLinearLeastSquare(DecompositionFactory.SV_COMMONS, MatrixAlgebraFactory.OG_ALGEBRA, 1e-9));
  protected static final double[] INITIAL_VALUES;

  static {
    final int n = 100;
    final double kStart = 50;
    final double delta = kStart / n;
    SMILE_DATA = new BlackFunctionData[n];
    for (int i = 0; i < n; i++) {
      OPTIONS[i] = new EuropeanVanillaOption(kStart + i * delta, T, true);
      FLAT_DATA[i] = new BlackFunctionData(FORWARD, DF, SIGMA);
      SMILE_DATA[i] = new BlackFunctionData(FORWARD, DF, SVI_VOL_FUNCTION.getVolatilityFunction(OPTIONS[i]).evaluate(SVI_DATA));
      ERRORS[i] = ERROR;
    }
    INITIAL_VALUES = new double[] {0.01, 0.01, 0.01, 0.01, FORWARD * 0.5};
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSolver() {
    new SVINonLinearLeastSquareFitter(null);
  }

  @Override
  protected LeastSquareSmileFitter getFitter() {
    return FITTER;
  }

  @Override
  protected double[] getInitialValues() {
    return INITIAL_VALUES;
  }

  @Test
  public void testSolutionFlatSurface() {
    final LeastSquareResults results = FITTER.getFitResult(OPTIONS, FLAT_DATA, ERRORS, INITIAL_VALUES, FIXED);
    final DoubleMatrix1D parameters = results.getParameters();
    assertEquals(parameters.getEntry(0), VARIANCE, EPS);
    assertEquals(parameters.getEntry(1), 0, EPS);
    assertEquals(results.getChiSq(), 0, EPS);
  }

  @Test
  public void testRecoverParameters() {
    LeastSquareResults results = FITTER.getFitResult(OPTIONS, SMILE_DATA, INITIAL_VALUES, FIXED);
    DoubleMatrix1D fit = results.getParameters();
    final double eps = ERRORS[0];
    assertEquals(fit.getEntry(0), SVI_DATA.getA(), eps);
    assertEquals(fit.getEntry(1), SVI_DATA.getB(), eps);
    assertEquals(fit.getEntry(2), SVI_DATA.getRho(), eps);
    assertEquals(fit.getEntry(3), SVI_DATA.getSigma(), eps * 100);
    assertEquals(fit.getEntry(4), SVI_DATA.getM(), eps);
    final double chiSq = results.getChiSq();
    assertEquals(chiSq, 0, eps * eps);
    results = FITTER.getFitResult(OPTIONS, SMILE_DATA, ERRORS, INITIAL_VALUES, FIXED);
    fit = results.getParameters();
    assertEquals(fit.getEntry(0), SVI_DATA.getA(), eps);
    assertEquals(fit.getEntry(1), SVI_DATA.getB(), eps);
    assertEquals(fit.getEntry(2), SVI_DATA.getRho(), eps);
    assertEquals(fit.getEntry(3), SVI_DATA.getSigma(), eps * 100);
    assertEquals(fit.getEntry(4), SVI_DATA.getM(), eps);
    assertEquals(results.getChiSq() * eps * eps, chiSq, chiSq);
  }
}
