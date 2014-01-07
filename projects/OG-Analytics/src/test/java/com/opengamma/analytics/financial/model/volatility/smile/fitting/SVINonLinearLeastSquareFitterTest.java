/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SVIFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SVIVolatilityFunction;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SVINonLinearLeastSquareFitterTest extends LeastSquareSmileFitterTestCase {
  private static final BlackFunctionData[] SMILE_DATA;
  private static final SVIVolatilityFunction SVI_VOL_FUNCTION = new SVIVolatilityFunction();
  private static final SVIFormulaData SVI_DATA = new SVIFormulaData(0.1, 0.3, -0.5, 0.3, 0.2);
  private static final SVINonLinearLeastSquareFitter FITTER = new SVINonLinearLeastSquareFitter(new NonLinearLeastSquare(DecompositionFactory.SV_COMMONS, MatrixAlgebraFactory.OG_ALGEBRA, 1e-9));
  static final double[] INITIAL_VALUES;

  static {
    final int n = 10;

    SMILE_DATA = new BlackFunctionData[n];
    for (int i = 0; i < n; i++) {
      SMILE_DATA[i] = new BlackFunctionData(FORWARD, DF, SVI_VOL_FUNCTION.getVolatilityFunction(OPTIONS[i], FORWARD).evaluate(SVI_DATA));
      ERRORS[i] = ERROR;
    }
    INITIAL_VALUES = new double[] {0.1, 0.1, 0.01, 0.01, 0.0 };
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
    final LeastSquareResultsWithTransform results = FITTER.getFitResult(OPTIONS, FLAT_DATA, ERRORS, INITIAL_VALUES, FIXED);
    final DoubleMatrix1D parameters = results.getModelParameters();
    assertEquals("a", SIGMA * SIGMA, parameters.getEntry(0), EPS);
    assertEquals("b", 0.0, parameters.getEntry(1), EPS);
    //TODO investigate why a chi^2 of 0 is not reached
    //assertEquals("chi^2", 0.0, results.getChiSq(), EPS);

  }

  @Test
  public void testRecoverParameters() {
    LeastSquareResultsWithTransform results = FITTER.getFitResult(OPTIONS, SMILE_DATA, INITIAL_VALUES, FIXED);
    DoubleMatrix1D fit = results.getModelParameters();
    final double eps = ERRORS[0];
    assertEquals("a", SVI_DATA.getA(), fit.getEntry(0), eps);
    assertEquals("b", SVI_DATA.getB(), fit.getEntry(1), eps);
    assertEquals("rho", SVI_DATA.getRho(), fit.getEntry(2), eps);
    assertEquals("sigma", SVI_DATA.getNu(), fit.getEntry(3), eps * 100);
    assertEquals("m", SVI_DATA.getM(), fit.getEntry(4), eps);
    final double chiSq = results.getChiSq();
    assertEquals("chi^2", 0, chiSq, eps * eps);
    results = FITTER.getFitResult(OPTIONS, SMILE_DATA, ERRORS, INITIAL_VALUES, FIXED);
    fit = results.getModelParameters();
    assertEquals(SVI_DATA.getA(), fit.getEntry(0), eps);
    assertEquals(SVI_DATA.getB(), fit.getEntry(1), eps);
    assertEquals(SVI_DATA.getRho(), fit.getEntry(2), eps);
    assertEquals(SVI_DATA.getNu(), fit.getEntry(3), eps * 100);
    assertEquals(SVI_DATA.getM(), fit.getEntry(4), eps);
    assertEquals(results.getChiSq() * eps * eps, chiSq, eps);
  }
}
