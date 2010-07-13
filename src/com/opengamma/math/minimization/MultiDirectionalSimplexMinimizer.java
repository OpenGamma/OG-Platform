/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.MultivariateRealOptimizer;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.direct.MultiDirectional;

import com.opengamma.math.MathException;
import com.opengamma.math.function.FunctionND;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * 
 */
public class MultiDirectionalSimplexMinimizer extends SimplexMinimizer {
  private static final MultivariateRealOptimizer OPTIMIZER = new MultiDirectional();
  private static final GoalType MINIMIZER = GoalType.MINIMIZE;

  @Override
  public double[] minimize(final FunctionND<Double, Double> f, final double[] initialPoint) {
    checkInputs(f, initialPoint);
    final MultivariateRealFunction commons = CommonsMathWrapper.wrap(f);
    try {
      return CommonsMathWrapper.unwrap(OPTIMIZER.optimize(commons, MINIMIZER, initialPoint));
    } catch (final OptimizationException e) {
      throw new MathException(e);
    } catch (final FunctionEvaluationException e) {
      throw new MathException(e);
    }
  }

}
