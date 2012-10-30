/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.univariate.AbstractUnivariateRealOptimizer;
import org.apache.commons.math.optimization.univariate.BrentOptimizer;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;

/**
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/optimization/univariate/BrentOptimizer.html">Commons Math library implementation</a>
 * of Brent minimization.
 */
public class BrentMinimizer1D implements ScalarMinimizer {
  //TODO this class doesn't work properly - e.g. if the curve is flat, the bounded method returns one of the bounds and the unbounded method shoots off to +/-infinity
  private static final GoalType MINIMIZE = GoalType.MINIMIZE;
  private static final AbstractUnivariateRealOptimizer OPTIMIZER = new BrentOptimizer();

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
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
