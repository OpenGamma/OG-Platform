/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.BrentSolver;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.InterpolationBoundedValues;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;

/**
 *  A one-dimensional interpolator using a vector of 12 values as input(seasonalities). The interpolated value of the function
 * <i>y</i> at <i>x</i> between two data points <i>(x<sub>1</sub>,
 * y<sub>1</sub>)</i> and <i>(x<sub>2</sub>, y<sub>2</sub>)</i> is given by a step interpolation using the following data:
 * <br>x<sub>i</sub>=x<sub>1</sub>+(x<sub>2</sub>-x<sub>1</sub>)/12<br>
 * <br>y<sub>i</sub>=y<sub>1</sub>&#928<sub>j=0</sub><sup>i</sup> (1+growth+seasonalities[j])<br>
 * where growth is the solution of the following equation
 * <br>0=y<sub>1</sub>&#928<sub>j=0</sub><sup>i</sup> (1+growth)-y<sub>2</sub><br>
 *
 */
public class LogLinearWithSeasonalitiesInterpolator1D extends Interpolator1D {

  private static final long serialVersionUID = 1L;
  /**
   * The number of month in a year.
   */
  private static final int NB_MONTH = 12;
  /**
   * The seasonal factors.
   */
  private final double[] _seasonalValues;

  /**
   * Construct a LogLinearWithSeasonalitiesInterpolator1D from seasonal values.
   * @param monthlyFactors The seasonal values
   *
   */
  public LogLinearWithSeasonalitiesInterpolator1D(final double[] monthlyFactors) {
    Validate.notNull(monthlyFactors, "Monthly factors");
    Validate.isTrue(monthlyFactors.length == 11, "Monthly factors with incorrect length; should be 11");
    double sum = 0.0;
    final double[] seasonalValues = new double[NB_MONTH];
    for (int loopmonth = 0; loopmonth < NB_MONTH - 1; loopmonth++) {
      seasonalValues[loopmonth] = monthlyFactors[loopmonth];
      sum = sum + monthlyFactors[loopmonth];
    }
    seasonalValues[NB_MONTH - 1] = 1.0 - sum;
    _seasonalValues = seasonalValues;
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle model, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(model, "data bundle");
    final InterpolationBoundedValues boundedValues = model.getBoundedValues(value);
    final Double x1 = boundedValues.getLowerBoundKey();
    final Double y1 = boundedValues.getLowerBoundValue();
    if (model.getLowerBoundIndex(value) == model.size() - 1) {
      return y1;
    }
    final Double x2 = boundedValues.getHigherBoundKey();
    final Double y2 = boundedValues.getHigherBoundValue();

    // nodes and values for the step interpolator
    final double[] nodes = new double[12];
    final double[] values = new double[12];
    nodes[0] = x1;
    values[0] = y1;

    // solver used to find the growth
    final BrentSolver solver = new BrentSolver();

    // definition of the function to minimize
    final Function1D<Double, Double> function = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double x) {
        double result = y1;
        for (int loopmonth = 0; loopmonth < NB_MONTH; loopmonth++) {
          result = result * (1 + x + _seasonalValues[loopmonth]);
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
        nodes[loopmonth] = x1 + loopmonth * (x2 - x1) / 12.0;
        values[loopmonth] = values[loopmonth - 1] * (1 + growth + _seasonalValues[loopmonth]);
      }
    } catch (final MaxIterationsExceededException ex) {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    } catch (final FunctionEvaluationException ex) {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    }

    final Interpolator1DDataBundle dataBundle = getDataBundleFromSortedArrays(nodes, values);
    final StepInterpolator1D stepInterpolator = new StepInterpolator1D();

    return stepInterpolator.interpolate(dataBundle, value);

  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y, true);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    return getFiniteDifferenceSensitivities(data, value);
  }

}
