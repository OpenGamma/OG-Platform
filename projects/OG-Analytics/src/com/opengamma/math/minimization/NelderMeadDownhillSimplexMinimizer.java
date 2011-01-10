/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * 
 */
public class NelderMeadDownhillSimplexMinimizer extends SimplexMinimizer {

  private static final GoalType MINIMIZER = GoalType.MINIMIZE;

  @Override
  public DoubleMatrix1D minimize(Function1D<DoubleMatrix1D, Double> function, DoubleMatrix1D startPosition) {
    checkInputs(function, startPosition);
    final MultivariateRealOptimizer optimizer = new NelderMead();
    final MultivariateRealFunction commonsFunction = CommonsMathWrapper.wrapMultivariate(function);
    try {
      return new DoubleMatrix1D(CommonsMathWrapper.unwrap(optimizer.optimize(commonsFunction, MINIMIZER,
          startPosition.getData())));
    } catch (final OptimizationException e) {
      throw new MathException(e);
    } catch (final FunctionEvaluationException e) {
      throw new MathException(e);
    }
  }
}
