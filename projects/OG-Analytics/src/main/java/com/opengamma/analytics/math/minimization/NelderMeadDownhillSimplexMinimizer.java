/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.MultivariateRealOptimizer;
import org.apache.commons.math.optimization.direct.NelderMead;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;

/** 
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/optimization/direct/NelderMead.html">Commons Math library implementation</a>
 * of the Nelder-Mead downhill simplex method.
 */
public class NelderMeadDownhillSimplexMinimizer implements Minimizer<Function1D<DoubleMatrix1D, Double>, DoubleMatrix1D> {
  private static final GoalType MINIMIZER = GoalType.MINIMIZE;

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix1D minimize(final Function1D<DoubleMatrix1D, Double> function, final DoubleMatrix1D startPosition) {
    Validate.notNull(function, "function");
    Validate.notNull(startPosition, "start position");
    final MultivariateRealOptimizer optimizer = new NelderMead();
    final MultivariateRealFunction commonsFunction = CommonsMathWrapper.wrapMultivariate(function);
    try {
      return new DoubleMatrix1D(CommonsMathWrapper.unwrap(optimizer.optimize(commonsFunction, MINIMIZER, startPosition.getData())));
    } catch (final ConvergenceException e) {
      throw new MathException(e);
    } catch (final FunctionEvaluationException e) {
      throw new MathException(e);
    }
  }
}
