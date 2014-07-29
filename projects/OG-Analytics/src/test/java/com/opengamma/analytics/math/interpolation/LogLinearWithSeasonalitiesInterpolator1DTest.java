/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import java.util.TreeMap;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.BrentSolver;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class LogLinearWithSeasonalitiesInterpolator1DTest {
  private static final int NB_MONTH = 12;
  private static final double[] MONTHLY_FACTORS = {.01, .01, .01, .01, .01, .01, .01, .01, .01, .01, .01 };
  private static final Interpolator1D STEP = new StepInterpolator1D();
  private static final Interpolator1D INTERPOLATOR = new LogLinearWithSeasonalitiesInterpolator1D(MONTHLY_FACTORS);
  private static final Function1D<Double, Double> FUNCTION = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 2 * x + 7;
    }
  };

  private static final Interpolator1DDataBundle MODEL;
  private static final Interpolator1DDataBundle TRANSFORMED_MODEL;
  private static final double EPS = 1e-9;

  static {

    double sum = 0.0;
    final double[] seasonalValues = new double[NB_MONTH];
    for (int loopmonth = 0; loopmonth < NB_MONTH - 1; loopmonth++) {
      seasonalValues[loopmonth] = MONTHLY_FACTORS[loopmonth];
      sum = sum + MONTHLY_FACTORS[loopmonth];
    }
    seasonalValues[NB_MONTH - 1] = 1.0 - sum;

    final TreeMap<Double, Double> data = new TreeMap<>();
    final TreeMap<Double, Double> transformedData = new TreeMap<>();
    double x;
    for (int i = 0; i < 10; i++) {
      x = Double.valueOf(i);
      data.put(x, FUNCTION.evaluate(x));

    }
    MODEL = STEP.getDataBundle(data);

    final double x1 = 3.0;
    final double y1 = FUNCTION.evaluate(x1);
    final double x2 = 4.0;
    final double y2 = FUNCTION.evaluate(x2);
    final double[] nodes = new double[NB_MONTH];
    final double[] values = new double[NB_MONTH];
    nodes[0] = x1;
    values[0] = y1;
    transformedData.put(nodes[0], values[0]);
    // solver used to find the growth
    final BrentSolver solver = new BrentSolver();

    // definition of the function to minimize
    final Function1D<Double, Double> function = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double xf) {
        double result = y1;
        for (int loopmonth = 0; loopmonth < NB_MONTH; loopmonth++) {
          result = result * (1 + xf + seasonalValues[loopmonth]);
        }
        return result - y2;
      }
    };

    // the initial guess for the solver is the solution when all seasonal values are set to 0.
    final double initialGuess = Math.pow(y2 / y1, 1 / 12.0) - 1.0;

    // We solve the equation define by the function and use the result to calculate values, nodes are also calculates.
    final UnivariateRealFunction f = CommonsMathWrapper.wrapUnivariate(function);
    double growth;
    try {
      growth = solver.solve(f, -.5, .5, initialGuess);

      for (int loopmonth = 1; loopmonth < NB_MONTH; loopmonth++) {
        values[loopmonth] = values[loopmonth - 1] * (1 + growth + seasonalValues[loopmonth]);
        nodes[loopmonth] = x1 + loopmonth * (x2 - x1) / NB_MONTH;
        transformedData.put(nodes[loopmonth], values[loopmonth]);
      }
    } catch (final MaxIterationsExceededException ex) {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    } catch (final FunctionEvaluationException ex) {
      // TODO Auto-generated catch block`
      ex.printStackTrace();
    }

    TRANSFORMED_MODEL = INTERPOLATOR.getDataBundle(transformedData);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataBundle() {
    INTERPOLATOR.interpolate(null, 3.4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate(MODEL, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowValue() {
    INTERPOLATOR.interpolate(MODEL, -2.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighValue() {
    INTERPOLATOR.interpolate(MODEL, 12.);
  }

  @Test
  public void testDataBundleType1() {
    assertEquals(INTERPOLATOR.getDataBundle(new double[] {1, 2, 3 }, new double[] {1, 2, 3 }).getClass(), ArrayInterpolator1DDataBundle.class);
  }

  @Test
  public void testDataBundleType2() {
    assertEquals(INTERPOLATOR.getDataBundleFromSortedArrays(new double[] {1, 2, 3 }, new double[] {1, 2, 3 }).getClass(), ArrayInterpolator1DDataBundle.class);
  }

  @Test
  public void test() {
    assertEquals(INTERPOLATOR.interpolate(MODEL, 3.9), STEP.interpolate(TRANSFORMED_MODEL, 3.9), EPS);
  }

}
