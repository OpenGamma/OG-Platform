/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * 
 */
public class BrentMinimizer1D implements ScalarMinimizer {
  private static final GoalType MINIMIZE = GoalType.MINIMIZE;
  private static final AbstractUnivariateRealOptimizer OPTIMIZER = new BrentOptimizer();

  @Override
  public double minimize(final Function1D<Double, Double> function, final double startPosition, final double lowerBound, final double upperBound) {
    Validate.notNull(function, "function");
    final UnivariateRealFunction commonsFunction = CommonsMathWrapper.wrapUnivariate(function);
    try {
      return OPTIMIZER.optimize(commonsFunction, MINIMIZE, lowerBound, upperBound, startPosition);
    } catch (final FunctionEvaluationException e) {
      throw new MathException(e);
    } catch (final org.apache.commons.math.ConvergenceException e) {
      throw new MathException(e);
    }
  }

  @Override
  public Double minimize(final Function1D<Double, Double> function, final Double startPosition) {
    Validate.notNull(function, "function");
    final UnivariateRealFunction commonsFunction = CommonsMathWrapper.wrapUnivariate(function);
    try {
      return OPTIMIZER.optimize(commonsFunction, MINIMIZE, -Double.MAX_VALUE, Double.MAX_VALUE, startPosition);
    } catch (final FunctionEvaluationException e) {
      throw new MathException(e);
    } catch (final org.apache.commons.math.ConvergenceException e) {
      throw new MathException(e);
    }
  }
}
