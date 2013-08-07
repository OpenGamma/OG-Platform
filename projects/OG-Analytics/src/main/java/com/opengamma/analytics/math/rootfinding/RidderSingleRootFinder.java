/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.RiddersSolver;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;

/**
 * Finds a single root of a function using Ridder's method. This class is a wrapper for the
 * <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/analysis/solvers/RiddersSolver.html">Commons Math library implementation</a>
 * of Ridder's method.
 */
public class RidderSingleRootFinder extends RealSingleRootFinder {

  private static final int MAX_ITER = 10000;
  private final RiddersSolver _ridder = new RiddersSolver();

  /**
   * Sets the accuracy to 10<sup>-15</sup>
   */
  public RidderSingleRootFinder() {
    this(1e-15);
  }

  /**
   * @param functionValueAccuracy The accuracy of the function evaluations.
   */
  public RidderSingleRootFinder(final double functionValueAccuracy) {
    _ridder.setFunctionValueAccuracy(functionValueAccuracy);
    _ridder.setMaximalIterationCount(MAX_ITER);
  }

  /**
   * @param functionValueAccuracy The accuracy of the function evaluations.
   * @param absoluteAccurary The maximum absolute error of the variable.
   */
  public RidderSingleRootFinder(final double functionValueAccuracy, final double absoluteAccurary) {
    _ridder.setAbsoluteAccuracy(absoluteAccurary);
    _ridder.setFunctionValueAccuracy(functionValueAccuracy);
    _ridder.setMaximalIterationCount(MAX_ITER);
  }

  /**
   * {@inheritDoc}
   * @throws MathException If the Commons method could not evaluate the function; if the Commons method could not converge.
   */
  @Override
  public Double getRoot(final Function1D<Double, Double> function, final Double xLow, final Double xHigh) {
    checkInputs(function, xLow, xHigh);
    final UnivariateRealFunction wrapped = CommonsMathWrapper.wrapUnivariate(function);
    try {
      return _ridder.solve(wrapped, xLow, xHigh);
    } catch (final MaxIterationsExceededException e) {
      throw new MathException(e);
    } catch (final FunctionEvaluationException e) {
      throw new MathException(e);
    }
  }
}
