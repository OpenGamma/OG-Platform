/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.MultivariateRealOptimizer;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.direct.NelderMead;

import com.opengamma.math.MathException;
import com.opengamma.math.function.FunctionND;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * 
 */
public class NelderMeadDownhillSimplexMinimizer extends SimplexMinimizer {
  private static final MultivariateRealOptimizer OPTIMIZER = new NelderMead();
  private static final GoalType MINIMIZER = GoalType.MINIMIZE;

  //TODO doesn't work for 1D functions
  @Override
  public double[] minimize(final FunctionND<Double, Double> f, final double[] initialPoint) {
    checkInputs(f, initialPoint);
    final MultivariateRealFunction commonsFunction = CommonsMathWrapper.wrap(f);
    try {
      return CommonsMathWrapper.unwrap(OPTIMIZER.optimize(commonsFunction, MINIMIZER, initialPoint));
    } catch (final OptimizationException e) {
      throw new MathException(e);
    } catch (final FunctionEvaluationException e) {
      throw new MathException(e);
    }
  }
}
