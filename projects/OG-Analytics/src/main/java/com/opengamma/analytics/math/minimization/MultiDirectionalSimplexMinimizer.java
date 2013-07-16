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
import org.apache.commons.math.optimization.direct.MultiDirectional;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;

/**
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/optimization/direct/MultiDirectional.html">Commons Math library implementation</a>
 * of the multi-directional direct search method.
 */
public class MultiDirectionalSimplexMinimizer implements Minimizer<Function1D<DoubleMatrix1D, Double>, DoubleMatrix1D> {
  private static final GoalType MINIMIZER = GoalType.MINIMIZE;

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix1D minimize(final Function1D<DoubleMatrix1D, Double> f, final DoubleMatrix1D initialPoint) {
    Validate.notNull(f, "function");
    Validate.notNull(initialPoint, "initial point");
    final MultivariateRealOptimizer optimizer = new MultiDirectional();
    final MultivariateRealFunction commons = CommonsMathWrapper.wrapMultivariate(f);
    try {
      return new DoubleMatrix1D(CommonsMathWrapper.unwrap(optimizer.optimize(commons, MINIMIZER, initialPoint.getData())));
    } catch (final ConvergenceException e) {
      throw new MathException(e);
    } catch (final FunctionEvaluationException e) {
      throw new MathException(e);
    }
  }

}
