/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.apache.commons.lang.Validate;
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
  public Double minimize(final Function1D<Double, Double> f, final Double point1, final Double point2) {
    Validate.notNull(f, "function");
    final UnivariateRealFunction commonsFunction = CommonsMathWrapper.wrap(f);
    try {
      return OPTIMIZER.optimize(commonsFunction, MINIMIZE, point1, point2);
    } catch (final FunctionEvaluationException e) {
      throw new MathException(e);
    } catch (final org.apache.commons.math.ConvergenceException e) {
      throw new ConvergenceException(e);
    }
  }
}
