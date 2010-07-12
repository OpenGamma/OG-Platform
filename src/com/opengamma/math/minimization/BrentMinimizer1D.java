/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.univariate.AbstractUnivariateRealOptimizer;
import org.apache.commons.math.optimization.univariate.BrentOptimizer;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * 
 */
public class BrentMinimizer1D extends Minimizer1D {
  private static final GoalType MINIMIZE = GoalType.MINIMIZE;
  private static final AbstractUnivariateRealOptimizer OPTIMIZER = new BrentOptimizer();

  @Override
  public double[] minimize(final Function1D<Double, Double> f, final Double[] initialPoints) {
    checkInputs(f, initialPoints);
    try {
      final UnivariateRealFunction commonsFunction = CommonsMathWrapper.wrap(f);
      final double a = initialPoints[0];
      final double b = initialPoints[1];
      return new double[] {OPTIMIZER.optimize(commonsFunction, MINIMIZE, a, b)};
    } catch (final FunctionEvaluationException e) {
      throw new MathException(e);
    } catch (final org.apache.commons.math.ConvergenceException e) {
      throw new ConvergenceException(e);
    }
  }
}
